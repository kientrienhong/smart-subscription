package com.timeskip.smartsubscription.scan

import com.timeskip.smartsubscription.i18n.scanText
import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import android.content.res.Resources
import android.os.LocaleList
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** The ML Kit model that reads a script. Latin ships in the APK; the others come from Google Play services. */
enum class OcrScript { LATIN, CHINESE, JAPANESE, KOREAN, DEVANAGARI }

/**
 * The first non-Latin script among [languages], so a phone set to Japanese reads Japanese invoices even though the
 * app itself falls back to English. Each of those models reads Latin text too, so English invoices still work.
 */
fun ocrScriptFor(languages: List<String>): OcrScript = languages.firstNotNullOfOrNull {
    when (it) {
        "zh" -> OcrScript.CHINESE
        "ja" -> OcrScript.JAPANESE
        "ko" -> OcrScript.KOREAN
        "hi", "mr", "ne", "sa" -> OcrScript.DEVANAGARI
        else -> null
    }
} ?: OcrScript.LATIN

private fun recognizerFor(script: OcrScript): TextRecognizer = TextRecognition.getClient(
    when (script) {
        OcrScript.LATIN -> TextRecognizerOptions.DEFAULT_OPTIONS
        OcrScript.CHINESE -> ChineseTextRecognizerOptions.Builder().build()
        OcrScript.JAPANESE -> JapaneseTextRecognizerOptions.Builder().build()
        OcrScript.KOREAN -> KoreanTextRecognizerOptions.Builder().build()
        OcrScript.DEVANAGARI -> DevanagariTextRecognizerOptions.Builder().build()
    }
)

/** A scan that could not produce text to parse. The English messages follow the iOS app's own. */
class ScanException(val kind: Kind, message: String) : Exception(message) {
    enum class Kind { NO_TEXT, UNREADABLE_IMAGE }
}

/** Why the on-device model was not used for a scan, when it was not. Shown on the review form. */
enum class ModelUnavailable { NOT_SUPPORTED, DOWNLOADING, FAILED }

data class ScanResult(
    val ocrLineCount: Int,
    val invoice: ParsedInvoice,
    val modelUnavailable: ModelUnavailable?,
)

/**
 * The Android version of the iOS two-stage pipeline (`OptimizingInvoiceParser`):
 * 1. ML Kit text recognition reads the image on the device.
 * 2. Gemini Nano, where the phone has it, turns the text into fields. Everywhere else, and for any field the
 *    model leaves empty, [PatternInvoiceParser] does it.
 * Nothing leaves the device.
 */
class InvoiceScanner(private val context: Context) {

