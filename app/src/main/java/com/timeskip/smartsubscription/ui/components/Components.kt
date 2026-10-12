package com.timeskip.smartsubscription.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.ui.theme.LocalSs
import com.timeskip.smartsubscription.ui.theme.SsType

/** One easing for everything that settles (MOTION 2 in the design). */
val Settle = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

// ---------- ink block: every screen opens with one ----------

@Composable
fun InkBlock(modifier: Modifier = Modifier, bottom: Dp = 22.dp, content: @Composable ColumnScope.() -> Unit) {
    val ss = LocalSs.current
    Column(
        modifier
            .fillMaxWidth()
            .background(ss.ink)
            .padding(start = 20.dp, end = 20.dp, bottom = bottom),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/** Navigation row inside an ink block: leading control, centred title, trailing control. */
@Composable
fun InkNav(
    title: String = "",
    leading: @Composable () -> Unit = { Spacer(Modifier.width(44.dp)) },
    trailing: @Composable () -> Unit = { Spacer(Modifier.width(44.dp)) },
) {
    val ss = LocalSs.current
    Row(
        Modifier
            .fillMaxWidth()
            .offset(x = (-8).dp)
            .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.widthIn(min = 64.dp)) { leading() }
        Text(
            title,
            color = ss.onInk,
            style = SsType.bodyStrong,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(Modifier.widthIn(min = 64.dp), contentAlignment = Alignment.CenterEnd) { trailing() }
    }
}

@Composable
fun BackButton(description: String, onClick: () -> Unit) {
    IconBtn(Glyphs.back, description, LocalSs.current.onInk, onClick)
}

@Composable
fun IconBtn(icon: ImageVector, description: String, tint: Color, onClick: () -> Unit, size: Dp = 22.dp) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(size))
    }
}

@Composable
fun TextBtn(
    text: String,
    color: Color = LocalSs.current.brand,
    bold: Boolean = false,
    enabled: Boolean = true,
    style: TextStyle = SsType.body,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = color,
            style = style.copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium),
            modifier = Modifier.alpha(if (enabled) 1f else 0.45f),
        )
    }
}

@Composable
fun Eyebrow(text: String, color: Color = LocalSs.current.onInk2) {
    Text(text, color = color, style = SsType.eyebrow)
}

/** A number that counts to its new value, so you see which way the total moved. */
@Composable
fun AnimatedMoney(value: Double, currency: String = "USD", style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val shown by animateFloatAsState(value.toFloat(), tween(480, easing = Settle), label = "money")
    Text(money(shown.toDouble(), currency), style = style, color = color, modifier = modifier, maxLines = 1)
}

/** The big number an ink block carries, with its small unit after it. */
@Composable
fun BigFigure(value: Double?, unit: String, currency: String = "USD", fontSize: Int = 56) {
    val ss = LocalSs.current
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
        val style = SsType.big.copy(fontSize = fontSize.sp, lineHeight = fontSize.sp)
        if (value == null) {
            Text("–", style = style, color = ss.onInk2)
        } else {
            AnimatedMoney(value, currency, style, ss.onInk, Modifier.weight(1f, fill = false))
        }
        Text(unit, color = ss.onInk2, style = SsType.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium), modifier = Modifier.padding(start = 6.dp, bottom = 6.dp))
    }
}

@Composable
fun BigText(text: String, fontSize: Int = 34, color: Color = LocalSs.current.onInk) {
    Text(text, style = SsType.title.copy(fontSize = fontSize.sp, lineHeight = (fontSize + 4).sp), color = color, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun InkSub(text: String, modifier: Modifier = Modifier) {
    Text(text, color = LocalSs.current.onInk2, style = SsType.sub, modifier = modifier.padding(top = 6.dp))
}

// ---------- cards and rows ----------

@Composable
fun SsCard(modifier: Modifier = Modifier, padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LocalSs.current.card)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(if (padding > 0.dp) 12.dp else 0.dp),
        content = content,
    )
}

/** A card of rows separated by hairlines. Pass one lambda per row. */
@Composable
fun CardList(modifier: Modifier = Modifier, rows: List<@Composable () -> Unit>) {
    val ss = LocalSs.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ss.card),
    ) {
        rows.forEachIndexed { i, row ->
            if (i > 0) HorizontalDivider(color = ss.line, thickness = 1.dp)
            row()
        }
    }
}

@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun KeyValueRow(key: String, value: String, small: String? = null) {
    val ss = LocalSs.current
    ListRow {
        Text(key, color = ss.text2, style = SsType.body, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(value, color = ss.text, style = SsType.body.copy(fontWeight = FontWeight.Medium), textAlign = TextAlign.End)
            if (small != null) Text(small, color = ss.text2, style = SsType.caption, textAlign = TextAlign.End)
        }
    }
}

