package com.antonin.friendswave.outils

import android.animation.ValueAnimator
import android.content.res.Resources
import android.view.View
import android.view.animation.DecelerateInterpolator


//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est une classe permettant d'afficher des animations

//https://stackoverflow.com/questions/4946295/android-expand-collapse-animation

class AnimationLayout {


    fun expand(view: View, duration:Int, targetHeight:Int) {

        val prevHeight  = view.getHeight()

        view.visibility = View.VISIBLE
        val valueAnimator : ValueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight.px)

        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                view.layoutParams.height = animation.getAnimatedValue() as Int
                view.requestLayout()
            }
        })
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()

    }

    fun  collapse(view: View, duration:Int, targetHeight:Int) {
        val prevHeight: Int = view.height
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight.px)
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { animation ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()

    }

    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}