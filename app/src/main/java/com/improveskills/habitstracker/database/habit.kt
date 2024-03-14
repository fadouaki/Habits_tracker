package com.improveskills.habitstracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_habit")
class habit (
    var habitName: String,
    var habitIcon: String,
    var habitTime: String,
    var duration: Long,
    var remainingTime:Long,
    var priority:Int,
    var color: Int
    ){
        @PrimaryKey(autoGenerate = true)
        var id = 0
    }