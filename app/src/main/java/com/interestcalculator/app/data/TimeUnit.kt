package com.interestcalculator.app.data

enum class TimeUnit(val label: String, val daysMultiplier: Int) {
    DAYS("Days", 1),
    MONTHS("Months (30 days)", 30),
    YEARS("Years (365 days)", 365)
}
