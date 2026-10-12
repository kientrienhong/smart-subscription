package com.timeskip.smartsubscription.scan

import com.timeskip.smartsubscription.data.Cadence
import com.timeskip.smartsubscription.data.step
import java.time.LocalDate

/** Which step turned the invoice text into fields. The review form says so, because their accuracy differs. */
enum class ParseEngine { ON_DEVICE_MODEL, PATTERNS }

/**
 * What the scan found on an invoice. Any field can be missing; the form leaves those for the person to fill.
 * Mirrors the iOS `ParsedInvoice` schema, minus line items and the second currency, which the form does not use yet.
 */
data class ParsedInvoice(
    val provider: String?,
    val subscriptionName: String?,
    val amount: Double?,
    val currency: String?,
    val cadence: Cadence?,
    val invoiceDate: LocalDate?,
    /** The next charge, when the invoice prints it ("Next billing date", "Renews on"). */
    val nextPaymentDate: LocalDate?,
    val engine: ParseEngine,
) {
    val foundCount: Int
        get() = listOf(provider, subscriptionName, amount, cadence, invoiceDate).count { it != null }

    /** Model fields win; patterns fill whatever the model left out. */
    fun orElse(fallback: ParsedInvoice) = copy(
        provider = provider ?: fallback.provider,
        subscriptionName = subscriptionName ?: fallback.subscriptionName,
        amount = amount ?: fallback.amount,
        currency = currency ?: fallback.currency,
        cadence = cadence ?: fallback.cadence,
        invoiceDate = invoiceDate ?: fallback.invoiceDate,
        nextPaymentDate = nextPaymentDate ?: fallback.nextPaymentDate,
    )

    /** Fills gaps from a parse of the English translation. Amount and currency stay from the original, since translation can shift number separators. */
    fun orElseTranslated(translated: ParsedInvoice) = orElse(translated.copy(amount = null, currency = null))
}

/**
 * Port of iOS `BillingCalculator.calculateNextBillingDate`: step the invoice date forward by the
 * billing cycle until it lands after today. An invoice dated in the future is itself the next date.
 */
fun nextBillingDate(start: LocalDate, cadence: Cadence, today: LocalDate): LocalDate {
    if (start.isAfter(today)) return start
    var steps = 1L
    var next = cadence.step(start, steps)
    // Stepping from the start each time keeps month ends honest: Jan 31 goes to Feb 28, then Mar 31.
    while (!next.isAfter(today) && steps < 10_000) {
        steps++
        next = cadence.step(start, steps)
    }
    return next
}
