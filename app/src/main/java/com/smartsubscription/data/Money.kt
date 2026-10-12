package com.smartsubscription.data

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import com.smartsubscription.i18n.commonText
import com.smartsubscription.i18n.isVietnamese
import java.time.LocalDate
import java.util.Locale

data class CurrencyInfo(val code: String, private val englishName: String, val symbol: String, val decimals: Int) {
    /** The platform's own name in Vietnamese ("Đô la Mỹ"); the iOS app's names otherwise. */
    val name: String
        get() = if (isVietnamese) java.util.Currency.getInstance(code).getDisplayName(Locale.getDefault()) else englishName
}

/** The iOS app's `CurrencyInfo.commonCurrencies`, in its order. */
object Currencies {
    private val zeroDecimal = setOf("VND", "JPY", "KRW", "IDR")

    val all: List<CurrencyInfo> = listOf(
        "USD" to ("US Dollar" to "$"), "EUR" to ("Euro" to "€"), "GBP" to ("British Pound" to "£"),
        "JPY" to ("Japanese Yen" to "¥"), "CAD" to ("Canadian Dollar" to "CA$"), "AUD" to ("Australian Dollar" to "A$"),
        "CHF" to ("Swiss Franc" to "CHF"), "CNY" to ("Chinese Yuan" to "¥"), "INR" to ("Indian Rupee" to "₹"),
        "MXN" to ("Mexican Peso" to "MX$"), "BRL" to ("Brazilian Real" to "R$"), "KRW" to ("South Korean Won" to "₩"),
        "SGD" to ("Singapore Dollar" to "S$"), "HKD" to ("Hong Kong Dollar" to "HK$"), "NOK" to ("Norwegian Krone" to "kr"),
        "SEK" to ("Swedish Krona" to "kr"), "DKK" to ("Danish Krone" to "kr"), "NZD" to ("New Zealand Dollar" to "NZ$"),
        "ZAR" to ("South African Rand" to "R"), "RUB" to ("Russian Ruble" to "₽"), "TRY" to ("Turkish Lira" to "₺"),
        "PLN" to ("Polish Zloty" to "zł"), "THB" to ("Thai Baht" to "฿"), "IDR" to ("Indonesian Rupiah" to "Rp"),
        "VND" to ("Vietnamese Dong" to "₫"),
    ).map { (code, rest) -> CurrencyInfo(code, rest.first, rest.second, if (code in zeroDecimal) 0 else 2) }

    private val byCode = all.associateBy { it.code }

    fun of(code: String): CurrencyInfo = byCode[code] ?: byCode.getValue("USD")
}

fun money(amount: Double, code: String = "USD"): String {
    val c = Currencies.of(code)
    val pattern = if (c.decimals == 0) "#,##0" else "#,##0.00"
    val format = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).apply { roundingMode = RoundingMode.HALF_UP }
    // Letter symbols ("CHF", "kr") need a space before the number; "$" and "€" sit flush.
    val sep = if (c.symbol.last().isLetter()) " " else ""
    return c.symbol + sep + format.format(amount)
}

/** Parses what a person typed. Commas are thousands separators; whole-number currencies refuse a decimal point. */
fun parseAmount(raw: String, code: String): Double? {
    val clean = raw.replace(",", "").trim()
    if (clean.isEmpty() || clean == "." || !Regex("""^\d*(\.\d*)?$""").matches(clean)) return null
    if (Currencies.of(code).decimals == 0 && clean.contains('.')) return null
    return clean.toDoubleOrNull()
}

fun LocalDate.shortText(): String = commonText.shortDate(month, dayOfMonth)
fun LocalDate.longText(): String = commonText.longDate(month, dayOfMonth, year)
fun LocalDate.monthLong(): String = commonText.monthLong(month)
fun LocalDate.monthShort(): String = commonText.monthShort(month)
fun LocalDate.dayShort(): String = commonText.dayShort(dayOfWeek)
fun java.time.YearMonth.monthShort(): String = commonText.monthShort(month)
fun LocalDate.monthKey(): Int = year * 12 + monthValue - 1

fun dueText(days: Long): String = when {
    days == 0L -> commonText.today
    days == 1L -> commonText.tomorrow
    days < 0 -> commonText.overdue
    else -> commonText.inDays(days)
}

/** English only; Vietnamese has no plural, so translated text builds its counts itself. */
fun plural(n: Int, one: String, many: String = one + "s") = "$n " + if (n == 1) one else many
