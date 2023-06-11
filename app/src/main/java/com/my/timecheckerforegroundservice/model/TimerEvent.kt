package com.my.timecheckerforegroundservice.model

/**
created by Mehmet E. Yıldız
 **/
sealed class TimerEvent {
    object START : TimerEvent()
    object END : TimerEvent()

}