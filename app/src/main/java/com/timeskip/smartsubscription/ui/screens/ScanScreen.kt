package com.timeskip.smartsubscription.ui.screens

import com.timeskip.smartsubscription.i18n.commonText
import com.timeskip.smartsubscription.i18n.scanText
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.timeskip.smartsubscription.AppViewModel
import com.timeskip.smartsubscription.Routes
import com.timeskip.smartsubscription.data.longText
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.scan.ParseEngine
import com.timeskip.smartsubscription.scan.ParsedInvoice
import com.timeskip.smartsubscription.scan.ScanException
import com.timeskip.smartsubscription.scan.ScanState
import com.timeskip.smartsubscription.scan.ScanViewModel
import com.timeskip.smartsubscription.ui.components.BigText
import com.timeskip.smartsubscription.ui.components.CardList
import com.timeskip.smartsubscription.ui.components.Caption
import com.timeskip.smartsubscription.ui.components.Eyebrow
import com.timeskip.smartsubscription.ui.components.InkBlock
import com.timeskip.smartsubscription.ui.components.InkNav
import com.timeskip.smartsubscription.ui.components.InkSub
import com.timeskip.smartsubscription.ui.components.PrimaryButton
import com.timeskip.smartsubscription.ui.components.SectionLabel
import com.timeskip.smartsubscription.ui.components.TextBtn
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SsType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private enum class StepState { DONE, ACTIVE, READY, FAILED, TODO }

/**
 * Reading an invoice: the camera or the photo picker hands over an image, ML Kit reads its text on the
 * device, then Gemini Nano (or pattern matching, where the phone has no model) finds the details.
 * Nothing is saved until the person checks the review form.
 */
@Composable
fun ScanScreen(source: String, nav: NavController, vm: AppViewModel) {
    val ss = LocalSs.current
    val context = LocalContext.current
    val app by vm.state.collectAsStateWithLifecycle()
    val scanVm: ScanViewModel = viewModel()
    val state by scanVm.state.collectAsStateWithLifecycle()
    val fromCamera = source == "camera"
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    fun start(uri: Uri) {
        imageUri = uri
        scanVm.scan(uri, app.currency, app.today)
    }

    // Closing the camera or picker without an image goes back to Add, the way cancelling does.
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingCameraUri
        if (saved && uri != null) start(uri) else if (imageUri == null) nav.popBackStack()
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) start(uri) else if (imageUri == null) nav.popBackStack()
    }

    fun pick() {
        if (fromCamera) {
            val uri = newScanUri(context)
            pendingCameraUri = uri
            camera.launch(uri)
        } else {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // Saved across rotation: the camera or picker is still open when the screen is recreated, so asking
    // again would open a second one on top of it.
    var askedForImage by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!askedForImage) {
            askedForImage = true
            pick()
        }
    }

    val phase = state.phase
    val error = state.error
    val result = state.result
    fun step(n: Int): StepState = when (phase) {
        ScanState.Phase.PICKING -> StepState.TODO
        ScanState.Phase.FAILED -> if (n == 1) StepState.FAILED else StepState.TODO
        ScanState.Phase.READING -> if (n == 1) StepState.ACTIVE else StepState.TODO
        ScanState.Phase.FINDING -> if (n == 1) StepState.DONE else if (n == 2) StepState.ACTIVE else StepState.TODO
        ScanState.Phase.DONE -> if (n == 3) StepState.READY else StepState.DONE
    }

    InkScreen(
        ink = {
            InkBlock(bottom = 18.dp) {
                InkNav(title = scanText.invoice, leading = { TextBtn(commonText.cancel, ss.onInk) { nav.popBackStack() } })
                Eyebrow(if (fromCamera) scanText.fromCamera else scanText.fromPhotos)
                BigText(
                    when (phase) {
                        ScanState.Phase.PICKING -> if (fromCamera) scanText.takePhoto else scanText.choosePhoto
                        ScanState.Phase.READING -> scanText.readingText
                        ScanState.Phase.FINDING -> scanText.findingDetails
                        ScanState.Phase.DONE -> scanText.foundIt
                        ScanState.Phase.FAILED -> if (error?.kind == ScanException.Kind.NO_TEXT) scanText.noTextFound else scanText.couldNotRead
                    },
                )
                InkSub(
                    when (phase) {
                        ScanState.Phase.DONE -> scanText.checkDetails
                        ScanState.Phase.FAILED -> error?.message.orEmpty()
                        else -> scanText.onDevice
                    },
                )
            }
        },
        bottomBar = {
            when (phase) {
                ScanState.Phase.DONE -> {
                    PrimaryButton(scanText.reviewAndSave) {
                        vm.setScanDraft(result)
                        nav.navigate(Routes.form(scanned = true)) { popUpTo(Routes.ADD) }
                    }
                    Caption(scanText.opensForm, Modifier.align(Alignment.CenterHorizontally))
                }
                ScanState.Phase.FAILED, ScanState.Phase.PICKING -> {
                    PrimaryButton(if (fromCamera) scanText.scanAgain else scanText.chooseAnother) {
                        scanVm.reset()
                        pick()
                    }
                    EnterByHand(nav)
                }
                else -> {
                    PrimaryButton(if (phase == ScanState.Phase.READING) scanText.reading else scanText.findingDetailsBusy, enabled = false) {}
                    EnterByHand(nav)
                }
            }
        },
    ) {
        CardList(
            rows = listOf(
                {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Thumbnail(imageUri)
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(if (fromCamera) scanText.yourScan else scanText.yourPhoto, style = SsType.bodyStrong, color = ss.text)
                            Caption(
                                when {
                                    error?.kind == ScanException.Kind.NO_TEXT -> scanText.noReadableText
                                    state.lines > 0 -> scanText.linesRead(state.lines)
                                    else -> scanText.staysOnPhone
                                },
                            )
                        }
                    }
                },
            ),
        )
        CardList(
            rows = listOf(
                {
                    Step(
                        step(1), scanText.stepRead,
                        when (step(1)) {
                            StepState.FAILED -> scanText.nothingReadable
                            StepState.DONE -> scanText.lines(state.lines)
                            else -> scanText.mlKitOnDevice
                        },
                    )
                },
                {
                    Step(
                        step(2), scanText.stepFind,
                        when {
                            result != null -> scanText.found(result.invoice.foundCount) +
                                if (result.invoice.engine == ParseEngine.ON_DEVICE_MODEL) scanText.onDeviceModel else scanText.patternMatching
                            else -> scanText.findHint
                        },
                    )
                },
                { Step(step(3), scanText.stepCheck, scanText.nothingSaved) },
            ),
        )
        if (result != null) {
            Column {
                SectionLabel(scanText.foundOnInvoice, Modifier.padding(top = 0.dp))
                CardList(rows = fieldRows(result.invoice))
            }
        }
    }
}

