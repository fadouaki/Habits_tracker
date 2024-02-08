package com.improveskill.habitstracker

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.improveskill.habitstracker.Receiver.AlarmReceiver
import java.util.*
import android.os.Handler
import android.util.Log
import android.widget.Toast

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val alarmTime = System.currentTimeMillis() + 1 * 60 * 1000 // 1 minutes from now
        setAlarm(this@MainActivity2, alarmTime)
        updateRemainingTime(this@MainActivity2, alarmTime)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(context: Context, alarmTime: Long) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_MUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }


    fun updateRemainingTime(context: Context, alarmTime: Long) {
        val handler = Handler()
        val delay = 1000L // 1 minute delay

        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val remainingTime = alarmTime - currentTime // System.currentTimeMillis() + 1 * 60 * 1000

                val remainingMinutes = remainingTime / (1000 * 60)
                val remainingSeconds = (remainingTime / 1000) % 60

                val toastMessage = "Remaining time until alarm: $remainingMinutes minutes $remainingSeconds seconds"
                Log.d("HabitTAki", toastMessage)
               // Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()

                // Schedule the next update if there is remaining time
                if (remainingTime > 0) {
                    handler.postDelayed(this, delay)
                }
            }
        }, delay)
    }

}