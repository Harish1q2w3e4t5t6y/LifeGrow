LifeGrow – Android Productivity App

LifeGrow is a personal productivity app I built as part of my MCA final year project. The idea was to bring together different productivity techniques into one Android application so users can manage tasks, focus better, and avoid distractions.

Modules and What I Implemented
 

1. Authentication
- Firebase Authentication for login and signup
- Basic form validation and error handling
<img src="https://github.com/user-attachments/assets/ef6a15f0-e91e-42e1-bbc2-91d2181f19a3" width="100"/>


2. Pomodoro Timer
- 25/5 timer cycle
- Sound alerts using mp3 in raw folder
- Tracks session count using SharedPreferences
<img src="https://github.com/user-attachments/assets/7f23f365-ce3f-4ddb-b8c6-b00251b4dc16" width="100"/>
<img src="https://github.com/user-attachments/assets/766651eb-7bc0-423d-8b6c-9ac441cc1a3c" width="100"/>
 
 
3. Eisenhower Matrix
- Tasks split into 4 categories: urgent/important, not urgent, etc.
- Data saved and loaded from Firestore
- Custom layout using CardView and ConstraintLayout
<img src="https://github.com/user-attachments/assets/3f511208-6a35-4eab-a5ea-e74a3c59958b" width="100"/>
<img src="https://github.com/user-attachments/assets/d64aff94-e110-4463-b20c-e21357381680" width="100"/>
 
4. Kanban Board
- Tasks organized in To Do, In Progress, and Done
- Drag and drop using RecyclerView and ItemTouchHelper
- Real-time update with Firestore
<img src="https://github.com/user-attachments/assets/2411cdc2-cc4a-40d1-9a08-5826cb0d383b" width="100"/>
<img src="https://github.com/user-attachments/assets/85afc3bf-97da-4257-8efc-22ec57aaed9a" width="100"/>
 
5. Calendar
- Scrollable horizontal timeline for blocking time slots
- Each task has start and end time with color code
- Data saved in Firestore
<img src="https://github.com/user-attachments/assets/b941742a-818a-426d-a5bc-39ae5dda8083" width="100"/>
<img src="https://github.com/user-attachments/assets/a4e56332-2cc9-4cf9-acad-64735231eb53" width="100"/>
 
6. App Blocker
- Used AccessibilityService to detect foreground app
- UsageStatsManager for checking daily usage
- Shows overlay if limit crossed
- Rechecks every few seconds using handler
<img src="https://github.com/user-attachments/assets/2bbc8152-a83c-4720-877e-ffeb6fb3e83d" width="100"/>
<img src="https://github.com/user-attachments/assets/1c37c496-7825-4008-94aa-38e5742673e9" width="100"/>
 
7. AI Task Parser
- Takes plain text like “Call at 3 PM tomorrow in personal”
- Extracts task name, time, and category using rule-based logic
- Prefills the AddTask screen
<img src="(https://github.com/user-attachments/assets/ef6a15f0-e91e-42e1-bbc2-91d2181f19a3" width="100"/>

8. Reports
- Custom charts for task completion rate, pomodoro usage, etc.
- Breakdown of task types and focus streaks
- All data comes from Firestore
 <img src="(https://github.com/user-attachments/assets/ef6a15f0-e91e-42e1-bbc2-91d2181f19a3" width="100"/>


