package com.example.bluetoothremote

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.UUID

class ControlActivity: AppCompatActivity() {

    companion object {
        // bluetooth UUID from Android documentation for serial bluetooth devices
        var uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var progress: ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected: Boolean = false
        lateinit var address: String
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set the UI
        setContentView(R.layout.control_layout)

        // get the MAC address from the selected device
        address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS).toString()

        ConnectToDevice(this).execute()


        var joystickLeftView: JoystickView = findViewById(R.id.joystickView_left)
        joystickLeftView.setOnMoveListener{ angle, strength ->
            // do whatever you want
            // Log.d("LEFT", ">>>>>>>>>>>>>>>>>>> LEFT: angle = "+angle+ " | strength = "+strength)
            sendCommand("l:"+angle+":"+strength+":\n")
        }

        var joystickRightView: JoystickView = findViewById(R.id.joystickView_right)
        joystickRightView.setOnMoveListener{ angle, strength ->
            // do whatever you want
            // Log.d("RIGHT", ">>>>>>>>>>>>>>>>>>> RIGHT: angle = "+angle+ " | strength = "+strength)
            sendCommand("r:"+angle+":"+strength+":\n")
        }

        /*
        // add listeners to buttons
        var moveForwardButton: Button = findViewById(R.id.move_forward)
        moveForwardButton.setOnClickListener{ sendCommand("forward\n") }

        var moveBackwardButton: Button = findViewById(R.id.move_backward)
        moveBackwardButton.setOnClickListener{ sendCommand("backward\n") }

        var moveLeftButton: Button = findViewById(R.id.move_left)
        moveLeftButton.setOnClickListener{ sendCommand("left\n") }

        var moveRightButton: Button = findViewById(R.id.move_right)
        moveRightButton.setOnClickListener{ sendCommand("right\n") }

         */

        var disconnectButton: Button = findViewById(R.id.disconnect)
        disconnectButton.setOnClickListener{ disconnect() }
    }

    private fun sendCommand(input: String) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.outputStream.write(input.toByteArray())
                bluetoothSocket!!.outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // go to the select device activity
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {

        private var connectSuccess: Boolean = true
        private val context: Context
        // constructor
        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog.show(context, "connecting...", "please wait")
        }

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                    // stops device discovery to save energy
                    bluetoothAdapter.cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                connectSuccess = false
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (!connectSuccess) {
                Log.d("data", "could not connect")
            } else {
                isConnected = true
            }
            progress.dismiss()
        }
    }
}