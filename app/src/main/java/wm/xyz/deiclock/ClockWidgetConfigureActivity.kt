package wm.xyz.deiclock

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.textfield.TextInputEditText
import wm.xyz.deiclock.databinding.ClockWidgetConfigureBinding
import java.util.*


/**
 * The configuration screen for the [ClockWidget] AppWidget.
 */
class ClockWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var timeZoneLeftSpinner: Spinner
    private lateinit var timeZoneRightSpinner: Spinner
    private lateinit var joinerEditText: TextInputEditText
    private var onClickListener = View.OnClickListener {
        val context = this@ClockWidgetConfigureActivity

        // Store the relevant preferences to show to the user later.
        stripHtml(joinerEditText.text.toString())?.let { string ->
            saveStringPref(context, appWidgetId, "joiner", string)
        }
        saveTimeZonePref(context, appWidgetId, timeZoneLeftSpinner, 0)
        saveTimeZonePref(context, appWidgetId, timeZoneRightSpinner, 1)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
    private lateinit var binding: ClockWidgetConfigureBinding

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = ClockWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timeZoneLeftSpinner = binding.spinnerTimezonesLeft
        timeZoneRightSpinner = binding.spinnerTimezonesRight
        joinerEditText = binding.textInputJoiner

        binding.addButton.setOnClickListener(onClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        populateTimeZoneSpinner(this, timeZoneRightSpinner)
        populateTimeZoneSpinner(this, timeZoneLeftSpinner)
    }

}

private const val PREFS_NAME = "wm.xyz.deiclock.ClockWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

/**
 * Saves the selected timezone from a spinner.
 *
 * @param context       The context of the call.
 * @param appWidgetId   The ID of the widget; used in the prefix of the preference.
 * @param spinner       The spinner selecting a valid list of timezones.
 * @param clock         Whether this should be the left clock (0) or the right clock (>0)
 */
internal fun saveTimeZonePref(context: Context, appWidgetId: Int, spinner: Spinner, clock: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(
        "${PREF_PREFIX_KEY}${appWidgetId}_${if (clock == 0) "left_tz" else "right_tz"}",
        spinner.selectedItem.toString()
    )
    prefs.apply()
}

/**
 * Saves a string object to a specified preference.
 *
 * @param context       The context of the call.
 * @param appWidgetId   The ID of the widget; used in the prefix of the preference.
 * @param pref_key      The preference key to save this string object to.
 * @param content       The string object itself.
 */
internal fun saveStringPref(context: Context, appWidgetId: Int, pref_key: String, content: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString("${PREF_PREFIX_KEY}${appWidgetId}_${pref_key}", content)
    prefs.apply()
}

/**
 * Loads a specified string preference.
 *
 * @param context       The context of the call.
 * @param appWidgetId   The ID of the widget; used in the prefix of the preference.
 * @param pref_key      The preference key to load.
 *
 * @return String       The string from the stored preference, a blank string if otherwise.
 */
internal fun loadStringPref(context: Context, appWidgetId: Int, pref_key: String): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    return prefs.getString("${PREF_PREFIX_KEY}${appWidgetId}_${pref_key}", "") ?: ""
}

/**
 * Loads the relevant timezone preference for a specified clock.
 *
 * @param context       The context of the call.
 * @param appWidgetId   The ID of the widget; used in the prefix of the preference.
 * @param clock         Whether this should be the left clock (0) or the right clock (>0)
 */
internal fun loadTimeZonePref(context: Context, appWidgetId: Int, clock: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(
        "${PREF_PREFIX_KEY}${appWidgetId}_${if (clock == 0) "left_tz" else "right_tz"}",
        null
    )
    return titleValue ?: context.getString(R.string.clock_default_tz)
}

// Removes all preferences related to the appWidgetId
internal fun clearPreference(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val prefEntries: Map<String, *> =
        prefs.all.filterKeys { it.contains("${PREF_PREFIX_KEY}${appWidgetId}") }

    prefEntries.forEach {
        prefs.edit().remove(it.key).apply()
    }
}

// Populates a spinner with available timezones, retrieved from the TimeZone class.
internal fun populateTimeZoneSpinner(context: Context, spinner: Spinner) {
    // TODO: Can we make this display a nicer list (i.e, similar to the system's clock app)?
    val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
        context, android.R.layout.simple_spinner_item, TimeZone.getAvailableIDs()
    )
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = spinnerArrayAdapter
}

// Strips HTML tags from user inputs.
private fun stripHtml(html: String?): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(html).toString()
    }
}
