package com.matchi

import com.matchi.conditions.CourtSlotCondition
import com.matchi.price.CourtPriceCondition
import com.matchi.requirements.RequirementProfile

class Court implements Serializable {

    private static final long serialVersionUID = 12L

    static belongsTo = [facility: Facility, sport: Sport]
    static hasMany = [slots: Slot, courtPriceConditions: CourtPriceCondition, courtSlotConditions: CourtSlotCondition, childs: Court, cameras: Camera, courtTypeAttributes: CourtTypeAttribute]
    static hasOne = [parent: Court]

    String name
    String description
    Boolean indoor
    Surface surface

    /** Property used for commmuncation with external system  */
    String externalId

    /** Determines of court is scheduled by Matchi or via integration */
    Boolean externalScheduling = false

    Integer listPosition

    Boolean archived = false

    Boolean showDescriptionOnline = false
    Boolean showDescriptionForAdmin = false

    Boolean membersOnly
    Boolean offlineOnly = false

    Restriction restriction = Restriction.NONE
    String profiles

    Set<CourtTypeAttribute> courtTypeAttributes

    static transients = ["requirementProfiles"]

    void setRequirementProfiles(List<String> profilesList) {
        if (profilesList)
            profiles = profilesList.join(',')
    }

    List<String> getRequirementProfiles() {
        return profiles?.split(',')
    }

    static constraints = {
        sport nullable: false
        description nullable: true, maxSize: 255
        surface nullable: true
        indoor nullable: true, default: true
        membersOnly nullable: true, default: false
        offlineOnly nullable: false, default: false
        externalId(nullable: true)
        externalScheduling nullable: true, default: false
        listPosition nullable: true
        showDescriptionOnline nullable: true
        showDescriptionForAdmin nullable: true
        restriction nullable: false, default: Restriction.NONE
        profiles nullable: true
    }
    static mapping = {
        sort "id"
        courtPriceConditions joinTable: [name: "court_price_condition_courts", key: 'cc_court_id']
        courtSlotConditions joinTable: [name: "court_slot_condition_court", key: 'court_id']
        facility index: 'facility_offline_only_idx'
        offlineOnly index: 'facility_offline_only_idx'
        cache true
        courtTypeAttributes cascade: "all-delete-orphan"
    }

    static namedQueries = {
        available { Facility f ->
            or {
                eq "facility", f
                if (!f.memberFacilities.isEmpty()) {
                    inList "facility", f.memberFacilities
                }
            }
            eq("archived", false)
            order("listPosition", "asc")
        }
        archivable { Facility f ->
            eq("facility", f)
            eq("archived", true)
            order("listPosition", "asc")
        }
    }

    def beforeInsert() {
        if (listPosition == null) {
            def max = withCriteria(uniqueResult: true) {
                projections {
                    max("listPosition")
                }
                eq("facility", facility)
            }
            listPosition = max ? max + 1 : 1
        }
    }

    String toString() { "$name" }

    boolean hasUserAccess(User user) {
        switch (this.restriction) {
            case Restriction.OFFLINE_ONLY:
                return false
            case Restriction.MEMBERS_ONLY:
                return user?.hasActiveMembershipIn(this.facility) || user?.getMembershipIn(this.facility)?.inStartingGracePeriod
            case Restriction.REQUIREMENT_PROFILES:
                List<RequirementProfile> requirementProfiles = RequirementProfile.findAllByIdInList(requirementProfiles)
                return user ? requirementProfiles?.any { it.validate(user) } : false
            default:
                return true
        }
    }

    CourtTypeAttribute getCourtTypeAttributeByMultiplePlayersPrice() {
        courtTypeAttributes.find { it.courtTypeEnum.affectMultiplePlayersPrice } as CourtTypeAttribute
    }

    /**
     * Transient getter to support old implementation
     * @return
     */
    boolean isMembersOnly() {
        return this.restriction?.equals(Restriction.MEMBERS_ONLY)
    }

    String validateCourtTypeAttribute() {
        if (!sport) {
            return true
        }

        Set<CourtTypeEnum> missingCourtTypeAttributes = CourtTypeEnum.getBySport(sport).findAll({ CourtTypeEnum courtTypeEnum ->
            courtTypeEnum.required
        }).findAll { CourtTypeEnum requiredCourtType ->
            !courtTypeAttributes.any {CourtTypeAttribute attribute -> attribute.courtTypeEnum == requiredCourtType}
        }

        if (missingCourtTypeAttributes.isEmpty()) {
            return null
        }
        else {
            return 'error, missing values for: ' + missingCourtTypeAttributes.join(" : ")
        }
    }

    /**
     * Transient getter to support old implementation
     * @return
     */
    boolean isOfflineOnly() {
        return this.restriction?.equals(Restriction.OFFLINE_ONLY)
    }

    boolean isRestrictedByRequirementProfile() {
        return this.restriction?.equals(Restriction.REQUIREMENT_PROFILES)
    }

    enum Restriction {
        NONE, MEMBERS_ONLY, OFFLINE_ONLY, REQUIREMENT_PROFILES

        static list() {
            return [NONE, MEMBERS_ONLY, OFFLINE_ONLY, REQUIREMENT_PROFILES]
        }
    }

    boolean hasCamera() {
        return cameras.any()
    }

    static enum Surface {
        CLAY('Grus'),
        HARD('Hard court'),
        LAWN('Gräs'),
        ARTIFICIAL('Konstgräs'),
        ARTIFICIAL_CLAY('Konstgrus'),
        TARMAC('Asfalt'),
        CARPET('Filt'),
        PARQUET('Parkett'),
        SPORTS('Sportgolv'),
        GENERAL('Övrigt')

        String name

        Surface(String name) {
            this.name = name
        }

        static list() {
            return [CLAY, HARD, LAWN, ARTIFICIAL, ARTIFICIAL_CLAY, TARMAC, CARPET, PARQUET, SPORTS, GENERAL]
        }

        // ex: [CLAY, HARD]
        static toListFromString(String surfaceNames) {
            if (surfaceNames) {
                surfaceNames = surfaceNames.replaceAll(/(\[)/, '')
                surfaceNames = surfaceNames.replaceAll(/(\])/, '')

                def names = surfaceNames.split(",")
                return toList(names)
            } else {
                return []
            }
        }

        static toList(def surfaceNames) {
            return surfaceNames.collect {
                Surface.valueOf(it.trim())
            }
        }
    }
    
    def getCourtGroupNames(CourtGroup group = null) {
        List<CourtGroup> groups = CourtGroup.facilityCourtGroups(this.facility).list()
        if (group) {
            groups.remove(group)
        }
        List<String> groupsForThisCourt = groups.findAll { it.courts.collect { it.id }.contains(this.id) && it.maxNumberOfBookings }.collect { it.name }
        return groupsForThisCourt
    }

    Boolean belongsToOtherCourtGroup(CourtGroup group = null) {
        List<CourtGroup> groups = CourtGroup.facilityCourtGroups(this.facility).list()
        if (group) {
            groups.remove(group)
        }
        groups.any { it.courts.collect { it.id }.contains(this.id) && it.maxNumberOfBookings }
    }

    Boolean isType(CourtTypeEnum courtTypeEnum, String value) {
        this.courtTypeAttributes.find { it.courtTypeEnum == courtTypeEnum}.value == value
    }
}
