package com.moesoft.myapplication

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moesoft.myapplication.Constants.Companion.CAR_LOCATION_LIST
import com.moesoft.myapplication.Constants.Companion.pattern
import com.moesoft.myapplication.model.RegLocation
import java.lang.reflect.Type
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SeeLocation : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences? = null
    private val myPreference = "myPref"
    private lateinit var textLocation: TextView
    private lateinit var buttonExit : Button
    private lateinit var buttonUpdate : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_location)

        textLocation = findViewById<TextView>(R.id.textViewLocation)
        buttonExit = findViewById<Button>(R.id.buttonExit)
        buttonUpdate = findViewById<Button>(R.id.buttonUpdate)

        setLink()

        buttonExit.setOnClickListener {
            finish()
        }
        buttonUpdate.setOnClickListener {
            setLink()
        }
    }




    private fun setLink(){
        sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)

        val gson = Gson()

        val oldLocationsJson = getSharedPreferences(myPreference, Context.MODE_PRIVATE).getString(CAR_LOCATION_LIST, "")

        if(oldLocationsJson!=""){
            val type: Type = object : TypeToken<List<RegLocation?>?>() {}.type
            var locations: List<RegLocation> = gson.fromJson(oldLocationsJson, type)

            var str = ""

            locations = locations.sortedByDescending { LocalDateTime.parse(it.date, DateTimeFormatter.ofPattern(pattern)) }
            locations.forEach {

                val linkedText = "${it.date} " +
                        java.lang.String.format("<a href=\"%s\">${it.deviceName}</a> ", "http://www.google.com/maps/place/${it.latitude},${it.longitude}")

                str+="\n$linkedText"
            }

            textLocation.text = str
            textLocation.movementMethod = LinkMovementMethod.getInstance()
        }

    }
}