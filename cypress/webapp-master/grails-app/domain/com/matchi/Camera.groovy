package com.matchi

class Camera implements Serializable {
    static belongsTo = [ court : Court ]

    String name // Friendly name for identification example: "Main camera", "Side camera"
    Integer cameraId // Uniquely identifiable ID of the camera provided by the camera provider
    CameraProvider cameraProvider

    static mapping = {
        sort "id"
    }

    String toString() { "$name" }

    static enum CameraProvider {
        CAAI('Caai'),
        NESSENCE('Nessence')

        static list() {
            return [CAAI, NESSENCE]
        }

        String name

        CameraProvider(String name) {
            this.name = name
        }

        static CameraProvider valueOfName( String name ) {
            values().find { it.name.toLowerCase() == name.toLowerCase() }
        }
    }
}
