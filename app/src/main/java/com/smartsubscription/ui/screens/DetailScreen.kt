package com.smartsubscription.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.smartsubscription.AppState
import com.smartsubscription.AppViewModel
import com.smartsubscription.Routes
import com.smartsubscription.data.Cadence
import com.smartsubscription.data.Status
import com.smartsubscription.data.Subscription
import com.smartsubscription.data.dueText
import com.smartsubscription.data.longText
import com.smartsubscription.data.money
import com.smartsubscription.data.shortText
import com.smartsubscription.i18n.commonText
import com.smartsubscription.i18n.detailText
import com.smartsubscription.ui.components.BackButton
import com.smartsubscription.ui.components.BigFigure
import com.smartsubscription.ui.components.CardList
import com.smartsubscription.ui.components.Caption
import com.smartsubscription.ui.components.CategoryGlyph
import com.smartsubscription.ui.components.Glyphs
import com.smartsubscription.ui.components.InkBlock
import com.smartsubscription.ui.components.InkNav
import com.smartsubscription.ui.components.KeyValueRow
import com.smartsubscription.ui.components.ListRow
import com.smartsubscription.ui.components.NavRow
import com.smartsubscription.ui.components.PrimaryButton
import com.smartsubscription.ui.components.SecondaryButton
import com.smartsubscription.ui.components.Settle
import com.smartsubscription.ui.components.SsCard
import com.smartsubscription.ui.components.SwitchRow
import com.smartsubscription.ui.components.TextBtn
import com.smartsubscription.ui.theme.LocalSs
import com.smartsubscription.ui.theme.SsType
import java.time.temporal.ChronoUnit

@Composable
fun DetailScreen(vm: AppViewModel, nav: NavController, id: String) {
    val state by vm.state.collectAsStateWithLifecycle()
    val sub = state.subscription(id)
    if (sub == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val ss = LocalSs.current
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val today = state.today
    val days = sub.daysUntilNext(today)
    val category = state.category(sub.categoryId)
    val recorded = sub.currentCycleRecorded
    val t = detailText

    fun lifecycle(status: Status) {
        vm.changeStatus(sub.id, status)
        nav.navigate(Routes.ARCHIVE) { popUpTo(Routes.HOME) }
    }

    InkScreen(
        ink = {
            InkBlock {
                InkNav(
                    title = sub.name,
                    leading = { BackButton(t.backToSubscriptions) { nav.popBackStack() } },
                    trailing = { TextBtn(commonText.edit, ss.onInk) { nav.navigate(Routes.form(edit = sub.id)) } },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${sub.provider} · ", color = ss.onInk2, style = SsType.eyebrow)
                    CategoryGlyph(category?.icon, ss.onInk2, Modifier.padding(end = 4.dp))
                    Text(category?.name ?: commonText.noCategory, color = ss.onInk2, style = SsType.eyebrow)
                }
                BigFigure(sub.amount, sub.cadence.perText, sub.currency)
                if (sub.status == Status.ACTIVE) {
                    Text(
                        buildAnnotatedString {
                            append(t.renewsPrefix)
                            withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append(sub.nextBilling.longText()) }
                            append(", ")
                            withStyle(SpanStyle(color = if (days <= 3) ss.onInkAccent else ss.onInk2, fontWeight = if (days <= 3) FontWeight.SemiBold else FontWeight.Normal)) {
                                append(dueText(days))
                            }
                        },
                        color = ss.onInk2,
                        style = SsType.sub,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    CycleStrip(sub, state)
                }
            }
        },
    ) {
        val months = ChronoUnit.MONTHS.between(sub.startDate, today)
        CardList(
            rows = listOfNotNull(
                { KeyValueRow(t.statusLabel, t.status(sub.status)) },
                {
                    if (months > 0) {
                        KeyValueRow(t.started, sub.startDate.longText(), t.startedSub(months.toInt(), money(sub.lifetimeSpend, sub.currency)))
                    } else {
                        KeyValueRow(t.started, commonText.today, t.nothingPaidYet)
                    }
                },
                {
                    KeyValueRow(
                        t.billing, sub.cadence.label,
                        if (sub.cadence == Cadence.YEARLY) t.perMonth(money(sub.perMonth, sub.currency)) else t.perYear(money(sub.perYear, sub.currency)),
                    )
                },
                if (sub.notes.isNotBlank()) ({ KeyValueRow(t.notes, sub.notes) }) else null,
            ),
        )

        if (sub.status == Status.ACTIVE) {
            SsCard {
                Text(t.thisCycle, style = SsType.h3, color = ss.text)
                Caption(
                    if (recorded) t.cycleRecorded(sub.nextBilling.shortText())
                    else t.cycleExpected(sub.nextBilling.shortText()),
                )
                if (recorded) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, ss.brand, RoundedCornerShape(10.dp))
                            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Glyphs.checkCircle, null, tint = ss.brand, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.renewed, style = SsType.bodyStrong.copy(fontSize = 15.sp), color = ss.text)
                            Caption(t.undoToChange)
                        }
                        TextBtn(commonText.undo) { vm.undoRenewed(sub.id) }
                    }
                }
                PrimaryButton(t.markAsRenewed, enabled = !recorded) { vm.markRenewed(sub.id) }
                SecondaryButton(t.priceOrDateChanged) { nav.navigate(Routes.form(edit = sub.id)) }
                if (recorded) Caption(t.alreadyRecorded)
            }

            CardList(
                rows = listOf(
                    { ListRow { Text(t.subscription, style = SsType.h3, color = ss.text) } },
                    { NavRow(t.pause) { lifecycle(Status.PAUSED) } },
                    { NavRow(t.archive) { lifecycle(Status.ARCHIVED) } },
                    { NavRow(t.cancelSubscription, color = ss.danger) { lifecycle(Status.CANCELLED) } },
                ),
            )

            CardList(
                rows = listOf(
                    {
                        SwitchRow(
                            t.renewalReminder,
                            sub.reminderOn,
                            sub = t.reminderSub,
                        ) { vm.setReminder(sub.id, it) }
                    },
                ),
            )
        }

        if (confirmDelete) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ss.card)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    buildAnnotatedString {
                        append(t.deletePrefix)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(sub.name) }
                        append(t.deleteSuffix(sub.paidPeriods.size))
                    },
                    style = SsType.body.copy(fontSize = 15.sp),
                    color = ss.text,
                    modifier = Modifier.weight(1f),
                )
                SmallButton(t.keep, ss.card2, ss.text) { confirmDelete = false }
                SmallButton(commonText.delete, ss.danger, Color.White) {
                    vm.delete(sub.id)
                    nav.popBackStack()
                }
            }
        } else {
            TextBtn(t.deleteSubscription, ss.danger, modifier = Modifier.align(Alignment.CenterHorizontally)) { confirmDelete = true }
        }
    }
}

