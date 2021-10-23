package com.example.reactornfc

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.nfc.NfcEvent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.*

class MainActivity : AppCompatActivity() {
    var mNfcAdapter: NfcAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        val beamData: Button = findViewById(R.id.buttonWrite)
        beamData.setOnClickListener(_onBeamClick)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent")
        setDisplayText("onNewIntent " + intent!!.action)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processNFCData(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        setDisplayText("onResume " + intent.action)
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processNFCData(intent)
        }
    }

    private fun processNFCData(inputIntent: Intent) {
        Log.i(TAG, "processNFCData")
        val rawMessages = inputIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMessages != null && rawMessages.size > 0) {
            val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
            for (i in rawMessages.indices) {
                messages[i] = rawMessages[i] as NdefMessage
            }
            Log.i(TAG, "message size = " + messages.size)
            val veiw: TextView = findViewById(R.id.result)
            if (veiw != null) {
                // only one message sent during the beam
                val msg = rawMessages[0] as NdefMessage
                // record 0 contains the MIME type, record 1 is the AAR, if present
                val base = String(msg.records[0].payload)
                val str = String.format(
                    Locale.getDefault(),
                    "Message entries=%d. Base message is %s",
                    rawMessages.size,
                    base
                )
                veiw.text = str
            }
        }
    }

    private fun setDisplayText(text: String) {
        val veiw: TextView = findViewById(R.id.result)
        if (veiw != null) {
            veiw.text = text
        }
    }

    private val _onBeamClick = View.OnClickListener {
        Log.i(TAG, "_onBeamClick onClick")
        turnOnNfcBeam()
    }

    /* **************************************************************
        This will create the NFC Adapter, if available,
        and setup the Callback listener when create message is needed.
     */
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
        Log.i(TAG, "createNdefMessage")
        createMessage()
    }

    private fun createMessage(): NdefMessage {
        val text = """
               Hello there from another device!
               
               Beam Time: ${System.currentTimeMillis()}
               """.trimIndent()
        return NdefMessage(
            arrayOf(
                NdefRecord.createMime(
                    "application/com.example.reactornfc.mimetype", text.toByteArray()
                )
                /**
                 * The Android Application Record (AAR) is commented out. When a device
                 * receives a push with an AAR in it, the application specified in the AAR
                 * is guaranteed to run. The AAR overrides the tag dispatch system.
                 * You can add it back in to guarantee that this
                 * activity starts when receiving a beamed message. For now, this code
                 * uses the tag dispatch system.
                 */
                //NdefRecord.createApplicationRecord("com.example.reactornfc")
            )
        )
    }

    companion object {
        private val TAG = "NFCDEMO:" + MainActivity::class.java.simpleName
    }
}


fun toast(context: Context, message: CharSequence) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

fun <T> Boolean.ifElse(primaryResult: T, secondaryResult: T) =
    if (this) primaryResult else secondaryResult