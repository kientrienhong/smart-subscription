package com.smartsubscription.ui.screens

import com.smartsubscription.i18n.archiveText
import com.smartsubscription.i18n.commonText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.smartsubscription.AppState
import com.smartsubscription.AppViewModel
import com.smartsubscription.data.Status
import com.smartsubscription.data.Subscription
import com.smartsubscription.data.longText
import com.smartsubscription.data.money
import com.smartsubscription.ui.components.BackButton
import com.smartsubscription.ui.components.Banner
import com.smartsubscription.ui.components.BigFigure
import com.smartsubscription.ui.components.CardList
import com.smartsubscription.ui.components.CategoryGlyph
import com.smartsubscription.ui.components.Eyebrow
import com.smartsubscription.ui.components.Footer
import com.smartsubscription.ui.components.Glyphs
import com.smartsubscription.ui.components.IconBtn
import com.smartsubscription.ui.components.InkBlock
import com.smartsubscription.ui.components.InkNav
import com.smartsubscription.ui.components.InkSub
import com.smartsubscription.ui.components.SecondaryButton
import com.smartsubscription.ui.components.TextBtn
import com.smartsubscription.ui.theme.LocalSs
import com.smartsubscription.ui.theme.SsType

private data class Section(val status: Status, val title: String, val why: String)

private val sections get() = archiveText.let { t ->
    listOf(
        Section(Status.PAUSED, t.paused, t.pausedWhy),
        Section(Status.CANCELLED, t.cancelled, t.cancelledWhy),
        Section(Status.ARCHIVED, t.archived, t.archivedWhy),
    )
}

/** A spend record first, a list of stopped subscriptions second. Sections, buttons and wording follow the iOS ArchiveView. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current
    val t = archiveText
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val stopped = state.subscriptions.filter { it.status != Status.ACTIVE }
    val total = stopped.filter { it.currency == state.currency }.sumOf { it.lifetimeSpend }

    InkScreen(
        spacing = 0.dp,
        ink = {
            InkBlock {
                InkNav(title = t.title, leading = { BackButton(t.backToSettings) { nav.popBackStack() } })
                Eyebrow(t.eyebrow)
                BigFigure(total, t.spent, state.currency)
                InkSub(
                    if (stopped.isEmpty()) t.nothingStopped
                    else t.stoppedSummary(stopped.size),
                )
            }
        },
    ) {
        state.archiveUndo?.let { undo ->
            Banner(undo.message, "", danger = false, modifier = Modifier.padding(bottom = 4.dp)) {
                TextBtn(commonText.undo) { vm.undoArchive() }
                IconBtn(Glyphs.close, t.dismiss, ss.text2, { vm.dismissUndo() }, size = 14.dp)
            }
        }
        if (stopped.isEmpty()) {
            EmptyCard(
                t.emptyTitle,
                t.emptyBody,
            )
        }
        sections.forEach { section ->
            val items = stopped.filter { it.status == section.status }
            if (items.isEmpty()) return@forEach
            Row(Modifier.padding(start = 4.dp, top = 18.dp, bottom = 2.dp), verticalAlignment = Alignment.Bottom) {
                Text(section.title, style = SsType.group.copy(fontSize = 17.sp), color = ss.text)
                Text("  ${items.size}", style = SsType.eyebrow, color = ss.text2)
            }
            Footer(section.why)
            CardList(
                modifier = Modifier.padding(top = 8.dp),
                rows = items.map { sub -> @Composable { ArchiveRow(sub, state, vm) { pendingDelete = sub.id } } },
            )
        }
    }

    val target = state.subscription(pendingDelete)
    if (target != null) {
        ModalBottomSheet(
            onDismissRequest = { pendingDelete = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ss.card,
        ) {
            val n = target.paidPeriods.size
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(t.deleteTitle(target.name), style = SsType.h3, color = ss.text, textAlign = TextAlign.Center)
                Text(t.deleteBody(target.name), style = SsType.sub, color = ss.text2, textAlign = TextAlign.Center)
                if (n > 0) {
                    Text(
                        t.deletePayments(n, money(target.lifetimeSpend, target.currency)),
                        style = SsType.sub, color = ss.text2, textAlign = TextAlign.Center,
                    )
                }
                Text(t.archiveKeeps, style = SsType.sub, color = ss.text2, textAlign = TextAlign.Center)
                HorizontalDivider(color = ss.line, modifier = Modifier.padding(vertical = 4.dp))
                SecondaryButton(t.keepArchived) { pendingDelete = null }
                TextBtn(t.deletePermanently, ss.danger, bold = true) {
                    vm.delete(target.id)
                    pendingDelete = null
                }
            }
        }
    }
}

@Composable
private fun ArchiveRow(sub: Subscription, state: AppState, vm: AppViewModel, onDelete: () -> Unit) {
    val ss = LocalSs.current
    val paid = sub.paidPeriods.size
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryGlyph(state.category(sub.categoryId)?.icon, ss.text2, Modifier.padding(end = 8.dp))
            Text(sub.name, style = SsType.bodyStrong, color = ss.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (paid > 0) Text(money(sub.lifetimeSpend, sub.currency), style = SsType.amount, color = ss.text)
        }
        Text(AppViewModel.lifetimeLine(sub), style = SsType.caption, color = ss.text2, modifier = Modifier.padding(top = 2.dp))
        if (sub.status == Status.CANCELLED && sub.accessUntil != null) {
            val ended = sub.accessUntil < state.today
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Glyphs.clock, null, tint = ss.text2, modifier = Modifier.size(13.dp))
                Text(if (ended) archiveText.accessEnded(sub.accessUntil.longText()) else archiveText.accessUntil(sub.accessUntil.longText()), style = SsType.caption, color = ss.text2)
            }
        }
        Row(Modifier.offset(x = (-12).dp).padding(top = 6.dp)) {
            TextBtn(if (sub.status == Status.PAUSED) archiveText.resume else archiveText.reactivate) { vm.restore(sub.id) }
            if (sub.status != Status.ARCHIVED) TextBtn(archiveText.archive) { vm.changeStatus(sub.id, Status.ARCHIVED) }
            TextBtn(commonText.delete, ss.danger, onClick = onDelete)
        }
    }
}
