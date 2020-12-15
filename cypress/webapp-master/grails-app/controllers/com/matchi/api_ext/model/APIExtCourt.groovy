package com.matchi.api_ext.model

class APIExtCourt {
    Long id
    String name
    Long position
    Boolean membersOnly
    Boolean offlineOnly
    Boolean indoor
    String surface
    APIExtSport sport
    List<APIExtCourtCamera> cameras

    APIExtCourt(Long id, String name, Long position, Boolean membersOnly, Boolean offlineOnly, Boolean indoor, String surface, APIExtSport sport, List<APIExtCourtCamera> cameras) {
        this.id = id
        this.name = name
        this.position = position
        this.membersOnly = membersOnly
        this.offlineOnly = offlineOnly
        this.indoor = indoor
        this.surface = surface
        this.sport = sport
        this.cameras = cameras
    }

    public static List<APIExtCourtCamera> parseCameras(String cameraStrings) {
        List<APIExtCourtCamera> cameras = new ArrayList<>()

        if (cameraStrings != null) {
            cameraStrings.split(",").each {cameraString ->
                String[] camera = cameraString.split(":")
                cameras.add(new APIExtCourtCamera(camera[0].toLong(), camera[1]))
            }
        }

        return cameras
    }
}
