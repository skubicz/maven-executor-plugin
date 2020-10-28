# Maven Executor IntelliJ Plugin

Maven Executor is a plugin for IntelliJ IDEA that provides new tool window with maven run parameters. 
It is an alternative for the standard Maven Project Tool Window. 

![Screenshot](window-screen.png)

## Installation
* find plugin using IDEA Brows Repository.
* install and restart IDEA.
* open Window by *View->Tool Windows->Maven Executor* (window should be visible on the right side).

## Functionality

### Toolbar Menu
* **Execute Goals For Selected Projects** - executes goals with a current parameter for selected Maven modules.
* **Execute Goals For All Projects** - always executes goals with a current parameter for all projects or project chosen from Project ComboBox.
* **Current Maven Run Settings** - opens detailed Current Maven Run Settings, more bellow.
* **Reimport Maven Projects** - standard reimport all maven projects action.
* **Generate Sources and Update Folders For All Projects** - standard maven generation sources and updating folders action.
* **Maven General Settings** - opens general Maven Settings.
* **Save Current Settings as...** - saves favorite settings, saving is possible only when Favorite List is visible.
* **Visible Properties Settings** - allows adjusting visible properties to own preferences on Tool Window.
  
### Main Configuration
* **Goals** - maven goals to execute.
* **More Parameters** - parameters which will be added to result command.
* **Offline** - enable/disable maven *--offline* flag.
* **Update Snapshots** - enable/disable *--update-snapshots* flag.
* **Threads** - number of thread to run parallel, *-T* option (currently not supported *"thread per cpu"*).
* **Profiles** - list of Maven profiles.
* **Jvm Options** - optional JVM Options, when checkbox is selected, parameters are added to main JVM parameters (from dialog window).
* **Projects ComboBox** - list of Maven Project, allows narrowing visible project in Tree of Projects.
* **Expand All** - expands all nodes in Tree of Projects.
* **Collapse All** - collapses all nodes in Tree of Projects, information about collapsed nodes is persisted.

### Tree of Projects
* Tree of checkbox representing structure of maven projects. Each node is maven module. If all nodes are selected, project is built normally.
When only some nodes are selected, project is built with *--projects* option. In both cases, order of building modules is calculated by Maven Reactor.
* **Select/Deselect Others** - actions visible after pressing right mouse button.

### Favorite List
* **Mode Button** - allows changing width of Favorite List. In addition, it is possible to hide this section via Visible Properties Settings.
* **DEFAULT** - it is a label to default settings - always available.
* **Favorite** - list of shortcuts to saved settings, every change is immediately saved.

### Selection Buttons
* **Open** - selects modules by current open files (open, not selected and visible in editor Tab).
* **Modified** - selects module by modified files since last build (list of modified files is cleared after each build).

### Current Maven Run Configuration
* Settings available from ToolBar menu contain all Maven settings with JVM Options and Environment Variables.
* **Result Command** - shows real command (in Advance Mode) which Maven will execute. There is also Simple Mode for increasing readability.
* **Always add a parent POMs modules to build** - when module is selected in the tree, parent POMs module will be added to build, even if not all the leaves are selected.