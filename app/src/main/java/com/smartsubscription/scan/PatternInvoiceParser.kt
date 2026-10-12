package com.smartsubscription.scan

import com.smartsubscription.data.Cadence
import com.smartsubscription.data.Currencies
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Pulls invoice fields out of OCR text with plain pattern matching. It runs on every phone, so it is the
 * fallback when Gemini Nano is not available, and it fills any field the model leaves empty.
 * It is less reliable than the model, which is why the review form asks the person to check every field.
 */
object PatternInvoiceParser {

    /** The iOS app's `KnownService` names, longest first so "Apple TV+" wins over "Apple". */
    private val knownServices = listOf(
        "Netflix", "Spotify", "YouTube Premium", "YouTube", "Apple Music", "Apple TV+", "iCloud+", "iCloud", "Apple",
        "Amazon Prime", "Disney+", "HBO Max", "Hulu", "Microsoft 365", "Dropbox", "Google One", "Notion", "Slack",
        "Zoom", "GitHub", "Adobe", "Canva", "Figma", "ChatGPT", "OpenAI", "Claude", "LinkedIn", "Twitch", "Discord",
    ).sortedByDescending { it.length }

    private val planWords = listOf("premium", "family", "plus", "pro", "individual", "standard", "basic", "duo", "student", "team", "personal")

    // Symbols and codes that mark an amount, longest first so "CA$" is tried before "$".
    private val symbols = linkedMapOf(
        "US$" to "USD", "CA$" to "CAD", "NZ$" to "NZD", "HK$" to "HKD", "MX$" to "MXN", "A$" to "AUD", "S$" to "SGD",
        "R$" to "BRL", "$" to "USD", "€" to "EUR", "£" to "GBP", "¥" to "JPY", "₹" to "INR", "₩" to "KRW",
        "₫" to "VND", "đ" to "VND", "₽" to "RUB", "₺" to "TRY", "฿" to "THB", "Rp" to "IDR", "zł" to "PLN",
    )
    private val codes = Currencies.all.map { it.code }.toSet()

    private val markerPattern = (symbols.keys.map { Regex.escape(it) } + codes.map { "\\b$it\\b" }).joinToString("|")
    private val number = """\d{1,3}(?:[,.\s]\d{3})+(?:[.,]\d{1,2})?|\d+(?:[.,]\d{1,2})?"""
    private val amountRegex = Regex("""(?i)($markerPattern)?\s?($number)\s?($markerPattern)?""")

    private val totalWords = listOf("total", "amount due", "amount paid", "amount charged", "charged", "you paid", "grand total")
    private val notTotalWords = listOf("subtotal", "sub-total", "tax", "vat", "discount", "fee", "credit")

    data class Amount(val value: Double, val currency: String?, val score: Int)

    fun parse(text: String, fallbackCurrency: String, today: LocalDate): ParsedInvoice {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val amount = findAmount(lines)
        val currency = amount?.currency ?: mostCommonCurrency(text) ?: fallbackCurrency
        val (provider, name) = findProvider(lines)
        val period = findServicePeriod(lines, currency)
        // The period's own dates would otherwise be read as the charge date.
        val dateLines = lines.filter { periodIn(it, currency) == null }
        return ParsedInvoice(
            provider = provider,
            subscriptionName = name,
            amount = amount?.value?.let { normalizeForCurrency(it, currency) },
            currency = currency,
            cadence = period?.let(::cadenceOf) ?: findCadence(text),
            invoiceDate = findDate(dateLines, currency, today) ?: period?.start,
            nextPaymentDate = findDate(dateLines, currency, today, next = true) ?: period?.endInclusive,
            engine = ParseEngine.PATTERNS,
        )
    }

    // ---------- amount ----------

