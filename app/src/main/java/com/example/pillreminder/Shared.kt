package com.example.pillreminder

import java.util.Calendar

object Shared {
    var pillNumber: Int = 0
    // todo: godzina i minuta na powiadomienie
    // todo: godzina powiadomienia o kupnie nowych tabletek

    fun printStepsGoal() {
        println("Current pill number: $pillNumber")
    }
}