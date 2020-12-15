package com.matchi.admin

import com.matchi.Region
import com.matchi.Municipality
import grails.validation.Validateable

class AdminRegionController {

    def index() {
        def regions = Region.list( [cache:true] )

        [ regions:regions ]
    }

    def create() { }

    def createMunicipality() {
        [ region:Region.get(params.id) ]
    }

    def edit() {
        [ region:Region.get(params.id) ]
    }

    def editMunicipality() {
        [ municipality:Municipality.get(params.id) ]
    }

    def save(RegionCommand cmd) {
        if (cmd.hasErrors()) {
            render(view: "create", model: [ cmd:cmd ])
            return
        }

        def region = new Region()
        region.country = cmd.country
        region.name = cmd.name
        region.lat = cmd.lat
        region.lng = cmd.lng
        region.zoomlv = cmd.zoomlv

        if( region.save() ) {
            flash.message = message(code: "adminRegion.save.success", args: [cmd.name])
        }

        redirect(action: "index")
    }

    def saveMunicipality(MunicipalCommand cmd) {
        if (cmd.hasErrors()) {
            render(view: "create", model: [ cmd:cmd ])
            return
        }

        def municipality = new Municipality()
        municipality.name = cmd.name
        municipality.lat = cmd.lat
        municipality.lng = cmd.lng
        municipality.zoomlv = cmd.zoomlv
        municipality.region = Region.get(cmd.regionId)

        if( municipality.save() ) {
            // Need to invalidate cache by modifying the collection on owning object (Facility)
            // https://planet.jboss.org/post/collection_caching_in_the_hibernate_second_level_cache
            municipality.region.addToMunicipalities(municipality)
            flash.message = message(code: "adminRegion.saveMunicipality.success", args: [cmd.name])
        }

        redirect(action: "edit", params: [ id: cmd.regionId ])
    }

    def update(RegionCommand cmd) {
        if (cmd.hasErrors()) {
            render(view: "edit", model: [ cmd:cmd ])
            return
        }

        def region = Region.get(cmd.id)
        region.country = cmd.country
        region.name = cmd.name
        region.lat = cmd.lat
        region.lng = cmd.lng
        region.zoomlv = cmd.zoomlv

        if( region.save() ) {
            flash.message = message(code: "adminRegion.update.success", args: [cmd.name])
        }

        flash.message = "Region ${cmd.name} uppdaterad"
        redirect(action: "index")
    }

    def updateMunicipality(MunicipalCommand cmd) {
        if (cmd.hasErrors()) {
            render(view: "editMunicipality", model: [ cmd:cmd ])
            return
        }

        def municipality = Municipality.get(cmd.id)
        municipality.name = cmd.name
        municipality.lat = cmd.lat
        municipality.lng = cmd.lng
        municipality.zoomlv = cmd.zoomlv

        if( municipality.save() ) {
            flash.message = message(code: "adminRegion.updateMunicipality.success", args: [cmd.name])
        }

        redirect(action: "edit", id: cmd.regionId)
    }

    def delete() {
        def region = Region.get(params.id)

        region.delete(failOnError: true)
        flash.message = message(code: "adminRegion.delete.success", args: [region.name])

        redirect(action: "index")
    }

    def deleteMunicipality() {
        def municipality = Municipality.get(params.id)
        def region = municipality.region

        municipality.delete(failOnError: true)
        flash.message = message(code: "adminRegion.deleteMunicipality.success", args: [municipality.name])

        redirect(action: "edit", id: region.id)
    }
}

@Validateable(nullable = true)
class RegionCommand {
    Long id
    String country
    String name
    Double lng
    Double lat
    int zoomlv

    static constraints = {
        country(nullable: false, blank: false)
        name(nullable: false, blank: false)
        lng(nullable: false, blank: false)
        lat(nullable: false, blank: false)
        zoomlv(nullable: false, blank: false)
    }
}

@Validateable(nullable = true)
class MunicipalCommand {
    Long id
    Long regionId
    String name
    Double lng
    Double lat
    int zoomlv

    static constraints = {
        regionId(nullable: false, blank: false)
        name(nullable: false, blank: false)
        lng(nullable: false, blank: false)
        lat(nullable: false, blank: false)
        zoomlv(nullable: false, blank: false)
    }
}
