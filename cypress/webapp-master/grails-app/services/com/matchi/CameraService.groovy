package com.matchi

class CameraService {

    def deleteCamerasByCourt(Court court) {
        Camera.findAllByCourt(court)*.delete()
    }

    Camera createCamera(String name, Integer cameraId, Camera.CameraProvider cameraProvider, Court court) {
        Camera camera = new Camera(name: name, cameraId: cameraId, cameraProvider: cameraProvider, court: court)
        camera.save(failOnError: true)
    }
}
