package com.improveskill.habitstracker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class HabitProgressWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        // Your background task code here
        // This method runs on a background thread
        // You can update UI elements here, but use postValue() for LiveData if needed
        // Make sure to return Result.success() if the task is successful
        return Result.success()
    }
}