@Composable
fun NavRow(label: String, color: Color = LocalSs.current.text, sub: String? = null, onClick: () -> Unit) {
    val ss = LocalSs.current
    ListRow(onClick = onClick) {
        Column(Modifier.weight(1f)) {
            Text(label, color = color, style = SsType.body)
            if (sub != null) Text(sub, color = ss.text2, style = SsType.caption)
        }
        Icon(Glyphs.chevron, contentDescription = null, tint = ss.text2, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, sub: String? = null, onChange: (Boolean) -> Unit) {
    val ss = LocalSs.current
    ListRow(onClick = { onChange(!checked) }) {
        Column(Modifier.weight(1f)) {
            Text(label, color = ss.text, style = SsType.body)
            if (sub != null) Text(sub, color = ss.text2, style = SsType.caption)
        }
        SsSwitch(checked, onChange)
    }
}

@Composable
fun SsSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val ss = LocalSs.current
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = ss.brand,
            checkedThumbColor = Color.White,
            checkedBorderColor = ss.brand,
            uncheckedTrackColor = ss.card2,
            uncheckedThumbColor = ss.text2,
            uncheckedBorderColor = ss.line,
        ),
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, color = LocalSs.current.text2, style = SsType.sectionLabel, modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 18.dp, bottom = 6.dp))
}

@Composable
fun Footer(text: String) {
    Text(text, color = LocalSs.current.text2, style = SsType.caption, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp))
}

@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = LocalSs.current.text2) {
    Text(text, color = color, style = SsType.sub, modifier = modifier)
}

// ---------- buttons ----------

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ss = LocalSs.current
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(10.dp))
            .background(ss.brand)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = ss.onBrand, style = SsType.bodyStrong)
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ss = LocalSs.current
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, ss.brand), RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = ss.brand, style = SsType.body.copy(fontWeight = FontWeight.Medium))
    }
}

// ---------- segmented control: one thumb slides to the chosen segment ----------

@Composable
fun Segmented(
    options: List<String>,
    selected: Int,
    onInk: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    onSelect: (Int) -> Unit,
) {
    val ss = LocalSs.current
    val track = if (onInk) Color.White.copy(alpha = 0.14f) else ss.card2
    val thumb = if (onInk) ss.onInk else ss.card
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(track)
            .padding(2.dp),
    ) {
        val segment = maxWidth / options.size
        val x by animateDpAsState(segment * selected, tween(260, easing = Settle), label = "thumb")
        Box(
            Modifier
                .offset(x = x)
                .width(segment)
                .height(height)
                .clip(RoundedCornerShape(7.dp))
                .background(thumb),
        )
        Row {
            options.forEachIndexed { i, label ->
                val on = i == selected
                val color = when {
                    onInk && on -> ss.ink
                    onInk -> ss.onInk
                    else -> ss.text
                }
                Box(
                    Modifier
                        .width(segment)
                        .height(height)
                        .semantics { this.selected = on; role = Role.Tab }
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = color,
                        style = SsType.sub.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, fontSize = if (height < 36.dp) 13.sp else 14.sp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// ---------- notices ----------

@Composable
fun Banner(
    title: String,
    body: String,
    danger: Boolean = true,
    modifier: Modifier = Modifier,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    val ss = LocalSs.current
    val edge = if (danger) ss.danger else ss.brand
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ss.card)
            .border(1.dp, edge, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(if (danger) Glyphs.warning else Glyphs.checkCircle, null, tint = edge, modifier = Modifier.padding(top = 2.dp).size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = ss.text, style = SsType.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
            if (body.isNotEmpty()) Text(body, color = ss.text2, style = SsType.sub)
        }
        trailing?.invoke(this)
    }
}

@Composable
fun CadenceBadge(text: String, color: Color) {
    Box(
        Modifier
            .padding(end = 6.dp)
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(text, color = color, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp))
    }
}

// ---------- pull-down: a trigger showing the current value, and a menu of checkable rows ----------

data class PullOption(val value: String, val label: String, val icon: String? = null, val trailing: String? = null, val group: String? = null)

@Composable
fun PullDown(
    options: List<PullOption>,
    selected: String,
    trigger: @Composable RowScope.() -> Unit,
    onPick: (String) -> Unit,
) {
    val ss = LocalSs.current
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.DropdownList) { open = true }
                .padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            trigger()
            Icon(Glyphs.updown, null, tint = ss.text2, modifier = Modifier.size(14.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.background(ss.card)) {
            var lastGroup: String? = null
            options.forEach { o ->
                if (o.group != null && o.group != lastGroup) {
                    if (lastGroup != null) HorizontalDivider(color = ss.line)
                    Text(o.group, color = ss.text2, style = SsType.caption.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(start = 44.dp, top = 8.dp, bottom = 4.dp))
                    lastGroup = o.group
                }
                val on = o.value == selected
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(o.label, color = ss.text, style = SsType.body.copy(fontSize = 16.sp, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal), modifier = Modifier.weight(1f, fill = false))
                            if (o.trailing != null) Text("  " + o.trailing, color = ss.text2, style = SsType.sub)
                        }
                    },
                    leadingIcon = {
                        Icon(Glyphs.check, null, tint = if (on) ss.brand else Color.Transparent, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = if (o.icon != null) ({ CategoryGlyph(o.icon, ss.text2, size = 18.dp) }) else null,
                    onClick = {
                        open = false
                        onPick(o.value)
                    },
                )
            }
        }
    }
}
