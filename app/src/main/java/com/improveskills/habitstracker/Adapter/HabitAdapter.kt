package com.improveskills.habitstracker.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.improveskills.habitstracker.database.HabitDataBase
import com.improveskills.habitstracker.database.habit
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import androidx.core.view.isVisible
import com.improveskills.habitstracker.Interface.OnComplite
import com.improveskills.habitstracker.R
import com.improveskills.habitstracker.Receiver.AlarmReceiver
import com.improveskills.habitstracker.SharedPrefData
import java.util.*
import kotlin.collections.ArrayList
import com.improveskills.habitstracker.Dialog.DialogTimer
import com.improveskills.habitstracker.Interface.DoAction
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers

class habitAdapter(
    private val context: Context,
    private val Habits: List<habit>,
    private val onComplite: OnComplite
) :
    RecyclerView.Adapter<habitAdapter.HabitViewHolder?>() {
    private val handler = Handler()
    val postDataBase: HabitDataBase = HabitDataBase.getInstance(context)
    var durationInMillis: Long = 0
    lateinit var pandentIntent: PendingIntent
    var sharedPrefData: SharedPrefData = SharedPrefData(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(
        holder: HabitViewHolder, @SuppressLint("RecyclerView") position: Int
    ) {
        var habit = Habits[position]
        holder.nameHAbit.text = habit.habitName
        holder.hTimeTask.text = "${habit.habitTime}"
        holder.backIconCard.setCardBackgroundColor(habit.color)
        holder.remaining_time.visibility = View.GONE

        when (habit.priority) {
            1 -> {
                holder.low_priority.visibility = View.VISIBLE
                holder.medium_priority.visibility = View.GONE
                holder.hight_priority.visibility = View.GONE
            }

            2 -> {
                holder.medium_priority.visibility = View.VISIBLE
                holder.low_priority.visibility = View.GONE
                holder.hight_priority.visibility = View.GONE
            }

            3 -> {
                holder.hight_priority.visibility = View.VISIBLE
                holder.low_priority.visibility = View.GONE
                holder.medium_priority.visibility = View.GONE
            }
        }

        if (habit.remainingTime == habit.duration) {
            holder.taskInProgress.visibility = View.GONE
            holder.taskCompleted.visibility = View.VISIBLE
        } else {
            holder.taskCompleted.visibility = View.GONE
            holder.taskInProgress.visibility = View.VISIBLE
        }

        SetBottomPadding(holder.hCardView, position)



        holder.remaining_time.text =
            "Remaining time : ${convertSecondsToHMS(((habit.duration - habit.remainingTime) / 1000).toInt())}"

        holder.hStartPause.setOnClickListener {
            if (!holder.taskCompleted.isVisible) {
                var remainingSeconds = 0L
                if (habit.duration != 1000L) {
                    durationInMillis =
                        System.currentTimeMillis() + (habit.duration - habit.remainingTime)
                    sharedPrefData.SaveBoolean("WorkingInHabit", true)
                    pandentIntent = scheduleAlarm(context, durationInMillis)

                    DialogTimer(context, habit).show(object : DoAction {
                        override fun OnDoAction() {
                            habit.remainingTime = (habit.duration - remainingSeconds)
                            UpdateHabit(habit)
                            cancelAlarm(context, pandentIntent)
                            handler.removeCallbacksAndMessages(null)
                        }
                    })
                    handler.postDelayed(object : Runnable {
                        override fun run() {
                            val currentTime = System.currentTimeMillis()
                            val remainingTime = durationInMillis - currentTime
                            remainingSeconds = remainingTime

                            holder.remaining_time.text =
                                "Remaining time :${convertSecondsToHMS((remainingSeconds / 1000).toInt())}"
                            if (remainingSeconds.toInt() <= 0) {
                                holder.taskInProgress.visibility = View.GONE
                                holder.taskCompleted.visibility = View.VISIBLE
                                habit.remainingTime = (habit.duration - remainingSeconds) / 1000
                                UpdateHabit(habit)
                                holder.remaining_time.text =
                                    "Remaining time :${convertSecondsToHMS(0)}"
                                onComplite.onComplite(habit.priority)
                            } else {
                                handler.postDelayed(this, 1000)
                            }
                        }
                    }, 1000)
                    /*      } else {
                              if ((holder.hProgressBar.progress * 1000).toLong() != habit.remainingTime) {
                                  habit.remainingTime = (holder.hProgressBar.progress * 1000).toLong()
                                  UpdateHabit(habit)
                                  cancelAlarm(context, pandentIntent)
                                  handler.removeCallbacksAndMessages(null)
                                  holder.hStartPause.setImageResource(R.drawable.baseline_play_circle_24)
                              } else Toast.makeText(
                                  context,
                                  "Now you are working",
                                  Toast.LENGTH_LONG
                              ).show()
                          }*/
                } else {
                    holder.taskInProgress.visibility = View.GONE
                    holder.taskCompleted.visibility = View.VISIBLE
                    habit.remainingTime = habit.duration
                    UpdateHabit(habit)
                    onComplite.onComplite(habit.priority)
                }
            }
        }
        holder.hDelete.setOnClickListener {

            if (sharedPrefData.LoadInt("Progress") == 0) {
                val priority = habit.priority
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setTitle("Delete Habit")
                alertDialogBuilder.setMessage("Are you sure to delete habit : ${habit.habitName} ?")
                alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->

                    postDataBase.postDao().getHabits()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<List<habit>> {
                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                            }
                            override fun onSuccess(t: List<habit>) {
                                postDataBase.postDao().deleteHabit(t[position])
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(object : CompletableObserver {
                                        override fun onSubscribe(d: Disposable) {
                                        }

                                        override fun onComplete() {
                                            (Habits as ArrayList).remove(habit)
                                        }

                                        override fun onError(e: Throwable) {

                                        }

                                    })


                            }

                        })
                    (Habits as ArrayList).remove(habit)
                    sharedPrefData.SaveInt(
                        "TotalPriority",
                        sharedPrefData.LoadInt("TotalPriority") - priority
                    )
                    notifyDataSetChanged()
                    dialog.dismiss()
                }

                alertDialogBuilder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            } else
                Toast.makeText(
                    context,
                    "It is better to reset all data after you can delete it to calculate your productivity",
                    Toast.LENGTH_LONG
                ).show()
        }


    }

    override fun getItemCount(): Int {
        return Habits.size
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameHAbit: TextView = itemView.findViewById(R.id.hName)
        val hTimeTask: TextView = itemView.findViewById(R.id.hTimeTask)
        val taskCompleted: TextView = itemView.findViewById(R.id.taskcompleted)
        val taskInProgress: TextView = itemView.findViewById(R.id.taskinirogress)
        val remaining_time: TextView = itemView.findViewById(R.id.Remaining_time)
        val low_priority: TextView = itemView.findViewById(R.id.l_priority)
        val medium_priority: TextView = itemView.findViewById(R.id.m_priority)
        val hight_priority: TextView = itemView.findViewById(R.id.h_priority)

        // val hProgressBar: ProgressBar = itemView.findViewById(R.id.hProgress)
        val hStartPause: ImageView = itemView.findViewById(R.id.hStartPause)
        val hDelete: ImageView = itemView.findViewById(R.id.hDelete)

        // val btn_layout: LinearLayout = itemView.findViewById(R.id.layout_btn)
        val hCardView: CardView = itemView.findViewById(R.id.hCardView)
        val backIconCard: CardView = itemView.findViewById(R.id.backIconCard)
    }

    fun UpdateHabit(habit: habit) {
        postDataBase.postDao().updateHabit(habit).subscribeOn(Schedulers.computation())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onComplete() {

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


    @SuppressLint("SuspiciousIndentation")
    fun SetBottomPadding(cardView: CardView, position: Int) {
        val paddingBottom =
            context.resources.getDimensionPixelSize(R.dimen.last_item_padding_bottom)
        val density = context.resources.displayMetrics.density
        val paddingBottomInPixels = (paddingBottom * density).toInt()

// Set padding for the CardView
        val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
        if (position < itemCount - 1) {
            layoutParams.setMargins(
                layoutParams.leftMargin,
                layoutParams.topMargin,
                layoutParams.rightMargin,
                layoutParams.leftMargin
            )
        } else
            layoutParams.setMargins(
                layoutParams.leftMargin,
                layoutParams.topMargin,
                layoutParams.rightMargin,
                paddingBottomInPixels
            )
        cardView.layoutParams = layoutParams
    }


}