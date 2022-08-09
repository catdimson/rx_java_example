package com.example.rxjaxaexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.annotation.WorkerThread
import com.example.rxjaxaexample.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitRxButton.setOnClickListener {
            Observable.just(binding.inputEditText.text)           // just() - создать обощреваемый объект из 1 объекта
                .map { it.toString() }
                .map { it.uppercase() }
                .map { it.reversed() }
                .observeOn(Schedulers.computation()) // Scheduler.io() - получение данных из сети, бд и тд. Scheduler.computation() - расчет
                .map { changeString(it) }
                .observeOn(Schedulers.io())
                .map { appendDate(it) }
                .observeOn(AndroidSchedulers.mainThread()) // возвращаемся на главный поток, тк трогаем вьюшку
                .subscribe {
                    binding.resultTextView.text = it
                }                                      // subscribe() - запускает всю цепочку. Иначе - не запустится
                // Thread.sleep(n) в главном потоке быть не должно!!!!!
                // .observeOn() - меняет поток всем последующим операциям
        }

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
        if (Thread.currentThread() == Looper.getMainLooper().thread) { // имитация падения, т.е. если вызовен из главного потока
            throw RuntimeException("Не верный поток")
        }
        Thread.sleep(3_000)
        input.forEach {
            sb.append(it)
            sb.append(" ")
        }
        return sb.toString()
    }

    @WorkerThread
    private fun appendDate(input: String): String {
        if (Thread.currentThread() == Looper.getMainLooper().thread) { // имитация падения, т.е. если вызовен из главного потока
            throw RuntimeException("Не верный поток")
        }
        Thread.sleep(1_000)
        val date = Calendar.getInstance().time
        val dateStr = SimpleDateFormat().format(date)
        return "$input $dateStr"
    }
}