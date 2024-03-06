package com.improveskill.habitstracker.database

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_habit")
class habit (
    var habitName: String,
    var duration: Long,
    var remainingTime:Long,
    var priority:Int,
    var color: Int
    ){
        @PrimaryKey(autoGenerate = true)
        var id = 0
    }