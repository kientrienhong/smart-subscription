package com.smartsubscription.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.smartsubscription.AppViewModel
import com.smartsubscription.Routes
import com.smartsubscription.UNLOCK_PRICE
import com.smartsubscription.data.Ledger
import com.smartsubscription.data.money
import com.smartsubscription.data.monthShort
import com.smartsubscription.i18n.analyticsText
import com.smartsubscription.ui.components.AnimatedMoney
import com.smartsubscription.ui.components.BackButton
import com.smartsubscription.ui.components.CadenceBadge
import com.smartsubscription.ui.components.Caption
import com.smartsubscription.ui.components.InkBlock
import com.smartsubscription.ui.components.InkNav
import com.smartsubscription.ui.components.Segmented
import com.smartsubscription.ui.components.Settle
import com.smartsubscription.ui.components.SsCard
import com.smartsubscription.ui.theme.Display
import com.smartsubscription.ui.theme.LocalSs
import com.smartsubscription.ui.theme.SsType
import java.time.YearMonth
import kotlin.math.ceil
import kotlin.math.roundToInt

private val windows = listOf(3, 6, 12)

/**
 * One chart instead of two: months behind today are what you paid (solid), months ahead are what
 * you are on the hook for (hollow), with today marked. Providers are ranked as a receipt.
 */
@Composable
fun AnalyticsScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current
    var windowIndex by rememberSaveable { mutableIntStateOf(0) }
    val window = windows[windowIndex]
    val ledger = state.ledger
    val a = ledger.analytics(window)
    val currency = state.currency
    val monthName = state.today.monthShort()
    val t = analyticsText

    InkScreen(
        ink = {
            InkBlock {
                InkNav(title = t.title, leading = { BackButton(t.backToSubscriptions) { nav.popBackStack() } })
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Total(
                        Modifier.weight(1f), solid = true, label = t.paidLast(window), value = a.paid, currency = currency,
                        note = if (a.payments > 0) t.recordedRenewals(a.payments) else t.nothingRecorded, yearly = a.paidYearly,
                    )
                    Total(
                        Modifier.weight(1f), solid = false, label = t.stillToPay, value = a.expected, currency = currency,
                        note = t.restOf(monthName, window), yearly = a.expectedYearly,
                    )
                }
            }
        },
    ) {
        Segmented(windows.map { t.months(it) }, windowIndex, onInk = false) {
            if (windows[it] > 3 && !state.unlocked) {
                vm.toast(t.unlockToast(money(UNLOCK_PRICE)))
                nav.navigate(Routes.UNLOCK)
            } else {
                windowIndex = it
            }
        }

        SsCard {
            Text(t.chartTitle, style = SsType.h3, color = ss.text)
            val first = a.buckets.first().month
            val last = a.buckets.last().month
            val range = t.range("${first.monthShort()} ${first.year}", "${last.monthShort()} ${last.year}")
            Caption(t.byMonth(range))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Swatch(t.recorded) { Modifier.background(ss.brand, RoundedCornerShape(2.dp)) }
                Swatch(t.expected) { Modifier.border(1.5.dp, ss.brand, RoundedCornerShape(2.dp)) }
                Swatch(t.yearly) { Modifier.background(ss.yearly, RoundedCornerShape(2.dp)) }
            }
            SpendChart(
                a, window, state.today.dayOfMonth.toFloat() / YearMonth.from(state.today).lengthOfMonth(),
                description = t.chartDescription(range, money(a.paid, currency), money(a.expected, currency)),
            )
            if (a.payments == 0) {
                Caption(t.noRenewals)
            }
        }

        SsCard {
            Text(t.providersTitle, style = SsType.h3, color = ss.text)
            Caption(t.providersCaption(money(ledger.monthlyAllIn, currency)))
            val rows = ledger.providers()
            if (rows.isEmpty()) Caption(t.noActive)
            Column {
                rows.forEach { p -> ProviderLine(p, ledger.monthlyAllIn, currency) }
            }
        }
    }
}

@Composable
private fun Total(modifier: Modifier, solid: Boolean, label: String, value: Double, currency: String, note: String, yearly: Double) {
    val ss = LocalSs.current
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier
                    .size(10.dp)
                    .then(if (solid) Modifier.background(ss.onInk, RoundedCornerShape(2.dp)) else Modifier.border(1.5.dp, ss.onInk2, RoundedCornerShape(2.dp))),
            )
            Text(label, color = ss.onInk2, style = SsType.caption)
        }
        AnimatedMoney(
            value, currency,
            TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.9).sp),
            ss.onInk, Modifier.padding(top = 4.dp),
        )
        Text(note, color = ss.onInk2, style = SsType.caption.copy(fontSize = 12.sp), modifier = Modifier.padding(top = 3.dp))
        if (yearly > 0) {
            Text(analyticsText.inclYearly(money(yearly, currency)), color = ss.onInkYearly, style = SsType.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun Swatch(label: String, shape: @Composable () -> Modifier) {
    val ss = LocalSs.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).then(shape()))
        Text(label, color = ss.text2, style = SsType.caption)
    }
}

