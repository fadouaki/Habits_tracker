package com.improveskills.habitstracker.Ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.improveskills.habitstracker.Adapter.habitAdapter
import com.improveskills.habitstracker.Interface.OnComplite
import com.improveskills.habitstracker.R
import com.improveskills.habitstracker.SharedPrefData
import com.improveskills.habitstracker.database.HabitDataBase
import com.improveskills.habitstracker.database.habit
import com.improveskills.habitstracker.databinding.ActivityMainBinding
import com.polidea.rxandroidble2.exceptions.BleException
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.Calendar
import kotlin.random.Random


class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var bind: ActivityMainBinding
    lateinit var postDataBase: HabitDataBase
    lateinit var adapter: habitAdapter
    lateinit var Habits: List<habit>
    private var duration = 1
    lateinit var sharedPrefData: SharedPrefData

    private var startTimeMinute: Int = 0
    private var endTimeMinute: Int = 0
    lateinit var endTimeText: TextView
    lateinit var startTimeText: TextView


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        RxJavaPlugins.setErrorHandler { throwable ->
            if (throwable is UndeliverableException && throwable.cause is BleException) {
                return@setErrorHandler // ignore BleExceptions since we do not have subscriber
            } else {
                throw throwable
            }
        }
        bind.addTask.setOnClickListener {
            showAddDialog()
        }
        bind.resetData.setOnClickListener {
            showRenewDataDialog(this)
        }
        Habits = ArrayList()
        postDataBase = HabitDataBase.getInstance(this)
        sharedPrefData = SharedPrefData(this)

        postDataBase.postDao().getHabits()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<habit>> {
                override fun onSubscribe(d: Disposable) {
                    Log.d("HabitTAki", " size dis")
                }

                override fun onError(e: Throwable) {
                    Log.d("HabitTAki", " size ${e.toString()}")
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess(t: List<habit>) {
                    Habits = t
                    adapter = habitAdapter(this@MainActivity, Habits, object : OnComplite {
                        override fun onComplite(priority: Int) {
                            runOnUiThread {
                                Log.d("HabitTAki", " totole ${sharedPrefData.LoadInt("TotalPriority")}")
                                UpgradeProductivity(priority)
                            }

                        }

                    })
                    Log.d("HabitTAki", " size ${t.size}")
                    bind.recyclerHabit.layoutManager = LinearLayoutManager(this@MainActivity)
                    bind.recyclerHabit.adapter = adapter

                }

            })

        getProgress()
    }
    /*  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
          menuInflater.inflate(R.menu.menu_main, menu)
          return true
      }*/

    /*    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.Add_Item -> {
                    showAddDialog()
                    return true
                }
                R.id.Reset_data -> {
                    showRenewDataDialog(this@MainActivity)
                    return true
                }
                else -> return super.onOptionsItemSelected(item)
            }
        }*/

    fun UpgradeProductivity(Priority: Int) {
        val percentage: Float =
            Priority.toFloat() / sharedPrefData.LoadInt("TotalPriority").toFloat()
        val progressStatus = sharedPrefData.LoadInt("Progress")

        bind.progressBar.progress = (progressStatus + percentage * bind.progressBar.max).toInt()
        if (bind.progressBar.progress > 97)
            bind.progressBar.progress = 100
        bind.emojiTextView.text = "${bind.progressBar.progress}%"

        /*  // Update position of the emoji based on progress
          val progressBarWidth =
              bind.progressBar.width - bind.progressBar.paddingLeft - bind.progressBar.paddingRight
          val layoutParams = bind.emojiTextView.layoutParams as LinearLayout.LayoutParams

          layoutParams.leftMargin =
              (progressStatus * progressBarWidth / 100 + progressBarWidth * percentage).toInt() - bind.emojiTextView.width / 2
          Log.d("HabitTAki", "    layoutParams.leftMargin  ${layoutParams.leftMargin} progressBarWidth $progressBarWidth ")
          if (layoutParams.leftMargin > progressBarWidth * 2 / 3) {
              bind.emojiTextView.text = "\uD83C\uDFC5"
              layoutParams.leftMargin -= 15
          } else if (layoutParams.leftMargin > progressBarWidth / 3)
              bind.emojiTextView.text = "✌️"
          else
              bind.emojiTextView.text = "\uD83D\uDE11"
          if (progressStatus < bind.progressBar.max) {
              if (layoutParams.leftMargin < 0)
                  layoutParams.leftMargin = 0
              bind.emojiTextView.layoutParams = layoutParams
          }*/
        sharedPrefData.SaveInt("Progress", bind.progressBar.progress)
    }

    fun getProgress() {

        val progressStatus = sharedPrefData.LoadInt("Progress")
        bind.progressBar.progress = progressStatus
        if (bind.progressBar.progress > 97)
            bind.progressBar.progress = 100
        bind.emojiTextView.text = "${bind.progressBar.progress}%"
        /*      // Update position of the emoji based on progress
              bind.progressBar.post {
                  val progressBarWidth = bind.progressBar.width - bind.progressBar.paddingLeft - bind.progressBar.paddingRight
                  val layoutParams = bind.emojiTextView.layoutParams as LinearLayout.LayoutParams

                  layoutParams.leftMargin =
                      ( progressBarWidth*progressStatus/100 ).toInt() - bind.emojiTextView.width / 2

                  if (layoutParams.leftMargin > progressBarWidth * 2 / 3) {
                      bind.emojiTextView.text = "\uD83C\uDFC5"
                      layoutParams.leftMargin -= 15
                  } else if (layoutParams.leftMargin > progressBarWidth / 3)
                      bind.emojiTextView.text = "✌️"
                  else
                      bind.emojiTextView.text = "\uD83D\uDE11"

                  if (progressStatus < bind.progressBar.max) {
                      if (layoutParams.leftMargin < 0)
                          layoutParams.leftMargin = 0
                      bind.emojiTextView.layoutParams = layoutParams
                  }
              }*/


    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddDialog() {
        if (sharedPrefData.LoadInt("Progress") == 0) {
            var priority: Int = 1
            val SettingsDialog: Dialog = Dialog(this)
            SettingsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            SettingsDialog.setContentView(R.layout.dialog_add_item)

            val name = SettingsDialog.findViewById<EditText>(R.id.name_habit)
            startTimeText = SettingsDialog.findViewById<TextView>(R.id.startTime)
            endTimeText = SettingsDialog.findViewById<TextView>(R.id.endTime)
            name.requestFocus()
            SettingsDialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            SettingsDialog.findViewById<RadioGroup>(R.id.radioGroup)
                .setOnCheckedChangeListener { _er, checkedId ->
                    val radioButton = _er.findViewById<RadioButton>(checkedId)
                    if (radioButton != null) {
                        priority = radioButton.tag.toString().toInt()
                        Log.d("HabitTAki", " priority $priority")

                        // Continue with your logic...
                    }
                }
            SettingsDialog.findViewById<CheckBox>(R.id.checkBox)
                .setOnCheckedChangeListener { buttonView, isChecked ->
                    if (!isChecked) {
                        duration = 1

                    } else {
                        duration = calculateDifferenceInSeconds()
                    }
                    Log.d("HabitTAki", " timer difference ${calculateDifferenceInSeconds()}")
                }
            endTimeText.setOnClickListener {
                showTimePickerDialog(false)
            }
            startTimeText.setOnClickListener {
                showTimePickerDialog(true)
            }
            SettingsDialog.findViewById<TextView>(R.id.add_habit).setOnClickListener {
                //    val Cardcolor:Int=generateRandomColor()
                val Cardcolor: Int = generateRandomColor()

                if (!name.text.isEmpty()) {
                    (Habits as ArrayList).add(
                        habit(
                            name.text.toString(),
                            "${endTimeText.text.toString()} -> ${startTimeText.text.toString()}",
                            (duration * 1000).toLong(),
                            0L,
                            priority,
                            Cardcolor
                        )
                    )
                    sharedPrefData.SaveInt(
                        "TotalPriority",
                        sharedPrefData.LoadInt("TotalPriority") + priority
                    )
                    adapter.notifyDataSetChanged()
                    postDataBase.postDao()
                        .insertHabit(
                            habit(
                                name.text.toString(),
                                "${endTimeText.text.toString()} -> ${startTimeText.text.toString()} ",
                                (duration * 1000).toLong(),
                                0,
                                priority,
                                Cardcolor
                            )
                        )
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

                    duration = 1
                } else

                    Toast.makeText(
                        this@MainActivity,
                        "Please fill out all fields!",
                        Toast.LENGTH_LONG
                    )
                        .show()
            }

            SettingsDialog.show()
        }else Toast.makeText(
            this@MainActivity,
            "It is better to reset all data after you can delete it to calculate your productivity",
            Toast.LENGTH_LONG
        ).show()
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
                            sharedPrefData.SaveInt("Progress", 0)
                            runOnUiThread {
                                UpgradeProductivity(0)
                            }

                        }

                        override fun onError(e: Throwable) {
                        }

                    })
            }
            adapter.notifyDataSetChanged()
            bind.imageReset.animate().rotation(180f).start();
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

    fun generateRandomColor(): Int {
        val random = Random.Default
        val red = random.nextInt(200, 256) // Generate random value for red (128-255)
        val green = random.nextInt(200, 256) // Generate random value for green (128-255)
        val blue = random.nextInt(200, 256) // Generate random value for blue (128-255)
        return Color.rgb(red, green, blue) // Combine components and return color
    }

    private fun showTimePickerDialog(isStartTime: Boolean) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        // Create a TimePickerDialog with a listener for time set events
        val timePickerDialog = TimePickerDialog(
            this,
            this,  // Implement OnTimeSetListener interface
            hour,  // Initial hour
            minute,  // Initial minute
            true  // 24-hour format
        )

        // Set the title of the time picker dialog (optional)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.window?.decorView?.tag = isStartTime

        // Show the time picker dialog
        timePickerDialog.show()
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        // This method is called when the user selects a time in the dialog
        if (view?.rootView?.tag as? Boolean == true) {
            // Selected start time
            startTimeMinute =hourOfDay* 60+ minute
            startTimeText.text = "$hourOfDay:$minute"
        } else {
            // Selected end time
            endTimeMinute = hourOfDay* 60+ minute
            endTimeText.text = "$hourOfDay:$minute"


        }


    }

    private fun calculateDifferenceInSeconds(): Int {
        // Calculate the difference in Seconds
        return if (endTimeMinute >= startTimeMinute) {
            (endTimeMinute - startTimeMinute)*60
        } else {
            ( (endTimeMinute + 24 * 60) - startTimeMinute)*60
        }
    }

}