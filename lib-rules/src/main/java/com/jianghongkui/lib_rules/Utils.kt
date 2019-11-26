package com.jianghongkui.lib_rules

import com.jianghongkui.lib_rules.Utils.RESULTMAP
import java.util.Arrays

/**
 * @author jianghongkui
 * @date 2018/10/10
 */
object Utils {
    val SIZE_UNITS: List<String> = Arrays.asList("dp", "dip", "dpi", "px", "sp")

    val RESULTMAP: Map<String, String> = mapOf("\\d+" to "数字", "[a-z]" to "小写字母", "[A-Z]" to "大写字母")

    @JvmStatic
    fun isEmpty(s: String?): Boolean {
        return s == null || "" == s.trim()
    }
}

fun String.getResultStr(): String? {
    if (isEmpty())
        return ""
    var s1 = this
    var s2 = s1
    val builder = StringBuilder()
    for ((key, value) in RESULTMAP) {
        s1 = delete(key)
        if (s1.length != s2.length) {
            builder.append(value)
            builder.append("和")
        }
        s2 = s1
        if (s2.isEmpty()) {
            return builder.deleteCharAt(builder.length - 1).toString()
        }
    }
    val sets = HashSet<Char>(s1.length)
    for (c in s1.toCharArray()) {
        sets.add(c)
    }
    for (character in sets) {
        builder.append(character)
        builder.append('、')
    }
    builder.deleteCharAt(builder.length - 1)
    return builder.toString()
}

fun String.checkLowerCamelCase(): String? {
    if (isEmpty()) return null
    val result = delete("\\d+|[a-z]|[A-Z]".toRegex())
    if (!result.isEmpty()) return "不能包含${result.getResultStr()}"
    val c = first()
    return if (c < 'a' || c > 'z') {
        "首字母必须小写"
    } else null
}

fun String.delete(regex: Regex): String = replace(regex, "").trim()

fun String.delete(regexStr: String): String = replace(regexStr.toRegex(), "").trim()

fun String.getUnitFromString(): String {
    for (ss in Utils.SIZE_UNITS) {
        if (endsWith(ss)) return ss
    }
    return ""
}

fun String.isChinese(): Boolean {
    if (isNullOrEmpty()) return false
    for (c in toCharArray()) {
        if (c.isChinese()) return true// 有一个中文字符就返回
    }
    return false
}

private fun Char.isChinese(): Boolean = toInt() in 0x4E00..0x9FA5// 根据字节码判断