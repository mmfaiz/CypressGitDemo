package com.matchi.integration.events

class Court implements Event {
    final String id
    final Facility facility
    final String name
    final String description
    final Boolean indoor
    final String surface
    final Camera[] cameras

    Court(com.matchi.Court court) {
        this.id = court.id
        this.name = court.name
        this.description = court.description
        this.indoor = court.indoor
        this.surface = court.surface.name()
        this.facility = new Facility(court.facility)
        this.cameras = court.cameras?.collect { new Camera(it) }
    }

    @Override
    String getKey() {
        return id
    }

    @Override
    String getTopic() {
        return "facilitysystem.fct.court.0"
    }
}

enum CourtEventType implements EventType<Court> {
    CREATED, UPDATED, DELETED
}