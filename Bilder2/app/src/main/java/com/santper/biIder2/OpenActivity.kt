package com.santper.biIder2

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date

class OpenActivity : AppCompatActivity() {
    internal var editSaveButton: Button? = null
    internal var content: EditText? = null

    @RequiresApi(api = 26)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_open_file)
        val OFALayout = findViewById<RelativeLayout>(R.id.OFALayout)
        val content = findViewById<EditText>(R.id.content)
        val editSaveButton = findViewById<Button>(R.id.editSaveButton)
        val OFACard = findViewById<CardView>(R.id.OFACard)
        val OFAOpenButton = findViewById<Button>(R.id.OFAOpenButton)
        val openButton = findViewById<ImageButton>(R.id.openButton)
        val OFABackButton = findViewById<Button>(R.id.OFABackButton)
        val sumButton = findViewById<ImageButton>(R.id.sumButton)
        val OFASumButton = findViewById<Button>(R.id.OFASumButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val context = applicationContext
        val extras = intent.extras
        content.setText(extras!!.getString("transcript"))
        OFACard.visibility = View.INVISIBLE

        editSaveButton.setOnClickListener { OFACard.visibility = View.VISIBLE }
        OFALayout.setOnClickListener(`OpenFileActivity$$Lambda$1`(OFACard, editSaveButton, OFALayout, content))
        OFAOpenButton.setOnClickListener { open(extras, content, context) }
        OFASumButton.setOnClickListener {
            val intent = Intent(this@OpenActivity, PreSummaryActivity::class.java)
            Log.e("CONT", content.text.toString())
            intent.putExtra("summary", content.text.toString())
            startActivity(intent)
        }
        OFABackButton.setOnClickListener(`OpenFileActivity$$Lambda$4`(this))
        openButton.setOnClickListener { open(extras, content, context) }
        backButton.setOnClickListener(`OpenFileActivity$$Lambda$6`(this))
    }

    internal /* synthetic */ fun `lambda$onCreate$4$OpenFileActivity`(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    internal fun open(extras: Bundle?, fileContent: EditText, context: Context) {
        val out: PrintStream?
        try {
            out = PrintStream(FileOutputStream(extras!!.getString("filepath")))
            out.print(fileContent.text)
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
        Log.e("File", fc.toString())
        val uri = FileProvider.getUriForFile(context, "com.santper.biIder2.FileProvider", file)
        Log.e("uri", uri.toString())
        val `in` = Intent("android.intent.action.VIEW")
        `in`.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())))
        `in`.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.grantUriPermission("com.google.android.apps.docs.editors.docs", uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(`in`)
    } // content:/storage/emulated/0/transcript06-05-2019-18-47-49.txt

    // content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Ftranscript26-04-2019-19-36-58.txt
    internal /* synthetic */ fun `lambda$onCreate$6$OpenFileActivity`(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    companion object {

        internal fun `lambda$onCreate$0$OpenFileActivity`(OFACard: CardView, editSaveButton: Button, OFALayout: RelativeLayout, fileContent: EditText, view: View) {
            OFACard.visibility = View.INVISIBLE
            editSaveButton.setBackgroundColor(Color.parseColor("#ee8000"))
            OFALayout.setBackgroundColor(Color.parseColor("#AAAAAA"))
            fileContent.setBackgroundColor(Color.parseColor("#FFF5F5F5"))
            editSaveButton.setTextColor(Color.parseColor("#FFF5F5F5"))
        }

        internal fun `lambda$onCreate$1$OpenFileActivity`(OFACard: CardView, editSaveButton: Button, OFALayout: RelativeLayout, fileContent: EditText, view: View) {
            if (OFACard.visibility == View.INVISIBLE) {
                OFACard.visibility = View.VISIBLE
                editSaveButton.setBackgroundColor(Color.parseColor("#FFFF8800"))
                OFALayout.setBackgroundColor(Color.parseColor("#FFF5F5F5"))
                fileContent.setBackgroundColor(Color.parseColor("#ffffff"))
                editSaveButton.setTextColor(Color.parseColor("#ffffff"))
            }
        }
    }
}
