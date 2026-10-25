# — Student Information Application

Android application built for student information management.

## Features
- Student form: Name, 15-digit Enrolment No., Date of Birth, 10-digit Contact No., Course and Semester.
- BCA dynamically offers semesters 1–6; MCA offers semesters 1–4.
- Validates names, enrolment number, past date of birth, contact number, course and semester.
- SQLite storage with Insert, Read/View, Update and Delete operations.
- Student records displayed in a ListView; tapping a record loads it for updating/deleting.
- Duplicate enrolment numbers are prevented by a UNIQUE database constraint.
- Delete confirmation dialog and clear form button.
- All records persist locally on the device; no internet permission is required.

## Open in Android Studio
1. Extract the ZIP.
2. Android Studio → **Open** → select the `StudentInformationPractical7` folder.
3. Use JDK 17 and let Gradle sync. Android Gradle Plugin is 8.7.3, Kotlin plugin 2.0.21, compile SDK 35.
4. Run on an emulator or Android phone.

## Notes
- The project uses Kotlin and XML layouts with Android's built-in SQLiteOpenHelper.
- The first build requires an internet connection to download Gradle/Android plugin dependencies.
- If Android Studio reports that SDK 35 is missing, install Android SDK Platform 35 from SDK Manager.
