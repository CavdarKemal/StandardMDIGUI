# Claude Code Kontext - StandardMDIGUI

## Projekt-Übersicht
**Pfad:** `E:\Projekte\ClaudeCode\StandardMDIGUI`
**Typ:** Java Swing MDI-Anwendung (Multi-Document Interface)
**Build:** Maven, Java 23 (Docker-kompatibel)
**IDE:** IntelliJ IDEA
**Ziel:** Modulares, erweiterbares Gerüst für zukünftige Programme

## Letzte Session (26.12.2025 Abend - Teil 3)

### Zusammenfassung der Änderungen

**Frühere Sessions:**
1. **Flaky Tests gefixt** - JUnit Vintage Engine + robuste `clickMenu()` Helper-Methode
2. **4 neue Features** - Keyboard Shortcuts, Icons, Menu Groups, SplitPane Persistence
3. **Custom Icons** - Eigene PNG-Icons statt UIManager-Icons
4. **DatabaseView erweitert** - SplitPane-Layout mit Tabellen-Tree
5. **Docker-Setup erstellt** - Dockerfile, docker-compose.yml, Startup-Scripts
6. **Docker-Voraussetzungen installiert** - Docker Desktop 4.55.0, VcXsrv 21.1.16.1, WSL2
7. **Docker-Setup erfolgreich getestet** - App läuft in Container mit X11-Forwarding
8. **Java 24 → 23** - Für Docker-Kompatibilität (Eclipse Temurin 23 verfügbar)
9. **Spalten-Info beim Aufklappen** - Lazy Loading von Spalten mit Typ, Größe, NULL, PK-Markierung
10. **Config-Persistenz in Docker** - Volume für `/app/config/config.properties`

**Aktuelle Session:**
1. **Config-Selector in Toolbar** - ComboBox zum Wechseln zwischen Config-Dateien
2. **GUI-Reload bei Config-Wechsel** - Alle Einstellungen werden bei Config-Wechsel aktualisiert
3. **Dynamischer Config-Speicherpfad** - Änderungen werden in die aktuell selektierte Config gespeichert

### Config-Wechsel Feature (neu)

**Problem:** Beim Wechsel der Config-Datei via ComboBox wurden DB_CONNECTIONS und andere Einstellungen nicht aktualisiert.

**Lösung:**

1. **ConnectionManager.reloadConnections()** - Neue Methode zum Neuladen der Verbindungen
2. **SettingsPanel.reloadAllSettings()** - Aktualisiert alle GUI-Komponenten:
   - DB-Verbindungen (ComboBox)
   - Test Sources, Test Types, ITSQ Revisions (ComboBoxen)
   - Alle Checkboxen (Dump, SFTP-Upload, etc.)
3. **AppConfig.currentFilePath** - Verfolgt die aktuell aktive Config-Datei
   - `loadFrom()` setzt `currentFilePath`
   - `save()` speichert in `currentFilePath`

**Verhalten:**
| Aktion | Ergebnis |
|--------|----------|
| App startet mit `config.properties` | `currentFilePath = config.properties` |
| Wechsel zu `default.config.properties` | GUI aktualisiert, `currentFilePath` geändert |
| Neue DB-Verbindung erstellen | Wird in `default.config.properties` gespeichert |

### Test-Configs (docker/config/)
Zwei Config-Dateien mit unterschiedlichen Werten zum Testen:

| Einstellung | config.properties | default.config.properties |
|-------------|-------------------|---------------------------|
| DB_CONNECTIONS | Default, Cavdar | LocalDB, DevDB |
| TEST-SOURCES | ITSQ;LOCAL;REMOTE | LOCAL;REMOTE |
| TEST-TYPES | PHASE1;PHASE2;PHASE1_AND_PHASE2 | UNIT;INTEGRATION;E2E |
| ITSQ_REVISIONS | EINE_PHASEN;ZWEI_PHASEN | v1.0;v2.0;v3.0;v4.0 |

### Custom Icons (IconLoader)
Neue Utility-Klasse `de.cavdar.util.IconLoader` lädt PNG-Icons aus `resources/icons/`:

| View | Icon-Datei |
|------|------------|
| SampleView | `client.png` |
| ProzessView | `gear_run.png` |
| AnalyseView | `table_sql.png` |
| TreeView | `folder_view.png` |
| CustomerTreeView | `folder_cubes.png` |

