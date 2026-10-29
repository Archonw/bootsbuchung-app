# Signierschlüssel für den Play Store

Jede Store-Version muss mit demselben **Upload-Schlüssel** signiert sein.
Google signiert die App danach noch einmal mit einem eigenen Schlüssel
(„Play App Signing“). Geht der Upload-Schlüssel verloren, kann man ihn bei
Google zurücksetzen lassen. Trotzdem gut aufbewahren!

## 1. Schlüssel erzeugen (einmalig, auf dem Laptop)

Java wird gebraucht (Ubuntu/Debian: `sudo apt install openjdk-17-jre-headless`).

```bash
keytool -genkeypair -v -keystore bootsbuchung-upload.jks -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Das Programm fragt nach einem Passwort (zweimal) und nach Name/Ort. Das
Passwort aufschreiben. Die Datei `bootsbuchung-upload.jks` **nie** ins
Repo legen, sondern sicher aufbewahren (z. B. Passwortmanager, USB-Stick).

## 2. Bei GitHub hinterlegen

Repo `bootsbuchung-app` → *Settings* → *Secrets and variables* → *Actions*
→ *New repository secret*, vier Einträge:

| Name | Wert |
|---|---|
| `BB_KEYSTORE_BASE64` | Ausgabe von `base64 -w0 bootsbuchung-upload.jks` |
| `BB_KEYSTORE_PASSWORD` | das Passwort |
| `BB_KEY_ALIAS` | `upload` |
| `BB_KEY_PASSWORD` | das Passwort (dasselbe) |

Ab dem nächsten Lauf unter *Actions* liegt zusätzlich
`bootsbuchung-store.aab` bereit. Diese Datei wird in der Play Console
hochgeladen.

## 3. Nach dem ersten Upload: Fingerabdruck auf die Server

Play Console → App → *Testen und veröffentlichen* → *App-Integrität* →
*App-Signatur*: dort steht der **SHA-256-Fingerabdruck des
App-Signaturschlüssels**. Der muss zusätzlich in die
`/.well-known/assetlinks.json` jedes Vereinsservers, sonst zeigt die
Store-Version eine Adressleiste.
