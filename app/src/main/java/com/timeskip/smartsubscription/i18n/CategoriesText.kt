package com.timeskip.smartsubscription.i18n

import com.timeskip.smartsubscription.data.plural

/** Settings → Manage categories, the category editor, and the default category names. */
interface CategoriesText {
    /** Names of the seeded categories, by their stable id. */
    fun defaultName(id: String): String
    val notUsedYet: String
    fun usage(count: Int, perMonth: String): String
    fun deleteTitle(name: String): String
    fun deleteBody(count: Int): String
    val backToSettings: String
    val settings: String
    val title: String
    fun inUse(used: Int, total: Int): String
    val empty: String
    val newCategory: String
    val reorderFooter: String
    val editFooter: String
    fun deleteNamed(name: String): String
    fun moveUp(name: String): String
    fun moveDown(name: String): String
    val editCategory: String
    val editorEyebrow: String
    val name: String
    val needsName: String
    fun duplicate(name: String): String
    fun renameMoves(count: Int): String
    val nothingUsesIt: String
    val newGoesLast: String
    val categoryName: String
    val icon: String
    val deleteCategory: String
}

private object CategoriesEn : CategoriesText {
    override fun defaultName(id: String) = when (id) {
        "entertainment" -> "Entertainment"
        "productivity" -> "Productivity"
        "cloud" -> "Cloud Storage"
        "music" -> "Music"
        "gaming" -> "Gaming"
        "news" -> "News & Media"
        "finance" -> "Finance"
        else -> "Other"
    }
    override val notUsedYet = "Not used yet"
    override fun usage(count: Int, perMonth: String) = "${plural(count, "subscription")} · $perMonth a month"
    override fun deleteTitle(name: String) = "Delete “$name”?"
    override fun deleteBody(count: Int) = "Its ${plural(count, "subscription")} move to No category. They are not deleted."
    override val backToSettings = "Back to Settings"
    override val settings = "Settings"
    override val title = "Categories"
    override fun inUse(used: Int, total: Int) = "$used of $total in use. This order is the order of Home’s filters and the category menu."
    override val empty = "No categories. Subscriptions still work without one; add a category to filter Home by it."
    override val newCategory = "New category"
    override val reorderFooter = "Move categories up or down, or delete one. Deleting keeps its subscriptions; they move to No category."
    override val editFooter = "Tap a category to rename it or change its icon. Deleting one keeps its subscriptions; they move to No category."
    override fun deleteNamed(name: String) = "Delete $name"
    override fun moveUp(name: String) = "Move $name up"
    override fun moveDown(name: String) = "Move $name down"
    override val editCategory = "Edit category"
    override val editorEyebrow = "In Home’s filters and on the rail"
    override val name = "Name"
    override val needsName = "Save needs a name."
    override fun duplicate(name: String) = "There is already a category called $name."
    override fun renameMoves(count: Int) = "Renaming moves its ${plural(count, "subscription")} with it."
    override val nothingUsesIt = "Nothing uses it yet."
    override val newGoesLast = "New categories go to the end of the list."
    override val categoryName = "Category name"
    override val icon = "Icon"
    override val deleteCategory = "Delete category"
}

private object CategoriesVi : CategoriesText {
    override fun defaultName(id: String) = when (id) {
        "entertainment" -> "Giải trí"
        "productivity" -> "Năng suất"
        "cloud" -> "Lưu trữ đám mây"
        "music" -> "Âm nhạc"
        "gaming" -> "Trò chơi"
        "news" -> "Tin tức & Báo chí"
        "finance" -> "Tài chính"
        else -> "Khác"
    }
    override val notUsedYet = "Chưa dùng"
    override fun usage(count: Int, perMonth: String) = "$count gói · $perMonth mỗi tháng"
    override fun deleteTitle(name: String) = "Xóa “$name”?"
    override fun deleteBody(count: Int) = "$count gói trong danh mục này sẽ chuyển sang Chưa có danh mục. Các gói không bị xóa."
    override val backToSettings = "Quay lại Cài đặt"
    override val settings = "Cài đặt"
    override val title = "Danh mục"
    override fun inUse(used: Int, total: Int) = "Đang dùng $used/$total. Thứ tự này cũng là thứ tự bộ lọc ở Trang chủ và menu danh mục."
    override val empty = "Chưa có danh mục. Gói vẫn dùng được khi không có danh mục; thêm danh mục để lọc Trang chủ theo nó."
    override val newCategory = "Danh mục mới"
    override val reorderFooter = "Đưa danh mục lên hoặc xuống, hoặc xóa đi. Khi xóa, các gói vẫn còn và chuyển sang Chưa có danh mục."
    override val editFooter = "Chạm vào danh mục để đổi tên hoặc biểu tượng. Khi xóa, các gói vẫn còn và chuyển sang Chưa có danh mục."
    override fun deleteNamed(name: String) = "Xóa $name"
    override fun moveUp(name: String) = "Đưa $name lên"
    override fun moveDown(name: String) = "Đưa $name xuống"
    override val editCategory = "Sửa danh mục"
    override val editorEyebrow = "Trong bộ lọc Trang chủ và trên danh sách"
    override val name = "Tên"
    override val needsName = "Cần có tên để lưu."
    override fun duplicate(name: String) = "Đã có danh mục tên $name."
    override fun renameMoves(count: Int) = "Đổi tên vẫn giữ $count gói trong danh mục."
    override val nothingUsesIt = "Chưa có gói nào dùng."
    override val newGoesLast = "Danh mục mới sẽ nằm cuối danh sách."
    override val categoryName = "Tên danh mục"
    override val icon = "Biểu tượng"
    override val deleteCategory = "Xóa danh mục"
}

val categoriesText: CategoriesText get() = if (isVietnamese) CategoriesVi else CategoriesEn
