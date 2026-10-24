package com.example.studentinformation

data class Student(
    val id: Long,
    val name: String,
    val enrollment: String,
    val dob: String,
    val contact: String,
    val course: String,
    val semester: String
) {
    override fun toString(): String =
        "$name\n$enrollment  •  $course / $semester\nDOB: $dob  •  Contact: $contact"
}
