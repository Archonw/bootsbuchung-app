package io.github.archonw.bootsbuchung

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.trusted.TrustedWebActivityIntentBuilder
import com.google.androidbrowserhelper.trusted.TwaLauncher

/**
 * Startet die Bootsbuchung des Vereins im Vollbild (Trusted Web Activity).
 * Ist noch kein Verein eingerichtet, geht es zur Einrichtung.
 *
 * Vollbild ohne Adressleiste gibt es nur, wenn der Vereinsserver unter
 * /.well-known/assetlinks.json diese App bestätigt. Sonst zeigt Chrome eine
 * schmale Leiste mit der Adresse – die Buchung funktioniert trotzdem.
 */
class StartActivity : Activity() {

    private var launcher: TwaLauncher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adresse = Verein.adresse(this)
        if (adresse == null || intent?.action == AKTION_VEREIN_WECHSELN) {
            startActivity(Intent(this, EinrichtungActivity::class.java))
            finish()
            return
        }

        // Nach Drehen o. Ä. nicht ein zweites Mal öffnen
        if (savedInstanceState != null) {
            finish()
            return
        }

        val farbe = Color.parseColor("#0C0E0D")
        val farben = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(farbe)
            .setNavigationBarColor(farbe)
            .build()
        val builder = TrustedWebActivityIntentBuilder(adresse)
            .setDefaultColorSchemeParams(farben)

        launcher = TwaLauncher(this).also {
            it.launch(builder, null, null, { finish() }, TwaLauncher.CCT_FALLBACK_STRATEGY)
        }
    }

    override fun onDestroy() {
        launcher?.destroy()
        super.onDestroy()
    }

    companion object {
        const val AKTION_VEREIN_WECHSELN = "io.github.archonw.bootsbuchung.VEREIN_WECHSELN"
    }
}
