package com.smartsubscription.data

import com.smartsubscription.i18n.commonText
import java.time.LocalDate
import java.time.YearMonth

/**
 * The numbers the screens show, computed from the subscription list.
 * Totals are in [currency]. Other currencies are converted with [rates] (today's rate, past payments too);
 * without a rate for one, its subscriptions stay out of the totals and get their own sums ([stillToComeElsewhere]).
 */
class Ledger(
    private val subscriptions: List<Subscription>,
    private val today: LocalDate,
    private val currency: String,
    rates: ExchangeRates? = null,
) {
    private val perBase = rates?.takeIf { it.base == currency }?.perBase.orEmpty()

    val active: List<Subscription> =
        subscriptions.filter { it.status == Status.ACTIVE }.sortedBy { it.nextBilling }

    /** [sub] with its amounts in [currency], or null when there is no rate for its currency. */
    private fun inHome(sub: Subscription): Subscription? {
        if (sub.currency == currency) return sub
        val rate = perBase[sub.currency] ?: return null
        return sub.copy(amount = sub.amount / rate, currency = currency, periods = sub.periods.map { it.copy(amount = it.amount / rate) })
    }

    private val counted = active.mapNotNull(::inHome)
    private val uncounted = active.filter { inHome(it) == null }
    val foreignCount = uncounted.size

    val monthlyAllIn: Double = counted.sumOf { it.perMonth }
    val yearlyAllIn: Double = counted.sumOf { it.perYear }

    private fun inThisMonth(date: LocalDate) = YearMonth.from(date) == YearMonth.from(today)

    // "Still to come" is everything that renews later this month; "charged" is the monthly
    // subscriptions whose next date has already moved past this month.
    private val toCome = counted.filter { inThisMonth(it.nextBilling) }
    private val charged = counted.filter { it.cadence == Cadence.MONTHLY && !inThisMonth(it.nextBilling) }
    val stillToCome: Double = toCome.sumOf { it.amount }
    val chargedThisMonth: Double = charged.sumOf { it.amount }
    val yearlyToCome: Double = toCome.filter { it.cadence == Cadence.YEARLY }.sumOf { it.amount }
    val monthSum: Double get() = stillToCome + chargedThisMonth

    /** Sums per currency for what could not be converted, e.g. "€9.99 + ₫268,129". Empty when nothing is left out. */
    private fun elsewhere(pick: (Subscription) -> Double): String =
        uncounted.groupBy { it.currency }.map { (code, subs) -> code to subs.sumOf(pick) }
            .filter { it.second > 0 }.joinToString(" + ") { (code, sum) -> money(sum, code) }

    val stillToComeElsewhere: String = elsewhere { if (inThisMonth(it.nextBilling)) it.amount else 0.0 }
    val yearlyElsewhere: String = elsewhere { it.perYear }

    data class RailGroup(val title: String, val total: String, val rows: List<Subscription>)

    data class Rail(val groups: List<RailGroup>, val laterCount: Int, val laterTotal: Double?)

    /** Home's rail: rows grouped by when they land. This month's view holds the next ~6 weeks. */
    fun rail(yearAhead: Boolean, categoryId: String?): Rail {
        val filtered = active.filter { categoryId == null || it.categoryId == categoryId }
        val (shown, later) = if (yearAhead) filtered to emptyList() else filtered.partition {
            it.daysUntilNext(today) <= 45 || inThisMonth(it.nextBilling)
        }
        val groups = shown.groupBy { groupLabel(it, yearAhead) }.map { (title, rows) ->
            val converted = rows.map(::inHome)
            val total = if (converted.all { it != null }) {
                money(converted.sumOf { it!!.amount }, currency)
            } else {
                rows.groupBy { it.currency }.map { (code, list) -> money(list.sumOf { it.amount }, code) }.joinToString(" + ")
            }
            RailGroup(title, total, rows)
        }
        val laterConverted = later.map(::inHome)
        val laterTotal = if (laterConverted.all { it != null }) laterConverted.sumOf { it!!.amount } else null
        return Rail(groups, later.size, laterTotal)
    }

    fun groupLabel(sub: Subscription, yearAhead: Boolean): String = groupLabelFor(sub.nextBilling, today, yearAhead)

    // ---------- analytics ----------

    data class Bucket(
        val month: YearMonth,
        val tense: Tense,
        var paidMonthly: Double = 0.0,
        var paidYearly: Double = 0.0,
        var expectedMonthly: Double = 0.0,
        var expectedYearly: Double = 0.0,
        var payments: Int = 0,
    ) {
        val total get() = paidMonthly + paidYearly + expectedMonthly + expectedYearly
    }

    enum class Tense { PAST, NOW, FUTURE }

    data class Analytics(
        val buckets: List<Bucket>,
        val paid: Double,
        val paidYearly: Double,
        val expected: Double,
        val expectedYearly: Double,
        val payments: Int,
    )

    /** [window] months behind today and [window] ahead, each split into paid and expected, monthly and yearly. */
    fun analytics(window: Int): Analytics {
        val now = YearMonth.from(today)
        val buckets = buildList {
            for (i in window - 1 downTo 1) add(Bucket(now.minusMonths(i.toLong()), Tense.PAST))
            add(Bucket(now, Tense.NOW))
            for (i in 1..window) add(Bucket(now.plusMonths(i.toLong()), Tense.FUTURE))
        }
        val byMonth = buckets.associateBy { it.month }

        counted.forEach { sub ->
            val first = YearMonth.from(sub.nextBilling)
            buckets.filter { it.tense != Tense.PAST }.forEach { b ->
                val diff = b.month.monthKey() - first.monthKey()
                when (sub.cadence) {
                    Cadence.YEARLY -> if (diff >= 0 && diff % 12 == 0) b.expectedYearly += sub.amount
                    Cadence.MONTHLY -> if (diff >= 0) b.expectedMonthly += sub.amount
                    Cadence.WEEKLY -> if (diff >= 0) b.expectedMonthly += sub.perMonth
                }
            }
        }
        subscriptions.mapNotNull(::inHome).forEach { sub ->
            sub.paidPeriods.forEach { p ->
                val b = byMonth[YearMonth.from(p.paidOn!!)] ?: return@forEach
                if (b.tense == Tense.FUTURE) return@forEach
                if (sub.cadence == Cadence.YEARLY) b.paidYearly += p.amount else b.paidMonthly += p.amount
                b.payments += 1
            }
        }
        val past = buckets.filter { it.tense != Tense.FUTURE }
        val ahead = buckets.filter { it.tense != Tense.PAST }
        return Analytics(
            buckets = buckets,
            paid = past.sumOf { it.paidMonthly + it.paidYearly },
            paidYearly = past.sumOf { it.paidYearly },
            expected = ahead.sumOf { it.expectedMonthly + it.expectedYearly },
            expectedYearly = ahead.sumOf { it.expectedYearly },
            payments = past.sumOf { it.payments },
        )
    }

    data class ProviderShare(val name: String, val perMonth: Double, val yearlyPrice: Double, val allYearly: Boolean)

    fun providers(): List<ProviderShare> = counted.groupBy { it.provider }.map { (name, subs) ->
        ProviderShare(
            name = name,
            perMonth = subs.sumOf { it.perMonth },
            yearlyPrice = subs.filter { it.cadence == Cadence.YEARLY }.sumOf { it.amount },
            allYearly = subs.all { it.cadence == Cadence.YEARLY },
        )
    }.sortedByDescending { it.perMonth }
}

private fun YearMonth.monthKey() = year * 12 + monthValue - 1

fun groupLabelFor(date: LocalDate, today: LocalDate, yearAhead: Boolean): String {
    val days = java.time.temporal.ChronoUnit.DAYS.between(today, date)
    if (!yearAhead && days >= 0) {
        if (days <= 6) return commonText.thisWeek
        if (days <= 13) return commonText.nextWeek
    }
    return date.monthLong() + if (date.year != today.year) " ${date.year}" else ""
}
