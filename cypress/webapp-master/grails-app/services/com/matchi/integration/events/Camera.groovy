package com.matchi.integration.events

class Camera {
    final String name
    final int cameraId
    final String cameraProvider

    Camera(com.matchi.Camera camera) {
        this.name = camera.name
        this.cameraId = camera.cameraId
        this.cameraProvider = camera.cameraProvider?.toString()
    }
}
