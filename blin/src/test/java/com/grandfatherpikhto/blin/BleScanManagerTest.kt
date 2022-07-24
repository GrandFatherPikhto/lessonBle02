package com.grandfatherpikhto.blin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BleScanManagerTest {
    private lateinit var closeable:AutoCloseable
    private val bleScanManager =
        BleScanManager(
            ApplicationProvider.getApplicationContext<Context?>().applicationContext,
            UnconfinedTestDispatcher()
        )

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
        closeable.close()
    }

    @Test
    fun testScan() = runTest (UnconfinedTestDispatcher()) {
        val results = mockRandomScanResults(7)
        bleScanManager.startScan()
        assertEquals(BleScanManager.State.Scanning, bleScanManager.scanState)
        results.forEach { scanResult ->
            bleScanManager.onReceiveScanResult(scanResult)
            println("BLE: ${scanResult.device.name} ${scanResult.device.address}")
        }
        bleScanManager.stopScan()
        assertEquals(results.map { it.device }.toList(),
            bleScanManager.results.map { it.device }.toList())
        assertEquals(BleScanManager.State.Stopped, bleScanManager.scanState)
    }
}