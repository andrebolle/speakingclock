package com.purelazy.speakingclock

import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

// Extending MainActivity TextToSpeech.OnInitListener class
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private lateinit var tvTime: TextView

    // 'constructor Handler()' is deprecated. Deprecated in Java
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvTime = findViewById(R.id.tv_time)
        //tvTime.text = "Yo!"
        tts = TextToSpeech(this, this)

        // Start a background thread to talk and update the UI time text-box.
        Thread {
            while (true) {
                // code to be executed in the loop
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val seconds = calendar.get(Calendar.SECOND)

                // Convert hours to the 12-hour system
                val hour12 = when (hour) {
                    0 -> 12
                    in 1..12 -> hour
                    else -> hour - 12
                }
                // If its a new minute, make a new speech string and speak the time
                if (seconds < 1) {

                    // It says different things for 0, 1 and other minute values
                    val minuteOrMinutes = when (minute) {
                        0 -> "precisely"
                        1 -> "$minute minute"
                        else -> "$minute minutes"
                    }

                    val spokenText = "It's $hour12 o'clock,  $minuteOrMinutes"

                    //  When QUEUE_FLUSH is passed to the speak method, it discards any
                    //  speeches that were already queued up and speaks the new speech immediately.
                    tts!!.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "")
                }

                // This "handler" stuff is needed to update the time text-box (in the UI thread)
                handler.post {
                    val amPM = if (hour < 12) "AM" else "PM"
                    val screenText = String.format("%02d : %02d : %02d %s", hour12, minute, seconds, amPM)
                    tvTime.text = screenText
                }
                // Sleep for 1 second
                Thread.sleep(1_000)
            }
        }.start()
    }

    // Called to signal the completion of the TextToSpeech engine initialization.
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            }
        }
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

}