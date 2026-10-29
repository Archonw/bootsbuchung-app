package io.github.archonw.bootsbuchung

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView

/** Einrichtung: Adresse der Bootsbuchung des eigenen Vereins eingeben. */
class EinrichtungActivity : Activity() {

    private lateinit var eingabe: EditText
    private lateinit var knopf: Button
    private lateinit var meldung: TextView
    private lateinit var laeuft: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_einrichtung)

        // Ab Android 15 reicht die App bis unter die Systemleisten
        val wurzel = findViewById<View>(R.id.wurzel)
        wurzel.setOnApplyWindowInsetsListener { v, insets ->
            val r = if (android.os.Build.VERSION.SDK_INT >= 30) {
                insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                    .let { intArrayOf(it.left, it.top, it.right, it.bottom) }
            } else {
                @Suppress("DEPRECATION")
                intArrayOf(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            v.setPadding(r[0], r[1], r[2], r[3])
            insets
        }

        eingabe = findViewById(R.id.adresse)
        knopf = findViewById(R.id.verbinden)
        meldung = findViewById(R.id.meldung)
        laeuft = findViewById(R.id.laeuft)

        Verein.adresse(this)?.let { eingabe.setText(it.host + it.path.orEmpty().trimEnd('/')) }

        knopf.setOnClickListener { verbinden() }
        eingabe.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                verbinden()
                true
            } else {
                false
            }
        }
    }

    private fun verbinden() {
        val adresse = Verein.normalisieren(eingabe.text.toString())
        if (adresse == null) {
            zeigeFehler(getString(R.string.fehler_adresse))
            return
        }
        beschaeftigt(true)
        Thread {
            val ergebnis = Verein.pruefen(adresse)
            runOnUiThread {
                if (isFinishing) return@runOnUiThread
                beschaeftigt(false)
                when (ergebnis) {
                    is Verein.Pruefung.Ok -> {
                        Verein.speichern(this, adresse)
                        startActivity(Intent(this, StartActivity::class.java))
                        finish()
                    }
                    is Verein.Pruefung.KeineBootsbuchung -> zeigeFehler(getString(R.string.fehler_keine_bootsbuchung))
                    is Verein.Pruefung.Fehler -> zeigeFehler(ergebnis.grund)
                }
            }
        }.start()
    }

    private fun beschaeftigt(ja: Boolean) {
        knopf.isEnabled = !ja
        eingabe.isEnabled = !ja
        laeuft.visibility = if (ja) View.VISIBLE else View.GONE
        if (ja) meldung.visibility = View.GONE
    }

    private fun zeigeFehler(text: String) {
        meldung.text = text
        meldung.visibility = View.VISIBLE
    }
}
