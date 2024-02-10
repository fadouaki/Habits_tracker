package com.improveskill.habitstracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.improveskill.habitstracker.Adapter.habitAdapter
import com.improveskill.habitstracker.database.HabitDataBase
import com.improveskill.habitstracker.database.habit
import com.improveskill.habitstracker.databinding.ActivityMainBinding
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    lateinit var postDataBase: HabitDataBase
    lateinit var adapter: habitAdapter
    lateinit var Habits: List<habit>

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        Habits = ArrayList()
        postDataBase = HabitDataBase.getInstance(this)

        postDataBase.postDao().getHabits()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<habit>> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess(t: List<habit>) {
                    Habits = t
                    adapter = habitAdapter(this@MainActivity, Habits)
                    Log.d("HabitTAki", " size ${t.size}")
                    bind.recyclerHabit.layoutManager = LinearLayoutManager(this@MainActivity)
                    bind.recyclerHabit.adapter = adapter

                }

            })
        // Set layout manager and adapter


        bind.addItem.setOnClickListener {

            showAddDialog()

        }

        bind.Renew.setOnClickListener {
            showRenewDataDialog(this@MainActivity)

        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddDialog() {
        val SettingsDialog: Dialog = Dialog(this)
        SettingsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SettingsDialog.setContentView(R.layout.dialog_add_item)

        val name = SettingsDialog.findViewById<EditText>(R.id.name_habit)
        val hoursDuration = SettingsDialog.findViewById<EditText>(R.id.hour_habit)
        val minutesDuration = SettingsDialog.findViewById<EditText>(R.id.min_habit)

        SettingsDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        SettingsDialog.findViewById<Button>(R.id.add_habit).setOnClickListener {

            if (!name.text.isEmpty() && !minutesDuration.text.isEmpty() && !hoursDuration.text.isEmpty()) {

                val duration: Int = (hoursDuration.text.toString()
                    .toInt() * 3600) + minutesDuration.text.toString().toInt() * 60
                (Habits as ArrayList).add(habit(name.text.toString(), duration, 0))
                adapter.notifyDataSetChanged()
                postDataBase.postDao()
                    .insertHabit(habit(name.text.toString(), duration, 0))
                    .subscribeOn(Schedulers.computation())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                            Log.d("HabitTAki", " add habit onSubscribe ")
                        }

                        @SuppressLint("NotifyDataSetChanged")
                        override fun onComplete() {
                            Log.d("HabitTAki", " add habit completed ")
                            SettingsDialog.dismiss()

                        }

                        override fun onError(e: Throwable) {
                            Log.d("HabitTAki", " add habit onError ")
                            Toast.makeText(this@MainActivity, "Error...", Toast.LENGTH_LONG)
                                .show()
                            SettingsDialog.dismiss()

                        }
                    })
            } else

                Toast.makeText(this@MainActivity, "Please fill out all fields!", Toast.LENGTH_LONG)
                    .show()
        }
        SettingsDialog.show()
    }

    fun showRenewDataDialog(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)

        // Set the title and message
        alertDialogBuilder.setTitle("Renew Data")
        alertDialogBuilder.setMessage("you want to start new day ?")

        // Set the positive button
        alertDialogBuilder.setPositiveButton("Ok") { dialog, which ->
            for (h in Habits) {
                h.remainingTime = 0
                postDataBase.postDao().updateHabit(h)
                    .subscribeOn(Schedulers.computation())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onComplete() {
                        }

                        override fun onError(e: Throwable) {
                        }

                    })
            }
            adapter.notifyDataSetChanged()
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