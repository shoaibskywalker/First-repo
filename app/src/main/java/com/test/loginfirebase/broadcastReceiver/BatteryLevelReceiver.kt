package com.test.loginfirebase.broadcastReceiver

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.widget.Toast

class BatteryLevelReceiver : BroadcastReceiver() {

    private var previousBatteryLevel: Int = -1

    override fun onReceive(p0: Context?, p1: Intent?) {

        val level = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = p1?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = p1?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Calculate the current battery percentage
        if (scale!! > 0) {
            val batteryPct = (level!! * 100) / scale

            // Only show the toast if the battery level has changed
            if (level != previousBatteryLevel) {
//                Toast.makeText(p0, "Battery Level: $batteryPct%", Toast.LENGTH_SHORT).show()
                // Show dialog if battery level touches 50% and it's different from the previous level
                if (level == 15 && level != previousBatteryLevel && !isCharging) {
                    previousBatteryLevel = level
                    showLowBatteryDialog(
                        context = p0!!,
                        title = "Low Battery",
                        message = "Battery level is at 15%. Please charge your device."
                    )
                }

                if (level == 10 && level != previousBatteryLevel && !isCharging) {
                    previousBatteryLevel = level
                    showLowBatteryDialog(
                        context = p0!!,
                        title = "Low Battery Warning",
                        message = "Battery level is at 10%. Please charge your device."
                    )
                }
            }
        }

    }

    private fun showLowBatteryDialog(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}