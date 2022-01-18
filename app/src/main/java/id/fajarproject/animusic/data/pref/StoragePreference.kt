package id.fajarproject.animusic.data.pref

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.fajarproject.animusic.data.network.model.MusicItem
import java.lang.reflect.Type


/**
 * Created by Fajar Adi Prasetyo on 11/08/2020.
 */

class StoragePreference(private val context: Context) {
    private val STORAGE = "id.fajarproject.animusic.STORAGE"
    private var preferences: SharedPreferences? = null

    fun storeAudio(arrayList: MutableList<MusicItem?>?) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor?.putString("audioArrayList", json)
        editor?.apply()
    }

    fun loadAudio(): MutableList<MusicItem?>? {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences?.getString("audioArrayList", null)
        val type: Type = object : TypeToken<MutableList<MusicItem?>?>() {}.type
        return gson.fromJson(json, type)
    }

    fun storeAudioIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        editor?.putInt("audioIndex", index)
        editor?.apply()
    }

    fun storeAudioType(type: String) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        editor?.putString("audioType", type)
        editor?.apply()
    }

    fun loadAudioIndex(): Int? {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences?.getInt("audioIndex", -1) //return -1 if no data found
    }

    fun loadAudioType(): String? {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences?.getString("audioType", "")
    }

    fun clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        editor?.clear()
        editor?.apply()
    }

}