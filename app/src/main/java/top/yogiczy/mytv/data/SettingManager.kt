package top.yogiczy.mytv.data

import android.content.Context
import android.content.SharedPreferences

object SettingManager {
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences("tv_setting", Context.MODE_PRIVATE)
    }

    var reverseChannel: Boolean
        get() = sp.getBoolean("reverse_channel", false)
        set(value) = sp.edit().putBoolean("reverse_channel", value).apply()

    var cacheTime: Int
        get() = sp.getInt("cache_time", 300)
        set(value) = sp.edit().putInt("cache_time", value).apply()

    var bootStart: Boolean
        get() = sp.getBoolean("boot_start", true)
        set(value) = sp.edit().putBoolean("boot_start", value).apply()
}
