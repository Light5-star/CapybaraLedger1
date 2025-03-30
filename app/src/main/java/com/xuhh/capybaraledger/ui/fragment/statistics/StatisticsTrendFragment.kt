package com.xuhh.capybaraledger.ui.fragment.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.FragmentStatisticsTrendBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import java.util.*

class StatisticsTrendFragment : BaseFragment<FragmentStatisticsTrendBinding>() {
    override fun initBinding(): FragmentStatisticsTrendBinding {
        return FragmentStatisticsTrendBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
    }

}