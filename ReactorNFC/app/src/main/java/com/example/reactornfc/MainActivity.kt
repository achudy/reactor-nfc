package com.example.reactornfc

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

/**
 * The main activity with all the logic.
 */
class MainActivity : AppCompatActivity() {

    /**
     * NFC needs to be initiated
     */
    private var mNfcAdapter: NfcAdapter? = null

    /**
     * The main function of an Android activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * Logic for turning NFC on and signifying that tu the user.
         */
        buttonActivate.setOnClickListener {
            turnOnNfcBeam()
            setBackgroundColor()
            setDisplayText("Make sure\nthat the reactor\nwas activated\non the other device!")
        }

        /**
         * For working with notifications - the app needs a Firebase connection.
         */
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            /**
             * Check the Firebase connection
             */
            if (!task.isSuccessful) {
                Log.w("Firebase", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            /**
             * Obtain the token
             */
            val token = task.result
            Log.d("Firebase", token.toString())

            /**
             * Send the token to the web server.
             */
            val bodyJson = """{"token" : "$token"}"""
            "http://192.168.0.228:3000/token".httpPost().jsonBody(bodyJson)
                .response { request, response, result ->
                    Log.d("HttpRequest", request.toString())
                    Log.d("HttpRequest", response.toString())
                }
        })
    }

    /**
     * A new intent needs to be processed.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            processNFCData(intent)
        }
    }

    /**
     * A new intent needs to be processed.
     */
    override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processNFCData(intent)
        }
    }

    /**
     * Take the input and get the text message from the NFC message.
     */
    private fun processNFCData(inputIntent: Intent) {
        val rawMessages = inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null && rawMessages.isNotEmpty()) {
            val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
            for (i in rawMessages.indices) {
                messages[i] = rawMessages[i] as NdefMessage
            }
            val msg = rawMessages[0] as NdefMessage
            val msgText = String(msg.records[0].payload)
            setBackgroundColor()
            setDisplayText(msgText)
            toast(this, msgText)
            httpPost("test")
        }
    }

    /**
     * A function for setting a string in a textview.
     */
    private fun setDisplayText(text: String) {
        val textView: TextView = findViewById(R.id.result)
        textView.text = text
    }

    /**
     * Background change.
     */
    private fun setBackgroundColor() {
        layout.setBackgroundColor(resources.getColor(R.color.my_purple))
    }

    /**
     * Turning on the adapter.
     */
    private fun turnOnNfcBeam() {
        if (mNfcAdapter == null) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        }
        if (mNfcAdapter == null || !mNfcAdapter!!.isEnabled) {
            mNfcAdapter = null
            toast(this, "NFC is not available")
            return
        }
        mNfcAdapter!!.setNdefPushMessageCallback(_onNfcCreateCallback, this)
    }

    /**
     * Creating a message in a callback.
     */
    private val _onNfcCreateCallback = CreateNdefMessageCallback {
        createMessage()
    }

    /**
     * Create an NdefMessage.
     */
    private fun createMessage(): NdefMessage {
        val text = "Reactor activated!".trimIndent()
        val mimeType = "application/com.example.reactornfc.mimetype"
        val mimeRecord = NdefRecord.createMime(mimeType, text.toByteArray())

        return NdefMessage(arrayOf(mimeRecord))
    }
}

/**
 * Simple toast making function.
 */
fun toast(context: Context, message: CharSequence) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

/**
 * Http POST - a reoccuring message to the www server.
 */
fun httpPost(url: String) {
    "http://192.168.0.228:3000/$url".httpPost().response { request, response, result ->
        Log.d("HttpRequest", response.responseMessage)
        Log.d("HttpRequest", result.toString())
    }
}