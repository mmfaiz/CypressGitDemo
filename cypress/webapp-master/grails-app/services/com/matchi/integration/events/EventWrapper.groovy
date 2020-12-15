package com.matchi.integration.events


import org.joda.time.DateTime

class EventWrapper<T extends Event> {
    final DateTime timeStamp
    final Initiator initiator
    final T entity
    final String eventType

    EventWrapper(T entity, EventType<T> eventType, Initiator initiator, DateTime timeStamp = DateTime.now()) {
        this.timeStamp = timeStamp
        this.initiator = initiator
        this.entity = entity
        this.eventType = eventType.name()
    }

    String getKey() {
        return entity.getKey()
    }

    String getTopic() {
        return entity.getTopic()
    }
}


interface Event {
    String getKey()

    String getTopic()
}

interface EventType<Event> {
    String name()
}