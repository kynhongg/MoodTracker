package com.mood.screen.sync

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.gson.Gson
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.*
import com.mood.databinding.ActivitySyncDataBinding
import com.mood.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class SyncDataActivity : BaseActivity<ActivitySyncDataBinding>() {

    companion object {
        const val REQ_ONE_TAP = 1111
        const val PREFIX_BEAN_DAILY = "beans"
        const val PREFIX_BEAN_ICON = "bean_icons"
        const val PREFIX_BEAN_IMAGE_ATTACH = "bean_image_attach"
        const val PREFIX_BLOCK = "blocks"
        const val PREFIX_ICON = "icons"
        const val PREFIX_MUSIC_CALM = "music_calms"

        val MAP_LOADING_DEFAULT: MutableMap<String, Boolean> = mutableMapOf(
            PREFIX_BEAN_DAILY to false,
            PREFIX_BEAN_ICON to false,
            PREFIX_BEAN_IMAGE_ATTACH to false,
            PREFIX_BLOCK to false,
            PREFIX_ICON to false,
            PREFIX_MUSIC_CALM to false,
        )
        const val TXT_TYPE = ".txt"
        const val TAG = Constant.TAG
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private var userId = ""

    private var mapLoadingUpload = MAP_LOADING_DEFAULT
    private var mapLoadingDownload = MAP_LOADING_DEFAULT

    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }

    private var allBeanDailyEntity: MutableList<BeanDailyEntity> = mutableListOf()
    private var allBeanIconEntity: MutableList<BeanIconEntity> = mutableListOf()
    private var allBeanImageAttachEntity: MutableList<BeanImageAttachEntity> = mutableListOf()
    private var allBlockEntity: MutableList<BlockEmojiEntity> = mutableListOf()
    private var allIconEntity: MutableList<IconEntity> = mutableListOf()
    private var allMusicCalmEntity: MutableList<MusicCalmEntity> = mutableListOf()

    override fun initView() {
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        auth = Firebase.auth
    }

    override fun initData() {
        getAllDataBase()
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = getBeginSignInRequest(true)
    }

    override fun initListener() {
        binding.btnBack.setOnClickListener {
            onBack()
        }
        binding.btnSignInGoogle.setOnClickListener {
            signInGoogle()
            setUpButton(it, 0.4f, false)
        }
        binding.btnLogOut.setOnClickListener { logOutAccountGoogle() }
        binding.btnSyncNow.setOnClickListener { syncData() }
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySyncDataBinding {
        return ActivitySyncDataBinding.inflate(inflater)
    }

    private fun getAllDataBase() {
        viewModel.apply {
            getAllBean {
                allBeanDailyEntity.clear()
                allBeanDailyEntity.addAll(it)
            }
            getAllBeanIcon {
                allBeanIconEntity.clear()
                allBeanIconEntity.addAll(it)
            }
            getAllBeanImageAttach {
                allBeanImageAttachEntity.clear()
                allBeanImageAttachEntity.addAll(it)
            }
            getAllBlock {
                allBlockEntity.clear()
                allBlockEntity.addAll(it)
            }
            getAllIcon {
                allIconEntity.clear()
                allIconEntity.addAll(it)
            }
            getAllMusicCalm {
                allMusicCalmEntity.clear()
                allMusicCalmEntity.addAll(it)
            }
        }
    }

    private fun setUpButton(view: View, alpha: Float, isClickAble: Boolean) {
        with(binding) {
            view.alpha = alpha

            view.isClickable = isClickAble

            view.isEnabled = isClickAble
        }
    }

    private fun syncData() {
        initLoadingAndDownload()
    }

    private fun initLoadingAndDownload() {
        val loading = LoadingDialog(this@SyncDataActivity).also {
            it.setViewWithState(false)
            it.show {
                finish()
                setResult(RESULT_OK)
            }
        }
        downloadFileFromServer(loading, true)
    }

    //region download and sync database
    private fun downloadFileFromServer(loading: LoadingDialog, isUploadAndSync: Boolean = true) {
        val path = filesDir
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val nameOfFileBeanDaily = userId + PREFIX_BEAN_DAILY
        val nameOfFileBeanIcon = userId + PREFIX_BEAN_ICON
        val nameOfFileBlock = userId + PREFIX_BLOCK
        val nameOfFileBeanImageAttach = userId + PREFIX_BEAN_IMAGE_ATTACH
        val nameOfFileMusicCalm = userId + PREFIX_MUSIC_CALM
        val nameOfFileIcon = userId + PREFIX_ICON
        val fileBeanDaily = File(letDirectory, nameOfFileBeanDaily + TXT_TYPE)
        val fileBeanIcon = File(letDirectory, nameOfFileBeanIcon + TXT_TYPE)
        val fileBlock = File(letDirectory, nameOfFileBlock + TXT_TYPE)
        val fileBeanImageAttach = File(letDirectory, nameOfFileBeanImageAttach + TXT_TYPE)
        val fileMusicCalm = File(letDirectory, nameOfFileMusicCalm + TXT_TYPE)
        val fileIcon = File(letDirectory, nameOfFileIcon + TXT_TYPE)
        CoroutineScope(Dispatchers.Default).launch {
            mapLoadingUpload = MAP_LOADING_DEFAULT
            if (isUploadAndSync) {
                mapLoadingDownload = MAP_LOADING_DEFAULT
            }
            val job = async {
                downloadFile(fileBeanDaily, nameOfFileBeanDaily, loading, isUploadAndSync)
                downloadFile(fileBeanIcon, nameOfFileBeanIcon, loading, isUploadAndSync)
                downloadFile(fileBlock, nameOfFileBlock, loading, isUploadAndSync)
                downloadFile(fileBeanImageAttach, nameOfFileBeanImageAttach, loading, isUploadAndSync)
                downloadFile(fileMusicCalm, nameOfFileMusicCalm, loading, isUploadAndSync)
                downloadFile(fileIcon, nameOfFileIcon, loading, isUploadAndSync)
            }
            job.await()
        }
    }

    private fun downloadFile(localFile: File, fileName: String, loading: LoadingDialog, isUploadAndSync: Boolean = false) {
        localFile.createNewFile()
        Log.d(TAG, "downloading: $fileName")
        val storageRef = FirebaseStorage.getInstance().reference
        val path = "database/$fileName"
        storageRef.child(path).getFile(localFile)
            .addOnSuccessListener {
                if (isUploadAndSync) {
                    mapLoadingDownload[fileName.substring(userId.length)] = true
                    if (!mapLoadingDownload.values.any { !it } && !mapLoadingUpload.values.any { !it }) {
                        loading.setViewWithState(true)
                    }
                }
                syncDatabaseWithNewData(localFile, fileName, loading, isUploadAndSync)
            }
            .addOnFailureListener {
                Log.d(TAG, "onFailedDownload: ${it.message} - ${it.printStackTrace()}")
                runBlocking {
                    createFileTxtAndUploadToServer(fileName, fileName.substring(userId.length), loading)
                }
            }
    }

    private fun syncDatabaseWithNewData(
        localFile: File,
        fileName: String,
        loading: LoadingDialog,
        isUploadAndSync: Boolean = false
    ) {
        Log.d(TAG, "----------------syncDatabase $fileName-------------------")
        val bufferReader = BufferedReader(FileReader(localFile))
        val dataJson = bufferReader.readLine()
        bufferReader.close()
        var list: List<Any>? = listOf()
        when (fileName) {
            userId + PREFIX_BEAN_DAILY -> {
                list = Gson().fromJson(dataJson, Array<BeanDailyEntity>::class.java).toList()
            }

            userId + PREFIX_BEAN_ICON -> {
                list = Gson().fromJson(dataJson, Array<BeanIconEntity>::class.java).toList()
            }

            userId + PREFIX_BLOCK -> {
                list = Gson().fromJson(dataJson, Array<BlockEmojiEntity>::class.java).toList()
            }

            userId + PREFIX_BEAN_IMAGE_ATTACH -> {
                list = Gson().fromJson(dataJson, Array<BeanImageAttachEntity>::class.java).toList()
            }

            userId + PREFIX_MUSIC_CALM -> {
                list = Gson().fromJson(dataJson, Array<MusicCalmEntity>::class.java).toList()
            }

            userId + PREFIX_ICON -> {
                list = Gson().fromJson(dataJson, Array<IconEntity>::class.java).toList()
            }
        }
        insertNewDataToDatabase(list, fileName, loading, isUploadAndSync)
    }

    private fun insertNewDataToDatabase(
        list: List<Any>?,
        fileName: String,
        loading: LoadingDialog,
        isUploadAndSync: Boolean = false
    ) =
        CoroutineScope(Dispatchers.IO).launch {
            when (fileName) {
                userId + PREFIX_BEAN_DAILY -> {
                    list?.forEach {
                        val beanDailyEntity = it as BeanDailyEntity
                        if (!existBeanDailyEntity(beanDailyEntity)) {
                            viewModel.insertBean(beanDailyEntity)
                        } else {
                            viewModel.updateBean(beanDailyEntity)
                        }
                    }
                    if (isUploadAndSync) {
                        createFileTxtAndUploadToServer(fileName, PREFIX_BEAN_DAILY, loading)
                    }
                }

                userId + PREFIX_BEAN_ICON -> {
                    list?.forEach {
                        val beanIcon = it as BeanIconEntity
                        if (!existBeanIconEntity(beanIcon)) {
                            viewModel.insertBeanIcon(beanIcon)
                        }
                    }
                    if (isUploadAndSync) {
                        createFileTxtAndUploadToServer(fileName, PREFIX_BEAN_ICON, loading)
                    }
                }

                userId + PREFIX_BLOCK -> {
                    list?.forEach {
                        val blockEntity = it as BlockEmojiEntity
                        if (!existBlock(blockEntity)) {
                            viewModel.insertBlock(blockEntity)
                        } else {
                            viewModel.updateBlock(blockEntity)
                        }
                    }
                    if (isUploadAndSync) {
                        createFileTxtAndUploadToServer(fileName, PREFIX_BLOCK, loading)
                    }
                }

                userId + PREFIX_BEAN_IMAGE_ATTACH -> {
//                    Log.d(TAG, "insert to bean image attach TABLE: ${list?.size}")
                    list?.forEach {
                        if (!existBeanImageAttach(it as BeanImageAttachEntity)) {
                            viewModel.insertBeanImageAttach(it)
                        }
                    }
                    if (isUploadAndSync) {
                        createFileTxtAndUploadToServer(fileName, PREFIX_BEAN_IMAGE_ATTACH, loading)
                    }
                }

                userId + PREFIX_MUSIC_CALM -> {
                    list?.forEach {
                        val musicCalmEntity = it as MusicCalmEntity
                        if (!existMusicCalm(musicCalmEntity)) {
                            viewModel.insertMusicCalm(musicCalmEntity)
                        } else {
                            viewModel.updateMusicCalm(musicCalmEntity)
                        }
                    }
                    if (isUploadAndSync) {
                        createFileTxtAndUploadToServer(fileName, PREFIX_MUSIC_CALM, loading)
                    }
                }

                userId + PREFIX_ICON -> {
                    list?.forEach {
                        if (!existIcon(it as IconEntity)) {
                            viewModel.insertIcon(it)
                        } else {
                            viewModel.updateIcon(it)
                        }
                    }
                    if (isUploadAndSync) {
                        createFileTxtAndUploadToServer(fileName, PREFIX_ICON, loading)
                    }
                }
            }
        }
    //endregion

    //region check data wit database
    private fun existBeanDailyEntity(beanDailyEntity: BeanDailyEntity) = allBeanDailyEntity.any { item ->
        item.beanId == beanDailyEntity.beanId
    }

    private fun existBeanIconEntity(beanIcon: BeanIconEntity) = allBeanIconEntity.any { item ->
        item.beanIconId == beanIcon.beanIconId && item.iconId == beanIcon.iconId
    }

    private fun existBeanImageAttach(beanImageAttachEntity: BeanImageAttachEntity) = allBeanImageAttachEntity.any { item ->
        item.beanId == beanImageAttachEntity.beanId
                && item.urlImage == beanImageAttachEntity.urlImage
    }

    private fun existBlock(blockEntity: BlockEmojiEntity) = allBlockEntity.any { item ->
        item.blockId == blockEntity.blockId
    }

    private fun existIcon(iconEntity: IconEntity) = allIconEntity.any { item ->
        item.iconId == iconEntity.iconId
    }

    private fun existMusicCalm(musicCalmEntity: MusicCalmEntity) = allMusicCalmEntity.any { item ->
        item.year == musicCalmEntity.year && item.month == musicCalmEntity.month
    }
    //endregion

    //region convert database to json, insert to file *.txt and upload to firebase storage
    private suspend fun createFileTxtAndUploadToServer(fileName: String, prefix: String, loading: LoadingDialog) =
        withContext(Dispatchers.IO) {
            val job = async { convertDatabaseToJson(prefix) }
            val content = job.await()
            Log.d(TAG, "uploading - $prefix: $content")
            val storageRef = FirebaseStorage.getInstance().reference
            val path = "database/${fileName}"
            val contentType = "text/plain"
            val fileDbRef = storageRef.child(path)
            val mediaData = StorageMetadata.Builder().setContentType(contentType).build()
            fileDbRef.putBytes(content.toByteArray(), mediaData)
                .addOnSuccessListener {
                    Log.d(TAG, "uploaded - $prefix")
                    mapLoadingUpload[prefix] = true
                    if (!mapLoadingDownload.values.any { !it } && !mapLoadingUpload.values.any { !it }) {
                        loading.setViewWithState(true)
                    }
                }.addOnFailureListener {
                    mapLoadingUpload[prefix] = false
                    loading.setError()
                    Log.d(TAG, "uploadDbWithFirebase: ${it.message}")
                }
        }

    private suspend fun convertDatabaseToJson(prefix: String): String = withContext(Dispatchers.IO) {
        val list: MutableList<Any> = mutableListOf()
        when (prefix) {
            PREFIX_BEAN_DAILY -> {
                list.addAll(allBeanDailyEntity)
            }

            PREFIX_BEAN_ICON -> {
                list.addAll(allBeanIconEntity)
            }

            PREFIX_BLOCK -> {
                list.addAll(allBlockEntity)
            }

            PREFIX_BEAN_IMAGE_ATTACH -> {
                list.addAll(allBeanImageAttachEntity)
            }

            PREFIX_MUSIC_CALM -> {
                list.addAll(allMusicCalmEntity)
            }

            PREFIX_ICON -> {
                list.addAll(allIconEntity)
            }
        }

        return@withContext Gson().toJson(list)
    }
    //endregion

    //region SignIn with google, setup UI
    private fun logOutAccountGoogle() {
        Firebase.auth.signOut()
        updateUI(null)
    }

    @Suppress("DEPRECATION")
    private fun signInGoogle() {
        signInRequest = getBeginSignInRequest()
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    setUpButton(binding.btnSignInGoogle, 1f, true)
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                e.localizedMessage?.let { Log.d(TAG, it) }
                setUpButton(binding.btnSignInGoogle, 1f, true)
            }
    }

    private fun getBeginSignInRequest(isFilterByAuthorizedAccount: Boolean = false) = BeginSignInRequest.builder()
        .setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build()
        )
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(isFilterByAuthorizedAccount)
                .build()
        )
        .build()

    private fun updateUI(user: FirebaseUser?) {
        setUpButton(binding.btnSignInGoogle, 1f, true)
        user?.apply {
            binding.layoutInformation.show()
            binding.layoutDescription.gone()
            photoUrl?.let {
                this@SyncDataActivity.loadImage(binding.imageAvatar, it)
            }
            binding.tvName.text = displayName
            binding.tvEmail.text = email
            binding.btnSignInGoogle.gone()
//            binding.textContent1.gone()
            binding.btnSyncNow.show()
            binding.btnLogOut.show()
            userId = email?.substring(0, (email?.length ?: 10) - 10).toString()
        } ?: kotlin.run {
            binding.layoutInformation.gone()
            binding.layoutDescription.show()
            binding.btnSignInGoogle.show()
//            binding.textContent1.show()
            binding.btnSyncNow.gone()
            binding.btnLogOut.gone()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = googleCredential.googleIdToken
                when {
                    idToken != null -> {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success - ${auth.currentUser.toString()}")
                                    showToast("${getString(R.string.login_success)} ${auth.currentUser?.email}")
                                    updateUI(auth.currentUser)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    showToast(getString(R.string.login_failed))
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                                    updateUI(null)
                                }
                            }
                    }

                    else -> {
                        // Shouldn't happen.
                        showToast("No ID token!")
                        Log.d(TAG, "No ID token!")
                        setUpButton(binding.btnSignInGoogle, 1f, true)
                    }
                }
            } catch (ex: Exception) {
                Log.d(TAG, "on  result exception: ${ex.printStackTrace()}")
                setUpButton(binding.btnSignInGoogle, 1f, true)
            }

        }
    }
    //endregion

    fun onBack() {
        finish()
    }

    override fun onBackPressed() {
        onBack()
    }
}