package com.improveskill.habitstracker.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.improveskill.habitstracker.database.HabitDataBase
import com.improveskill.habitstracker.database.habit
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import com.improveskill.habitstracker.OnComplite
import com.improveskill.habitstracker.R
import com.improveskill.habitstracker.Receiver.AlarmReceiver
import com.improveskill.habitstracker.SharedPrefData
import java.util.*
import kotlin.collections.ArrayList
import com.airbnb.lottie.LottieAnimationView

class habitAdapter(
    private val context: Context,
    private val Habits: List<habit>,
    private val onComplite: OnComplite
) :
    RecyclerView.Adapter<habitAdapter.HabitViewHolder?>() {
    private val handler = Handler()
    lateinit var postDataBase: HabitDataBase
    var durationInMillis: Long = 0
    lateinit var pandentIntent: PendingIntent
    var sharedPrefData: SharedPrefData = SharedPrefData(context)
    lateinit var PuaseClickListener: (View) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HabitViewHolder, @SuppressLint("RecyclerView") position: Int
    ) {
        val habit = Habits[position]
        holder.nameHAbit.text = habit.habitName
        holder.hDuration.text = "Duration ${convertSecondsToHMS((habit.duration / 1000).toInt())}"
        holder.hProgressBar.max = (habit.duration / 1000).toInt()
        holder.hProgressBar.setProgress((habit.remainingTime / 1000).toInt())
        holder.hCardView.setCardBackgroundColor(habit.color)
        if (habit.duration == 1000L) {

            holder.hDuration.visibility = View.GONE
            holder.hProgressBar.visibility = View.GONE
            holder.remaining_time.visibility = View.GONE
        }
        if (habit.remainingTime == habit.duration) {
            holder.btn_layout.visibility = View.GONE
            holder.taskcompleted.visibility = View.VISIBLE
        } else {
            holder.btn_layout.visibility = View.VISIBLE
            holder.taskcompleted.visibility = View.GONE
            // holder.hStartPause.setImageResource(R.drawable.baseline_play_circle_24)
        }

        PuaseClickListener = { view: View ->
            if (habit.duration != 1000L) {
                durationInMillis =
                    System.currentTimeMillis() + (habit.duration - habit.remainingTime)

                if (!sharedPrefData.LoadBoolean("WorkingInHabit")) {

                    sharedPrefData.SaveBoolean("WorkingInHabit", true)
                    pandentIntent = scheduleAlarm(context, durationInMillis)
                    ShowDialogTimer(
                        context,
                        habit.duration,
                        habit.habitName,
                        habit.remainingTime
                    )
                    //     holder.hStartPause.setImageResource(R.drawable.baseline_pause_circle_24)
                    handler.postDelayed(object : Runnable {
                        override fun run() {
                            val currentTime = System.currentTimeMillis()
                            val remainingTime = durationInMillis - currentTime
                            val remainingSeconds = remainingTime

                            holder.hProgressBar.setProgress(((habit.duration - remainingSeconds) / 1000).toInt())
                            holder.remaining_time.text =
                                "Remaining time :${convertSecondsToHMS((remainingSeconds / 1000).toInt())}"
                            if (remainingSeconds.toInt() <= 0) {
                                Log.d("HabitTAki", "Receiver started1111")
                                holder.btn_layout.visibility = View.GONE
                                holder.taskcompleted.visibility = View.VISIBLE
                                habit.remainingTime = (holder.hProgressBar.progress * 1000).toLong()
                                UpdateHabit(habit)
                                holder.remaining_time.text =
                                    "Remaining time :${convertSecondsToHMS(0)}"
                                onComplite.onComplite(habit.priority)
                            } else {
                                // Post the next update after the defined interval
                                handler.postDelayed(this, 1000)
                            }
                        }
                    }, 1000)
                } else {
                    handlerAnimationView.removeCallbacksAndMessages(null)
                    if (SettingsDialog?.isShowing == true)
                        SettingsDialog!!.dismiss()
                    if ((holder.hProgressBar.progress * 1000).toLong() != habit.remainingTime) {
                        habit.remainingTime = (holder.hProgressBar.progress * 1000).toLong()
                        UpdateHabit(habit)
                        cancelAlarm(context, pandentIntent)
                        handler.removeCallbacksAndMessages(null)
                        holder.hStartPause.setImageResource(R.drawable.baseline_play_circle_24)
                    } else Toast.makeText(
                        context,
                        "Now you are working ${sharedPrefData.LoadBoolean("WorkingInHabit")}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                holder.btn_layout.visibility = View.GONE
                holder.taskcompleted.visibility = View.VISIBLE
                habit.remainingTime = habit.duration
                UpdateHabit(habit)
                onComplite.onComplite(habit.priority)
            }


        }
        holder.remaining_time.text =
            "Remaining time : ${convertSecondsToHMS(((habit.duration - habit.remainingTime) / 1000).toInt())}"

        holder.hStartPause.setOnClickListener(PuaseClickListener)
        holder.hEdite.setOnClickListener {
            //   showAddEditeDialog(habit)
            showDeleteHabit(habit)
        }
        /* holder.hCardView.setOnLongClickListener {
             showDeleteHabit(habit)
             true
         }*/

    }

    override fun getItemCount(): Int {
        return Habits.size
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameHAbit: TextView = itemView.findViewById(R.id.hName)
        val hDuration: TextView = itemView.findViewById(R.id.hDuration)
        val taskcompleted: TextView = itemView.findViewById(R.id.taskcompleted)
        val remaining_time: TextView = itemView.findViewById(R.id.Remaining_time)
        val hProgressBar: ProgressBar = itemView.findViewById(R.id.hProgress)
        val hStartPause: ImageView = itemView.findViewById(R.id.hStartPause)
        val hEdite: ImageView = itemView.findViewById(R.id.hEdite)
        val btn_layout: LinearLayout = itemView.findViewById(R.id.layout_btn)
        val hCardView: CardView = itemView.findViewById(R.id.hCardView)
    }

    fun UpdateHabit(habit: habit) {
        postDataBase = HabitDataBase.getInstance(context)
        postDataBase.postDao().updateHabit(habit).subscribeOn(Schedulers.computation())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onComplete() {
                    Log.d("HabitTAki", "Update done ${habit.remainingTime}")
                }

                override fun onError(e: Throwable) {
                }

            })

        sharedPrefData.SaveBoolean("WorkingInHabit", false)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context, durationInMillis: Long): PendingIntent {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, durationInMillis, pendingIntent)
        return pendingIntent
    }

    fun cancelAlarm(context: Context, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun convertSecondsToHMS(seconds: Int): String {
        val hours = seconds / 3600
        val remainingSeconds = seconds % 3600
        val minutes = remainingSeconds / 60
        val remainingSecondsFinal = remainingSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSecondsFinal)
    }

    fun showDeleteHabit(dHabit: habit) {
        if (sharedPrefData.LoadInt("Progress") == 0) {
            val priority = dHabit.priority
            val alertDialogBuilder = AlertDialog.Builder(context)

            // Set the title and message
            alertDialogBuilder.setTitle("Delete Habit")
            alertDialogBuilder.setMessage("Are you sure to delete habit : ${dHabit.habitName} ?")

            // Set the positive button
            alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                (Habits as ArrayList).remove(dHabit)
                postDataBase = HabitDataBase.getInstance(context)
                postDataBase.postDao().deleteHabit(dHabit).subscribeOn(Schedulers.computation())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onComplete() {
                            sharedPrefData.SaveInt(
                                "TotalPriority",
                                sharedPrefData.LoadInt("TotalPriority") - priority
                            )
                        }

                        override fun onError(e: Throwable) {
                        }

                    })

                notifyDataSetChanged()
                dialog.dismiss()
                // Action when "Ok" button is clicked
                // You can put your code here to handle the action
            }

            // Set the negative button
            alertDialogBuilder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
                // Action when "No" button is clicked
                // You can put your code here to handle the action
            }

            // Create and show the dialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        } else
            Toast.makeText(
                context,
                "It is better to reset all data after you can delete it to calculate your productivity",
                Toast.LENGTH_LONG
            ).show()
    }

    //-------------------------------- ShowDialogTimer Animation --------------------------------

    var SettingsDialog: Dialog? = null
    fun ShowDialogTimer(
        context: Context,
        animationDelay: Long,
        habitName: String,
        reminingTimer: Long
    ) {
        SettingsDialog = Dialog(context)
        SettingsDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SettingsDialog?.setContentView(R.layout.dialog_timer)

        val habitNameTextView = SettingsDialog?.findViewById<TextView>(R.id.habit_name)
        val pause_timer = SettingsDialog?.findViewById<ImageView>(R.id.pause_timer)
        val lottieAnimationView =
            SettingsDialog?.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        pause_timer?.setOnClickListener(PuaseClickListener)
        habitNameTextView?.text = habitName
        ProgressHourGlass(lottieAnimationView!!, animationDelay, reminingTimer)
        SettingsDialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        SettingsDialog?.setCancelable(false)
        SettingsDialog?.show()
    }


    //-------------------------------- ProgressHourGlass Animation --------------------------------

    val handlerAnimationView = Handler()
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
        Log.d(
            "HabitTAki",
            "reminingTimer ${reminingTimer / 100} : durationanimation ${animationDelay / 100}"
        )
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