package com.matchi.tournaments

import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.S3Object
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.aws.S3ObjectWrapper
import com.sun.mail.iap.ResponseInputStream
import grails.plugin.awssdk.AmazonWebService
import org.codehaus.groovy.grails.commons.GrailsApplication
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder

class ExcelLeagueService {

    AmazonWebService amazonWebService
    GrailsApplication grailsApplication

    ExcelLeague getExcelLeague(Facility facility, Customer customer) {
        String leagueJson = getJsonForFacilityFromAwsS3(facility.id.toString())
        if(leagueJson?.isEmpty()) return null

        ExcelLeague excelLeague = new JsonSlurper().parseText(leagueJson) as ExcelLeague
        excelLeague.removePrivateLeagues(customer)
        return excelLeague
    }

    private String getJsonForFacilityFromAwsS3(String facilityPrefix) {
        String bucketName = grailsApplication.config.matchi.excelleague.s3Bucket
        String key = facilityPrefix + grailsApplication.config.matchi.excelleague.s3Key

        try {
            return new S3ObjectWrapper(amazonWebService.s3.getObject(bucketName, key)).toJsonString()
        }
        catch (AmazonS3Exception s3Exception) {
            log.error("No file found on AWS S3 with name " + key + " in bucket " + bucketName + ": " + s3Exception.message)
        }

        return ""
    }
}
