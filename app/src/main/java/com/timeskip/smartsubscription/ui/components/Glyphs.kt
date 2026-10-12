package com.timeskip.smartsubscription.ui.components

import com.timeskip.smartsubscription.i18n.isVietnamese
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser

/** One stroke or fill in a glyph, as SVG path data. */
private data class Part(val d: String, val fill: Boolean = false, val stroke: Float? = null)

private fun rrect(x: Float, y: Float, w: Float, h: Float, r: Float) =
    "M${x + r},$y H${x + w - r} A$r,$r 0 0 1 ${x + w},${y + r} V${y + h - r} A$r,$r 0 0 1 ${x + w - r},${y + h} " +
        "H${x + r} A$r,$r 0 0 1 $x,${y + h - r} V${y + r} A$r,$r 0 0 1 ${x + r},$y Z"

private fun circle(cx: Float, cy: Float, r: Float) =
    "M${cx - r},$cy A$r,$r 0 1 0 ${cx + r},$cy A$r,$r 0 1 0 ${cx - r},$cy Z"

private fun buildGlyph(name: String, viewport: Float, strokeWidth: Float, parts: List<Part>): ImageVector {
    val b = ImageVector.Builder(name, viewport.dp, viewport.dp, viewport, viewport)
    parts.forEach { p ->
        if (p.fill) {
            b.addPath(addPathNodes(p.d), fill = SolidColor(Color.Black))
        } else {
            b.addPath(
                addPathNodes(p.d),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = p.stroke ?: strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return b.build()
}

/**
 * Category icons drawn after the SF Symbols iOS stores in `iconName`. Shape alone tells them apart;
 * the seeded pastel colours are left out so orange and teal keep their meanings.
 */
object CategoryGlyphs {
    private val parts: Map<String, List<Part>> = mapOf(
        "tv" to listOf(Part(rrect(1.5f, 2.5f, 13f, 9f, 1.8f)), Part("M5.5 14h5")),
        "laptopcomputer" to listOf(Part(rrect(3f, 3f, 10f, 7.5f, 1.2f)), Part("M1 13h14")),
        "cloud" to listOf(Part("M4.6 12.5h6.9a2.9 2.9 0 0 0 .3-5.8A4.1 4.1 0 0 0 4 6.4a3.05 3.05 0 0 0 .6 6.1z")),
        "music.note" to listOf(Part(circle(4.8f, 12f, 2.1f), fill = true), Part("M6.9 12V2.2c1.6 1.3 4.5 1.4 4.6 4.8")),
        "gamecontroller" to listOf(
            Part("M4.6 4.5h6.8a3.4 3.4 0 0 1 3.3 4.2l-.7 3a1.7 1.7 0 0 1-3 .7L9.8 11H6.2l-1.2 1.4a1.7 1.7 0 0 1-3-.7l-.7-3a3.4 3.4 0 0 1 3.3-4.2z"),
            Part("M4.9 6.9v2.4M3.7 8.1h2.4"),
            Part(circle(10.5f, 7.3f, .7f), fill = true), Part(circle(11.9f, 8.9f, .7f), fill = true),
        ),
        "newspaper" to listOf(
            Part(rrect(1.5f, 2.5f, 10.5f, 11f, 1.2f)),
            Part("M12 5.5h1.7a.8.8 0 0 1 .8.8V12a1.5 1.5 0 0 1-3 0"),
            Part("M4 5.5h5.5M4 8h5.5M4 10.5h3.5"),
        ),
        "dollarsign.circle" to listOf(
            Part(circle(8f, 8f, 6.5f)),
            Part("M10 6c-.4-.6-1.1-.9-2-.9-1.1 0-1.9.6-1.9 1.4 0 1.9 3.9 1 3.9 3 0 .8-.8 1.4-2 1.4-.9 0-1.6-.3-2.1-1M8 3.9V5.1M8 10.9v1.2"),
        ),
        "ellipsis.circle" to listOf(
            Part(circle(8f, 8f, 6.5f)),
            Part(circle(5f, 8f, .8f), fill = true), Part(circle(8f, 8f, .8f), fill = true), Part(circle(11f, 8f, .8f), fill = true),
        ),
        "cart" to listOf(
            Part("M1.5 2.5h2l1.7 7.5h7.3l1.6-5.5H4.3"),
            Part(circle(6.3f, 13f, 1f), fill = true), Part(circle(11.5f, 13f, 1f), fill = true),
        ),
        "house" to listOf(Part("M1.8 7.6L8 2.4l6.2 5.2"), Part("M3.5 6.4v7.1h9V6.4"), Part("M6.6 13.5V9.8h2.8v3.7")),
        "car" to listOf(
            Part("M3 8.3l1.3-3.5a1.2 1.2 0 0 1 1.1-.8h5.2a1.2 1.2 0 0 1 1.1.8L13 8.3"),
            Part(rrect(1.5f, 8.3f, 13f, 4.2f, 1.2f)),
            Part("M3.8 12.5v1.3M12.2 12.5v1.3"),
            Part(circle(4.6f, 10.4f, .7f), fill = true), Part(circle(11.4f, 10.4f, .7f), fill = true),
        ),
        "book" to listOf(Part("M8 3.6C6.5 2.6 4 2.4 2 2.9v10c2-.5 4.5-.3 6 .7 1.5-1 4-1.2 6-.7v-10c-2-.5-4.5-.3-6 .7z"), Part("M8 3.6v10")),
        "tag" to listOf(Part("M1.8 2.6v4.8l6.8 6.8 5.4-5.4-6.8-6.8H2.4a.6.6 0 0 0-.6.6z"), Part(circle(5f, 5.6f, 1f), fill = true)),
    )

    private val cache = mutableMapOf<String, ImageVector>()

    fun vector(icon: String?): ImageVector {
        val key = if (icon != null && icon in parts) icon else "tag"
        return cache.getOrPut(key) { buildGlyph(key, 16f, 1.5f, parts.getValue(key)) }
    }

    /** The same glyph drawn white on a bitmap, for the home screen widget, which can't draw an [ImageVector]. Tint it there. */
    fun bitmap(icon: String?, sizePx: Int): Bitmap {
        val key = if (icon != null && icon in parts) icon else "tag"
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { scale(sizePx / 16f, sizePx / 16f) }
        parts.getValue(key).forEach { p ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                if (p.fill) {
                    style = Paint.Style.FILL
                } else {
                    style = Paint.Style.STROKE
                    strokeWidth = p.stroke ?: 1.5f
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
            }
            canvas.drawPath(PathParser.createPathFromPathData(p.d), paint)
        }
        return bitmap
    }

    private val namesEn = mapOf(
        "tv" to "TV", "laptopcomputer" to "Laptop", "cloud" to "Cloud", "music.note" to "Music note",
        "gamecontroller" to "Game controller", "newspaper" to "Newspaper", "dollarsign.circle" to "Dollar sign",
        "ellipsis.circle" to "More", "cart" to "Cart", "house" to "House", "car" to "Car", "book" to "Book", "tag" to "Tag",
    )
    private val namesVi = mapOf(
        "tv" to "TV", "laptopcomputer" to "Máy tính xách tay", "cloud" to "Đám mây", "music.note" to "Nốt nhạc",
        "gamecontroller" to "Tay cầm chơi game", "newspaper" to "Báo", "dollarsign.circle" to "Ký hiệu đô la",
        "ellipsis.circle" to "Khác", "cart" to "Giỏ hàng", "house" to "Nhà", "car" to "Ô tô", "book" to "Sách", "tag" to "Nhãn",
    )
    val names: Map<String, String> get() = if (isVietnamese) namesVi else namesEn
}

@Composable
fun CategoryGlyph(icon: String?, tint: Color, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Icon(CategoryGlyphs.vector(icon), contentDescription = null, tint = tint, modifier = modifier.size(size))
}

/** The design's interface icons, from its inline SVGs. */
object Glyphs {
    val back = buildGlyph("back", 22f, 2.4f, listOf(Part("M14 3L6 11l8 8")))
    val chevron = buildGlyph("chevron", 16f, 2f, listOf(Part("M5 2l6 6-6 6")))
    val plus = buildGlyph("plus", 18f, 2.4f, listOf(Part("M9 2v14M2 9h14")))
    val plusSmall = buildGlyph("plusSmall", 18f, 2.2f, listOf(Part("M9 3v12M3 9h12")))
    val analytics = buildGlyph(
        "analytics", 18f, 0f,
        listOf(
            Part(rrect(2f, 9f, 3.5f, 7f, 1f), fill = true),
            Part(rrect(7.25f, 5f, 3.5f, 11f, 1f), fill = true),
            Part(rrect(12.5f, 2f, 3.5f, 14f, 1f), fill = true),
        ),
    )
    val gear = buildGlyph(
        "gear", 18f, 1.8f,
        listOf(Part(circle(9f, 9f, 2.6f)), Part("M9 1.5v2.2M9 14.3v2.2M1.5 9h2.2M14.3 9h2.2M3.7 3.7l1.6 1.6M12.7 12.7l1.6 1.6M3.7 14.3l1.6-1.6M12.7 5.3l1.6-1.6")),
    )
    val check = buildGlyph("check", 16f, 2f, listOf(Part("M3 8.5l3.2 3.2L13 4.5")))
    val checkCircle = buildGlyph("checkCircle", 20f, 1.8f, listOf(Part(circle(10f, 10f, 8.5f), stroke = 1.6f), Part("M6 10.5l2.5 2.5L14 7.5")))
    val warning = buildGlyph(
        "warning", 20f, 1.8f,
        listOf(Part("M10 2l8.5 15h-17z"), Part("M10 8v4"), Part(circle(10f, 14.5f, 1f), fill = true)),
    )
    val scan = buildGlyph(
        "scan", 44f, 2.2f,
        listOf(
            Part("M4 14V8a4 4 0 0 1 4-4h6M30 4h6a4 4 0 0 1 4 4v6M40 30v6a4 4 0 0 1-4 4h-6M14 40H8a4 4 0 0 1-4-4v-6"),
            Part(rrect(12f, 15f, 20f, 14f, 3f), stroke = 2f),
            Part(circle(22f, 22f, 4f), stroke = 2f),
        ),
    )
    val photo = buildGlyph(
        "photo", 28f, 1.8f,
        listOf(Part(rrect(3.5f, 5f, 21f, 18f, 3f)), Part(circle(10f, 11f, 2.2f), fill = true), Part("M4.5 21l6-6 4 4 3-3 6 6")),
    )
    val pencil = buildGlyph("pencil", 28f, 1.8f, listOf(Part("M5 23l1.5-5.5L19 5l4 4L10.5 21.5 5 23z"), Part("M16.5 7.5l4 4")))
    val share = buildGlyph(
        "share", 22f, 1.8f,
        listOf(Part("M11 13V2.5M7 6.2L11 2.5l4 3.7"), Part("M7.5 9H5a1.5 1.5 0 0 0-1.5 1.5v8A1.5 1.5 0 0 0 5 20h12a1.5 1.5 0 0 0 1.5-1.5v-8A1.5 1.5 0 0 0 17 9h-2.5")),
    )
    val external = buildGlyph("external", 14f, 1.5f, listOf(Part("M5 2H2.5a.5.5 0 0 0-.5.5v9a.5.5 0 0 0 .5.5h9a.5.5 0 0 0 .5-.5V9M8 2h4v4M12 2L6.5 7.5")))
    val clock = buildGlyph("clock", 14f, 1.4f, listOf(Part(circle(7f, 7f, 5.8f)), Part("M7 4v3.2l2 1.3")))
    val updown = buildGlyph("updown", 14f, 1.6f, listOf(Part("M4 5l3-3 3 3M4 9l3 3 3-3")))
    val up = buildGlyph("up", 14f, 2f, listOf(Part("M3 9l4-4 4 4")))
    val down = buildGlyph("down", 14f, 2f, listOf(Part("M3 5l4 4 4-4")))
    val minusCircle = buildGlyph("minusCircle", 22f, 0f, listOf(Part(circle(11f, 11f, 9f), fill = true)))
    val close = buildGlyph("close", 16f, 1.8f, listOf(Part("M4 4l8 8M12 4l-8 8")))
    val featScan = buildGlyph(
        "featScan", 24f, 1.8f,
        listOf(Part("M3 8V5a2 2 0 0 1 2-2h3M16 3h3a2 2 0 0 1 2 2v3M21 16v3a2 2 0 0 1-2 2h-3M8 21H5a2 2 0 0 1-2-2v-3"), Part("M7 9h10M7 12h10M7 15h6")),
    )
    val featMore = buildGlyph("featMore", 24f, 1.8f, listOf(Part("M4 6h16M4 12h16M4 18h10"), Part("M18 16v5M15.5 18.5h5")))
    val featSync = buildGlyph("featSync", 24f, 1.8f, listOf(Part("M7 18h10.5a3.5 3.5 0 0 0 .4-7A5.5 5.5 0 0 0 7.3 9.6 4.2 4.2 0 0 0 7 18z")))
    val featAnalytics = buildGlyph(
        "featAnalytics", 24f, 1.6f,
        listOf(Part(rrect(3f, 12f, 4f, 8f, 1f), fill = true), Part(rrect(10f, 7f, 4f, 13f, 1f)), Part(rrect(17f, 4f, 4f, 16f, 1f))),
    )
}
