package com.matchi.googletagmanager

import com.matchi.Facility

class GoogleTagManagerService {

    private static final GOOGLE_TAG_MANAGER_MATCHI_CONTAINER_ID = "GTM-NZDDFSM"

    static List<String> getGoogleTagManagerContainers(Facility facility) {
        List<String> googleTagManagerContainers = new ArrayList<String>()

        // Default MATCHi Google Tag Manager.
        googleTagManagerContainers.add(GOOGLE_TAG_MANAGER_MATCHI_CONTAINER_ID)

        // Third party Google Tag Manager containers only used on facility specific views.
        if(facility?.googleTagManagerContainerId)
            googleTagManagerContainers.add(facility.googleTagManagerContainerId)

        return googleTagManagerContainers
    }
}
