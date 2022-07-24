package com.grandfatherpikhto.lessonble02.ui.models

import androidx.lifecycle.ViewModel
import com.grandfatherpikhto.blin.BleScanManager

class ScannerViewModel constructor(bleScanManager: BleScanManager) : ViewModel() {
    val sharedFlowScanResult = bleScanManager.sharedFlowScanResult
    val stateFLowScanState   = bleScanManager.stateFlowScanState
    val scanState            = bleScanManager.scanState
    val stateFlowError       = bleScanManager.stateFlowError
    val error                = bleScanManager.error
}