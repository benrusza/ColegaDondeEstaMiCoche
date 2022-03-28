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

class SeeLocation : AppCompatActivity() {
    var sharedPreferences: SharedPreferences? = null
    private val myPreference = "myPref"
    lateinit var textLocation: TextView
    lateinit var buttonExit : Button
    lateinit var buttonUpdate : Button
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




    fun setLink(){
        sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)

        //var deviceName = sharedPreferences!!.getString("Device", "")

        //textLocation.setText(deviceName)

        var carLocation:String = sharedPreferences!!.getString("carLocation", "").toString()
        Log.d("car",carLocation)
        if(carLocation!=null){

            val linkedText = "Your car location : " +
                    java.lang.String.format("<a href=\"%s\">$carLocation</a> ", "http://www.google.com/maps/place/"+carLocation)
            textLocation.setText(Html.fromHtml(linkedText));
            textLocation.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }
}