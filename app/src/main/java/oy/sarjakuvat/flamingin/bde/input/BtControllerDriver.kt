package oy.sarjakuvat.flamingin.bde.input

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat

@SuppressLint("MissingPermission")
class BtControllerDriver(activityContext: Activity) {
    private val context: Activity = activityContext
    private val discoveryReceiver: BroadcastReceiver
    private val bondingReceiver: BroadcastReceiver

    private var discoveryFilter: IntentFilter? = null
    private var pairFilter: IntentFilter? = null
    private var controllerDevice: BluetoothDevice? = null
    private var controllerSocker: BluetoothSocket? = null
    private var currentStatus = ""
    private var lastError = ""

    private lateinit var bluetoothAdapter: BluetoothAdapter

    fun getStatus(): String {
        val stat = currentStatus
        currentStatus = ""
        return stat
    }

    fun getError(): String {
        val err = lastError
        lastError = ""
        return err
    }

    fun initiateDiscoverAndConnect() {
        val systemService = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = systemService.adapter
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            context.startActivityForResult(enableBtIntent, hidEnableBt)
            setStatus("BT Enable")
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            context.requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),2)
            return
        }

        discoveryFilter = IntentFilter(Intent.CATEGORY_DEFAULT)
        discoveryFilter!!.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        discoveryFilter!!.addAction(BluetoothDevice.ACTION_FOUND)
        discoveryFilter!!.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        discoveryFilter!!.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(discoveryReceiver, discoveryFilter)
        if (bluetoothAdapter.startDiscovery()) {
            setStatus("Discovery")
        } else {
            logError("Failed to initiate discovery")
        }
    }

    private fun discoveryCallback(context: Context, intent: Intent) {
        val action: String = intent.action!!
        if (action == BluetoothDevice.ACTION_FOUND) {
            setStatus("Found a device")
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
            if(device.name == null) {
                return
            }

            val devicename: String = device.name
            setStatus("Found $devicename at ${device.address}")
            var isSupportedDevice = false
            try {
                isSupportedDevice = devicename.contains(deviceTypeSensor)
            } catch (ex: Exception) {
            }

            if (isSupportedDevice) {
                bluetoothAdapter.cancelDiscovery()
                context.unregisterReceiver(discoveryReceiver)
                determineConnectOrPair(device)
            }
        } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
            context.unregisterReceiver(discoveryReceiver)
        } else if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            val devices: Array<BluetoothDevice> = bluetoothAdapter.bondedDevices.toTypedArray()
            for (i in devices.indices) {
                setStatus("Paired with ${devices[i].name}")
            }
        } else {
            logError("Disregarding discovery callback $action")
        }
    }

    private fun determineConnectOrPair(device: BluetoothDevice) {
        controllerDevice = device
        @Suppress("ControlFlowWithEmptyBody")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) { }

        if (determinePairingStatus(controllerDevice!!.bondState)) {
            connectToController()
        } else {
            initiatePairing()
        }
    }

    private fun initiatePairing() {
        pairFilter = IntentFilter(Intent.CATEGORY_DEFAULT)
        pairFilter!!.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        pairFilter!!.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        context.registerReceiver(bondingReceiver, pairFilter)
        @Suppress("ControlFlowWithEmptyBody")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) { }

        if (controllerDevice!!.createBond()) {
            setStatus("Pairing initiated")
        } else {
            logError("Failed to initiate pairing")
        }
    }

    private fun handlePairIntent(context: Context, intent: Intent) {
        val action: String = intent.action.toString()
        setStatus("Pairing callback $action")
        if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            if (determinePairingStatus(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1))) {
                context.unregisterReceiver(bondingReceiver)
                connectToController()
            }
        } else if (BluetoothDevice.ACTION_PAIRING_REQUEST == action) {
            val addressString: String =  controllerDevice!!.address //bluetoothAdapter.address //
            val addressBytes = addressString.split(":").toTypedArray()
            val pin = ByteArray(6)
            for (i in addressBytes.indices.reversed()) {
                val pinByte = Integer.valueOf(addressBytes[i], 16).toByte()
                pin[5 - i] = pinByte
            }

            @Suppress("ControlFlowWithEmptyBody")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) { }

            if (controllerDevice!!.setPin(pin)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_PRIVILEGED
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    this.context.requestPermissions(arrayOf(
                        Manifest.permission.BLUETOOTH_PRIVILEGED
                    ),3)
                    logError("Missing the BLUETOOTH_PRIVILEGED privilege")
                }

                val pairingConfirmed = true
                setStatus("HID pin accepted, $pairingConfirmed")
            } else {
                logError("HID pin rejected")
                return
            }
        } else {
            logError("Unknown intent received when pairing: $action")
        }
    }

    private fun connectToController() {
        @Suppress("ControlFlowWithEmptyBody")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) { }

        val uuids: Array<ParcelUuid> = controllerDevice!!.uuids
        if (uuids.isNotEmpty()) {
            try {
                controllerSocker = controllerDevice!!.createInsecureRfcommSocketToServiceRecord(uuids[0].uuid)
                setStatus("Connecting")
                controllerSocker!!.connect()
            } catch (ex: Exception) {
                logError(ex.toString())
            }
        }
    }

    private fun determinePairingStatus(status: Int): Boolean {
        var isPaired = false
        when (status) {
            BluetoothDevice.BOND_NONE -> setStatus("Pairing status is ${BluetoothDevice.BOND_NONE::class.simpleName}")
            BluetoothDevice.BOND_BONDING -> setStatus("Pairing status is ${BluetoothDevice.BOND_BONDING::class.simpleName}")
            BluetoothDevice.BOND_BONDED -> {
                setStatus("Paired")
                isPaired = true
            }
            else -> logError("Disregarded pairing status $status")
        }

        return isPaired
    }

    private fun setStatus(stat: String) {
        currentStatus += stat.trimIndent()
        Log.d(BtControllerDriver::class.simpleName, stat)
    }

    private fun logError(err: String) {
        setStatus(errorLiteral)
        lastError += err.trimIndent()
        Log.e(BtControllerDriver::class.simpleName, err)
    }

    init {
        discoveryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                discoveryCallback(context, intent)
            }
        }

        bondingReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handlePairIntent(context, intent)
            }
        }
    }

    companion object {
        private const val hidEnableBt = 1
        private const val deviceTypeSensor = "intend"
        private const val errorLiteral = "Error"
    }
}
