# Claude Code Kontext - StandardMDIGUI

## Projekt-Übersicht
**Pfad:** `E:\Projekte\ClaudeCode\StandardMDIGUI`
**Typ:** Java Swing MDI-Anwendung (Multi-Document Interface)
**Build:** Maven, Java 24
**IDE:** IntelliJ IDEA
**Ziel:** Modulares, erweiterbares Gerüst für zukünftige Programme

## Durchgeführte Änderungen (25.12.2025)

### 1. Modulare Panel-Architektur
- **EmbeddablePanel:** Abstrakte Basisklasse für einbettbare Panels
- **SettingsPanel:** Einstellungs-Controls (aus MainFrame extrahiert)
  - Labels links neben ComboBoxen (nicht darüber)
  - Checkboxen in zwei Spalten (GridLayout)
  - DB-Verbindung, Test Sources, Test Types, ITSQ Revisions
- **TreePanel:** Customer-Tree (aus MainFrame extrahiert)
- **DesktopPanel:** MDI-Desktop mit View-Management und Layout-Funktionen

### 2. Dynamisches View-Registrierungssystem
- **ViewInfo Interface:** Metadaten für automatische Menü/Toolbar-Erstellung
  - `getMenuLabel()` - Pflicht: Label im Menü
  - `getToolbarLabel()` - Optional: Toolbar-Button (null = kein Button)
  - `getIcon()` - Optional: Icon für Menü/Toolbar
  - `getKeyboardShortcut()` - Optional: Tastenkürzel
  - `getToolbarTooltip()` - Optional: Tooltip
- **BaseView:** Implementiert ViewInfo mit Default-Werten
- **MainFrame.registerView(Supplier<BaseView>):** Registriert Views dynamisch
  - Liest ViewInfo aus temporärer Instanz
  - Erstellt automatisch Menüeintrag und optional Toolbar-Button

### 3. Neue Views
- **ProzessView:** Log-Ausgabe mit Start/Clear Buttons, Toolbar-Button
- **AnalyseView:** Tabelle mit Ergebnissen, Export-Funktion, Toolbar-Button

### 4. MainFrame Umbau
- JSplitPane-Struktur (horizontal + vertikal)
- Dynamische View-Registrierung statt hartcodierter Menüeinträge
- ViewRegistration Record für interne Verwaltung

### 5. Bug-Fixes
- Template Method Pattern Fix: `cfg` wird in `initializePanel()` initialisiert
- LAST_TEST_TYPE Speicherung für Test Types ComboBox hinzugefügt

### 6. Git neu eingerichtet
- Repository neu initialisiert (User: Kemal Cavdar)
- .gitignore korrigiert (target/, logs/, .idea/, *.iml, config.properties)
- Initial Commit erstellt

## Projekt-Struktur
```
src/main/java/de/cavdar/
├── MainFrame.java          (JSplitPane-Layout, registerView())
├── EmbeddablePanel.java    (Abstract, für einbettbare Panels)
├── SettingsPanel.java      (Einstellungen, ConnectionListener)
├── TreePanel.java          (Customer-Tree)
├── DesktopPanel.java       (MDI-Desktop, Layout-Funktionen)
├── ViewInfo.java           (Interface für View-Metadaten)
├── BaseView.java           (Abstract, implementiert ViewInfo)
├── SampleView.java         (Beispiel-View)
├── ProzessView.java        (Prozess-View mit Log)
├── AnalyseView.java        (Analyse-View mit Tabelle)
├── DatabaseView.java       (JDBC-View)
├── AppConfig.java          (Singleton, Thread-safe)
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
├── DatabaseIntegrationTest.java
├── MainFrameTest.java
├── SampleViewTest.java
└── util/
    └── ConfigEntryTest.java
```

## Architektur-Übersicht

### Erweiterungspunkte
| Komponente | Erweiterung | Beschreibung |
|------------|-------------|--------------|
| **Neue View** | `extends BaseView` | `registerView()` in main() aufrufen |
| **Neues Panel** | `extends EmbeddablePanel` | In MainFrame einbinden |
| **Konfiguration** | `AppConfig.getInstance()` | Properties lesen/schreiben |
| **DB-Verbindungen** | `ConnectionManager` | Listener-Pattern für Änderungen |

### Neue View hinzufügen (Beispiel)
```java
public class MeineView extends BaseView {
    public MeineView() {
        super("Meine View");
        // Content hier
    }

    @Override
    public String getToolbarLabel() {
        return "Meine";  // null = kein Toolbar-Button
    }

    @Override
    protected void setupViewToolbar(JToolBar tb) {
        tb.add(new JButton("Action"));
    }
}

// In MainFrame.main():
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
- **Initial Commit:** 25.12.2025

## Lokale Umgebung
- **PostgreSQL 16:** Port 5432, JDBC: `jdbc:postgresql://localhost:5432/postgres`
- **Maven:** Nicht im PATH (Build über IntelliJ)

## Offene Punkte / Mögliche Erweiterungen
- [ ] Keyboard Shortcuts für Views implementieren
- [ ] Icons für Menü/Toolbar
- [ ] View-Gruppen im Menü (getMenuGroup())
- [ ] Persistierung der SplitPane-Divider-Positionen
- [ ] Weitere Views nach Bedarf

## Prompt zum Fortsetzen
```
Ich arbeite am Java-Projekt StandardMDIGUI unter E:\Projekte\ClaudeCode\StandardMDIGUI.
Bitte lies die Datei CLAUDE_CONTEXT.md für den Kontext unserer letzten Sitzung.
```
