package com.timeskip.smartsubscription.i18n

import com.timeskip.smartsubscription.data.Status
import com.timeskip.smartsubscription.data.plural

/** The subscription detail screen. */
interface DetailText {
    val backToSubscriptions: String
    /** Before the next date: "Renews Oct 11, 2026". */
    val renewsPrefix: String
    fun status(s: Status): String
    val statusLabel: String
    val started: String
    fun startedSub(months: Int, spent: String): String
    val nothingPaidYet: String
    val billing: String
    fun perMonth(amount: String): String
    fun perYear(amount: String): String
    val notes: String
    val thisCycle: String
    fun cycleRecorded(date: String): String
    fun cycleExpected(date: String): String
    val renewed: String
    val undoToChange: String
    val markAsRenewed: String
    val priceOrDateChanged: String
    val alreadyRecorded: String
    val subscription: String
    val pause: String
    val archive: String
    val cancelSubscription: String
    val renewalReminder: String
    val reminderSub: String
    /** "Delete <name> and its 3 recorded payments?", around the bold name. */
    val deletePrefix: String
    fun deleteSuffix(payments: Int): String
    val keep: String
    val deleteSubscription: String
    fun stripDescription(paid: Int, n: Int, dayIn: Long, cycleLen: Long): String
    val recordedSuffix: String
    val expectedSuffix: String
    /** After the bold count: "3 of 6 cycles recorded". */
    fun cyclesRecordedSuffix(n: Int): String
    /** Around the bold day: "Day 12 of 30". */
    val dayPrefix: String
    fun daySuffix(cycleLen: Long): String
}

private object DetailEn : DetailText {
    override val backToSubscriptions = "Back to Subscriptions"
    override val renewsPrefix = "Renews "
    override fun status(s: Status) = s.name.lowercase().replaceFirstChar { it.uppercase() }
    override val statusLabel = "Status"
    override val started = "Started"
    override fun startedSub(months: Int, spent: String) = "${plural(months, "month")}, $spent so far"
    override val nothingPaidYet = "nothing paid yet"
    override val billing = "Billing"
    override fun perMonth(amount: String) = "$amount a month"
    override fun perYear(amount: String) = "$amount a year"
    override val notes = "Notes"
    override val thisCycle = "This cycle"
    override fun cycleRecorded(date: String) = "$date is now recorded."
    override fun cycleExpected(date: String) = "$date is expected, not yet paid. Mark it when the charge lands, or say what changed."
    override val renewed = "Renewed"
    override val undoToChange = "Undo it to change what you recorded."
    override val markAsRenewed = "Mark as renewed"
    override val priceOrDateChanged = "Price or date changed"
    override val alreadyRecorded = "This payment is already recorded. Use “Price or date changed” to edit it."
    override val subscription = "Subscription"
    override val pause = "Pause"
    override val archive = "Archive"
    override val cancelSubscription = "Cancel subscription"
    override val renewalReminder = "Renewal reminder"
    override val reminderSub = "A single reminder before each renewal, delivered on this device. Nothing is sent anywhere."
    override val deletePrefix = "Delete "
    override fun deleteSuffix(payments: Int) = " and its ${plural(payments, "recorded payment")}?"
    override val keep = "Keep"
    override val deleteSubscription = "Delete subscription"
    override fun stripDescription(paid: Int, n: Int, dayIn: Long, cycleLen: Long) =
        "$paid of the last $n cycles recorded; day $dayIn of $cycleLen in the current cycle"
    override val recordedSuffix = " recorded"
    override val expectedSuffix = " expected"
    override fun cyclesRecordedSuffix(n: Int) = " of $n cycles recorded"
    override val dayPrefix = "Day "
    override fun daySuffix(cycleLen: Long) = " of $cycleLen"
}

private object DetailVi : DetailText {
    override val backToSubscriptions = "Quay lại danh sách gói"
    override val renewsPrefix = "Gia hạn "
    override fun status(s: Status) = when (s) {
        Status.ACTIVE -> "Đang dùng"
        Status.PAUSED -> "Tạm dừng"
        Status.CANCELLED -> "Đã hủy"
        Status.ARCHIVED -> "Đã lưu trữ"
    }
    override val statusLabel = "Trạng thái"
    override val started = "Bắt đầu"
    override fun startedSub(months: Int, spent: String) = "$months tháng, đã chi $spent"
    override val nothingPaidYet = "chưa trả khoản nào"
    override val billing = "Thanh toán"
    override fun perMonth(amount: String) = "$amount mỗi tháng"
    override fun perYear(amount: String) = "$amount mỗi năm"
    override val notes = "Ghi chú"
    override val thisCycle = "Kỳ này"
    override fun cycleRecorded(date: String) = "Đã ghi nhận kỳ $date."
    override fun cycleExpected(date: String) = "Kỳ $date dự kiến, chưa trả. Đánh dấu khi tiền bị trừ, hoặc cập nhật nếu có thay đổi."
    override val renewed = "Đã gia hạn"
    override val undoToChange = "Hoàn tác để sửa khoản đã ghi nhận."
    override val markAsRenewed = "Đánh dấu đã gia hạn"
    override val priceOrDateChanged = "Đổi giá hoặc ngày"
    override val alreadyRecorded = "Khoản này đã được ghi nhận. Dùng “Đổi giá hoặc ngày” để sửa."
    override val subscription = "Gói đăng ký"
    override val pause = "Tạm dừng"
    override val archive = "Lưu trữ"
    override val cancelSubscription = "Hủy gói"
    override val renewalReminder = "Nhắc gia hạn"
    override val reminderSub = "Một lời nhắc trước mỗi lần gia hạn, ngay trên máy này. Không gửi đi đâu cả."
    override val deletePrefix = "Xóa "
    override fun deleteSuffix(payments: Int) = " và $payments khoản đã ghi nhận?"
    override val keep = "Giữ lại"
    override val deleteSubscription = "Xóa gói"
    override fun stripDescription(paid: Int, n: Int, dayIn: Long, cycleLen: Long) =
        "$paid trên $n kỳ gần nhất đã ghi nhận; ngày $dayIn trên $cycleLen của kỳ hiện tại"
    override val recordedSuffix = " đã ghi nhận"
    override val expectedSuffix = " dự kiến"
    override fun cyclesRecordedSuffix(n: Int) = "/$n kỳ đã ghi nhận"
    override val dayPrefix = "Ngày "
    override fun daySuffix(cycleLen: Long) = "/$cycleLen"
}

val detailText: DetailText get() = if (isVietnamese) DetailVi else DetailEn