@Composable
private fun EnterByHand(nav: NavController) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextBtn(scanText.enterByHandInstead) { nav.navigate(Routes.form()) { popUpTo(Routes.ADD) } }
    }
}

private fun fieldRows(invoice: ParsedInvoice): List<@Composable () -> Unit> {
    val amount = invoice.amount?.let { money(it, invoice.currency ?: "USD") + " " + (invoice.currency ?: "") }
    return listOf(
        scanText.provider to invoice.provider,
        scanText.subscription to invoice.subscriptionName,
        scanText.amount to amount,
        scanText.billing to invoice.cadence?.label,
        scanText.invoiceDate to invoice.invoiceDate?.longText(),
        scanText.nextPayment to invoice.nextPaymentDate?.longText(),
    ).map { (k, v) -> @Composable { FieldRow(k, v ?: scanText.notFound, muted = v == null) } } +
        listOf(@Composable { FieldRow(scanText.category, scanText.categoryNotOnInvoice, muted = true) })
}

/** Where the camera writes its photo. Shared through the app's FileProvider; cleared with the cache. */
private fun newScanUri(context: Context): Uri {
    val dir = File(context.cacheDir, "scans").apply { mkdirs() }
    val file = File(dir, "scan-${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, context.packageName + ".export", file)
}

/** A small copy of the chosen image, decoded off the main thread and scaled down to save memory. */
@Composable
private fun Thumbnail(uri: Uri?) {
    val ss = LocalSs.current
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, uri) {
        value = uri?.let { withContext(Dispatchers.IO) { decodeSmall(context, it) } }
    }
    Box(
        Modifier
            .width(52.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(ss.card2)
            .border(1.5.dp, ss.line, RoundedCornerShape(4.dp)),
    ) {
        bitmap?.let { Image(it.asImageBitmap(), contentDescription = scanText.invoiceImage, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(68.dp)) }
    }
}

private fun decodeSmall(context: Context, uri: Uri): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > 256 || bounds.outHeight / sample > 256) sample *= 2
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
} catch (_: Exception) {
    null
}

@Composable
private fun Step(state: StepState, name: String, detail: String) {
    val ss = LocalSs.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            val ring = when (state) {
                StepState.TODO -> ss.line
                StepState.FAILED -> ss.danger
                else -> ss.brand
            }
            val fill = when (state) {
                StepState.DONE -> ss.brand
                StepState.FAILED -> ss.danger
                else -> ss.card
            }
            Box(Modifier.size(14.dp).clip(CircleShape).background(fill).border(2.dp, ring, CircleShape))
            // The one indicator that turns while work runs; it stops the moment the step does.
            if (state == StepState.ACTIVE) {
                CircularProgressIndicator(Modifier.size(26.dp), color = ss.brand, strokeWidth = 2.dp, trackColor = ss.card.copy(alpha = 0f))
            }
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(
                name,
                style = SsType.body.copy(fontSize = 16.sp, fontWeight = if (state == StepState.TODO) FontWeight.Medium else FontWeight.SemiBold),
                color = if (state == StepState.TODO) ss.text2 else ss.text,
            )
            Text(detail, style = SsType.caption, color = if (state == StepState.FAILED) ss.danger else ss.text2)
        }
    }
}

@Composable
private fun FieldRow(key: String, value: String, muted: Boolean = false) {
    val ss = LocalSs.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(key, style = SsType.body, color = ss.text2)
        Text(
            value,
            style = SsType.body.copy(fontWeight = if (muted) FontWeight.Normal else FontWeight.Medium),
            color = if (muted) ss.text2 else ss.text,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}
