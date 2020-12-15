package com.matchi.schedule

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.ReadableDateTime

class Schedules {
    Log log = LogFactory.getLog(Schedules.class)
	def schedules = []
	
	def addSchedule(def schedule) {
		schedules << schedule
	}

    def firstSlot() {
        return schedules.findAll { !it.isEmpty() }
            .collect { it.firstSlot() }
            .min { it.start.millis }
    }

    def lastSlot() {
        return schedules.findAll { !it.isEmpty() }
                .collect { it.lastSlot() }
                .max { it.end.millis }
    }
    def firstSlotHour() {
        return schedules.findAll { !it.isEmpty() }
                .collect { it.firstSlot() }
                .min { it.start.getHourOfDay() }
    }


    def lastSlotHour() {
        return schedules.findAll { !it.isEmpty() }
                .collect { it.lastSlot() }
                .max { it.end.getHourOfDay() }
    }
	
	def freeSlotsOfDay(ReadableDateTime dateTime) {
		freeSlotsOfDay(dateTime, null)
	}

	def freeSlotsOfDay(ReadableDateTime dateTime, def surfaces, def sports) {
		def results = []
		schedules.each { schedule ->
			results.addAll(schedule.freeSlotsOfDay(dateTime, surfaces, sports))
		}
		
		return results
	}
	
	def facilitySchedule(def facility) {
		def result = null
		schedules.each { schedule ->
			if(schedule.facility.id.equals(facility.id)) {
				result = schedule
			}
		}
		return result
	}
}
