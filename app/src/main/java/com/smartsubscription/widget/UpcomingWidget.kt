package com.smartsubscription.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.smartsubscription.AppState
import com.smartsubscription.MainActivity
import com.smartsubscription.R
import com.smartsubscription.data.dueText
import com.smartsubscription.data.money
import com.smartsubscription.ui.components.CategoryGlyphs
import com.smartsubscription.i18n.widgetText
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The iOS "Upcoming Renewals" widget: active subscriptions renewing in the next [WINDOW_DAYS] days, soonest first.
 * The app has no store the widget could read, so the app writes a snapshot of its active list on every change
 * and the widget counts days against today each time it draws.
 */
private data class RowIds(val row: Int, val icon: Int, val name: Int, val due: Int, val amount: Int)

class UpcomingWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = render(context, LocalDate.now())
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    companion object {
        private const val WINDOW_DAYS = 7
        private const val PREFS = "widget"
        private const val KEY = "active"

        /** Called by the app whenever its subscriptions or categories change. */
        fun publish(context: Context, state: AppState) {
            val rows = JSONArray()
            state.ledger.active.forEach {
                rows.put(
                    JSONObject()
                        .put("name", it.name)
                        .put("icon", state.category(it.categoryId)?.icon)
                        .put("next", it.nextBilling.toString())
                        .put("amount", it.amount)
                        .put("currency", it.currency),
                )
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, rows.toString()).apply()

            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, UpcomingWidget::class.java))
            if (ids.isNotEmpty()) UpcomingWidget().onUpdate(context, manager, ids)
        }

        private fun render(context: Context, today: LocalDate): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_upcoming)
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.root, open)

            val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            // Overdue renewals stay in: an unpaid charge past its date is the one most worth seeing.
            val due = saved?.let { json ->
                val rows = JSONArray(json)
                (0 until rows.length()).map { rows.getJSONObject(it) }
                    .map { it to ChronoUnit.DAYS.between(today, LocalDate.parse(it.getString("next"))) }
                    .filter { (_, days) -> days <= WINDOW_DAYS }
            }.orEmpty()

            // The launcher inflates the layout in the system language, so the header is set here to follow the app's.
            views.setTextViewText(R.id.title, widgetText.title)
            views.setViewVisibility(R.id.rows, if (due.isEmpty()) View.GONE else View.VISIBLE)
            views.setViewVisibility(R.id.empty, if (due.isEmpty()) View.VISIBLE else View.GONE)
            if (due.isEmpty()) {
                views.setTextViewText(
                    R.id.empty,
                    if (saved == null) widgetText.openApp
                    else widgetText.nothingRenews(WINDOW_DAYS),
                )
            }

            val rowIds = listOf(
                RowIds(R.id.row0, R.id.icon0, R.id.name0, R.id.due0, R.id.amount0),
                RowIds(R.id.row1, R.id.icon1, R.id.name1, R.id.due1, R.id.amount1),
                RowIds(R.id.row2, R.id.icon2, R.id.name2, R.id.due2, R.id.amount2),
            )
            val iconPx = (16 * context.resources.displayMetrics.density).toInt()
            rowIds.forEachIndexed { i, (rowId, iconId, nameId, dueId, amountId) ->
                val entry = due.getOrNull(i)
                views.setViewVisibility(rowId, if (entry == null) View.GONE else View.VISIBLE)
                if (entry == null) return@forEachIndexed
                val (row, days) = entry
                views.setImageViewBitmap(iconId, CategoryGlyphs.bitmap(row.optString("icon").ifEmpty { null }, iconPx))
                if (Build.VERSION.SDK_INT >= 31) views.setColorStateList(iconId, "setImageTintList", R.color.widget_text2)
                else views.setInt(iconId, "setColorFilter", context.getColor(R.color.widget_text2))
                views.setTextViewText(nameId, row.getString("name"))
                views.setTextViewText(dueId, dueText(days))
                // Same rule as the Home rail: orange means within three days.
                val color = if (days <= 3) R.color.widget_soon else R.color.widget_text2
                // From API 31 the launcher resolves the colour, so it follows a light/dark switch without a redraw.
                if (Build.VERSION.SDK_INT >= 31) views.setColorStateList(dueId, "setTextColor", color)
                else views.setTextColor(dueId, context.getColor(color))
                views.setTextViewText(amountId, money(row.getDouble("amount"), row.getString("currency")))
            }

            val more = due.size - rowIds.size
            views.setViewVisibility(R.id.more, if (more > 0) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.more, widgetText.more(more))
            return views
        }
    }
}
