package com.mood.screen.addbean

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.mood.R
import com.mood.databinding.DialogPickImageBinding
import com.mood.utils.GalleryUtils
import com.mood.utils.ImageModel
import com.mood.utils.SharePrefUtils
import com.mood.utils.gone
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.show
import com.mood.utils.showOrGone
import com.mood.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DialogPickImage(val context: Context, val activity: Activity) {
    private val binding by lazy {
        DialogPickImageBinding.inflate(LayoutInflater.from(context))
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
    }

    //    private val pickImageAdapter by lazy {
//        PickImageAdapter()
//    }
    private val pickImageAdapter2 by lazy {
        PickImageAdapterV2()
    }

    fun isShowing() = dialog.isShowing
    fun hide() {
        dialog.dismiss()
    }

    private fun showLoading() {
        binding.tvScanner.show()
        binding.loading.show()
        binding.loading.playAnimation()
    }

    private fun hideLoading() {
        binding.tvScanner.gone()
        binding.loading.gone()
        binding.loading.pauseAnimation()
    }

    @SuppressLint("SetTextI18n")
    fun show(
        limitCount: Int,
        onClickDone: ((listSelected: List<ImageModel>) -> Unit)? = null
    ) {
        binding.tvPremium.showOrGone(!SharePrefUtils.isBought())
        if (!isShowing()) {
            dialog.show()
        }
        binding.layoutBtnClose.setOnSafeClick { hide() }
        binding.btnClose.setOnSafeClick { hide() }
        binding.btnDonePickIcon.setOnSafeClick {
            onClickDone?.invoke(pickImageAdapter2.getAllImageSelected())
            hide()
        }
        dialog.setCancelable(true)
        showLoading()
        binding.tvCountImage.text = "0/$limitCount"
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
//        binding.rcvListImage.setGridManager(context, 3, adapter = pickImageAdapter)
        binding.rcvListImage.setGridManager(context, 3, adapter = pickImageAdapter2)
        pickImageAdapter2.setOnClickItem { item, position ->
            if (pickImageAdapter2.getAllImageSelected().size < limitCount) {
                pickImageAdapter2.setSelectedItem(position)
                binding.tvCountImage.text = "${pickImageAdapter2.getAllImageSelected().size}/$limitCount"
            } else if (item?.isSelected == false) {
                context.showToast(context.getString(R.string.cannot_select_over_limit_image))
            } else if (item?.isSelected == true) {
                pickImageAdapter2.setSelectedItem(position)
                binding.tvCountImage.text = "${pickImageAdapter2.getAllImageSelected().size}/$limitCount"
            }
        }
        Log.d(TAG, "--------start ")
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "--------loading")
            val job = async { GalleryUtils.getAllPhotos(activity) }
//            val job2 = async { GalleryUtils.getMusicFiles(context.contentResolver) }
//            Log.d(TAG, "show: ${job2.await()}")
            val listPhoto = job.await()
            withContext(Dispatchers.Main) {
                hideLoading()
                pickImageAdapter2.setDataList(listPhoto)
            }
            Log.d(TAG, "--------end loading")
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            Log.d(TAG, "--------loading")
//            val job = async { DataUtils.retrieveImagesFromGallery(context.contentResolver) }
//            val list = job.await()
//            withContext(Dispatchers.Main) {
//                pickImageAdapter.setDataList(list.map { ImagePick(it, false) })
//                hideLoading()
//            }
//            Log.d(TAG, "--------end loading")
//        }
        Log.d(TAG, "--------end ")

    }

    val TAG = "doanvvv"
}