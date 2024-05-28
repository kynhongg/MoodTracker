package com.mood.screen.tutorial

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mood.utils.Constant

class ViewPagerTutorialAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return TutorialStep.tabSize()
    }

    override fun createFragment(position: Int): Fragment {
        return when (TutorialStep.getTabByPosition(position)) {
            TutorialStep.One -> TutorialFragment().apply {
                arguments = bundleOf(
                    Constant.TUTORIAL_STEP to TutorialStep.One.step
                )
            }

            TutorialStep.Two -> TutorialFragment().apply {
                arguments = bundleOf(
                    Constant.TUTORIAL_STEP to TutorialStep.Two.step
                )
            }

            TutorialStep.Three -> TutorialFragment().apply {
                arguments = bundleOf(
                    Constant.TUTORIAL_STEP to TutorialStep.Three.step
                )
            }
        }
    }
}