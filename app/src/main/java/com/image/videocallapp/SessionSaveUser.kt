package com.image.videocallapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


/**
 * This common class to store the require data by using SharedPreferences.
 */
class SessionSaveUser(context: Context) {
    var context: Context? = context
    var sharedpreferences: SharedPreferences
    var editor: SharedPreferences.Editor
    private var instance: SessionSaveUser? = null

    fun getInstance(): SessionSaveUser? {
        if (instance == null) {
            instance = context?.let { SessionSaveUser(it) }
        } else {
            instance!!.context = null
            instance!!.context = context
        }
        return instance
    }

    fun putSharedString(KEY: String?, value: String?) {
        if (!isValid(sharedpreferences)) sharedpreferences = context!!.getSharedPreferences(
            PREFERENCES, Context.MODE_PRIVATE
        )
        if (!isValid(editor)) editor = sharedpreferences.edit()
        editor.putString(KEY, value)
        editor.apply()
    }

    fun getSharedString(KEY: String?, defValue: String?): String? {
        if (!isValid(sharedpreferences)) sharedpreferences = context!!.getSharedPreferences(
            PREFERENCES, Context.MODE_PRIVATE
        )
        if (!isValid(editor)) editor = sharedpreferences.edit()
        return sharedpreferences.getString(KEY, defValue)
    }

    fun getSharedBoolean(KEY: String?, defValue: Boolean): Boolean {
        if (!isValid(sharedpreferences)) sharedpreferences = context!!.getSharedPreferences(
            PREFERENCES, Context.MODE_PRIVATE
        )
        if (!isValid(editor)) editor = sharedpreferences.edit()
        return sharedpreferences.getBoolean(KEY, defValue)
    }

    fun putSharedBoolean(KEY: String?, value: Boolean) {
        if (!isValid(sharedpreferences)) sharedpreferences = context!!.getSharedPreferences(
            PREFERENCES, Context.MODE_PRIVATE
        )
        if (!isValid(editor)) editor = sharedpreferences.edit()
        editor.putBoolean(KEY, value).apply()
    }

    fun isValid(text: Any?): Boolean {
        return if (text != null) true else false
    }


    /*fun saveUser(root: UserData?) {
        try {
            val gson = Gson()
            val json = gson.toJson(root)
            putSharedString(USER, json)
        } catch (e: Exception) {
        }
    }

    val user: UserData?
        get() {
            var loginRoot: UserData? = null
            try {
                val jsonString = getSharedString(USER, "")
                val gson = GsonBuilder().setPrettyPrinting().create()
                loginRoot = gson.fromJson(jsonString, UserData::class.java)
            } catch (e: Exception) {
                e.message
            }
            return loginRoot
        }*/

    companion object {
        const val PREFERENCES = "user_preference"
        const val USER = "user"

        // Delete Data's from SharedPreferences
        fun deleteSession(context: Context) {
            val editor = context.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE).edit()
            editor.clear()
            editor.apply()
        }
    }

    init {
        sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        editor = sharedpreferences.edit()
    }
}
