/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.twlk.lib_rules.base

import com.android.tools.lint.detector.api.Location

/**
 *
 * @author twlk
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