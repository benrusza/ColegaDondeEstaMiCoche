package com.moesoft.myapplication

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.moesoft.myapplication.BlueListener.Companion.IS_RUNNING
import java.time.OffsetTime

class MainActivity : AppCompatActivity() {

    private val PERMISSION_ID = 42
    lateinit var textClock:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonGPS = findViewById<Button>(R.id.buttonGPS)
        val buttonLocation = findViewById<Button>(R.id.buttonGPSLocation)
        textClock = findViewById<TextView>(R.id.textViewClock)
        val offset = OffsetTime.now()
        textClock.text = "${offset.hour} : ${offset.minute}"
        buttonGPS.setOnClickListener{

            val intent = Intent(this@MainActivity, ListDevices::class.java)
            startActivity(intent)
            //finish()
        }
        buttonLocation.setOnClickListener{

            val intent = Intent(this@MainActivity, SeeLocation::class.java)
            startActivity(intent)
            //finish()
        }


        if (!checkPermissions()) {
            requestPermissions()
        }

        if(!isLocationEnabled()){
            showSettingsAlert()
        }

        if(!isMyServiceRunning(BlueListener::class.java)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, BlueListener::class.java))
            } else {
                this.startService(Intent(this, BlueListener::class.java))
            }
        }
    }

    fun showSettingsAlert() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)

        // Setting Dialog Title
        alertDialog.setTitle("Configuración GPS")

        // Setting Dialog Message
        alertDialog.setMessage("GPS no esta habilitado.¿Quieres ir al menú de configuración?")

        // On pressing Settings button
        alertDialog.setPositiveButton("Configuración",
            DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                this.startActivity(intent)
            })

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancelar",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        // Showing Alert Message
        alertDialog.show()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        //https://stackoverflow.com/questions/45817813/alternate-of-activitymanager-getrunningservicesint-after-oreo
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

}