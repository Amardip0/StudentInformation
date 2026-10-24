package com.example.studentinformation

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_STUDENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_ENROLLMENT TEXT NOT NULL UNIQUE,
                $COL_DOB TEXT NOT NULL,
                $COL_CONTACT TEXT NOT NULL,
                $COL_COURSE TEXT NOT NULL,
                $COL_SEMESTER TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No schema migration is needed for version 1.
        // Keep existing student records when the app is upgraded.
    }

    fun insertStudent(
        name: String, enrollment: String, dob: String, contact: String,
        course: String, semester: String
    ): Long {
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_ENROLLMENT, enrollment)
            put(COL_DOB, dob)
            put(COL_CONTACT, contact)
            put(COL_COURSE, course)
            put(COL_SEMESTER, semester)
        }
        return try {
            writableDatabase.insertOrThrow(TABLE_STUDENTS, null, values)
        } catch (_: SQLiteConstraintException) {
            -1L
        }
    }

    fun updateStudent(
        id: Long, name: String, enrollment: String, dob: String, contact: String,
        course: String, semester: String
    ): Int {
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_ENROLLMENT, enrollment)
            put(COL_DOB, dob)
            put(COL_CONTACT, contact)
            put(COL_COURSE, course)
            put(COL_SEMESTER, semester)
        }
        return try {
            writableDatabase.update(
                TABLE_STUDENTS, values, "$COL_ID = ?", arrayOf(id.toString())
            )
        } catch (_: SQLiteConstraintException) {
            -1
        }
    }

    fun deleteStudent(id: Long): Int =
        writableDatabase.delete(TABLE_STUDENTS, "$COL_ID = ?", arrayOf(id.toString()))

    fun getAllStudents(): List<Student> {
        val result = mutableListOf<Student>()
        readableDatabase.query(
            TABLE_STUDENTS,
            arrayOf(COL_ID, COL_NAME, COL_ENROLLMENT, COL_DOB, COL_CONTACT, COL_COURSE, COL_SEMESTER),
            null, null, null, null, "$COL_NAME COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    Student(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        enrollment = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENROLLMENT)),
                        dob = cursor.getString(cursor.getColumnIndexOrThrow(COL_DOB)),
                        contact = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT)),
                        course = cursor.getString(cursor.getColumnIndexOrThrow(COL_COURSE)),
                        semester = cursor.getString(cursor.getColumnIndexOrThrow(COL_SEMESTER))
                    )
                )
            }
        }
        return result
    }

    companion object {
        private const val DATABASE_NAME = "student_information.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_STUDENTS = "students"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_ENROLLMENT = "enrollment"
        private const val COL_DOB = "dob"
        private const val COL_CONTACT = "contact"
        private const val COL_COURSE = "course"
        private const val COL_SEMESTER = "semester"
    }
}
