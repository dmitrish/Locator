package com.spb.spb.locator

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.spb.spb.locator.Utils
import com.spb.spb.locator.Utils.isMyServiceRunning


class MainActivity : AppCompatActivity() {
    private var mStartUpdatesButton: Button? = null
    private var mStopUpdatesButton: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        mStartUpdatesButton =
            findViewById<View>(R.id.start_updates_button) as Button
        mStopUpdatesButton =
            findViewById<View>(R.id.stop_updates_button) as Button
        updateUI()
        try {
            // Initiate DevicePolicyManager.
            val policyMgr =
                getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

            // Set DeviceAdminDemo Receiver for active the component with different option
            val componentName = ComponentName(this, DeviceAdminComponent::class.java)
            if (!policyMgr.isAdminActive(componentName)) {
                // try to become active
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Click on Activate button to protect your application from uninstalling!"
                )
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mStartUpdatesButton!!.setOnClickListener {
            val intent =
                Intent(this@MainActivity, BackgroundLocationService::class.java)
            startService(intent)
            updateUI()
        }
        mStopUpdatesButton!!.setOnClickListener {
            val intent =
                Intent(this@MainActivity, BackgroundLocationService::class.java)
            stopService(intent)
            updateUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun updateUI() {
        if (isMyServiceRunning(
                this,
                BackgroundLocationService::class.java
            )
        ) {
            mStartUpdatesButton!!.isEnabled = false
            //  mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton!!.isEnabled = true
            mStopUpdatesButton!!.isEnabled = false
        }
    }
}
