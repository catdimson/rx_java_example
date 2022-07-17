package com.example.rxjaxaexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.WorkerThread
import com.example.rxjaxaexample.databinding.ActivityMainBinding
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitButton.setOnClickListener {
            val inputText = binding.inputEditText.text.toString()
            val upperText = inputText.uppercase()
            val reversedText = upperText.reversed()
            Thread {
                try {
                    val changedText = changeString(reversedText)
                    Thread {
                        try {
                            val dateText = appendDate(changedText)
                            runOnUiThread {
                                binding.resultTextView.text = dateText
                            }
                        } catch (ie: InterruptedException) {
                            // todo
                        }
                    }.start()
                } catch (ie: InterruptedException) {
                    // todo
                }
            }.start()
        }
    }

    @WorkerThread
    private fun changeString(input: String): String {
        val sb = StringBuilder()
        Thread.sleep(3_000)
        input.forEach {
            sb.append(it)
            sb.append(" ")
        }
        return sb.toString()
    }

    @WorkerThread
    private fun appendDate(input: String): String {
        val date = Calendar.getInstance().time
        val dateStr = SimpleDateFormat().format(date)
        return "$input $dateStr"
    }
}