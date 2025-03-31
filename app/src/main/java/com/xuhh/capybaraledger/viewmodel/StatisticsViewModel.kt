package com.xuhh.capybaraledger.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class StatisticsViewModel : ViewModel() {
    private val _calendar = MutableLiveData<Calendar>().apply {
        value = Calendar.getInstance()
    }
    val calendar: LiveData<Calendar> = _calendar
    private val _currentYear = MutableLiveData<Int>().apply {
        value = Calendar.getInstance().get(Calendar.YEAR)
    }
    val currentYear: LiveData<Int> = _currentYear
    private val _currentMonth = MutableLiveData<Int>().apply {
        value = Calendar.getInstance().get(Calendar.MONTH)
    }
    val currentMonth: LiveData<Int> = _currentMonth

    fun nextMonth() {
        val newCalendar = _calendar.value!!.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        _calendar.value = newCalendar
        _currentYear.value = newCalendar.get(Calendar.YEAR)
        _currentMonth.value = newCalendar.get(Calendar.MONTH)
    }

    fun backMonth() {
        val newCalendar = _calendar.value!!.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        _calendar.value = newCalendar
        _currentYear.value = newCalendar.get(Calendar.YEAR)
        _currentMonth.postValue(newCalendar.get(Calendar.MONTH))
    }
}
