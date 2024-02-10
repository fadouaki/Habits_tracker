package com.improveskill.habitstracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface habitDAO {
    @Insert
    fun insertHabit(habit: habit): Completable

    @Update
    fun updateHabit(habit: habit): Completable

    @Delete
    fun deleteHabit(habit: habit): Completable

    @Query("SELECT * FROM table_habit")
    fun getHabits(): Single<List<habit>>

/*    @Query("SELECT * FROM table_habit WHERE habitName LIKE :query")
    fun searchByTopic(query: String?): Single<List<habit?>?>?*/

}