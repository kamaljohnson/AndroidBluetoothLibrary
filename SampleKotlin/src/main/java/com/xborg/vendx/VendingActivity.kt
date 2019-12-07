package com.xborg.vendx

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xborg.vendx.MainActivity.Companion.items
import com.xborg.vendx.SupportClasses.Item
import com.xborg.vendx.SupportClasses.ItemSlipAdapter
import kotlinx.android.synthetic.main.activity_vending.*
import kotlinx.android.synthetic.main.fragment_home.rv_items_list
import java.util.*
import kotlin.collections.HashMap

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus

private var TAG = "VendingActivity"

class VendingActivity : AppCompatActivity(), BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback{

    val db = FirebaseFirestore.getInstance()
    val uid =  FirebaseAuth.getInstance().uid.toString()
    var bag_items : HashMap<String, Int> = HashMap()

    var vendID = ""

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mService: BluetoothService
    private var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_vending)

        setupBluetoothConnection()

        getBag()

        send_to_server.setOnClickListener {
            when {
                to_server_text.text.toString() == "" -> Toast.makeText(this, "the test text is empty", Toast.LENGTH_SHORT).show()
                to_server_text.text.toString() == "rqt" -> {
                    val vend = HashMap<String, Any>()
                    vend["UID"] = FirebaseAuth.getInstance().uid.toString()
                    vend["MID"] = "yDWzDc79Uu1IO2lEeVyG"    //TODO: use the actual MID
                    vend["Status"] = "Request Created"

                    db.collection("Vends")
                            .add(vend)
                            .addOnSuccessListener { vendRef ->
                                Log.e(TAG, "vendReference created with ID: ${vendRef.id}")
                                vendID = vendRef.id

                            }
                            .addOnFailureListener{
                                Log.e(TAG, "Failed to place vend")
                            }
                }
                else -> {
                    val vend = HashMap<String, Any>()
                    vend["Status"] = "Message Received"
                    vend["Msg"] = to_server_text.text.toString()

                    db.document("Vends/$vendID")
                            .update(vend)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener{
                                Log.e(TAG, "Failed to place vend")
                            }
                }
            }
        }
    }

    private fun getBag() {
        items.clear()

        db.document("Users/$uid")
                .get()
                .addOnSuccessListener { userSnap ->
                    if(userSnap.data?.get("Shelf")  == null) {
                        Log.e(TAG, "shelf is empty")
                        Toast.makeText(this, "Your Shelf is Empty", Toast.LENGTH_SHORT).show()
                    } else {
                        var shelfItems: Map<String, Number> = userSnap.data?.get("Bag") as Map<String, Number>
                        for (shelfItem in shelfItems){
                            Log.e(TAG, shelfItem.key)
                            Log.e(TAG, shelfItem.value.toString())

                            var itemId = shelfItem.key
                            var quantity = shelfItem.value

                            db.document("Inventory/${itemId}")
                                    .get()
                                    .addOnSuccessListener { document ->
                                        Log.e(TAG, "${document.id} => ${document.data}")

                                        val item = Item()

                                        item.item_id = document.id
                                        item.name = document.data?.get("Name").toString()
                                        item.quantity = document.data?.get("Quantity").toString()
                                        item.cost = "-1"    // shelf items are already bought, no need to show the cost
                                        item.item_limit = quantity.toString()
                                        item.image_src = document.data?.get("Image").toString()

                                        bag_items[item.item_id] = quantity.toInt()
                                        Log.e(TAG, item.item_id + " -> " + quantity)
                                        items.add(item)

                                        if(items.size == shelfItems.size) {
                                            addItemsToRV(items)
                                        }
                                    }
                                    .addOnFailureListener{exception ->
                                        Log.w(TAG, "Error getting documents.", exception)
                                    }
                        }
                    }
                }
                .addOnFailureListener {exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
    }

    /**
     * adds all the items to the recycler view
     * as item_card cards
     */
    private fun addItemsToRV(items: ArrayList<Item>){
        rv_items_list.layoutManager = LinearLayoutManager(this)
        rv_items_list.layoutManager = GridLayoutManager(this, 1)
        rv_items_list.adapter = ItemSlipAdapter(items, this)
    }

    private fun setupBluetoothConnection(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val config = BluetoothConfiguration()
        config.context = applicationContext
        config.bluetoothServiceClass = BluetoothClassicService::class.java
        config.bufferSize = 1024
        config.characterDelimiter = '\n'
        config.deviceName = "VendX"
        config.callListenersInMainThread = true
        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        BluetoothService.init(config)

        mService = BluetoothService.getDefaultInstance()
        mService.setOnScanCallback(this)
        mService.setOnEventCallback(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDataRead(buffer: ByteArray?, length: Int) {
        Log.e(TAG, "onDataRead")
    }

    override fun onStatusChange(status: BluetoothStatus?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDataWrite(buffer: ByteArray?) {
        Log.e(TAG, "onDataWrite")
    }

    override fun onToast(message: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeviceName(deviceName: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopScan() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartScan() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeviceDiscovered(device: BluetoothDevice?, rssi: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}