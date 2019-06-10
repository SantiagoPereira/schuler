package com.santper.biIder2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

class SummarizedTextActivity : AppCompatActivity() {
    internal var editSaveSumButton: Button? = null
    internal var content: EditText? = null

    @RequiresApi(api = 26)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summarized_text)
        val sumContent = findViewById<EditText>(R.id.sumContent)
        val editSaveSumButton = findViewById<Button>(R.id.editSaveSumButton)
        val context = applicationContext
        val extras = intent.extras
        sumContent.setText(extras!!.getString("summary"))

        editSaveSumButton.setOnClickListener { open(extras, sumContent, context) }
    }

    internal fun open(extras: Bundle?, sumContent: EditText, context: Context) {
        val out: PrintStream?
        try {
            out = PrintStream(FileOutputStream(extras!!.getString("filepath")))
            out.print(sumContent.text)
            Log.e("path:", extras.getString("filepath"))
            out?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val file = File(extras!!.getString("filepath"))
        val stringBuilder = StringBuilder()
        stringBuilder.append("content://")
        stringBuilder.append(file)
        val fc = File(stringBuilder.toString())
        val uri = FileProvider.getUriForFile(context, "com.santper.biIder2.FileProvider", file)
        val `in` = Intent("android.intent.action.VIEW")
        `in`.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())))
        `in`.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.grantUriPermission("com.google.android.apps.docs.editors.docs", uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(`in`)
    }
}

