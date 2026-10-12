package com.smartsubscription.i18n

import com.smartsubscription.data.plural

/** Home: the ink header, the rail and the bottom bar. */
interface HomeText {
    val title: String
    val firstRunTitle: String
    val firstRunBody: String
    val firstRunAction: String
    fun nothingIn(category: String): String
    val otherCategoriesBody: String
    val showAll: String
    val nothingActiveTitle: String
    val nothingActiveBody: String
    fun moreLater(count: Int, total: String?): String

    val toCome: String
    val nothingRenewing: String
    fun monthYear(month: String, year: Int): String
    val stillToCome: String
    val next: String
    fun plusElsewhereToCome(sums: String): String
    fun foreignNoneDue(count: Int): String
    val next12Months: String
    val aYear: String
    fun yearSummary(count: Int, yearly: Int): String
    fun plusElsewhereYearly(sums: String): String
    val thisMonth: String
    val yearAhead: String

    fun ledgerDescription(charged: String, toCome: String, yearly: String?): String
    val legendCharged: String
    val legendYearly: String
    val legendAllIn: String

    val all: String
    fun rowDescription(name: String, category: String, price: String, renews: String, due: String): String

    val analytics: String
    val add: String
    val settings: String
}

private object HomeEn : HomeText {
    override val title = "Subscriptions"
    override val firstRunTitle = "No subscriptions yet"
    override val firstRunBody = "Add your first subscription to start tracking your spend. Scan a receipt, or type it in."
    override val firstRunAction = "Add a subscription"
    override fun nothingIn(category: String) = "Nothing in $category"
    override val otherCategoriesBody = "You have other subscriptions filed under other categories."
    override val showAll = "Show all"
    override val nothingActiveTitle = "Nothing active right now"
    override val nothingActiveBody = "Your subscriptions are paused, cancelled or archived. They are safe in the Archive, nothing has been deleted."
    override fun moreLater(count: Int, total: String?) =
        "$count more later in the year${total?.let { ", $it in total" }.orEmpty()}. Switch to Year ahead to see them."

    override val toCome = "to come"
    override val nothingRenewing = "Nothing is renewing."
    override fun monthYear(month: String, year: Int) = "$month $year"
    override val stillToCome = "still to come"
    override val next = "Next: "
    override fun plusElsewhereToCome(sums: String) = "Plus $sums still to come. No exchange rate offline, so it's kept apart."
    override fun foreignNoneDue(count: Int) = "$count in another currency, none due this month. No exchange rate offline."
    override val next12Months = "Next 12 months"
    override val aYear = "a year"
    override fun yearSummary(count: Int, yearly: Int) =
        "$count subscriptions" + (if (yearly > 0) ", $yearly billed yearly" else "") + ". Grouped by the month each one lands."
    override fun plusElsewhereYearly(sums: String) = "Plus $sums a year. No exchange rate offline, so it's kept apart."
    override val thisMonth = "This month"
    override val yearAhead = "Year ahead"

    override fun ledgerDescription(charged: String, toCome: String, yearly: String?) =
        "$charged charged so far this month, $toCome still to come" + (yearly?.let { ", $it of it yearly" }.orEmpty())
    override val legendCharged = " charged"
    override val legendYearly = " yearly"
    override val legendAllIn = " a month, all in"

    override val all = "All"
    override fun rowDescription(name: String, category: String, price: String, renews: String, due: String) =
        "$name, $category, $price, renews $renews, $due"

    override val analytics = "Analytics"
    override val add = "Add"
    override val settings = "Settings"
}

