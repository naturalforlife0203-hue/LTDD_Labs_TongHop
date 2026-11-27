package com.example.water_me // Đổi đúng theo tên package của bạn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

data class Plant(val name: String, val schedule: String, val type: String, val description: String)

class WaterViewModel(application: Application) : AndroidViewModel(application) {
    private val workManager = WorkManager.getInstance(application)

    val plants = listOf(
        Plant("Lithops", "Hàng tháng", "Cây mọng nước", "Cây đá sống vùng sa mạc"),
        Plant("Cà rốt", "Hàng ngày", "Rau củ", "Cây ăn củ giòn ngọt"),
        Plant("Hoa mẫu đơn", "Hàng tuần", "Hoa", "Hoa nở rực rỡ vào mùa xuân"),
        Plant("Dâu tây", "Hàng ngày", "Cây ăn quả", "Cây bụi cho quả mọng ngọt")
    )

    fun scheduleReminder(duration: Long, unit: TimeUnit, plantName: String) {
        val data = Data.Builder()
            .putString(WaterReminderWorker.nameKey, plantName)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(duration, unit)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            plantName + duration,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}