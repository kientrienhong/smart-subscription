package com.timeskip.smartsubscription.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timeskip.smartsubscription.AppViewModel
import com.timeskip.smartsubscription.BuildConfig
import com.timeskip.smartsubscription.Routes
import com.timeskip.smartsubscription.UNLOCK_PRICE
import com.timeskip.smartsubscription.data.CsvExport
import com.timeskip.smartsubscription.data.PurchaseState
import com.timeskip.smartsubscription.data.ThemeMode
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.ui.components.CardList
import com.timeskip.smartsubscription.ui.components.Eyebrow
import com.timeskip.smartsubscription.ui.components.Footer
import com.timeskip.smartsubscription.ui.components.Glyphs
import com.timeskip.smartsubscription.ui.components.InkBlock
import com.timeskip.smartsubscription.ui.components.InkNav
import com.timeskip.smartsubscription.ui.components.ListRow
import com.timeskip.smartsubscription.ui.components.NavRow
import com.timeskip.smartsubscription.ui.components.PullDown
import com.timeskip.smartsubscription.ui.components.PullOption
import com.timeskip.smartsubscription.ui.components.SectionLabel
import com.timeskip.smartsubscription.ui.components.Segmented
import com.timeskip.smartsubscription.ui.components.SwitchRow
import com.timeskip.smartsubscription.ui.components.TextBtn
import com.timeskip.smartsubscription.ui.theme.Display
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SsType
import com.timeskip.smartsubscription.i18n.commonText
import com.timeskip.smartsubscription.i18n.settingsText
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The app's own settings in its order. The ink block carries the one row that is not a preference:
 * the one-time unlock. iCloud sync is left out until Android has an equivalent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current
    val context = LocalContext.current
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    val version = remember(context) {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName} (${androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(info)})"
    }

    InkScreen(
        spacing = 0.dp,
        ink = {
            InkBlock(bottom = 18.dp) {
                InkNav(
                    title = settingsText.title,
                    trailing = { TextBtn(commonText.done, ss.onInk, bold = true) { nav.popBackStack() } },
                )
                UnlockCard(state.unlocked) { nav.navigate(Routes.UNLOCK) }
            }
        },
    ) {
        SectionLabel(settingsText.appearance, Modifier.padding(top = 4.dp))
        CardList(
            rows = listOf(
                {
                    ListRow {
                        Text(settingsText.theme, style = SsType.body, color = ss.text, modifier = Modifier.weight(1f))
                        Segmented(
                            ThemeMode.entries.map { it.label }, state.theme.ordinal, onInk = false,
                            height = 32.dp, modifier = Modifier.width(196.dp),
                        ) { vm.setTheme(ThemeMode.entries[it]) }
                    }
                },
            ),
        )

        SectionLabel(settingsText.currency)
        CardList(
            rows = listOf(
                {
                    ListRow {
                        Text(settingsText.defaultCurrency, style = SsType.body, color = ss.text, modifier = Modifier.weight(1f))
                        CurrencyPull(state.currency, state.subscriptions.map { it.currency }) { vm.setCurrency(it) }
                    }
                },
            ),
        )
        Footer(settingsText.currencyFooter)

        SectionLabel(settingsText.organization)
        CardList(
            rows = listOf(
                { NavRow(settingsText.manageCategories) { nav.navigate(Routes.CATEGORIES) } },
                { NavRow(settingsText.archive) { nav.navigate(Routes.ARCHIVE) } },
            ),
        )
        Footer(settingsText.archiveFooter)

        SectionLabel(settingsText.data)
        val anything = state.subscriptions.isNotEmpty()
        CardList(
            rows = listOf(
                {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = anything, role = Role.Button) { CsvExport.share(context, state.subscriptions) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .alpha(if (anything) 1f else 0.6f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(Glyphs.share, null, tint = if (anything) ss.brand else ss.text2, modifier = Modifier.size(22.dp))
                        Column {
                            Text(settingsText.exportCsv, style = SsType.body, color = if (anything) ss.brand else ss.text2)
                            if (!anything) Text(settingsText.nothingToExport, style = SsType.caption, color = ss.text2)
                        }
                    }
                },
            ),
        )
        Footer(settingsText.exportFooter)

        SectionLabel(settingsText.notifications)
        CardList(
            rows = buildList {
                add { SwitchRow(settingsText.renewalReminders, state.remindersOn) { vm.setReminders(it) } }
                if (state.remindersOn) {
                    add {
                        ListRow {
                            Text(settingsText.remindMe, style = SsType.body, color = ss.text, modifier = Modifier.weight(1f))
                            PullDown(
                                options = AppViewModel.leadChoices.map { PullOption(it.toString(), AppViewModel.leadLabel(it)) },
                                selected = state.leadDays.toString(),
                                trigger = { Text(AppViewModel.leadLabel(state.leadDays), style = SsType.body, color = ss.text) },
                            ) { vm.setLeadDays(it.toInt()) }
                        }
                    }
                    add {
                        ListRow(onClick = { pickingTime = true }) {
                            Text(settingsText.deliverAt, style = SsType.body, color = ss.text, modifier = Modifier.weight(1f))
                            Text(
                                state.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = SsType.body,
                                color = ss.text,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ss.card2)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            },
        )
        Footer(settingsText.remindersFooter)

        SectionLabel(settingsText.about)
        CardList(
            rows = listOf(
                {
                    ListRow {
                        Text(settingsText.version, style = SsType.body, color = ss.text, modifier = Modifier.weight(1f))
                        Text(version, style = SsType.body, color = ss.text2)
                    }
                },
            ),
        )

        if (BuildConfig.DEBUG) DeveloperSection(state.purchase) { vm.devSetPurchase(it) }
        Spacer(Modifier.padding(bottom = 8.dp))
    }

    if (pickingTime) {
        val picker = rememberTimePickerState(state.reminderTime.hour, state.reminderTime.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            confirmButton = {
                TextBtn(commonText.done, bold = true) {
                    vm.setReminderTime(LocalTime.of(picker.hour, picker.minute))
                    vm.toast(settingsText.remindersArriveAt(picker.hour, picker.minute))
                    pickingTime = false
                }
            },
            dismissButton = { TextBtn(commonText.cancel) { pickingTime = false } },
            text = { TimePicker(picker) },
            containerColor = ss.card,
        )
    }
}

@Composable
private fun UnlockCard(owned: Boolean, onClick: () -> Unit) {
    val ss = LocalSs.current
    Row(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ss.onInk.copy(alpha = 0.06f))
            .border(1.dp, ss.ink2, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            val title = androidx.compose.ui.text.TextStyle(fontFamily = Display, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp)
            if (owned) {
                Eyebrow("SmartSubscription")
                Text(settingsText.unlocked, style = title, color = ss.onInk, modifier = Modifier.padding(top = 2.dp))
                Text(settingsText.paidOnce(money(UNLOCK_PRICE)), style = SsType.sub, color = ss.onInk2)
            } else {
                Eyebrow(settingsText.unlockFeatures)
                Text(
                    buildAnnotatedString {
                        append(settingsText.unlockFor)
                        withStyle(SpanStyle(color = ss.onInkAccent)) { append(settingsText.priceOnce(money(UNLOCK_PRICE))) }
                    },
                    style = title,
                    color = ss.onInk,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(settingsText.notASubscription, style = SsType.sub, color = ss.onInk2)
            }
        }
        Icon(Glyphs.chevron, null, tint = ss.onInk2, modifier = Modifier.size(14.dp))
    }
}

/** Purchase states a developer can jump to. BUYING is left out because it only lasts while the store sheet is open. */
private val devStates = listOf(
    PurchaseState.OFFER to "Locked",
    PurchaseState.FAILED to "Failed",
    PurchaseState.OWNED to "Unlocked",
)

/** Debug builds only. Sets the purchase state directly so the Pro features and every Unlock screen state can be checked. */
@Composable
private fun DeveloperSection(purchase: PurchaseState, onPick: (PurchaseState) -> Unit) {
    val ss = LocalSs.current
    SectionLabel("Developer")
    CardList(
        rows = listOf(
            {
                ListRow {
                    Text("Pro", style = SsType.body, color = ss.text, modifier = Modifier.weight(1f))
                    Segmented(
                        devStates.map { it.second },
                        devStates.indexOfFirst { it.first == purchase }.coerceAtLeast(0),
                        onInk = false,
                        height = 32.dp,
                        modifier = Modifier.width(220.dp),
                    ) { onPick(devStates[it].first) }
                }
            },
        ),
    )
    Footer("Debug builds only. Skips the store; resets when the app restarts.")
}
