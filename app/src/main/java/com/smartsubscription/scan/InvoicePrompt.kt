package com.smartsubscription.scan

import com.smartsubscription.data.Cadence
import com.smartsubscription.data.Currencies
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * The instructions and reply format for Gemini Nano. The rules are the iOS `AppleFoundationTextParser`
 * instructions; Apple's model fills a typed schema, while Nano is asked for JSON and its reply is checked here.
 */
object InvoicePrompt {
    fun instructions(today: LocalDate) = """
        You are an expert invoice parsing assistant.
        You will receive text extracted from an invoice via OCR. Extract structured data.

        CONTEXT:
        - Current system date: $today

        NUMBER FORMAT:
        - Period (.) is the decimal separator.
        - Comma (,) is the thousands separator: "268,129" means 268129.
        - "1,234.56" means 1234.56.

        ZERO-DECIMAL CURRENCIES (no decimal places):
        - VND, JPY, KRW, IDR. Amounts are always whole numbers (e.g. 268129 VND, never 268.13 VND).

        SANITY CHECK:
        - A subscription priced "268.13" VND would be about $0.01 USD, which is unreasonable.
          The correct interpretation is 268129 VND (about $10 USD).
        - Compare parsed amounts against typical subscription prices ($5 to $100 USD equivalent)
          and reconsider the number format if the result looks implausible.

        CURRENCY:
        - currencyCode is the currency the invoice is BILLED in, the one next to the amount actually charged.
          Take it from the symbol or code printed on the invoice, never from the language the invoice is written in.
        - Symbols map as: đ or ₫ or VND -> VND, $ -> USD (unless qualified, e.g. CA$ -> CAD, A$ -> AUD),
          € -> EUR, £ -> GBP, ¥ -> JPY or CNY as the invoice indicates.
        - Never convert anything yourself. Report only what is printed.

        DATES:
        - invoiceDate is the date this charge was made ("Date paid", "Paid on"). With no such date, use the
          start of the service period.
        - nextPaymentDate is the next charge only when the invoice prints it ("Next billing date", "Renews on")
          or prints a service or billing period ("Sep 11 – Oct 11, 2026"), in which case it is the period's end date.
          Leave it empty otherwise; never work it out yourself.

        If a field is missing, make a best guess. If subscriptionName is not stated, use providerName.

        Reply with one JSON object and nothing else, with exactly these keys:
        {"invoiceDate": "YYYY-MM-DD", "nextPaymentDate": "YYYY-MM-DD or empty", "totalAmount": number, "currencyCode": "3-letter ISO 4217 code",
         "subscriptionName": "name including plan, e.g. Netflix Premium", "providerName": "company only, e.g. Netflix",
         "billingCycle": "weekly" | "monthly" | "yearly" | ""}
    """.trimIndent()

    fun prompt(ocrText: String) = "Parse this invoice:\n\n$ocrText"

    /**
     * Reads the model's reply. Returns null when there is no JSON object in it at all; otherwise keeps only the
     * fields that make sense (a known currency, an amount above zero, a real date) and leaves the rest empty.
     */
    fun parseReply(reply: String): ParsedInvoice? {
        val start = reply.indexOf('{')
        val end = reply.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val json = try {
            JSONObject(reply.substring(start, end + 1))
        } catch (_: Exception) {
            return null
        }
        fun text(key: String) = json.optString(key, "").trim().takeIf { it.isNotEmpty() && it != "null" }
        val currency = text("currencyCode")?.uppercase()?.takeIf { code -> Currencies.all.any { it.code == code } }
        val amount = json.optDouble("totalAmount", Double.NaN).takeIf { !it.isNaN() && it > 0 }?.let { value ->
            if (currency != null && Currencies.of(currency).decimals == 0) Math.round(value).toDouble() else value
        }
        fun date(key: String) = text(key)?.let {
            try {
                LocalDate.parse(it.take(10))
            } catch (_: DateTimeParseException) {
                null
            }
        }
        val cadence = when (text("billingCycle")?.lowercase()) {
            "weekly" -> Cadence.WEEKLY
            "monthly" -> Cadence.MONTHLY
            "yearly", "annual", "annually" -> Cadence.YEARLY
            else -> null
        }
        val provider = text("providerName")
        return ParsedInvoice(
            provider = provider,
            subscriptionName = text("subscriptionName") ?: provider,
            amount = amount,
            currency = currency,
            cadence = cadence,
            invoiceDate = date("invoiceDate"),
            nextPaymentDate = date("nextPaymentDate"),
            engine = ParseEngine.ON_DEVICE_MODEL,
        )
    }
}