@Composable
private fun SpendChart(a: Ledger.Analytics, window: Int, monthProgress: Float, description: String) {
    val ss = LocalSs.current
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 11.sp, color = ss.text2)
    val todayStyle = TextStyle(fontSize = 11.sp, color = ss.accentText, fontWeight = FontWeight.Bold)
    // Bars rise from the baseline when the window changes.
    val rise = remember { Animatable(0f) }
    LaunchedEffect(window) {
        rise.snapTo(0f)
        rise.animateTo(1f, tween(360, easing = Settle))
    }

    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(340f / 200f)
            .semantics { contentDescription = description },
    ) {
        val padL = 8.dp.toPx()
        val padR = 36.dp.toPx()
        val padT = 22.dp.toPx()
        val padB = 26.dp.toPx()
        val plotW = size.width - padL - padR
        val plotH = size.height - padT - padB
        val max = a.buckets.maxOf { it.total }.coerceAtLeast(1.0)
        val step = when {
            max > 400 -> 200.0
            max > 200 -> 100.0
            max > 100 -> 50.0
            else -> 25.0
        }
        val top = ceil(max / step) * step

        var g = 0.0
        while (g <= top + 0.001) {
            val y = padT + plotH - (g / top * plotH).toFloat()
            drawLine(ss.line, Offset(padL, y), Offset(size.width - padR, y), strokeWidth = 1.dp.toPx())
            drawText(measurer, g.roundToInt().toString(), Offset(size.width - padR + 6.dp.toPx(), y - 7.dp.toPx()), labelStyle)
            g += step
        }

        val slot = plotW / a.buckets.size
        val barW = (slot * 0.62f).coerceIn(4.dp.toPx(), 28.dp.toPx())
        val labelEvery = when {
            a.buckets.size <= 6 -> 1
            a.buckets.size <= 12 -> 2
            else -> 4
        }
        a.buckets.forEachIndexed { i, b ->
            val x = padL + i * slot + (slot - barW) / 2
            var base = padT + plotH
            // Bottom to top: paid monthly, paid yearly, expected monthly, expected yearly.
            listOf(
                b.paidMonthly to 0, b.paidYearly to 1, b.expectedMonthly to 2, b.expectedYearly to 3,
            ).forEach { (v, kind) ->
                if (v <= 0) return@forEach
                val h = (v / top * plotH).toFloat() * rise.value
                base -= h
                drawSegment(kind, Offset(x, base), Size(barW, h), ss.brand, ss.yearly, ss.card)
            }
            if (((i - (window - 1)) % labelEvery + labelEvery) % labelEvery == 0) {
                val text = measurer.measure(b.month.monthShort(), labelStyle)
                drawText(text, topLeft = Offset(x + barW / 2 - text.size.width / 2, size.height - 20.dp.toPx()))
            }
        }

        // Today sits inside this month's slot, as far along as the month is.
        val nowX = padL + (window - 1) * slot + slot * monthProgress
        drawLine(ss.accent, Offset(nowX, padT - 4.dp.toPx()), Offset(nowX, padT + plotH), strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
        val label = measurer.measure(analyticsText.today, todayStyle)
        drawText(label, topLeft = Offset(nowX - label.size.width / 2, padT - 8.dp.toPx() - label.size.height))
    }
}

private fun DrawScope.drawSegment(kind: Int, topLeft: Offset, size: Size, brand: Color, yearly: Color, card: Color) {
    val r = CornerRadius(2.dp.toPx())
    val stroke = Stroke(1.5.dp.toPx())
    when (kind) {
        0 -> drawRoundRect(brand, topLeft, size, r)
        1 -> drawRoundRect(yearly, topLeft, size, r)
        2 -> {
            drawRoundRect(card, topLeft, size, r)
            drawRoundRect(brand, topLeft, size, r, style = stroke)
        }
        else -> {
            drawRoundRect(card, topLeft, size, r)
            clipRect(topLeft.x, topLeft.y, topLeft.x + size.width, topLeft.y + size.height) {
                val gap = 4.dp.toPx()
                var x = topLeft.x - size.height
                while (x < topLeft.x + size.width) {
                    drawLine(yearly, Offset(x, topLeft.y + size.height), Offset(x + size.height, topLeft.y), strokeWidth = 2.dp.toPx())
                    x += gap * 1.4f
                }
            }
            drawRoundRect(yearly, topLeft, size, r, style = stroke)
        }
    }
}

/** A receipt line: name, dotted leader, monthly amount, share of the month. */
@Composable
private fun ProviderLine(p: Ledger.ProviderShare, monthly: Double, currency: String) {
    val ss = LocalSs.current
    val color = if (p.allYearly) ss.yearly else ss.text
    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(p.name, style = SsType.body.copy(fontSize = 15.sp), color = ss.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 150.dp))
        if (p.yearlyPrice > 0) Box(Modifier.padding(start = 6.dp)) { CadenceBadge(analyticsText.perYear(money(p.yearlyPrice, currency)), ss.yearly) }
        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(2.dp)
                .drawBehind {
                    drawLine(
                        ss.line, Offset(0f, size.height / 2), Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx())),
                    )
                },
        )
        Text(money(p.perMonth, currency), style = SsType.amount.copy(fontSize = 16.sp), color = color)
        Text(
            "${if (monthly > 0) (p.perMonth / monthly * 100).roundToInt() else 0}%",
            style = SsType.caption.copy(fontSize = 12.sp),
            color = ss.text2,
            modifier = Modifier.width(40.dp).padding(start = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}
