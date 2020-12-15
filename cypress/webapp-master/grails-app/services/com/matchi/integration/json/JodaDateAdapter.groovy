package com.matchi.integration.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.joda.time.DateTime

import java.lang.reflect.Type

class JodaDateAdapter extends DateAdapter implements JsonSerializer<DateTime> {

    @Override
    JsonElement serialize(DateTime date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(formatter.print(date))
    }
}
