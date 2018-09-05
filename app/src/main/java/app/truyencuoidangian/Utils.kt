package app.truyencuoidangian

import java.text.Normalizer
import java.util.regex.Pattern

fun String.removeAccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D')
}

fun String.removeWhiteSpaces(): String {
    return this.replace("\\s".toRegex(), "")
}