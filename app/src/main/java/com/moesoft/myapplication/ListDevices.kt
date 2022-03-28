package com.moesoft.myapplication

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import androidx.core.view.iterator
import androidx.core.view.size
import android.app.Activity

import android.content.Intent
import android.view.View

import android.widget.AdapterView

import android.widget.AdapterView.OnItemClickListener




class ListDevices : AppCompatActivity() {


    var sharedPreferences: SharedPreferences? = null
    private val myPreference = "myPref"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_devices)

        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        var listDevices:MutableList<String> = ArrayList()

        val pairedDevices: Set<BluetoothDevice> = bluetoothManager.getAdapter().bondedDevices
        if (pairedDevices.size > 0) {

            for (device in pairedDevices) {
                val deviceName = device.name
                listDevices.add(deviceName)
            }
        }

        val adapter = ArrayAdapter(this,
            R.layout.listview_item, listDevices)

        val listView:ListView = findViewById(R.id.listView)

        listView.setAdapter(adapter)



        sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)

        val buttonSelect = findViewById<Button>(R.id.buttonSelect)
        buttonSelect.setOnClickListener{
            var device = ""
            for (item in listView){
                var temp = item as CheckBox
                if(temp.isChecked)
                    device= temp.text as String

        }

        val buttonExit = findViewById<Button>(R.id.buttonExit2)
        buttonExit.setOnClickListener{
            finish()

        }

            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putString("Device", device)
            editor.apply()

            val intent = Intent(this@ListDevices, MainActivity::class.java)
            startActivity(intent)
            finish()
        }



    }
}