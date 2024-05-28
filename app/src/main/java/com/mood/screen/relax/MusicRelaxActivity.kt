package com.mood.screen.relax

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.data.database.BeanViewModel
import com.mood.data.entity.MusicCalmEntity
import com.mood.databinding.ActivityMusicRelaxBinding
import com.mood.screen.home.calendar.TAG
import com.mood.utils.CalendarUtil
import com.mood.utils.Constant
import com.mood.utils.DataUtils
import com.mood.utils.SharePrefUtils
import com.mood.utils.setFullScreenMode
import com.mood.utils.setOnSafeClick
import com.mood.utils.showToast

class MusicRelaxActivity : BaseActivity<ActivityMusicRelaxBinding>() {

    private val triggerIndex by lazy {
        intent?.extras?.getInt(Constant.TRIGGER_SOUND_SELECT) ?: 0
    }
    private val ambientIndex by lazy {
        intent?.extras?.getInt(Constant.AMBIENT_SOUND_SELECT) ?: 0
    }
    private val timeCountDown by lazy {
        intent?.extras?.getInt(Constant.TIME_COUNTDOWN) ?: 1
    }
    private val viewModel: BeanViewModel by viewModels {
        Constant.getViewModelFactory(application)
    }

    //region variable
    private var isShowNativeAds = false
    private var secondInMonth = 0
    private var mediaPlay1: MediaPlayer? = null
    private var mediaPlay2: MediaPlayer? = null
    private var currentMedia1 = 0
    private var currentMedia2 = 0
    private var isPlay = true
    private var timeListen = 0
    private val currentMonth = CalendarUtil.getMonthInt() + 1
    private val currentYear = CalendarUtil.getYearInt()
    private var countDown: Long = 0L
    private var countDownTimer: CountDownTimer = object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            countDown = 0
        }
    }
    //endregion

    override fun initView() {
        setFullScreenMode(SharePrefUtils.isFullScreenMode())
        if (SharePrefUtils.isCustomBackgroundImage()) {
            binding.imgBackground.setImageResource(SharePrefUtils.getBackgroundImageApp())
        }
        binding.imgTriggerSound.setImageResource(TriggerSound.values()[triggerIndex].imageSource)
        binding.imgAmbientSound.setImageResource(AmbientSound.values()[ambientIndex].imageSource)
        binding.tvCountDown.text = DataUtils.getFormatMinutes(timeCountDown, 0)
        if (triggerIndex == TriggerSound.values().size - 1) {
            binding.seekBarTrigger.apply {
                isEnabled = false
                progress = 0
            }
        }
        if (ambientIndex == AmbientSound.values().size - 1) {
            binding.seekBarAmbient.apply {
                isEnabled = false
                progress = 0
            }
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    override fun initData() {
        viewModel.getAllMusicCalm { calms ->
            if (!calms.any { it.month == currentMonth && it.year == currentYear }) {
                viewModel.insertMusicCalm(MusicCalmEntity().apply {
                    this.second = 0
                    this.month = currentMonth
                    this.year = currentYear
                })
            }
        }
        viewModel.getSecondMusicCalmWithMonth(
            currentMonth, currentYear
        ) {
            secondInMonth = it
        }

        countDown = timeCountDown * 60L
        setupView(isPlay)
    }

    override fun initListener() {
        binding.layoutBtnBlack.setOnSafeClick { onBack() }
        binding.seekBarTrigger.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                val percent = (progress * 1.0f) / (seekBar?.max ?: 100)
                mediaPlay1?.setVolume(percent, percent)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        binding.seekBarAmbient.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                val percent = (progress * 1.0f) / (seekBar?.max ?: 100)
                mediaPlay2?.setVolume(percent, percent)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        binding.btnPlayPause.setOnSafeClick {
            isPlay = !isPlay
            setupView(isPlay)
        }
    }

    private fun setupView(play: Boolean) {
        if (play) {
            binding.imgButtonPlayPause.setImageResource(R.drawable.ic_pause)
            binding.tvButtonPlayPause.text = getString(R.string.pause)
            playMedia()
            createTimer(countDown)
        } else {
            binding.imgButtonPlayPause.setImageResource(R.drawable.ic_play)
            binding.tvButtonPlayPause.text = getString(R.string.play)
            pauseMedia()
            countDownTimer.cancel()
        }
    }

    private fun createTimer(seconds: Long) {
        countDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDown = millisUntilFinished / 1000
                binding.tvCountDown.text = DataUtils.getFormatMinutes(countDown.toInt())
                timeListen++
            }

            override fun onFinish() {
                countDown = 0
                isPlay = false
                setupView(isPlay)
                showToast(getString(R.string.finish_sound))
                onBack()
            }
        }
        countDownTimer.start()
    }

    //region Play media
    private fun playMedia() {
        playMusicTrigger()
        playMusicAmbient()
    }

    private fun playMusicTrigger() {
        try {
            if (mediaPlay1 == null) {
                val soundNameTrigger = TriggerSound.values()[triggerIndex].url
                if (soundNameTrigger == "") {
                    mediaPlay1 = MediaPlayer()
                    return
                }
                mediaPlay1 = MediaPlayer().apply {
                    val assetPath = "music"
                    val afd: AssetFileDescriptor = assets.openFd("$assetPath/$soundNameTrigger")
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    isLooping = true
                    setVolume(1f, 1f)
                    start()
                }
            } else {
                mediaPlay1?.seekTo(currentMedia1)
                mediaPlay1?.start()
            }
        } catch (e: Exception) {
            Log.d(TAG, "playMusicTrigger: ${e.message}")
        }
    }

    private fun playMusicAmbient() {
        try {
            if (mediaPlay2 == null) {
                val soundNameAmbient = AmbientSound.values()[ambientIndex].url
                if (soundNameAmbient == "") {
                    mediaPlay2 = MediaPlayer()
                    return
                }
                mediaPlay2 = MediaPlayer().apply {
                    val assetPath = "music"
                    val afd: AssetFileDescriptor = assets.openFd("$assetPath/$soundNameAmbient")
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    isLooping = true
                    setVolume(1f, 1f)
                    start()
                }
            } else {
                mediaPlay2?.seekTo(currentMedia2)
                mediaPlay2?.start()
            }
        } catch (e: Exception) {
            Log.d(TAG, "playMusicTrigger: ${e.message}")
        }
    }

    private fun pauseMedia() {
        mediaPlay1?.pause()
        currentMedia1 = mediaPlay1?.currentPosition ?: 0
        mediaPlay2?.pause()
        currentMedia2 = mediaPlay2?.currentPosition ?: 0
    }
    //endregion

    private fun stopMedia() {
        stopMusicTrigger()
        stopMusicAmbient()
    }

    private fun stopMusicTrigger() {
        try {
            mediaPlay1?.stop()
            mediaPlay1?.release()
            mediaPlay1 = null

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun stopMusicAmbient() {
        try {
            mediaPlay2?.stop()
            mediaPlay2?.release()
            mediaPlay2 = null

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun onBack() {
        countDownTimer.cancel()
        val timeInMonth = secondInMonth + timeListen
        viewModel.updateSecondCalmWithMonthYear(
            timeInMonth,
            currentMonth, currentYear
        )
        stopMedia()
        finish()
    }

    override fun onDestroy() {
        countDownTimer.cancel()
        stopMedia()
        val timeInMonth = secondInMonth + timeListen
        viewModel.updateSecondCalmWithMonthYear(
            timeInMonth,
            currentMonth, currentYear
        )
        super.onDestroy()
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMusicRelaxBinding {
        return ActivityMusicRelaxBinding.inflate(inflater)
    }
}