# DayFlow

<h3 align="center">Plan. Train. Flow.</h3>

<p align="center">
  A simple Android workout planner and timer for organizing exercises, sets, reps, rest periods, and workout schedules.
</p>

---

## 📱 About

**DayFlow** is an Android workout and fitness timer application built to make planning and completing workouts easier.

The app organizes workouts around **dates**, allowing users to create exercises for future workout days while keeping completed and past workouts organized.

Each exercise can have its own:

- Number of sets
- Number of reps
- Exercise countdown
- Rest period
- Completion status

DayFlow also provides a workout timer with pause, resume, and reset functionality, allowing users to focus on completing their workout without manually managing timers.

---

## ✨ Features

### 🏋️ Workout Management

- Create exercises for a workout
- Set the number of sets and repetitions
- Configure exercise countdown timers
- Configure rest periods
- Edit exercise information
- Delete exercises
- Track exercise completion

### ⏱️ Workout Timer

- Exercise countdown timer
- Rest timer
- Start workout
- Pause workout
- Resume workout
- Reset workout
- Automatic exercise completion tracking
- Visual completion state for finished exercises
- Audio notification when timers finish

### 📅 Date-Based Workout Planning

DayFlow uses a date-based workout timeline.

- View today's workout
- Scroll upward to view previous workouts
- Scroll downward to view future workouts
- Create workout plans for future dates
- Today is highlighted in the timeline
- Past workouts become read-only
- Future workouts remain editable
- The app automatically ensures that the current day exists

### 💾 Local Data Persistence

Workout data is stored locally on the device using JSON.

This means workouts remain available after closing and reopening the application without requiring an online account or server.

---

## 🖥️ Application Flow

```text
                         DayFlow
                            │
                            ▼
                    Date-Based Timeline
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
      Past Days          Today           Future Days
     Read-Only          Editable           Editable
                            │
                            ▼
                         Workout
                            │
                            ▼
                       Exercises
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
           Sets            Reps          Timer
                                           │
                                    ┌──────┴──────┐
                                    ▼             ▼
                                Exercise        Rest
                                  Timer          Timer
