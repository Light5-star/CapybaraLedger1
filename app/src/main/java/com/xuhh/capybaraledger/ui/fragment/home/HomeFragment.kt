package com.xuhh.capybaraledger.ui.fragment.home

import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentHomeBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class HomeFragment: BaseFragment<FragmentHomeBinding>() {
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var billAdapter: BillAdapter

    override fun initBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        setDate()
        setQuote()
        setupLedgerSelector()
        setupRecyclerView()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察当前账本
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    mBinding.tvLedgerName.text = it.name
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 观察账单列表
            mViewModel.bills.collect { bills ->
                billAdapter.submitList(bills)
                mBinding.tvEmpty.visibility = if (bills.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 观察余额
            mViewModel.balance.collect { balance ->
                mBinding.tvBalance.text = "今日结余：${String.format("%.2f", balance)}"
            }
        }
    }

    private fun setupLedgerSelector() {
        mBinding.layoutLedger.setOnClickListener {
            LedgerSelectorDialog(
                requireContext(),
                mViewModel
            ) { _ -> 
                // 账本选择的处理已经在 Dialog 中完成
            }.show()
        }
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter { billWithCategory ->
            // 处理账单点击事件
        }

        mBinding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    //设置每日一句
    private fun setQuote() {
        val quotes = resources.getStringArray(R.array.daily_quotes)
        val randomQuote = quotes[Random().nextInt(quotes.size)]
        mBinding.tvQuote.text = randomQuote
    }

    //设置日期
    private fun setDate() {
        val dateFormat = SimpleDateFormat("MM月dd日 E", Locale.CHINESE)
        mBinding.tvDate.text = dateFormat.format(Date())
    }
}