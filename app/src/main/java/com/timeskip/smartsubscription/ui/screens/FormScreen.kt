package com.timeskip.smartsubscription.ui.screens

import com.timeskip.smartsubscription.i18n.commonText
import com.timeskip.smartsubscription.i18n.formText
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.timeskip.smartsubscription.AppViewModel
import com.timeskip.smartsubscription.Draft
import com.timeskip.smartsubscription.data.Cadence
import com.timeskip.smartsubscription.data.Currencies
import com.timeskip.smartsubscription.data.dayShort
import com.timeskip.smartsubscription.data.groupLabelFor
import com.timeskip.smartsubscription.data.longText
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.data.parseAmount
import com.timeskip.smartsubscription.scan.ModelUnavailable
import com.timeskip.smartsubscription.scan.ParseEngine
import com.timeskip.smartsubscription.scan.ScanResult
import com.timeskip.smartsubscription.scan.nextBillingDate
import com.timeskip.smartsubscription.ui.components.Banner
import com.timeskip.smartsubscription.ui.components.CardList
import com.timeskip.smartsubscription.ui.components.CategoryGlyph
import com.timeskip.smartsubscription.ui.components.Eyebrow
import com.timeskip.smartsubscription.ui.components.InkBlock
import com.timeskip.smartsubscription.ui.components.InkNav
import com.timeskip.smartsubscription.ui.components.PullDown
import com.timeskip.smartsubscription.ui.components.PullOption
import com.timeskip.smartsubscription.ui.components.SectionLabel
import com.timeskip.smartsubscription.ui.components.Segmented
import com.timeskip.smartsubscription.ui.components.TextBtn
import com.timeskip.smartsubscription.ui.theme.Display
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SsType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Enter by hand, review a scan, or edit. The ink block previews the rail row the form will create and
 * updates as you type. Save stays off until there is a name and an amount, and the reason is written under it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(vm: AppViewModel, nav: NavController, editId: String?, scanned: Boolean) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ss = LocalSs.current
    val today = state.today
    val editing = state.subscription(editId)
    val scan = if (scanned) state.scanDraft else null
    val scannedInvoice = scan?.invoice
    fun amountText(value: Double, code: String) = if (Currencies.of(code).decimals == 0) "%.0f".format(value) else "%.2f".format(value)

    var name by rememberSaveable { mutableStateOf(editing?.name ?: scannedInvoice?.let { it.subscriptionName ?: it.provider } ?: "") }
    var provider by rememberSaveable { mutableStateOf(editing?.provider ?: scannedInvoice?.provider ?: "") }
    var categoryId by rememberSaveable { mutableStateOf(editing?.categoryId) }
    var currency by rememberSaveable { mutableStateOf(editing?.currency ?: scannedInvoice?.currency ?: state.currency) }
    var amountText by rememberSaveable {
        mutableStateOf(
            editing?.let { amountText(it.amount, it.currency) }
                ?: scannedInvoice?.amount?.let { amountText(it, currency) }
                ?: "",
        )
    }
    var cadence by rememberSaveable { mutableStateOf(editing?.cadence ?: scannedInvoice?.cadence ?: Cadence.MONTHLY) }
    // Next billing is the date the invoice prints for it, or else the charge date stepped forward by the cycle, as on iOS.
    var dateEpoch by rememberSaveable {
        mutableStateOf(
            (editing?.nextBilling ?: nextBillingDate(scannedInvoice?.let { it.nextPaymentDate ?: it.invoiceDate } ?: today, cadence, today)).toEpochDay(),
        )
    }
    var notes by rememberSaveable { mutableStateOf(editing?.notes ?: "") }
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    val date = LocalDate.ofEpochDay(dateEpoch)
    val amount = parseAmount(amountText, currency)
    val amountBad = amountText.isNotBlank() && (amount == null || amount <= 0)
    val trimmedName = name.trim()
    val missing = buildList {
        if (trimmedName.isEmpty()) add(formText.needName)
        if (amount == null || amount <= 0) add(if (amountBad) formText.needValidAmount else formText.needAmount)
    }
    val ok = missing.isEmpty()
    val category = state.category(categoryId)
    val days = ChronoUnit.DAYS.between(today, date)
    val whenText = when {
        days == 0L -> commonText.today
        days == 1L -> commonText.tomorrow
        days < 0 -> formText.pastDate
        else -> commonText.inDays(days)
    }

    fun save() {
        if (!ok) return
        vm.save(
            Draft(
                editingId = editId,
                name = trimmedName,
                provider = provider.trim(),
                categoryId = categoryId,
                amount = amount!!,
                currency = currency,
                cadence = cadence,
                nextBilling = date,
                notes = notes.trim(),
            ),
        )
        nav.popBackStack()
    }

    InkScreen(
        ink = {
            InkBlock(bottom = 18.dp) {
                InkNav(
                    title = when {
                        editing != null -> commonText.edit
                        scanned -> formText.titleReview
                        else -> formText.titleByHand
                    },
                    leading = { TextBtn(commonText.cancel, ss.onInk) { nav.popBackStack() } },
                    trailing = { TextBtn(commonText.save, ss.onInk, bold = true, enabled = ok) { save() } },
                )
                Eyebrow(formText.previewEyebrow)
                RailPreview(
                    day = date.dayOfMonth.toString(),
                    weekday = date.dayShort().uppercase(),
                    icon = category?.icon,
                    name = trimmedName.ifEmpty { formText.namePlaceholder },
                    line = (provider.trim().takeIf { it.isNotEmpty() }?.let { "$it · " } ?: "") + whenText,
                    amount = money(amount?.takeIf { it > 0 } ?: 0.0, currency),
                    ready = ok,
                )
                Text(
                    if (ok) {
                        buildAnnotatedString {
                            append(formText.savingPuts)
                            withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append(trimmedName) }
                            append(formText.onRailUnder)
                            withStyle(SpanStyle(color = ss.onInk, fontWeight = FontWeight.SemiBold)) { append(groupLabelFor(date, today, false)) }
                            append(formText.savingTail(money(amount!!, currency), cadence.aWord))
                        }
                    } else {
                        buildAnnotatedString { append(formText.saveNeeds(missing)) }
                    },
                    color = ss.onInk2,
                    style = SsType.sub,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        },
    ) {
        if (scan != null && editing == null) {
            Banner(formText.filledFromInvoice, scanNote(scan), danger = false)
        }

        Column {
            SectionLabel(formText.sectionSubscription, Modifier.padding(top = 0.dp))
            CardList(
                rows = listOf(
                    { FieldInput(name, { name = it }, formText.nameHint, formText.nameLabel) },
                    { FieldInput(provider, { provider = it }, formText.providerHint, formText.providerLabel) },
                    {
                        FieldRow(formText.category) {
                            PullDown(
                                options = listOf(PullOption("", formText.none)) + state.categories.map { PullOption(it.id, it.name, icon = it.icon) },
                                selected = categoryId.orEmpty(),
                                trigger = {
                                    if (category != null) {
                                        CategoryGlyph(category.icon, ss.text2)
                                        Text(category.name, style = SsType.body, color = ss.text)
                                    } else {
                                        Text(formText.none, style = SsType.body, color = ss.text2)
                                    }
                                },
                            ) { categoryId = it.ifEmpty { null } }
                        }
                    },
                ),
            )
        }

        Column {
            SectionLabel(formText.sectionPayment, Modifier.padding(top = 0.dp))
            CardList(
                rows = listOf(
                    {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .invalidEdge(amountBad, ss.danger)
                                .padding(start = 16.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicInput(
                                amountText,
                                { amountText = it },
                                "0.00",
                                formText.amountLabel,
                                style = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = (-0.4).sp),
                                keyboard = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f),
                            )
                            CurrencyPull(currency, state.subscriptions.map { it.currency }) { currency = it }
                        }
                    },
                    {
                        FieldRow(formText.billingCycle) {
                            Segmented(
                                options = Cadence.entries.map { it.label },
                                selected = cadence.ordinal,
                                onInk = false,
                                height = 32.dp,
                                modifier = Modifier.padding(vertical = 8.dp),
                            ) { cadence = Cadence.entries[it] }
                        }
                    },
                    {
                        FieldRow(formText.nextBilling, onClick = { pickingDate = true }) {
                            Text(date.longText(), style = SsType.body, color = ss.brand)
                        }
                    },
                ),
            )
            Text(
                buildAnnotatedString {
                    if (Currencies.of(currency).decimals == 0) {
                        append(formText.wholeNumbersOnly(currency))
                        withStyle(SpanStyle(color = ss.text, fontWeight = FontWeight.SemiBold)) { append("150,000") }
                    } else {
                        append(formText.decimalsWithPeriod)
                        withStyle(SpanStyle(color = ss.text, fontWeight = FontWeight.SemiBold)) { append("1,234.56") }
                    }
                    append(".")
                },
                color = ss.text2,
                style = SsType.caption,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
        }

        Column {
            SectionLabel(formText.sectionNotes, Modifier.padding(top = 0.dp))
            CardList(
                rows = listOf(
                    {
                        BasicInput(
                            notes,
                            { notes = it },
                            formText.notesHint,
                            formText.notesLabel,
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(horizontal = 16.dp, vertical = 14.dp),
                        )
                    },
                ),
            )
        }
    }

    if (pickingDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextBtn(commonText.done, bold = true) {
                    picker.selectedDateMillis?.let {
                        dateEpoch = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    pickingDate = false
                }
            },
            dismissButton = { TextBtn(commonText.cancel) { pickingDate = false } },
        ) {
            DatePicker(picker)
        }
    }
}

