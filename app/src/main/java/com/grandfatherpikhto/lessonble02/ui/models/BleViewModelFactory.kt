package com.grandfatherpikhto.lessonble02.ui.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grandfatherpikhto.lessonble02.LessonBle02App
import java.lang.Exception

@Suppress("UNCHECKED_CAST")
class BleViewModelFactory constructor(private val application: Application)
    : ViewModelProvider.Factory {
    private val logTag = this.javaClass.simpleName
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        (application.applicationContext as LessonBle02App).bleScanManager?.let {
            return ScannerViewModel(it) as T
        }
        throw Exception("Не создан объект BleScanManager")
    }
}
