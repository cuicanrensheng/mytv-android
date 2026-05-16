package top.yogiczy.mytv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import top.yogiczy.mytv.activities.MainActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action && top.yogiczy.mytv.data.SettingManager.bootStart) {
            val i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        }
    }
}
