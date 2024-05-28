package com.mood.screen.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mood.base.BaseFragment
import com.mood.databinding.FragmentTutorialBinding
import com.mood.utils.Constant
import com.mood.utils.gone
import com.mood.utils.show

class TutorialFragment : BaseFragment<FragmentTutorialBinding>() {
    private val step by lazy {
        arguments?.getInt(Constant.TUTORIAL_STEP, TutorialStep.One.step)
    }

    override fun initView() {
        when (step) {
            TutorialStep.One.step -> {
                setUpTitleStepOne()
            }

            TutorialStep.Two.step -> {
                setUpTitleStepTwo()
            }

            TutorialStep.Three.step -> {
                setUpTitleStepThree()
            }
        }
    }

    private fun setUpTitleStepThree() {
        binding.step1.root.gone()
        binding.step2.root.gone()
        binding.step3.root.show()
    }

    private fun setUpTitleStepTwo() {
        binding.step1.root.gone()
        binding.step2.root.show()
        binding.step3.root.gone()
    }

    private fun setUpTitleStepOne() {
        binding.step1.root.show()
        binding.step2.root.gone()
        binding.step3.root.gone()
    }

    override fun initData() {

    }

    override fun initListener() {
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTutorialBinding {
        return FragmentTutorialBinding.inflate(inflater, container, false)
    }
}