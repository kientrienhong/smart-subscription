package com.smartsubscription.i18n

/** The Add screen, the scan screen and the scanner's error messages. */
interface ScanText {
    fun scanIsPartOfUnlock(price: String): String
    val backToSubscriptions: String
    val addEyebrow: String
    val addTitle: String
    val addSub: String
    val scanInvoice: String
    val scanInvoiceBody: String
    val readsOnDevice: String
    fun partOfUnlock(price: String): String
    val choosePhoto: String
    val choosePhotoBody: String
    val enterByHand: String
    val enterByHandBody: String
    val reviewEveryField: String

    val invoice: String
    val fromCamera: String
    val fromPhotos: String
    val takePhoto: String
    val readingText: String
    val findingDetails: String
    val foundIt: String
    val noTextFound: String
    val couldNotRead: String
    val checkDetails: String
    val onDevice: String
    val reviewAndSave: String
    val opensForm: String
    val scanAgain: String
    val chooseAnother: String
    val reading: String
    val findingDetailsBusy: String
    val yourScan: String
    val yourPhoto: String
    val noReadableText: String
    fun linesRead(n: Int): String
    val staysOnPhone: String
    val stepRead: String
    val nothingReadable: String
    fun lines(n: Int): String
    val mlKitOnDevice: String
    val stepFind: String
    fun found(n: Int): String
    val onDeviceModel: String
    val patternMatching: String
    val findHint: String
    val stepCheck: String
    val nothingSaved: String
    val foundOnInvoice: String
    val enterByHandInstead: String
    val provider: String
    val subscription: String
    val amount: String
    val billing: String
    val invoiceDate: String
    val nextPayment: String
    val notFound: String
    val category: String
    val categoryNotOnInvoice: String
    val invoiceImage: String

    val errUnreadableImage: String
    val errNoExtract: String
    val errNoText: String
}

private object ScanEn : ScanText {
    override fun scanIsPartOfUnlock(price: String) = "Scanning is part of the $price unlock."
    override val backToSubscriptions = "Back to Subscriptions"
    override val addEyebrow = "New"
    override val addTitle = "Add a subscription"
    override val addSub = "Scanning runs on this device. Nothing is uploaded."
    override val scanInvoice = "Scan an invoice"
    override val scanInvoiceBody = "Point the camera at a receipt. It reads the provider, amount, date and billing cycle."
    override val readsOnDevice = "Reads on this device."
    override fun partOfUnlock(price: String) = "Part of the $price unlock. Reads on this device."
    override val choosePhoto = "Choose a photo"
    override val choosePhotoBody = "An invoice screenshot from your gallery works the same way."
    override val enterByHand = "Enter by hand"
    override val enterByHandBody = "Name, price and the next renewal date."
    override val reviewEveryField = "Whichever way you add it, you review every field before it is saved."

    override val invoice = "Invoice"
    override val fromCamera = "From the camera"
    override val fromPhotos = "From your photos"
    override val takePhoto = "Take a photo"
    override val readingText = "Reading the text"
    override val findingDetails = "Finding the details"
    override val foundIt = "Found it"
    override val noTextFound = "No text found"
    override val couldNotRead = "Couldn’t read it"
    override val checkDetails = "Check the details below, then save."
    override val onDevice = "On this device. Nothing is uploaded."
    override val reviewAndSave = "Review and save"
    override val opensForm = "Opens the form with these filled in."
    override val scanAgain = "Scan again"
    override val chooseAnother = "Choose another photo"
    override val reading = "Reading…"
    override val findingDetailsBusy = "Finding the details…"
    override val yourScan = "Your scan"
    override val yourPhoto = "Your photo"
    override val noReadableText = "No readable text in it"
    override fun linesRead(n: Int) = "$n lines of text read"
    override val staysOnPhone = "Stays on this phone"
    override val stepRead = "Read the text"
    override val nothingReadable = "Nothing readable"
    override fun lines(n: Int) = "$n lines"
    override val mlKitOnDevice = "ML Kit, on this device"
    override val stepFind = "Find the details"
    override fun found(n: Int) = "$n of 5 found · "
    override val onDeviceModel = "on-device model"
    override val patternMatching = "pattern matching"
    override val findHint = "Provider, amount, date, billing"
    override val stepCheck = "You check and save"
    override val nothingSaved = "Nothing is saved until you do"
    override val foundOnInvoice = "Found on the invoice"
    override val enterByHandInstead = "Enter by hand instead"
    override val provider = "Provider"
    override val subscription = "Subscription"
    override val amount = "Amount"
    override val billing = "Billing"
    override val invoiceDate = "Invoice date"
    override val nextPayment = "Next payment"
    override val notFound = "Not found"
    override val category = "Category"
    override val categoryNotOnInvoice = "Not on invoices; you pick it"
    override val invoiceImage = "The invoice image"

