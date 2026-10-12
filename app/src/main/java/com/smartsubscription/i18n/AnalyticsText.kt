package com.smartsubscription.i18n

/** The Analytics screen. */
interface AnalyticsText {
    val title: String
    val backToSubscriptions: String
    fun paidLast(months: Int): String
    fun recordedRenewals(n: Int): String
    val nothingRecorded: String
    val stillToPay: String
    fun restOf(month: String, months: Int): String
    fun months(n: Int): String
    fun unlockToast(price: String): String
    val chartTitle: String
    /** "Oct 2026 to Jan 2027". */
    fun range(from: String, to: String): String
    fun byMonth(range: String): String
    val recorded: String
    val expected: String
    val yearly: String
    fun chartDescription(range: String, paid: String, expected: String): String
    val noRenewals: String
    val providersTitle: String
    fun providersCaption(monthly: String): String
    val noActive: String
    fun inclYearly(amount: String): String
    val today: String
    fun perYear(amount: String): String
}

private object AnalyticsEn : AnalyticsText {
    override val title = "Analytics"
    override val backToSubscriptions = "Back to Subscriptions"
    override fun paidLast(months: Int) = "Paid, last $months months"
    override fun recordedRenewals(n: Int) = "$n recorded renewals"
    override val nothingRecorded = "Nothing recorded yet"
    override val stillToPay = "Still to pay"
    override fun restOf(month: String, months: Int) = "Rest of $month and next $months months"
    override fun months(n: Int) = "$n months"
    override fun unlockToast(price: String) = "More than 3 months is part of the $price unlock."
    override val chartTitle = "What I paid, and what is coming"
    override fun range(from: String, to: String) = "$from to $to"
    override fun byMonth(range: String) = "By month, $range."
    override val recorded = "Recorded"
    override val expected = "Expected"
    override val yearly = "Yearly"
    override fun chartDescription(range: String, paid: String, expected: String) =
        "Spend by month, $range. Paid $paid. Still to pay $expected."
    override val noRenewals = "No renewals recorded yet. Confirm a renewal on a subscription to start filling in the solid bars."
    override val providersTitle = "Who gets the most each month"
    override fun providersCaption(monthly: String) =
        "Expected, at today’s prices, with each provider’s share of $monthly. Yearly charges are spread over twelve months."
    override val noActive = "No active subscriptions."
    override fun inclYearly(amount: String) = "incl. $amount yearly"
    override val today = "today"
    override fun perYear(amount: String) = "$amount/yr"
}

private object AnalyticsVi : AnalyticsText {
    override val title = "Thống kê"
    override val backToSubscriptions = "Quay lại danh sách gói"
    override fun paidLast(months: Int) = "Đã trả, $months tháng qua"
    override fun recordedRenewals(n: Int) = "$n lần gia hạn đã ghi nhận"
    override val nothingRecorded = "Chưa ghi nhận khoản nào"
    override val stillToPay = "Còn phải trả"
    override fun restOf(month: String, months: Int) = "Phần còn lại $month và $months tháng tới"
    override fun months(n: Int) = "$n tháng"
    override fun unlockToast(price: String) = "Xem hơn 3 tháng có trong gói mở khóa $price."
    override val chartTitle = "Đã trả và sắp phải trả"
    override fun range(from: String, to: String) = "$from đến $to"
    override fun byMonth(range: String) = "Theo tháng, $range."
    override val recorded = "Đã ghi nhận"
    override val expected = "Dự kiến"
    override val yearly = "Hàng năm"
    override fun chartDescription(range: String, paid: String, expected: String) =
        "Chi tiêu theo tháng, $range. Đã trả $paid. Còn phải trả $expected."
    override val noRenewals = "Chưa ghi nhận lần gia hạn nào. Xác nhận gia hạn trong một gói để các cột đặc bắt đầu hiện lên."
    override val providersTitle = "Ai nhận nhiều nhất mỗi tháng"
    override fun providersCaption(monthly: String) =
        "Dự kiến theo giá hiện tại, phần của từng nhà cung cấp trong $monthly. Khoản hàng năm được chia đều cho mười hai tháng."
    override val noActive = "Không có gói nào đang dùng."
    override fun inclYearly(amount: String) = "gồm $amount hàng năm"
    override val today = "hôm nay"
    override fun perYear(amount: String) = "$amount/năm"
}

val analyticsText: AnalyticsText get() = if (isVietnamese) AnalyticsVi else AnalyticsEn
