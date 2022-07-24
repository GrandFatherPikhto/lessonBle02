package com.grandfatherpikhto.blin

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class BleScanManager constructor(private val context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO)
    : DefaultLifecycleObserver {

    enum class State(val value: Int) {
        Stopped(0x00),
        Scanning(0x01),
        Error(0xFF)
    }

    private val logTag = this.javaClass.simpleName

    private val scope = CoroutineScope(dispatcher)

    private val scanResults = mutableListOf<ScanResult>()
    val results get() = scanResults.toList()

    val applicationContext:Context get() = context.applicationContext
    private val bluetoothLeScanner =
        (context.applicationContext
            .getSystemService(Context.BLUETOOTH_SERVICE)
                as BluetoothManager).adapter.bluetoothLeScanner

    private val bcScanReceiver = BcScanReceiver(this, dispatcher)
    private val pendingIntent  = bcScanReceiver.pendingIntent

    private val mutableSharedFlowScanResult = MutableSharedFlow<ScanResult>(replay = 10)
    val sharedFlowScanResult get() = mutableSharedFlowScanResult.asSharedFlow()

    private val mutableStateFlowScanState = MutableStateFlow(State.Stopped)
    val stateFlowScanState get() = mutableStateFlowScanState.asStateFlow()
    val scanState get() = mutableStateFlowScanState.value

    private val mutableStateFlowError = MutableStateFlow(-1)
    val stateFlowError get() = mutableStateFlowError.asSharedFlow()
    val error get() = mutableStateFlowError.value

    private var stopTimeout = 0L

    private val scanFilters = mutableListOf<ScanFilter>()
    private val scanSettingsBuilder = ScanSettings.Builder()

    private var scanIdling:ScanIdling? = null
    fun getScanIdling() : ScanIdling {
        val idling = ScanIdling.getInstance()
        if (scanIdling == null) {
            scanIdling = idling
            scope.launch {
                scanIdling?.let { idling ->
                    sharedFlowScanResult.collect {
                        idling.scanned = true
                    }
                }
            }
        }
        return idling
    }

    init {
        initScanSettings()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        applicationContext.registerReceiver(bcScanReceiver, makeIntentFilters())
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        applicationContext.unregisterReceiver(bcScanReceiver)
    }

    fun onReceiveError(errorCode: Int) {
        mutableStateFlowError.tryEmit(errorCode)

    }

    fun onReceiveScanResult(scanResult: ScanResult) {
        if (!scanResults.map { it.device }.contains(scanResult.device)) {
            scanResults.add(scanResult)
            mutableSharedFlowScanResult.tryEmit(scanResult)
        }
    }

    fun startScan(stopTimeout: Long = 10000L) : Boolean {
        if (scanState == State.Stopped || scanState == State.Error) {
            this.stopTimeout = stopTimeout

            if (stopTimeout > 0) {
                scope.launch {
                    this@BleScanManager.stopTimeout = stopTimeout
                    delay(stopTimeout)
                    stopScan()
                }
            }

            val result = bluetoothLeScanner.startScan(
                scanFilters,
                scanSettingsBuilder.build(),
                pendingIntent
            )
            if (result == 0) {
                mutableStateFlowScanState.tryEmit(State.Scanning)
                return true
            } else {
                mutableStateFlowScanState.tryEmit(State.Error)
            }
        }

        return false
    }

    fun stopScan() {
        if (scanState == State.Scanning) {
            bluetoothLeScanner.stopScan(pendingIntent)
            mutableStateFlowScanState.tryEmit(State.Stopped)
        }
    }

    private fun initScanSettings() {
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        // setReportDelay() -- отсутствует. Не вызывать! Ответ приходит ПУСТОЙ!
        // В официальной документации scanSettingsBuilder.setReportDelay(1000)
        scanSettingsBuilder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        scanSettingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        scanSettingsBuilder.setLegacy(false)
        scanSettingsBuilder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
    }

    private fun makeIntentFilters() : IntentFilter = IntentFilter().let { intentFilter ->
        intentFilter.addAction(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction(BcScanReceiver.ACTION_BLE_SCAN)
        intentFilter
    }
}