    override val errUnreadableImage = "That image couldn't be read. Try a clearer photo of the invoice."
    override val errNoExtract = "Couldn't extract text from the image. Try a clearer photo of the invoice."
    override val errNoText = "The photo may be blurry, dark or cut off. Try again with the whole receipt in frame."
}

private object ScanVi : ScanText {
    override fun scanIsPartOfUnlock(price: String) = "Quét hóa đơn nằm trong gói mở khóa $price."
    override val backToSubscriptions = "Quay lại Gói đăng ký"
    override val addEyebrow = "Mới"
    override val addTitle = "Thêm gói đăng ký"
    override val addSub = "Quét ngay trên máy. Không tải gì lên."
    override val scanInvoice = "Quét hóa đơn"
    override val scanInvoiceBody = "Hướng camera vào hóa đơn. Ứng dụng đọc nhà cung cấp, số tiền, ngày và chu kỳ thanh toán."
    override val readsOnDevice = "Đọc ngay trên máy."
    override fun partOfUnlock(price: String) = "Nằm trong gói mở khóa $price. Đọc ngay trên máy."
    override val choosePhoto = "Chọn ảnh"
    override val choosePhotoBody = "Ảnh chụp màn hình hóa đơn trong thư viện cũng dùng được."
    override val enterByHand = "Nhập thủ công"
    override val enterByHandBody = "Tên, giá và ngày gia hạn tới."
    override val reviewEveryField = "Dù thêm bằng cách nào, bạn đều kiểm tra từng mục trước khi lưu."

    override val invoice = "Hóa đơn"
    override val fromCamera = "Từ camera"
    override val fromPhotos = "Từ thư viện ảnh"
    override val takePhoto = "Chụp ảnh"
    override val readingText = "Đang đọc chữ"
    override val findingDetails = "Đang tìm thông tin"
    override val foundIt = "Đã tìm thấy"
    override val noTextFound = "Không thấy chữ"
    override val couldNotRead = "Không đọc được"
    override val checkDetails = "Kiểm tra thông tin bên dưới rồi lưu."
    override val onDevice = "Ngay trên máy. Không tải gì lên."
    override val reviewAndSave = "Kiểm tra và lưu"
    override val opensForm = "Mở biểu mẫu đã điền sẵn các thông tin này."
    override val scanAgain = "Quét lại"
    override val chooseAnother = "Chọn ảnh khác"
    override val reading = "Đang đọc…"
    override val findingDetailsBusy = "Đang tìm thông tin…"
    override val yourScan = "Ảnh quét của bạn"
    override val yourPhoto = "Ảnh của bạn"
    override val noReadableText = "Không có chữ đọc được"
    override fun linesRead(n: Int) = "Đã đọc $n dòng chữ"
    override val staysOnPhone = "Chỉ lưu trên điện thoại này"
    override val stepRead = "Đọc chữ"
    override val nothingReadable = "Không đọc được gì"
    override fun lines(n: Int) = "$n dòng"
    override val mlKitOnDevice = "ML Kit, ngay trên máy"
    override val stepFind = "Tìm thông tin"
    override fun found(n: Int) = "Tìm thấy $n/5 · "
    override val onDeviceModel = "mô hình trên máy"
    override val patternMatching = "so khớp mẫu"
    override val findHint = "Nhà cung cấp, số tiền, ngày, thanh toán"
    override val stepCheck = "Bạn kiểm tra và lưu"
    override val nothingSaved = "Chưa lưu gì cho đến khi bạn lưu"
    override val foundOnInvoice = "Tìm thấy trên hóa đơn"
    override val enterByHandInstead = "Nhập thủ công"
    override val provider = "Nhà cung cấp"
    override val subscription = "Gói đăng ký"
    override val amount = "Số tiền"
    override val billing = "Thanh toán"
    override val invoiceDate = "Ngày hóa đơn"
    override val nextPayment = "Kỳ thanh toán tới"
    override val notFound = "Không tìm thấy"
    override val category = "Danh mục"
    override val categoryNotOnInvoice = "Không có trên hóa đơn; bạn tự chọn"
    override val invoiceImage = "Ảnh hóa đơn"

    override val errUnreadableImage = "Không đọc được ảnh này. Hãy thử ảnh hóa đơn rõ hơn."
    override val errNoExtract = "Không lấy được chữ từ ảnh. Hãy thử ảnh hóa đơn rõ hơn."
    override val errNoText = "Ảnh có thể bị mờ, tối hoặc bị cắt. Hãy chụp lại với cả hóa đơn trong khung."
}

val scanText: ScanText get() = if (isVietnamese) ScanVi else ScanEn
