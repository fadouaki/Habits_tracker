package com.improveskills.habitstracker.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.improveskills.habitstracker.Interface.DoAction
import com.improveskills.habitstracker.R
import com.improveskills.habitstracker.database.habit

class DialogTimer(
    private val context: Context,
    private val habit: habit
) {
    private val handlerAnimationView = Handler()
    private var SettingsDialog: Dialog? = null
    fun show(doAction: DoAction) {
        SettingsDialog = Dialog(context)
        SettingsDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SettingsDialog?.setContentView(R.layout.dialog_timer)

        val habitNameTextView = SettingsDialog?.findViewById<TextView>(R.id.habit_name)
        val pause_timer = SettingsDialog?.findViewById<ImageView>(R.id.pause_timer)
        val lottieAnimationView =
            SettingsDialog?.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        pause_timer?.setOnClickListener {
            doAction.OnDoAction()
            SettingsDialog?.dismiss()
        }
        habitNameTextView?.text = habit.habitName
        ProgressHourGlass(lottieAnimationView!!, habit.duration, habit.remainingTime)
        SettingsDialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        SettingsDialog?.setCancelable(false)
        SettingsDialog?.show()
    }


    private fun ProgressHourGlass(
        lottieAnimationView: LottieAnimationView,
        animationDelay: Long,
        reminingTimer: Long
    ) {
        val StartingTime = System.currentTimeMillis() - reminingTimer

        var startAnimation = 93
        val durationanimation = 610L
        val duran = 517
        var percentage: Float = 0f

        handlerAnimationView.postDelayed(object : Runnable {
            override fun run() {
                percentage =
                    (System.currentTimeMillis() - StartingTime).toFloat() / animationDelay
                lottieAnimationView.frame = startAnimation + (percentage * duran).toInt()

                if (lottieAnimationView.frame > durationanimation) {
                    if (SettingsDialog?.isShowing == true)
                        SettingsDialog!!.dismiss()
                } else {
                    handlerAnimationView.postDelayed(this, 100)
                }
            }
        }, 100)
    }
}