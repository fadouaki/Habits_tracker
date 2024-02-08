package com.improveskill.habitstracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
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
import android.app.PendingIntent
import android.content.Intent
import com.improveskill.habitstracker.Receiver.AlarmReceiver
import java.util.*
class habitAdapter(private val context: Context, private val Habits: List<habit>) :
    RecyclerView.Adapter<habitAdapter.HabitViewHolder?>() {
    private val handler = Handler()
    lateinit var postDataBase: HabitDataBase
    var  durationInMillis:Long = 0
    lateinit var pandentIntent:PendingIntent
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
        holder.hDuration.text ="Duration ${convertSecondsToHMS(habit.duration)}"
        holder.hProgressBar.max = habit.duration
        holder.hProgressBar.setProgress(habit.remainingTime)
        holder.hCardView.setCardBackgroundColor(generateRandomColor())
        if (habit.remainingTime==habit.duration)
        {
            holder.hbtn.visibility=View.GONE
            holder.taskcompleted.visibility=View.VISIBLE
        }else{
            holder.hbtn.visibility=View.VISIBLE
            holder.taskcompleted.visibility=View.GONE
            holder.hbtn.setImageResource(R.drawable.baseline_play_circle_24)
        }

        holder.remaining_time.text="Remaining time : ${convertSecondsToHMS(habit.duration-habit.remainingTime)}"

        holder.hbtn.setOnClickListener {

            durationInMillis =System.currentTimeMillis()+(habit.duration-habit.remainingTime).toLong() * 1000

            if (!sharedPrefData.LoadBoolean("WorkInHabit")) {
                sharedPrefData.SaveBoolean("WorkInHabit", true)
                pandentIntent = scheduleAlarm(context, durationInMillis)

                holder.hbtn.setImageResource(R.drawable.baseline_pause_circle_24)
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        val currentTime = System.currentTimeMillis()
                        val remainingTime = durationInMillis - currentTime
                        val remainingSeconds =  remainingTime / (1000)

                        holder.hProgressBar.setProgress(habit.duration-remainingSeconds.toInt())
                        holder.remaining_time.text="Remaining time :${convertSecondsToHMS(remainingSeconds.toInt())}"
                        if (remainingSeconds.toInt()<=0) {
                            Log.d("HabitTAki", "Receiver started1111")
                            holder.hbtn.visibility=View.GONE
                            holder.taskcompleted.visibility=View.VISIBLE
                            habit.remainingTime = holder.hProgressBar.progress
                            UpdateHabit(habit)
                            holder.remaining_time.text="Remaining time :${convertSecondsToHMS(0)}"
                        } else {
                            // Post the next update after the defined interval
                            handler.postDelayed(this, 1000)
                        }
                    }
                }, 1000)
            } else {
                if (holder.hProgressBar.progress!= habit.remainingTime) {
                    habit.remainingTime = holder.hProgressBar.progress
                    UpdateHabit(habit)
                    cancelAlarm(context,pandentIntent)
                    handler.removeCallbacksAndMessages(null)
                    holder.hbtn.setImageResource(R.drawable.baseline_play_circle_24)

                }else
                    Toast.makeText(context, "Now you are working", Toast.LENGTH_LONG).show()
            }

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
        val hbtn: ImageView = itemView.findViewById(R.id.hStartPause)
        val hCardView: CardView = itemView.findViewById(R.id.hCardView)
    }
    fun UpdateHabit(habit:habit){
        postDataBase = HabitDataBase.getInstance(context)
        postDataBase.postDao().updateCategory(habit)
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
    fun scheduleAlarm(context: Context, durationInMillis: Long):PendingIntent {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
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
}