    fun findAmount(lines: List<String>): Amount? {
        val candidates = lines.flatMap { line ->
            val lower = line.lowercase(Locale.ROOT)
            val lineScore = when {
                notTotalWords.any { lower.contains(it) } -> -2
                totalWords.any { lower.contains(it) } -> 3
                else -> 0
            }
            amountRegex.findAll(line).mapNotNull { m ->
                val marker = m.groupValues[1].ifEmpty { m.groupValues[3] }
                // A bare number is only an amount on a line that says it is a total; otherwise it is a date, an ID or a count.
                if (marker.isEmpty() && lineScore <= 0) return@mapNotNull null
                val currency = currencyFor(marker)
                val value = toNumber(m.groupValues[2], currency) ?: return@mapNotNull null
                if (value <= 0) return@mapNotNull null
                Amount(value, currency, lineScore + (if (marker.isNotEmpty()) 1 else 0))
            }.toList()
        }
        return candidates.maxWithOrNull(compareBy<Amount> { it.score }.thenBy { it.value })
    }

    private fun currencyFor(marker: String): String? {
        if (marker.isEmpty()) return null
        symbols[marker]?.let { return it }
        val upper = marker.uppercase(Locale.ROOT)
        return upper.takeIf { it in codes }
    }

    private fun mostCommonCurrency(text: String): String? =
        Regex("(?i)$markerPattern").findAll(text).mapNotNull { currencyFor(it.value) }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

    /**
     * Reads "1,234.56", "1.234,56", "9,99" and "268,129". The last separator is the decimal point when two or
     * fewer digits follow it; otherwise every separator is a thousands separator. Whole-number currencies
     * (VND, JPY, KRW, IDR) never have decimals.
     */
    fun toNumber(raw: String, currency: String?): Double? {
        val s = raw.replace(" ", "")
        val zeroDecimal = currency != null && Currencies.of(currency).decimals == 0
        val lastSep = s.indexOfLast { it == ',' || it == '.' }
        if (lastSep < 0) return s.toDoubleOrNull()
        val digitsAfter = s.length - lastSep - 1
        val whole = s.substring(0, lastSep).replace(",", "").replace(".", "")
        val tail = s.substring(lastSep + 1)
        return if (!zeroDecimal && digitsAfter <= 2) "$whole.$tail".toDoubleOrNull() else (whole + tail).toDoubleOrNull()
    }

    private fun normalizeForCurrency(value: Double, currency: String): Double =
        if (Currencies.of(currency).decimals == 0) Math.round(value).toDouble() else value

    // ---------- cadence ----------

    fun findCadence(text: String): Cadence? {
        val lower = text.lowercase(Locale.ROOT)
        fun count(words: List<String>) = words.sumOf { Regex(Regex.escape(it)).findAll(lower).count() }
        val yearly = count(listOf("annual", "yearly", "/yr", "/year", "per year", "a year", "12 months", "1 year"))
        val monthly = count(listOf("monthly", "/mo", "/month", "per month", "a month", "1 month"))
        val weekly = count(listOf("weekly", "/wk", "/week", "per week", "a week"))
        val best = maxOf(yearly, monthly, weekly)
        return when {
            best == 0 -> null
            best == yearly -> Cadence.YEARLY
            best == monthly -> Cadence.MONTHLY
            else -> Cadence.WEEKLY
        }
    }

    // ---------- date ----------

    private val monthNames = "jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec|january|february|march|april|june|july|august|september|october|november|december"
    private val isoDate = Regex("""\b(\d{4})-(\d{1,2})-(\d{1,2})\b""")
    private val monthFirst = Regex("""(?i)\b($monthNames)\.?\s+(\d{1,2})(?:st|nd|rd|th)?,?\s+(\d{4})\b""")
    private val dayFirst = Regex("""(?i)\b(\d{1,2})(?:st|nd|rd|th)?\s+(?:of\s+)?($monthNames)\.?,?\s+(\d{4})\b""")
    // The same separator twice: "11/10/2026", "11.10.2026", "11-10-2026".
    private val numeric = Regex("""\b(\d{1,2})([/.-])(\d{1,2})\2(\d{4}|\d{2})\b""")
    private val dateWords = listOf("date", "invoice", "billed", "paid", "issued")
    private val nextWords = listOf("next", "renew")

