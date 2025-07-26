## 📱 LifeGrow – Android Productivity App

LifeGrow is a personal productivity app I developed as part of my MCA final year project. It combines various productivity techniques into one Android application to help users manage tasks, focus better, and avoid distractions.
<video src="https://github.com/user-attachments/assets/77a4a85f-bf0c-4903-831b-149c97a04cd2.mp4" width="400" controls></video>


 

---

## 🧩 Modules and What I Implemented

### 🔐 Authentication  
--> Firebase Authentication for login and signup  
--> Basic form validation and error handling  

<img src="https://github.com/user-attachments/assets/24472504-9bf1-4c0d-9067-644a54371331" width="250"/>
<img src="https://github.com/user-attachments/assets/ef6a15f0-e91e-42e1-bbc2-91d2181f19a3" width="250"/>

---

### ⏱️ Pomodoro Timer  
--> 25/5 timer cycle with break intervals  
--> Sound alerts using mp3 from raw folder  
--> Tracks sessions using SharedPreferences  

<img src="https://github.com/user-attachments/assets/7f23f365-ce3f-4ddb-b8c6-b00251b4dc16" width="250"/>
<img src="https://github.com/user-attachments/assets/766651eb-7bc0-423d-8b6c-9ac441cc1a3c" width="250"/>

---

### 🧠 Eisenhower Matrix  
--> Task prioritization into 4 categories  
--> Stored in Firestore with real-time updates  
--> Custom layout using CardView and ConstraintLayout  

<img src="https://github.com/user-attachments/assets/3f511208-6a35-4eab-a5ea-e74a3c59958b" width="250"/>
<img src="https://github.com/user-attachments/assets/d64aff94-e110-4463-b20c-e21357381680" width="250"/>

---

### 📋 Kanban Board  
--> Organizes tasks into To Do, In Progress, Done  
--> Drag-and-drop using RecyclerView and ItemTouchHelper  
--> Real-time sync with Firestore  

<img src="https://github.com/user-attachments/assets/2411cdc2-cc4a-40d1-9a08-5826cb0d383b" width="250"/>
<img src="https://github.com/user-attachments/assets/85afc3bf-97da-4257-8efc-22ec57aaed9a" width="250"/>

---

### 📆 Time Blocking Calendar  
--> Scrollable horizontal timeline with color-coded tasks  
--> Tasks include start and end time  
--> Data stored and fetched from Firestore  

<img src="https://github.com/user-attachments/assets/b941742a-818a-426d-a5bc-39ae5dda8083" width="250"/>
<img src="https://github.com/user-attachments/assets/a4e56332-2cc9-4cf9-acad-64735231eb53" width="250"/>

---

### 🚫 App Blocker  
--> Detects foreground apps using AccessibilityService  
--> Tracks app usage time with UsageStatsManager  
--> Blocks apps with overlay after time limit is reached  

<img src="https://github.com/user-attachments/assets/2bbc8152-a83c-4720-877e-ffeb6fb3e83d" width="250"/>
<img src="https://github.com/user-attachments/assets/1c37c496-7825-4008-94aa-38e5742673e9" width="250"/>

---

### 🤖 AI Task Parser  
--> Accepts natural language like “Meeting at 6 PM tomorrow in personal”  
--> Extracts task title, time, category  
--> Prefills task creation screen automatically  

<img src="https://github.com/user-attachments/assets/34941fc0-9387-4456-9501-30ccf61e716c" width="250"/>
<img src="https://github.com/user-attachments/assets/a03958cb-ce73-48b0-a44a-6a1ee15d3c60" width="250"/>

---

### 📊 Reports and Analytics  
--> Visual reports for task completion rate, Pomodoro usage, etc.  
--> Goal streak tracking and Eisenhower breakdown  
--> All data fetched from Firestore  

<img src="https://github.com/user-attachments/assets/c02726aa-e92a-4517-a0bd-18e886aaec31" width="250"/>
<img src="https://github.com/user-attachments/assets/9d9fb3c0-ac20-49ee-b88a-75f547ea9411" width="250"/>
