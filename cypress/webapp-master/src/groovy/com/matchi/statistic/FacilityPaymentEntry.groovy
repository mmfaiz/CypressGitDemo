package com.matchi.statistic

class FacilityPaymentEntry {
    def facilityId = null
    def cid = null
    def type = null
    def article = null
    def sport = null
    Boolean indoor = true
    def price = 0
    def revenue = 0
    def num = 0
    def numCredited = 0
    def totalCreditFees = 0
    def numOriginApi = 0
    def numOriginWeb = 0
    def couponFacilityId = null
    def groupedSubFacilityId = null

    def getTotalRevenue() {
        return num * revenue
    }
}
