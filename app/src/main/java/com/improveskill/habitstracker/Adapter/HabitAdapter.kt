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
import kotlin.random.Random
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.improveskill.habitstracker.R
import com.improveskill.habitstracker.Receiver.AlarmReceiver
import com.improveskill.habitstracker.SharedPrefData
import java.util.*
import kotlin.collections.ArrayList

class habitAdapter(private val context: Context, private val Habits: List<habit>) :
    RecyclerView.Adapter<habitAdapter.HabitViewHolder?>() {
    private val handler = Handler()
    lateinit var postDataBase: HabitDataBase
    var durationInMillis: Long = 0
    lateinit var pandentIntent: PendingIntent
    var sharedPrefData: SharedPrefData = SharedPrefData(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HabitViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val habit = Habits[position]
        holder.nameHAbit.text = habit.habitName
        holder.hDuration.text = "Duration ${convertSecondsToHMS(habit.duration)}"
        holder.hProgressBar.max = habit.duration
        holder.hProgressBar.setProgress(habit.remainingTime)
        holder.hCardView.setCardBackgroundColor(generateRandomColor())
        if (habit.remainingTime == habit.duration) {
            holder.btn_layout.visibility = View.GONE
            holder.taskcompleted.visibility = View.VISIBLE
        } else {
            holder.btn_layout.visibility = View.VISIBLE
            holder.taskcompleted.visibility = View.GONE
            holder.hStartPause.setImageResource(R.drawable.baseline_play_circle_24)
        }

        holder.remaining_time.text =
            "Remaining time : ${convertSecondsToHMS(habit.duration - habit.remainingTime)}"

        holder.hStartPause.setOnClickListener {

            durationInMillis =
                System.currentTimeMillis() + (habit.duration - habit.remainingTime).toLong() * 1000

            if (!sharedPrefData.LoadBoolean("WorkInHabit")) {
                sharedPrefData.SaveBoolean("WorkInHabit", true)
                pandentIntent = scheduleAlarm(context, durationInMillis)

                holder.hStartPause.setImageResource(R.drawable.baseline_pause_circle_24)
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        val currentTime = System.currentTimeMillis()
                        val remainingTime = durationInMillis - currentTime
                        val remainingSeconds = remainingTime / (1000)

                        holder.hProgressBar.setProgress(habit.duration - remainingSeconds.toInt())
                        holder.remaining_time.text =
                            "Remaining time :${convertSecondsToHMS(remainingSeconds.toInt())}"
                        if (remainingSeconds.toInt() <= 0) {
                            Log.d("HabitTAki", "Receiver started1111")
                            holder.btn_layout.visibility = View.GONE
                            holder.taskcompleted.visibility = View.VISIBLE
                            habit.remainingTime = holder.hProgressBar.progress
                            UpdateHabit(habit)
                            holder.remaining_time.text = "Remaining time :${convertSecondsToHMS(0)}"
                        } else {
                            // Post the next update after the defined interval
                            handler.postDelayed(this, 1000)
                        }
                    }
                }, 1000)
            } else {
                if (holder.hProgressBar.progress != habit.remainingTime) {
                    habit.remainingTime = holder.hProgressBar.progress
                    UpdateHabit(habit)
                    cancelAlarm(context, pandentIntent)
                    handler.removeCallbacksAndMessages(null)
                    holder.hStartPause.setImageResource(R.drawable.baseline_play_circle_24)

                } else
                    Toast.makeText(context, "Now you are working", Toast.LENGTH_LONG).show()
            }

        }
        holder.hEdite.setOnClickListener {
            showAddEditeDialog(habit)
        }
        holder.hCardView.setOnLongClickListener {
            showDeleteHabit(habit)
            true
        }

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
        postDataBase.postDao().updateHabit(habit)
            .subscribeOn(Schedulers.computation())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onComplete() {
                }

                override fun onError(e: Throwable) {
                }

            })
        sharedPrefData.SaveBoolean("WorkInHabit", false)
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

    fun generateRandomColor(): Int {
        val random = Random.Default
        val red = random.nextInt(128, 256) // Generate random value for red (128-255)
        val green = random.nextInt(128, 256) // Generate random value for green (128-255)
        val blue = random.nextInt(128, 256) // Generate random value for blue (128-255)
        return Color.rgb(red, green, blue) // Combine components and return color
    }

    fun convertSecondsToHMS(seconds: Int): String {
        val hours = seconds / 3600
        val remainingSeconds = seconds % 3600
        val minutes = remainingSeconds / 60
        val remainingSecondsFinal = remainingSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSecondsFinal)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddEditeDialog(Ehabit: habit) {
        val SettingsDialog: Dialog = Dialog(context)
        SettingsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SettingsDialog.setContentView(R.layout.dialog_add_item)

        val EditeBtn = SettingsDialog.findViewById<Button>(R.id.add_habit)
        EditeBtn.setText("Edite habit")
        val name = SettingsDialog.findViewById<EditText>(R.id.name_habit)
        val hoursDuration = SettingsDialog.findViewById<EditText>(R.id.hour_habit)
        val minutesDuration = SettingsDialog.findViewById<EditText>(R.id.min_habit)

        name.setText(Ehabit.habitName)
        hoursDuration.setText("${Ehabit.duration / 3600}")
        minutesDuration.setText("${(Ehabit.duration / 60) % 60}")
        Log.d("HabitTAki", "minite ${Ehabit.duration / 60} with ${Ehabit.duration} ")
        SettingsDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        SettingsDialog.findViewById<Button>(R.id.add_habit).setOnClickListener {

            if (!name.text.isEmpty() && !minutesDuration.text.isEmpty() && !hoursDuration.text.isEmpty()) {

                val duration: Int = (hoursDuration.text.toString()
                    .toInt() * 3600) + minutesDuration.text.toString().toInt() * 60

                Ehabit.habitName = name.text.toString()
                Ehabit.duration = duration
                notifyDataSetChanged()
                postDataBase = HabitDataBase.getInstance(context)
                postDataBase.postDao()
                    .updateHabit(Ehabit)
                    .subscribeOn(Schedulers.computation())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                        }

                        @SuppressLint("NotifyDataSetChanged")
                        override fun onComplete() {
                            Log.d("HabitTAki", " Edite habit completed ")
                            SettingsDialog.dismiss()

                        }

                        override fun onError(e: Throwable) {
                            Log.d("HabitTAki", " Edite habit onError ")
                            Toast.makeText(context, "Error...", Toast.LENGTH_LONG)
                                .show()
                            SettingsDialog.dismiss()
                        }
                    })
            } else
                Toast.makeText(context, "Please don't leave any fields empty!", Toast.LENGTH_LONG)
                    .show()
        }
        SettingsDialog.show()
    }

    fun showDeleteHabit(dHabit: habit) {
        val alertDialogBuilder = AlertDialog.Builder(context)

        // Set the title and message
        alertDialogBuilder.setTitle("Delete Habit")
        alertDialogBuilder.setMessage("Are you sure to delete habit : ${dHabit.habitName} ?")

        // Set the positive button
        alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
            (Habits as ArrayList).remove(dHabit)
            postDataBase = HabitDataBase.getInstance(context)
            postDataBase.postDao().deleteHabit(dHabit)
                .subscribeOn(Schedulers.computation())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onComplete() {
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
    }
}