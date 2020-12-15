package com.matchi.marshallers

import com.matchi.Camera
import grails.converters.JSON

import javax.annotation.PostConstruct

class CameraMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Camera) { Camera camera ->
            marshallCamera(camera)
        }
    }

    def marshallCamera(Camera camera) {
        [
                id: camera.cameraId,
                name: camera.name
        ]
    }
}
