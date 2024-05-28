package com.mood.screen.home.share

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.databinding.ActivityShareImageBinding
import com.mood.utils.Constant
import com.mood.utils.RemoteConfigUtil
import com.mood.utils.ShareImageUtil
import com.mood.utils.SharePrefUtils
import com.mood.utils.WRITE_EXTERNAL_STORAGE
import com.mood.utils.checkPermission
import com.mood.utils.checkReadImagePermission
import com.mood.utils.hasReadStoragePermission
import com.mood.utils.isSdkR
import com.mood.utils.setFullScreenMode
import com.mood.utils.setOnSafeClick
import com.mood.utils.setSize
import com.mood.utils.showToast
import com.mood.utils.toBitmap
import java.util.UUID

class ShareImageActivity : BaseActivity<ActivityShareImageBinding>() {
    //region variable
    enum class ScaleImage {
        Basic, Ratio11, Ratio916
    }

    private val byteArray: ByteArray? by lazy {
        intent?.extras?.getByteArray(Constant.IMAGE_SHARE)
    }
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }
    private var bitmap: Bitmap? = null
    private var scaledSelected = ScaleImage.Basic
    private var oldWidth = 0
    private var oldHeight = 0
    private var maxWith = 0
    private var maxHeight = 0
    //endregion

    override fun initView() {
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        bitmap = byteArray?.size?.let { BitmapFactory.decodeByteArray(byteArray, 0, it) }
        binding.imgShare.setImageBitmap(bitmap)
        setupRadioGroup()
    }

    private fun setupRadioGroup() {
        binding.btnBasic.tvRadioButton.text = getString(R.string.scale_basic)
        binding.btnScale11.tvRadioButton.text = getString(R.string.scale_1_1)
        binding.btnScale916.tvRadioButton.text = getString(R.string.scale_9_16)
        setSelectScale(scaledSelected)
    }

    override fun initData() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadPermissionGranted = hasReadStoragePermission()
                isWritePermissionGranted =
                    checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        binding.cardImageShare.apply {
            measure(0, 0)
            oldWidth = this.measuredWidth
            maxWith = this.measuredWidth
            oldHeight = this.measuredHeight
            Log.d(Constant.TAG, "cardImage: $oldWidth - $oldHeight")
        }
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        Log.d(Constant.TAG, "device:$width - $height ")
        binding.layoutToolBar.measure(0, 0)
        binding.layoutScale.measure(0, 0)
        val toolbarHeight = binding.layoutToolBar.measuredHeight
        val bannerHeight =
            if (!SharePrefUtils.isBought() && RemoteConfigUtil.isShowBannerAd) 200 else 0
        val scaleTypeHeight = binding.layoutScale.measuredHeight
        val defaultSpace = 200
        maxHeight = height - toolbarHeight - bannerHeight - scaleTypeHeight - defaultSpace - 40
        Log.d(Constant.TAG, "initData: maxWidth = $maxWith, maxHeight = $maxHeight")

    }

    override fun initListener() {
        handleOnBackPressed()
        binding.btnBack.setOnSafeClick { onBack() }
        binding.layoutBtnBack.setOnSafeClick { onBack() }
        binding.btnSaveImage.setOnSafeClick {
            saveImage()
        }
        binding.layoutSaveImage.setOnSafeClick {
            saveImage()
        }
        binding.btnShareImage.setOnSafeClick {
            shareImage(binding.imgShare)
        }
        binding.layoutShareImage.setOnSafeClick {
            shareImage(binding.imgShare)
        }
        binding.btnBasic.root.setOnSafeClick {
            setSelectScale(ScaleImage.Basic)
            changeImageScale()
        }
        binding.btnScale11.root.setOnSafeClick {
            setSelectScale(ScaleImage.Ratio11)
            changeImageScale()
        }
        binding.btnScale916.root.setOnSafeClick {
            setSelectScale(ScaleImage.Ratio916)
            changeImageScale()
        }
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    private fun changeImageScale() {
        binding.cardImageShare.apply {
            when (scaledSelected) {
                ScaleImage.Basic -> {
                    Log.d(Constant.TAG, "basic:$oldWidth - $oldHeight ")
                    setSize(oldWidth, oldHeight)
                }

                ScaleImage.Ratio11 -> {
                    val newWidth = oldWidth.coerceAtLeast(oldHeight)
                    Log.d(Constant.TAG, "ratio 1-1:$newWidth - $newWidth ")
                    setSize(newWidth, newWidth)
                }

                ScaleImage.Ratio916 -> {
                    if (oldWidth * 16 / 9 > maxHeight) {
                        val newWidth = maxHeight * 9 / 16
                        val newHeight = maxHeight
                        Log.d(Constant.TAG, "ratio 9-16:$newWidth - $newHeight ")
                        setSize(newWidth, newHeight)
                    } else {
                        val newWidth = oldWidth
                        val newHeight = newWidth * 16 / 9
                        Log.d(Constant.TAG, "ratio 9-16:$newWidth - $newHeight ")
                        setSize(newWidth, newHeight)
                    }
                }
            }
        }
    }

    private fun setSelectScale(ratio: ScaleImage) {
        scaledSelected = ratio
        val iconSelect = R.drawable.ic_radio_select
        val iconRegular = R.drawable.ic_radio_regular
        var iconBasic = 0
        var iconScale11 = 0
        var iconScale916 = 0
        when (ratio) {
            ScaleImage.Basic -> {
                iconBasic = iconSelect
                iconScale11 = iconRegular
                iconScale916 = iconRegular
            }

            ScaleImage.Ratio11 -> {
                iconBasic = iconRegular
                iconScale11 = iconSelect
                iconScale916 = iconRegular
            }

            ScaleImage.Ratio916 -> {
                iconBasic = iconRegular
                iconScale11 = iconRegular
                iconScale916 = iconSelect
            }
        }
        binding.btnBasic.imgRadioButton.setBackgroundResource(iconBasic)
        binding.btnScale11.imgRadioButton.setBackgroundResource(iconScale11)
        binding.btnScale916.imgRadioButton.setBackgroundResource(iconScale916)
    }

    private fun shareImage(view: View) {
        val actionShare = {
            viewModel.insertSharingImage(contentResolver, view.toBitmap())?.also { uri ->
                shareImage(uri)
            }
        }
        if (!isSdkR()) {
            if (checkPermission(WRITE_EXTERNAL_STORAGE)) {
                actionShare.invoke()
            } else {
                requestPermissionStorage()
            }
        } else {
            actionShare.invoke()
        }
    }

    private fun saveImage() {
        if (!isSdkR()) {
            if (checkPermission(WRITE_EXTERNAL_STORAGE)) {
                binding.cardImageShare.toBitmap().let {
                    savePhoto(it)
                }
            } else {
                requestPermissionStorage()
            }
        } else {
            savePhoto(binding.cardImageShare.toBitmap())
        }
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = Constant.TYPE_JPEG
        startActivity(Intent.createChooser(intent, getString(R.string.app_name)))
    }

    private fun onBack() {
        finish()
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityShareImageBinding {
        return ActivityShareImageBinding.inflate(inflater)
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false

    private fun requestPermissionStorage() {
        val isWritePermission = checkPermission(WRITE_EXTERNAL_STORAGE)

        val minSdkLevel = isSdkR()

        isReadPermissionGranted = checkReadImagePermission()
        isWritePermissionGranted = isWritePermission || minSdkLevel

        val permissionRequest = mutableListOf<String>()
        if (!isWritePermissionGranted) {
            permissionRequest.add(WRITE_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun savePhoto(bitmapMerge: Bitmap?) {
        val resultSaveImage =
            ShareImageUtil.savePhotoToExternalStorage(
                contentResolver,
                UUID.randomUUID().toString(),
                bitmapMerge
            )
        if (resultSaveImage) {
            showToast(getString(R.string.photo_saved_successfully))
        } else {
            showToast(getString(R.string.failed_to_save_photo))
        }
    }
}