package com.example.macc_project.utilities

import android.os.Handler
import android.os.Looper

class ExtraInfo {

    companion object{

        var myUsername:String = ""
        var myLobbyID:String = "1"
        var myTime:Int  = 0
        var myScore:Int = 0
        var myLevel:Int = 1
        var actualMilliseconds = 0
        var myEmail = ""


        var MAX_LEVEL: Int = 3
        var scoreThreshold1ms = 15000
        var scoreThreshold1pts = 10
        var scoreThreshold2ms = 30000
        var scoreThreshold2pts = 5
        var scoreThreshold3pts = 2


        fun setEmail (email:String){
            myEmail = email.toString()
        }
        fun setUsername(username:String){
            myUsername = username
        }

        fun setLobbyID(lobbyID:String) {
            myLobbyID = lobbyID.toString()
        }

        fun setTime(time:Int){
            myTime = time
        }

        fun setScore(score:Int){
            if (myScore <= 0 && score < 0) return
            myScore += score
        }

        fun updateLevel(){
            myLevel +=1
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
            actualMilliseconds += 10
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
        actualMilliseconds = 0
        println("Timerstarted :$actualMilliseconds")
        handler.post(updateTimer)
    }

    fun stopTimer() {
        handler.removeCallbacks(updateTimer)
        val timerMinutes = minutes
        val timerSeconds = seconds
        val timerDeciseconds = deciseconds
        val timerMilliseconds = milliseconds

        println("Timerended :$actualMilliseconds")

        milliseconds = 0
        deciseconds = 0
        seconds = 0
        minutes = 0

        timerUpdateListener?.onTimerFinished(timerMinutes, timerSeconds, timerDeciseconds, timerMilliseconds)

    }

}