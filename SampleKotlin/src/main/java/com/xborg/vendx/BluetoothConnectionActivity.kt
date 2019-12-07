/*
 * MIT License
 *
 * Copyright (c) 2015 Douglas Nassif Roma Junior
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.xborg.vendx

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus

import java.util.Arrays

class BluetoothConnectionActivity : AppCompatActivity(), BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback {

    private var pgBar: ProgressBar? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null
    private var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connection)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        pgBar = findViewById<View>(R.id.pg_bar) as ProgressBar
        pgBar!!.visibility = View.GONE

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        mService = BluetoothService.getDefaultInstance()

        mService!!.setOnScanCallback(this)
        mService!!.setOnEventCallback(this)
        mService!!.startScan()
    }

    override fun onResume() {
        super.onResume()
        mService!!.setOnEventCallback(this)
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
        Log.d(TAG, "onDeviceDiscovered: " + device.name + " - " + device.address + " - " + Arrays.toString(device.uuids))
        if(device.address == DeviceAddress) {
            mService!!.connect(device)
        }
    }

    override fun onStartScan() {
        Log.d(TAG, "onStartScan")
        mScanning = true
        pgBar!!.visibility = View.VISIBLE
    }

    override fun onStopScan() {
        Log.d(TAG, "onStopScan")
        mScanning = false
        pgBar!!.visibility = View.GONE
    }

    override fun onDataRead(buffer: ByteArray, length: Int) {
        Log.d(TAG, "onDataRead")
    }

    override fun onStatusChange(status: BluetoothStatus) {
        Log.d(TAG, "onStatusChange: $status")
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()

        if (status == BluetoothStatus.CONNECTED) {
            val intent = Intent(this, VendingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onDeviceName(deviceName: String) {
        Log.d(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String) {
        Log.d(TAG, "onToast")
    }

    override fun onDataWrite(buffer: ByteArray) {
        Log.d(TAG, "onDataWrite")
    }

    companion object {
        val TAG = "BluetoothExampleKotlin"
        val DeviceAddress = "3C:71:BF:79:86:22"
    }
}
