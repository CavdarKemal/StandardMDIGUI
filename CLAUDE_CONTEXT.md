# Claude Code Kontext - StandardMDIGUI

## Projekt-Übersicht
**Pfad:** `E:\Projekte\ClaudeCode\StandardMDIGUI`
**Typ:** Java Swing MDI-Anwendung (Multi-Document Interface)
**Build:** Maven, Java 24
**IDE:** IntelliJ IDEA
**Ziel:** Modulares, erweiterbares Gerüst für zukünftige Programme

## Letzte Session (26.12.2025 Abend)

### Zusammenfassung der heutigen Änderungen

1. **Flaky Tests gefixt** - JUnit Vintage Engine + robuste `clickMenu()` Helper-Methode
2. **4 neue Features** - Keyboard Shortcuts, Icons, Menu Groups, SplitPane Persistence
3. **Custom Icons** - Eigene PNG-Icons statt UIManager-Icons
4. **DatabaseView erweitert** - SplitPane-Layout mit Tabellen-Tree

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

## Test-Konfiguration (AssertJ Swing + Java 24)

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
ba3ba96 Add SplitPane layout and table tree to DatabaseView
d2d70d6 Use custom PNG icons instead of UIManager icons
094bff5 Add keyboard shortcuts, icons, menu groups, and split pane persistence
e32f83b Fix flaky menu tests in MainFrameTest
```

## Lokale Umgebung
- **Maven:** `C:\Tools\maven\apache-maven-3.9.12` (im PATH, `mvn` direkt aufrufbar)
- **MAVEN_HOME:** `C:\Tools\maven\apache-maven-3.9.12`
- **PostgreSQL 16:** Port 5432
- **Java:** 25

## TODO / Nächste Schritte
- [ ] DatabaseView: Spalten-Info beim Aufklappen einer Tabelle anzeigen
- [ ] DatabaseView: SQL-History/Favoriten
- [ ] DatabaseView: Export-Funktionalität (CSV, Excel)
- [ ] TreeView-Struktur verbessern
- [ ] Weitere Views nach Bedarf

## Prompt zum Fortsetzen
```
Ich arbeite am Java-Projekt StandardMDIGUI unter E:\Projekte\ClaudeCode\StandardMDIGUI.
Bitte lies die Datei CLAUDE_CONTEXT.md für den Kontext unserer letzten Sitzung.
```
