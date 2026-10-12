package com.smartsubscription.ui.screens

import com.smartsubscription.i18n.commonText
import com.smartsubscription.i18n.homeText
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.smartsubscription.AppState
import com.smartsubscription.AppViewModel
import com.smartsubscription.Routes
import com.smartsubscription.data.Cadence
import com.smartsubscription.data.Ledger
import com.smartsubscription.data.Subscription
import com.smartsubscription.data.dayShort
import com.smartsubscription.data.dueText
import com.smartsubscription.data.longText
import com.smartsubscription.data.money
import com.smartsubscription.data.monthLong
import com.smartsubscription.ui.components.BigFigure
import com.smartsubscription.ui.components.CadenceBadge
import com.smartsubscription.ui.components.Caption
import com.smartsubscription.ui.components.CategoryGlyph
import com.smartsubscription.ui.components.Eyebrow
import com.smartsubscription.ui.components.Glyphs
import com.smartsubscription.ui.components.InkBlock
import com.smartsubscription.ui.components.InkSub
import com.smartsubscription.ui.components.PrimaryButton
import com.smartsubscription.ui.components.Segmented
import com.smartsubscription.ui.components.Settle
import com.smartsubscription.ui.theme.LocalSs
import com.smartsubscription.ui.theme.SsType

private val DateCol = 44.dp
private val DotCol = 40.dp
private val RailX = DateCol + DotCol / 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current
    var yearAhead by rememberSaveable { mutableStateOf(false) }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }
    val ledger = state.ledger
    val t = homeText

    Box(Modifier.fillMaxSize().background(ss.bg)) {
        LazyColumn(Modifier.fillMaxSize()) {
            item(key = "ink") { HomeInk(state, ledger, yearAhead) { yearAhead = it } }

            if (state.subscriptions.isEmpty()) {
                item(key = "first-run") {
                    EmptyCard(
                        title = t.firstRunTitle,
                        body = t.firstRunBody,
                        action = t.firstRunAction,
                        onAction = { nav.navigate(Routes.ADD) },
                    )
                }
                return@LazyColumn
            }

            stickyHeader(key = "tabs") {
                CategoryTabs(state, ledger, categoryId) { categoryId = it }
            }

            val rail = ledger.rail(yearAhead, categoryId)
            if (rail.groups.isEmpty()) {
                item(key = "empty") {
                    val name = state.category(categoryId)?.name
                    if (name != null && ledger.active.isNotEmpty()) {
                        EmptyCard(t.nothingIn(name), t.otherCategoriesBody, t.showAll) { categoryId = null }
                    } else {
                        EmptyCard(t.nothingActiveTitle, t.nothingActiveBody)
                    }
                }
            }
            item(key = "rail-top") { RailSpacer(8) }
            rail.groups.forEach { group ->
                item(key = "g-" + group.title) {
                    GroupHeader(group.title, group.total, Modifier.animateItem())
                }
                items(group.rows, key = { it.id }) { sub ->
                    RailRow(sub, state, Modifier.animateItem()) { nav.navigate(Routes.detail(sub.id)) }
                }
            }
            if (rail.laterCount > 0) {
                item(key = "later") {
                    Caption(
                        t.moreLater(rail.laterCount, rail.laterTotal?.let { money(it, state.currency) }),
                        Modifier.padding(start = 84.dp, end = 20.dp, top = 20.dp),
                    )
                }
            }
            item(key = "bottom") { Spacer(Modifier.navigationBarsPadding().height(120.dp)) }
        }

        BottomBar(
            onAnalytics = { nav.navigate(Routes.ANALYTICS) },
            onAdd = { nav.navigate(Routes.ADD) },
            onSettings = { nav.navigate(Routes.SETTINGS) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HomeInk(state: AppState, ledger: Ledger, yearAhead: Boolean, onPeriod: (Boolean) -> Unit) {
    val ss = LocalSs.current
    val today = state.today
    val t = homeText
    InkBlock {
        Text(t.title, style = SsType.screenTitle, color = ss.onInk, modifier = Modifier.padding(top = 10.dp, bottom = 14.dp))
        when {
            ledger.active.isEmpty() -> {
                Eyebrow(today.monthLong())
                BigFigure(0.0, t.toCome, state.currency)
                InkSub(t.nothingRenewing)
            }
            !yearAhead -> {
                val next = ledger.active.first()
                val days = next.daysUntilNext(today)
                Eyebrow(t.monthYear(today.monthLong(), today.year))
                BigFigure(ledger.stillToCome, t.stillToCome, state.currency)
                Text(
                    buildAnnotatedString {
                        append(t.next)
                        withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append(next.name) }
                        append(" ${money(next.amount, next.currency)}, ")
                        if (days <= 3) {
                            withStyle(SpanStyle(color = ss.onInkAccent, fontWeight = FontWeight.SemiBold)) { append(dueText(days)) }
                        } else {
                            append(dueText(days))
                        }
                    },
                    color = ss.onInk2,
                    style = SsType.sub,
                    modifier = Modifier.padding(top = 6.dp),
                )
                LedgerBar(ledger, state.currency)
                if (ledger.foreignCount > 0) {
                    InkSub(
                        if (ledger.stillToComeElsewhere.isNotEmpty()) t.plusElsewhereToCome(ledger.stillToComeElsewhere)
                        else t.foreignNoneDue(ledger.foreignCount),
                    )
                }
            }
            else -> {
                val yearlyOnes = ledger.active.count { it.cadence == Cadence.YEARLY && it.currency == state.currency }
                Eyebrow(t.next12Months)
                BigFigure(ledger.yearlyAllIn, t.aYear, state.currency)
                InkSub(t.yearSummary(ledger.active.size, yearlyOnes))
                if (ledger.yearlyElsewhere.isNotEmpty()) {
                    InkSub(t.plusElsewhereYearly(ledger.yearlyElsewhere))
                }
            }
        }
        Segmented(
            options = listOf(t.thisMonth, t.yearAhead),
            selected = if (yearAhead) 1 else 0,
            onInk = true,
            modifier = Modifier.padding(top = 18.dp),
        ) { onPeriod(it == 1) }
    }
}

/**
 * How much of the month is already charged (solid) and how much is left (hollow).
 * The yearly part of "still to come" is a hatched teal slice at the right end.
 */
@Composable
private fun LedgerBar(ledger: Ledger, currency: String) {
    val ss = LocalSs.current
    val total = ledger.monthSum
    val charged by animateFloatAsState(
        if (total > 0) (ledger.chargedThisMonth / total).toFloat() else 0f,
        tween(460, easing = Settle),
        label = "ledger",
    )
    val yearly = if (total > 0) (ledger.yearlyToCome / total).toFloat() else 0f
    val t = homeText
    val description = t.ledgerDescription(
        money(ledger.chargedThisMonth, currency),
        money(ledger.stillToCome, currency),
        if (ledger.yearlyToCome > 0) money(ledger.yearlyToCome, currency) else null,
    )

    Column(Modifier.padding(top = 16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(1.5.dp, ss.onInk2, RoundedCornerShape(3.dp))
                .semantics { contentDescription = description }
                .drawBehind {
                    drawRect(ss.onInk, size = size.copy(width = size.width * charged))
                    if (yearly > 0f) {
                        val start = size.width * (1 - yearly)
                        clipRect(left = start) {
                            var x = start - size.height
                            while (x < size.width) {
                                drawLine(ss.onInkYearly, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 2.dp.toPx())
                                x += 5.dp.toPx()
                            }
                        }
                        drawLine(ss.onInkYearly, Offset(start, 0f), Offset(start, size.height), strokeWidth = 1.5.dp.toPx())
                    }
                },
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendText(money(ledger.chargedThisMonth, currency), t.legendCharged, ss.onInk)
            if (ledger.yearlyToCome > 0) LegendText(money(ledger.yearlyToCome, currency), t.legendYearly, ss.onInkYearly)
            LegendText(money(ledger.monthlyAllIn, currency), t.legendAllIn, ss.onInk)
        }
    }
}

@Composable
private fun LegendText(strong: String, rest: String, strongColor: Color) {
    val ss = LocalSs.current
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = strongColor, fontWeight = FontWeight.SemiBold)) { append(strong) }
            append(rest)
        },
        color = ss.onInk2,
        style = SsType.caption,
    )
}

