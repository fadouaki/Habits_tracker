package com.improveskill.habitstracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface habitDAO {
    @Insert
    fun insertCategory(habit: habit): Completable

    @Update
    fun updateCategory(habit: habit): Completable

    @Query("SELECT * FROM table_habit")
    fun getCategories(): Single<List<habit>>

/*    @Query("SELECT * FROM table_habit WHERE habitName LIKE :query")
    fun searchByTopic(query: String?): Single<List<habit?>?>?*/

}