package com.example.reactornfc

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import java.io.IOException

object NFCUtil {

    fun createNFCMessage(payload: String, intent: Intent?): Boolean {

        val pathPrefix = "example.com:nfc"
        val nfcRecord = NdefRecord(
            NdefRecord.TNF_EXTERNAL_TYPE,
            pathPrefix.toByteArray(),
            ByteArray(0),
            payload.toByteArray()
        )
        Log.d("testing", nfcRecord.toString())
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        Log.d("testing", nfcMessage.toString())
        intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            Log.d("testing", tag.toString())
            return writeMessageToTag(nfcMessage, tag)
        }
        return false
    }

    fun retrieveNFCMessage(intent: Intent?): String {
        intent?.let {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                val nDefMessages = getNDefMessages(intent)
                nDefMessages[0].records?.let { it ->
                    it.forEach { it ->
                        it?.payload.let {
                            it?.let {
                                return String(it)

                            }
                        }
                    }
                }

            } else {
                return "Touch NFC tag to read data"
            }
        }
        return "Touch NFC tag to read data"
    }


    private fun getNDefMessages(intent: Intent): Array<NdefMessage> {

        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }
        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

    fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
        val pendingIntent = PendingIntent.getActivity(
            activity, 0,
            Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter)

        val techLists =
            arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
    }


    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)
            Log.d("testing", nDefTag.toString())

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    //Message to large to write to NFC tag
                    Log.d("testing", "2 lrge")
                    return false
                }
                return if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    //Message is written to tag
                    true
                } else {
                    //NFC tag is read-only
                    Log.d("testing", "READONLY!")
                    false
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)
            Log.d("testing", nDefFormatableTag.toString())

            nDefFormatableTag?.let {
                return try {
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    //The data is written to the tag
                    true
                } catch (e: IOException) {
                    //Failed to format tag
                    false
                }
            }
            Log.d("testing", "NDEF NOT SUPPORTED!")
            //NDEF is not supported
            return false

        } catch (e: Exception) {
            //Write operation has failed
            Log.d("testing", e.toString())
        }
        return false
    }
}