    /** The charge date, or with [next] the upcoming one, read only from lines that say "next" or "renews". */
    fun findDate(lines: List<String>, currency: String, today: LocalDate, next: Boolean = false): LocalDate? {
        val found = lines.indices.flatMap { i ->
            val label = labelOf(lines, i)
            if (nextWords.any { label.contains(it) } != next) return@flatMap emptyList()
            val preferred = dateWords.any { label.contains(it) }
            datesIn(lines[i], currency).map { it to preferred }
        }
        // Prefer a date on a line that names it ("Invoice date", "Paid on"), and ignore ones far from today.
        val plausible = found.filter { (d, _) -> d.isAfter(today.minusYears(3)) && d.isBefore(today.plusYears(2)) }
        return (plausible.firstOrNull { it.second } ?: plausible.firstOrNull())?.first
    }

    /** ML Kit often reads a label and its value as two lines ("Date paid" / "October 11, 2026"), so a bare date borrows the line above. */
    private fun labelOf(lines: List<String>, i: Int): String {
        val line = lines[i].lowercase(Locale.ROOT)
        if ((dateWords + nextWords).any { line.contains(it) }) return line
        return (lines.getOrNull(i - 1).orEmpty().lowercase(Locale.ROOT) + " " + line)
    }

    // "Oct 11 – Nov 11, 2026" and "11 Oct – 11 Nov 2026" print the year once; these copy it onto the start.
    private val openRangeMonthFirst = Regex("""(?i)\b($monthNames)\.?\s+(\d{1,2})(?:st|nd|rd|th)?\s*([-–—]|to|through|until)\s*(?=(?:$monthNames)\.?\s+\d{1,2}(?:st|nd|rd|th)?,?\s+(\d{4}))""")
    private val openRangeDayFirst = Regex("""(?i)\b(\d{1,2})(?:st|nd|rd|th)?\s+($monthNames)\.?\s*([-–—]|to|through|until)\s*(?=\d{1,2}(?:st|nd|rd|th)?\s+(?:$monthNames)\.?,?\s+(\d{4}))""")
    // "11/09 to 11/10/2026" does the same.
    private val openRangeNumeric = Regex("""(?i)\b(\d{1,2})([/.])(\d{1,2})\s*([-–—]|to|through|until)\s*(?=\d{1,2}\2\d{1,2}\2(\d{4}|\d{2})\b)""")
    // A plain hyphen needs spaces around it, or a numeric date on each side ("10/27/25-11/26/25"), so ISO dates are not split.
    private val rangeMark = Regex("""(?i)\s-\s|(?<=\d)-(?=\d{1,2}[/.]\d)|[–—]|\bto\b|\bthrough\b|\buntil\b""")

    /** A service or billing period such as "Sep 11 – Oct 11, 2026", as start..end. */
    fun findServicePeriod(lines: List<String>, currency: String): ClosedRange<LocalDate>? =
        lines.firstNotNullOfOrNull { periodIn(it, currency) }

    private fun periodIn(line: String, currency: String): ClosedRange<LocalDate>? {
        val filled = line.replace(openRangeMonthFirst, "$1 $2, $4 $3 ").replace(openRangeDayFirst, "$1 $2 $4 $3 ")
            .replace(openRangeNumeric, "$1$2$3$2$5 $4 ")
        val parts = filled.split(rangeMark, limit = 2)
        if (parts.size < 2) return null
        var start = datesIn(parts[0], currency).singleOrNull() ?: return null
        val end = datesIn(parts[1], currency).firstOrNull() ?: return null
        // "Dec 11 – Jan 11, 2027" copied 2027 onto December.
        if (start.isAfter(end)) start = start.minusYears(1)
        return (start..end).takeIf { ChronoUnit.DAYS.between(start, end) in 1..400 }
    }