```java
// Verwendung:
@Override
public Icon getIcon() {
    return IconLoader.load("client.png");
}
```

### DatabaseView SplitPane-Layout
Neues Layout unter "Verbindung":
```
┌─────────────────────────────────────────────────┐
│ Verbindung (Connection Panel)                   │
├──────────────┬──────────────────────────────────┤
│ Tabellen     │ SQL-Abfrage                      │
│ ├─Tabellen   │ ┌────────────────────────────┐   │
│ │ ├─users    │ │ SELECT * FROM users        │   │
│ │ └─orders   │ └────────────────────────────┘   │
│ └─Views      ├──────────────────────────────────┤
│   └─v_sales  │ Ergebnisse                       │
│              │ ┌────────────────────────────┐   │
│              │ │ id | name | email          │   │
│              │ └────────────────────────────┘   │
└──────────────┴──────────────────────────────────┘
```

**Funktionen:**
- `loadTables()` - Lädt Tabellen/Views nach Verbindung aus DatabaseMetaData
- `onTableSelected()` - Klick auf Tabelle füllt SQL-Query mit `SELECT * FROM tablename`
- `clearTables()` - Leert Tree bei Disconnect
- `loadColumns()` - Lazy Loading von Spalten beim Aufklappen einer Tabelle

**Spalten-Info (Lazy Loading):**
```
Tabellen
└─ customers
   ├─ 🔑 id (int4, 10)
   ├─ name (varchar, 100, NULL)
   ├─ email (varchar, 255, NULL)
   └─ created_at (timestamp, 29, NULL)
```
- Primary Keys werden mit 🔑 markiert
- Format: `spaltenname (TYP, größe[, NULL])`

## Package-Struktur
```
src/main/java/de/cavdar/
├── design/                    # GUI-Panels (für GUI-Designer)
│   ├── EmbeddablePanel.java   # Abstract für einbettbare Panels
│   ├── SettingsPanel.java     # Einstellungen
│   ├── TreePanel.java         # Customer-Tree (links)
│   ├── DesktopPanel.java      # MDI-Desktop
│   ├── BaseViewPanel.java     # Basis für View-Panels
│   ├── SampleViewPanel.java
│   ├── ProzessViewPanel.java
│   ├── AnalyseViewPanel.java
│   ├── DatabaseViewPanel.java # Mit SplitPanes und TableTree
│   ├── TreeViewPanel.java     # Basis für Tree-Views
│   └── CustomerTreeViewPanel.java
│
├── view/                      # View-Logik
│   ├── ViewInfo.java          # Interface für Metadaten
│   ├── BaseView.java          # Abstract, implementiert ViewInfo
│   ├── SampleView.java
│   ├── ProzessView.java
│   ├── AnalyseView.java
│   ├── DatabaseView.java      # Mit Tabellen-Lade-Logik
│   ├── TreeView.java          # Basis für Tree-Views
│   └── CustomerTreeView.java
│
├── frame/
│   └── MainFrame.java         # Hauptfenster mit Menu Groups
│
├── model/
│   ├── AppConfig.java         # Singleton, gruppiertes Speichern
│   ├── ConnectionInfo.java    # DB-Verbindungsdaten
│   └── ConfigEntry.java       # Record
│
├── util/
│   ├── ConnectionManager.java # Zentrales Connection Management
│   └── IconLoader.java        # PNG-Icons aus Resources laden
│
└── exception/
    ├── ConfigurationException.java
    └── ViewException.java

src/main/resources/
└── icons/                     # PNG-Icons für Views
    ├── client.png
    ├── gear_run.png
    ├── table_sql.png
    ├── folder_view.png
    └── folder_cubes.png

src/test/java/de/cavdar/
├── AppConfigTest.java
├── DatabaseViewTest.java
├── DatabaseIntegrationTest.java
├── MainFrameTest.java         # Mit robuster clickMenu() Methode
├── SampleViewTest.java
└── util/
    └── ConfigEntryTest.java
```

## Panel/View Separation Pattern
Jede View besteht aus zwei Klassen:
1. **Panel-Klasse** (`design` Package): Nur GUI-Komponenten, keine Logik
2. **View-Klasse** (`view` Package): Nur Logik und Event-Handler

