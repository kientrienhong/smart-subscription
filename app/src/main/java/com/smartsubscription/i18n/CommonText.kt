package com.smartsubscription.i18n

import com.smartsubscription.data.Cadence
import com.smartsubscription.data.ThemeMode
import java.time.DayOfWeek
import java.time.Month
import java.util.Locale

/**
 * The app's languages. Each area of the app has a text interface with an English and a Vietnamese object,
 * picked here from the device language, or the per-app language on Android 13+. A missing translation
 * does not compile.
 */
val isVietnamese: Boolean get() = Locale.getDefault().language == "vi"

/** Text shared across screens: dates, due labels, cadences, common buttons. */
interface CommonText {
    fun monthShort(m: Month): String
    fun monthLong(m: Month): String
    fun dayShort(d: DayOfWeek): String
    /** "Oct 11" / "11 thg 10". */
    fun shortDate(m: Month, day: Int): String
    /** "Oct 11, 2026" / "11 thg 10, 2026". */
    fun longDate(m: Month, day: Int, year: Int): String

    val today: String
    val tomorrow: String
    val overdue: String
    fun inDays(days: Long): String

    val thisWeek: String
    val nextWeek: String

    fun cadenceLabel(c: Cadence): String
    /** After a price: "$9.99 a month" / "$9.99 mỗi tháng". */
    fun cadenceA(c: Cadence): String
    /** Under a big price: "per month" / "mỗi tháng". */
    fun cadencePer(c: Cadence): String
    fun themeLabel(t: ThemeMode): String

    val save: String
    val cancel: String
    val delete: String
    val edit: String
    val undo: String
    val done: String
    val back: String
    val noCategory: String
}

private object CommonEn : CommonText {
    override fun monthShort(m: Month) = m.getDisplayName(java.time.format.TextStyle.SHORT, Locale.US)
    override fun monthLong(m: Month) = m.getDisplayName(java.time.format.TextStyle.FULL, Locale.US)
    override fun dayShort(d: DayOfWeek) = d.getDisplayName(java.time.format.TextStyle.SHORT, Locale.US)
    override fun shortDate(m: Month, day: Int) = "${monthShort(m)} $day"
    override fun longDate(m: Month, day: Int, year: Int) = "${shortDate(m, day)}, $year"

    override val today = "Today"
    override val tomorrow = "Tomorrow"
    override val overdue = "Overdue"
    override fun inDays(days: Long) = "in $days days"

    override val thisWeek = "This week"
    override val nextWeek = "Next week"

    override fun cadenceLabel(c: Cadence) = when (c) {
        Cadence.WEEKLY -> "Weekly"
        Cadence.MONTHLY -> "Monthly"
        Cadence.YEARLY -> "Yearly"
    }
    override fun cadenceA(c: Cadence) = when (c) {
        Cadence.WEEKLY -> "a week"
        Cadence.MONTHLY -> "a month"
        Cadence.YEARLY -> "a year"
    }
    override fun cadencePer(c: Cadence) = when (c) {
        Cadence.WEEKLY -> "per week"
        Cadence.MONTHLY -> "per month"
        Cadence.YEARLY -> "per year"
    }
    override fun themeLabel(t: ThemeMode) = when (t) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

    override val save = "Save"
    override val cancel = "Cancel"
    override val delete = "Delete"
    override val edit = "Edit"
    override val undo = "Undo"
    override val done = "Done"
    override val back = "Back"
    override val noCategory = "No category"
}

private object CommonVi : CommonText {
    override fun monthShort(m: Month) = "thg ${m.value}"
    override fun monthLong(m: Month) = "Tháng ${m.value}"
    override fun dayShort(d: DayOfWeek) = if (d == DayOfWeek.SUNDAY) "CN" else "T${d.value + 1}"
    override fun shortDate(m: Month, day: Int) = "$day thg ${m.value}"
    override fun longDate(m: Month, day: Int, year: Int) = "${shortDate(m, day)}, $year"

    override val today = "Hôm nay"
    override val tomorrow = "Ngày mai"
    override val overdue = "Quá hạn"
    override fun inDays(days: Long) = "$days ngày nữa"

    override val thisWeek = "Tuần này"
    override val nextWeek = "Tuần sau"

    override fun cadenceLabel(c: Cadence) = when (c) {
        Cadence.WEEKLY -> "Hàng tuần"
        Cadence.MONTHLY -> "Hàng tháng"
        Cadence.YEARLY -> "Hàng năm"
    }
    override fun cadenceA(c: Cadence) = when (c) {
        Cadence.WEEKLY -> "mỗi tuần"
        Cadence.MONTHLY -> "mỗi tháng"
        Cadence.YEARLY -> "mỗi năm"
    }
    override fun cadencePer(c: Cadence) = cadenceA(c)
    override fun themeLabel(t: ThemeMode) = when (t) {
        ThemeMode.SYSTEM -> "Hệ thống"
        ThemeMode.LIGHT -> "Sáng"
        ThemeMode.DARK -> "Tối"
    }

    override val save = "Lưu"
    override val cancel = "Hủy"
    override val delete = "Xóa"
    override val edit = "Sửa"
    override val undo = "Hoàn tác"
    override val done = "Xong"
    override val back = "Quay lại"
    override val noCategory = "Chưa có danh mục"
}

val commonText: CommonText get() = if (isVietnamese) CommonVi else CommonEn
