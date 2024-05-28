package com.mood.screen.splash

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.databinding.ActivitySplashBinding
import com.mood.screen.home.MainActivity
import com.mood.screen.premium.PurchaseHelper
import com.mood.screen.tutorial.TutorialActivity
import com.mood.utils.Constant
import com.mood.utils.SharePrefUtils
import com.mood.utils.gone
import com.mood.utils.openActivity
import com.mood.utils.setFullScreenMode
import com.mood.utils.setOnSafeClick
import com.mood.utils.show
import com.mood.utils.showOrGone
import com.mood.utils.showToast
import java.util.concurrent.Executor

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    companion object {
        const val TIME_COUNT = 3L
        const val TAG = Constant.TAG
    }

    private var timeCountDown: Long = 0L
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }
    private val isFirstOpen by lazy {
        SharePrefUtils.getBoolean(Constant.IS_FIRST_OPEN)
    }
    private lateinit var purchaseHelper: PurchaseHelper

    override fun initView() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        setupFingerprint()
        handleLayoutClickLock()
    }

    //    val isEmulator: Boolean = Build.FINGERPRINT.startsWith("generic") ||
//            Build.FINGERPRINT.startsWith("unknown") ||
//            Build.MODEL.contains("google_sdk") ||
//            Build.MODEL.contains("Emulator") ||
//            Build.MODEL.contains("Android SDK built for x86") ||
//            Build.MANUFACTURER.contains("Genymotion") ||
//            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
//            "google_sdk" == Build.PRODUCT
    override fun initData() {
        createTimer()

//        purchaseHelper = PurchaseHelper(this)
//        purchaseHelper.onServiceReady { status ->
//            if (status) {
//                purchaseHelper.queryPurchases(
//                    PurchaseHelper.SUBS_TYPE
//                ) { billingResult, listPurchase ->
//                    runOnUiThread {
//                        if (billingResult.responseCode == PurchaseHelper.RESPONSE_CODE_OK) {
//                            if (listPurchase.isNotEmpty()) {//user is bought premium
//                                Log.d(Constant.TAG, "onResume: $listPurchase")
//                                SharePrefUtils.setBought(true)
//                            } else {
//                                SharePrefUtils.setBought(false)
//                                Log.d(Constant.TAG, "onResume: user not bought")
//                            }
//                            createTimer()
//                        } else {
//                            createTimer()
//                        }
//                    }
//                }
//            } else {
//                createTimer()
//            }
//        }
    }

    override fun initListener() {
        binding.number1.setOnClickListener { clickNumber(1) }
        binding.number2.setOnClickListener { clickNumber(2) }
        binding.number3.setOnClickListener { clickNumber(3) }
        binding.number4.setOnClickListener { clickNumber(4) }
        binding.number5.setOnClickListener { clickNumber(5) }
        binding.number6.setOnClickListener { clickNumber(6) }
        binding.number7.setOnClickListener { clickNumber(7) }
        binding.number8.setOnClickListener { clickNumber(8) }
        binding.number9.setOnClickListener { clickNumber(9) }
        binding.number0.setOnClickListener { clickNumber(0) }
        binding.imgDelete.setOnClickListener { clickNumber(-1) }
        binding.btnFingerprint.setOnClickListener {
            showFingerPrint()
        }
        binding.tvUsingPassword.setOnSafeClick {
            showLayoutPasscode()
        }
        binding.btnClosePasscode.setOnSafeClick {
            binding.llPasscode.gone()
        }
    }

    private fun createTimer() {
        val countDownTimer: CountDownTimer = object : CountDownTimer(TIME_COUNT * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                startApp()
            }
        }
        countDownTimer.start()
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private fun startApp() {
        if (!isFirstOpen) {
            openActivity(TutorialActivity::class.java, isFinish = true)
        } else {
            if (SharePrefUtils.isShowFingerprint()) {
                val biometricManager = BiometricManager.from(this)
                when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        Log.d(Constant.TAG, "App can authenticate using biometrics.")
                        showFingerPrint()
                    }

                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                        Log.e(Constant.TAG, "No biometric features available on this device.")

                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                        Log.e(Constant.TAG, "Biometric features are currently unavailable.")
                }
            } else {
                if (SharePrefUtils.isShowPasscode()) {
                    showLayoutPasscode()
                } else {
                    binding.llPasscode.gone()
                    openActivity(MainActivity::class.java, isFinish = true)
                }
            }
        }
    }

    private fun showLayoutPasscode() {
        if (SharePrefUtils.isShowFingerprint()) {
            binding.btnClosePasscode.show()
        } else {
            binding.btnClosePasscode.gone()
        }
        binding.llPasscode.show()
    }

    private fun showFingerPrint() {
        //show fingerprint authenticate
        biometricPrompt.authenticate(promptInfo)
    }

    private fun setupFingerprint() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
//                    showToast(getString(R.string.txt_authen_finger_error) + errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
//                    showToast(getString(R.string.txt_authen_finger_success))
                    startMain()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
//                    showToast(getString(R.string.txt_authen_finger_failed_v2))
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.title_finger_print_authenticator))
            .setSubtitle(getString(R.string.subtitle_finger_print_authenticator))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()
    }

    private var passCode = ""
    private fun clickNumber(number: Int) {
        if (number != -1) {
            if (passCode.length == 4) {
                return
            }
            passCode += number
        } else {
            if (passCode.isNotEmpty()) {
                passCode = passCode.substring(0, passCode.length - 1)
            }
        }
        showPass(passCode)
        if (passCode.length == 4) {
            if (passCode == SharePrefUtils.getPassCode()) {
                startMain()
            } else {
                showToast(getString(R.string.passcode_incorrect))
                passCode = ""
                showPass(passCode)
            }
        }
    }

    private fun startMain() {
        openActivity(MainActivity::class.java, isFinish = true)
    }

    private fun showPass(passCode: String) {
        val passCodeLength = passCode.length
        val passcodeViews = arrayOf(
            binding.passcode1,
            binding.passcode2,
            binding.passcode3,
            binding.passcode4
        )

        for (i in passcodeViews.indices) {
            passcodeViews[i].setImageResource(
                if (i < passCodeLength) R.drawable.ic_round_radio_button_checked
                else R.drawable.ic_round_radio_button_unchecked
            )
        }
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

//    override fun onDestroy() {
//        purchaseHelper.endConnect()
//        super.onDestroy()
//    }

    private fun handleLayoutClickLock() {
        binding.btnFingerprint.showOrGone(SharePrefUtils.isShowFingerprint())
        binding.tvUsingPassword.showOrGone(SharePrefUtils.isShowPasscode())
    }
}