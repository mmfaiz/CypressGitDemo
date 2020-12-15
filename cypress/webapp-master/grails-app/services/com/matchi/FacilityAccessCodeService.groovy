package com.matchi

class FacilityAccessCodeService {

    def groovySql
    public final static String COURT_ID_SEPARATOR = ','

    def getCodesAndCourts(def facility, def codeIds = null) {
        if(!facility || codeIds?.size() == 0) {
            return []
        }

        String sqlquery = """
            select 
                fac.id as id,
                fac.content as content,
                fac.valid_from as validFrom,
                fac.valid_to as validTo,
                fac.active as active,
                group_concat(c.id separator ',') as courtIds,
                group_concat(c.name separator ',') as courtNames
            from 
                facility_access_code fac
                left join court_facility_access_codes cfac on cfac.facility_access_code_id = fac.id
                left join (select * from court c where c.archived = 0) c on cfac.court_id = c.id
            where
                fac.facility_id = :facilityId"""

        if(codeIds) {
            sqlquery += """
                and fac.id in (${codeIds.join(COURT_ID_SEPARATOR)})"""
        }

        sqlquery += """
            group by
                fac.id
	        ;"""

        def parameters = [facilityId: facility.id]

        def result = []
        def rows = groovySql.rows(sqlquery, parameters)

        rows.each {
            result << createAccessCodeItem(it)
        }
        groovySql.close()

        return result
    }

    def createAccessCodeItem(def row) {

        return [
                id: row.id,
                content: row.content,
                validFrom: row.validFrom,
                validTo: row.validTo,
                active: row.active,
                courtIds: row.courtIds?.split(COURT_ID_SEPARATOR).collect { Long.parseLong(it) },
                courtNames: row.courtNames?.split(COURT_ID_SEPARATOR).collect { it }
        ]
    }
}
