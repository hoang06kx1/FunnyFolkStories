package app.truyencuoidangian

import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

fun String.removeAccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D')
}

fun String.removeWhiteSpaces(): String {
    return this.replace("\\s".toRegex(), "")
}

fun getTimeString(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return ""
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    val dateString = formatter.format(Date(timestamp))
    val timeFormatter = SimpleDateFormat("HH:mm")
    val hourString = timeFormatter.format(timestamp)
    return "Đã xem vào lúc $hourString ngày $dateString"
}