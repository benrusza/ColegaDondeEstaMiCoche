package com.moesoft.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ListDevices : AppCompatActivity() {


    private var sharedPreferences: SharedPreferences? = null
    private val myPreference = "myPref"

    override fun onRequestPermissionsResult(

        requestCode: Int,

        permissions: Array<out String>,

        grantResults: IntArray

    ) {

        if (requestCode == 101) {

            if (grantResults.isNotEmpty() &&

                grantResults[0] == PackageManager.PERMISSION_GRANTED

            ) {
                // Permission is granted
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                setView()

            } else {
                // Permission is denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_devices)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(

                    this,

                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),

                    101 // Request code for the permission

                )
                return
            }
            setView()
        } else {
            setView()
        }


    }

    @SuppressLint("MissingPermission")
    fun setView() {

        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val listDevices: MutableList<String> = ArrayList()

        val pairedDevices: Set<BluetoothDevice> = bluetoothManager.adapter.bondedDevices
        if (pairedDevices.isNotEmpty()) {

            for (device in pairedDevices) {
                val deviceName = device.name
                listDevices.add(deviceName)
            }
        }


        val listView: RecyclerView = findViewById(R.id.listView)


        sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)
        val deviceName = sharedPreferences!!.getString("Device", "")


        val adapter = MyListAdapter(
            listDevices,
            deviceName?:""
        )
        listView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        listView.adapter = adapter

        val buttonSelect = findViewById<Button>(R.id.buttonSelect)
        buttonSelect.setOnClickListener {
            var device = ""
            for (item in listView) {
                var temp = item as CheckBox
                if (temp.isChecked) {
                    device = temp.text as String
                    break
                }


            }

            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putString("Device", device)
            editor.apply()

            val intent = Intent(this@ListDevices, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        val buttonExit = findViewById<Button>(R.id.buttonExit2)
        buttonExit.setOnClickListener {
            finish()

        }

    }
}

class MyListAdapter(private val items: List<String>,private val selDevice : String, private val onClick: (String) -> Unit = {}) :
    RecyclerView.Adapter<MyListAdapter.MyViewHolder>() {
    var selected = -1
    init {

        if(selDevice.isNotEmpty()){
            items.forEachIndexed { index, item ->
                if(item==selDevice) {
                    selected = index
                    return@forEachIndexed
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_item, parent, false)
        return MyViewHolder(view)

    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.checkBox?.text = items[position]
        holder.checkBox?.isChecked = selected==position
        holder.itemView.setOnClickListener {
            var ol = selected
            selected = position
            notifyItemChanged(ol)
        }

    }

    override fun getItemCount() = items.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkBox: CheckBox? = null
        init {
            checkBox = itemView.findViewById(R.id.label)

        }
    }

}