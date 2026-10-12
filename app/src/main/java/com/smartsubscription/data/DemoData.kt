package com.smartsubscription.data

import com.smartsubscription.i18n.categoriesText
import java.time.LocalDate

/** Port of `DemoDataSeeder.swift`: the same subscriptions, relative to today, with the same billing history. */
object DemoData {
    /** Named in the language the app starts in; renaming one later is the person's choice. */
    val defaultCategories: List<Category>
        get() = listOf(
            "entertainment" to "tv",
            "productivity" to "laptopcomputer",
            "cloud" to "cloud",
            "music" to "music.note",
            "gaming" to "gamecontroller",
            "news" to "newspaper",
            "finance" to "dollarsign.circle",
            "other" to "ellipsis.circle",
        ).map { (id, icon) -> Category(id, categoriesText.defaultName(id), icon) }

    /** The twelve icons the category editor offers (`CategoryEditView.commonIcons`). */
    val iconChoices = listOf(
        "tv", "laptopcomputer", "cloud", "music.note", "gamecontroller", "newspaper",
        "dollarsign.circle", "ellipsis.circle", "cart", "house", "car", "book",
    )

    private data class Spec(
        val name: String,
        val provider: String,
        val amount: Double,
        val category: String,
        val nextInDays: Long,
        val monthsActive: Long,
        val cadence: Cadence = Cadence.MONTHLY,
        val status: Status = Status.ACTIVE,
    )

    private val specs = listOf(
        Spec("Netflix Premium", "Netflix", 22.99, "entertainment", 2, 14),
        Spec("Spotify Family", "Spotify", 19.99, "music", 5, 9),
        Spec("iCloud+ 2 TB", "Apple", 9.99, "cloud", 9, 22),
        Spec("YouTube Premium", "YouTube", 13.99, "entertainment", 12, 6),
        Spec("Notion Plus", "Notion", 10.00, "productivity", 16, 11),
        Spec("ChatGPT Plus", "OpenAI", 20.00, "productivity", 19, 8),
        Spec("Disney+", "Disney+", 15.99, "entertainment", 23, 4),
        Spec("GitHub Pro", "GitHub", 4.00, "productivity", 27, 18),
        Spec("Microsoft 365 Family", "Microsoft", 129.99, "productivity", 84, 20, cadence = Cadence.YEARLY),
        Spec("Adobe Creative Cloud", "Adobe", 59.99, "productivity", 0, 3, status = Status.CANCELLED),
    )

    fun subscriptions(today: LocalDate): List<Subscription> = specs.mapIndexed { index, spec ->
        val next = today.plusDays(spec.nextInDays)
        // A cancelled one stopped three days ago; its newest period ends there and is paid.
        val anchor = if (spec.status == Status.ACTIVE) next else today.minusDays(3)
        val cycles = if (spec.cadence == Cadence.YEARLY) 2 else spec.monthsActive.coerceIn(1, 7).toInt()
        val periods = (0 until cycles).map { i ->
            val end = spec.cadence.step(anchor, -i.toLong())
            val start = spec.cadence.step(anchor, -(i + 1).toLong())
            val isCurrent = i == 0 && spec.status == Status.ACTIVE
            BillingPeriod(start, end, spec.amount, paidOn = if (isCurrent) null else start)
        }.reversed()
        Subscription(
            id = "seed-$index",
            name = spec.name,
            provider = spec.provider,
            amount = spec.amount,
            cadence = spec.cadence,
            categoryId = spec.category,
            startDate = today.minusMonths(spec.monthsActive),
            nextBilling = next,
            status = spec.status,
            accessUntil = if (spec.status == Status.CANCELLED) anchor else null,
            periods = periods,
        )
    }
}
