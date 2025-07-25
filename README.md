LifeGrow – Android Productivity App

LifeGrow is a personal productivity app I built as part of my MCA final year project. The idea was to bring together different productivity techniques into one Android application so users can manage tasks, focus better, and avoid distractions.

Modules and What I Implemented
 

1. Authentication
- Firebase Authentication for login and signup
- Basic form validation and error handling
 <h3>Pomodoro Timer Output</h3>
<img src="https://github.com/user-attachments/assets/72d87743-e716-46cd-af49-55a3a7b68e19" width="100"/>


2. Pomodoro Timer
- 25/5 timer cycle
- Sound alerts using mp3 in raw folder
- Tracks session count using SharedPreferences

3. Eisenhower Matrix
- Tasks split into 4 categories: urgent/important, not urgent, etc.
- Data saved and loaded from Firestore
- Custom layout using CardView and ConstraintLayout

4. Kanban Board
- Tasks organized in To Do, In Progress, and Done
- Drag and drop using RecyclerView and ItemTouchHelper
- Real-time update with Firestore

5. Calendar
- Scrollable horizontal timeline for blocking time slots
- Each task has start and end time with color code
- Data saved in Firestore

6. App Blocker
- Used AccessibilityService to detect foreground app
- UsageStatsManager for checking daily usage
- Shows overlay if limit crossed
- Rechecks every few seconds using handler

7. AI Task Parser
- Takes plain text like “Call at 3 PM tomorrow in personal”
- Extracts task name, time, and category using rule-based logic
- Prefills the AddTask screen

8. Reports
- Custom charts for task completion rate, pomodoro usage, etc.
- Breakdown of task types and focus streaks
- All data comes from Firestore


