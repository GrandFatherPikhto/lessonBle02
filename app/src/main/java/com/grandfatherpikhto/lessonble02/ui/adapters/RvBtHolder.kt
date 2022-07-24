package com.grandfatherpikhto.lessonble02.ui.adapters

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grandfatherpikhto.lessonble02.R
import com.grandfatherpikhto.lessonble02.databinding.LayoutBleDeviceBinding

class RvBtHolder (view: View) : RecyclerView.ViewHolder (view) {
    private val binding = LayoutBleDeviceBinding.bind(view)

    fun bind(scanResult: ScanResult) {
        binding.apply {
            tvBleAddress.text = scanResult.device.address
            tvBleName.text = scanResult.device.name ?: itemView.context.getString(R.string.ble_unknown_name)
            tvBleRssi.text = itemView.context.getString(R.string.ble_rssi_title, scanResult.rssi)
            if (scanResult.isConnectable) {
                ivConnectable.setImageResource(R.drawable.ic_connectable)
            } else {
                ivConnectable.setImageResource(R.drawable.ic_no_connectable)
            }
            if (scanResult.device.bondState == BluetoothDevice.BOND_BONDED) {
                ivPaired.setImageResource(R.drawable.ic_paired)
            } else {
                ivPaired.setImageResource(R.drawable.ic_unpaired)
            }
        }
    }
}