private object HomeVi : HomeText {
    override val title = "Gói đăng ký"
    override val firstRunTitle = "Chưa có gói đăng ký nào"
    override val firstRunBody = "Thêm gói đầu tiên để bắt đầu theo dõi chi tiêu. Quét hóa đơn, hoặc tự nhập."
    override val firstRunAction = "Thêm gói đăng ký"
    override fun nothingIn(category: String) = "Không có gói nào trong $category"
    override val otherCategoriesBody = "Bạn có các gói khác thuộc danh mục khác."
    override val showAll = "Xem tất cả"
    override val nothingActiveTitle = "Hiện không có gói nào đang dùng"
    override val nothingActiveBody = "Các gói của bạn đang tạm dừng, đã hủy hoặc đã lưu trữ. Chúng vẫn nằm an toàn trong Lưu trữ, không có gì bị xóa."
    override fun moreLater(count: Int, total: String?) =
        "Còn $count gói khác trong năm${total?.let { ", tổng $it" }.orEmpty()}. Chuyển sang 12 tháng tới để xem."

    override val toCome = "sắp tới"
    override val nothingRenewing = "Không có gói nào sắp gia hạn."
    override fun monthYear(month: String, year: Int) = "$month, $year"
    override val stillToCome = "còn phải trả"
    override val next = "Tiếp theo: "
    override fun plusElsewhereToCome(sums: String) = "Cộng thêm $sums còn phải trả. Đang offline nên chưa có tỷ giá, khoản này được tính riêng."
    override fun foreignNoneDue(count: Int) = "$count gói tính bằng tiền tệ khác, không có kỳ nào trong tháng này. Đang offline nên chưa có tỷ giá."
    override val next12Months = "12 tháng tới"
    override val aYear = "mỗi năm"
    override fun yearSummary(count: Int, yearly: Int) =
        "$count gói đăng ký" + (if (yearly > 0) ", $yearly gói thanh toán hàng năm" else "") + ". Xếp theo tháng mỗi gói đến hạn."
    override fun plusElsewhereYearly(sums: String) = "Cộng thêm $sums mỗi năm. Đang offline nên chưa có tỷ giá, khoản này được tính riêng."
    override val thisMonth = "Tháng này"
    override val yearAhead = "12 tháng tới"

    override fun ledgerDescription(charged: String, toCome: String, yearly: String?) =
        "Tháng này đã trừ $charged, còn phải trả $toCome" + (yearly?.let { ", trong đó $it là gói hàng năm" }.orEmpty())
    override val legendCharged = " đã trừ"
    override val legendYearly = " hàng năm"
    override val legendAllIn = " mỗi tháng, tính cả"

    override val all = "Tất cả"
    override fun rowDescription(name: String, category: String, price: String, renews: String, due: String) =
        "$name, $category, $price, gia hạn $renews, $due"

    override val analytics = "Thống kê"
    override val add = "Thêm"
    override val settings = "Cài đặt"
}

val homeText: HomeText get() = if (isVietnamese) HomeVi else HomeEn

/** Archive: stopped subscriptions and what they cost. */
interface ArchiveText {
    val paused: String
    val pausedWhy: String
    val cancelled: String
    val cancelledWhy: String
    val archived: String
    val archivedWhy: String
    val title: String
    val backToSettings: String
    val eyebrow: String
    val spent: String
    val nothingStopped: String
    fun stoppedSummary(count: Int): String
    val dismiss: String
    val emptyTitle: String
    val emptyBody: String
    fun deleteTitle(name: String): String
    fun deleteBody(name: String): String
    fun deletePayments(count: Int, amount: String): String
    val archiveKeeps: String
    val keepArchived: String
    val deletePermanently: String
    fun accessEnded(date: String): String
    fun accessUntil(date: String): String
    val resume: String
    val reactivate: String
    val archive: String
}

