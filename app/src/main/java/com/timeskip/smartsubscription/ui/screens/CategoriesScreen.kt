package com.timeskip.smartsubscription.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timeskip.smartsubscription.AppState
import com.timeskip.smartsubscription.AppViewModel
import com.timeskip.smartsubscription.data.Category
import com.timeskip.smartsubscription.data.DemoData
import com.timeskip.smartsubscription.data.Status
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.ui.components.BackButton
import com.timeskip.smartsubscription.ui.components.BigText
import com.timeskip.smartsubscription.ui.components.CardList
import com.timeskip.smartsubscription.ui.components.CategoryGlyph
import com.timeskip.smartsubscription.ui.components.CategoryGlyphs
import com.timeskip.smartsubscription.ui.components.Eyebrow
import com.timeskip.smartsubscription.ui.components.Footer
import com.timeskip.smartsubscription.ui.components.Glyphs
import com.timeskip.smartsubscription.ui.components.IconBtn
import com.timeskip.smartsubscription.ui.components.InkBlock
import com.timeskip.smartsubscription.ui.components.InkNav
import com.timeskip.smartsubscription.ui.components.InkSub
import com.timeskip.smartsubscription.ui.components.ListRow
import com.timeskip.smartsubscription.ui.components.SectionLabel
import com.timeskip.smartsubscription.ui.components.TextBtn
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SsType
import com.timeskip.smartsubscription.i18n.categoriesText
import com.timeskip.smartsubscription.i18n.commonText

private data class Usage(val count: Int, val perMonth: Double)

private fun AppState.usage(id: String): Usage {
    val subs = subscriptions.filter { it.categoryId == id && it.status == Status.ACTIVE }
    return Usage(subs.size, subs.filter { it.currency == currency }.sumOf { it.perMonth })
}

private fun Usage.text(currency: String) =
    if (count == 0) categoriesText.notUsedYet else categoriesText.usage(count, money(perMonth, currency))

/**
 * Settings → Manage categories. Each row says what the category holds and costs a month, the two facts
 * you need before renaming or deleting it. Edit reorders them; the order drives Home's filters and the menu.
 */
@Composable
fun CategoriesScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    var reorder by rememberSaveable { mutableStateOf(false) }
    // null: the list. "": a new category. Otherwise the id being edited.
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var asking by rememberSaveable { mutableStateOf<String?>(null) }

    fun askDelete(id: String) {
        // One in use asks first and says what happens; an unused one goes at once.
        if (state.usage(id).count == 0) {
            vm.deleteCategory(id)
            editing = null
        } else {
            asking = id
        }
    }

    val edit = editing
    if (edit != null) {
        CategoryEditor(state, state.category(edit.ifEmpty { null }), onCancel = { editing = null }, onDelete = { askDelete(edit) }) { name, icon ->
            vm.saveCategory(edit.ifEmpty { null }, name, icon)
            editing = null
        }
    } else {
        CategoryList(state, reorder, onToggleReorder = { reorder = !reorder }, onBack = { nav.popBackStack() }, onEdit = { editing = it }, onDelete = ::askDelete) { id, by ->
            vm.moveCategory(id, by)
        }
    }

    asking?.let { id ->
        val cat = state.category(id)
        val n = state.usage(id).count
        if (cat == null) {
            asking = null
            return@let
        }
        val ss = LocalSs.current
        AlertDialog(
            onDismissRequest = { asking = null },
            title = { Text(categoriesText.deleteTitle(cat.name), style = SsType.h3, color = ss.text) },
            text = { Text(categoriesText.deleteBody(n), style = SsType.sub, color = ss.text2) },
            confirmButton = {
                TextBtn(commonText.delete, ss.danger, bold = true) {
                    vm.deleteCategory(id)
                    asking = null
                    editing = null
                }
            },
            dismissButton = { TextBtn(commonText.cancel) { asking = null } },
            containerColor = ss.card,
        )
    }
}

@Composable
private fun CategoryList(
    state: AppState,
    reorder: Boolean,
    onToggleReorder: () -> Unit,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (String, Int) -> Unit,
) {
    val ss = LocalSs.current
    val used = state.categories.count { state.usage(it.id).count > 0 }
    InkScreen(
        ink = {
            InkBlock(bottom = 18.dp) {
                InkNav(
                    leading = { BackButton(categoriesText.backToSettings, onBack) },
                    trailing = { TextBtn(if (reorder) commonText.done else commonText.edit, ss.onInk, bold = reorder, onClick = onToggleReorder) },
                )
                Eyebrow(categoriesText.settings)
                BigText(categoriesText.title)
                InkSub(categoriesText.inUse(used, state.categories.size))
            }
        },
    ) {
        if (state.categories.isEmpty()) {
            Footer(categoriesText.empty)
        } else {
            CardList(
                rows = state.categories.mapIndexed { i, c ->
                    @Composable {
                        CategoryRow(
                            c, state.usage(c.id).text(state.currency), reorder,
                            first = i == 0, last = i == state.categories.lastIndex,
                            onClick = { onEdit(c.id) }, onDelete = { onDelete(c.id) }, onMove = { onMove(c.id, it) },
                        )
                    }
                },
            )
        }
        CardList(
            rows = listOf(
                {
                    ListRow(onClick = { onEdit("") }) {
                        Icon(Glyphs.plusSmall, null, tint = ss.brand, modifier = Modifier.size(18.dp))
                        Text(categoriesText.newCategory, style = SsType.body, color = ss.brand)
                    }
                },
            ),
        )
        Footer(
            if (reorder) categoriesText.reorderFooter else categoriesText.editFooter,
        )
    }
}

