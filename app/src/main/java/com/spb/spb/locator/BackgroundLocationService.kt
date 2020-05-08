package com.spb.spb.locator

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices


class BackgroundLocationService : Service(), ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {
    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null
    var mIntentService: Intent? = null
    var mPendingIntent: PendingIntent? = null
    var mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: BackgroundLocationService
            get() = this@BackgroundLocationService
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate()")
        mIntentService = Intent(this, FusedCoreService::class.java)
        mPendingIntent =
            PendingIntent.getService(this, 1, mIntentService!!, PendingIntent.FLAG_UPDATE_CURRENT)
        buildGoogleApiClient()
    }

    //@Nullable
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (mGoogleApiClient!!.isConnected) {
            Log.i(
                "$TAG onStartCommand",
                "GoogleApiClient Connected"
            )
            return START_STICKY
        }
        if (!mGoogleApiClient!!.isConnected || !mGoogleApiClient!!.isConnecting) {
            Log.i(
                "$TAG onStartCommand",
                "GoogleApiClient not Connected"
            )
            mGoogleApiClient!!.connect()
        }
        return START_STICKY
    }

    @Synchronized
    fun buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient")
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        createLocationRequest()
    }

    private fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest()")
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

     private fun startLocationUpdates() {
        Log.i(TAG, "Started Location Updates")

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest,
            mPendingIntent
        )
    }

    private fun stopLocationUpdates() {
        Log.i(TAG, "Stopped Location Updates")

        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPendingIntent)
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.i(TAG, "Connected to GoogleApiClient")
        startLocationUpdates()
    }

    /**
     * Callback that fires when the location changes.
     */
    override fun onLocationChanged(location: Location) {}
    override fun onConnectionSuspended(cause: Int) {
        Log.i(TAG, "Connection suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(
            TAG,
            "Connection failed: ConnectionResult.getErrorCode() = " + result.errorCode
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    companion object {
        protected const val TAG = "BackgroundLocationSer"
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 60000
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
}
