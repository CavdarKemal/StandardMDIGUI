# Claude Code Kontext - StandardMDIGUI

## Projekt-Übersicht
**Pfad:** `E:\Projekte\ClaudeCode\StandardMDIGUI`
**Typ:** Java Swing MDI-Anwendung (Multi-Document Interface)
**Build:** Maven, Java 24
**IDE:** IntelliJ IDEA

## Durchgeführte Änderungen (24.12.2025)

### 1. Bug-Fixes
- `setResizable(true)` in MainFrame hinzugefügt
- Fenster-Validierung für Multi-Monitor-Support (verhindert Off-Screen-Positionierung)
- ComboBoxen im Settings-Panel behalten jetzt feste Höhe

### 2. Logging
- SLF4J + Logback integriert
- `logback.xml` Konfiguration erstellt
- Logger in allen Klassen implementiert

### 3. Error-Handling
- `ConfigurationException` und `ViewException` erstellt
- AppConfig: Thread-safe Singleton (Eager Initialization)
- Besseres Exception-Handling statt `printStackTrace()`

### 4. Neue Features
- **DatabaseView:** Neue View mit generischer JDBC-Unterstützung
  - Verbindung zu MySQL, PostgreSQL, Oracle, SQL Server, H2, SQLite
  - SQL-Query-Editor mit Ergebnistabelle
  - **Connection Management:**
    - ComboBox für gespeicherte Verbindungen in DatabaseView
    - "<Neue Verbindung>" Option
    - Speichern/Löschen Buttons
    - Persistierung in config.properties (Base64-kodierte Passwörter)
  - **Zentrales Connection Management (NEU):**
    - `ConnectionManager`: Singleton für alle DB-Verbindungen
    - `ConnectionInfo`: Utility-Klasse für Verbindungsdaten
    - Listener-Pattern für Änderungsbenachrichtigungen
    - DB-Verbindungen ComboBox im MainFrame Settings-Panel
    - "DB-View öffnen" Button mit vorselektierter Verbindung
- **ConfigEntry:** Java Record für typsichere Config-Werte
- **PostgreSQL JDBC Driver:** Version 42.7.4 in pom.xml hinzugefügt

### 5. Tests
- JUnit 5 + AssertJ-Swing für GUI-Tests
- Test-Klassen: `AppConfigTest`, `ConfigEntryTest`, `MainFrameTest`, `SampleViewTest`, `DatabaseViewTest`

## Projekt-Struktur
```
src/main/java/de/cavdar/
├── AppConfig.java          (Singleton, Thread-safe)
├── BaseView.java           (Abstract, Template Method)
├── DatabaseView.java       (JDBC-View mit ConnectionManager)
├── MainFrame.java          (Hauptfenster mit DB-Verbindungen ComboBox)
├── SampleView.java         (Beispiel-View)
├── exception/
│   ├── ConfigurationException.java
│   └── ViewException.java
└── util/
    ├── ConfigEntry.java    (Record)
    ├── ConnectionInfo.java (DB-Verbindungsdaten)
    └── ConnectionManager.java (Zentrales Connection Management)

src/main/resources/
└── logback.xml

src/test/java/de/cavdar/
├── AppConfigTest.java
├── DatabaseViewTest.java
├── MainFrameTest.java
├── SampleViewTest.java
└── util/
    └── ConfigEntryTest.java
```

## Git-Konfiguration
- **User:** Kemal Cavdar <kemal@cavdar.de>
- **Branch:** master
- **Safe Directory:** bereits konfiguriert

## Lokale Umgebung
- **PostgreSQL 16:** Installiert via winget (`PostgreSQL.PostgreSQL.16`)
  - Service: `postgresql-x64-16`
  - Standard-Port: 5432
  - JDBC-URL: `jdbc:postgresql://localhost:5432/postgres`

## Offene Punkte / Mögliche Erweiterungen
- Factory-Klassen (MenuBarFactory, ToolbarFactory, etc.) - wurde vorerst als nicht nötig erachtet
- Weitere Views nach Bedarf
- Datenbankverbindung mit PostgreSQL testen

## Prompt zum Fortsetzen
```
Ich arbeite am Java-Projekt StandardMDIGUI unter E:\Projekte\ClaudeCode\StandardMDIGUI.
Bitte lies die Datei CLAUDE_CONTEXT.md für den Kontext unserer letzten Sitzung.
```
