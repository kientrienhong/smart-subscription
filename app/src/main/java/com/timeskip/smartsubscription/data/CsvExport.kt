package com.timeskip.smartsubscription.data

import com.timeskip.smartsubscription.i18n.settingsText
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Same columns as the iOS `exportCSV`, then hands the file to the share sheet. */
object CsvExport {
    private fun String.escaped(): String =
        if (any { it == ',' || it == '"' || it == '\n' }) "\"" + replace("\"", "\"\"") + "\"" else this

    fun build(subscriptions: List<Subscription>): String {
        val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return buildString {
            append("Name,Provider,Amount,Currency,Billing Cycle,Status,Start Date,Next Billing Date,Notes\n")
            subscriptions.forEach { s ->
                append(
                    listOf(
                        s.name.escaped(), s.provider.escaped(), s.amount.toString(), s.currency,
                        // English like the header, so the file matches the iOS export in any language.
                        s.cadence.name.lowercase().replaceFirstChar { it.uppercase() },
                        s.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        s.startDate.format(date), s.nextBilling.format(date), s.notes.escaped(),
                    ).joinToString(","),
                )
                append('\n')
            }
        }
    }

    fun share(context: Context, subscriptions: List<Subscription>) {
        val dir = File(context.cacheDir, "export").apply { mkdirs() }
        val file = File(dir, "SmartSubscriptions_Export.csv")
        file.writeText(build(subscriptions))
        val uri = FileProvider.getUriForFile(context, context.packageName + ".export", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, settingsText.exportChooser))
    }
}
