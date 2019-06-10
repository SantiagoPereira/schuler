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
import android.widget.RelativeLayout

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date

import java.lang.Float.parseFloat

class PreSummaryActivity : AppCompatActivity() {
    internal var preSumButton: Button? = null
    internal var content: EditText? = null

    @RequiresApi(api = 26)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_summary)
        val preSumLayout = findViewById<RelativeLayout>(R.id.preSumLayout)
        val preSumContent = findViewById<EditText>(R.id.preSumContent)
        val preSumButton = findViewById<Button>(R.id.preSumButton)
        val preSumCard = findViewById<CardView>(R.id.preSumCard)
        val editText = findViewById<EditText>(R.id.editText)
        val modalPreSumButton = findViewById<Button>(R.id.modalPreSumButton)
        val context = applicationContext
        val extras = intent.extras
        Log.e("extra2", extras!!.getString("summary"))
        preSumContent?.setText(extras!!.getString("summary"))
        preSumCard.visibility = View.INVISIBLE

        preSumButton.setOnClickListener {
            preSumCard.visibility = View.VISIBLE
            preSumButton.setBackgroundColor(Color.parseColor("#FFFF8800"))
            preSumLayout.setBackgroundColor(Color.parseColor("#FFF5F5F5"))
            preSumContent.setBackgroundColor(Color.parseColor("#ffffff"))
            preSumButton.setTextColor(Color.parseColor("#ffffff"))
        }

        preSumLayout.setOnClickListener {
            preSumCard.visibility = View.INVISIBLE
            preSumButton.setBackgroundColor(Color.parseColor("#ee8000"))
            preSumLayout.setBackgroundColor(Color.parseColor("#AAAAAA"))
            preSumContent.setBackgroundColor(Color.parseColor("#FFF5F5F5"))
            preSumButton.setTextColor(Color.parseColor("#FFF5F5F5"))
        }

        modalPreSumButton.setOnClickListener {
            Log.e("num antes", editText.text.toString())
            Log.e("num procesado", Integer.parseInt(editText.text.toString()).toString())
            Log.e("num procesado 2", (parseFloat(editText.text.toString()) / 100).toString())
            sum(preSumContent.text.toString(), parseFloat(editText.text.toString()) / 100, context)
        }
    }

    internal fun sum(textToSum: String, num: Float, context: Context) {
        val dfc = DocumentFrequencyCounter()

        val summ = Summarizer(dfc)
        print("Resumiendo: ")
        val sum = summ.summarize(textToSum, num)
        val sumIntent = Intent(context, SummarizedTextActivity::class.java)
        val date = Date()
        val sdf = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss")
        val filepath = Environment.getExternalStorageDirectory().absolutePath + "/summary" + sdf.format(date) + ".txt"
        sumIntent.putExtra("summary", sum)
        sumIntent.putExtra("filepath", filepath)
        startActivity(sumIntent)
    }
}