@Composable
private fun SmallButton(text: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = fg, style = SsType.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
    }
}

/**
 * This subscription's own timeline: past cycles solid, the expected one hollow, and an orange mark
 * for today so you can see how far through the cycle you are.
 */
@Composable
private fun CycleStrip(sub: Subscription, state: AppState) {
    val ss = LocalSs.current
    val periods = sub.periods.takeLast(6)
    if (periods.isEmpty()) return
    val current = periods.last()
    val cycleLen = ChronoUnit.DAYS.between(current.start, current.end).coerceAtLeast(1)
    val dayIn = ChronoUnit.DAYS.between(current.start, state.today).coerceIn(0, cycleLen)
    val n = periods.size
    val todayX = if (n > 1) (n - 2f) / (n - 1) + (dayIn.toFloat() / cycleLen) / (n - 1) else dayIn.toFloat() / cycleLen
    val paidCount = periods.count { it.paidOn != null }
    val recorded = sub.currentCycleRecorded

    // "Mark as renewed" pops the point it fills.
    val pop = remember { Animatable(1f) }
    LaunchedEffect(recorded) {
        if (recorded) {
            pop.snapTo(0.4f)
            pop.animateTo(1.35f, tween(200, easing = Settle))
            pop.animateTo(1f, tween(140, easing = Settle))
        }
    }

    Column(Modifier.padding(top = 20.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .semantics { contentDescription = detailText.stripDescription(paidCount, n, dayIn, cycleLen) },
        ) {
            val inset = 6.dp.toPx()
            val w = size.width - inset * 2
            val y = 14.dp.toPx()
            drawLine(ss.ink2, Offset(inset, y), Offset(inset + w, y), strokeWidth = 2.dp.toPx())
            drawLine(ss.onInk, Offset(inset, y), Offset(inset + w * todayX, y), strokeWidth = 2.dp.toPx())
            periods.forEachIndexed { i, p ->
                val x = inset + w * (if (n > 1) i / (n - 1f) else 1f)
                val scale = if (i == n - 1) pop.value else 1f
                val r = 5.dp.toPx() * scale
                if (p.paidOn != null) {
                    drawCircle(ss.onInk, r + 1.dp.toPx(), Offset(x, y))
                } else {
                    drawCircle(ss.ink, r, Offset(x, y))
                    drawCircle(ss.onInk, r, Offset(x, y), style = Stroke(2.dp.toPx()))
                }
            }
            drawCircle(ss.onInkAccent, 4.dp.toPx(), Offset(inset + w * todayX, y))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(periods.first().end.shortText(), color = ss.onInk2, style = SsType.caption.copy(fontSize = 12.sp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append(current.end.shortText()) }
                    append(if (recorded) detailText.recordedSuffix else detailText.expectedSuffix)
                },
                color = ss.onInk2,
                style = SsType.caption.copy(fontSize = 12.sp),
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append("$paidCount") }
                    append(detailText.cyclesRecordedSuffix(n))
                },
                color = ss.onInk2,
                style = SsType.caption,
            )
            Text(
                buildAnnotatedString {
                    append(detailText.dayPrefix)
                    withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append("$dayIn") }
                    append(detailText.daySuffix(cycleLen))
                },
                color = ss.onInk2,
                style = SsType.caption,
            )
        }
    }
}
