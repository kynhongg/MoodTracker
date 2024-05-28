package com.mood.screen.addbean

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.BeanDailyEntity
import com.mood.data.entity.BeanIconEntity
import com.mood.data.entity.BeanImageAttachEntity
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.databinding.ActivityAddBeanBinding
import com.mood.screen.premium.PremiumActivity
import com.mood.screen.setting.content.SettingBeanActivity
import com.mood.screen.setting.content.dialog.DialogConfirmExit
import com.mood.utils.CalendarUtil
import com.mood.utils.Constant
import com.mood.utils.DataUtils
import com.mood.utils.Define
import com.mood.utils.RemoteConfigUtil
import com.mood.utils.SharePrefUtils
import com.mood.utils.checkReadImagePermission
import com.mood.utils.clear
import com.mood.utils.getActivityResultLauncher
import com.mood.utils.getDataSerializable
import com.mood.utils.gone
import com.mood.utils.hideKeyboard
import com.mood.utils.isSoftKeyboardVisible
import com.mood.utils.loadImage
import com.mood.utils.openActivity
import com.mood.utils.requestPermissionReadStorage
import com.mood.utils.scrollToBottom
import com.mood.utils.setFullScreenMode
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.show
import com.mood.utils.showOrGone
import com.mood.utils.showToast
import com.mood.utils.trackingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis


class AddBeanActivity : BaseActivity<ActivityAddBeanBinding>() {
    companion object {
        const val TAG = Constant.TAG
        const val NUMBER_BEAN_ONE_ROW = 5
        const val NUMBER_DEFAULT_ITEM_ONE_ROW = 1
    }

    private var maxBeanIconId = 0
    private var beanTypeSelect = 1
    private var listBlock: MutableList<BlockEmojiEntity> = mutableListOf()
    private var listIcon: MutableList<IconEntity> = mutableListOf()
    private var listImageAttach: MutableList<BeanImageAttachEntity> = mutableListOf()
    private var listBean: MutableList<BeanDailyEntity> = mutableListOf()

    private var currentDay: Int = 0
    private var currentMonth: Int = 0
    private var currentYear: Int = 0
    private var currentHourSleepStart = 0
    private var currentMinutesSleepStart = 0

    private var beanEdit: BeanDailyEntity? = null
    private val listIconEdit = mutableListOf<BeanIconEntity>()

    private var urlImg1: String? = null
    private var urlImg2: String? = null
    private var urlImg3: String? = null

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isAdd = true
    private var isConfirmUpdate = false

