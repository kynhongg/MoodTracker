package com.mood.screen.report

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.ItemLayoutReportBeanBinding

class ReportBeanTypeAdapter : BaseAdapterRecyclerView<NumberBeanEntity, ItemLayoutReportBeanBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemLayoutReportBeanBinding {
        return ItemLayoutReportBeanBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemLayoutReportBeanBinding, item: NumberBeanEntity, position: Int) {
        binding.tvCountBean.text = item.count.toString()
        binding.imgTypeBean.setImageResource(item.type.icon)
    }
}