package com.smartsubscription.i18n

import com.smartsubscription.data.Cadence
import com.smartsubscription.data.dueText
import com.smartsubscription.data.groupLabelFor
import com.smartsubscription.data.longText
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class CommonTextTest {
    private val before = Locale.getDefault()
    private val today = LocalDate.of(2026, 10, 11)

    @After
    fun restore() = Locale.setDefault(before)

    @Test
    fun `text follows the device language`() {
        Locale.setDefault(Locale.US)
        assertEquals("Oct 11, 2026", today.longText())
        assertEquals("in 3 days", dueText(3))
        assertEquals("a month", Cadence.MONTHLY.aWord)

        Locale.setDefault(Locale.forLanguageTag("vi-VN"))
        assertEquals("11 thg 10, 2026", today.longText())
        assertEquals("3 ngày nữa", dueText(3))
        assertEquals("Hôm nay", dueText(0))
        assertEquals("mỗi tháng", Cadence.MONTHLY.aWord)
        assertEquals("Tuần này", groupLabelFor(today.plusDays(2), today, yearAhead = false))
        assertEquals("Tháng 12", groupLabelFor(today.plusMonths(2), today, yearAhead = false))
    }
}
