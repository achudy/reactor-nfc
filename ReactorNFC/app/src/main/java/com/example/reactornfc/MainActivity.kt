package com.example.reactornfc

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonRead = findViewById<View>(R.id.buttonRead) as Button
        val buttonWrite = findViewById<View>(R.id.buttonWrite) as Button
        val resultTextView = findViewById<View>(R.id.result) as TextView
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
         * FCM
         */

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            resultTextView.text = msg
        })


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