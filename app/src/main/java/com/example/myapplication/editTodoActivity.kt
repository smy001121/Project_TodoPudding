package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat

class EditTodoActivity : AppCompatActivity() {
    lateinit var doName : EditText
    lateinit var dateBtn : Button
    lateinit var datePicker: DatePicker
    lateinit var okBtn : Button
    lateinit var category : Spinner
    var selectYear : Int = 0
    var selectMonth : Int = 0
    var selectDay : Int = 0
    lateinit var todoName : String
    lateinit var backBtn : Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.activity_edit_todo)

        doName = findViewById<EditText>(R.id.doName)
        dateBtn = findViewById<Button>(R.id.dateBnt)
        datePicker = findViewById<DatePicker>(R.id.datePicker)
        okBtn = findViewById<Button>(R.id.okBtn)
        category = findViewById<Spinner>(R.id.category)
        var items = resources.getStringArray(R.array.list_array)
        backBtn = findViewById<Button>(R.id.backBtn)

        // 폰트 적용
        val customFont = ResourcesCompat.getFont(this, R.font.font)
        val boldTypeface = Typeface.create(customFont, Typeface.BOLD)

        okBtn.typeface = boldTypeface
        backBtn.typeface = boldTypeface
        dateBtn.typeface = boldTypeface

        // 마감기한버튼 누르면 날짜설정창 보임
        datePicker.visibility = View.INVISIBLE

        dateBtn.setOnClickListener {
            datePicker.visibility = View.VISIBLE
        }
        datePicker.setOnDateChangedListener { view, year, month, dayOfMonth ->
            selectYear = year
            selectMonth = month + 1
            selectDay = dayOfMonth
        }

        // 카테고리 목록 가져오기
        val categories = getCategoriesFromSharedPreferences()

        // Spinner에 어댑터 설정
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories)
        category.adapter = adapter

        category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // 선택된 카테고리에 대한 동작
                val selectedCategory = categories[position]
                println(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("카테고리를 선택하세요")
            }
        }


        //확인버튼 누르면 오늘 탭에 할일이 추가되어야함
        okBtn.setOnClickListener {
            //카테고리 선택됨
            var categorySelected = category.selectedItem.toString()
            println(categorySelected)

            //오늘 탭으로 넘어감
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("todoname", doName.text.toString())
            intent.putExtra("deadline", "$selectYear-$selectMonth-$selectDay")
            intent.putExtra("category", categorySelected)
            setResult(RESULT_OK, intent)
            finish()
        }

        // 취소 버튼 누르면 뒤로가기
        backBtn.setOnClickListener {
            finish()
        }
    }

    // SharedPreferences에서 카테고리 목록을 가져오는 함수
    private fun getCategoriesFromSharedPreferences(): List<String> {
        val sharedPreferences =
            getSharedPreferences("MyTodoPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("categories", setOf())?.toList() ?: emptyList()
    }

    companion object {
        const val EDIT_TODO_REQUEST_CODE = 1
    }

}