@Composable
private fun CategoryRow(
    c: Category,
    usage: String,
    reorder: Boolean,
    first: Boolean,
    last: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMove: (Int) -> Unit,
) {
    val ss = LocalSs.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(if (!reorder) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = if (reorder) 4.dp else 16.dp, end = if (reorder) 4.dp else 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (reorder) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onDelete)
                    .semantics { contentDescription = categoriesText.deleteNamed(c.name) },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(22.dp).clip(RoundedCornerShape(11.dp)).background(ss.danger), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 8.dp, height = 2.dp).background(Color.White))
                }
            }
        }
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(ss.card2),
            contentAlignment = Alignment.Center,
        ) { CategoryGlyph(c.icon, ss.text, size = 18.dp) }
        Column(Modifier.weight(1f)) {
            Text(c.name, style = SsType.bodyStrong, color = ss.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(usage, style = SsType.caption, color = ss.text2)
        }
        if (reorder) {
            MoveButton(Glyphs.up, categoriesText.moveUp(c.name), enabled = !first) { onMove(-1) }
            MoveButton(Glyphs.down, categoriesText.moveDown(c.name), enabled = !last) { onMove(1) }
        } else {
            Icon(Glyphs.chevron, null, tint = ss.text2, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun MoveButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    val ss = LocalSs.current
    if (enabled) IconBtn(icon, description, ss.brand, onClick, size = 14.dp)
    else Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Icon(icon, null, tint = ss.line, modifier = Modifier.size(14.dp)) }
}

@Composable
private fun CategoryEditor(
    state: AppState,
    original: Category?,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    val ss = LocalSs.current
    var name by rememberSaveable { mutableStateOf(original?.name ?: "") }
    var icon by rememberSaveable { mutableStateOf(original?.icon ?: "tag") }
    val trimmed = name.trim()
    val duplicate = state.categories.any { it.name.equals(trimmed, ignoreCase = true) && it.id != original?.id }
    val ok = trimmed.isNotEmpty() && !duplicate
    val usage = original?.let { state.usage(it.id) }

    InkScreen(
        ink = {
            InkBlock(bottom = 18.dp) {
                InkNav(
                    title = if (original != null) categoriesText.editCategory else categoriesText.newCategory,
                    leading = { TextBtn(commonText.cancel, ss.onInk, onClick = onCancel) },
                    trailing = { TextBtn(commonText.save, ss.onInk, bold = true, enabled = ok) { onSave(trimmed, icon) } },
                )
                Eyebrow(categoriesText.editorEyebrow)
                Row(
                    Modifier
                        .padding(top = 10.dp)
                        .border(1.dp, ss.ink2, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryGlyph(icon, ss.onInk2, size = 18.dp)
                    Text(trimmed.ifEmpty { categoriesText.name }, style = SsType.bodyStrong, color = ss.onInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                InkSub(
                    when {
                        trimmed.isEmpty() -> categoriesText.needsName
                        duplicate -> categoriesText.duplicate(trimmed)
                        usage != null && usage.count > 0 -> categoriesText.renameMoves(usage.count)
                        usage != null -> categoriesText.nothingUsesIt
                        else -> categoriesText.newGoesLast
                    },
                )
            }
        },
    ) {
        Column {
            SectionLabel(categoriesText.name, Modifier.padding(top = 0.dp))
            CardList(
                rows = listOf(
                    {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            textStyle = SsType.body.copy(color = ss.text),
                            cursorBrush = SolidColor(ss.brand),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp)
                                .semantics { contentDescription = categoriesText.categoryName },
                            decorationBox = { inner ->
                                Box {
                                    if (name.isEmpty()) Text(categoriesText.categoryName, style = SsType.body, color = ss.text2)
                                    inner()
                                }
                            },
                        )
                    },
                ),
            )
        }
        Column {
            SectionLabel(categoriesText.icon, Modifier.padding(top = 0.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ss.card)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(DemoData.iconChoices) { choice ->
                    val on = choice == icon
                    Box(
                        Modifier
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (on) ss.brandTint else ss.card2)
                            .border(1.5.dp, if (on) ss.brand else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { icon = choice }
                            .semantics {
                                contentDescription = CategoryGlyphs.names[choice] ?: choice
                                selected = on
                            },
                        contentAlignment = Alignment.Center,
                    ) { CategoryGlyph(choice, if (on) ss.brand else ss.text, size = 22.dp) }
                }
            }
        }
        if (original != null) {
            TextBtn(categoriesText.deleteCategory, ss.danger, modifier = Modifier.align(Alignment.CenterHorizontally), onClick = onDelete)
        }
    }
}