## ViewInfo Interface
Jede View implementiert:
```java
public interface ViewInfo {
    String getMenuLabel();      // Menü-Text (default: getTitle())
    String getToolbarLabel();   // Toolbar-Text (null = kein Button)
    Icon getIcon();             // Icon für Menü/Toolbar
    KeyStroke getKeyboardShortcut(); // Tastaturkürzel
    String getMenuGroup();      // Submenu-Gruppe (default: null)
}
```

## Keyboard Shortcuts
| View | Shortcut |
|------|----------|
| SampleView (Kunden Analyse) | Ctrl+1 |
| ProzessView | Ctrl+2 |
| AnalyseView | Ctrl+3 |
| TreeView | Ctrl+4 |
| CustomerTreeView | Ctrl+5 |

## Menu Groups (Submenüs)
- **Analyse**: Kunden Analyse, Analyse
- **Verwaltung**: Prozess, Kunden Explorer
- **Navigation**: Tree View

## SplitPane Divider Persistence
In `config.properties`:
- `LAST_LEFT_SPLIT_DIVIDER` - Vertikaler Split (Settings/Tree)
- `LAST_MAIN_SPLIT_DIVIDER` - Horizontaler Split (Left/Desktop)

## Konfigurationsdatei (AppConfig)

### Speicherort-Priorität
1. **Kommandozeilen-Argument:** `java -jar app.jar /pfad/zur/config.properties`
2. **System Property:** `java -Dconfig.file=/pfad/zur/config.properties -jar app.jar`
3. **Umgebungsvariable:** `CONFIG_FILE_PATH=/pfad/zur/config.properties`
4. **Standard:** `config.properties` (im Arbeitsverzeichnis)

### Beispiele
```bash
# Lokal mit eigener Config
java -jar StandardMDIGUI.jar C:\Users\kemal\meine-config.properties

# Mit System Property
java -Dconfig.file=/opt/config/app.properties -jar StandardMDIGUI.jar

# Docker (via Umgebungsvariable in docker-compose.yml)
CONFIG_FILE_PATH=/app/config/config.properties
```

### Docker-Persistenz (Bind Mounts)
| Host-Pfad | Container-Pfad | Zweck |
|-----------|----------------|-------|
| `docker/config/` | `/app/config/` | Aktive Config (wird beim Speichern aktualisiert) |
| `docker/configs/` | `/app/configs/` | Zusätzliche Config-Dateien zum Laden |

### Runtime Config laden (Menü)
**Datei → Konfiguration laden...** öffnet einen Datei-Dialog:
- In Docker: Startet in `/app/configs` (= `docker/configs/` auf Host)
- Lokal: Startet im aktuellen Verzeichnis

```
docker/
├── config/
│   └── config.properties    # Aktive Konfiguration
└── configs/
    ├── dev.properties       # Entwicklung
    ├── test.properties      # Test
    └── prod.properties      # Produktion
```

## Test-Konfiguration (AssertJ Swing + Java 23)

### JVM-Argumente in pom.xml
```xml
<argLine>
    --add-opens java.base/java.util=ALL-UNNAMED
    --add-opens java.base/java.lang=ALL-UNNAMED
    --add-opens java.desktop/javax.swing=ALL-UNNAMED
    ...
</argLine>
```

### Robuste Menü-Tests
```java
private JMenuItemFixture clickMenu(String... path) {
    robot().waitForIdle();
    Pause.pause(300);
    window.focus();
    robot().waitForIdle();
    robot().settings().delayBetweenEvents(150);
    return window.menuItemWithPath(path).click();
}
```

## Git-Historie (letzte Commits)
```
2040665 Add Docker support with PostgreSQL
ba3ba96 Add SplitPane layout and table tree to DatabaseView
d2d70d6 Use custom PNG icons instead of UIManager icons
094bff5 Add keyboard shortcuts, icons, menu groups, and split pane persistence
```

## Docker-Konfiguration

Die Anwendung kann in Docker-Containern ausgeführt werden:

