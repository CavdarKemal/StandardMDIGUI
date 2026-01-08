# Entwicklungsumgebung Setup - Windows 11 VM

Diese Anleitung beschreibt die Installation aller benötigten Tools für die Entwicklung des StandardMDIGUI-Projekts.

## Voraussetzungen

- Windows 11 (64-bit)
- Mindestens 8 GB RAM (für VM)
- 50 GB freier Speicherplatz
- Internetzugang

---

## 1. Windows Terminal (empfohlen)

Windows Terminal bietet bessere Unterstützung für CLI-Tools.

```powershell
winget install Microsoft.WindowsTerminal
```

---

## 2. Git

Versionskontrolle für den Quellcode.

```powershell
winget install Git.Git
```

**Nach Installation - Terminal neu starten**, dann konfigurieren:

```powershell
git config --global user.name "Dein Name"
git config --global user.email "deine@email.com"
```

---

## 3. Java Development Kit (JDK 23)

Das Projekt verwendet Java 23.

```powershell
winget install EclipseAdoptium.Temurin.23.JDK
```

**Prüfen (neues Terminal öffnen):**

```powershell
java -version
# Erwartete Ausgabe: openjdk version "23.x.x"
```

---

## 4. Apache Maven

Build-Tool für das Java-Projekt.

```powershell
winget install Apache.Maven
```

**Prüfen (neues Terminal öffnen):**

```powershell
mvn -version
# Erwartete Ausgabe: Apache Maven 3.9.x
```

---

## 5. Node.js (LTS)

Wird für Claude Code benötigt.

```powershell
winget install OpenJS.NodeJS.LTS
```

**Prüfen (neues Terminal öffnen):**

```powershell
node -version
# Erwartete Ausgabe: v20.x.x oder höher

npm -version
# Erwartete Ausgabe: 10.x.x oder höher
```

---

## 6. PostgreSQL 16

Datenbank für die Anwendung.

```powershell
winget install PostgreSQL.PostgreSQL.16
```

**Während der Installation:**
- Passwort für `postgres` User festlegen (z.B. `postgres`)
- Port: `5432` (Standard)
- Locale: Default

**Nach Installation - Datenbank erstellen:**

```powershell
# PostgreSQL bin-Verzeichnis zum PATH hinzufügen (falls nicht automatisch)
# Normalerweise: C:\Program Files\PostgreSQL\16\bin

# Datenbank erstellen
psql -U postgres -c "CREATE DATABASE standardmdi;"

# Prüfen
psql -U postgres -l
```

**Alternativ mit pgAdmin** (wird mit PostgreSQL installiert):
1. pgAdmin starten
2. Server verbinden (localhost, postgres/postgres)
3. Rechtsklick auf Databases → Create → Database
4. Name: `standardmdi`

---

## 7. Claude Code

KI-gestütztes Entwicklungstool.

```powershell
npm install -g @anthropic-ai/claude-code
```

**Prüfen:**

```powershell
claude --version
```

**Erster Start:**

```powershell
claude
```

Beim ersten Start wird nach dem API-Key gefragt oder Browser-Authentifizierung durchgeführt.

---

## 8. IntelliJ IDEA (optional)

IDE für Java-Entwicklung.

```powershell
# Community Edition (kostenlos)
winget install JetBrains.IntelliJIDEA.Community

# Oder Ultimate Edition (kostenpflichtig, 30 Tage Trial)
winget install JetBrains.IntelliJIDEA.Ultimate
```

---

## 9. Projekt klonen

```powershell
# Verzeichnis erstellen
mkdir C:\Projekte
cd C:\Projekte

# Projekt klonen (URL anpassen falls nötig)
git clone <repository-url> StandardMDIGUI

# Oder: Projekt vom Host kopieren (bei Shared Folder)
# Copy-Item "\\vmware-host\Shared Folders\Projekte\StandardMDIGUI" -Destination "C:\Projekte\" -Recurse
```

---

## 10. Projekt bauen und testen

```powershell
cd C:\Projekte\StandardMDIGUI

# Dependencies herunterladen und bauen
mvn clean package

# Tests ausführen
mvn test

# Anwendung starten
mvn exec:java
```

---

## 11. Datenbank initialisieren (optional)

Falls du die Beispieldaten aus dem Docker-Setup verwenden möchtest:

```powershell
cd C:\Projekte\StandardMDIGUI
psql -U postgres -d standardmdi -f docker/init-db.sql
```

---

## Schnell-Setup (alle Befehle)

Kopiere diese Befehle nacheinander in PowerShell (als Administrator):

```powershell
# Basis-Tools
winget install Microsoft.WindowsTerminal
winget install Git.Git

# Java & Build
winget install EclipseAdoptium.Temurin.23.JDK
winget install Apache.Maven

# Node.js & Claude Code
winget install OpenJS.NodeJS.LTS

# PostgreSQL
winget install PostgreSQL.PostgreSQL.16

# IDE (optional)
winget install JetBrains.IntelliJIDEA.Community
```

**Nach allen Installationen - Terminal schließen und neu öffnen!**

```powershell
# Claude Code installieren
npm install -g @anthropic-ai/claude-code

# Prüfen ob alles funktioniert
java -version
mvn -version
node -version
git --version
claude --version
```

---

## Troubleshooting

### winget nicht gefunden
Windows 11 sollte winget vorinstalliert haben. Falls nicht:
1. Microsoft Store öffnen
2. "App Installer" suchen und installieren

### Java/Maven nicht im PATH
Terminal neu starten. Falls Problem bleibt:
```powershell
# Umgebungsvariablen prüfen
echo $env:JAVA_HOME
echo $env:PATH
```

### PostgreSQL Verbindung schlägt fehl
1. Dienst prüfen: `services.msc` → "postgresql-x64-16" muss laufen
2. Firewall: Port 5432 freigeben
3. pg_hba.conf prüfen (falls Remote-Zugriff nötig)

### Claude Code Authentifizierung
Beim ersten Start `claude` wird ein Browser-Fenster geöffnet oder nach einem API-Key gefragt.

---

## Verbindung zu PostgreSQL (in der App)

| Feld | Wert |
|------|------|
| Host | `localhost` |
| Port | `5432` |
| Database | `standardmdi` |
| User | `postgres` |
| Password | (dein gewähltes Passwort) |
| JDBC URL | `jdbc:postgresql://localhost:5432/standardmdi` |

---

## Nächste Schritte

1. Terminal öffnen
2. `cd C:\Projekte\StandardMDIGUI`
3. `claude` starten
4. Weiterarbeiten am Projekt!
