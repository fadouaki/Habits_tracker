package com.improveskill.habitstracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_habit")
class habit (
    val habitName: String,
    val duration: Int,
    var remainingTime:Int
    ){
        @PrimaryKey(autoGenerate = true)
        var id = 0
    }