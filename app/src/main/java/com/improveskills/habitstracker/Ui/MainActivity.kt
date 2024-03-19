package com.improveskills.habitstracker.Ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.improveskills.habitstracker.Adapter.IconsAdapter
import com.improveskills.habitstracker.Adapter.habitAdapter
import com.improveskills.habitstracker.Interface.GetImage
import com.improveskills.habitstracker.Interface.OnComplite
import com.improveskills.habitstracker.R
import com.improveskills.habitstracker.Utils.ExFunctions
import com.improveskills.habitstracker.Utils.SharedPrefData
import com.improveskills.habitstracker.database.HabitDataBase
import com.improveskills.habitstracker.database.habit
import com.improveskills.habitstracker.databinding.ActivityMainBinding
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Calendar
import kotlin.random.Random


class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var bind: ActivityMainBinding
    private lateinit var postDataBase: HabitDataBase
    private lateinit var adapter: habitAdapter
    private lateinit var Habits: List<habit>
    private var duration = 1
    private lateinit var sharedPrefData: SharedPrefData

    private var startTimeMinute: Int = 0
    private var endTimeMinute: Int = 0
    private lateinit var endTimeText: TextView
    private lateinit var startTimeText: TextView
    private lateinit var bottomSheetDialog: BottomSheetDialog


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        bind.addTask.setOnClickListener {
            showAddDialog()
        }
        bind.resetData.setOnClickListener {
            showRenewDataDialog(this)
        }
        bind.btnChart.setOnClickListener {
            startActivity(Intent(this@MainActivity,MainChartActivity::class.java))
        }
        Habits = ArrayList()
        postDataBase = HabitDataBase.getInstance(this)
        sharedPrefData = SharedPrefData(this)

        postDataBase.postDao().getHabits()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<habit>> {
                override fun onSubscribe(d: Disposable) {
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
                                UpgradeProductivity(priority)
                            }
                        }

                    })
                    bind.recyclerHabit.layoutManager = LinearLayoutManager(this@MainActivity)
                    bind.recyclerHabit.adapter = adapter

                }

            })

        getProgress()
    }

    fun UpgradeProductivity(Priority: Int) {
        val percentage: Float =
            Priority.toFloat() / sharedPrefData.LoadInt("TotalPriority").toFloat()
        val progressStatus = sharedPrefData.LoadInt("Progress")

        bind.progressBar.progress = (progressStatus + percentage * bind.progressBar.max).toInt()
        if (bind.progressBar.progress > 97)
            bind.progressBar.progress = 100
        bind.emojiTextView.text = "${bind.progressBar.progress}%"

        sharedPrefData.SaveInt("Progress", bind.progressBar.progress)
    }

    fun getProgress() {

        val progressStatus = sharedPrefData.LoadInt("Progress")
        bind.progressBar.progress = progressStatus
        if (bind.progressBar.progress > 97)
            bind.progressBar.progress = 100
        bind.emojiTextView.text = "${bind.progressBar.progress}%"

    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddDialog() {
        var iconName: String = ""
        var habitTime: String = ""
        if (sharedPrefData.LoadInt("Progress") == 0) {
            var priority: Int = 1
            val AddDialog: Dialog = Dialog(this)
            AddDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            AddDialog.setContentView(R.layout.dialog_add_item)

            val name = AddDialog.findViewById<EditText>(R.id.name_habit)
            startTimeText = AddDialog.findViewById<TextView>(R.id.startTime)
            endTimeText = AddDialog.findViewById<TextView>(R.id.endTime)
            val IconImage = AddDialog.findViewById<ImageView>(R.id.addicon)
            IconImage.setOnClickListener {
                showSheetDialog(this@MainActivity, object : GetImage {
                    override fun getImage(imageRes: String) {
                        iconName = imageRes
                        IconImage.setImageDrawable(
                            ExFunctions().loadImageFromAssets(
                                this@MainActivity,
                                imageRes
                            )
                        )
                        bottomSheetDialog.dismiss()
                    }
                })
            }
            name.requestFocus()
            AddDialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            AddDialog.findViewById<RadioGroup>(R.id.radioGroup)
                .setOnCheckedChangeListener { _er, checkedId ->
                    val radioButton = _er.findViewById<RadioButton>(checkedId)
                    if (radioButton != null) {
                        priority = radioButton.tag.toString().toInt()
                    }
                }
            AddDialog.findViewById<CheckBox>(R.id.checkBox)
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
            AddDialog.findViewById<TextView>(R.id.add_habit).setOnClickListener {
                val Cardcolor: Int = generateRandomColor()

                if (!name.text.isEmpty()) {
                    if (!endTimeText.text.toString().isEmpty())
                        habitTime =
                            "${endTimeText.text.toString()} -> ${startTimeText.text.toString()}"

                    (Habits as ArrayList).add(
                        habit(
                            name.text.toString(),
                            iconName,
                            habitTime,
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
                                iconName,
                                habitTime,
                                (duration * 1000).toLong(),
                                0,
                                priority,
                                Cardcolor
                            )
                        )
                        .subscribeOn(Schedulers.computation())
                        .subscribe(object : CompletableObserver {
                            override fun onSubscribe(d: Disposable) {
                            }

                            @SuppressLint("NotifyDataSetChanged")
                            override fun onComplete() {
                                Log.d("HabitTAki", " add habit completed ")
                                AddDialog.dismiss()

                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(this@MainActivity, "Error...", Toast.LENGTH_LONG)
                                    .show()
                                AddDialog.dismiss()

                            }
                        })

                    duration = 1
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please fill out all fields!",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

            AddDialog.show()
        } else Toast.makeText(
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


    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) =
        // This method is called when the user selects a time in the dialog
        if (view?.rootView?.tag as? Boolean == true) {
            // Selected start time
            startTimeMinute = hourOfDay * 60 + minute
            startTimeText.text = "$hourOfDay:$minute"
        } else {
            // Selected end time
            endTimeMinute = hourOfDay * 60 + minute
            endTimeText.text = "$hourOfDay:$minute"

        }

    private fun calculateDifferenceInSeconds(): Int {
        // Calculate the difference in Seconds
        return if (endTimeMinute >= startTimeMinute) {
            (endTimeMinute - startTimeMinute) * 60
        } else {
            ((endTimeMinute + 24 * 60) - startTimeMinute) * 60
        }
    }

    fun showSheetDialog(context: Context, getImage: GetImage) {
        // Create a BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(context)

        // Inflate the layout for the dialog
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null)

        // Find RecyclerView in layout
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        // Set layout manager for RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 5)

        // Set adapter for RecyclerView
        val imageNames = (1..49).map { "$it.png" }
        val adapter = IconsAdapter(context, imageNames, getImage)
        recyclerView.adapter = adapter

        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }


}