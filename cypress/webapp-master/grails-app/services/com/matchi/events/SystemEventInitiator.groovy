package com.matchi.events

// Initiated by system user for example triggered by a periodically job (not by user interaction).
class SystemEventInitiator implements EventInitiator {

   private static final String EVENT_INITIATOR_SYSTEM = "System"

   @Override
   String getInitiatorIdentifier() {
      return EVENT_INITIATOR_SYSTEM
   }
}