/** The rail row the form will create, drawn on the ink. Faded until the form can save. */
@Composable
private fun RailPreview(day: String, weekday: String, icon: String?, name: String, line: String, amount: String, ready: Boolean) {
    val ss = LocalSs.current
    Row(
        Modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .alpha(if (ready) 1f else 0.55f)
            .clip(RoundedCornerShape(10.dp))
            .background(ss.onInk.copy(alpha = 0.08f))
            .border(1.dp, ss.ink2, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$name, $line, $amount" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day, style = SsType.day, color = ss.onInk)
            Text(weekday, style = SsType.caption.copy(fontSize = 10.sp), color = ss.onInk2)
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) CategoryGlyph(icon, ss.onInk2, Modifier.padding(end = 6.dp))
                Text(name, style = SsType.bodyStrong, color = ss.onInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(line, style = SsType.caption, color = ss.onInk2)
        }
        Text(amount, style = SsType.amount.copy(fontSize = 18.sp), color = ss.onInk)
    }
}

@Composable
private fun FieldRow(label: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val ss = LocalSs.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = SsType.body, color = ss.text, modifier = Modifier.width(112.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { content() }
    }
}

@Composable
private fun FieldInput(value: String, onChange: (String) -> Unit, placeholder: String, label: String) {
    BasicInput(value, onChange, placeholder, label, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 15.dp))
}