    private fun cadenceOf(period: ClosedRange<LocalDate>): Cadence? = when (ChronoUnit.DAYS.between(period.start, period.endInclusive)) {
        in 6L..8L -> Cadence.WEEKLY
        in 27L..32L -> Cadence.MONTHLY
        in 360L..367L -> Cadence.YEARLY
        else -> null
    }

    private fun datesIn(line: String, currency: String): List<LocalDate> = buildList {
        isoDate.findAll(line).forEach { m -> date(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())?.let(::add) }
        monthFirst.findAll(line).forEach { m -> monthIndex(m.groupValues[1])?.let { mo -> date(m.groupValues[3].toInt(), mo, m.groupValues[2].toInt())?.let(::add) } }
        dayFirst.findAll(line).forEach { m -> monthIndex(m.groupValues[2])?.let { mo -> date(m.groupValues[3].toInt(), mo, m.groupValues[1].toInt())?.let(::add) } }
        numeric.findAll(line).forEach { m ->
            val a = m.groupValues[1].toInt()
            val b = m.groupValues[3].toInt()
            // "10/27/25" is 2025.
            val y = m.groupValues[4].toInt().let { if (it < 100) 2000 + it else it }
            // 20/09 can only be day first and 09/20 only month first. When both work, US-dollar invoices
            // are usually month first and most others day first.
            val monthFirstReading = when {
                a > 12 -> false
                b > 12 -> true
                else -> currency == "USD"
            }
            (if (monthFirstReading) date(y, a, b) else date(y, b, a))?.let(::add)
        }
    }

    private fun monthIndex(name: String): Int? {
        val key = name.lowercase(Locale.ROOT).take(3)
        return listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec").indexOf(key).takeIf { it >= 0 }?.plus(1)
    }

    private fun date(y: Int, m: Int, d: Int): LocalDate? = try {
        LocalDate.parse("%04d-%02d-%02d".format(y, m, d), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: DateTimeParseException) {
        null
    }

    // ---------- provider and name ----------

    /** Returns (provider, subscription name). The name includes a plan word when the invoice prints one next to the provider. */
    fun findProvider(lines: List<String>): Pair<String?, String?> {
        val text = lines.joinToString("\n")
        val service = knownServices.firstOrNull { Regex("(?i)(^|[^\\p{L}])${Regex.escape(it)}($|[^\\p{L}])").containsMatchIn(text) }
        if (service != null) {
            val planLine = lines.firstOrNull { line ->
                val lower = line.lowercase(Locale.ROOT)
                lower.contains(service.lowercase(Locale.ROOT)) && planWords.any { Regex("\\b$it\\b").containsMatchIn(lower) }
            }
            val name = planLine?.let { tidyName(it) }?.takeIf { it.length <= 40 } ?: service
            val provider = when {
                service.startsWith("Apple") || service.startsWith("iCloud") -> "Apple"
                service.startsWith("YouTube") -> "YouTube"
                service == "ChatGPT" -> "OpenAI"
                else -> service
            }
            return provider to name
        }
        // No known service: the first line with letters that is not a date, amount or label is usually the vendor.
        val vendor = lines.firstOrNull { line ->
            val lower = line.lowercase(Locale.ROOT)
            line.any { it.isLetter() } && line.length in 2..40 && line.count { it.isDigit() } * 3 < line.length &&
                dateWords.none { lower.startsWith(it) } && listOf("receipt", "invoice", "bill to").none { lower.contains(it) }
        }?.let { tidyName(it) }
        return vendor to vendor
    }

    private fun tidyName(line: String): String =
        line.replace(Regex("""(?i)\s*($markerPattern)?\s?($number)\s?($markerPattern)?\s*$"""), "").trim().trimEnd(':', '-', '·').trim()
}
