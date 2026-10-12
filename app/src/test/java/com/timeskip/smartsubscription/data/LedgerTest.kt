package com.timeskip.smartsubscription.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LedgerTest {
    // The design's own "today", so the numbers can be checked against the mockup.
    private val today = LocalDate.of(2026, 9, 20)
    private val subs = DemoData.subscriptions(today)
    private val ledger = Ledger(subs, today, "USD")

    @Test
    fun `seed has nine active subscriptions sorted by next date`() {
        assertEquals(9, ledger.active.size)
        assertEquals("Netflix Premium", ledger.active.first().name)
        assertEquals("Microsoft 365 Family", ledger.active.last().name)
    }

    @Test
    fun `still to come counts renewals left this month`() {
        // Sep 22 Netflix, Sep 25 Spotify, Sep 29 iCloud+: 22.99 + 19.99 + 9.99
        assertEquals(52.97, ledger.stillToCome, 0.001)
        // Monthly ones whose next date is already in October were charged in September.
        assertEquals(13.99 + 10.00 + 20.00 + 15.99 + 4.00, ledger.chargedThisMonth, 0.001)
        assertEquals(0.0, ledger.yearlyToCome, 0.001)
    }

    @Test
    fun `monthly all-in spreads the yearly charge over twelve months`() {
        val monthly = 22.99 + 19.99 + 9.99 + 13.99 + 10.00 + 20.00 + 15.99 + 4.00 + 129.99 / 12
        assertEquals(monthly, ledger.monthlyAllIn, 0.001)
    }

    @Test
    fun `this month's rail groups by week and leaves the far yearly renewal for later`() {
        val rail = ledger.rail(yearAhead = false, categoryId = null)
        assertEquals(listOf("This week", "Next week", "October"), rail.groups.map { it.title })
        assertEquals(1, rail.laterCount)
        assertEquals(129.99, rail.laterTotal!!, 0.001)
    }

    @Test
    fun `year ahead groups by month and filters by category`() {
        val rail = ledger.rail(yearAhead = true, categoryId = "music")
        assertEquals(1, rail.groups.size)
        assertEquals("Spotify Family", rail.groups.single().rows.single().name)
    }

    @Test
    fun `analytics splits paid history from what is still expected`() {
        val a = ledger.analytics(3)
        // Two months behind, this month, three ahead.
        assertEquals(6, a.buckets.size)
        assertTrue(a.paid > 0)
        assertTrue(a.expected > 0)
        // Microsoft 365 renews in December, inside the next three months.
        assertEquals(129.99, a.expectedYearly, 0.001)
    }

    @Test
    fun `money follows each currency's decimals and symbol spacing`() {
        assertEquals("$1,234.50", money(1234.5))
        assertEquals("₫150,000", money(150000.0, "VND"))
        assertEquals("CHF 9.00", money(9.0, "CHF"))
    }

    @Test
    fun `whole-number currencies refuse a decimal point`() {
        assertEquals(9.99, parseAmount("9.99", "USD")!!, 0.0)
        assertEquals(150000.0, parseAmount("150,000", "VND")!!, 0.0)
        assertNull(parseAmount("1.5", "JPY"))
        assertNull(parseAmount("abc", "USD"))
    }

    @Test
    fun `other currencies are converted with rates and summed apart without them`() {
        val usd = subs.first { it.nextBilling.month == today.month && it.nextBilling.isAfter(today) }
        val vnd = usd.copy(id = "vnd", amount = 263_000.0, currency = "VND")
        val eur = usd.copy(id = "eur", amount = 9.20, currency = "EUR")
        val mixed = subs + vnd + eur

        val converted = Ledger(mixed, today, "USD", ExchangeRates("USD", mapOf("VND" to 26_300.0, "EUR" to 0.92)))
        assertEquals(ledger.stillToCome + 10.0 + 10.0, converted.stillToCome, 0.001)
        assertEquals(0, converted.foreignCount)
        assertEquals("", converted.stillToComeElsewhere)

        // Offline: USD totals unchanged, the others summed in their own currencies.
        val offline = Ledger(mixed, today, "USD")
        assertEquals(ledger.stillToCome, offline.stillToCome, 0.001)
        assertEquals(2, offline.foreignCount)
        assertTrue(offline.stillToComeElsewhere.contains("9.20") && offline.stillToComeElsewhere.contains("263"))

        // Rates for another base are ignored rather than misapplied.
        assertEquals(2, Ledger(mixed, today, "USD", ExchangeRates("EUR", mapOf("VND" to 1.0))).foreignCount)
    }
}
