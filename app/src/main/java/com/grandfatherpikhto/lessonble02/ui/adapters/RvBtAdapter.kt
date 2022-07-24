package com.grandfatherpikhto.lessonble02.ui.adapters

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonble02.R

typealias OnClickBtDevice<T> = (ScanResult, View) -> Unit
typealias OnLongClickBtDevice<T> = (ScanResult, View) -> Unit

class RvBtAdapter : RecyclerView.Adapter<RvBtHolder>() {
    private val scanResults = mutableListOf<ScanResult>()
    private var onClickBtDevice: OnClickBtDevice<ScanResult>? = null
    private var onLongClickBtDevice: OnLongClickBtDevice<ScanResult>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvBtHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_ble_device, parent, false)

        return RvBtHolder(view)
    }

    override fun onBindViewHolder(holder: RvBtHolder, position: Int) {
        holder.itemView.setOnClickListener { view ->
            onClickBtDevice?.let { listener ->
                listener(scanResults[position], view)
            }
        }

        holder.itemView.setOnLongClickListener { view ->
            onLongClickBtDevice?.let { listener ->
                listener(scanResults[position], view)
            }
            true
        }
        holder.bind(scanResults[position])
    }

    override fun getItemCount(): Int = scanResults.size

    fun addScanResult(scanResult: ScanResult) {
        if (!scanResults.map { it.device }.contains(scanResult.device)) {
            scanResults.add(scanResult)
            notifyItemInserted(scanResults.indexOf(scanResult))
        }
    }

    fun setOnClickBtDevice(onClickBtDevice: OnClickBtDevice<ScanResult>) {
        this.onClickBtDevice = onClickBtDevice
    }

    fun setOnLongClickBtDevice(onLongClickBtDevice: OnLongClickBtDevice<ScanResult>) {
        this.onLongClickBtDevice = onLongClickBtDevice
    }
}