### Struktur
```
docker/
├── init-db.sql         # Datenbank-Initialisierung (Tabellen, Beispieldaten)
├── start-windows.bat   # Windows Starter (mit VcXsrv X11)
├── start-linux.sh      # Linux Starter (mit xhost)
├── stop.bat            # Container stoppen
└── README.md           # Vollständige Dokumentation

Dockerfile              # Multi-Stage Build (Maven → JRE)
docker-compose.yml      # PostgreSQL + Java-App Orchestrierung
```

### Container
- **postgres** (PostgreSQL 16 Alpine): Port 5432, DB `standardmdi`
- **app** (Java 23 Swing): X11-Forwarding für GUI via VcXsrv

### Verbindung zur Docker-Datenbank
**Aus der Docker-App (DatabaseView):**
| Feld | Wert |
|------|------|
| JDBC URL | `jdbc:postgresql://postgres:5432/standardmdi` |
| Host | `postgres` (Container-Name, nicht localhost!) |
| Port | `5432` |
| Database | `standardmdi` |
| User | `postgres` |
| Password | `postgres` |

**Von außen (z.B. DBeaver, lokale App):**
- Host: `localhost`, Port: `5432`

### Beispieldaten (init-db.sql)
- `customers` - Kundenstammdaten
- `orders` - Bestellungen
- `products` - Produkte
- `v_order_summary` - View für Bestellübersicht

### Schnellstart (getestet)
```batch
# 1. Docker Desktop starten
# 2. XLaunch starten mit "Disable access control" ☑️
# 3. Container starten:
docker-compose -f E:/Projekte/ClaudeCode/StandardMDIGUI/docker-compose.yml up -d

# Logs anzeigen:
docker-compose -f E:/Projekte/ClaudeCode/StandardMDIGUI/docker-compose.yml logs -f app

# Container stoppen:
docker-compose -f E:/Projekte/ClaudeCode/StandardMDIGUI/docker-compose.yml down
```

## Assembly/Distribution

Maven Assembly Plugin erstellt ZIP-Distribution:
```
target/StandardMDIGUI-1.0-SNAPSHOT-distribution.zip
├── StandardMDIGUI-1.0-SNAPSHOT.jar   # Main-JAR (im Root)
├── lib/                              # Dependencies
├── start.bat                         # Windows Starter
└── start.sh                          # Linux Starter
```

## Lokale Umgebung
- **Maven:** `E:\Projekte\Tools\apache-maven-3.9.12` (im PATH, `mvn` direkt aufrufbar)
- **MAVEN_HOME:** `E:\Projekte\Tools\apache-maven-3.9.12`
- **PostgreSQL 16:** Port 5432
- **Java:** 25
- **Docker Desktop:** 4.55.0 (installiert via winget)
- **VcXsrv:** 21.1.16.1 (X Server für Docker GUI, installiert via winget)
- **WSL2:** Installiert (Neustart erforderlich für Aktivierung)

### Docker starten (nach Neustart)
1. **Docker Desktop** aus Startmenü starten
2. **XLaunch** (VcXsrv) starten mit "Disable access control" ☑️
3. `cd E:\Projekte\ClaudeCode\StandardMDIGUI\docker && start-windows.bat`

## TODO / Nächste Schritte
- [x] ~~**Docker testen**~~ - Erfolgreich getestet (26.12.2025)
- [x] ~~**Spalten-Info beim Aufklappen**~~ - Lazy Loading mit PK-Markierung (26.12.2025)
- [x] ~~**Config-Persistenz in Docker**~~ - Volume + Umgebungsvariable (26.12.2025)
- [x] ~~**Config-Pfad als Parameter**~~ - Kommandozeile, System Property, Env (26.12.2025)
- [x] ~~**Config-Wechsel mit GUI-Reload**~~ - Alle Einstellungen werden aktualisiert (26.12.2025)
- [x] ~~**Dynamischer Config-Speicherpfad**~~ - Speichert in aktuell selektierte Config (26.12.2025)
- [ ] DatabaseView: SQL-History/Favoriten
- [ ] DatabaseView: Export-Funktionalität (CSV, Excel)
- [ ] TreeView-Struktur verbessern
- [ ] Weitere Views nach Bedarf

## Prompt zum Fortsetzen
```
Ich arbeite am Java-Projekt StandardMDIGUI unter E:\Projekte\ClaudeCode\StandardMDIGUI.
Bitte lies die Datei CLAUDE_CONTEXT.md für den Kontext unserer letzten Sitzung.
```
