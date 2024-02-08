package com.improveskill.habitstracker.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import com.improveskill.habitstracker.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.task_completed)
        mediaPlayer.start()
        Log.d("HabitTAki", "Receiver started")
    }
}
