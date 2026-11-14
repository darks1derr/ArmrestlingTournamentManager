# Armwrestling Tournament Management System
### Group: SE-2512
### Members: Darkhan Daurambekov, Assylzhan Abishev, Magzhan Assankhan

A Java application for running single-elimination armwrestling tournaments. Organizers can register athletes, draw brackets by weight class, record or undo match results, and view a report with the champion of each completed division. The application has a Swing desktop interface and a console mode. Tournament data is kept in memory for the current session.

Athletes enter the lightest division that accepts their weight. An optional open division takes athletes above the highest weight limit. If a bracket has empty slots, some athletes advance through a first-round bye. A division needs at least two athletes to award a champion.

## Run the application

Extract the entire ZIP and open the project folder. With Java 17 or later installed, double-click `Start.cmd` on Windows or run the bundled JAR from a terminal.

```sh
java -jar app/ArmwrestlingTournament.jar
```

To build a portable Windows version, run `Build-Windows-EXE.cmd` on Windows with JDK 17 or later and `jpackage` on your PATH. Open `dist/ArmwrestlingTournament/ArmwrestlingTournament.exe`. To share that version, send the entire output folder so the executable can find its bundled Java runtime.

## Desktop demo

1. Open **Bracket**. The app starts with four demo athletes.
2. Choose **Random draw** and click **Preview**. Switch to **Rating seeding** and click **Start tournament**.
3. In semifinal **M1**, click **Record winner** and select **Miras**. Click **Undo last result** to clear his spot in the final.
4. Record **Arman** as the winner of **M1** and **Dias** as the winner of **M2**.
5. Record **Arman** as the winner of final **M3**, then open **Report** to see the champion.
6. To show how the **Builder** works, choose **New tournament** and set a name and weight limits such as `70, 80, 90`.

## Console demo

```sh
java -jar app/ArmwrestlingTournament.jar --console
```

At the first prompt, enter `2` to load the demo roster. Enter each number separately and press Enter. Use match numbers and athlete IDs without the `M` or `#` prefix.

| Menu inputs | Expected result |
|---|---|
| `3` → `2` | Preview a random draw |
| `4` → `1` | Start with rating seeding. M1 pairs #1 with #4, and M2 pairs #2 with #3 |
| `6` → `1` → `4` | Record athlete #4 as the winner of M1 |
| `7` | Undo the M1 result |
| `6` → `1` → `1` | Record athlete #1 as the winner of M1 |
| `6` → `2` → `2` | Record athlete #2 as the winner of M2 |
| `6` → `3` → `1` | Record athlete #1 as the winner of M3 |
| `8` | Show the tournament report with Arman as champion |

A preview leaves registration open. Starting a random draw generates fresh pairings. Undo reverses the most recent successful result, including a final.

## Build from source

Run these commands from the project folder in PowerShell with JDK 17 or later.

```powershell
New-Item -ItemType Directory -Force build/classes, app | Out-Null
$sources = (Get-ChildItem src/tournament -Filter *.java).FullName
javac --release 17 -encoding UTF-8 -d build/classes $sources
if ($LASTEXITCODE -eq 0) {
    jar --create --file app/ArmwrestlingTournament.jar --main-class tournament.DesktopMain -C build/classes .
}
```

## Code overview

- `DesktopMain` opens the desktop window. `Main` runs the console menu.
- `TournamentDashboard` handles the desktop controls. `DashboardStyle` handles the visual styling.
- `TournamentFacade` connects both interfaces to the tournament logic through methods such as `register`, `start`, and `recordResult`.
- `Tournament` manages registration and divisions. `Division` builds the bracket. `Match` uses winners from earlier matches to fill later rounds.

## Team and design patterns

**Group SE-2512**

The table shows the planned responsibilities for the project. The classes are in `src/tournament/`.

| Team member | Pattern | Classes | Role |
|---|---|---|---|
| Darkhan Daurambekov | Builder | `TournamentConfig.Builder` | Builds a tournament configuration with a name and weight divisions |
| Darkhan Daurambekov | Strategy | `DrawStrategy`, `RatingDraw`, `RandomDraw` | Switches between rating-based seeding and a random draw |
| Assylzhan Abishev | Command | `ResultCommand`, `RecordWinnerCommand`, `ResultHistory` | Records a winner and supports undo through command history |
| Assylzhan Abishev | Observer | `ResultHistory.Observer`, subscribers in `TournamentFacade` and `TournamentDashboard` | Notifies the interface after a result is recorded or undone so it can refresh the bracket |
| Magzhan Assankhan | Facade | `TournamentFacade` | Gives both interfaces a common way to register athletes, start a tournament, and manage results |
| Magzhan Assankhan | Visitor | `TournamentVisitor`, `ReportVisitor`, and the `accept` methods | Visits the tournament, divisions, and matches to generate the report |
