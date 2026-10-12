package com.timeskip.smartsubscription.i18n

/** The add/edit form. */
interface FormText {
    val needName: String
    val needAmount: String
    val needValidAmount: String
    fun saveNeeds(missing: List<String>): String
    val pastDate: String

    val titleReview: String
    val titleByHand: String
    val previewEyebrow: String
    val namePlaceholder: String
    val savingPuts: String
    val onRailUnder: String
    fun savingTail(amount: String, cadenceA: String): String

    val filledFromInvoice: String
    val sectionSubscription: String
    val nameHint: String
    val nameLabel: String
    val providerHint: String
    val providerLabel: String
    val category: String
    val none: String

    val sectionPayment: String
    val amountLabel: String
    val billingCycle: String
    val nextBilling: String
    fun wholeNumbersOnly(currency: String): String
    val decimalsWithPeriod: String

    val sectionNotes: String
    val notesHint: String
    val notesLabel: String

    val groupInUse: String
    val groupAll: String

    val noteModel: String
    val notePatterns: String
    val noteModelDownloading: String
    val noteDatePrinted: String
    val noteDateFromInvoice: String
    val noteNoDate: String
    val notePickCategory: String
}

private object FormEn : FormText {
    override val needName = "a name"
    override val needAmount = "an amount"
    override val needValidAmount = "an amount that is a number above zero"
    override fun saveNeeds(missing: List<String>) = "Save needs ${missing.joinToString(" and ")}."
    override val pastDate = "a past date"

    override val titleReview = "Review"
    override val titleByHand = "Enter by hand"
    override val previewEyebrow = "On the rail it will look like this"
    override val namePlaceholder = "Name"
    override val savingPuts = "Saving puts "
    override val onRailUnder = " on the rail under "
    override fun savingTail(amount: String, cadenceA: String) = ", $amount $cadenceA."

    override val filledFromInvoice = "Filled from your invoice"
    override val sectionSubscription = "Subscription"
    override val nameHint = "Name, e.g. Netflix"
    override val nameLabel = "Name"
    override val providerHint = "Provider (optional)"
    override val providerLabel = "Provider"
    override val category = "Category"
    override val none = "None"

    override val sectionPayment = "Payment"
    override val amountLabel = "Amount"
    override val billingCycle = "Billing cycle"
    override val nextBilling = "Next billing"
    override fun wholeNumbersOnly(currency: String) = "$currency takes whole numbers only, like "
    override val decimalsWithPeriod = "Use a period for decimals, like "

    override val sectionNotes = "Notes"
    override val notesHint = "Anything worth remembering, like who shares it"
    override val notesLabel = "Notes"

    override val groupInUse = "In your subscriptions"
    override val groupAll = "All currencies"

    override val noteModel = "Read on this device by the on-device model."
    override val notePatterns = "Read on this device by pattern matching, which misses more than the on-device model, so check every field."
    override val noteModelDownloading = " The model is downloading and will be used next time."
    override val noteDatePrinted = "Next billing is the date printed on the invoice."
    override val noteDateFromInvoice = "Next billing is worked out from the invoice date."
    override val noteNoDate = "No date was found, so next billing is one cycle from today."
    override val notePickCategory = "Pick a category, then save."
}

private object FormVi : FormText {
    override val needName = "tên"
    override val needAmount = "số tiền"
    override val needValidAmount = "số tiền là một số lớn hơn 0"
    override fun saveNeeds(missing: List<String>) = "Cần nhập ${missing.joinToString(" và ")} để lưu."
    override val pastDate = "một ngày đã qua"

    override val titleReview = "Kiểm tra"
    override val titleByHand = "Nhập thủ công"
    override val previewEyebrow = "Trong danh sách sẽ hiện như thế này"
    override val namePlaceholder = "Tên"
    override val savingPuts = "Khi lưu, "
    override val onRailUnder = " sẽ nằm trong mục "
    override fun savingTail(amount: String, cadenceA: String) = ", $amount $cadenceA."

    override val filledFromInvoice = "Điền từ hóa đơn của bạn"
    override val sectionSubscription = "Gói đăng ký"
    override val nameHint = "Tên, ví dụ Netflix"
    override val nameLabel = "Tên"
    override val providerHint = "Nhà cung cấp (không bắt buộc)"
    override val providerLabel = "Nhà cung cấp"
    override val category = "Danh mục"
    override val none = "Không có"

    override val sectionPayment = "Thanh toán"
    override val amountLabel = "Số tiền"
    override val billingCycle = "Chu kỳ thanh toán"
    override val nextBilling = "Kỳ thanh toán tới"
    override fun wholeNumbersOnly(currency: String) = "$currency chỉ nhận số nguyên, ví dụ "
    override val decimalsWithPeriod = "Dùng dấu chấm cho phần thập phân, ví dụ "

    override val sectionNotes = "Ghi chú"
    override val notesHint = "Điều cần nhớ, ví dụ dùng chung với ai"
    override val notesLabel = "Ghi chú"

    override val groupInUse = "Trong các gói của bạn"
    override val groupAll = "Tất cả tiền tệ"

    override val noteModel = "Đã đọc trên máy bằng mô hình trên máy."
    override val notePatterns = "Đã đọc trên máy bằng so khớp mẫu, cách này dễ sót hơn mô hình trên máy, nên hãy kiểm tra từng mục."
    override val noteModelDownloading = " Mô hình đang được tải và sẽ dùng ở lần sau."
    override val noteDatePrinted = "Kỳ thanh toán tới là ngày in trên hóa đơn."
    override val noteDateFromInvoice = "Kỳ thanh toán tới được tính từ ngày hóa đơn."
    override val noteNoDate = "Không tìm thấy ngày, nên kỳ thanh toán tới là một chu kỳ tính từ hôm nay."
    override val notePickCategory = "Chọn danh mục rồi lưu."
}

val formText: FormText get() = if (isVietnamese) FormVi else FormEn
