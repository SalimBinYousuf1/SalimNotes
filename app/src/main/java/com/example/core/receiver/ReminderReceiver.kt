package com.example.core.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SalimDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "salim_reminders_channel"
        const val ACTION_REMINDER = "com.salim.notes.ACTION_REMINDER"
        const val ACTION_SNOOZE = "com.salim.notes.ACTION_SNOOZE"
        const val ACTION_DONE = "com.salim.notes.ACTION_DONE"

        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"

        fun scheduleAlarm(context: Context, reminderId: String, noteId: String, title: String, triggerAt: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_NOTE_ID, noteId)
                putExtra(EXTRA_TITLE, title)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }

        fun cancelAlarm(context: Context, reminderId: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val db = SalimDatabase.getDatabase(context)

        when (action) {
            ACTION_REMINDER -> {
                val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
                val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Note Reminder"

                showNotification(context, reminderId, noteId, title)
            }

            ACTION_DONE -> {
                val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(reminderId.hashCode())

                CoroutineScope(Dispatchers.IO).launch {
                    db.reminderDao().markDone(reminderId)
                }
            }

            ACTION_SNOOZE -> {
                val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
                val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Note Reminder"

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(reminderId.hashCode())

                // Snooze for 10 minutes
                val newTrigger = System.currentTimeMillis() + (10 * 60 * 1000)
                scheduleAlarm(context, reminderId, noteId, title, newTrigger)
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // Reschedule pending alarms from database
                CoroutineScope(Dispatchers.IO).launch {
                    val pending = db.reminderDao().getPendingUpcomingReminders()
                    for (rem in pending) {
                        val note = db.noteDao().getNoteByIdSync(rem.noteId)
                        val title = note?.title?.ifBlank { "Note Reminder" } ?: "Note Reminder"
                        scheduleAlarm(context, rem.id, rem.noteId, title, rem.triggerAt)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, reminderId: String, noteId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Salim note reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap opens app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_NOTE_ID, noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done action
        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_DONE
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            ("done_$reminderId").hashCode(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTE_ID, noteId)
            putExtra(EXTRA_TITLE, title)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            ("snooze_$reminderId").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_starburst)
            .setContentTitle("Salim Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Done", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze (10m)", snoozePendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }
}
