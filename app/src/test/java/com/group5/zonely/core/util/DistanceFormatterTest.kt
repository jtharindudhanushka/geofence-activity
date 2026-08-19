package com.group5.zonely.core.util

import com.group5.zonely.domain.model.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatterTest {

    private val formatter = DistanceFormatter()

    @Test
    fun `format metric small distance`() {
        assertEquals("100 m", formatter.format(100f, DistanceUnit.METRIC))
    }

    @Test
    fun `format metric large distance`() {
        assertEquals("1.2 km", formatter.format(1234f, DistanceUnit.METRIC))
    }

    @Test
    fun `format imperial small distance`() {
        // 100m * 3.28084 = 328.084 ft -> 328 ft
        assertEquals("328 ft", formatter.format(100f, DistanceUnit.IMPERIAL))
    }

    @Test
    fun `format imperial large distance`() {
        // 2000m * 3.28084 = 6561.68 ft -> 6561.68 / 5280 = 1.2427... mi -> 1.2 mi
        assertEquals("1.2 mi", formatter.format(2000f, DistanceUnit.IMPERIAL))
    }
}
