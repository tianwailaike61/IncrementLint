package com.jianghongkui.lib_rules.base

import com.android.tools.lint.detector.api.Location

/**
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
open class Position {
    @JvmField
    protected var start: Int = 0

    @JvmField
    protected var end: Int = 0

    constructor(location: Location) : this(location.start!!.line, location.end!!.line)

    constructor(start: Int, end: Int) {
        this.start = start
        this.end = end
    }

    fun isParent(position: Position): Boolean {
        return start <= position.start && end >= position.end
    }

    fun isChild(position: Position): Boolean {
        return start >= position.start && end <= position.end
    }

    override fun equals(other: Any?): Boolean {
        if (other is Position) {
            if (start == other.start && end == other.end)
                return true
        }
        return false
    }

    override fun toString(): String {
        val builder = StringBuilder("Position:")
        builder.append("[")
        builder.append(Integer.toString(start))
        builder.append(",")
        builder.append(Integer.toString(end))
        builder.append("]")
        return builder.toString()
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        return result
    }
}