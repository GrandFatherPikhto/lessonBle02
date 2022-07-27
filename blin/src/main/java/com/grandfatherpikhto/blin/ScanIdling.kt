package com.grandfatherpikhto.blin

import androidx.test.espresso.IdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class ScanIdling constructor(private val bleScanManager: BleScanManager) : IdlingResource {
    companion object {
        private var scanIdling: ScanIdling? = null
        fun getInstance(bleScanManager: BleScanManager) : ScanIdling {
            return scanIdling ?: ScanIdling(bleScanManager)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    private var isIdling = AtomicBoolean(true)

    init {
        scope.launch {
            bleScanManager.stateFlowScanState.collect { state ->
                when(state) {
                    BleScanManager.State.Stopped -> {
                        isIdling.set(true)
                        resourceCallback?.let { callback ->
                            callback.onTransitionToIdle()
                        }
                    }
                    BleScanManager.State.Scanning -> {
                        isIdling.set(false)
                    }
                    else -> { }
                }
            }
        }
        scope.launch {
            bleScanManager.sharedFlowScanResult.collect {
                if (it.isConnectable) {
                    isIdling.set(true)
                    resourceCallback?.let { callback ->
                        callback.onTransitionToIdle()
                    }
                }
            }
        }
    }

    override fun getName(): String = this.javaClass.simpleName

    override fun isIdleNow(): Boolean = isIdling.get()

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}