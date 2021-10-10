package com.example.nfc2

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.pushnotifications.PushNotifications

class MainActivity : AppCompatActivity() {

    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonRead = findViewById<View>(R.id.buttonRead) as Button
        val buttonWrite = findViewById<View>(R.id.buttonWrite) as Button
        val editTextSendMessage = findViewById<View>(R.id.editTextSendMessage) as EditText

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        toast(this, NFCUtil.retrieveNFCMessage(this.intent))


        buttonWrite.setOnClickListener {
            val messageWrittenSuccessfully =
                NFCUtil.createNFCMessage(editTextSendMessage.text.toString(), intent)
            toast(
                this,
                messageWrittenSuccessfully.ifElse(
                    "Successful Written to Tag",
                    "Something When wrong Try Again"
                )
            )
        }

        buttonRead.setOnClickListener {
            toast(this, NFCUtil.retrieveNFCMessage(this.intent))
        }


        /***
         * PUSHER
         */
        PushNotifications.start(this, "5741b69e-86ff-4ef7-aa96-0c5d2968a3eb");
        PushNotifications.addDeviceInterest("hello")
        PushNotifications.addDeviceInterest("debug-notifications")

        val options = PusherOptions()
        options.setCluster("eu");

        val pusher = Pusher("2ac22b7f84ea6383f1e3", options)

        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.i(
                    "Pusher",
                    "State changed from ${change.previousState} to ${change.currentState}"
                )
            }

            override fun onError(
                message: String,
                code: String,
                e: Exception
            ) {
                Log.i(
                    "Pusher",
                    "There was a problem connecting! code ($code), message ($message), exception($e)"
                )
            }
        }, ConnectionState.ALL)

        val channel = pusher.subscribe("my-channel")
        channel.bind("my-event") { event ->
            Log.i("Pusher", "Received event with data: $event")
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val editTextSendMessage = findViewById<View>(R.id.editTextSendMessage) as EditText
        val messageWrittenSuccessfully =
            NFCUtil.createNFCMessage(editTextSendMessage.text.toString(), intent)
        toast(
            this,
            messageWrittenSuccessfully.ifElse(
                "Successful Written to Tag",
                "Something When wrong Try Again"
            )
        )
    }


    //NFC handling with resuming and pausing
    override fun onResume() {
        super.onResume()
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

}


fun toast(context: Context, message: CharSequence) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

fun <T> Boolean.ifElse(primaryResult: T, secondaryResult: T) =
    if (this) primaryResult else secondaryResult