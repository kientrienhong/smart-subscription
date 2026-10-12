package com.timeskip.smartsubscription.i18n

/** Settings and its unlock card. The debug-only Developer section stays in English. */
interface SettingsText {
    val title: String
    val appearance: String
    val theme: String
    val currency: String
    val defaultCurrency: String
    val currencyFooter: String
    val organization: String
    val manageCategories: String
    val archive: String
    val archiveFooter: String
    val data: String
    val exportCsv: String
    val nothingToExport: String
    val exportFooter: String
    val exportChooser: String
    val notifications: String
    val renewalReminders: String
    val remindMe: String
    val deliverAt: String
    val remindersFooter: String
    val about: String
    val version: String
    fun remindersArriveAt(hour: Int, minute: Int): String
    /** "3 days before", the reminder lead time. */
    fun leadLabel(days: Int): String

    val unlocked: String
    fun paidOnce(price: String): String
    val unlockFeatures: String
    val unlockFor: String
    fun priceOnce(price: String): String
    val notASubscription: String
}

private object SettingsEn : SettingsText {
    override val title = "Settings"
    override val appearance = "Appearance"
    override val theme = "Theme"
    override val currency = "Currency"
    override val defaultCurrency = "Default currency"
    override val currencyFooter = "Totals are shown in this currency. Subscriptions billed in others are converted at today's exchange rate; offline, they get their own totals."
    override val organization = "Organization"
    override val manageCategories = "Manage categories"
    override val archive = "Archive"
    override val archiveFooter = "Paused and cancelled subscriptions live in the Archive, along with what they cost you."
    override val data = "Data"
    override val exportCsv = "Export data (CSV)"
    override val nothingToExport = "Nothing to export yet"
    override val exportChooser = "Export subscriptions"
    override val exportFooter = "The CSV file opens in Sheets or Excel and stays on this phone until you choose where to send it."
    override val notifications = "Notifications"
    override val renewalReminders = "Renewal reminders"
    override val remindMe = "Remind me"
    override val deliverAt = "Deliver at"
    override val remindersFooter = "A single reminder before each renewal, delivered on this device. Nothing is sent anywhere."
    override val about = "About"
    override val version = "Version"
    override fun remindersArriveAt(hour: Int, minute: Int) = "Reminders will arrive at %02d:%02d.".format(hour, minute)
    override fun leadLabel(days: Int) = if (days == 1) "1 day before" else "$days days before"

    override val unlocked = "Unlocked"
    override fun paidOnce(price: String) = "Paid $price once. Nothing renews."
    override val unlockFeatures = "Scanning, unlimited list, 12-month Analytics"
    override val unlockFor = "Unlock for "
    override fun priceOnce(price: String) = "$price once"
    override val notASubscription = "Not a subscription."
}

private object SettingsVi : SettingsText {
    override val title = "Cài đặt"
    override val appearance = "Giao diện"
    override val theme = "Chủ đề"
    override val currency = "Tiền tệ"
    override val defaultCurrency = "Tiền tệ mặc định"
    override val currencyFooter = "Tổng được hiển thị bằng tiền tệ này. Gói thanh toán bằng tiền tệ khác được quy đổi theo tỷ giá hôm nay; khi ngoại tuyến, chúng được cộng riêng."
    override val organization = "Sắp xếp"
    override val manageCategories = "Quản lý danh mục"
    override val archive = "Lưu trữ"
    override val archiveFooter = "Các gói tạm dừng và đã hủy nằm trong Lưu trữ, kèm số tiền bạn đã chi cho chúng."
    override val data = "Dữ liệu"
    override val exportCsv = "Xuất dữ liệu (CSV)"
    override val nothingToExport = "Chưa có gì để xuất"
    override val exportChooser = "Xuất danh sách gói"
    override val exportFooter = "Tệp CSV mở được bằng Sheets hoặc Excel và chỉ nằm trên điện thoại này cho đến khi bạn chọn nơi gửi."
    override val notifications = "Thông báo"
    override val renewalReminders = "Nhắc gia hạn"
    override val remindMe = "Nhắc tôi"
    override val deliverAt = "Gửi lúc"
    override val remindersFooter = "Mỗi lần gia hạn có một nhắc nhở, gửi ngay trên thiết bị này. Không có gì được gửi đi đâu cả."
    override val about = "Giới thiệu"
    override val version = "Phiên bản"
    override fun remindersArriveAt(hour: Int, minute: Int) = "Nhắc nhở sẽ đến lúc %02d:%02d.".format(hour, minute)
    override fun leadLabel(days: Int) = "$days ngày trước"

    override val unlocked = "Đã mở khóa"
    override fun paidOnce(price: String) = "Đã trả $price một lần. Không gia hạn."
    override val unlockFeatures = "Quét hóa đơn, danh sách không giới hạn, Thống kê 12 tháng"
    override val unlockFor = "Mở khóa với "
    override fun priceOnce(price: String) = "$price một lần"
    override val notASubscription = "Không phải gói đăng ký."
}

val settingsText: SettingsText get() = if (isVietnamese) SettingsVi else SettingsEn
