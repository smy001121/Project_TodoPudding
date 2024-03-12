package com.example.myapplication

import CategoryAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val categoriesKey = "categories"
    private lateinit var sharedViewModel: SharedViewModel
    private var allTodoItems: List<String> = emptyList() // allTodoItems에 접근 가능한 프로퍼티 추가

    private lateinit var categories: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        // SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences("MyTodoPreferences", Context.MODE_PRIVATE)

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        val spanCount = 2
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        // ViewModel 초기화
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 추가 버튼 비활성화
        // requireActivity() 함수를 사용하여 현재 프래그먼트가 속한 액티비티에 접근하고,
        // 거기서 findViewById 메서드를 통해 추가 버튼을 찾아서 비활성화 시킴
        val addTaskButton: Button = requireActivity().findViewById(R.id.addTaskButton)
        addTaskButton.isVisible = false

        // 아래 코드를 추가하여 ViewModel에서 데이터가 업데이트될 때마다 처리할 로직을 정의
        sharedViewModel.allTodoItems.observe(viewLifecycleOwner, Observer { updatedAllTodoItems ->
            allTodoItems = updatedAllTodoItems
        })

        // 아이템 간격 추가
        val itemDecoration = CategoryItemDecoration(16) // 간격 크기
        recyclerView.addItemDecoration(itemDecoration)

        // 초기 카테고리 목록을 정렬하여 어댑터에 설정
        categories = getCategories().sorted().toMutableList()
        categoryAdapter = CategoryAdapter(categories)
        recyclerView.adapter = categoryAdapter

        // 카테고리 추가 버튼 초기화 및 클릭 리스너 설정
        val addCategoryButton: Button = view.findViewById(R.id.addCategoryButton)
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()

            // 업데이트된 카테고리 목록을 어댑터에 설정
            categoryAdapter.updateCategories(getCategories())
        }

        // onDeleteButtonClick 이벤트 처리
        categoryAdapter.onDeleteButtonClick = { position ->
            val category = categoryAdapter.getCategories()[position]
            showDeleteCategoryDialog(category)
        }

        // 카테고리 아이템 클릭 리스너
        categoryAdapter.onCategoryItemClick = { category ->
            showTasksDialog(category)
        }

        // 폰트 적용
        val customFont = ResourcesCompat.getFont(requireContext(), R.font.font)
        addCategoryButton.typeface = customFont

        return view
    }

    // 클릭한 카테고리로 설정된 할일들 보여주는 다이얼로그
    private fun showTasksDialog(category: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category_progress, null)
        // AlertDialog.Builder를 사용하여 다이얼로그 생성
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)

        val dialog = builder.create()

        val titleTextView: TextView = dialogView.findViewById(R.id.categoryDialogTitle)
        val progressBar: ProgressBar = dialogView.findViewById(R.id.categoryProgressBar)
        val progressText: TextView = dialogView.findViewById(R.id.categoryProgressText)

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.font)

        // 다이얼로그 제목에 선택한 카테고리 이름 설정
        titleTextView.text = category
        titleTextView.setTypeface(customFont, Typeface.BOLD)

        // 해당 카테고리에 속한 모든 할 일 아이템 가져오기
        val tasks = allTodoItems.filter { it.contains(category) }
        val totalTasks = tasks.size

        // 해당 카테고리에 속한 모든 할 일 아이템 중에서 체크된 아이템 개수 계산
        val completedTasks = tasks.count { task ->
            // 할 일 아이템의 이름과 체크 여부를 가져와서 체크된 경우만 세기
            val isChecked = sharedViewModel.checkedItems.value?.get(task) ?: false
            isChecked
        }

        // 아래 코드를 추가하여 ViewModel에서 데이터가 업데이트될 때마다 처리할 로직을 정의
        sharedViewModel.checkedItems.observe(viewLifecycleOwner, Observer { checkedItems ->
            // 여기에서 필요한 작업을 수행
            // 예를 들어, 업데이트된 데이터로부터 특정 카테고리에 해당하는 할 일 개수를 가져올 수 있음
            val totalTasks = checkedItems.count { it.key.contains(category) }

            // 완료된 할 일 개수
            val completedTasks = checkedItems.count { (task, isChecked) ->
                task.contains(category) && isChecked
            }

            // 해당 카테고리에 속한 모든 할 일 아이템 중에서 체크된 아이템의 비율 계산
            val progress = if (tasks.isNotEmpty()) {
                (completedTasks.toDouble() / tasks.size.toDouble() * 100).toInt()
            } else {
                0
            }
            // 다이얼로그에 진행 상황 정보 설정
            progressBar.progress = progress
            progressText.text = "$completedTasks/$totalTasks"
        })

        // 해당 카테고리에 속한 모든 할 일 아이템 중에서 체크된 아이템의 비율 계산
        val progress = if (tasks.isNotEmpty()) {
            (completedTasks.toDouble() / tasks.size.toDouble() * 100).toInt()
        } else {
            0
        }

        // 진행 상황 정보 설정
        progressBar.progress = progress
        progressText.text = "$completedTasks/$totalTasks"

        /*// 다이얼로그 생성 및 표시
        builder.setView(dialogView)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()*/

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        // 다이얼로그 인스턴스 저장
        val alertDialog = builder.create()

        val okButton = dialogView.findViewById<Button>(R.id.addBtn)
        val noButton = dialogView.findViewById<Button>(R.id.deleteBtn)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val inputCategory = dialogView.findViewById<EditText>(R.id.inputCategory)

        val customFont = ResourcesCompat.getFont(requireContext(), R.font.font)

        // 다이얼로그 제목 설정
        messageTextView.setTypeface(customFont, Typeface.BOLD)


        // 나머지 다이얼로그 내용
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL

        // 추가 버튼 클릭 시
        okButton.setOnClickListener {
            val newCategory = inputCategory.text.toString()
            saveCategory(newCategory)
            Snackbar.make(requireView(), "카테고리 '$newCategory'가 추가되었습니다!", Snackbar.LENGTH_SHORT).show()

            // 업데이트된 카테고리 목록을 어댑터에 설정
            categoryAdapter.updateCategories(getCategories())
            alertDialog.dismiss()
        }

        // 취소 버튼 클릭 시
        noButton.setOnClickListener {
            alertDialog.cancel()
        }

        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }

    // 카테고리 저장 메서드
    private fun saveCategory(newCategory: String) {
        // 기존 카테고리 목록을 가져와 새로운 카테고리 추가
        val categories = getCategories().apply {
            add(newCategory)
            sort()
        }

        // SharedPreferences를 사용하여 카테고리 목록 저장
        val editor = sharedPreferences.edit()
        editor.putStringSet(categoriesKey, categories.toSet())
        editor.apply()

        // 업데이트된 카테고리 목록을 어댑터에 설정
        categoryAdapter.updateCategories(categories)
    }

    // 카테고리 목록 가져오는 메서드
    fun getCategories(): MutableList<String> {
        // SharedPreferences에서 카테고리 목록을 가져옴
        return sharedPreferences.getStringSet(categoriesKey, setOf())?.toMutableList() ?: mutableListOf()
    }

    // 카테고리 삭제 메서드
    private fun deleteCategory(category: String) {
        val categories = getCategories().toMutableList()
        categories.remove(category)

        val editor = sharedPreferences.edit()
        editor.putStringSet(categoriesKey, categories.toSet())
        editor.apply()

        // 업데이트된 카테고리 목록을 어댑터에 설정
        categoryAdapter.updateCategories(getCategories())
    }

    // 삭제 다이얼로그
    private fun showDeleteCategoryDialog(category: String) {
        //val builder = AlertDialog.Builder(requireContext())

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        // 다이얼로그 인스턴스 저장
        val alertDialog = builder.create()

        val okButton = dialogView.findViewById<Button>(R.id.deleteOkBtn)
        val noButton = dialogView.findViewById<Button>(R.id.deleteNoBtn)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)

        messageTextView.text = "카테고리 '$category'를 삭제하시겠습니까?"

        okButton.setOnClickListener {
            val position = categories.indexOf(category)
            if (position != -1) {
                try {
                    // deleteCategory 함수가 예외를 발생시킬 수 있는 부분
                    deleteCategory(category)
                    Snackbar.make(requireView(), "카테고리 '$category'가 삭제되었습니다!", Snackbar.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // 예외가 발생한 경우 처리
                    e.printStackTrace()
                } finally {
                    // AlertDialog 닫기
                    alertDialog?.dismiss()
                }
            }
        }

        noButton.setOnClickListener {
            alertDialog.cancel()
        }

        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog?.show()
    }

    // 카테고리 간의 간격을 주는 클래스
    class CategoryItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = space
            outRect.right = space / 2
            outRect.left = space / 2
        }
    }

}