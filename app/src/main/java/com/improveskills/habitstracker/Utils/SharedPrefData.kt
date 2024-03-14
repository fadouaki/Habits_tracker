package com.improveskills.habitstracker.Utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefData(context: Context) {
    var mySharedPref: SharedPreferences

    init {
        mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE)
    }

    fun SaveInt(key: String?, state: Int) {
        val editor = mySharedPref.edit()
        editor.putInt(key, state)
        editor.commit()
    }

    // this method will load the Night Mode State
    fun LoadInt(key: String?): Int {
        return mySharedPref.getInt(key, 0)
    }

    fun SaveBoolean(key: String?, state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean(key, state!!)
        editor.commit()
    }

    // this method will load the Night Mode State
    fun LoadBoolean(key: String?): Boolean {
        return mySharedPref.getBoolean(key, false)
    }

    fun SaveString(key: String?, state: String?) {
        val editor = mySharedPref.edit()
        editor.putString(key, state)
        editor.commit()
    }

    fun LoadString(key: String?): String? {
        return mySharedPref.getString(key, "")
    }

    fun clear() {
        mySharedPref.edit().clear().apply()
    }
}