@Composable
private fun CategoryTabs(state: AppState, ledger: Ledger, selected: String?, onSelect: (String?) -> Unit) {
    val ss = LocalSs.current
    val inUse = state.categories.filter { c -> ledger.active.any { it.categoryId == c.id } }
    Column(Modifier.background(ss.bg)) {
        LazyRow(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { Tab(homeText.all, null, selected == null) { onSelect(null) } }
            items(inUse, key = { it.id }) { c -> Tab(c.name, c.icon, selected == c.id) { onSelect(c.id) } }
        }
        HorizontalDivider(color = ss.line)
    }
}

@Composable
private fun Tab(label: String, icon: String?, on: Boolean, onClick: () -> Unit) {
    val ss = LocalSs.current
    Column(
        Modifier
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick)
            .semantics { this.selected = on },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp).height(42.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) CategoryGlyph(icon, if (on) ss.text else ss.text2)
            Text(
                label,
                color = if (on) ss.text else ss.text2,
                style = SsType.sub.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.Medium),
            )
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(if (on) ss.ink else Color.Transparent))
    }
}

private fun Modifier.railLine(color: Color) = drawBehind {
    val x = RailX.toPx()
    drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
}

@Composable
private fun RailSpacer(height: Int) {
    Spacer(Modifier.fillMaxWidth().height(height.dp).railLine(LocalSs.current.line))
}