    private val dialogPickDate by lazy {
        DialogPickDate(this)
    }
    private val dialogPickTimeSleep by lazy {
        DialogPickTimeSleep(this)
    }
    private val dialogPickImage by lazy {
        DialogPickImage(this, this)
    }
    private val dialogConfirmExit by lazy {
        DialogConfirmExit(this)
    }
    private val beanTypeAdapter by lazy {
        BeanTypeAdapter()
    }
    private val blockIconAdapter by lazy {
        BlockIconAdapter()
    }
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }

    private var isEdit = false

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideKeyboard()
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView: View = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    override fun initView() {
        binding.loading.show()
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        setupUI(binding.root)
        if (SharePrefUtils.isCustomBackgroundImage()) {
            binding.root.setBackgroundResource(SharePrefUtils.getBackgroundImageApp())
        }
        binding.rcvBeanType.setGridManager(this, NUMBER_BEAN_ONE_ROW, beanTypeAdapter)
        binding.rcvBlockIcon.setGridManager(this, NUMBER_DEFAULT_ITEM_ONE_ROW, blockIconAdapter)
        handleViewTimeSleep()
        handleViewTodayPhoto()
        handleViewTodayNote()
        binding.edtTodayNote.clearFocus()
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.root.isSoftKeyboardVisible()) {
                binding.nestedScrollView.scrollToBottom()
            }
        }
    }

    private fun handleViewTimeSleep() {
        val isShowTimeSleep = SharePrefUtils.getIsShowTimeSleep()
        binding.tvTimeSleep.showOrGone(isShowTimeSleep)
        binding.cardTimeSleep.showOrGone(isShowTimeSleep)
    }

    private fun handleViewTodayPhoto() {
        val isShowTodayPhoto = SharePrefUtils.getIsShowTodayPhoto()
        binding.tvTodayPhoto.showOrGone(isShowTodayPhoto)
        binding.cardTodayPhoto.showOrGone(isShowTodayPhoto)
    }

    private fun handleViewTodayNote() {
        val isShowTodayNote = SharePrefUtils.getIsShowTodayNote()
        binding.tvTodayNote.showOrGone(isShowTodayNote)
        binding.cardTodayNote.showOrGone(isShowTodayNote)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        isAdd = intent?.extras?.getBoolean(Constant.IS_ADD) ?: true
        if (!isAdd) {
            binding.layoutDone.tvButtonDone.text = getString(R.string.save)
        }
        isReadPermissionGranted = checkReadImagePermission()
        permissionLauncher = getActivityResultLauncher { permissions ->
            isReadPermissionGranted = checkReadImagePermission()
        }
//            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//                isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
//                    ?: isReadPermissionGranted
//            }
        /** update targetAPI 33 */
        beanEdit = intent?.extras?.getDataSerializable(Constant.BEAN_EDIT, BeanDailyEntity::class.java)
//        beanEdit = intent?.extras?.getSerializable(Constant.BEAN_EDIT) as? BeanDailyEntity?
        Log.d(TAG, "initData: $beanEdit")
        fillDataEditBean()
        beanTypeSelect = intent?.extras?.getInt(Constant.TYPE) ?: 1
        queryDatabase()
        setUpFirstTitle()
        setUpAdapterBeanType()
    }

    private fun fillDataEditBean() {
        beanEdit?.let {
            beanTypeSelect = it.beanTypeId!!
            viewModel.getAllBeanIconById(it.beanIconId ?: 0) { list ->
                listIconEdit.clear()
                listIconEdit.addAll(list)
                setSelectedIcon()
            }
            binding.edtTodayNote.setText(it.beanDescription)
            if (it.timeGoToBed != null) {
                binding.switchSleep.isChecked = true
                binding.tvTimeStart.text = it.timeGoToBed
                binding.tvTimeEnd.text = it.timeWakeup
            }
            viewModel.getAllBeanImageAttachById(it.beanId) { images ->
                listImageAttach.clear()
                listImageAttach.addAll(images)
                binding.layoutSelectPhoto.showOrGone(images.isEmpty())
                setUpLayoutSelectPhoto(imageCount = images.size, uriList = images.map { item -> item.urlImage })
                when (images.size) {
                    0 -> {}
                    1 -> {
                        urlImg1 = images[0].urlImage
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.gone()
                    }

                    2 -> {
                        urlImg1 = images[0].urlImage
                        urlImg2 = images[1].urlImage
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                    }

                    3 -> {
                        urlImg1 = images[0].urlImage
                        urlImg2 = images[1].urlImage
                        urlImg3 = images[2].urlImage
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                    }
                }
            }
        }
    }

    private fun setSelectedIcon() {
        val dataList = Constant.orderIconByBlock
        dataList.filterIsInstance<List<*>>()
            .forEach { itemList ->
                val icons = itemList.filterIsInstance<IconEntity>().onEach { it.isSelected = false }
                if (!isAdd) {
                    icons.forEach { icon ->
                        if (icon.iconId in listIconEdit.map { it.iconId }) {
                            icon.isSelected = true
                        }
                    }
                }
            }
        executeAndSetDataListView(dataList)
//        blockIconAdapter.setDataList(dataList)
    }

    private fun queryDatabase() {
        viewModel.getAllBean {
            listBean.clear()
            listBean.addAll(it)
        }
        viewModel.getAllIcon { icons ->
            listIcon.clear()
            listIcon.addAll(icons)
            viewModel.getAllBlock { blocks ->
                listBlock.clear()
                listBlock.addAll(blocks)
                Constant.orderIconByBlock = DataUtils.orderIconByBlock(listBlock, listIcon)
                setUpDataBlock()
            }
            binding.loading.gone()
        }
        viewModel.getMaxBeanIconId { id ->
            maxBeanIconId = (id + 1).toInt()
        }
    }

    private fun setUpDataBlock() {
        setSelectedIcon()
    }

    private fun setUpAdapterBeanType() {
        beanTypeAdapter.setDataList(DataUtils.getDataBeanType(1))
        beanTypeAdapter.setSelectedItem(beanTypeSelect - 1)
    }

    private fun setUpFirstTitle() {
        currentDay = intent?.extras?.getInt(Constant.BEAN_DAY) ?: CalendarUtil.getDayInt()
        currentMonth = intent?.extras?.getInt(Constant.BEAN_MONTH) ?: (CalendarUtil.getMonthInt() + 1)
        currentYear = intent?.extras?.getInt(Constant.BEAN_YEAR) ?: CalendarUtil.getYearInt()
        setUpTitle()
    }

    private fun setUpTitle() {
        binding.tvTitleTime.text = CalendarUtil.getTimeFormatDay(this, currentDay, currentMonth - 1, currentYear)
    }

    override fun onStop() {
        super.onStop()
        binding.edtTodayNote.clearFocus()
    }

    override fun initListener() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (dialogPickDate.isShowing()) {
                    dialogPickDate.hide()
                } else if (dialogPickTimeSleep.isShowing()) {
                    dialogPickTimeSleep.hide()
                } else {
                    if (isAdd || isEdit) {
                        dialogConfirmExit.show {
                            onBack()
                        }
                    } else {
                        onBack()
                    }
                }
            }
        })
        binding.edtTodayNote.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                binding.nestedScrollView.scrollToBottom()
            }
        }
        binding.edtTodayNote.setOnClickListener {
            binding.nestedScrollView.scrollToBottom()
        }
        binding.layoutSetting.setOnSafeClick {
            openActivity(SettingBeanActivity::class.java, isFinish = true)
        }
        binding.layoutChooseTime.setOnSafeClick {
            showDialogPickDate()
        }
        blockIconAdapter.setOnClickItem { _, _ ->
            isEdit = true
        }
        beanTypeAdapter.setOnClickItem { _, position ->
            isEdit = true
            beanTypeAdapter.setSelectedItem(position)
            beanTypeSelect = position + 1
        }
        binding.layoutBtnBack.setOnSafeClick {
            onBack()
        }
        binding.btnBack.setOnSafeClick {
            onBack()
        }
        binding.switchSleep.setOnCheckedChangeListener { _, isChecked ->
            isEdit = true
            if (!isChecked) {
                binding.tvTimeStart.clear()
                binding.tvTimeEnd.clear()
            }
        }
        binding.layoutTimeStart.setOnSafeClick {
            if (binding.switchSleep.isChecked) {
                showDialogPickTimeStart()
            }
        }
        binding.layoutTimeEnd.setOnSafeClick {
            if (binding.switchSleep.isChecked) {
                showDialogPickTimeWeak()
            }
        }

        binding.btnAddBean.setOnSafeClick {
            val bedTime = binding.tvTimeStart.text.toString()
            val wakeTime = binding.tvTimeEnd.text.toString()
            if (binding.switchSleep.isChecked) {
                if (bedTime.isEmpty() || wakeTime.isEmpty()) {
                    showToast(getString(R.string.must_select_start_and_end))
                    return@setOnSafeClick
                }
            }
            val exeCuteSave = {
                if (Constant.isShowDialogWatchAds) {
                    WatchAdsDialog(this).also {
                        it.show(onClickWatchAds = {
                            Constant.isShowDialogWatchAds = false
                            saveBean()
                        }, onClickBuyPremium = {
                            openActivity(PremiumActivity::class.java)
                        })
                    }
                } else {
                    saveBean()
                }
            }
            if (isConfirmUpdate) {
                dialogConfirmExit.show(getString(R.string.confirm_overwrite_bean)) {
                    saveBean()
                }
            } else {
                saveBean()
            }
        }
        binding.layoutSelectPhoto.setOnClickListener {
            showDialogPickImage(0)
        }
        binding.imgSelectPhoto.setOnClickListener {
            showDialogPickImage(0)
        }
        binding.imgSelect1.setOnSafeClick {
            showDialogPickImage(1)
        }
        binding.imgSelect2.setOnSafeClick {
            showDialogPickImage(2)
//             else {
////                showToast("Premium feature")
//                GoPremiumDialog(this).also {
//                    it.show {
//                        openActivity(PremiumActivity::class.java)
//                    }
//                }
//            }
        }
        binding.imgSelect3.setOnSafeClick {
            showDialogPickImage(3)
//            else {
////                showToast("Premium feature")
//                GoPremiumDialog(this).also {
//                    it.show {
//                        openActivity(PremiumActivity::class.java)
//                    }
//                }
//            }
        }
        binding.imgRemove1.setOnSafeClick {
            setupClearSelectImage(1)
        }
        binding.imgRemove2.setOnSafeClick {
            setupClearSelectImage(2)
        }
        binding.imgRemove3.setOnSafeClick {
            setupClearSelectImage(3)
        }
    }

    private fun setupClearSelectImage(orderClear: Int) {
        val actionRemove3 = {
            urlImg1 = urlImg2
            urlImg2 = urlImg3
            urlImg3 = null
            setUpLayoutSelectPhoto(imageCount = 2, uriList = listOf(urlImg1, urlImg2))
        }
        val actionRemove2 = {
            urlImg2 = null
            urlImg3 = null
            setUpLayoutSelectPhoto(imageCount = 1, uriList = listOf(urlImg1))
        }
        when (orderClear) {
            1 -> {
                if (urlImg3 != null) {//3 images select
                    actionRemove3.invoke()
                } else if (urlImg2 != null) {//2 images select
                    urlImg1 = urlImg2
                    actionRemove2.invoke()
                } else {// 1 image select
                    urlImg1 = null
                    setUpLayoutSelectPhoto(imageCount = 0, uriList = listOf<String>())
                }
            }

            2 -> {
                if (urlImg3 != null) {
                    urlImg2 = urlImg3
                    urlImg3 = null
                    setUpLayoutSelectPhoto(imageCount = 2, uriList = listOf(urlImg1, urlImg2))
                } else {
                    actionRemove2.invoke()
                }
            }

            3 -> {
                urlImg3 = null
                setUpLayoutSelectPhoto(imageCount = 2, uriList = listOf(urlImg1, urlImg2))
            }
        }
    }

    private fun showDialogPickImage(orderImagePick: Int) {
        //check permission
        if (isReadPermissionGranted) {
            if (!dialogPickImage.isShowing()) {
                isEdit = true
                val limitSelect =
                    if (SharePrefUtils.isBought()) {
                        when (orderImagePick) {
                            0, 1 -> 3
                            2 -> 2
                            3 -> 1
                            else -> 3
                        }
                    } else {
                        1
                    }
                dialogPickImage.show(limitSelect) { images ->
                    setUpLayoutSelectPhoto(orderImagePick, images.size, images.map { it.photoUri })
                }
            }
        } else {
            requestPermissionReadStorage(permissionLauncher)
        }
    }

    private fun setUpLayoutSelectPhoto(orderImagePick: Int = -1, imageCount: Int, uriList: List<String?>) {
        binding.layoutSelectPhoto.showOrGone(imageCount == 0)
        when (orderImagePick) {
            -1 -> {//edit bean, show image select
                when (imageCount) {
                    0 -> {
                        binding.cardImg1.gone()
                        binding.cardImg2.gone()
                        binding.cardImg3.gone()
                        binding.imgRemove1.gone()
                        binding.imgRemove2.gone()
                        binding.imgRemove3.gone()
                        urlImg1 = null
                        urlImg2 = null
                        urlImg3 = null
                    }

                    1 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect1, it)
                            urlImg1 = uriList[0]
                            urlImg2 = null
                            urlImg3 = null
                            loadImage(binding.imgSelect2, R.drawable.ic_add_photo)
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = true
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.gone()
                        binding.imgRemove1.show()
                        binding.imgRemove2.gone()
                        binding.imgRemove3.gone()
                    }

                    2 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect1, it)
                            urlImg1 = it
                        }
                        uriList[1]?.let {
                            loadImage(binding.imgSelect2, it)
                            urlImg2 = it
                            urlImg3 = null
                            loadImage(binding.imgSelect3, R.drawable.ic_add_photo)
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = true
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.gone()
                    }

                    3 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect1, it)
                            urlImg1 = it
                        }
                        uriList[1]?.let {
                            loadImage(binding.imgSelect2, it)
                            urlImg2 = it
                        }
                        uriList[2]?.let {
                            loadImage(binding.imgSelect3, it)
                            urlImg3 = it
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = false
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.show()
                    }
                }
            }

            0 -> {//select base
                when (imageCount) {
                    1 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect1, it)
                            urlImg1 = uriList[0]
                            urlImg2 = null
                            urlImg3 = null
                            loadImage(binding.imgSelect2, R.drawable.ic_add_photo)
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = true
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.gone()
                        binding.imgRemove1.show()
                        binding.imgRemove2.gone()
                        binding.imgRemove3.gone()
                    }

                    2 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect1, it)
                            urlImg1 = it
                        }
                        uriList[1]?.let {
                            loadImage(binding.imgSelect2, it)
                            urlImg2 = it
                            urlImg3 = null
                            loadImage(binding.imgSelect3, R.drawable.ic_add_photo)
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = true
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.gone()
                    }

                    3 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect1, it)
                            urlImg1 = it
                        }
                        uriList[1]?.let {
                            loadImage(binding.imgSelect2, it)
                            urlImg2 = it
                        }
                        uriList[2]?.let {
                            loadImage(binding.imgSelect3, it)
                            urlImg3 = it
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = false
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.show()
                    }
                }
            }

            1 -> {//select at position 1

            }

            2 -> {//select at position 2
                when (imageCount) {
                    1 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect2, it)
                            urlImg2 = it
                            urlImg3 = null
                            binding.imgSelect3.show()
                            loadImage(binding.imgSelect3, R.drawable.ic_add_photo)
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = true
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.gone()
                    }

                    2 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect2, it)
                            urlImg2 = it
                        }
                        uriList[1]?.let {
                            loadImage(binding.imgSelect3, it)
                            urlImg3 = it
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = false
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.show()
                    }
                }
            }

            3 -> {//select at position 3
                when (imageCount) {
                    1 -> {
                        uriList[0]?.let {
                            loadImage(binding.imgSelect3, it)
                            urlImg3 = it
                            binding.imgSelect1.isEnabled = false
                            binding.imgSelect2.isEnabled = false
                            binding.imgSelect3.isEnabled = false
                        }
                        binding.cardImg1.show()
                        binding.cardImg2.show()
                        binding.cardImg3.show()
                        binding.imgRemove1.show()
                        binding.imgRemove2.show()
                        binding.imgRemove3.show()
                    }
                }
            }
        }
    }

    private fun saveBean() {
        val x = Constant.isCountClickSave.value ?: 0
        Constant.isCountClickSave.value = x + 1
        Log.d(TAG, "----------------start save info----------------")
        runBlocking {
            Log.d(TAG, ">>>>start blocking")
            val time = measureTimeMillis {
                if (isAdd) {
                    Log.d(TAG, ">>>>start add bean record")
                    //insert Bean
                    val job1 = async { insertNewBeanToDatabase() }
                    //insert bean_icon
                    val job2 = async { insertBeanIconsToDatabase() }
                    //insert bean attach_image
                    val job3 = async { insertBeanImageAttachToDatabase() }
                    job1.await()
                    job2.await()
                    job3.await()
                    showToast(getString(R.string.add_record_success))
                    Define.CLICK_SAVE_RECORD.trackingEvent()
                    onBack()
                    Log.d(TAG, ">>>>end add bean record")
                } else {
                    Log.d(TAG, ">>>>before edit record")
                    val note = binding.edtTodayNote.text.toString()
                    beanEdit?.apply {
                        this.beanTypeId = beanTypeSelect
                        beanDescription = note.ifEmpty { null }
                        timeGoToBed = if (binding.switchSleep.isChecked) binding.tvTimeStart.text.toString() else null
                        timeWakeup = if (binding.switchSleep.isChecked) binding.tvTimeEnd.text.toString() else null
                    }
                    val job1 = async {
                        beanEdit?.let { it1 -> viewModel.updateBean(it1) }
                    }
                    //remove icons old
                    val job2 = async {
                        withContext(Dispatchers.IO) {
                            val listIconIdSelected = blockIconAdapter.getAllIconSelected()
                            listIconEdit.removeAll { iconEdit ->
                                listIconIdSelected.any { it.iconId == iconEdit.iconId }
                            }
                            listIconEdit.forEach { iconEdit ->
                                iconEdit.iconId?.let { iconId ->
                                    beanEdit?.beanIconId?.let { beanIconId ->
                                        viewModel.deleteBeanIconByIconAndBean(iconId, beanIconId)
                                    }
                                }
                            }
                        }
                    }
                    //insert new bean <-> icon to bean_icon
                    val job3 = async {
                        insertBeanIconsToDatabase()
                    }
                    val job4 = async {
                        insertBeanImageAttachToDatabase()
                    }
                    job1.await()
                    job2.await()
                    job3.await()
                    job4.await()
                    Log.d(TAG, ">>>>end edit record")
                    showToast(getString(R.string.edit_record_success))
                    Define.CLICK_DONE_EDIT_RECORD.trackingEvent()
                    onBack()
                }
            }
            val addOrEdit = if (isAdd) "add" else "edit"
            Log.d(TAG, ">>>>$addOrEdit bean with $time ms")
        }
        Log.d(TAG, "----------------done execute database -> back----------------")
    }

    private suspend fun insertBeanIconsToDatabase() = withContext(Dispatchers.IO) {
        val listIconIdSelected = blockIconAdapter.getAllIconSelected()
        val listBeanIcon = mutableListOf<BeanIconEntity>()
        listIconIdSelected.forEach { icon ->
            listBeanIcon.add(BeanIconEntity().apply {
                this.beanIconId = if (isAdd) {
                    maxBeanIconId
                } else {
                    beanEdit?.beanIconId
                }
                this.iconId = icon.iconId
            })
        }
        listBeanIcon.forEach { beanIcon ->
            viewModel.insertBeanIcon(beanIcon)
        }
    }

    private suspend fun insertNewBeanToDatabase() = withContext(Dispatchers.IO) {
        val note = binding.edtTodayNote.text.toString()
        val newBean = BeanDailyEntity()
        newBean.apply {
            this.beanTypeId = beanTypeSelect
            year = currentYear
            month = currentMonth
            day = currentDay
            hour = CalendarUtil.getCurrentHour()
            minutes = CalendarUtil.getCurrentMinutes()
            beanIconId = maxBeanIconId
            beanDescription = note.ifEmpty { null }
            timeGoToBed = if (binding.switchSleep.isChecked) binding.tvTimeStart.text.toString() else null
            timeWakeup = if (binding.switchSleep.isChecked) binding.tvTimeEnd.text.toString() else null
        }
        Log.d(TAG, "insert new bean: $newBean")
        viewModel.insertBean(newBean)
    }

    private suspend fun insertBeanImageAttachToDatabase() = withContext(Dispatchers.IO) {
        val beanId = if (isAdd) {
            maxBeanIconId
        } else {
            beanEdit?.beanIconId
        }
        val listImageAttachEntity = mutableListOf<BeanImageAttachEntity>()
        urlImg1?.let { url ->
            listImageAttachEntity.add(BeanImageAttachEntity().apply {
                this.beanId = beanId
                this.urlImage = url
            })
        }
        urlImg2?.let { url ->
            listImageAttachEntity.add(BeanImageAttachEntity().apply {
                this.beanId = beanId
                this.urlImage = url
            })
        }
        urlImg3?.let { url ->
            listImageAttachEntity.add(BeanImageAttachEntity().apply {
                this.beanId = beanId
                this.urlImage = url
            })
        }
        viewModel.deleteBeanImageAttachById(beanId!!) {
            listImageAttachEntity.forEach { imgAttach ->
                viewModel.insertBeanImageAttach(imgAttach)
            }
        }
    }

    private fun showDialogPickTimeStart() {
        if (!dialogPickTimeSleep.isShowing()) {
            dialogPickTimeSleep.show(getString(R.string.pick_time_sleep_start)) { hour, minutes ->
                isEdit = true
                currentHourSleepStart = hour
                currentMinutesSleepStart = minutes
                binding.tvTimeStart.text = CalendarUtil.formatTime(hour, minutes)
            }
        }
    }

    private fun showDialogPickTimeWeak() {
        if (!dialogPickTimeSleep.isShowing()) {
            dialogPickTimeSleep.show(getString(R.string.pick_time_sleep_start)) { hour, minutes ->
                isEdit = true
                binding.tvTimeEnd.text = CalendarUtil.formatTime(hour, minutes)
            }
        }
    }

    private fun showDialogPickDate() {
        if (!dialogPickDate.isShowing()) {
            dialogPickDate.show(
                currentDay, currentMonth - 1, currentYear, true,
                onClickSubmit = { day, month, year ->
                    val isFeature = CalendarUtil.checkFeatures(day, month + 1, year)
                    if (!isFeature) {
                        currentDay = day
                        currentMonth = month + 1
                        currentYear = year
                        setUpTitle()
                        val beanDaySelect = listBean.find {
                            it.day == currentDay && it.month == currentMonth && it.year ==
                                    currentYear
                        }
                        beanDaySelect?.let {
                            isConfirmUpdate = true
                            isAdd = false
                            beanEdit = it
                            fillDataEditBean()
                            beanTypeAdapter.setSelectedItem(beanTypeSelect - 1)
                            setUpDataBlock()
                        } ?: kotlin.run {
                            isConfirmUpdate = false
                            isAdd = true
                            listIconEdit.clear()
                            resetAddBean()
                        }
                        fillDataEditBean()
                    } else {
                        showToast(getString(R.string.cannot_choose_future))
                    }
                })
        }
    }

    private fun resetAddBean() {
        beanTypeSelect = 1
        beanTypeAdapter.setSelectedItem(beanTypeSelect - 1)
        setUpDataBlock()
        binding.switchSleep.isChecked = false
        setUpLayoutSelectPhoto(-1, 0, listOf())
        binding.edtTodayNote.clear()
        beanEdit = null
    }

    private fun executeAndSetDataListView(list: List<Any>) {
        if (!RemoteConfigUtil.isShowNative || SharePrefUtils.isBought()) {
            blockIconAdapter.setDataList(list)
            return
        }
        val newList = mutableListOf<Any>()
        var indexAds = 10
        list.forEachIndexed { index, message ->
            if (index == indexAds) {
                indexAds += 10
                newList.add(0)
                newList.add(message)
            } else {
                newList.add(message)
            }
        }
        blockIconAdapter.setDataList(newList)
    }

    private fun onBack() {
        finish()
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityAddBeanBinding {
        return ActivityAddBeanBinding.inflate(inflater)
    }

    override fun onResume() {
        super.onResume()
        if (binding.root.isSoftKeyboardVisible()) {
            binding.nestedScrollView.scrollToBottom()
        }
    }
}