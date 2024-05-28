package com.mood.screen.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mood.base.BaseAdapterRecyclerView
import com.mood.data.entity.BeanDefaultEmoji
import com.mood.databinding.ItemLayoutTimelineBinding
import com.mood.screen.home.calendar.BeanIconAdapter
import com.mood.utils.CalendarUtil
import com.mood.utils.gone
import com.mood.utils.loadImage
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.show
import com.mood.utils.showOrGone

class TimeLineAdapter : BaseAdapterRecyclerView<BeanIconDetailEntity, ItemLayoutTimelineBinding>() {
    var onClickShareBean: ((itemView: View, BeanIconDetailEntity) -> Unit)? = null
    var onClickEditBean: ((BeanIconDetailEntity) -> Unit)? = null
    var onClickRemoveBean: ((BeanIconDetailEntity) -> Unit)? = null

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemLayoutTimelineBinding {
        return ItemLayoutTimelineBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemLayoutTimelineBinding, item: BeanIconDetailEntity, position: Int) {
        val context = binding.root.context
        val beanIconAdapter = BeanIconAdapter()
        binding.rcvIconBean.setGridManager(context, 7, beanIconAdapter)
        beanIconAdapter.setDataList(item.listIcon)
        val index = item.beanDailyEntity.beanTypeId ?: 0
        binding.imgBean.setImageResource(BeanDefaultEmoji.getImageIdByIndex(index))
        binding.layoutBean.apply {
            show()
            setBackgroundResource(BeanDefaultEmoji.getBackgroundResourceByIndex(index))
        }
        binding.tvTimeCreateBean.text = CalendarUtil.getNameOfDay(
            context, item.beanDailyEntity.day, item.beanDailyEntity.month,
            item.beanDailyEntity.year
        )

        //attach image
        val listImageAttach = item.listImageAttach.filter {
            it.beanId == item.beanDailyEntity.beanId
        }
        val isShow =
            !(listImageAttach.isEmpty() && item.listIcon.isEmpty() && (item.beanDailyEntity.beanDescription.isNullOrEmpty()))
        val txtStatus = BeanDefaultEmoji.getStatusByIndex(context, index)
        binding.tvBeanStatus.apply {
            text = txtStatus
            showOrGone(isShow)
        }
        binding.tvBeanStatus2.apply {
            text = txtStatus
            showOrGone(!isShow)
        }
        binding.tvBeanContent.apply {
            text = item.beanDailyEntity.beanDescription
            showOrGone(item.beanDailyEntity.beanDescription?.isNotEmpty() == true)
        }
        binding.rcvIconBean.showOrGone(item.listIcon.isNotEmpty())
        binding.lineHorizontal.showOrGone(isShow)
        binding.imgRemoveBean.showOrGone(isShow)
        binding.imgRemoveBean2.showOrGone(!isShow)
        binding.imgEditBean.showOrGone(isShow)
        binding.imgEditBean2.showOrGone(!isShow)
        binding.imgShareBean.showOrGone(isShow)
        binding.imgShareBean2.showOrGone(!isShow)
        val imageViews = arrayOf(binding.imgChoose1, binding.imgChoose2, binding.imgChoose3)
        val cardImageViews = arrayOf(binding.cardImg1, binding.cardImg2, binding.cardImg3)
        for (i in imageViews.indices) {
            if (i < listImageAttach.size) {
                cardImageViews[i].show()
                val url = listImageAttach[i].urlImage ?: ""
                //new solution load image
                context.loadImage(imageViews[i], url)
//                val uri = Uri.parse(url)
//                if (Constant.mapImageGallery[uri] == null) {
//                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
//                    val bitmapScale = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
//                    Constant.mapImageGallery[uri] = bitmapScale
//                    imageViews[i].setImageBitmap(bitmapScale)
//                } else {
//                    imageViews[i].setImageBitmap(Constant.mapImageGallery[uri])
//                }
            } else {
                cardImageViews[i].gone()
            }
        }
        binding.imgShareBean.setOnSafeClick { onClickShareBean?.invoke(binding.root, item) }
        binding.imgShareBean2.setOnSafeClick { onClickShareBean?.invoke(binding.root, item) }
        binding.imgEditBean.setOnSafeClick { onClickEditBean?.invoke(item) }
        binding.imgEditBean2.setOnSafeClick { onClickEditBean?.invoke(item) }
        binding.imgRemoveBean.setOnSafeClick { onClickRemoveBean?.invoke(item) }
        binding.imgRemoveBean2.setOnSafeClick { onClickRemoveBean?.invoke(item) }
    }
}