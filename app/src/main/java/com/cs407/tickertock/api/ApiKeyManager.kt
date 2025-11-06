package com.cs407.tickertock.api

object ApiKeyManager {
    private val apiKeys = listOf(
        "S1Q3JC1NDLE7ERI7",
        "7GJ66YO83V3LXCJN",
        "S9XTI2ITBQEI99LB",
        "9VN02FGK60FGL723",
        "TJH0L0KBOLQITN1K",
        "8EZ2H7EQYHXLDMSN",
        "L5DWR0TWHQS75PE3",
        "PDXFW0RRMBZCOHYC",
        "RRR97PZE28LKA2NP",
        "1Y68UB0NP6HV5TKD",
        "TC0C1L17CG78EFFX",
        "SWVLWR5X3G5IY856",
        "V88IWY3Q826SR90F",
        "GONBE5UF1QF0LGHS",
        "WJHS7BXJHRB611VT",
        "S85H1CQZGSGF6NKI"
    )

    private var currentIndex = 0

    /**
     * Get the next API key in round-robin fashion
     */
    @Synchronized
    fun getNextKey(): String {
        val key = apiKeys[currentIndex]
        currentIndex = (currentIndex + 1) % apiKeys.size
        return key
    }

    /**
     * Get total number of API keys available
     */
    fun getTotalKeys(): Int {
        return apiKeys.size
    }
}
