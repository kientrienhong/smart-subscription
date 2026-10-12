package com.timeskip.smartsubscription.i18n

/** The one-time unlock screen. */
interface UnlockText {
    val unlocked: String
    val unlock: String
    /** "Paid $2.99 once, on Oct 11, 2026. Nothing renews.", around the bold price. */
    val paidPrefix: String
    fun paidSuffix(date: String): String
    val wholeApp: String
    val once: String
    val notASubscription: String
    val scanInvoice: String
    val waiting: String
    val confirmInSheet: String
    fun unlockFor(price: String): String
    val onePayment: String
    val restore: String
    val failedTitle: String
    val failedBody: String
    val restoredTitle: String
    val restoredBody: String
    val nowIncluded: String
    fun whatAdds(price: String): String
    val scanTitle: String
    val scanBody: String
    val moreTitle: String
    val moreBody: String
    val analyticsTitle: String
    val analyticsBody: String
    val freeLead: String
    val freeRest: String
    fun receiptDescription(monthly: String, price: String): String
    val yourSubscriptions: String
    val thisApp: String
}

private object UnlockEn : UnlockText {
    override val unlocked = "Unlocked"
    override val unlock = "Unlock"
    override val paidPrefix = "Paid "
    override fun paidSuffix(date: String) = " once, on $date. Nothing renews."
    override val wholeApp = "SmartSubscription, the whole app"
    override val once = "once"
    override val notASubscription = "Not a subscription. You pay one time and it is yours on this Google account."
    override val scanInvoice = "Scan an invoice"
    override val waiting = "Waiting for Google Play…"
    override val confirmInSheet = "Confirm in the Google Play sheet. Nothing is charged until you do."
    override fun unlockFor(price: String) = "Unlock for $price"
    override val onePayment = "One Google Play payment."
    override val restore = "Restore purchase"
    override val failedTitle = "The purchase didn’t go through"
    override val failedBody = "Google Play couldn’t finish it and you weren’t charged. Try again."
    override val restoredTitle = "Everything below is on"
    override val restoredBody = "Restoring on another device with the same Google account costs nothing."
    override val nowIncluded = "Now included"
    override fun whatAdds(price: String) = "What $price adds"
    override val scanTitle = "Scan invoices"
    override val scanBody = "Camera or screenshot. Read on this device, never uploaded."
    override val moreTitle = "More than 5 subscriptions"
    override val moreBody = "No cap on how many you track."
    override val analyticsTitle = "12 months of Analytics"
    override val analyticsBody = "Free shows the last and next 3."
    override val freeLead = "Free, and staying free: "
    override val freeRest = "entering by hand, renewal reminders, the Home rail and the Archive."
    override fun receiptDescription(monthly: String, price: String) =
        "Your subscriptions cost $monthly a month. This app costs $price once."
    override val yourSubscriptions = "Your subscriptions, a month"
    override val thisApp = "This app, ever"
}

private object UnlockVi : UnlockText {
    override val unlocked = "Đã mở khóa"
    override val unlock = "Mở khóa"
    override val paidPrefix = "Đã trả "
    override fun paidSuffix(date: String) = " một lần, ngày $date. Không gia hạn."
    override val wholeApp = "SmartSubscription, trọn bộ ứng dụng"
    override val once = "một lần"
    override val notASubscription = "Không phải gói đăng ký. Trả một lần là dùng mãi trên tài khoản Google này."
    override val scanInvoice = "Quét hóa đơn"
    override val waiting = "Đang chờ Google Play…"
    override val confirmInSheet = "Xác nhận trong cửa sổ Google Play. Chưa trừ tiền cho đến khi bạn xác nhận."
    override fun unlockFor(price: String) = "Mở khóa với $price"
    override val onePayment = "Một lần thanh toán qua Google Play."
    override val restore = "Khôi phục giao dịch"
    override val failedTitle = "Giao dịch chưa thành công"
    override val failedBody = "Google Play chưa hoàn tất và bạn chưa bị trừ tiền. Hãy thử lại."
    override val restoredTitle = "Mọi tính năng bên dưới đã bật"
    override val restoredBody = "Khôi phục trên máy khác cùng tài khoản Google hoàn toàn miễn phí."
    override val nowIncluded = "Đã bao gồm"
    override fun whatAdds(price: String) = "$price mở thêm"
    override val scanTitle = "Quét hóa đơn"
    override val scanBody = "Chụp ảnh hoặc ảnh màn hình. Đọc ngay trên máy, không tải lên."
    override val moreTitle = "Hơn 5 gói đăng ký"
    override val moreBody = "Theo dõi bao nhiêu gói cũng được."
    override val analyticsTitle = "Thống kê 12 tháng"
    override val analyticsBody = "Bản miễn phí xem 3 tháng trước và sau."
    override val freeLead = "Miễn phí, và luôn miễn phí: "
    override val freeRest = "nhập tay, nhắc gia hạn, danh sách ở Trang chủ và Lưu trữ."
    override fun receiptDescription(monthly: String, price: String) =
        "Các gói của bạn tốn $monthly mỗi tháng. Ứng dụng này tốn $price một lần."
    override val yourSubscriptions = "Các gói của bạn, mỗi tháng"
    override val thisApp = "Ứng dụng này, trọn đời"
}

val unlockText: UnlockText get() = if (isVietnamese) UnlockVi else UnlockEn
