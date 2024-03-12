package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val TAG_TODAY = "today_fragment"
const val TAG_TOTAL = "total_fragment" //메인액티비티 클래스 외부에서도 접근 가능하도록
const val TAG_CATEGORY = "category_fragment"

class MainActivity : AppCompatActivity() {

    private lateinit var addTaskButton : Button
    private lateinit var category: String
    private var deadline: String = ""

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.navigationView.selectedItemId = R.id.tab2
        }

        // addTaskButton 초기화
        addTaskButton = binding.addTaskButton
        addTaskButton.setOnClickListener {
            val intent = Intent(this, EditTodoActivity::class.java)
            startActivityForResult(intent, EditTodoActivity.EDIT_TODO_REQUEST_CODE)

        }

    }

    private fun setBottomNavigationView() {
        binding.navigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab1 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrameLayout,
                        TodayFragment(),TAG_TODAY).commit()
                    true
                }
                R.id.tab2 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrameLayout,
                        TotalFragment(),TAG_TOTAL).commit()
                    true
                }
                R.id.tab3 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, CategoryFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }

    // MainActivity의 onActivityResult 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EditTodoActivity.EDIT_TODO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // EditTodoActivity에서 데이터를 받아옴
            val todoName = data?.getStringExtra("todoname") ?: ""
            val deadline = data?.getStringExtra("deadline") ?: ""
            category = data?.getStringExtra("category") ?: "No Category"

            //TODAY
            val todayFragment = supportFragmentManager.findFragmentByTag(TAG_TODAY) as? TodayFragment
            if(todayFragment != null) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.show(todayFragment)
                transaction.commit()
            }

            // TotalFragment에 할일 추가
            val totalFragment = supportFragmentManager.findFragmentByTag(TAG_TOTAL) as? TotalFragment
            if (totalFragment != null) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.show(totalFragment)
                transaction.commit()
            }

            // 마감 날짜를 계산하여 "d-n" 형식으로 표시
            val daysUntilDeadline = calculateDaysUntilDeadline(deadline)
            if (daysUntilDeadline == "day") {
                addTodoItemDAY("$todoName        D - $daysUntilDeadline")
            }

            addTodoItemNOTDAY("$todoName        D - $daysUntilDeadline")

        }

    }

    fun addTodoItemDAY(todoItem: String) {
        val todayFragment = supportFragmentManager.findFragmentByTag(TAG_TODAY) as? TodayFragment
        todayFragment?.addTodoItem("$todoItem   ($category)")
    }

    fun addTodoItemNOTDAY(todoItem: String) {
        val totalFragment = supportFragmentManager.findFragmentByTag(TAG_TOTAL) as? TotalFragment
        totalFragment?.addTodoItem("$todoItem   ($category)")
    }


    // MainActivity에서 호출되어 할일을 추가하는 메서드
    fun addTodoItem(todoItem: String) {
        val totalFragment = supportFragmentManager.findFragmentByTag(TAG_TOTAL) as? TotalFragment
        val todayFragment = supportFragmentManager.findFragmentByTag(TAG_TODAY) as? TodayFragment

        if (deadline == "day") {
            todayFragment?.addTodoItem("$todoItem   ($category)")
        }

        totalFragment?.addTodoItem("$todoItem   ($category)")
    }

    // 현재 날짜와 마감 날짜 간의 차이를 계산
    fun calculateDaysUntilDeadline(deadline: String): Any {
        if (deadline.isEmpty() || deadline.equals("No Deadline", ignoreCase = true)) {
            return "No Deadline"
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val deadlineDate = dateFormat.parse(deadline) ?: currentDate

        val diffInMillis = deadlineDate.time - currentDate.time
        val daysUntilDeadline = (diffInMillis / (1000 * 60 * 60 * 24)) + 1
        if (daysUntilDeadline < 1){
            return "day"
        }
        return daysUntilDeadline.toInt()
    }

}