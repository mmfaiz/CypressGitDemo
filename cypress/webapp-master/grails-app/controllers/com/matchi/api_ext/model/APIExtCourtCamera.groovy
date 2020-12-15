package com.matchi.api_ext.model

import com.matchi.Camera

class APIExtCourtCamera {
    Long id
    String name

    APIExtCourtCamera(Camera camera) {
        this.id = camera.cameraId
        this.name = camera.name
    }

    APIExtCourtCamera(Long id, String name) {
        this.id = id
        this.name = name
    }
}
