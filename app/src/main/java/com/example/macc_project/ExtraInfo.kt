package com.example.macc_project

import android.icu.number.NumberFormatter.DecimalSeparatorDisplay
import android.os.Handler
import android.os.Looper

class ExtraInfo {

    companion object{

        lateinit var myUsername:String
        var myLobbyID:String = "1"
        var myTime:String = ""
        var myScore:Int = 0

        fun setUsername(username:String){
            myUsername = username.toString()
        }

        fun setLobbyID(lobbyID:String) {
            myLobbyID = lobbyID.toString()
        }

        fun setTime(time:String){
            myTime = time
        }

        fun setScore(score:Int){
            myScore += score
        }
    }


    // timer handler
    private val handler = Handler(Looper.getMainLooper())
    var minutes = 0
    var seconds = 0
    var deciseconds = 0
    var milliseconds = 0
    private var timerUpdateListener: TimerUpdateListener? = null

    fun setTimerUpdateListener(listener: TimerUpdateListener) {
        timerUpdateListener = listener
    }

    interface TimerUpdateListener {
        fun onTimerUpdate(minutes: Int, seconds: Int, deciseconds: Int, milliseconds: Int)
        fun onTimerFinished(minutes: Int, seconds:Int,  deciseconds: Int, milliseconds:Int )
    }

    private val updateTimer = object : Runnable {
        override fun run() {
            milliseconds += 10
            if (milliseconds == 100) {
                deciseconds++
                milliseconds = 0
            }
            if (deciseconds == 10){
                seconds++
                deciseconds = 0
            }
            if (seconds == 60){
                minutes++
                seconds = 0
            }
            timerUpdateListener?.onTimerUpdate(minutes, seconds, deciseconds, milliseconds)
            handler.postDelayed(this,10)
        }
    }

    fun startTimer() {
        println("Timer started")
        handler.post(updateTimer)

    }

    fun stopTimer() {
        handler.removeCallbacks(updateTimer)
        val timerMinutes = minutes
        val timerSeconds = seconds
        val timerDeciseconds = deciseconds
        val timerMilliseconds = milliseconds

        milliseconds = 0
        deciseconds = 0
        seconds = 0
        minutes = 0

        timerUpdateListener?.onTimerFinished(timerMinutes, timerSeconds, timerDeciseconds, timerMilliseconds)

    }

}