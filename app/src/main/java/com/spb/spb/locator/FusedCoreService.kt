package com.spb.spb.locator


import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderApi
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder



class FusedCoreService : IntentService {
    private var mContext: Context? = null
    private val TAG = this.javaClass.simpleName
    var ifilter: IntentFilter
    var batteryStatus: Intent? = null

    constructor() : super("Fused Location") {
        ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    }

    constructor(name: String?) : super(name) {
        ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mContext = applicationContext
        batteryStatus = mContext?.registerReceiver(null, ifilter)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, "onHandleIntent")
        val location =
            intent!!.getParcelableExtra<Location>(FusedLocationProviderApi.KEY_LOCATION_CHANGED)

        println("location received: $location")

        if (location != null) { /*
            Log.i(TAG, "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(this);
            noti.setContentTitle("Fused Location");
            noti.setContentText(location.getLatitude() + "," + location.getLongitude());
            noti.setSmallIcon(R.mipmap.ic_launcher);
            */

            //notificationManager.notify(1234, noti.build());
            val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level / scale.toFloat()

            // getApplicationContext().unregisterReceiver(null, ifilter);
            Post(location.latitude, location.longitude, batteryPct)
        }
    }

    fun Post(
        latitude: Double?,
        longitude: Double?,
        batteryLvl: Float
    ) {
        try {
            val url = URL("------")
            val postDataParams = JSONObject()
            postDataParams.put("latitude", latitude)
            postDataParams.put("longitude", longitude)
            postDataParams.put("battery", batteryLvl.toDouble())
            Log.e("params", postDataParams.toString())
            val conn =
                url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true

            //Send request
            val os = conn.outputStream
            val writer = BufferedWriter(
                OutputStreamWriter(os, "UTF-8")
            )
            writer.write(getPostDataString(postDataParams))
            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode
            Log.i(TAG, "responseCode:$responseCode")
        } catch (e: Exception) {
            println("exception on posting to server: ${e.message}")
        }
    }

    @Throws(Exception::class)
    fun getPostDataString(params: JSONObject): String {
        val result = StringBuilder()
        var first = true
        val itr = params.keys()
        while (itr.hasNext()) {
            val key = itr.next()
            val value = params[key]
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }
}
