package com.mood.screen.setting.content

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.BlockEmojiEntity
import com.mood.data.entity.IconEntity
import com.mood.databinding.ActivitySettingBeanBinding
import com.mood.screen.addbean.AddBeanActivity
import com.mood.screen.setting.content.adapter.BlockIconSettingAdapter
import com.mood.screen.setting.content.adapter.SettingBlockAdapter
import com.mood.screen.setting.content.dialog.DialogAddNewBlock
import com.mood.screen.setting.content.dialog.DialogAddNewIcon
import com.mood.screen.setting.content.dialog.DialogChangeInfoIcon
import com.mood.screen.setting.content.dialog.DialogConfirmExit
import com.mood.screen.setting.content.dialog.DialogConfirmRemoveIcon
import com.mood.screen.setting.content.dialog.DialogMoveBlock
import com.mood.screen.setting.content.entity.BlockIconDetailEntity
import com.mood.utils.Constant
import com.mood.utils.DataUtils
import com.mood.utils.RemoteConfigUtil
import com.mood.utils.SharePrefUtils
import com.mood.utils.openActivity
import com.mood.utils.setFullScreenMode
import com.mood.utils.setGridManager
import com.mood.utils.setOnSafeClick
import com.mood.utils.showOrGone
import com.mood.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections


class SettingBeanActivity : BaseActivity<ActivitySettingBeanBinding>() {
    enum class State {
        None,
        Add,
        Update,
        Delete
    }

    companion object {
        const val TAG = Constant.TAG
        const val TAB_ADD_EDIT = 1
        const val TAB_EDIT_ORDER = 2
    }

