package com.grandfatherpikhto.lessonble02.ui.fragments

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.grandfatherpikhto.lessonble02.R
import com.grandfatherpikhto.lessonble02.databinding.FragmentDeviceBinding
import com.grandfatherpikhto.lessonble02.helper.linkMenu
import com.grandfatherpikhto.lessonble02.ui.models.MainActivityViewModel

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DeviceFragment : Fragment() {

    private val logTag = this.javaClass.simpleName
    private var _binding: FragmentDeviceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_device, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.action_to_scanner -> {
                    findNavController().navigate(R.id.action_DeviceFragment_to_ScanFragment)
                    true
                }
                else -> { false }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDeviceBinding.inflate(inflater, container, false)

        linkMenu(true, menuProvider)

        binding.apply {
            mainActivityViewModel.scanResult?.let { scanResult ->
                includeLayout.tvBleName.text = scanResult.device.name
                    ?: getString(R.string.ble_unknown_name)
                includeLayout.tvBleAddress.text = scanResult.device.address
                includeLayout.tvBleRssi.text = getString(R.string.ble_rssi_title, scanResult.rssi)

                if (scanResult.isConnectable) {
                    includeLayout.ivConnectable.setImageResource(R.drawable.ic_connectable)
                } else {
                    includeLayout.ivConnectable.setImageResource(R.drawable.ic_no_connectable)
                }
                if (scanResult.device.bondState == BluetoothDevice.BOND_BONDED) {
                    includeLayout.ivPaired.setImageResource(R.drawable.ic_paired)
                } else {
                    includeLayout.ivPaired.setImageResource(R.drawable.ic_unpaired)
                }

                Log.d(logTag, "${scanResult.device.uuids}")
            }
        }


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        linkMenu(false, menuProvider)
        _binding = null
    }
}