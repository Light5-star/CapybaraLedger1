package com.xuhh.capybaraledger.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class StatisticsViewModel:ViewModel() {
    private val _calendar = MutableLiveData<Calendar>().apply {
        value = Calendar.getInstance()
    }
    val calendar: LiveData<Calendar> = _calendar

    fun nextMonth() {
        val newCalendar = _calendar.value!!.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        _calendar.value = newCalendar
    }

    fun backMonth() {
        val newCalendar = _calendar.value!!.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        _calendar.value = newCalendar
    }

}
