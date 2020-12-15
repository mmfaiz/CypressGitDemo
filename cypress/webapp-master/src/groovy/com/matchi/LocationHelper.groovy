package com.matchi

class LocationHelper {
    /* Translate ZoomLevel from locations to radius in kilometers
     * Made by approximations and skilled map knowledge by Martin
     */
    private final static Map<Integer, Long> ZOOM_LEVEL_TO_RADIUS = new LinkedHashMap<Integer, Long>(
            1: 500L,
            2: 500L,
            3: 500L,
            4: 500L,
            5: 300L,
            6: 150L,
            7: 75L,
            8: 50L,
            9: 30L,
            10: 10L,
            11: 5L,
            12: 3L,
            13: (1.5).toLong(),
            14: 1L,
            15: (0.5).toLong(),
    ).asImmutable()

    static Long getApproxRadiusByZoomLevel(Integer zoomLevel) {
        return ZOOM_LEVEL_TO_RADIUS[zoomLevel]
    }
}
