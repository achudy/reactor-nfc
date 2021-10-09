package com.example.nfc2

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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