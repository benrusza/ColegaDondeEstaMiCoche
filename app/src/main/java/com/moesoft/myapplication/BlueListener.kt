package com.moesoft.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moesoft.myapplication.Constants.Companion.CAR_LOCATION_LIST
import com.moesoft.myapplication.Constants.Companion.pattern
import com.moesoft.myapplication.model.RegLocation
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class BlueListener : Service() {

    var sharedPreferences: SharedPreferences? = null
    private val myPreference = "myPref"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        if(IS_RUNNING){
            return
        }

        val CHANNEL_ID = "moesoft.service"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Background service",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CDEMC is Running in background")
            .setContentText("To hide this, hold and select deactivate notifications")
            .setSmallIcon(R.drawable.baseline_bluetooth_drive_24)
            .build()

        startForeground(1, notification)

        startBroadcastListener()
    }


    override fun onBind(p0: Intent?): IBinder? {
        //TODO("Not yet implemented")
        return null
    }

    //No se usa?
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {

            when (intent.action) {

                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopService()
                }

                ACTION_OPEN_APP -> openAppHomePage()
            }
        }
        return START_STICKY

        //return super.onStartCommand(intent, flags, startId)
    }

    private fun openAppHomePage() {

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }


    private fun startBroadcastListener(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        application.registerReceiver(bluetoothDeviceListener, filter)
        println("blue service start")

        IS_RUNNING = true
        newNotification(title="Info", message = "blue service start")
    }


    private fun stopService() {
        // Stop foreground service and remove the notification.
        stopForeground(true)
        // Stop the foreground service.
        stopSelf()

        IS_RUNNING = false
    }

    override fun onDestroy() {

        IS_RUNNING = false

        newNotification(R.drawable.baseline_bluetooth_disabled_24,"App detenida: ","Colega donde esta mi coche se ha detenido")

        super.onDestroy()
    }

    companion object {


        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

        const val ACTION_OPEN_APP = "ACTION_OPEN_APP"

        var IS_RUNNING: Boolean = false

        var deviceSaved = ""
    }


    private var bluetoothDeviceListener: BroadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {

            val device: BluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
            sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)

            val deviceName = sharedPreferences!!.getString("Device", "")
            println("service blue detection $deviceName")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                if (device.alias.toString() == deviceName){
                    deviceSaved = deviceName
                    getLastLocation()
                }
            }

        }
    }



    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                fusedLocationClient.lastLocation.addOnCompleteListener() { task ->
                    var mLastLocation: Location? = task.result

                    if (mLastLocation != null) {
                        saveLoc(mLastLocation.latitude,mLastLocation.longitude)
                    }
                    requestNewLocationData()
                }
            } else {
                newNotification(R.drawable.baseline_bluetooth_disabled_24,"Something go wrong: ","Location is OFF, please set location ON and click this box to try save car location again")

            }
        }else{
            newNotification(R.drawable.baseline_bluetooth_disabled_24,"Something go wrong: ","No permissions")
        }
    }



    private fun newNotification(icon: Int = R.drawable.baseline_bluetooth_drive_24 ,title:String,message: String) {

        val CHANNEL_ID = "moesoft.info" // Use same Channel ID
        var channel = NotificationChannel(
            CHANNEL_ID,
            "Info notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        val intent = Intent(this, BlueListener::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, channel.id) // Create notification with channel Id
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setContentIntent(pendingIntent).setAutoCancel(true)

        val mNotificationManager =
           getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        with(mNotificationManager) {
            notify(123, builder.build())
        }

    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Looper.myLooper()?.let {
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                it
            )
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation

            saveLoc(mLastLocation.latitude,mLastLocation.longitude)

        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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


    private fun saveLoc(latitude : Double,longitude : Double){

        val gson = Gson()

        var oldLocations: List<RegLocation> = emptyList()
        val oldLocationsJson = getSharedPreferences(myPreference, Context.MODE_PRIVATE).getString(CAR_LOCATION_LIST, "")

        if(oldLocationsJson!=""){
            val type: Type = object : TypeToken<List<RegLocation?>?>() {}.type
            oldLocations = gson.fromJson(oldLocationsJson, type)
        }


        val dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
        val regLoc = RegLocation(latitude,longitude,dateTime,deviceSaved)

        val locations = ArrayList<RegLocation>()
        locations.add(regLoc)
        if (oldLocations.size>10){
            locations.addAll(oldLocations.subList(0,9))
        }else{
            locations.addAll(oldLocations)
        }


        val json = gson.toJson(locations)

        val editor: SharedPreferences.Editor = getSharedPreferences(myPreference, Context.MODE_PRIVATE).edit()

        editor.putString(CAR_LOCATION_LIST, json)
        editor.apply()

        newNotification(R.drawable.baseline_location_on_24,"Info:","Car location saved!")
    }

}