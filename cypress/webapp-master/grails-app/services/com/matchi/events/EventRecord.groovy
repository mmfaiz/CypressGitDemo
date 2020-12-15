package com.matchi.events

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

abstract class EventRecord {

   EventRecord(EventType eventType, EventInitiator eventInitiator) {
      this.eventType = eventType
      this.initiator = eventInitiator?.getInitiatorIdentifier()
      timeStamp = new DateTime(DateTimeZone.UTC)
   }

   // Structured data fields common for all events.
   EventType eventType
   DateTime timeStamp
   String initiator // For example id of user or 'Adyen'

   // JSON data for the actual affected domain object
   abstract def data
}


