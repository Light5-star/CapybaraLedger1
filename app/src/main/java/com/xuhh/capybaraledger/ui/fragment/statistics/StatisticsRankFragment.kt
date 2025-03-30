package com.xuhh.capybaraledger.ui.fragment.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import java.util.*
import com.github.mikephil.charting.components.Legend
import com.xuhh.capybaraledger.databinding.FragmentStatisticsBinding
import com.xuhh.capybaraledger.databinding.FragmentStatisticsRankBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment

class StatisticsRankFragment : BaseFragment<FragmentStatisticsRankBinding>() {

    override fun initBinding(): FragmentStatisticsRankBinding {
        return FragmentStatisticsRankBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
    }
} 