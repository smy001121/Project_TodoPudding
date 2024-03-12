package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SharedViewModel : ViewModel() {
    private val _todayTodoItems = MutableLiveData<List<String>>()
    val todayTodoItems: LiveData<List<String>> get() = _todayTodoItems

    private val _allTodoItems = MutableLiveData<List<String>>()
    val allTodoItems: LiveData<List<String>> get() = _allTodoItems

    val checkedItems: MutableLiveData<MutableMap<String, Boolean>> by lazy {
        MutableLiveData<MutableMap<String, Boolean>>().also {
            it.value = mutableMapOf() // 초기화
        }
    }

    fun setTodayTodoItems(items: List<String>) {
        _todayTodoItems.value = items
    }
    fun setAllTodoItems(items: List<String>) {
        _allTodoItems.value = items
    }

    // 체크된 아이템의 상태를 업데이트하는 메서드
    fun updateCheckedItems(todoItem: String, isChecked: Boolean) {
        val currentCheckedItems = checkedItems.value ?: mutableMapOf()
        currentCheckedItems[todoItem] = isChecked
        checkedItems.postValue(currentCheckedItems)
    }

}
