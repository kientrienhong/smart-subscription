package com.smartsubscription.i18n

/** The home-screen widget. Its title and picker description are string resources (values-vi/strings.xml). */
interface WidgetText {
    val title: String
    val openApp: String
    fun nothingRenews(days: Int): String
    fun more(count: Int): String
}

private object WidgetEn : WidgetText {
    override val title = "Upcoming renewals"
    override val openApp = "Open SmartSubscription to see your renewals here."
    override fun nothingRenews(days: Int) = "Nothing renews in the next $days days."
    override fun more(count: Int) = "$count more in the app"
}

private object WidgetVi : WidgetText {
    override val title = "Sắp gia hạn"
    override val openApp = "Mở SmartSubscription để xem các lần gia hạn ở đây."
    override fun nothingRenews(days: Int) = "Không có gói nào gia hạn trong $days ngày tới."
    override fun more(count: Int) = "Còn $count gói nữa trong ứng dụng"
}

val widgetText: WidgetText get() = if (isVietnamese) WidgetVi else WidgetEn
