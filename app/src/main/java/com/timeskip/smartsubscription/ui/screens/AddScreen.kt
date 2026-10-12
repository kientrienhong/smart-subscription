package com.timeskip.smartsubscription.ui.screens

import com.timeskip.smartsubscription.i18n.scanText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timeskip.smartsubscription.AppViewModel
import com.timeskip.smartsubscription.Routes
import com.timeskip.smartsubscription.UNLOCK_PRICE
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.ui.components.BackButton
import com.timeskip.smartsubscription.ui.components.BigText
import com.timeskip.smartsubscription.ui.components.CardList
import com.timeskip.smartsubscription.ui.components.Caption
import com.timeskip.smartsubscription.ui.components.Eyebrow
import com.timeskip.smartsubscription.ui.components.Glyphs
import com.timeskip.smartsubscription.ui.components.InkBlock
import com.timeskip.smartsubscription.ui.components.InkNav
import com.timeskip.smartsubscription.ui.components.InkSub
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SsType

/** Scanning is the differentiator, so it gets the ink tile; the two other ways are plain rows beneath it. */
@Composable
fun AddScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current

    fun scan(source: String) {
        if (!state.unlocked) {
            vm.toast(scanText.scanIsPartOfUnlock(money(UNLOCK_PRICE)))
            nav.navigate(Routes.UNLOCK)
        } else {
            nav.navigate(Routes.scan(source))
        }
    }

    InkScreen(
        ink = {
            InkBlock(bottom = 18.dp) {
                InkNav(leading = { BackButton(scanText.backToSubscriptions) { nav.popBackStack() } })
                Eyebrow(scanText.addEyebrow)
                BigText(scanText.addTitle)
                InkSub(scanText.addSub)
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ss.ink)
                .clickable(role = Role.Button) { scan("camera") }
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(scanText.scanInvoice, style = SsType.title.copy(fontSize = 24.sp, lineHeight = 27.sp), color = ss.onInk)
                    Text(
                        scanText.scanInvoiceBody,
                        color = ss.onInk2,
                        style = SsType.sub,
                        modifier = Modifier.padding(top = 6.dp).widthIn(max = 240.dp),
                    )
                }
                Icon(Glyphs.scan, null, tint = ss.onInk, modifier = Modifier.padding(start = 16.dp).size(44.dp))
            }
            HorizontalDivider(color = ss.ink2, modifier = Modifier.padding(top = 14.dp))
            Text(
                if (state.unlocked) scanText.readsOnDevice else scanText.partOfUnlock(money(UNLOCK_PRICE)),
                color = ss.onInk2,
                style = SsType.caption.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        CardList(
            rows = listOf(
                { OptionRow(Glyphs.photo, scanText.choosePhoto, scanText.choosePhotoBody) { scan("photo") } },
                { OptionRow(Glyphs.pencil, scanText.enterByHand, scanText.enterByHandBody) { nav.navigate(Routes.form()) } },
            ),
        )
        Caption(scanText.reviewEveryField, Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun OptionRow(icon: ImageVector, title: String, body: String, onClick: () -> Unit) {
    val ss = LocalSs.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, null, tint = ss.brand, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = SsType.bodyStrong, color = ss.text)
            Text(body, style = SsType.sub, color = ss.text2, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Glyphs.chevron, null, tint = ss.text2, modifier = Modifier.size(14.dp))
    }
}
