<%@ page import="com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="court.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="court.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="adminFacilityCourts.edit.editCourt"/></li>
</ul>
<g:errorMessage bean="${courtInstance}"/>

<g:form action="update" class="form-horizontal form-well" name="facilityCourtEditFrm">
    <g:hiddenField name="id" value="${courtInstance?.id}" />
    <g:hiddenField name="version" value="${courtInstance?.version}" />
    <div class="form-header">
        <g:message code="facilityCourts.edit.message8"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <g:render template="form"/>
        <div class="control-group">
            <label class="control-label" for="restriction"><g:message code="adminFacilityCourts.create.bookingAvailability"/></label>
            <div class="controls">
                <label class="radio">
                    <g:radio name="restriction" value="${Court.Restriction.NONE}" checked="${courtInstance.restriction == Court.Restriction.NONE}" class="styled"/>
                    <g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.NONE}"/>
                </label>
                <label class="radio">
                    <g:radio name="restriction" value="${Court.Restriction.MEMBERS_ONLY}" checked="${courtInstance.restriction == Court.Restriction.MEMBERS_ONLY}" class="styled"/>
                    <g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.MEMBERS_ONLY}"/>
                </label>
                <label class="radio">
                    <g:radio name="restriction" value="${Court.Restriction.OFFLINE_ONLY}" checked="${courtInstance.restriction == Court.Restriction.OFFLINE_ONLY}" class="styled"/>
                    <g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.OFFLINE_ONLY}"/>
                </label>

                <g:if test="${courtInstance?.facility?.hasRequirementProfiles()}">
                    <label class="radio">
                        <g:radio id="rp-restriction" name="restriction" value="${Court.Restriction.REQUIREMENT_PROFILES}" checked="${courtInstance.restriction == Court.Restriction.REQUIREMENT_PROFILES}" class="styled"/>
                        <g:message code="adminFacilityCourts.create.restriction.${Court.Restriction.REQUIREMENT_PROFILES}"/>
                    </label>
                    <div id="profile-selector" class="left-margin20" style="display: none;">
                        <g:if test="${requirementProfiles?.isEmpty()}">
                            <p><g:message code="adminFacilityCourts.create.restriction.no.profiles"/></p>
                            ${courtInstance.profiles}
                        </g:if>
                        <g:each in="${requirementProfiles}">
                            <label class="checkbox">
                                <g:checkBox name="requirementProfiles" value="${it.id}"
                                            checked="${courtInstance.requirementProfiles?.contains(it.id.toString())}"/>
                                ${it.name}
                            </label>
                        </g:each>
                    </div>
                </g:if>

                <p class="help-block"><g:message code="adminFacilityCourts.create.bookingAvailability.note"/></p>
            </div>
        </div>
        <div class="form-actions">
            <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:if test="${courtInstance.archived}">
                <g:actionSubmit onclick="return confirm('${message(code: 'facilityCourts.archive.confirm')}')"
                                action="getFromArchive" name="btnSumbit" value="${message(code: 'reactivate.label')}" class="btn btn-info"/>
            </g:if>
            <g:else>
                <g:actionSubmit onclick="return confirm('${message(code: 'facilityCourts.archive.confirm')}')"
                                action="putToArchive" name="btnSumbit" value="${message(code: 'button.archive.label')}" class="btn btn-info"/>
            </g:else>
            <g:actionSubmit onclick="return confirm('${message(code: 'adminFacilityCourts.edit.delete.confirm')}')"
                            action="delete" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
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

        $("#facilityCourtEditFrm").preventDoubleSubmission({});
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
