package com.santper.biIder2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Random

import android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS

class MainActivity : AppCompatActivity() {
    internal var AudioSavePathInDevice: String? = null
    internal var RandomAudioFileName = "ABCDEFGHIJKLMNOP"
    internal lateinit var mediaRecorder: MediaRecorder
    internal lateinit var random: Random
    internal lateinit var sum: Button
    internal lateinit var record: ImageButton
    internal lateinit var stop: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        record = findViewById(R.id.imgButton)
        stop = findViewById(R.id.imgButton2)
        sum = findViewById(R.id.button)
        stop.isEnabled = false
        random = Random()
        record.setOnClickListener(OnClickListener {
            if (this@MainActivity.checkPermission()) {
                val mainActivity = this@MainActivity
                val stringBuilder = Environment.getExternalStorageDirectory().absolutePath +
                        "/" +
                        this@MainActivity.CreateRandomAudioFileName(5) +
                        "record.3gp"
                mainActivity.AudioSavePathInDevice = stringBuilder
                this@MainActivity.MediaRecorderReady()
                try {
                    this@MainActivity.mediaRecorder.prepare()
                    this@MainActivity.mediaRecorder.start()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }

                this@MainActivity.record.isEnabled = false
                this@MainActivity.stop.isEnabled = true
                return@OnClickListener
            }
            this@MainActivity.requestPermission()
        })
        stop.setOnClickListener(C00422())
        val context = applicationContext


        sum.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/*"
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
            startActivityForResult(intent, 2)
        }
    }

    fun getSpeechInput(view: View) {
        val intent = Intent("android.speech.action.RECOGNIZE_SPEECH")
        intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form")
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
        intent.putExtra("android.speech.extra.LANGUAGE", Locale.getDefault())
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, 10)
        } else {
            Toast.makeText(this, "Jaja, pero cómo vas a tener esa terrible garcha de teléfono? Comprate uno nuevo, ratón", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val out: PrintStream
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                val path = data!!.data
                Log.e("path", path!!.path)
                val file = File(path.toString())
                Log.e("file", file.toString())
                val intent = Intent(this@MainActivity, PreSummaryActivity::class.java)
                var sb = "babg"
                try {
                    sb = readTextFromUri(path)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
                intent.putExtra("summary", sb)
                Log.e("extra", intent.getStringExtra("summary"))
                startActivity(intent)
            }


        }
        if (requestCode == 10) {
            if (resultCode == -1 && data != null) {
                val result = data.getStringArrayListExtra("android.speech.extra.RESULTS")
                val aintent = Intent(this, OpenActivity::class.java)
                aintent.putExtra("transcript", result[0])
                val date = Date()
                val sdf = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss")
                val filepath = Environment.getExternalStorageDirectory().absolutePath + "/transcript" + sdf.format(date) + ".txt"
                aintent.putExtra("filepath", filepath)
                try {
                    out = PrintStream(FileOutputStream(filepath))
                    out.print(result[0])
                    Log.e("La transcripción: ", result[0])
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                startActivity(aintent)
            }
        }
    }

    fun MediaRecorderReady() {
        this.mediaRecorder = MediaRecorder()
        this.mediaRecorder.setAudioSource(1)
        this.mediaRecorder.setOutputFormat(1)
        this.mediaRecorder.setAudioEncoder(3)
        this.mediaRecorder.setOutputFile(this.AudioSavePathInDevice)
    }

    fun CreateRandomAudioFileName(string: Int): String {
        val stringBuilder = StringBuilder(string)
        for (i in 0 until string) {
            stringBuilder.append(this.RandomAudioFileName[this.random.nextInt(this.RandomAudioFileName.length)])
        }
        return stringBuilder.toString()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.size > 0) {
                var RecordPermission = false
                val StoragePermission = grantResults[0] == 0
                if (grantResults[1] == 0) {
                    RecordPermission = true
                }
                if (StoragePermission && RecordPermission) {
                    Toast.makeText(this, "Permiso otorgado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, "android.permission.WRITE_EXTERNAL_STORAGE") == 0 && ContextCompat.checkSelfPermission(applicationContext, "android.permission.RECORD_AUDIO") == 0
    }

    internal inner class C00422 : OnClickListener {

        override fun onClick(view: View) {
            this@MainActivity.mediaRecorder.stop()
            this@MainActivity.stop.isEnabled = false
            this@MainActivity.record.isEnabled = true
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(
                inputStream))
        val stringBuilder = StringBuilder()
        var line: String
        while ((reader.readLine()) != null) {
            stringBuilder.append(reader.readLine())
        }
        Log.e("sb", stringBuilder.toString())
        return stringBuilder.toString()
    }
}
