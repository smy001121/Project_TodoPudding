package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodayFragment : Fragment() {

    lateinit var items: ArrayList<String>
    private lateinit var listView: ListView
    lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedViewModel: SharedViewModel
    // 다이얼로그를 저장할 변수 선언
    var alertDialog: AlertDialog? = null // 추가

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today, container, false)

        listView = view.findViewById(R.id.listView)
        arrayAdapter = ArrayAdapter(requireContext(), R.layout.custom_list_item, ArrayList())
        listView.adapter = arrayAdapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        items = arrayListOf()

        //  SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)

        // ViewModel 초기화
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 추가 버튼 비활성화
        val addTaskButton: Button = requireActivity().findViewById(R.id.addTaskButton)
        addTaskButton.isVisible = false

        // 할일목록 불러오기
        loadTodoItems()

        // 추가: LiveData를 관찰하여 데이터 업데이트
        sharedViewModel.todayTodoItems.observe(viewLifecycleOwner, { items ->
            // 필터링된 목록을 가져옴 (마감 날짜가 0이거나 오늘인 것만)
            val filteredItems = items.filter { it.contains("day") }

            arrayAdapter.clear()
            arrayAdapter.addAll(filteredItems)
            arrayAdapter.notifyDataSetChanged()
        })

        // 할일 목록을 ViewModel에 업데이트
        sharedViewModel.setTodayTodoItems(items)

        // 정렬
        items.sortBy { extractDaysUntilDeadline(it) }
        arrayAdapter.clear()
        arrayAdapter.addAll(items)
        arrayAdapter.notifyDataSetChanged()


        // 리스트뷰 토스트 메세지
        listView.setOnItemClickListener { _, view, position, _ ->
            val selectedItem = listView.getItemAtPosition(position) as String

            // 체크 상태에 따라 동작 수행
            if (listView.isItemChecked(position)) { // 체크된 경우
                Toast.makeText(view.context, "할 일을 완료하셨습니다!", Toast.LENGTH_SHORT).show()

            } else { // 체크 해제된 경우
                Toast.makeText(view.context, "해제되었습니다!", Toast.LENGTH_SHORT).show()
            }

            // 체크 상태를 ViewModel을 통해 업데이트
            sharedViewModel.updateCheckedItems(selectedItem, listView.isItemChecked(position))
        }

        // ViewModel에서 체크 상태를 관찰하여 UI 업데이트
        sharedViewModel.checkedItems.observe(viewLifecycleOwner, Observer { checkedItems ->
            // checkedItems를 사용하여 UI를 업데이트
            for ((todoItem, isChecked) in checkedItems) {
                // 리스트뷰의 아이템 체크 상태를 설정
                val position = items.indexOf(todoItem)
                if (position != -1) {
                    listView.setItemChecked(position, isChecked)
                }
            }
        })

        listView.setOnItemLongClickListener { _, view, position, _ ->
            val selectedItem = listView.getItemAtPosition(position) as String

            showDeleteConfirmationDialog(position) // 할일 삭제 시 뜨는 다이얼로그 창

            arrayAdapter.clear()
            arrayAdapter.addAll(items)
            arrayAdapter.notifyDataSetChanged()

            true // 롱클릭 이벤트를 소비함
        }

        return view
    }

    // MainActivity에서 호출되어 할일을 추가하는 메서드
    fun addTodoItem(todoItem: String) {
        items.add(todoItem)

        // Save todo items
        saveTodoItems()

        // 추가: 체크 상태도 함께 저장
        sharedViewModel.updateCheckedItems(todoItem, false)

        arrayAdapter.clear()
        arrayAdapter.addAll(items)
        arrayAdapter.notifyDataSetChanged()

        // 추가: TodayFragment에서 할일이 추가될 때 ViewModel을 업데이트
        sharedViewModel.setTodayTodoItems(items)
    }

    // 기존 정보 저장 함수
    private fun saveTodoItems() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("todo_items", HashSet(items))
        editor.apply()
    }

    // 할일 목록 불러오는 함수
    private fun loadTodoItems() {
        val savedItems = sharedPreferences.getStringSet("todo_items", HashSet())
        items.clear()

        // 마감 날짜가 "day"인 항목만 필터링
        items.addAll(savedItems?.filter { it.contains("day") } ?: emptySet())

        // 추가: TodayFragment에서 할일이 추가될 때 ViewModel을 업데이트
        sharedViewModel.setTodayTodoItems(items)


    }

    // 추가 : 삭제 시 뜨는 다이얼로그창
    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        // 다이얼로그 인스턴스 저장
        alertDialog = builder.create()

        val okButton = dialogView.findViewById<Button>(R.id.deleteOkBtn)
        val noButton = dialogView.findViewById<Button>(R.id.deleteNoBtn)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)

        // 폰트 적용
        val customFont = ResourcesCompat.getFont(requireContext(), R.font.font)
        okButton.typeface = customFont
        noButton.typeface = customFont
        messageTextView.typeface = customFont

        // 확인 버튼 클릭 시 동작 설정
        okButton.setOnClickListener {
            // 아이템 삭제
            val selectedItem = listView.getItemAtPosition(position) as String
            items.remove(selectedItem)
            arrayAdapter.notifyDataSetChanged()

            // 추가: SharedPreferences에서 데이터 삭제
            val editor = sharedPreferences.edit()
            editor.putStringSet("todo_items", HashSet(items))
            editor.apply()

            // 추가: 체크 상태도 함께 업데이트
            sharedViewModel.updateCheckedItems(selectedItem, false)

            // 할일 목록을 ViewModel에 업데이트
            sharedViewModel.setTodayTodoItems(items)

            // 리스트뷰 갱신
            arrayAdapter.clear()
            arrayAdapter.addAll(items)
            arrayAdapter.notifyDataSetChanged()

            Toast.makeText(requireContext(),"할일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            alertDialog?.dismiss()
        }

        // 취소 버튼 클릭 시 동작 설정
        noButton.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialog?.show()
    }

    // 문자열에서 "D - n" 형식의 n(일수)를 추출하는 함수
    private fun extractDaysUntilDeadline(todoItem: String): Int {
        val regexResult = Regex("D - (\\d+)").find(todoItem)
        return regexResult?.groupValues?.getOrNull(1)?.toIntOrNull() ?: Int.MIN_VALUE
    }


}