private object ArchiveEn : ArchiveText {
    override val paused = "Paused"
    override val pausedWhy = "Not billing. Resume and the next charge lands in the future — you’re not back-billed for the pause."
    override val cancelled = "Cancelled"
    override val cancelledWhy = "Won’t bill again. The spend already on record still counts."
    override val archived = "Archived"
    override val archivedWhy = "Filed away. Kept so past spend stays in your analytics."
    override val title = "Archive"
    override val backToSettings = "Back to Settings"
    override val eyebrow = "What they cost while they ran"
    override val spent = "spent"
    override val nothingStopped = "Nothing paused or cancelled."
    override fun stoppedSummary(count: Int) =
        "On ${plural(count, "subscription")} you no longer pay for. It stays in Analytics while they are here."
    override val dismiss = "Dismiss"
    override val emptyTitle = "Nothing archived yet"
    override val emptyBody =
        "When you pause or cancel a subscription it moves here instead of disappearing, along with everything it has cost you so far. " +
            "Keeping it here is what keeps its past payments in your analytics — deleting one throws that history away."
    override fun deleteTitle(name: String) = "Delete $name permanently?"
    override fun deleteBody(name: String) = "Deleting $name removes it permanently. This can’t be undone."
    override fun deletePayments(count: Int, amount: String) =
        "This also deletes ${plural(count, "recorded payment")}, so $amount will disappear from your analytics."
    override val archiveKeeps = "Archiving keeps the spend history instead."
    override val keepArchived = "Keep it archived"
    override val deletePermanently = "Delete permanently"
    override fun accessEnded(date: String) = "Access ended $date"
    override fun accessUntil(date: String) = "Access until $date"
    override val resume = "Resume"
    override val reactivate = "Reactivate"
    override val archive = "Archive"
}

private object ArchiveVi : ArchiveText {
    override val paused = "Tạm dừng"
    override val pausedWhy = "Không thanh toán. Khi tiếp tục, kỳ trừ tiền tới sẽ tính từ sau đó — bạn không bị truy thu cho thời gian tạm dừng."
    override val cancelled = "Đã hủy"
    override val cancelledWhy = "Sẽ không trừ tiền nữa. Số tiền đã ghi nhận vẫn được tính."
    override val archived = "Đã lưu trữ"
    override val archivedWhy = "Đã cất đi. Được giữ lại để chi tiêu cũ vẫn nằm trong thống kê."
    override val title = "Lưu trữ"
    override val backToSettings = "Quay lại Cài đặt"
    override val eyebrow = "Số tiền đã chi khi còn dùng"
    override val spent = "đã chi"
    override val nothingStopped = "Không có gói nào tạm dừng hay đã hủy."
    override fun stoppedSummary(count: Int) =
        "Cho $count gói bạn không còn trả. Khoản này vẫn nằm trong Thống kê khi các gói còn ở đây."
    override val dismiss = "Đóng"
    override val emptyTitle = "Chưa có gì trong lưu trữ"
    override val emptyBody =
        "Khi bạn tạm dừng hoặc hủy một gói, gói đó chuyển vào đây thay vì biến mất, cùng toàn bộ số tiền nó đã tốn. " +
            "Giữ ở đây giúp các khoản đã trả vẫn nằm trong thống kê — xóa đi là mất luôn lịch sử đó."
    override fun deleteTitle(name: String) = "Xóa vĩnh viễn $name?"
    override fun deleteBody(name: String) = "Xóa $name sẽ gỡ bỏ vĩnh viễn. Không thể hoàn tác."
    override fun deletePayments(count: Int, amount: String) =
        "Thao tác này cũng xóa $count khoản đã ghi nhận, nên $amount sẽ biến mất khỏi thống kê."
    override val archiveKeeps = "Lưu trữ thì vẫn giữ được lịch sử chi tiêu."
    override val keepArchived = "Giữ trong lưu trữ"
    override val deletePermanently = "Xóa vĩnh viễn"
    override fun accessEnded(date: String) = "Hết quyền dùng từ $date"
    override fun accessUntil(date: String) = "Còn dùng đến $date"
    override val resume = "Tiếp tục"
    override val reactivate = "Kích hoạt lại"
    override val archive = "Lưu trữ"
}

val archiveText: ArchiveText get() = if (isVietnamese) ArchiveVi else ArchiveEn
