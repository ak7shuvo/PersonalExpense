package com.ak7shuvo.personalexpense.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun parsesPlainAndDecimal() {
        assertEquals(125_000L, Money.parse("1250"))
        assertEquals(125_050L, Money.parse("1250.5"))
        assertEquals(125_005L, Money.parse("1250.05"))
        assertEquals(50L, Money.parse(".5"))
        assertEquals(1_200L, Money.parse("12."))
        assertEquals(123_456_789L, Money.parse("1,234,567.89"))
    }

    @Test fun parseIsExactForValuesBinaryFloatsGetWrong() {
        assertEquals(1_010L, Money.parse("10.10"))
        assertEquals(29L, Money.parse("0.29")) // 0.29 * 100 = 28.999… in Double
        assertEquals(1_005L, Money.parse("10.05"))
    }

    @Test fun parseRejectsBadInput() {
        for (bad in listOf("", "abc", "1.234", "-5", "1e3", "1.2.3", ".", "99999999999.00")) assertNull(bad, Money.parse(bad))
    }

    @Test fun sanitizeKeepsTypingValid() {
        assertEquals("12.34", Money.sanitizeInput("12.3456"))
        assertEquals("1.5", Money.sanitizeInput("1a.5-"))
        assertEquals("1.23", Money.sanitizeInput("1.2.3"))
        assertEquals("1234567890", Money.sanitizeInput("12345678901"))
    }

    @Test fun formatsWithLakhGrouping() {
        assertEquals("৳0.00", Money.format(0))
        assertEquals("৳999.99", Money.format(99_999))
        assertEquals("৳1,000.00", Money.format(100_000))
        assertEquals("৳1,23,456.78", Money.format(12_345_678))
        assertEquals("৳1,00,00,000.00", Money.format(1_000_000_000))
        assertEquals("-৳500.00", Money.format(-50_000))
    }

    @Test fun toInputRoundTrips() {
        for (v in listOf(0L, 5L, 100L, 123_45L, 99_999_999L)) assertEquals(v, Money.parse(Money.toInput(v)))
        assertEquals("1250", Money.toInput(125_000))
        assertEquals("1250.50", Money.toInput(125_050))
    }

    @Test fun compactLabels() {
        assertEquals("950", Money.compact(95_000))
        assertEquals("12k", Money.compact(1_200_000))
        assertEquals("1.5L", Money.compact(15_000_000))
        assertEquals("2Cr", Money.compact(2_000_000_000))
    }
}
