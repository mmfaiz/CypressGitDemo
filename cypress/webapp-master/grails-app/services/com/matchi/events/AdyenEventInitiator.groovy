package com.matchi.events

class AdyenEventInitiator implements EventInitiator {

   private static final String EVENT_INITIATOR_ADYEN = "Adyen"

   @Override
   String getInitiatorIdentifier() {
      return EVENT_INITIATOR_ADYEN
   }
}



