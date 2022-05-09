package com.example.moodleLearning.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.moodleLearning.adapters.LecturesAdapter
import com.example.moodleLearning.databinding.ActivityCourseDetailsBinding
import com.example.moodleLearning.models.Course
import com.example.moodleLearning.models.Lecture
import com.example.moodleLearning.models.User
import com.example.moodleLearning.ui.LectureInfoFragment.Companion.LECTURE_DOCS
import com.example.moodleLearning.ui.LectureInfoFragment.Companion.LECTURE_ID
import com.example.moodleLearning.ui.LectureInfoFragment.Companion.LECTURE_VIDEO
import com.example.moodleLearning.utils.FCMService
import com.example.moodleLearning.utils.Helper.Companion.fillImage
import com.example.moodleLearning.utils.Helper.Companion.formatDate
import com.example.moodleLearning.utils.Helper.Companion.log
import com.example.moodleLearning.utils.Helper.Companion.subscribeToTopic
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.example.moodleLearning.utils.Helper.Companion.unSubscribeToTopic
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CourseDetailsActivity : AppCompatActivity(), LecturesAdapter.OnClick {
    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityCourseDetailsBinding
    private lateinit var lecturesAdapter: LecturesAdapter
    private var db = Firebase.firestore
    private var user = Firebase.auth.currentUser
    private var courseId : String = ""
    private var courseName : String = ""
    private var isCourseRegistered : Boolean = false
    private lateinit var instructorId: String
    private var isTeacher: Boolean = false
    private var teacherToken = ""

    companion object {
        const val EXTRA_COURSE_ID = "courseId"
        const val EXTRA_COURSE_NAME = "courseName"
        const val EXTRA_IS_TEACHER = "isTeacher"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra(EXTRA_COURSE_ID)!!
        courseName = intent.getStringExtra(EXTRA_COURSE_NAME)!!
        isTeacher = intent.getBooleanExtra(EXTRA_IS_TEACHER, false)

        lecturesAdapter = LecturesAdapter(this, this, isTeacher)
        binding.rvLectures.adapter = lecturesAdapter

        if (isTeacher) {
            binding.btnRegisterCourse.visibility = View.GONE
            binding.btnNewLecture.visibility = View.VISIBLE
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        getCourseInfo()
        getLectures()
        checkIsCourseRegistered()

        binding.tvPublicChat.setOnClickListener {
            val i = Intent(this, PublicChatActivity::class.java)
            i.putExtra(PublicChatActivity.COURSE_ID,courseId)
            i.putExtra(PublicChatActivity.CHAT_TITLE,courseName)
            startActivity(i)
        }

        binding.btnNewLecture.setOnClickListener {
            val intent = Intent(this, AddLectureActivity::class.java)
            intent.putExtra(AddLectureActivity.EXTRA_COURSE_ID, courseId)
            intent.putExtra(AddLectureActivity.EXTRA_COURSE_NAME, courseName)
            startActivity(intent)
        }

        binding.cvInstructor.setOnClickListener {
            InstructorInfoFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(InstructorInfoFragment.INSTRUCTOR_ID, instructorId)
                    }
                }
                .show(supportFragmentManager, "InstructorInfoFragment")
        }

        binding.btnRegisterCourse.setOnClickListener {
            if (isCourseRegistered) {
                unregisterCourse()
            } else {
                checkIsCanAddCourse()
            }
        }
    }

    private fun checkIsCourseRegistered() {
        db.collection(User.USERS_COLLECTION).document(user!!.uid).get()
            .addOnSuccessListener {
                val activeCourses = it[User.USER_ACTIVE_COURSES] as List<*>?
                if (activeCourses?.contains(courseId) == true) {
                    binding.btnRegisterCourse.text = "Move To Trash"
                    isCourseRegistered = true
                } else {
                    binding.btnRegisterCourse.text = "Register Course"
                    isCourseRegistered = false
                }
                binding.btnRegisterCourse.isEnabled = true
            }
            .addOnFailureListener {
                toast(applicationContext, "Failed to get course info")
                finish()
            }
    }

    private fun getLectures() {
        val lectures = ArrayList<Lecture>()

        db.collection(Course.COURSES_COLLECTION)
            .document(courseId)
            .collection(Lecture.LECTURES_COLLECTION)
            .orderBy(Lecture.LECTURE_Date)
            .get()
            .addOnSuccessListener { docs ->
                docs.documents.forEach {
                    val title = it[Lecture.LECTURE_TITLE] as String
                    val docsUrl = it[Lecture.LECTURE_DOCS_URL] as String
                    val videoUrl = it[Lecture.LECTURE_VIDEO_URL] as String
                    val watchersIds = it[Lecture.LECTURE_WATCHERS_IDS] as List<String>
                    lectures.add(Lecture(it.id, title, docsUrl, videoUrl, watchersIds))
                }
                lecturesAdapter.setData(lectures)
            }
            .addOnFailureListener {
                log(this, it.message!!)
                toast(applicationContext, "Error getting registered courses, ${it.message}")
            }
    }

    private fun unregisterCourse() {
        db.collection(User.USERS_COLLECTION)
            .document(user!!.uid)
            .update(User.USER_ACTIVE_COURSES, FieldValue.arrayRemove(courseId), User.USER_FINISHED_COURSES, FieldValue.arrayUnion(courseId))
            .addOnSuccessListener {
                toast(this, "Course Unregistered Successfully")
                db.collection(Course.COURSES_COLLECTION).document(courseId).update(Course.COURSE_REGISTERS_IDS, FieldValue.arrayRemove(user!!.uid))
                db.collection(Course.COURSES_COLLECTION).document(courseId).update(Course.COURSE_REGISTERS_EMAILS, FieldValue.arrayRemove(user!!.email))
                unSubscribeToTopic(courseId)
                isCourseRegistered = false
                binding.btnRegisterCourse.text = "Register Course"
            }
            .addOnFailureListener {
                log(this, "Error removing document: ${it.message}")
                toast(this, "Course Unregistered Failed, ${it.message}")
            }
    }

    private fun checkIsCanAddCourse() {
        db.collection(User.USERS_COLLECTION)
            .document(user!!.uid)
            .get()
            .addOnSuccessListener {
                val activeCourses = it[User.USER_ACTIVE_COURSES] as List<*>?
                if (activeCourses == null) {
                    registerCourse()
                } else {
                    if (activeCourses.size >= 5) {
                        toast(this, "You can't add more than 5 courses")
                    } else {
                        registerCourse()
                    }
                }
            }
            .addOnFailureListener {
                log(this, "Error removing document: ${it.message}")
                toast(this, "Course Registered Failed, ${it.message}")
            }
    }

    private fun registerCourse() {
        db.collection(User.USERS_COLLECTION)
            .document(user!!.uid)
            .update(User.USER_ACTIVE_COURSES, FieldValue.arrayUnion(courseId))
            .addOnSuccessListener {
                db.collection(Course.COURSES_COLLECTION).document(courseId).update(Course.COURSE_REGISTERS_IDS, FieldValue.arrayUnion(user!!.uid))
                db.collection(Course.COURSES_COLLECTION).document(courseId).update(Course.COURSE_REGISTERS_EMAILS, FieldValue.arrayUnion(user!!.email))
                toast(this, "Course Registered Successfully")
                subscribeToTopic(courseId)
                FCMService.sendRemoteNotification("New Registration to $courseName", "${user?.email ?: user?.displayName} have been registered to ${courseName}", teacherToken)
                isCourseRegistered = true
                binding.btnRegisterCourse.text = "Unregister Course"
            }
            .addOnFailureListener {
                toast(this, "Course Registered Failed, ${it.message}")
            }
    }

    private fun getCourseInfo() {
        db.collection(Course.COURSES_COLLECTION)
            .document(courseId)
            .get()
            .addOnSuccessListener {
                it.toObject(Course::class.java)?.let { course ->
                    fillImage(this, course.img, binding.ivCourse)
                    binding.tvCourseName.text = course.title
                    binding.tvCourseDesc.text = course.desc
                    binding.tvCourseCategory.text = course.category
                    binding.tvCourseHours.text = "${course.hours} \n hours"
                    binding.tvCourseRegistration.text = "${course.registersIds.size} \n registration"
                    binding.tvCourseCreateDate.text = " Created At \n ${formatDate(course.createDate)}"
                    binding.tvCourseLastUpdate.text = " Updated At \n ${formatDate(course.lastUpdateDate)}"
                    binding.tvCourseDesc.text = course.desc

                    fillInstructorInfo(course.ownerId)
                }
            }.addOnFailureListener {
                log(this, "Error getting course info $it")
                toast(applicationContext, "Error getting course info $it")
                finish()
            }
    }

    private fun fillInstructorInfo(ownerId: String) {
        db.collection(User.USERS_COLLECTION).document(ownerId).get()
            .addOnSuccessListener {
                it.toObject(User::class.java)?.let { user ->
                    binding.cvInstructor.visibility = View.VISIBLE
                    instructorId = user.id
                    binding.tvInstructorName.text ="${user.firstName} ${user.middleName[0].uppercaseChar()} ${user.lastName}"
                    binding.tvInstructorEmail.text = user.email
                    teacherToken = user.token
                }
            }
            .addOnFailureListener {
                log(this, "Error getting instructor info $it")
                toast(applicationContext, "Error getting instructor info $it")
            }
    }

    override fun onStudentClickLecture(lecture: Lecture, position: Int) {
        if (isCourseRegistered) {
            db.collection(Course.COURSES_COLLECTION)
                .document(courseId)
                .collection(Lecture.LECTURES_COLLECTION)
                .orderBy(Lecture.LECTURE_Date)
                .get()
                .addOnSuccessListener {
                    val lectures = it.toObjects(Lecture::class.java)

                    if (position != 0 && !lectures[position-1].watchersIds.contains(user!!.uid)) {
                        toast(applicationContext, "All previous lectures must be viewed first")
                        return@addOnSuccessListener
                    }

                    LectureInfoFragment()
                        .apply {
                            arguments = Bundle().apply {
                                putString(LECTURE_VIDEO, lecture.videoUrl)
                                putString(LECTURE_DOCS, lecture.docsUrl)
                                putString(LECTURE_ID, lecture.id)
                                putString(LectureInfoFragment.COURSE_ID, courseId)
                            }
                        }
                        .show(supportFragmentManager, "LectureInfoFragment")
                }
                .addOnFailureListener {
                    log(this, "Error getting lectures $it")
                    toast(applicationContext, "Error getting lectures $it")
                }
        } else {
            toast(this, "You must register course to view lectures")
        }
    }

    override fun onTeacherClickLecture(lecture: Lecture) {
        val intent = Intent(this, AddLectureActivity::class.java)
        intent.putExtra(AddLectureActivity.EXTRA_COURSE_ID, courseId)
        intent.putExtra(AddLectureActivity.EXTRA_LECTURE_ID, lecture.id)
        intent.putExtra(AddLectureActivity.EXTRA_COURSE_NAME, courseName)
        startActivity(intent)
    }
}