@Composable
private fun BasicInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    label: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SsType.body,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    val ss = LocalSs.current
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        textStyle = style.copy(color = ss.text),
        cursorBrush = SolidColor(ss.brand),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = modifier.semantics { contentDescription = label },
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(placeholder, style = style, color = ss.text2)
                inner()
            }
        },
    )
}

/** Currencies already on the person's list come first, the rest follow in the app's order. */
@Composable
fun CurrencyPull(selected: String, used: List<String>, onPick: (String) -> Unit) {
    val ss = LocalSs.current
    val inUse = (used + selected).distinct()
    val options = inUse.map { code -> Currencies.of(code) }.map { PullOption(it.code, it.code, trailing = it.name + "  " + it.symbol, group = formText.groupInUse) } +
        Currencies.all.filter { it.code !in inUse }.map { PullOption(it.code, it.code, trailing = it.name + "  " + it.symbol, group = formText.groupAll) }
    PullDown(
        options = options,
        selected = selected,
        trigger = {
            Text(Currencies.of(selected).symbol, style = SsType.body.copy(fontFamily = Display, fontWeight = FontWeight.Bold), color = ss.text2)
            Text(selected, style = SsType.bodyStrong, color = ss.text)
        },
        onPick = onPick,
    )
}

/** A red edge on the left of a field whose value cannot be saved. */
private fun Modifier.invalidEdge(on: Boolean, color: Color): Modifier = if (!on) this else drawBehind {
    drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 3.dp.toPx())
}

/** Says how the fields were found, so the person knows how closely to check them. */
private fun scanNote(scan: ScanResult): String {
    val invoice = scan.invoice
    val how = if (invoice.engine == ParseEngine.ON_DEVICE_MODEL) {
        formText.noteModel
    } else {
        formText.notePatterns +
            if (scan.modelUnavailable == ModelUnavailable.DOWNLOADING) formText.noteModelDownloading else ""
    }
    val date = if (invoice.nextPaymentDate != null) {
        formText.noteDatePrinted
    } else if (invoice.invoiceDate != null) {
        formText.noteDateFromInvoice
    } else {
        formText.noteNoDate
    }
    return "$how $date ${formText.notePickCategory}"
}
