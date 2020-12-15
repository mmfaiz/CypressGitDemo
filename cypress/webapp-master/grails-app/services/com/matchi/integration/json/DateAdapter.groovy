package com.matchi.integration.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder

import java.lang.reflect.Type

class DateAdapter implements JsonSerializer<Date>{
    protected final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            .toFormatter();

    @Override
    JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(formatter.print(date.getTime()))
    }
}
