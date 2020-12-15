package com.matchi.marshallers

import com.matchi.watch.ClassActivityWatch
import grails.converters.JSON

import javax.annotation.PostConstruct

/**
 * @author Sergei Shushkevich
 */
class ClassActivityWatchMarshaller {

    @PostConstruct
    void register() {
        JSON.registerObjectMarshaller(ClassActivityWatch) { ClassActivityWatch watch ->
            marshallClassActivityWatch(watch)
        }
    }

    def marshallClassActivityWatch(ClassActivityWatch watch) {
        [
            id: watch.id,
            activity: [id: watch.classActivity.id, name: watch.classActivity.name],
            from: watch.fromDate.format("yyyy-MM-dd HH:mm"),
            to: watch.toDate.format("yyyy-MM-dd HH:mm"),
            facility: watch.facility
        ]
    }
}