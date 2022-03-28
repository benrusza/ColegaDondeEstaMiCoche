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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build

class BlueListener : Service() {

    var sharedPreferences: SharedPreferences? = null
    private val myPreference = "myPref"
    lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "moesoft"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Info notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        }

        startBroadcastListener()
    }


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        println(TAG+ " ON START COMMAND")

        if (intent != null) {

            when (intent.action) {

                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopService()
                }

                ACTION_OPEN_APP -> openAppHomePage("intent?.getStringExtra(KEY_DATA)")
            }
        }
        return START_STICKY;

        return super.onStartCommand(intent, flags, startId)
    }

    private fun openAppHomePage(value: String) {

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(KEY_DATA, value)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }


    private fun startBroadcastListener(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        application.registerReceiver(bluetoothDeviceListener, filter)
        println("blue service start")
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

        Toast.makeText(applicationContext, "CDEMC FINALIZADA", Toast.LENGTH_LONG).show()
        super.onDestroy()
    }

    companion object {

        const val TAG = "FOREGROUND_SERVICE"

        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

        const val ACTION_OPEN_APP = "ACTION_OPEN_APP"
        const val KEY_DATA = "KEY_DATA"

        var IS_RUNNING: Boolean = false
    }


    private var bluetoothDeviceListener: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            var device: BluetoothDevice

            device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!;
            sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)

            var deviceName = sharedPreferences!!.getString("Device", "")
            println("service blue detection $deviceName")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                if (device.alias.toString().equals(deviceName)){

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
                    var location: Location? = task.result

                    if (location != null) {
                        var carLocation = location.latitude.toString()+","+location.longitude.toString()
                        sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                        editor.putString("carLocation", carLocation)
                        editor.apply()
                    }
                    requestNewLocationData()
                }
            } else {
                newNotification("Something go wrong: ","Location is OFF, please set location ON and click this box to try save car location again")

            }
        }
    }



    private fun newNotification(title:String,message: String) {

        val channelId = "moesoft" // Use same Channel ID
        val intent = Intent(this, BlueListener::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        val builder = NotificationCompat.Builder(this, channelId) // Create notification with channel Id
            .setSmallIcon(R.drawable.ic_launcher_foreground)
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
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation

            var carLocation = mLastLocation.latitude.toString()+","+mLastLocation.longitude.toString()
            sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putString("carLocation", carLocation)
            editor.apply()
            newNotification("Info:","Car location saved!")

        }
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

}