    private var listBlock: MutableList<BlockEmojiEntity> = mutableListOf()
    private var listRootBlock: MutableList<BlockEmojiEntity> = mutableListOf()
    private var listIcon: MutableList<IconEntity> = mutableListOf()
    private var listRootIcon: MutableList<IconEntity> = mutableListOf()
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }
    private val blockIconSettingAdapter by lazy {
        BlockIconSettingAdapter()
    }
    private val blockIconHiddenSettingAdapter by lazy {
        BlockIconSettingAdapter()
    }
    private val settingBlockAdapter by lazy {
        SettingBlockAdapter()
    }
    private val dialogAddNewBlock by lazy {
        DialogAddNewBlock(this)
    }
    private val dialogAddNewIcon by lazy {
        DialogAddNewIcon(this)
    }
    private val dialogChangeInfoIcon by lazy {
        DialogChangeInfoIcon(this)
    }
    private val dialogConfirmExit by lazy {
        DialogConfirmExit(this)
    }
    private val dialogMoveBlock by lazy {
        DialogMoveBlock(this)
    }
    private val dialogConfirmRemove by lazy {
        DialogConfirmRemoveIcon(this)
    }
    private val mapBlockState = mutableMapOf<Int, State>()
    private val mapIconState = mutableMapOf<Int, State>()
    private var isEdit = false
    private var isShowTimeSleep: Boolean = SharePrefUtils.getIsShowTimeSleep()
    private var isShowTodayPhoto: Boolean = SharePrefUtils.getIsShowTodayPhoto()
    private var isShowTodayNote: Boolean = SharePrefUtils.getIsShowTodayNote()

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, // Kéo lên và kéo xuống
        0 // Không hỗ trợ kéo sang trái hoặc phải
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            isEdit = true
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            val dataList = settingBlockAdapter.dataList
            if (fromPosition < toPosition) { //Thưc hiện đổi chỗ trong đoạn từ vị trí hiện tại đến vị trí đích
                for (i in fromPosition until toPosition) {
                    val blockId1 = dataList[i].blockEmojiEntity.blockId
                    val blockId2 = dataList[i + 1].blockEmojiEntity.blockId
                    if (mapBlockState[blockId1] != State.Add) {
                        mapBlockState[blockId1] = State.Update
                    }
                    if (mapBlockState[blockId2] != State.Add) {
                        mapBlockState[blockId2] = State.Update
                    }
                    val blockOrder1 = dataList[i].blockEmojiEntity.blockOrder
                    val blockOrder2 = dataList[i + 1].blockEmojiEntity.blockOrder
                    dataList[i].blockEmojiEntity.blockOrder = blockOrder2
                    dataList[i + 1].blockEmojiEntity.blockOrder = blockOrder1
                    Collections.swap(dataList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    val blockId1 = dataList[i].blockEmojiEntity.blockId
                    val blockId2 = dataList[i - 1].blockEmojiEntity.blockId
                    if (mapBlockState[blockId1] != State.Add) {
                        mapBlockState[blockId1] = State.Update
                    }
                    if (mapBlockState[blockId2] != State.Add) {
                        mapBlockState[blockId2] = State.Update
                    }
                    val blockOrder1 = dataList[i].blockEmojiEntity.blockOrder
                    val blockOrder2 = dataList[i - 1].blockEmojiEntity.blockOrder
                    dataList[i].blockEmojiEntity.blockOrder = blockOrder2
                    dataList[i - 1].blockEmojiEntity.blockOrder = blockOrder1
                    Collections.swap(dataList, i, i - 1)
                }
            }
            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Không làm gì khi bị vuốt
        }
    })


    override fun initView() {
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        if (SharePrefUtils.isCustomBackgroundImage()) {
            binding.root.setBackgroundResource(SharePrefUtils.getBackgroundImageApp())
        }
        binding.rcvBlockIcon.setGridManager(this, 1, blockIconSettingAdapter)
        binding.rcvBlockIconHidden.setGridManager(this, 1, blockIconHiddenSettingAdapter)
        binding.layoutEditOrder.rcvBlockOrder.setGridManager(this, 1, settingBlockAdapter)
        itemTouchHelper.attachToRecyclerView(binding.layoutEditOrder.rcvBlockOrder)
        binding.layoutTimeSleep.btnViewTimeSleepHidden.setImageResource(R.drawable.ic_un_view)
        binding.layoutTodayPhoto.btnViewTodayPhoto.setImageResource(R.drawable.ic_un_view)
        binding.layoutTodayNote.btnViewTodayNote.setImageResource(R.drawable.ic_un_view)
        handleViewTimeSleep()
        handleViewTodayPhoto()
        handleViewTodayNote()
    }

    override fun initData() {
        viewModel.getAllBlock { blocks ->
            listRootBlock.clear()
            listRootBlock.addAll(blocks)
            blocks.forEach {
                mapBlockState[it.blockId] = State.None
            }
        }
        queryDatabase()
    }

    private fun queryDatabase() {
        viewModel.getAllIcon { icons ->
            listIcon.clear()
            listIcon.addAll(icons)
            listRootIcon.clear()
            listRootIcon.addAll(icons)
            listIcon.forEach {
                mapIconState[it.iconId] = State.None
            }
            viewModel.getAllBlock { blocks ->
                listBlock.clear()
                listBlock.addAll(blocks)
                setUpDataBlock()
            }
        }
    }

    private fun setUpDataBlock() {
        val dataList = DataUtils.orderBlockIconDetail(listBlock, listIcon, isCheckIconShow = false)
        blockIconSettingAdapter.setDataList(executeAndSetDataListView(dataList))
        val dataList2 = DataUtils.orderBlockIconDetail(listBlock, listIcon, false, isCheckIconShow = false)
        blockIconHiddenSettingAdapter.setDataList(executeAndSetDataListView(dataList2))
        val listBlockSetting = DataUtils.orderBlockIconDetail(listBlock, listIcon)
        settingBlockAdapter.setDataList(listBlockSetting)
    }

    override fun initListener() {
        binding.layoutBtnBlack.setOnSafeClick {
            onBack()
        }
        binding.tabAddEdit.setOnSafeClick {
            editLayoutTab(TAB_ADD_EDIT)
        }
        binding.tabEditOrder.setOnSafeClick {
            editLayoutTab(TAB_EDIT_ORDER)
        }
        blockIconSettingAdapter.onClickEditBlock = {
            showDialogEditBlock(it)
        }
        blockIconSettingAdapter.onClickViewBlock = {
            handleStatusHiddenBlock(it)
        }
        blockIconSettingAdapter.onClickRemoveBlock = {
            handleRemoveBlock(it)
        }
        blockIconSettingAdapter.onClickIcon = { blockId: Int, item: IconEntity, position: Int ->
            Log.d("$TAG - click icon", "$item - position (from 0): $position")
            if (item.isTemp) {//btn add new icon
                showDialogAddIcon(blockId, item.iconId)
            } else {
                showDialogChangeIcon(item, blockId)
            }
        }
        blockIconHiddenSettingAdapter.onClickViewBlock = {
            handleStatusHiddenBlock(it)
        }
        blockIconHiddenSettingAdapter.onClickRemoveBlock = {
            handleRemoveBlock(it)
        }
        blockIconHiddenSettingAdapter.onClickIcon = { blockId: Int, item: IconEntity, position: Int ->
            Log.d("$TAG - click icon", "$item - position (from 0): $position")
        }
        blockIconHiddenSettingAdapter.onClickEditBlock = {
            showDialogEditBlock(it)
        }
        binding.layoutAddBlock.setOnSafeClick {
            showDialogAddBlock()
        }
        binding.btnPlusBlock.setOnSafeClick {
            showDialogAddBlock()
        }
        binding.btnSave.setOnSafeClick {
            saveDataAndBack()
        }
        binding.layoutTimeSleep.btnViewTimeSleepHidden.setOnSafeClick {
            isEdit = true
            isShowTimeSleep = !isShowTimeSleep
            handleViewTimeSleep()
        }
        binding.layoutTimeSleepHidden.btnViewTimeSleepHidden.setOnSafeClick {
            isEdit = true
            isShowTimeSleep = !isShowTimeSleep
            handleViewTimeSleep()
        }
        binding.layoutTodayPhoto.btnViewTodayPhoto.setOnSafeClick {
            isEdit = true
            isShowTodayPhoto = !isShowTodayPhoto
            handleViewTodayPhoto()
        }

        binding.layoutTodayPhotoHidden.btnViewTodayPhoto.setOnSafeClick {
            isEdit = true
            isShowTodayPhoto = !isShowTodayPhoto
            handleViewTodayPhoto()
        }
        binding.layoutTodayNote.btnViewTodayNote.setOnSafeClick {
            isEdit = true
            isShowTodayNote = !isShowTodayNote
            handleViewTodayNote()
        }

        binding.layoutTodayNoteHidden.btnViewTodayNote.setOnSafeClick {
            isEdit = true
            isShowTodayNote = !isShowTodayNote
            handleViewTodayNote()
        }
        settingBlockAdapter.setOnClickItem { item, position ->
            item?.let {
                item.isVisibility = !item.isVisibility
                settingBlockAdapter.notifyItemChanged(position)
            }
        }
        settingBlockAdapter.onUpdateMapStateIcon = { iconId1, iconId2 ->
            isEdit = true
            val icon1 = listIcon.find { it.iconId == iconId1 }
            val icon2 = listIcon.find { it.iconId == iconId2 }
            val orderIcon1 = icon1?.iconOrder
            val orderIcon2 = icon2?.iconOrder
            if (orderIcon2 != null) {
                icon1?.iconOrder = orderIcon2
            }
            if (orderIcon1 != null) {
                icon2?.iconOrder = orderIcon1
            }

            if (mapIconState[iconId1] != State.Add) {
                mapIconState[iconId1] = State.Update
            }
            if (mapIconState[iconId2] != State.Add) {
                mapIconState[iconId2] = State.Update
            }
        }
    }

    private fun handleViewTimeSleep() {
        binding.layoutTimeSleep.root.showOrGone(isShowTimeSleep)
        //hidden block
        binding.layoutTimeSleepHidden.root.showOrGone(!isShowTimeSleep)
    }

    private fun handleViewTodayPhoto() {
        binding.layoutTodayPhoto.root.showOrGone(isShowTodayPhoto)
        //hidden block
        binding.layoutTodayPhotoHidden.root.showOrGone(!isShowTodayPhoto)
    }

    private fun handleViewTodayNote() {
        binding.layoutTodayNote.root.showOrGone(isShowTodayNote)
        //hidden block
        binding.layoutTodayNoteHidden.root.showOrGone(!isShowTodayNote)
    }

    private fun showDialogEditIcon(blockId: Int, iconId: Int, iconName: String? = null, iconUrl: String? = null) {
        DialogAddNewIcon(this).show(iconName, iconUrl) { name, iconSource ->
            isEdit = true
            if (iconName != null) {
                //edit
                if (mapIconState[iconId] != State.Add) {
                    mapIconState[iconId] = State.Update
                }
                listIcon.find { it.iconId == iconId }.apply {
                    this?.iconName = name
                    this?.iconUrl = iconSource
                }
                setUpDataBlock()
            }
        }
    }

    private fun showDialogChangeIcon(iconEntity: IconEntity, blockId: Int) {
        if (!dialogChangeInfoIcon.isShowing()) {
            dialogChangeInfoIcon.show(iconEntity.iconName ?: "",
                iconEntity.iconUrl ?: "R.drawable.ic_icon_5",
                iconEntity.iconIsShow,
                onClickEditIcon = {
                    showDialogEditIcon(
                        blockId,
                        iconEntity.iconId,
                        iconEntity.iconName,
                        iconEntity.iconUrl ?: "R.drawable.ic_icon_5"
                    )
                },
                onClickMoveBlock = {
                    showDialogMoveBlock(iconEntity)
                },
                onClickHideShowIcon = {
                    isEdit = true
                    val iconId = iconEntity.iconId
                    val index = listIcon.indexOfFirst { it.iconId == iconId }
                    if (index != -1 && mapIconState[iconId] != State.Add) {
                        mapIconState[iconId] = State.Update
                        listIcon[index].iconIsShow = !listIcon[index].iconIsShow
                        setUpDataBlock()
                    }
                },
                onClickRemoveIcon = {
                    isEdit = true
                    dialogChangeInfoIcon.hide()
                    //remove temp icon and mark to mapIconState
                    if (mapIconState[iconEntity.iconId] != State.Add) {
                        mapIconState[iconEntity.iconId] = State.Delete
                        listIcon.remove(iconEntity)
                        setUpDataBlock()
                    }
                })
        } else {
            dialogChangeInfoIcon.hide()
        }
    }

    private fun showDialogMoveBlock(iconEntity: IconEntity) {
        if (!dialogMoveBlock.isShowing()) {
            dialogMoveBlock.show(iconEntity.iconBlockId!!, listBlock) { blockIdSelect ->
                blockIdSelect?.let {
                    iconEntity.iconBlockId = it
                    listIcon.findLast { item -> item.iconId == iconEntity.iconId }?.iconBlockId = it
                    if (mapIconState[iconEntity.iconId] != State.Add) {
                        mapIconState[iconEntity.iconId] = State.Update
                    }
                    setUpDataBlock()
                }
            }
        }
    }

    private fun showDialogAddIcon(blockId: Int, iconId: Int, iconName: String? = null, iconUrl: String? = null) {
        if (!dialogAddNewIcon.isShowing()) {
            dialogAddNewIcon.show(iconName, iconUrl) { name, iconSource ->
                isEdit = true
                if (iconName != null) {
                    //edit
                    if (mapIconState[iconId] != State.Add) {
                        mapIconState[iconId] = State.Update
                    }
                    listIcon.find { it.iconId == iconId }.apply {
                        this?.iconName = name
                        this?.iconUrl = iconSource
                    }
                } else {
                    //add icon to block and list icon
                    val id = listIcon.maxOf { it.iconId } + 1
                    val iconOrder = listIcon.maxOf { it.iconOrder } + 1
                    mapIconState[id] = State.Add
                    listIcon.add(IconEntity().apply {
                        this.iconId = id
                        iconBlockId = blockId
                        this.iconName = name
                        this.iconUrl = iconSource
                        this.iconOrder = iconOrder
                    })
                }
                setUpDataBlock()
            }
        }
    }

    private fun handleRemoveBlock(it: BlockIconDetailEntity) {
        if (!dialogConfirmRemove.isShowing()) {
            dialogConfirmRemove.show(getString(R.string.txt_confirm_remove_block)) {
                isEdit = true
                listBlock.remove(it.blockEmojiEntity)
                if (mapBlockState[it.blockEmojiEntity.blockId] != State.Add) {
                    mapBlockState[it.blockEmojiEntity.blockId] = State.Delete
                } else {
                    mapBlockState[it.blockEmojiEntity.blockId] = State.None
                }
                setUpDataBlock()
            }
        }
    }

    private fun handleStatusHiddenBlock(it: BlockIconDetailEntity) {
        isEdit = true
        val blockId = it.blockEmojiEntity.blockId
        val isShow = !it.blockEmojiEntity.blockIsShow
        if (mapBlockState[blockId] != State.Add) {
            mapBlockState[blockId] = State.Update
        }
        listBlock.findLast { item -> item.blockId == blockId }?.blockIsShow = isShow
        setUpDataBlock()
        Log.d("$TAG - view", "$it")
    }

    private fun saveDataAndBack() {
        lifecycleScope.launch {
            //save is show time_sleep, today photo, today note
            SharePrefUtils.saveKey(Constant.IS_SHOW_TIME_SLEEP, isShowTimeSleep)
            SharePrefUtils.saveKey(Constant.IS_SHOW_TODAY_PHOTO, isShowTodayPhoto)
            SharePrefUtils.saveKey(Constant.IS_SHOW_TODAY_NOTE, isShowTodayNote)
            //save in database
            flow {
                var index1 = 0
                var index2 = 0
                var index3 = 0
                var index4 = 0
                // job1
                listBlock.forEachIndexed { index, block ->
                    if (mapBlockState[block.blockId] == State.Add) {
                        viewModel.insertBlock(block)
                    }
                    index1 = index
                }
                //update or delete block
                listRootBlock.forEachIndexed { index, block ->
                    when (mapBlockState[block.blockId]) {
                        State.Update -> {
                            //update name or hide block
                            listBlock.findLast { it.blockId == block.blockId }?.let { viewModel.updateBlock(it) }
                        }

                        State.Delete -> {
                            //delete in table: block
                            viewModel.deleteBlock(block.blockId)
                            //delete all icon (map with block_id) in table: icons (icon_block_id ~ block_id)
                            viewModel.deleteIconByBlockId(block.blockId)
                            //delete all element in table bean_icons (map with icon_id)
                            val listIconRemove = listIcon.filter { it.iconBlockId == block.blockId }
                            listIconRemove.forEach {
                                viewModel.deleteBeanIconByIconId(it.iconId)
                            }
                        }

                        else -> {}
                    }
                    index2 = index
                }

                //job 2
                listIcon.forEachIndexed { index, icon ->
                    if (mapIconState[icon.iconId] == State.Add) {
                        viewModel.insertIcon(icon)
                    }
                    index3 = index
                }
                //update or delete icon
                listRootIcon.forEachIndexed { index, icon ->
                    when (mapIconState[icon.iconId]) {
                        State.Update -> {
                            //update name or hide icon
                            listIcon.findLast { it.iconId == icon.iconId }?.let { viewModel.updateIcon(it) }
                        }

                        State.Delete -> {
                            //delete in table: icon
                            viewModel.deleteIcon(icon.iconId)
                            //delete form bean_icon
                            viewModel.deleteBeanIconByIconId(icon.iconId)
                        }

                        else -> {}
                    }
                    index4 = index
                }
                while (true) {
                    if (index1 == listBlock.size - 1 && index2 == listRootBlock.size - 1
                        && index3 == listIcon.size - 1 && index4 == listRootIcon.size - 1
                    ) {
                        emit(true)
                        break
                    }
                }
            }.flowOn(Dispatchers.IO).collect { isDone ->
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "saveDataAndBack: $isDone")
                    if (isDone) {
                        //hide loading
                        showToast(getString(R.string.update_success))
                        openActivity(AddBeanActivity::class.java, isFinish = true)
                    }
                }
            }

        }
    }

    private fun showDialogEditBlock(blockIconDetailEntity: BlockIconDetailEntity) {
        if (!dialogAddNewBlock.isShowing()) {
            dialogAddNewBlock.show(
                getString(R.string.title_edit_block), blockName = blockIconDetailEntity.blockEmojiEntity
                    .blockName
            ) { name ->
                blockIconDetailEntity.blockEmojiEntity.blockName = name
                mapBlockState[blockIconDetailEntity.blockEmojiEntity.blockId] = State.Update
                val index = listBlock.indexOfFirst { it.blockId == blockIconDetailEntity.blockEmojiEntity.blockId }
                listBlock[index].blockName = name
                setUpDataBlock()
            }
        }
    }

    private fun showDialogAddBlock() {
        if (!dialogAddNewBlock.isShowing()) {
            dialogAddNewBlock.show(getString(R.string.create_new_block), blockName = null) { name ->
                val blockId = listBlock.maxOf { it.blockId } + 1
                mapBlockState[blockId] = State.Add
                val newBlock = BlockEmojiEntity(blockId = blockId, blockName = name,
                    blockOrder = listBlock.maxOf { it.blockOrder } + 1)
                listBlock.add(newBlock)
                setUpDataBlock()
            }
        }
    }

    private fun editLayoutTab(tabAddEdit: Int) {
        val backgroundResourcesTabEdit: Int
        val backgroundResourcesTabEditOrder: Int
        val textColorTabEdit: Int
        val textColorTabEditOrder: Int
        val isShowAddEdit: Boolean
        if (tabAddEdit == TAB_ADD_EDIT) {
            backgroundResourcesTabEdit = R.drawable.background_tab_selected
            backgroundResourcesTabEditOrder = 0
            textColorTabEdit = R.color.green_stroke
            textColorTabEditOrder = R.color.grey_text_tab
            //show tab add, hide tab edit order
            isShowAddEdit = true
            setUpDataBlock()
        } else {
            backgroundResourcesTabEdit = 0
            backgroundResourcesTabEditOrder = R.drawable.background_tab_selected
            textColorTabEdit = R.color.grey_text_tab
            textColorTabEditOrder = R.color.green_stroke
            //hide tab add, show tab edit order
            isShowAddEdit = false
        }
        binding.tabAddEdit.setBackgroundResource(backgroundResourcesTabEdit)
        binding.tabEditOrder.setBackgroundResource(backgroundResourcesTabEditOrder)
        binding.tabAddEdit.setTextColor(ContextCompat.getColor(this, textColorTabEdit))
        binding.tabEditOrder.setTextColor(ContextCompat.getColor(this, textColorTabEditOrder))
        binding.layoutAddEditDelete.showOrGone(isShowAddEdit)
        binding.layoutEditOrder.root.showOrGone(!isShowAddEdit)
    }

    private fun executeAndSetDataListView(list: List<Any>): List<Any> {
        if (!RemoteConfigUtil.isShowNative || SharePrefUtils.isBought()) {
            return list
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
        return newList
    }

    private fun onBack() {
        val actionExit = {
            openActivity(AddBeanActivity::class.java, isFinish = true)
        }
        if (isEdit) {
            dialogConfirmExit.show {
                actionExit()
            }
        } else {
            actionExit()
        }
    }

    override fun onBackPressed() {
        onBack()
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySettingBeanBinding {
        return ActivitySettingBeanBinding.inflate(inflater)
    }
}