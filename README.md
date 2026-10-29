# Bootsbuchung für Vereine – Android-App

Die App zur [Bootsbuchung für Vereine](https://github.com/Archonw/mrbs-bootsbuchung).
Beim ersten Start gibt man die Adresse der Bootsbuchung des eigenen Vereins
ein (z. B. `boote.mein-verein.de`). Danach öffnet die App sie im Vollbild,
mit Push-Benachrichtigungen und eigenem App-Symbol.

Technisch ist das eine *Trusted Web Activity*: Die App zeigt die Webseite des
Vereins über Chrome an. Alles, was die Webseite kann, kann die App auch – und
jede Verbesserung der Webseite kommt ohne App-Update bei allen an.

## Voraussetzungen auf dem Vereinsserver

- Die Bootsbuchung läuft über **HTTPS**.
- Sie bindet ein App-Manifest ein (`app-head.patch` und `Themes/<theme>/app/`).
  Daran erkennt die App beim Einrichten, dass sie richtig ist.
- Für Vollbild ohne Adressleiste: die Datei `/.well-known/assetlinks.json`
  mit dem Fingerabdruck dieser App. Fehlt sie, funktioniert alles, nur mit
  einer schmalen Adressleiste oben.

## Bauen

GitHub baut die App bei jeder Änderung automatisch (*Actions* → *App bauen*).
Unter *Artifacts* liegt:

- `bootsbuchung-test.apk` – zum direkten Installieren auf dem Handy
  (in den Android-Einstellungen „Unbekannte Apps installieren“ erlauben).
  Test-Versionen sind mit dem festen Schlüssel `app/debug.keystore`
  signiert (kein Geheimnis, nur für Tests) und lassen sich einfach
  drüber installieren.
- `bootsbuchung-store.aab` – für den Play Store. Entsteht nur, wenn die
  Secrets für den Signierschlüssel eingetragen sind:
  `BB_KEYSTORE_BASE64`, `BB_KEYSTORE_PASSWORD`, `BB_KEY_ALIAS`, `BB_KEY_PASSWORD`.

Lokal: Android Studio öffnen, Projekt öffnen, *Run*.

## Lizenz

GPL-2.0, wie das Hauptprojekt.
