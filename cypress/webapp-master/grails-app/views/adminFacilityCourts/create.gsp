<%@ page import="com.matchi.Sport; com.matchi.Court; com.matchi.CourtTypeEnum"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminFacilityCourts.create.title"/></title>
</head>
<body>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li><i class="fas fa-list"></i> <g:link action="index" params="[id: courtInstance?.facility?.id]"><g:message code="court.label.plural"/></g:link><span class="divider">/</span></li>
        <li class="active"><g:message code="adminFacilityCourts.create.createCourt"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${courtInstance}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 2]"/>

        <g:form action="save" class="form panel-body">
            <g:hiddenField name="id" value="${courtInstance?.id}" />
            <g:hiddenField name="version" value="${courtInstance?.version}" />
            <g:hiddenField name="facilityId" value="${courtInstance.facility.id}"/>

            <div class="form-group col-sm-12">
                <label for="name"><g:message code="court.name.label"/>*</label>
                <g:textField name="name" value="${courtInstance?.name}" class="form-control"/>
            </div>
            <div class="form-group col-sm-12">
                <label for="description"><g:message code="court.description.label"/></label>
                <g:textArea name="description" rows="3" cols="30" value="${courtInstance?.description}" class="form-control"/>
            </div>

            <div class="form-group col-sm-6">
                <label for="sport.id"><g:message code="court.sport.label"/></label>
                <g:select from="${Sport.list()}" optionKey="id" optionValue="${ {g.message(code:'sport.name.'+it.id) } }" name="sport.id" class="form-control"/>
            </div>
            <div class="form-group col-sm-6">
                <label for="surface"><g:message code="court.surface.label"/></label>
                <g:select from="${Court.Surface}" optionValue="${ {name->g.message(code:'court.surface.'+name) } }" name="surface" class="form-control"/>
            </div>

            <g:each in="${CourtTypeEnum.findAll() as Collection<CourtTypeEnum>}" var="courtType">
                <div class="form-group col-sm-6 court-type-selector"
                     data-type="${courtType.name()}"
                     data-sports="${courtType.sportsIds.collect { "_" + it + "_" }.join("")}"
                     style="display: none;">
                    <label class="control-label" for="sport"><g:message code="court.type.${courtType.name()}"/></label>

                    <g:hiddenField name="courtTypeAttributeNames" value="${courtType.name()}" isabled="disabled"/>
                    <g:select class="form-control"
                              from="${courtType.options}"
                              optionValue="${{ g.message(code: 'court.type.' + courtType.name() + '.' + it) }}"
                              name="courtTypeAttribute"
                              value="${courtInstance.courtTypeAttributes.find { it.courtTypeEnum == courtType }?.value}"
                              disabled="disabled" />
                </div>
            </g:each>

            <div class="form-group col-sm-12">
                <label for="indoor"><g:message code="court.indoorOutdoor.label"/></label>
                <div class="checkbox">
                    <g:checkBox id="indoor" name="indoor" value="true"/>
                    <label for="indoor"><g:message code="court.indoor.label"/></label>
                </div>
            </div>

            <div class="form-group col-sm-12">
                <label for="restriction"><g:message code="adminFacilityCourts.create.bookingAvailability"/></label>
                <div class="radio">
                    <g:radio id="bookingAvailability_1" name="restriction" value="${Court.Restriction.NONE}" checked="${courtInstance.restriction == Court.Restriction.NONE}"/>
                    <label for="bookingAvailability_1"><g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.NONE}"/></label>
                </div>
                <div class="radio">
                    <g:radio id="bookingAvailability_2" name="restriction" value="${Court.Restriction.MEMBERS_ONLY}" checked="${courtInstance.restriction == Court.Restriction.MEMBERS_ONLY}"/>
                    <label for="bookingAvailability_2"><g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.MEMBERS_ONLY}"/></label>
                </div>
                <div class="radio">
                    <g:radio id="bookingAvailability_3" name="restriction" value="${Court.Restriction.OFFLINE_ONLY}" checked="${courtInstance.restriction == Court.Restriction.OFFLINE_ONLY}"/>
                    <label for="bookingAvailability_3"><g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.OFFLINE_ONLY}"/></label>
                </div>
                <g:if test="${facility.hasRequirementProfiles()}">
                    <div class="radio">
                        <g:radio id="rp-restriction" name="restriction" value="${Court.Restriction.REQUIREMENT_PROFILES}" checked="${courtInstance.restriction == Court.Restriction.REQUIREMENT_PROFILES}"/>
                        <label for="rp-restriction"><g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.REQUIREMENT_PROFILES}"/></label>
                    </div>
                    <div id="profile-selector" class="left-margin20" style="display: none;">
                        <g:if test="${requirementProfiles?.isEmpty()}">
                            <p><g:message code="adminFacilityCourts.create.restriction.no.profiles"/></p>
                            ${courtInstance.profiles}
                        </g:if>
                        <g:each in="${requirementProfiles}">
                            <div class="checkbox">
                                <g:checkBox id="req_${it.id}" name="requirementProfiles" value="${it.id}"
                                            checked="${courtInstance.requirementProfiles?.contains(it.id.toString())}"/>
                                <label for="req_${it.id}">${it.name}</label>
                            </div>
                        </g:each>
                    </div>
                </g:if>
            </div>

            <div class="form-group col-sm-12">
                <h3><g:message code="adminFacilityCourts.cameras"/></h3>
                <g:message code="adminFacilityCourts.cameras.useEditViewToAddCameras"/>
            </div>
            <div class="form-group col-sm-12"></div>

            <div class="form-controls col-sm-12">
                <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" params="[id: courtInstance.facility.id]" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </g:form>
    </div>
</div>
<r:script>
    $(document).ready(function () {
        $('[name=restriction]').on('change', function () {
            $('#profile-selector').hide();
        });

        var $rpSelector = $('#rp-restriction');
        showHideRPs($rpSelector);

        $rpSelector.on('change', function () {
            showHideRPs($(this));
        });

        $('select[name="sport.id"').on("change", function () {
            var sportId = $(this).val();
            $(".court-type-selector").hide().find("input, select").prop("disabled", true).end().filter(function () {
                return $(this).data("sports").includes("_" + sportId + "_");
            }).show().find("input, select").prop("disabled", false)
        }).trigger("change");
    });

    var showHideRPs = function ($rpSelector) {
        var $profiles = $('#profile-selector');
        if ($rpSelector.prop('checked')) {
            $profiles.show();
        } else {
            $profiles.hide();
        }
    };
</r:script>
</body>
</html>