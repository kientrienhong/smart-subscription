package com.smartsubscription.scan

import com.smartsubscription.data.Cadence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class InvoiceParsingTest {
    private val today = LocalDate.of(2026, 9, 28)
    private fun parse(text: String, currency: String = "USD", today: LocalDate = this.today) = PatternInvoiceParser.parse(text.trimIndent(), currency, today)

    @Test
    fun `english receipt with a known service`() {
        val p = parse(
            """
            NETFLIX
            Your receipt
            Netflix Premium
            Billing date: Sep 20, 2026
            Monthly plan
            Total ${'$'}22.99
            """,
        )
        assertEquals("Netflix", p.provider)
        assertEquals("Netflix Premium", p.subscriptionName)
        assertEquals(22.99, p.amount!!, 0.0)
        assertEquals("USD", p.currency)
        assertEquals(Cadence.MONTHLY, p.cadence)
        assertEquals(LocalDate.of(2026, 9, 20), p.invoiceDate)
    }

    @Test
    fun `total wins over subtotal and tax`() {
        val p = parse(
            """
            Apple
            Invoice Date: 20 Sep 2026
            Apple TV+ Monthly
            Subtotal ${'$'}9.17
            Tax ${'$'}0.82
            Total ${'$'}9.99
            """,
        )
        assertEquals("Apple", p.provider)
        assertEquals(9.99, p.amount!!, 0.0)
        assertEquals(LocalDate.of(2026, 9, 20), p.invoiceDate)
    }

    @Test
    fun `invoice in dong reads day-first dates and whole numbers`() {
        val p = parse(
            """
            Spotify
            Invoice
            Date: 20/09/2026
            Spotify Premium Family - monthly
            Total: 99.000 đ
            """,
            currency = "USD",
        )
        assertEquals("VND", p.currency)
        assertEquals(99000.0, p.amount!!, 0.0)
        assertEquals(LocalDate.of(2026, 9, 20), p.invoiceDate)
        assertEquals(Cadence.MONTHLY, p.cadence)

        assertEquals(LocalDate.of(2026, 10, 11), parse("Date: 11-10-2026\nTotal 50.000 ₫").invoiceDate)
        val period = parse("From 11/09 to 11/10/2026\nTotal 50.000 ₫")
        assertEquals(LocalDate.of(2026, 9, 11), period.invoiceDate)
        assertEquals(LocalDate.of(2026, 10, 11), period.nextPaymentDate)
    }

    @Test
    fun `unknown vendor falls back to the first plain line`() {
        val p = parse(
            """
            Acme Cloud Ltd
            Invoice #12345
            Date 2026-09-01
            Amount due: 120.00 EUR
            Billed annually
            """,
        )
        assertEquals("Acme Cloud Ltd", p.provider)
        assertEquals(120.0, p.amount!!, 0.0)
        assertEquals("EUR", p.currency)
        assertEquals(Cadence.YEARLY, p.cadence)
        assertEquals(LocalDate.of(2026, 9, 1), p.invoiceDate)
    }

    @Test
    fun `next payment date is read apart from the charge date`() {
        val p = parse(
            """
            Spotify
            Next billing date: Oct 20, 2026
            Paid on: Sep 20, 2026
            Premium Individual monthly
            Total ${'$'}11.99
            """,
        )
        assertEquals(LocalDate.of(2026, 9, 20), p.invoiceDate)
        assertEquals(LocalDate.of(2026, 10, 20), p.nextPaymentDate)
        assertNull(parse("Netflix\nBilling date: Sep 20, 2026").nextPaymentDate)
        val reply = InvoicePrompt.parseReply("""{"invoiceDate": "2026-09-20", "nextPaymentDate": "2026-10-20"}""")!!
        assertEquals(LocalDate.of(2026, 10, 20), reply.nextPaymentDate)
    }

    @Test
    fun `service period gives the next payment and the cadence`() {
        // ML Kit splits "Date paid" and its value onto separate lines, and the period prints the year once.
        val p = parse(
            """
            Receipt
            Date paid
            October 11th, 2026
            Claude Pro
            Oct 11 – Nov 11, 2026
            Total ${'$'}20.00
            """,
            today = LocalDate.of(2026, 10, 11),
        )
        assertEquals(LocalDate.of(2026, 10, 11), p.invoiceDate)
        assertEquals(LocalDate.of(2026, 11, 11), p.nextPaymentDate)
        assertEquals(Cadence.MONTHLY, p.cadence)

        val q = parse("Service period: Dec 11, 2025 - Dec 11, 2026\nAmount paid ${'$'}99.00", today = LocalDate.of(2026, 1, 2))
        assertEquals(LocalDate.of(2025, 12, 11), q.invoiceDate)
        assertEquals(LocalDate.of(2026, 12, 11), q.nextPaymentDate)
        assertEquals(Cadence.YEARLY, q.cadence)

        val r = PatternInvoiceParser.findServicePeriod(listOf("Billing period 11 Dec – 11 Jan 2027"), "EUR")!!
        assertEquals(LocalDate.of(2026, 12, 11), r.start)
    }

    @Test
    fun `netflix invoice with two-digit years and labels in their own column`() {
        // ML Kit can return the label column and the value column as separate blocks.
        val p = parse(
            """
            NETFLIX
            Date
            Description
            Service Period
            Amount
            10/27/25
            Streaming Service
            10/27/25—11/26/25
            ${'$'}24.99
            TOTAL ${'$'}27.43
            """,
        )
        assertEquals("Netflix", p.provider)
        assertEquals(27.43, p.amount!!, 0.0)
        assertEquals(LocalDate.of(2025, 10, 27), p.invoiceDate)
        assertEquals(LocalDate.of(2025, 11, 26), p.nextPaymentDate)
        assertEquals(Cadence.MONTHLY, p.cadence)
        assertEquals(LocalDate.of(2025, 11, 26), PatternInvoiceParser.findServicePeriod(listOf("Service Period 10/27/25-11/26/25"), "USD")!!.endInclusive)
    }

    @Test
    fun `comma decimals and thousands separators`() {
        assertEquals(9.99, PatternInvoiceParser.toNumber("9,99", "EUR")!!, 0.0)
        assertEquals(1234.56, PatternInvoiceParser.toNumber("1.234,56", "EUR")!!, 0.0)
        assertEquals(1234.56, PatternInvoiceParser.toNumber("1,234.56", "USD")!!, 0.0)
        assertEquals(268129.0, PatternInvoiceParser.toNumber("268,129", "VND")!!, 0.0)
    }

    @Test
    fun `nothing recognisable leaves the fields empty`() {
        val p = parse("thank you for your business")
        assertNull(p.amount)
        assertNull(p.invoiceDate)
        assertNull(p.cadence)
    }

    @Test
    fun `model reply inside a code fence is read and checked`() {
        val reply = """
            ```json
            {"invoiceDate": "2026-09-20", "totalAmount": 268129.4, "currencyCode": "vnd",
             "subscriptionName": "", "providerName": "Netflix", "billingCycle": "monthly"}
            ```
        """.trimIndent()
        val p = InvoicePrompt.parseReply(reply)!!
        assertEquals("VND", p.currency)
        assertEquals(268129.0, p.amount!!, 0.0)
        assertEquals("Netflix", p.subscriptionName)
        assertEquals(Cadence.MONTHLY, p.cadence)
        assertEquals(ParseEngine.ON_DEVICE_MODEL, p.engine)
    }

    @Test
    fun `model reply with made-up values keeps only the sensible ones`() {
        val p = InvoicePrompt.parseReply("""{"invoiceDate": "sometime", "totalAmount": -3, "currencyCode": "XYZ", "providerName": "Hulu", "billingCycle": "daily"}""")!!
        assertNull(p.invoiceDate)
        assertNull(p.amount)
        assertNull(p.currency)
        assertNull(p.cadence)
        assertEquals("Hulu", p.provider)
        assertNull(InvoicePrompt.parseReply("I can't help with that."))
    }

    @Test
    fun `model fields win and patterns fill the gaps`() {
        val model = InvoicePrompt.parseReply("""{"providerName": "Netflix", "totalAmount": 22.99, "currencyCode": "USD"}""")!!
        val patterns = parse("Netflix\nBilling date: Sep 20, 2026\nMonthly\nTotal ${'$'}20.00")
        val merged = model.orElse(patterns)
        assertEquals(22.99, merged.amount!!, 0.0)
        assertEquals(LocalDate.of(2026, 9, 20), merged.invoiceDate)
        assertEquals(Cadence.MONTHLY, merged.cadence)
    }

    @Test
    fun `next billing steps the invoice date past today`() {
        assertEquals(LocalDate.of(2026, 10, 20), nextBillingDate(LocalDate.of(2026, 9, 20), Cadence.MONTHLY, today))
        assertEquals(LocalDate.of(2027, 9, 20), nextBillingDate(LocalDate.of(2026, 9, 20), Cadence.YEARLY, today))
        // Month ends clamp per step from the start: Jan 31 plus eight months is Sep 30, already past Sep 28.
        assertEquals(LocalDate.of(2026, 9, 30), nextBillingDate(LocalDate.of(2026, 1, 31), Cadence.MONTHLY, today))
        assertEquals(LocalDate.of(2026, 10, 31), nextBillingDate(LocalDate.of(2026, 1, 31), Cadence.MONTHLY, LocalDate.of(2026, 9, 30)))
        assertEquals(LocalDate.of(2026, 12, 1), nextBillingDate(LocalDate.of(2026, 12, 1), Cadence.MONTHLY, today))
    }

    @Test
    fun `translation fills gaps but never the amount or currency`() {
        val original = ParsedInvoice("Viettel", null, null, "VND", null, null, null, ParseEngine.PATTERNS)
        val translated = parse("Viettel\nMonthly plan\nTotal 1,234.00 USD")
        val merged = original.orElseTranslated(translated)
        assertEquals("Viettel", merged.provider)
        assertEquals(Cadence.MONTHLY, merged.cadence)
        assertNull(merged.amount)
        assertEquals("VND", merged.currency)
    }
}
