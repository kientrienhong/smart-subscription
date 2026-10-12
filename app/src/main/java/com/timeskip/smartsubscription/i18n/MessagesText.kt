package com.timeskip.smartsubscription.i18n

import com.timeskip.smartsubscription.data.Status
import com.timeskip.smartsubscription.data.plural

/** The view model's snackbar messages and the lifetime line it builds for the Archive. */
interface MessagesText {
    fun renewed(name: String, date: String): String
    val cycleExpectedAgain: String
    fun reminderOn(name: String): String
    fun reminderOff(name: String): String
    /** The undo bar after pause, cancel, archive or resume. */
    fun statusChanged(name: String, status: Status): String
    fun restored(name: String, wasPaused: Boolean): String
    val undone: String
    fun deleted(name: String): String
    /** [converted] is null when [currency] is the home currency. */
    fun saved(name: String, currency: String, home: String, converted: Boolean?): String
    fun updated(name: String): String
    fun categoryAdded(name: String): String
    fun categoryRenamed(old: String, new: String): String
    fun categoryDeleted(name: String, inUse: Int): String
    fun totalsIn(code: String): String
    fun leadChanged(days: Int): String
    val unlocked: String
    val noPastPurchases: String
    val noPayments: String
    fun lifetime(payments: Int, amount: String): String
}

private object MessagesEn : MessagesText {
    override fun renewed(name: String, date: String) = "Renewed. $name recorded for $date."
    override val cycleExpectedAgain = "Undone. The cycle is expected again."
    override fun reminderOn(name: String) = "Reminder on for $name."
    override fun reminderOff(name: String) = "Reminder off for $name."
    override fun statusChanged(name: String, status: Status) = "$name " + when (status) {
        Status.PAUSED -> "paused"
        Status.CANCELLED -> "cancelled"
        Status.ARCHIVED -> "archived"
        Status.ACTIVE -> "resumed"
    }
    override fun restored(name: String, wasPaused: Boolean) = "$name " + if (wasPaused) "resumed" else "reactivated"
    override val undone = "Undone."
    override fun deleted(name: String) = "$name and its payments are deleted."
    override fun saved(name: String, currency: String, home: String, converted: Boolean?): String {
        val elsewhere = when (converted) {
            null -> ""
            true -> ", converted to $home in the totals"
            false -> ", shown in $currency and summed apart until exchange rates load"
        }
        return "Saved. $name is on the rail$elsewhere."
    }
    override fun updated(name: String) = "$name updated."
    override fun categoryAdded(name: String) = "$name added at the end of the list."
    override fun categoryRenamed(old: String, new: String) = "$old is now $new."
    override fun categoryDeleted(name: String, inUse: Int): String {
        val moved = if (inUse > 0) " Its ${plural(inUse, "subscription")} ${if (inUse == 1) "has" else "have"} no category now." else ""
        return "$name deleted.$moved"
    }
    override fun totalsIn(code: String) = "Totals are now in $code."
    override fun leadChanged(days: Int) = "Each reminder now comes ${settingsText.leadLabel(days)} the renewal."
    override val unlocked = "Unlocked. Thank you."
    override val noPastPurchases = "No past purchases on this account."
    override val noPayments = "No payments recorded"
    override fun lifetime(payments: Int, amount: String) = "Lifetime spend · ${plural(payments, "payment")} of $amount"
}

private object MessagesVi : MessagesText {
    override fun renewed(name: String, date: String) = "Đã gia hạn. Đã ghi nhận $name cho ngày $date."
    override val cycleExpectedAgain = "Đã hoàn tác. Kỳ này lại đang chờ thanh toán."
    override fun reminderOn(name: String) = "Đã bật nhắc nhở cho $name."
    override fun reminderOff(name: String) = "Đã tắt nhắc nhở cho $name."
    override fun statusChanged(name: String, status: Status) = when (status) {
        Status.PAUSED -> "Đã tạm dừng"
        Status.CANCELLED -> "Đã hủy"
        Status.ARCHIVED -> "Đã lưu trữ"
        Status.ACTIVE -> "Đã tiếp tục"
    } + " $name"
    override fun restored(name: String, wasPaused: Boolean) = (if (wasPaused) "Đã tiếp tục" else "Đã kích hoạt lại") + " $name"
    override val undone = "Đã hoàn tác."
    override fun deleted(name: String) = "Đã xóa $name cùng các khoản thanh toán."
    override fun saved(name: String, currency: String, home: String, converted: Boolean?): String {
        val elsewhere = when (converted) {
            null -> ""
            true -> ", được quy đổi sang $home trong tổng số"
            false -> ", hiển thị bằng $currency và tính riêng cho đến khi có tỷ giá"
        }
        return "Đã lưu. $name đã có trong danh sách$elsewhere."
    }
    override fun updated(name: String) = "Đã cập nhật $name."
    override fun categoryAdded(name: String) = "Đã thêm $name vào cuối danh sách."
    override fun categoryRenamed(old: String, new: String) = "Đã đổi $old thành $new."
    override fun categoryDeleted(name: String, inUse: Int): String {
        val moved = if (inUse > 0) " $inUse gói của danh mục này giờ chưa có danh mục." else ""
        return "Đã xóa $name.$moved"
    }
    override fun totalsIn(code: String) = "Tổng số giờ tính bằng $code."
    override fun leadChanged(days: Int) = "Mỗi nhắc nhở giờ đến $days ngày trước ngày gia hạn."
    override val unlocked = "Đã mở khóa. Cảm ơn bạn."
    override val noPastPurchases = "Tài khoản này chưa có giao dịch mua nào."
    override val noPayments = "Chưa ghi nhận khoản thanh toán nào"
    override fun lifetime(payments: Int, amount: String) = "Tổng chi · $payments lần thanh toán $amount"
}

val messagesText: MessagesText get() = if (isVietnamese) MessagesVi else MessagesEn
