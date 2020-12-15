package com.matchi.api.v2

import com.matchi.Facility
import com.matchi.LocationHelper
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.api.GenericAPIController
import grails.converters.JSON
import grails.validation.Validateable

class LocationResourceController extends GenericAPIController {

    static namespace = "v2"

    enum LocationType {
        REGION('region'), MUNICIPALITY('municipality'), FACILITY('facility')

        String key

        LocationType(String key) {
            this.key = key
        }

        static List<LocationType> list() {
            [REGION, MUNICIPALITY, FACILITY]
        }
    }

    def list( RetrieveLocationsCommand cmd ) {
        def locations = []

        if (cmd.types.contains(LocationType.REGION.key)) {
            List<Region> regions = Region.createCriteria().listDistinct() {
                if (cmd.search) {
                    like("name", "%${cmd.search}%")
                }
            }

            regions.each { region ->
                locations.add([
                        name: region.name,
                        latitude: region.lat,
                        longitude: region.lng,
                        zoomLevel: region.zoomlv,
                        filterRadius: LocationHelper.getApproxRadiusByZoomLevel(region.zoomlv),
                        type: LocationType.REGION.key,
                        id: region.id,
                ])
            }
        }

        if (cmd.types.contains(LocationType.MUNICIPALITY.key)) {
            List<Municipality> municipalities = Municipality.createCriteria().listDistinct() {
                if (cmd.search) {
                    like("name", "%${cmd.search}%")
                }
            }

            municipalities.each { municipality ->
                locations.add([
                        name: municipality.name,
                        latitude: municipality.lat,
                        longitude: municipality.lng,
                        zoomLevel: municipality.zoomlv,
                        filterRadius: LocationHelper.getApproxRadiusByZoomLevel(municipality.zoomlv),
                        type: LocationType.MUNICIPALITY.key,
                        id: municipality.id,
                ])
            }
        }

        if (cmd.types.contains(LocationType.FACILITY.key)) {
            List<Facility> facilities = Facility.createCriteria().listDistinct() {
                if (cmd.search) {
                    or {
                        like("name", "%${cmd.search}%")
                        like("description", "%${cmd.search}%")
                    }
                }
            }

            facilities.each { facility ->
                locations.add([
                        name        : facility.name,
                        latitude         : facility.lat,
                        longitude        : facility.lng,
                        zoomLevel   : 15,
                        filterRadius: LocationHelper.getApproxRadiusByZoomLevel(15),
                        type        : LocationType.FACILITY.key,
                        id    : facility.id,
                ])
            }
        }

        def sortedLocations = locations

        if (cmd.search) {
            sortedLocations = locations.sort {
                return !it.name.startsWith(cmd.search)
            }
        }

        render sortedLocations as JSON

    }
}


import com.matchi.api.v2.LocationResourceController.LocationType

@Validateable(nullable = true)
class RetrieveLocationsCommand {
    String search

    List<String> types = LocationType.list().collect {it.key}

    def types() {
        types.collect() { String.valueOf(it.key) }
    }

    static constraints = {
        search(nullable: false)
    }
}