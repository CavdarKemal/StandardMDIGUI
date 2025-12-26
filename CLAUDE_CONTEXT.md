# Claude Code Kontext - StandardMDIGUI

## Projekt-Übersicht
**Pfad:** `E:\Projekte\ClaudeCode\StandardMDIGUI`
**Typ:** Java Swing MDI-Anwendung (Multi-Document Interface)
**Build:** Maven, Java 24
**IDE:** IntelliJ IDEA
**Ziel:** Modulares, erweiterbares Gerüst für zukünftige Programme

## Letzte Session (26.12.2025 Nachmittag)

### Package-Reorganisation durchgeführt
Die Klassen wurden in logische Packages aufgeteilt:
- `de.cavdar.design` - GUI-Panel-Klassen (für GUI-Designer geeignet)
- `de.cavdar.view` - Logik-Klassen (View-Controller)
- `de.cavdar.frame` - MainFrame
- `de.cavdar.model` - Datenmodelle (AppConfig, ConnectionInfo, ConfigEntry)
- `de.cavdar.util` - Utilities (ConnectionManager)
- `de.cavdar.exception` - Exceptions

### Panel/View Separation Pattern
Jede View besteht aus zwei Klassen:
1. **Panel-Klasse** (`design` Package): Nur GUI-Komponenten, keine Logik
   - Kann von GUI-Designer generiert werden
   - Alle Komponenten als `protected` Fields
   - Getter für View-Zugriff
   - `setName()` für alle testbaren Komponenten (AssertJ Swing)

2. **View-Klasse** (`view` Package): Nur Logik und Event-Handler
   - Erstellt Panel via `createPanel()`
   - Registriert Listener in `setupToolbarActions()` und `setupListeners()`

### Wichtige Änderungen für Tests (AssertJ Swing + Java 24)

#### 1. JVM-Argumente in pom.xml (maven-surefire-plugin)
```xml
<argLine>
    --add-opens java.base/java.util=ALL-UNNAMED
    --add-opens java.base/java.lang=ALL-UNNAMED
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED
    --add-opens java.desktop/java.awt=ALL-UNNAMED
    --add-opens java.desktop/javax.swing=ALL-UNNAMED
    --add-opens java.desktop/sun.awt=ALL-UNNAMED
    --add-opens java.desktop/java.awt.event=ALL-UNNAMED
</argLine>
```

#### 2. Komponenten-Namen für Tests
Alle GUI-Komponenten brauchen `setName()` für AssertJ Swing:
```java
btnStart = new JButton("Start Prozess");
btnStart.setName("Start Prozess");  // Wichtig für Tests!
```

#### 3. Menü-Tests mit menuItemWithPath
```java
// Falsch (findet JMenu statt JMenuItem):
window.menuItem("Datei").click();

// Richtig:
window.menuItemWithPath("Datei", "Analyse").click();
```

### Gruppierte config.properties
AppConfig.save() schreibt Properties gruppiert mit Kommentaren:
- WINDOW, LATEST, FLAGS, CUSTOMERS, URL, DATABASE, CONFIG, MISC
- `PROPERTY_GROUPS` ist static final Map mit statischer Initialisierung

### Neue Features implementiert (26.12.2025)

#### 1. Keyboard Shortcuts für Views
Jede View hat nun einen Tastaturkürzel:
| View | Shortcut |
|------|----------|
| SampleView (Kunden Analyse) | Ctrl+1 |
| ProzessView | Ctrl+2 |
| AnalyseView | Ctrl+3 |
| TreeView | Ctrl+4 |
| CustomerTreeView | Ctrl+5 |

#### 2. Icons für Menü/Toolbar
Views verwenden UIManager-Icons:
- SampleView: `FileView.fileIcon`
- ProzessView: `FileChooser.detailsViewIcon`
- AnalyseView: `Table.ascendingSortIcon`
- TreeView: `Tree.openIcon`
- CustomerTreeView: `FileView.directoryIcon`

#### 3. View-Gruppen im Menü
Das Datei-Menü hat nun Submenüs:
- **Analyse**: Kunden Analyse, Analyse
- **Verwaltung**: Prozess, Kunden Explorer
- **Navigation**: Tree View

#### 4. SplitPane Divider Persistence
Divider-Positionen werden in `config.properties` gespeichert:
- `LAST_LEFT_SPLIT_DIVIDER` - Vertikaler Split (Settings/Tree)
- `LAST_MAIN_SPLIT_DIVIDER` - Horizontaler Split (Left/Desktop)

### Gelöste Test-Probleme (26.12.2025)
Die flaky Menü-Tests wurden gefixt durch:
1. **JUnit Vintage Engine** zu pom.xml hinzugefügt (für JUnit 4 Tests mit AssertJ-Swing)
2. **Helper-Methode `clickMenu()`** mit robustem Timing:
   - `window.focus()` vor Menü-Interaktion
   - `Pause.pause()` zwischen Events
   - Erhöhter `delayBetweenEvents` während Menü-Interaktionen
3. **Zusätzliche Pausen** nach Button-Clicks bevor Menü geöffnet wird

