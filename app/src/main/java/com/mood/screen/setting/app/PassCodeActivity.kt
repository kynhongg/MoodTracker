package com.mood.screen.setting.app

import android.view.LayoutInflater
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.databinding.LayoutLockBinding
import com.mood.utils.SharePrefUtils
import com.mood.utils.hide
import com.mood.utils.show
import com.mood.utils.showToast

class PassCodeActivity : BaseActivity<LayoutLockBinding>() {
    private var passCode = ""
    private var passCodeConfirm = ""
    override fun initView() {

    }

    override fun initData() {

    }

    private fun onBack() {
        SharePrefUtils.setIsShowPasscode(false)
        finish()
    }

    override fun initListener() {
        binding.btnExit.setOnClickListener {
            if (isConfirm) {
                passCode = ""
                passCodeConfirm = ""
                showPass(passCodeConfirm)
                binding.tvTitle.text = getString(R.string.enter_passcode)
                binding.tvNotCorrect.hide()
                isConfirm = false
                binding.btnExit.text = getString(R.string.txt_exit)
            } else {
                onBack()
            }
        }
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
    }

    override fun inflateViewBinding(inflater: LayoutInflater): LayoutLockBinding {
        return LayoutLockBinding.inflate(inflater)
    }

    private var isConfirm = false
    private fun clickNumber(number: Int) {
        if (!isConfirm) {
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
                isConfirm = true
                passCodeConfirm = ""
                showPass(passCodeConfirm)
                binding.tvTitle.text = getString(R.string.confirm_passcode)
                binding.btnExit.text = getString(R.string.txt_back_screen)
            }
        } else {
            if (number != -1) {
                if (passCodeConfirm.length == 4) {
                    return
                }
                passCodeConfirm += number
            } else {
                if (passCodeConfirm.isNotEmpty()) {
                    passCodeConfirm = passCodeConfirm.substring(0, passCodeConfirm.length - 1)
                }
            }
            showPass(passCodeConfirm)
            if (passCodeConfirm.length == 4) {
                if (passCode == passCodeConfirm) {
                    SharePrefUtils.setIsShowPasscode(true)
                    SharePrefUtils.setPassCode(passCode)
                    showToast(getString(R.string.set_passcode_success))
                    finish()
                } else {
                    binding.tvNotCorrect.show()
                    SharePrefUtils.setIsShowPasscode(false)
                    passCodeConfirm = ""
                    showPass(passCodeConfirm)
                    showToast(getString(R.string.passcode_not_match))
                }
            } else {
                binding.tvNotCorrect.hide()
            }
        }
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

    override fun onBackPressed() {
    }
}