@Composable
private fun GroupHeader(title: String, total: String, modifier: Modifier = Modifier) {
    val ss = LocalSs.current
    Row(
        modifier
            .fillMaxWidth()
            .railLine(ss.line)
            .padding(top = 20.dp, bottom = 6.dp, end = 20.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(DateCol + DotCol))
        Text(title, style = SsType.group, color = ss.text, modifier = Modifier.weight(1f))
        Text(total, style = SsType.caption, color = ss.text2)
    }
}

@Composable
private fun RailRow(sub: Subscription, state: AppState, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ss = LocalSs.current
    val days = sub.daysUntilNext(state.today)
    val soon = days <= 3
    val yearly = sub.cadence == Cadence.YEARLY
    val category = state.category(sub.categoryId)
    val label = homeText.rowDescription(
        sub.name, category?.name ?: commonText.noCategory, "${money(sub.amount, sub.currency)} ${sub.cadence.aWord}",
        sub.nextBilling.longText(), dueText(days),
    )

    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .railLine(ss.line)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { contentDescription = label }
            .padding(top = 8.dp, bottom = 8.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(DateCol), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sub.nextBilling.dayOfMonth.toString(), style = SsType.day, color = ss.text)
            Text(sub.nextBilling.dayShort().uppercase(), style = SsType.caption.copy(fontSize = 11.sp), color = ss.text2, modifier = Modifier.padding(top = 3.dp))
        }
        Box(Modifier.width(DotCol), contentAlignment = Alignment.Center) {
            // Solid means charged, hollow means upcoming. Orange is "within three days"; teal is "yearly".
            val ring = when {
                soon -> ss.accent
                yearly -> ss.yearly
                else -> ss.brand
            }
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (soon) ss.accent else ss.bg)
                    .border(if (yearly && !soon) 3.dp else 2.dp, ring, CircleShape),
            )
        }
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryGlyph(category?.icon, ss.text2, Modifier.padding(end = 6.dp))
                Text(sub.name, style = SsType.bodyStrong, color = ss.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (sub.cadence != Cadence.MONTHLY) CadenceBadge(sub.cadence.label, if (yearly) ss.yearly else ss.text)
                Text(
                    (if (soon) dueText(days) + " · " else "") + sub.provider,
                    style = SsType.caption.copy(fontWeight = if (soon) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (soon) ss.accentText else ss.text2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(money(sub.amount, sub.currency), style = SsType.amount, color = if (yearly) ss.yearly else ss.text)
            if (sub.cadence != Cadence.MONTHLY) {
                Text(sub.cadence.aWord, style = SsType.caption.copy(fontSize = 12.sp), color = if (yearly) ss.yearly else ss.text2)
            }
        }
    }
}

@Composable
fun EmptyCard(title: String, body: String, action: String? = null, onAction: () -> Unit = {}) {
    val ss = LocalSs.current
    Column(
        Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ss.card)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = SsType.group.copy(fontSize = 19.sp), color = ss.text, textAlign = TextAlign.Center)
        Text(body, style = SsType.body.copy(fontSize = 15.sp), color = ss.text2, textAlign = TextAlign.Center)
        if (action != null) {
            PrimaryButton(action, modifier = Modifier.padding(top = 10.dp).width(220.dp), onClick = onAction)
        }
    }
}

@Composable
private fun BottomBar(onAnalytics: () -> Unit, onAdd: () -> Unit, onSettings: () -> Unit, modifier: Modifier = Modifier) {
    val ss = LocalSs.current
    Row(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.4f to ss.bg))
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val t = homeText
        BarButton(t.analytics, Glyphs.analytics, ss.card, ss.brand, onAnalytics)
        BarButton(t.add, Glyphs.plus, ss.ink, ss.onInk, onAdd)
        BarButton(t.settings, Glyphs.gear, ss.card, ss.brand, onSettings)
    }
}

@Composable
private fun BarButton(label: String, icon: ImageVector, bg: Color, fg: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .shadow(4.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        Text(label, color = fg, style = SsType.sub.copy(fontWeight = FontWeight.SemiBold))
    }
}