    suspend fun readText(uri: Uri): String {
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: IOException) {
            throw ScanException(ScanException.Kind.UNREADABLE_IMAGE, scanText.errUnreadableImage)
        }
        val script = ocrScriptFor(userLanguages())
        val text = try {
            recognize(image, script)
        } catch (e: Exception) {
            if (script == OcrScript.LATIN) throw ScanException(ScanException.Kind.UNREADABLE_IMAGE, scanText.errNoExtract)
            // The script's model has not downloaded yet (offline, or Play services still fetching it). Read the Latin text meanwhile.
            try {
                recognize(image, OcrScript.LATIN)
            } catch (e: Exception) {
                throw ScanException(ScanException.Kind.UNREADABLE_IMAGE, scanText.errNoExtract)
            }
        }
        if (text.isBlank()) {
            throw ScanException(ScanException.Kind.NO_TEXT, scanText.errNoText)
        }
        return text
    }

    private suspend fun recognize(image: InputImage, script: OcrScript): String {
        val recognizer = recognizerFor(script)
        try {
            return recognizer.process(image).await().text
        } finally {
            recognizer.close()
        }
    }

    /** Asks Play services for the model the person's languages need, so it is there by the time they pick an image. */
    fun prefetchModel() {
        val script = ocrScriptFor(userLanguages())
        if (script == OcrScript.LATIN) return
        val recognizer = recognizerFor(script)
        ModuleInstall.getClient(context)
            .installModules(ModuleInstallRequest.newBuilder().addApi(recognizer).build())
            .addOnCompleteListener { recognizer.close() }
    }

    /** The app language first, then every language in the system settings. */
    private fun userLanguages(): List<String> {
        val app = LocaleList.getDefault()
        val system = Resources.getSystem().configuration.locales
        return (0 until app.size()).map { app[it].language } + (0 until system.size()).map { system[it].language }
    }

    suspend fun parse(text: String, defaultCurrency: String, today: LocalDate): ScanResult {
        val original = PatternInvoiceParser.parse(text, defaultCurrency, today)
        val patterns = translateToEnglish(text)?.let { original.orElseTranslated(PatternInvoiceParser.parse(it, defaultCurrency, today)) } ?: original
        val (model, unavailable) = parseWithModel(text, today)
        val invoice = model?.orElse(patterns) ?: patterns
        return ScanResult(text.lines().count { it.isNotBlank() }, invoice, unavailable)
    }

    /**
     * The text in English, translated line by line so the parser still sees the invoice's lines. Null when it is
     * English already, the language is unknown, or the translation model has not downloaded yet.
     */
    private suspend fun translateToEnglish(text: String): String? = try {
        val tag = LanguageIdentification.getClient().use { it.identifyLanguage(text).await() }
        val source = TranslateLanguage.fromLanguageTag(tag)
        when {
            source == null || source == TranslateLanguage.ENGLISH -> null
            !RemoteModelManager.getInstance().isModelDownloaded(TranslateRemoteModel.Builder(source).build()).await() -> {
                downloadTranslation(source)
                null
            }
            else -> translatorFor(source).use { translator ->
                text.lines().map { line -> if (line.any { it.isLetter() }) translator.translate(line).await() else line }.joinToString("\n")
            }
        }
    } catch (_: Exception) {
        null
    }

    /** Fetches the ~30 MB model on Wi-Fi in the background, so the next scan in this language gets translated. */
    private fun downloadTranslation(source: String) {
        val translator = translatorFor(source)
        translator.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())
            .addOnCompleteListener { translator.close() }
    }

    private fun translatorFor(source: String) = Translation.getClient(
        TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(TranslateLanguage.ENGLISH).build()
    )

    private suspend fun parseWithModel(text: String, today: LocalDate): Pair<ParsedInvoice?, ModelUnavailable?> {
        val model = try {
            Generation.getClient()
        } catch (_: Exception) {
            return null to ModelUnavailable.NOT_SUPPORTED
        }
        try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> Unit
                FeatureStatus.DOWNLOADABLE -> {
                    startDownload()
                    return null to ModelUnavailable.DOWNLOADING
                }
                FeatureStatus.DOWNLOADING -> return null to ModelUnavailable.DOWNLOADING
                else -> return null to ModelUnavailable.NOT_SUPPORTED
            }
            val request = generateContentRequest(
                SystemInstruction(InvoicePrompt.instructions(today)),
                // Nano's context window is small; an invoice's useful text fits well inside this.
                TextPart(InvoicePrompt.prompt(text.take(MAX_PROMPT_CHARS))),
            ) {
                temperature = 0f
            }
            val reply = model.generateContent(request).candidates.firstOrNull()?.text.orEmpty()
            val parsed = InvoicePrompt.parseReply(reply) ?: return null to ModelUnavailable.FAILED
            return parsed to null
        } catch (_: GenAiException) {
            return null to ModelUnavailable.FAILED
        } catch (e: Exception) {
            // Devices without AICore throw from checkStatus rather than reporting UNAVAILABLE.
            return null to ModelUnavailable.NOT_SUPPORTED
        } finally {
            model.close()
        }
    }

    /** Fetches Gemini Nano in the background so the next scan can use it. Outlives the scan screen on purpose. */
    private fun startDownload() {
        downloadScope.launch {
            val model = Generation.getClient()
            try {
                model.download().first { it is DownloadStatus.DownloadCompleted || it is DownloadStatus.DownloadFailed }
            } catch (_: Exception) {
                // The next scan checks the status again and falls back to patterns until the model is ready.
            } finally {
                model.close()
            }
        }
    }

    companion object {
        private const val MAX_PROMPT_CHARS = 6000
        private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