## Projekt-Struktur (aktuell)
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
│   ├── DatabaseViewPanel.java
│   ├── TreeViewPanel.java     # Basis für Tree-Views
│   └── CustomerTreeViewPanel.java
│
├── view/                      # View-Logik
│   ├── ViewInfo.java          # Interface für Metadaten
│   ├── BaseView.java          # Abstract, implementiert ViewInfo
│   ├── SampleView.java
│   ├── ProzessView.java
│   ├── AnalyseView.java
│   ├── DatabaseView.java
│   ├── TreeView.java          # Basis für Tree-Views
│   └── CustomerTreeView.java
│
├── frame/
│   └── MainFrame.java         # Hauptfenster
│
├── model/
│   ├── AppConfig.java         # Singleton, gruppiertes Speichern
│   ├── ConnectionInfo.java    # DB-Verbindungsdaten
│   └── ConfigEntry.java       # Record
│
├── util/
│   └── ConnectionManager.java # Zentrales Connection Management
│
└── exception/
    ├── ConfigurationException.java
    └── ViewException.java

src/test/java/de/cavdar/
├── AppConfigTest.java         # Mit StandardCopyOption.REPLACE_EXISTING
├── DatabaseViewTest.java      # Verwendet benannte Komponenten
├── DatabaseIntegrationTest.java
├── MainFrameTest.java         # 2 flaky Menü-Tests
├── SampleViewTest.java
└── util/
    └── ConfigEntryTest.java

docs/
└── klassendiagramm.puml       # PlantUML mit Package-Struktur
```

## Architektur-Übersicht

### Vererbungshierarchie
```
JPanel
└── EmbeddablePanel (abstract)
    ├── SettingsPanel
    ├── TreePanel
    └── DesktopPanel

JPanel
└── BaseViewPanel
    ├── SampleViewPanel
    ├── ProzessViewPanel
    ├── AnalyseViewPanel
    ├── DatabaseViewPanel
    └── TreeViewPanel
        └── CustomerTreeViewPanel

JInternalFrame
└── BaseView (abstract, implements ViewInfo)
    ├── SampleView
    ├── ProzessView
    ├── AnalyseView
    ├── DatabaseView
    └── TreeView
        └── CustomerTreeView
```

### Neue View hinzufügen (Pattern)
```java
// 1. Panel-Klasse (design Package)
public class MeineViewPanel extends BaseViewPanel {
    protected JButton btnAction;

    protected void initCustomComponents() {
        btnAction = new JButton("Action");
        btnAction.setName("Action");  // Für Tests!
        viewToolbar.add(btnAction);
    }

    public JButton getActionButton() { return btnAction; }
}

// 2. View-Klasse (view Package)
public class MeineView extends BaseView {
    private MeineViewPanel meinePanel;

    public MeineView() {
        super("Meine View");
    }

    @Override
    protected BaseViewPanel createPanel() {
        meinePanel = new MeineViewPanel();
        return meinePanel;
    }

    @Override
    protected void setupToolbarActions() {
        meinePanel.getActionButton().addActionListener(e -> doAction());
    }

    @Override
    public String getToolbarLabel() {
        return "Meine";  // null = kein Toolbar-Button
    }
}

// 3. In MainFrame.main() registrieren:
frame.registerView(MeineView::new);
```

### Layout-Struktur
```
MainFrame
├── MenuBar (Datei: Views dynamisch, Fenster: Layout)
├── Toolbar (Buttons für Views mit toolbarLabel)
└── JSplitPane (horizontal)
    ├── Left: JSplitPane (vertical)
    │   ├── Top: SettingsPanel
    │   └── Bottom: TreePanel
    └── Right: DesktopPanel (JDesktopPane)
```

## Git-Konfiguration
- **User:** Kemal Cavdar <kemal@cavdar.de>
- **Branch:** master

## Lokale Umgebung
- **PostgreSQL 16:** Port 5432
- **Maven:** Nicht im PATH (Build über IntelliJ oder vollständiger Pfad)
- **Java:** 24 (mit Modul-Einschränkungen für Reflection)

## TODO / Nächste Schritte
- [x] ~~2 flaky Menü-Tests in MainFrameTest fixen~~ ✓ Erledigt 26.12.2025
- [x] ~~Keyboard Shortcuts für Views~~ ✓ Ctrl+1 bis Ctrl+5
- [x] ~~Icons für Menü/Toolbar~~ ✓ UIManager-Icons
- [x] ~~View-Gruppen im Menü (getMenuGroup())~~ ✓ Submenüs
- [x] ~~Persistierung der SplitPane-Divider-Positionen~~ ✓ In config.properties
- [ ] TreeView-Struktur verbessern (User wollte dies studieren)
- [ ] DatabaseView weiter ausbauen

## Prompt zum Fortsetzen
```
Ich arbeite am Java-Projekt StandardMDIGUI unter E:\Projekte\ClaudeCode\StandardMDIGUI.
Bitte lies die Datei CLAUDE_CONTEXT.md für den Kontext unserer letzten Sitzung.
```
