package com.smartsubscription.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.smartsubscription.AppViewModel
import com.smartsubscription.Routes
import com.smartsubscription.UNLOCK_PRICE
import com.smartsubscription.data.PurchaseState
import com.smartsubscription.data.longText
import com.smartsubscription.data.money
import com.smartsubscription.i18n.commonText
import com.smartsubscription.i18n.unlockText
import com.smartsubscription.ui.components.BackButton
import com.smartsubscription.ui.components.Banner
import com.smartsubscription.ui.components.BigFigure
import com.smartsubscription.ui.components.BigText
import com.smartsubscription.ui.components.CardList
import com.smartsubscription.ui.components.Caption
import com.smartsubscription.ui.components.Eyebrow
import com.smartsubscription.ui.components.Glyphs
import com.smartsubscription.ui.components.InkBlock
import com.smartsubscription.ui.components.InkNav
import com.smartsubscription.ui.components.InkSub
import com.smartsubscription.ui.components.PrimaryButton
import com.smartsubscription.ui.components.SecondaryButton
import com.smartsubscription.ui.components.SectionLabel
import com.smartsubscription.ui.components.TextBtn
import com.smartsubscription.ui.theme.Display
import com.smartsubscription.ui.theme.LocalSs
import com.smartsubscription.ui.theme.SsType

/**
 * An app that tracks subscriptions should not sell you one, so the price is a single payment. The receipt
 * sets it against what this person already pays each month, computed from their own list.
 */
@Composable
fun UnlockScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current
    val owned = state.purchase == PurchaseState.OWNED
    val monthly = state.ledger.monthlyAllIn
    val t = unlockText

    InkScreen(
        ink = {
            InkBlock {
                InkNav(title = if (owned) t.unlocked else t.unlock, leading = { BackButton(commonText.back) { nav.popBackStack() } })
                if (owned) {
                    Eyebrow("SmartSubscription")
                    BigText(t.unlocked, fontSize = 40)
                    Text(
                        buildAnnotatedString {
                            append(t.paidPrefix)
                            withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append(money(UNLOCK_PRICE)) }
                            append(t.paidSuffix(state.today.longText()))
                        },
                        color = ss.onInk2, style = SsType.sub, modifier = Modifier.padding(top = 6.dp),
                    )
                } else {
                    Eyebrow(t.wholeApp)
                    BigFigure(UNLOCK_PRICE, t.once, fontSize = 72)
                    InkSub(t.notASubscription)
                    Receipt(monthly, state.currency)
                }
            }
        },
        bottomBar = {
            when (state.purchase) {
                PurchaseState.OWNED -> SecondaryButton(t.scanInvoice) { nav.navigate(Routes.ADD) }
                PurchaseState.BUYING -> {
                    PrimaryButton(t.waiting, enabled = false) {}
                    Caption(t.confirmInSheet, Modifier.align(Alignment.CenterHorizontally))
                }
                else -> {
                    PrimaryButton(t.unlockFor(money(UNLOCK_PRICE))) { vm.purchase() }
                    Caption(t.onePayment, Modifier.align(Alignment.CenterHorizontally))
                }
            }
            if (!owned) TextBtn(t.restore, modifier = Modifier.align(Alignment.CenterHorizontally)) { vm.restorePurchase() }
        },
    ) {
        if (state.purchase == PurchaseState.FAILED) {
            Banner(t.failedTitle, t.failedBody)
        }
        if (owned) {
            Banner(t.restoredTitle, t.restoredBody, danger = false)
        }
        Column {
            SectionLabel(if (owned) t.nowIncluded else t.whatAdds(money(UNLOCK_PRICE)), Modifier.padding(top = 0.dp))
            CardList(
                rows = listOf(
                    { Feature(Glyphs.featScan, t.scanTitle, t.scanBody) },
                    { Feature(Glyphs.featMore, t.moreTitle, t.moreBody) },
                    { Feature(Glyphs.featAnalytics, t.analyticsTitle, t.analyticsBody) },
                ),
            )
        }
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = ss.text, fontWeight = FontWeight.SemiBold)) { append(t.freeLead) }
                append(t.freeRest)
            },
            style = SsType.sub, color = ss.text2, modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun Receipt(monthly: Double, currency: String) {
    val ss = LocalSs.current
    Column(
        Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .drawBehind {
                drawLine(ss.onInk2, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
            }
            .padding(top = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = unlockText.receiptDescription(money(monthly, currency), money(UNLOCK_PRICE))
            },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReceiptLine(unlockText.yourSubscriptions, money(monthly, currency), ss.onInk)
        ReceiptLine(unlockText.thisApp, money(UNLOCK_PRICE), ss.onInkAccent)
    }
}

@Composable
private fun ReceiptLine(label: String, amount: String, amountColor: Color) {
    val ss = LocalSs.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = ss.onInk2, style = SsType.sub)
        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(2.dp)
                .drawBehind {
                    drawLine(ss.ink2, Offset(0f, 1f), Offset(size.width, 1f), strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx())))
                },
        )
        Text(amount, color = amountColor, style = SsType.amount.copy(fontFamily = Display, fontSize = 16.sp))
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, body: String) {
    val ss = LocalSs.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = ss.brand, modifier = Modifier.padding(top = 2.dp).size(24.dp))
        Column {
            Text(title, style = SsType.bodyStrong.copy(fontSize = 16.sp), color = ss.text)
            Text(body, style = SsType.sub, color = ss.text2, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
