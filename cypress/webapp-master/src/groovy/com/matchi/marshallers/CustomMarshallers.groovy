package com.matchi.marshallers

class CustomMarshallers {
    List marshallers = []

    def register(def json) {
        marshallers.each{ it.register(json) }
    }
}
