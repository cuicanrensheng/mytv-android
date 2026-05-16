package top.yogiczy.mytv.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SourceManager {
    private lateinit var sp: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        sp = context.getSharedPreferences("tv_source", Context.MODE_PRIVATE)
    }

    var sourceHistory: MutableList<String>
        get() = gson.fromJson(sp.getString("source_history", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        set(value) = sp.edit().putString("source_history", gson.toJson(value)).apply()

    var epgHistory: MutableList<String>
        get() = gson.fromJson(sp.getString("epg_history", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        set(value) = sp.edit().putString("epg_history", gson.toJson(value)).apply()

    var validDomain: MutableSet<String>
        get() = gson.fromJson(sp.getString("valid_domain", "[]"), object : TypeToken<MutableSet<String>>() {}.type)
        set(value) = sp.edit().putString("valid_domain", gson.toJson(value)).apply()

    var collectChannel: MutableSet<Int>
        get() = gson.fromJson(sp.getString("collect_channel", "[]"), object : TypeToken<MutableSet<Int>>() {}.type)
        set(value) = sp.edit().putString("collect_channel", gson.toJson(value)).apply()

    fun saveCustomSource(url: String) {
        val list = sourceHistory
        if (!list.contains(url)) list.add(url)
        sourceHistory = list
    }

    fun removeFailSource(url: String) {
        val list = sourceHistory
        list.remove(url)
        sourceHistory = list
    }

    fun saveCustomEpg(url: String) {
        val list = epgHistory
        if (!list.contains(url)) list.add(url)
        epgHistory = list
    }
}
