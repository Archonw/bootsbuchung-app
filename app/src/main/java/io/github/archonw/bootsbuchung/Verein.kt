package io.github.archonw.bootsbuchung

import android.content.Context
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL

/** Gespeicherte Adresse der Bootsbuchung des eigenen Vereins. */
object Verein {
    private const val PREFS = "verein"
    private const val KEY_ADRESSE = "adresse"

    fun adresse(context: Context): Uri? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ADRESSE, null)
            ?.let(Uri::parse)

    fun speichern(context: Context, adresse: Uri) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ADRESSE, adresse.toString())
            .apply()
    }

    /**
     * Macht aus einer Eingabe wie „boote.mein-verein.de“ oder
     * „https://boote.mein-verein.de/index.php“ die Grundadresse
     * „https://boote.mein-verein.de/“. Gibt null zurück, wenn die Eingabe
     * keine gültige Adresse ist.
     */
    fun normalisieren(eingabe: String): Uri? {
        var text = eingabe.trim()
        if (text.isEmpty()) return null
        if (!text.contains("://")) text = "https://$text"
        val uri = Uri.parse(text)
        if (uri.scheme?.lowercase() != "https" || uri.host.isNullOrBlank()) return null
        var pfad = uri.path.orEmpty()
        // Auf eine einzelne MRBS-Seite verlinkt? Dann deren Verzeichnis nehmen.
        if (pfad.endsWith(".php")) pfad = pfad.substringBeforeLast('/')
        if (!pfad.endsWith("/")) pfad += "/"
        return uri.buildUpon().path(pfad).query(null).fragment(null).build()
    }

    sealed class Pruefung {
        object Ok : Pruefung()
        object KeineBootsbuchung : Pruefung()
        data class Fehler(val grund: String) : Pruefung()
    }

    /**
     * Prüft, ob unter der Adresse eine Bootsbuchung mit App-Unterstützung
     * läuft: Die Seite muss ein App-Manifest einbinden (app-head.patch).
     * Läuft im Hintergrund-Thread.
     */
    fun pruefen(adresse: Uri): Pruefung {
        return try {
            val verbindung = URL(adresse.toString()).openConnection() as HttpURLConnection
            verbindung.connectTimeout = 10_000
            verbindung.readTimeout = 10_000
            verbindung.instanceFollowRedirects = true
            verbindung.setRequestProperty("Accept", "text/html")
            try {
                if (verbindung.responseCode !in 200..299) {
                    return Pruefung.Fehler("Der Server antwortet mit Fehler ${verbindung.responseCode}.")
                }
                val html = verbindung.inputStream.bufferedReader().use { reader ->
                    val puffer = CharArray(256 * 1024)
                    val n = reader.read(puffer)
                    if (n > 0) String(puffer, 0, n) else ""
                }
                if (Regex("""rel=["']?manifest""", RegexOption.IGNORE_CASE).containsMatchIn(html)) {
                    Pruefung.Ok
                } else {
                    Pruefung.KeineBootsbuchung
                }
            } finally {
                verbindung.disconnect()
            }
        } catch (e: java.net.UnknownHostException) {
            Pruefung.Fehler("Diese Adresse gibt es nicht. Bitte prüfe die Schreibweise.")
        } catch (e: javax.net.ssl.SSLException) {
            Pruefung.Fehler("Die Verbindung ist nicht sicher (Zertifikat ungültig).")
        } catch (e: java.io.IOException) {
            Pruefung.Fehler("Keine Verbindung. Bitte prüfe das Internet und versuche es noch einmal.")
        }
    }
}
