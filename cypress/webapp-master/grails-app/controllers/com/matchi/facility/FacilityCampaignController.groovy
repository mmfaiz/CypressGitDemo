package com.matchi.facility

import com.matchi.GenericController;

class FacilityCampaignController extends GenericController {
	
	static layout = 'facilityLayout'

    def index() {
		def facility = getUserFacility()
		
		if(facility == null) {
			render(view: "noFacility")
			return
		}
		
		[facility:facility]
    }
}
