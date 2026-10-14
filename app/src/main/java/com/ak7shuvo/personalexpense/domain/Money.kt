package com.ak7shuvo.personalexpense.domain

import java.math.BigDecimal

/** Decimal-safe money helpers. Amounts are Long minor units (poisha); no Double ever touches a balance. */
object Money {
    const val SYMBOL = "৳"
    const val MAX_MINOR = 999_999_999_999L // ৳9,999,999,999.99

    private val input = Regex("""^(\d+\.?\d{0,2}|\.\d{1,2})$""")

    /** Parses user input like "1,250.5" into minor units. Null when malformed, negative or too large. */
    fun parse(text: String): Long? {
        val s = text.trim().replace(",", "").replace(" ", "")
        if (!input.matches(s)) return null
        val minor = try {
            BigDecimal(s).movePointRight(2).longValueExact()
        } catch (_: ArithmeticException) {
            return null
        }
        return if (minor in 0..MAX_MINOR) minor else null
    }

    /** Keeps only digits and one dot, at most 2 decimals and 10 integer digits, while typing. */
    fun sanitizeInput(raw: String): String {
        val sb = StringBuilder()
        var dot = false
        var decimals = 0
        var whole = 0
        for (c in raw) {
            when {
                c.isAsciiDigit() -> {
                    if (dot) {
                        if (decimals < 2) { sb.append(c); decimals++ }
                    } else if (whole < 10) { sb.append(c); whole++ }
                }
                c == '.' && !dot -> { dot = true; sb.append(c) }
            }
        }
        return sb.toString()
    }

    private fun Char.isAsciiDigit() = this in '0'..'9'

    /** Whole part with Bangladeshi (lakh/crore) grouping: 1234567 -> "12,34,567". */
    fun groupWhole(minor: Long): String {
        val digits = (Math.abs(minor) / 100).toString()
        if (digits.length <= 3) return digits
        val head = digits.dropLast(3)
        val tail = digits.takeLast(3)
        val grouped = head.reversed().chunked(2).joinToString(",").reversed()
        return "$grouped,$tail"
    }

    /** ".05" style fraction part. */
    fun fraction(minor: Long): String = ".%02d".format(java.util.Locale.ROOT, Math.abs(minor) % 100)

    /** "৳12,34,567.50"; negative values get a leading minus. */
    fun format(minor: Long): String {
        val sign = if (minor < 0) "-" else ""
        return "$sign$SYMBOL${groupWhole(minor)}${fraction(minor)}"
    }

    /** Editable text for an amount: "1250" or "1250.50". */
    fun toInput(minor: Long): String {
        val abs = Math.abs(minor)
        return if (abs % 100 == 0L) (abs / 100).toString()
        else "${abs / 100}.%02d".format(java.util.Locale.ROOT, abs % 100)
    }

    /** Short axis label: 950, 12k, 1.5L (lakh), 2Cr (crore). */
    fun compact(minor: Long): String {
        val taka = Math.abs(minor) / 100.0
        fun trim(v: Double) = if (v >= 10 || v % 1.0 == 0.0) v.toLong().toString() else "%.1f".format(java.util.Locale.ROOT, v)
        return when {
            taka >= 1e7 -> trim(taka / 1e7) + "Cr"
            taka >= 1e5 -> trim(taka / 1e5) + "L"
            taka >= 1e3 -> trim(taka / 1e3) + "k"
            else -> taka.toLong().toString()
        }
    }
}
