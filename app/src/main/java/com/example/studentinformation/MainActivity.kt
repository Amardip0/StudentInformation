package com.example.studentinformation

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var db: StudentDbHelper

    // Navigation & Page Views
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var layoutRecordsPage: View
    private lateinit var layoutFormPage: View
    private lateinit var layoutEmptyState: View
    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvHeaderSub: TextView

    // Search & List Views
    private lateinit var etSearch: EditText
    private lateinit var tvCount: TextView
    private lateinit var listStudents: ListView
    private lateinit var fabAddStudent: FloatingActionButton
    private lateinit var btnAddFirstStudent: MaterialButton

    // Form Views
    private lateinit var layoutEditModeBanner: View
    private lateinit var tvModeBannerText: TextView
    private lateinit var btnCancelEdit: Button
    private lateinit var tvFormTitle: TextView
    private lateinit var etName: EditText
    private lateinit var etEnrollment: EditText
    private lateinit var etDob: EditText
    private lateinit var etContact: EditText
    private lateinit var spCourse: Spinner
    private lateinit var spSemester: Spinner
    private lateinit var btnSave: MaterialButton
    private lateinit var btnUpdate: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnClear: MaterialButton

    // State & Adapters
    private var selectedStudentId: Long? = null
    private var allStudentsList: List<Student> = emptyList()
    private lateinit var studentAdapter: StudentCardAdapter
    private lateinit var courseAdapter: ArrayAdapter<String>
    private lateinit var semesterAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = StudentDbHelper(this)
        bindViews()
        setupSpinners()
        setupNavigation()
        setupListAndSearch()
        setupFormActions()
        refreshStudents()
    }

    private fun bindViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        layoutRecordsPage = findViewById(R.id.layoutRecordsPage)
        layoutFormPage = findViewById(R.id.layoutFormPage)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        tvHeaderSub = findViewById(R.id.tvHeaderSub)

        etSearch = findViewById(R.id.etSearch)
        tvCount = findViewById(R.id.tvCount)
        listStudents = findViewById(R.id.listStudents)
        fabAddStudent = findViewById(R.id.fabAddStudent)
        btnAddFirstStudent = findViewById(R.id.btnAddFirstStudent)

        layoutEditModeBanner = findViewById(R.id.layoutEditModeBanner)
        tvModeBannerText = findViewById(R.id.tvModeBannerText)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        tvFormTitle = findViewById(R.id.tvFormTitle)
        etName = findViewById(R.id.etName)
        etEnrollment = findViewById(R.id.etEnrollment)
        etDob = findViewById(R.id.etDob)
        etContact = findViewById(R.id.etContact)
        spCourse = findViewById(R.id.spCourse)
        spSemester = findViewById(R.id.spSemester)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnClear = findViewById(R.id.btnClear)

        etEnrollment.filters = arrayOf(InputFilter.LengthFilter(15))
        etContact.filters = arrayOf(InputFilter.LengthFilter(10))
    }

    private fun setupNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_records -> {
                    showRecordsPage()
                    true
                }
                R.id.nav_add -> {
                    showFormPage()
                    true
                }
                else -> false
            }
        }

        fabAddStudent.setOnClickListener {
            resetFormToAddMode()
            bottomNavigation.selectedItemId = R.id.nav_add
        }

        btnAddFirstStudent.setOnClickListener {
            resetFormToAddMode()
            bottomNavigation.selectedItemId = R.id.nav_add
        }

        btnCancelEdit.setOnClickListener {
            resetFormToAddMode()
            bottomNavigation.selectedItemId = R.id.nav_records
        }
    }

    private fun showRecordsPage() {
        layoutRecordsPage.visibility = View.VISIBLE
        layoutFormPage.visibility = View.GONE
        tvHeaderTitle.text = "Student Directory"
        tvHeaderSub.text = "Manage and search student information"
    }

    private fun showFormPage() {
        layoutRecordsPage.visibility = View.GONE
        layoutFormPage.visibility = View.VISIBLE
        if (selectedStudentId != null) {
            tvHeaderTitle.text = "Edit Student"
            tvHeaderSub.text = "Update or delete student record"
        } else {
            tvHeaderTitle.text = "Add New Student"
            tvHeaderSub.text = "Enter details to register student"
        }
    }

    private fun setupSpinners() {
        courseAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            mutableListOf("Select course", "BCA", "MCA")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spCourse.adapter = courseAdapter

        semesterAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            mutableListOf("Select semester")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spSemester.adapter = semesterAdapter

        spCourse.onItemSelectedListener = SimpleItemSelectedListener { position ->
            val semesters = when (position) {
                1 -> listOf("Select semester") + (1..6).map { "Semester $it" }
                2 -> listOf("Select semester") + (1..4).map { "Semester $it" }
                else -> listOf("Select semester")
            }
            semesterAdapter.clear()
            semesterAdapter.addAll(semesters)
            semesterAdapter.notifyDataSetChanged()
            spSemester.setSelection(0)
        }
    }

    private fun setupListAndSearch() {
        studentAdapter = StudentCardAdapter(this, mutableListOf())
        listStudents.adapter = studentAdapter

        listStudents.setOnItemClickListener { _, _, position, _ ->
            val student = studentAdapter.getItem(position) ?: return@setOnItemClickListener
            loadStudentForEditing(student)
            bottomNavigation.selectedItemId = R.id.nav_add
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStudents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFormActions() {
        etDob.setOnClickListener { showDatePicker() }

        btnSave.setOnClickListener {
            val data = validateForm() ?: return@setOnClickListener
            val result = db.insertStudent(
                data.name, data.enrollment, data.dob, data.contact, data.course, data.semester
            )
            if (result > 0L) {
                toast("Student added successfully")
                refreshStudents()
                resetFormToAddMode()
                bottomNavigation.selectedItemId = R.id.nav_records
            } else {
                toast("Enrolment number already exists")
            }
        }

        btnUpdate.setOnClickListener {
            val id = selectedStudentId
            if (id == null) {
                toast("Select a student from the directory first")
                return@setOnClickListener
            }
            val data = validateForm() ?: return@setOnClickListener
            val result = db.updateStudent(
                id, data.name, data.enrollment, data.dob, data.contact, data.course, data.semester
            )
            when {
                result > 0 -> {
                    toast("Student updated successfully")
                    refreshStudents()
                    resetFormToAddMode()
                    bottomNavigation.selectedItemId = R.id.nav_records
                }
                result == -1 -> toast("Another record already uses this enrolment number")
                else -> toast("No changes were saved")
            }
        }

        btnDelete.setOnClickListener {
            val id = selectedStudentId
            if (id == null) {
                toast("Select a student from the directory first")
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Delete Student Record?")
                .setMessage("Are you sure you want to permanently delete this student record?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    if (db.deleteStudent(id) > 0) {
                        toast("Student deleted")
                        refreshStudents()
                        resetFormToAddMode()
                        bottomNavigation.selectedItemId = R.id.nav_records
                    } else {
                        toast("Could not delete record")
                    }
                }
                .show()
        }

        btnClear.setOnClickListener {
            resetFormToAddMode()
        }
    }

    private fun loadStudentForEditing(student: Student) {
        selectedStudentId = student.id
        etName.setText(student.name)
        etEnrollment.setText(student.enrollment)
        etDob.setText(student.dob)
        etContact.setText(student.contact)

        val courseIndex = when (student.course) {
            "BCA" -> 1
            "MCA" -> 2
            else -> 0
        }
        spCourse.setSelection(courseIndex)
        spCourse.post {
            val semesterNumber = student.semester.substringAfterLast(" ").toIntOrNull() ?: 0
            if (semesterNumber in 1 until semesterAdapter.count) {
                spSemester.setSelection(semesterNumber)
            } else {
                spSemester.setSelection(0)
            }
        }

        // Switch form UI to Edit Mode
        tvFormTitle.text = "Update Student Record"
        tvModeBannerText.text = "Editing: ${student.name}"
        layoutEditModeBanner.visibility = View.VISIBLE
        btnSave.visibility = View.GONE
        btnUpdate.visibility = View.VISIBLE
        btnDelete.visibility = View.VISIBLE
    }

    private fun resetFormToAddMode() {
        selectedStudentId = null
        etName.text.clear()
        etEnrollment.text.clear()
        etDob.text.clear()
        etContact.text.clear()

        etName.error = null
        etEnrollment.error = null
        etDob.error = null
        etContact.error = null

        spCourse.setSelection(0)
        tvFormTitle.text = "Student Registration Details"
        layoutEditModeBanner.visibility = View.GONE
        btnSave.visibility = View.VISIBLE
        btnUpdate.visibility = View.GONE
        btnDelete.visibility = View.GONE
    }

    private fun refreshStudents() {
        allStudentsList = db.getAllStudents()
        if (allStudentsList.isEmpty()) {
            db.insertStudent(
                "Aman Sharma", "123456789012345", "15/08/2002", "9876543210", "BCA", "Semester 3"
            )
            allStudentsList = db.getAllStudents()
        }
        filterStudents(etSearch.text.toString())
    }

    private fun filterStudents(query: String) {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        val filtered = if (trimmed.isEmpty()) {
            allStudentsList
        } else {
            allStudentsList.filter {
                it.name.lowercase(Locale.getDefault()).contains(trimmed) ||
                        it.enrollment.contains(trimmed) ||
                        it.course.lowercase(Locale.getDefault()).contains(trimmed) ||
                        it.semester.lowercase(Locale.getDefault()).contains(trimmed)
            }
        }

        studentAdapter.clear()
        studentAdapter.addAll(filtered)
        studentAdapter.notifyDataSetChanged()

        val count = filtered.size
        tvCount.text = if (count == 1) "1 student found" else "$count students found"

        if (allStudentsList.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            listStudents.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            listStudents.visibility = View.VISIBLE
        }
    }

    private data class FormData(
        val name: String, val enrollment: String, val dob: String,
        val contact: String, val course: String, val semester: String
    )

    private fun validateForm(): FormData? {
        val name = etName.text.toString().trim()
        val enrollment = etEnrollment.text.toString().trim()
        val dob = etDob.text.toString().trim()
        val contact = etContact.text.toString().trim()
        val coursePosition = spCourse.selectedItemPosition
        val semesterPosition = spSemester.selectedItemPosition

        if (name.isEmpty() || !name.matches(Regex("[\\p{L} ]+"))) {
            etName.error = "Enter a name using letters only"
            etName.requestFocus()
            return null
        }
        if (!enrollment.matches(Regex("\\d{15}"))) {
            etEnrollment.error = "Enrolment number must contain exactly 15 digits"
            etEnrollment.requestFocus()
            return null
        }
        if (!isPastDate(dob)) {
            etDob.error = "Select a valid date in the past"
            toast("Date of birth must be a past date")
            return null
        }
        if (!contact.matches(Regex("\\d{10}"))) {
            etContact.error = "Contact number must contain exactly 10 digits"
            etContact.requestFocus()
            return null
        }
        if (coursePosition !in 1..2) {
            toast("Please select BCA or MCA")
            return null
        }
        if (semesterPosition <= 0 || spSemester.selectedItem == null) {
            toast("Please select a semester")
            return null
        }

        return FormData(
            name, enrollment, dob, contact,
            spCourse.selectedItem.toString(), spSemester.selectedItem.toString()
        )
    }

    private fun isPastDate(value: String): Boolean {
        if (value.isBlank()) return false
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            isLenient = false
        }
        val position = ParsePosition(0)
        val parsed = format.parse(value, position) ?: return false
        if (position.index != value.length) return false

        val selected = Calendar.getInstance().apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return selected.before(today)
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()
        val picker = DatePickerDialog(
            this,
            { _, year, month, day ->
                etDob.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year))
                etDob.error = null
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        picker.datePicker.maxDate = yesterday.timeInMillis
        picker.show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    // Custom Adapter for Student Cards with Navy, Teal & Ice Mint styling
    private inner class StudentCardAdapter(
        context: Context,
        private val items: MutableList<Student>
    ) : ArrayAdapter<Student>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.item_student_card, parent, false
            )
            val student = getItem(position) ?: return view

            val tvAvatar = view.findViewById<TextView>(R.id.tvAvatar)
            val tvStudentName = view.findViewById<TextView>(R.id.tvStudentName)
            val tvCourseBadge = view.findViewById<TextView>(R.id.tvCourseBadge)
            val tvEnrollment = view.findViewById<TextView>(R.id.tvEnrollment)
            val tvDob = view.findViewById<TextView>(R.id.tvDob)
            val tvContact = view.findViewById<TextView>(R.id.tvContact)

            val firstLetter = student.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "S"
            tvAvatar.text = firstLetter
            tvStudentName.text = student.name
            tvCourseBadge.text = "${student.course} • ${student.semester}"
            tvEnrollment.text = "Enrolment: ${student.enrollment}"
            tvDob.text = "DOB: ${student.dob}"
            tvContact.text = "Ph: ${student.contact}"

            return view
        }
    }
}
