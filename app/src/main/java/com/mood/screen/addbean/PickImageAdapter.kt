package com.mood.screen.addbean

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.R
import com.mood.base.BaseAdapterRecyclerView
import com.mood.databinding.LayoutItemPickImageBinding
import com.mood.utils.Constant


class PickImageAdapter : BaseAdapterRecyclerView<ImagePick, LayoutItemPickImageBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): LayoutItemPickImageBinding {
        return LayoutItemPickImageBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutItemPickImageBinding, item: ImagePick, position: Int) {
        val context = binding.root.context
        try {
            if (Constant.mapImageGallery[item.uri] == null) {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, item.uri)
                val bitmapScale = Bitmap.createScaledBitmap(bitmap, 70, 70, false)
                Constant.mapImageGallery[item.uri] = bitmapScale
                binding.imgImagePick.setImageBitmap(bitmapScale)
            } else {
                binding.imgImagePick.setImageBitmap(Constant.mapImageGallery[item.uri])
            }
        } catch (e: Exception) {
            Log.d(Constant.TAG, "bindData: ${e.message}")
        }

        binding.switchPick.setImageResource(
            if (item.isSelected) {
                R.drawable.ic_image_pick
            } else {
                R.drawable.ic_image_none_pick
            }
        )
    }

    fun setSelectedItem(position: Int) {
        dataList[position].isSelected = !dataList[position].isSelected
        notifyItemChanged(position)
    }

    fun getAllImageSelected() = dataList.filter { it.isSelected }
}