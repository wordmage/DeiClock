package wm.xyz.deiclock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ClockWidgetConfigureActivity]
 */
class ClockWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            clearPreference(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetTimeZoneLeft = loadTimeZonePref(context, appWidgetId, 0)
    val widgetTimeZoneRight = loadTimeZonePref(context, appWidgetId, 1)
    val joinerText = loadStringPref(context, appWidgetId, "joiner")

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.clock_widget)

    views.setTextViewText(R.id.widget_clock_joiner, joinerText)
    views.setString(R.id.widget_left_clock, "setTimeZone", widgetTimeZoneLeft)
    views.setString(R.id.widget_right_clock, "setTimeZone", widgetTimeZoneRight)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}