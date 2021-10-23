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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging



class MainActivity : AppCompatActivity() {
    private var mNfcAdapter: NfcAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val beamData: Button = findViewById(R.id.buttonActivate)
        beamData.setOnClickListener {
            turnOnNfcBeam()
            setBackgroundColor()
            setDisplayText("Make sure\nthat the reactor\nwas activated\non the other device!")
        }



        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Firebase log", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("Firebase log", msg)
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            processNFCData(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processNFCData(intent)
        }
    }

    private fun processNFCData(inputIntent: Intent) {
        val rawMessages = inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null && rawMessages.isNotEmpty()) {
            val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
            for (i in rawMessages.indices) {
                messages[i] = rawMessages[i] as NdefMessage
            }
            //Log.i(TAG, "message size = " + messages.size)
            // only one message sent during the beam
            val msg = rawMessages[0] as NdefMessage
            // record 0 contains the MIME type, record 1 is the AAR, if present
            val base = String(msg.records[0].payload)
            setBackgroundColor()
            setDisplayText(base)
            toast(this, base)
            "http://192.168.0.228:3000/test".httpPost().response{
                    request, response, result ->
                Log.d("HttpRequest",response.responseMessage)
                Log.d("HttpRequest",result.toString())
            }
        }
    }

    private fun setDisplayText(text: String) {
        val textView: TextView = findViewById(R.id.result)
        textView.text = text
    }

    private fun setBackgroundColor() {
        val layout: ConstraintLayout = findViewById(R.id.layout)
        layout.setBackgroundColor(resources.getColor(R.color.my_purple))
    }

    private fun turnOnNfcBeam() {
        // Check for available NFC Adapter
        if (mNfcAdapter == null) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        }
        if (mNfcAdapter == null || !mNfcAdapter!!.isEnabled) {
            mNfcAdapter = null
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            return
        }

        // Register callback
        mNfcAdapter!!.setNdefPushMessageCallback(_onNfcCreateCallback, this)
    }

    private val _onNfcCreateCallback = CreateNdefMessageCallback {
        createMessage()
    }

    private fun createMessage(): NdefMessage {
        val text = "Reactor activated!".trimIndent()
        val mimeType = "application/com.example.reactornfc.mimetype"
        val mimeRecord = NdefRecord.createMime(mimeType, text.toByteArray())

        return NdefMessage(arrayOf(mimeRecord))
    }
}


fun toast(context: Context, message: CharSequence) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

//fun <T> Boolean.ifElse(primaryResult: T, secondaryResult: T) =
//    if (this) primaryResult else secondaryResult