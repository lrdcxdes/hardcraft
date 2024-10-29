package dev.lrdcxdes.hardcraft.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun Double.formatPrice2(): String {
    val decimalFormat = DecimalFormat("#.##")
    return decimalFormat.format(this)
}

fun Double.formatPrice(): String {
    val decimalFormatSymbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
        groupingSeparator = ','
    }
    val decimalFormat = DecimalFormat("#,###.###", decimalFormatSymbols)
    return decimalFormat.format(this)
}
