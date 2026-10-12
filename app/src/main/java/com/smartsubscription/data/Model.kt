package com.smartsubscription.data

import com.smartsubscription.i18n.commonText
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Cadence {
    WEEKLY, MONTHLY, YEARLY;

    val label: String get() = commonText.cadenceLabel(this)
    /** After a price: "a month". */
    val aWord: String get() = commonText.cadenceA(this)
    /** Under a big price: "per month". */
    val perText: String get() = commonText.cadencePer(this)
}

/** Mirrors `Subscription.Status` on iOS. Only [ACTIVE] bills. */
enum class Status { ACTIVE, PAUSED, CANCELLED, ARCHIVED }

data class Category(
    val id: String,
    val name: String,
    /** The SF Symbol name iOS stores in `iconName`; [com.smartsubscription.ui.components.CategoryGlyph] draws it. */
    val icon: String,
)

/** One billing period. The newest period of an active subscription is the current cycle, unpaid until recorded. */
data class BillingPeriod(
    val start: LocalDate,
    val end: LocalDate,
    val amount: Double,
    val paidOn: LocalDate?,
)

data class Subscription(
    val id: String,
    val name: String,
    val provider: String,
    val amount: Double,
    val currency: String = "USD",
    val cadence: Cadence = Cadence.MONTHLY,
    val categoryId: String?,
    val startDate: LocalDate,
    val nextBilling: LocalDate,
    val status: Status = Status.ACTIVE,
    val notes: String = "",
    val reminderOn: Boolean = true,
    /** For a cancelled subscription: the last day it was paid up to. */
    val accessUntil: LocalDate? = null,
    val periods: List<BillingPeriod> = emptyList(),
) {
    val perMonth: Double
        get() = when (cadence) {
            Cadence.YEARLY -> amount / 12
            Cadence.WEEKLY -> amount * 52 / 12
            Cadence.MONTHLY -> amount
        }

    val perYear: Double
        get() = when (cadence) {
            Cadence.YEARLY -> amount
            Cadence.WEEKLY -> amount * 52
            Cadence.MONTHLY -> amount * 12
        }

    val paidPeriods: List<BillingPeriod> get() = periods.filter { it.paidOn != null }
    val lifetimeSpend: Double get() = paidPeriods.sumOf { it.amount }

    /** The current cycle is recorded once its period has a payment date. */
    val currentCycleRecorded: Boolean
        get() = status == Status.ACTIVE && periods.lastOrNull()?.paidOn != null

    fun daysUntilNext(today: LocalDate): Long = ChronoUnit.DAYS.between(today, nextBilling)
}

fun Cadence.step(date: LocalDate, count: Long): LocalDate = when (this) {
    Cadence.WEEKLY -> date.plusWeeks(count)
    Cadence.MONTHLY -> date.plusMonths(count)
    Cadence.YEARLY -> date.plusYears(count)
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    val label: String get() = commonText.themeLabel(this)
}

enum class PurchaseState { OFFER, BUYING, FAILED, OWNED }
