package com.grandfatherpikhto.lessonble02.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.grandfatherpikhto.blin.BleScanManager
import com.grandfatherpikhto.lessonble02.LessonBle02App
import com.grandfatherpikhto.lessonble02.R
import com.grandfatherpikhto.lessonble02.databinding.FragmentScanBinding
import com.grandfatherpikhto.lessonble02.helper.linkMenu
import com.grandfatherpikhto.lessonble02.ui.adapters.RvBtAdapter
import com.grandfatherpikhto.lessonble02.ui.models.BleViewModelFactory
import com.grandfatherpikhto.lessonble02.ui.models.MainActivityViewModel
import com.grandfatherpikhto.lessonble02.ui.models.ScannerViewModel
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var bleScanManager: BleScanManager

    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val scannerViewModel by viewModels<ScannerViewModel> {
        BleViewModelFactory(requireActivity().application)
    }


    private val rvBtAdapter = RvBtAdapter()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_scanner, menu)
                    menu.findItem(R.id.action_scan).let { actionScan ->
                // fillActionScan(actionScan, scannerViewModel.scanState)
                lifecycleScope.launch {
                    scannerViewModel.stateFLowScanState.collect { state ->
                        fillActionScan(actionScan, state)
                    }
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when(menuItem.itemId) {
                R.id.action_scan -> {
                    when(scannerViewModel.scanState) {
                        BleScanManager.State.Stopped -> {
                            bleScanManager.startScan(10000L)
                        }
                        BleScanManager.State.Scanning -> {
                            bleScanManager.stopScan()
                        }
                        BleScanManager.State.Error -> {

                        }
                    }
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

        _binding = FragmentScanBinding.inflate(inflater, container, false)

        bleScanManager =
            (requireContext().applicationContext as LessonBle02App).bleScanManager!!

        linkMenu(true, menuProvider)

        binding.apply {
            rvBleDevices.adapter = rvBtAdapter
            rvBleDevices.layoutManager = LinearLayoutManager(requireContext())
        }

        lifecycleScope.launch {
            scannerViewModel.sharedFlowScanResult.collect { scanResult ->
                rvBtAdapter.addScanResult(scanResult)
            }
        }

        rvBtAdapter.setOnClickBtDevice { scanResult, _ ->
            mainActivityViewModel.changeScanResult(scanResult)
            findNavController().navigate(R.id.action_ScanFragment_to_DeviceFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        bleScanManager.stopScan()
        linkMenu(false, menuProvider)
        _binding = null
    }

    private fun fillActionScan(actionScan: MenuItem, state: BleScanManager.State) {
        when(state) {
            BleScanManager.State.Stopped -> {
                actionScan.setIcon(R.drawable.ic_scan)
                actionScan.title = getString(R.string.scan_start)
            }
            BleScanManager.State.Scanning -> {
                actionScan.setIcon(R.drawable.ic_stop)
                actionScan.title = getString(R.string.scan_stop)
            }
            BleScanManager.State.Error -> {
                actionScan.setIcon(R.drawable.ic_error)
                actionScan.title = getString(R.string.scan_error,
                    scannerViewModel.error)
            }
        }
    }
}