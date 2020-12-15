package com.matchi.sie.Document

import jline.internal.Log
import org.apache.commons.lang.math.NumberUtils
import org.joda.time.DateTime

import java.text.NumberFormat


class Renderer {

    public List<String> lines = []

    void addLine(String label, List<Object> values)
    {
        this.append("#" + label + " " + this.formatValues(values).join(" "))
    }

    void addBeginningOfArray()
    {
        this.append("{")
    }

    void addEndOfArray()
    {
        this.append("}")
    }

    String render()
    {
        this.lines[] = ""
        return this.lines.join("\n")
    }

    private append(String text)
    {
        this.lines.add(encoded(text))
    }

    private List<String> formatValues(List<Object> values)
    {
        List<String> subValues = []
        values.each { subValue ->
            subValues.add(this.formatValue(subValue))
        }
        return subValues
    }

    private String encoded(String text)
    {
        return CP437Encoding.convertFromUTF8ToCP437(text)
    }

    private String formatValue(Object value)
    {

        if(value instanceof Date) {
            return value.format("YMd")
        }
        else if(value instanceof Map) {
            List subValues = []
            if(value.size()>0) {
                value.each {
                    entry ->
                        subValues.add(this.formatValue(entry?.key?.toString()))
                        subValues.add(this.formatValue(entry?.value?.toString()))
                }
            }
            return "{" + subValues.collect{ it }.join(" ") + "}"
        }
        else if (value.toString().isInteger()) {
            return (String) value
        }
        else {
            return '"' + value.toString().replace('"','\"') + '"'
        }

    }
}
