package com.matchi

import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.Trainer
import grails.test.mixin.TestFor

@TestFor(TrainingPlanningTagLib)
class TrainingPlanningTagLibTests {

    void testPageBreakInsideOccasions() {
        def trainers = []
        def participants = []
        3.times {trainers.add(new Trainer())}
        14.times {participants.add(new Participant())}
        def extraOccasion = new ActivityOccasion(message: "<p>Description</p>", trainers: trainers, participants: participants)

        def out = new TrainingPlanningTagLib().extraPageBreaker(position: "inside", extraOccasion: extraOccasion)

        assert "<div id='break'></div>" == out.toString()
    }

    void testPageBreakBetweenOccasionsGroups() {
        def occasions = [:]
        def trainers = []
        def participants = []
        3.times {trainers.add(new Trainer())}
        5.times {participants.add(new Participant())}
        occasions.put(14, [new ActivityOccasion(message: "<p>Description1</p>", trainers: trainers, participants: participants)])
        occasions.put(15, [new ActivityOccasion(message: "<p>Description2</p>", trainers: trainers, participants: participants)])
        def tpTagLib = new TrainingPlanningTagLib()

        assert "<div id='break'></div>" == tpTagLib.extraPageBreaker(position: "between", occasions: occasions).toString()
        assert "<div id='break'></div>" == tpTagLib.extraPageBreaker(position: "between", occasions: occasions, isStartDay: true).toString()
    }
}
