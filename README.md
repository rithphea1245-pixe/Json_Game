# 🌟 World of Wonder (Words-Of-Wonder GUI)

A feature-rich Java desktop application designed using the **MVC (Model-View-Controller)** architectural pattern. Features interactive puzzle games, word searches, water cup sorting, and world trivia quizzes.

---

## 🏛️ Project Architecture: MVC Pattern

The project strictly separates concerns into **Model**, **View**, and **Controller**:

```
                         User Interaction (Button Clicks, Inputs)
                     ┌──────────────────────────────────────────────┐
                     │                                              ▼
              ┌──────────────┐       Updates View            ┌──────────────┐
              │     VIEW     │ ◄──────────────────────────── │  CONTROLLER  │
              └──────────────┘                               └──────────────┘
                     ▲                                              │
                     │                 Reads/Writes                 ▼
                     └─────────────────────────────────────── ┌──────────────┐
                                                              │    MODEL     │
                                                              └──────────────┘
                                                                     │
                                                              Persists to JSON
                                                                     ▼
                                                                data/*.json
```

### 1. 📦 Model (`com.worldofwonder.model`)

- **Entity Classes:** `User.java`, `World.java`, `Level.java`, `Question.java`, `WowLevel.java`.
- **Repositories:**
  - `UserRepository.java`: Manages player accounts, passwords, and points in memory using `ArrayList<User>`, saving directly to `data/users.json`.
  - `GameRepository.java`: Loads worlds, difficulty levels, and quiz questions from `data/*.json` into Java Collections.

### 2. 🖥️ View (`com.worldofwonder.view`)

- **Swing GUI Screens:**
  - `MainUI.java`: Main frame holding `CardLayout` for fluid screen transitions.
  - `WelcomeScreen.java`: User registration and login interface.
  - `Dashboard.java`: Central game hub showing current user, total points, and game selection cards.
  - `QuizGameScreen.java`: Interactive multiple-choice trivia quiz.
  - `WordSearchGameScreen.java`: Grid-based word search puzzle.
  - `CupsWaterSortGameScreen.java`: Physics-based liquid color sorting game.
  - `WordsOfWondersGameScreen.java`: Circular anagram crossword puzzle.
- **Design & Utilities:** `UITheme.java` (gradients, floating particles, fonts), `UIUtil.java` (layout and responsive resizing), `SoundUtil.java` (audio synthesis).

### 3. 🎮 Controller (`com.worldofwonder.controller`)

- `AuthController.java`: Validates login/registration credentials, manages user sessions, and enforces account rules.
- `GameController.java`: Coordinates score rewards, level navigation, and leaderboard calculations.
- `QuizController.java`: Manages quiz question progression, validates player answers, and calculates score bonuses.

---

## 🎓 Academic Concepts Demonstrated (For Teacher Presentation)

1. **Object-Oriented Programming (OOP):**
   - **Encapsulation:** All data models (`User`, `Question`, `Level`) have private fields exposed through getters and setters.
   - **Inheritance:** Custom GUI components extend Swing classes (`JPanel`, `JFrame`, `JButton`).
   - **Separation of Concerns:** Zero UI code inside Models; zero database/file I/O inside Views.
2. **Java Collections Framework:**
   - Utilizes `ArrayList`, `List`, `Map`, `Set`, and `Comparator` to store, filter, and sort game data in real time.
   - Dynamic leaderboard sorting by total points descending.
3. **Exception Handling:**
   - Uses `try-catch` blocks to protect file I/O operations and JSON parsing.
   - Uses `IllegalArgumentException` to validate user inputs (e.g., duplicate usernames, weak passwords).
4. **Zero-Setup JSON Persistence:**
   - All game data lives in human-readable JSON files inside `data/`. No external database (PostgreSQL/MySQL) setup is needed!

---

## 🚀 How to Run the Game

### Option 1: 1-Click Windows Launcher

Simply double-click `run.bat` in the project root folder.

### Option 2: Command Line (Windows PowerShell)

```powershell
# 1. Compile
if (-not (Test-Path bin)) { New-Item -ItemType Directory -Path bin }
javac -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })

# 2. Run
java -cp bin com.worldofwonder.Main
```

### Option 3: Command Line (Linux / macOS)

```bash
chmod +x run.sh
./run.sh
```

---

## 🎤 Teacher Presentation Q&A Cheat Sheet

- **Q: Where is your Model, View, and Controller?**
  - _Answer:_ They are organized in `src/com/worldofwonder/`:
    - `model/` holds our data entities (`User`, `Question`, `Level`) and JSON repositories.
    - `view/` holds all Swing GUI screens and animations.
    - `controller/` holds all event handlers, validation, and game rules.
- **Q: What happens when a player submits an answer?**
  - _Answer:_ The View captures the button click and notifies `QuizController`. The Controller checks if the answer matches the `Question` model, awards points via `GameController`, saves the new score into `data/users.json`, and instructs the View to show feedback (green for correct, red for incorrect).
- **Q: How does data persistence work?**
  - _Answer:_ We built a zero-dependency JSON utility (`JsonUtil.java`) that reads and writes clean `.json` files inside the `data/` folder, ensuring players' scores and accounts persist across restarts.
