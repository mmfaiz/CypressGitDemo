<%@ page import="com.matchi.Sport; com.matchi.Court; com.matchi.Camera; com.matchi.CourtTypeEnum"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>${ facility } - <g:message code="court.label.plural"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li><i class="fas fa-list"></i> <g:link action="index" params="[id: courtInstance?.facility?.id]"><g:message code="court.label.plural"/></g:link><span class="divider">/</span></li>
        <li class="active"><g:message code="adminFacilityCourts.edit.editCourt"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${courtInstance}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 2]"/>

        <g:form action="update" class="form panel-body">
            <g:hiddenField name="id" value="${courtInstance?.id}" />
            <g:hiddenField name="version" value="${courtInstance?.version}" />

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
                <g:select from="${Sport.list()}" optionKey="id" optionValue="${ {g.message(code:'sport.name.'+it.id) } }" name="sport.id" class="form-control" value="${courtInstance.sport.id}"/>
            </div>
            <div class="form-group col-sm-6">
                <label for="surface"><g:message code="court.surface.label"/></label>
                <g:select from="${Court.Surface}" optionValue="${ {name->g.message(code:'court.surface.'+name) } }" name="surface" class="form-control" value="${courtInstance.surface}"/>
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
                    <g:checkBox name="indoor" value="true"/>
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
                <p class="help-block"><g:message code="adminFacilityCourts.create.bookingAvailability.note"/></p>
            </div>

            <div class="form-group col-sm-6">
                <label for="parentCourtId"><g:message code="adminFacilityCourts.edit.parentCourtId"/></label>
                <g:select noSelection="['':'None']" from="${parents}" optionKey="id" class="form-control"
                          optionValue="name" name="parentCourtId" value="${courtInstance.parent?.id}"/>
            </div>

            <div class="form-group col-sm-12">
                <label for="externalScheduling"><g:message code="adminFacilityCourts.edit.externalScheduling"/></label>
                <div class="checkbox">
                    <g:checkBox name="externalScheduling" value="${courtInstance?.externalScheduling}"/>
                    <label for="externalScheduling"><g:message code="adminFacilityCourts.edit.externalScheduling"/></label>
                </div>

                <label for="name"><g:message code="adminFacilityCourts.edit.externalId"/></label>
                <g:textField name="externalId" value="${courtInstance?.externalId}" class="form-control"/>
            </div>

            <div class="form-group col-sm-12">
                <label for="archived"><g:message code="court.archived.label"/></label>
                <div class="checkbox">
                    <g:checkBox name="archived" value="${courtInstance?.archived}"/>
                    <label for="archived"><g:message code="court.archived.label"/></label>
                </div>
            </div>

            <div class="form-group col-sm-12">
                <h3><g:message code="adminFacilityCourts.cameras"/></h3>

                <table id="camerasTable" class="table table-striped table-bordered">
                    <g:hiddenField name="cameraTableMaxIndex" value="${cameras?.size() ?: 0}"/>
                    <thead>
                    <tr>
                        <th><g:message code="adminFacilityCourts.cameras.name"/><br/><small><g:message code="adminFacilityCourts.cameras.name.description"/></small></th>
                        <th><g:message code="adminFacilityCourts.cameras.cameraId"/><br/><small><g:message code="adminFacilityCourts.cameras.cameraId.description"/></small></th>
                        <th><g:message code="adminFacilityCourts.cameras.provider"/><br/><small><g:message code="adminFacilityCourts.cameras.provider.description"/></small></th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${cameras}" var="camera" status="idx">
                        <tr>
                            <td><input type="text" name="cameraName_${idx}" value="${camera.name}" required pattern=".*\S+.*" title="This field is required"/></td>
                            <td><input type="number" name="cameraExternalId_${idx}" value="${camera.cameraId}" required pattern=".*\S+.*" title="This field is required"/></td>
                            <td><g:select from="${Camera.CameraProvider}" name="cameraProvider_${idx}" class="form-control" value="${camera.cameraProvider.name()}"/></td>
                            <td class="text-center"><a href="javascript:void(0)" onclick="removeCamera($(this))"><g:message code="adminFacilityCourts.cameras.remove"/></a></td>
                        </tr>
                    </g:each>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="3">
                            <a href="javascript:void(0)" onclick="addCamera()" class="btn btn-sm btn-default"><i class="fa fa-plus"></i> <g:message code="adminFacilityCourts.cameras.addCamera"/></a>
                        </td>
                    </tr>
                    </tfoot>
                </table>

            </div>

            <div class="form-group col-sm-12"> </div>

            <div class="form-controls col-sm-12">
                <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:actionSubmit onclick="return confirm('${message(code: 'adminFacilityCourts.edit.delete.confirm')}')"
                                action="delete" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
                <g:link action="index" params="[facilityId: courtInstance.facility.id]" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </g:form>
    </div>
</div>

<!-- Camera editing functionality. Cloned dynamically into camera table when pressing Add camera button. -->
<input id="cameraNameField" type="text" style="display: none;" required pattern=".*\S+.*" title="This field is required"/>
<input id="cameraIdField" type="number" style="display: none;" required pattern=".*\S+.*" title="This field is required"/>
<g:select from="${Camera.CameraProvider}" id="cameraProviderField" name="provider" class="form-control"/>
<a id="cameraRemoveButton" href="javascript:void(0)" onclick="removeCamera($(this))" style="display: none;"><g:message code="adminFacilityCourts.cameras.remove"/></a>

<r:script>

    var $cameraNameField, $cameraIdField, $cameraProviderField, $cameraRemoveButton, $cameraTableMaxIndex, $camerasTableBody;

    $(document).ready(function () {
        $('[name=restriction]').on('change', function () {
            $('#profile-selector').hide();
        });

        var $rpSelector = $('#rp-restriction');
        showHideRPs($rpSelector);

        $rpSelector.on('change', function () {
            showHideRPs($(this));
        });

        $cameraNameField  = $('#cameraNameField');
        $cameraIdField  = $('#cameraIdField');
        $cameraProviderField = $('#cameraProviderField');
        $cameraRemoveButton     = $('#removeBtn');
        $cameraTableMaxIndex      = $('#cameraTableMaxIndex');
        $camerasTableBody  = $('#camerasTable').find('tbody');

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

    // Add/remove camera editing functionality.
    var addCamera = function() {
        var indexInTable   = getNewCameraTableIndex();
        var cameraNameProperty     = "cameraName_" + indexInTable;
        var cameraIdProperty   = "cameraExternalId_" + indexInTable;
        var cameraProviderProperty   = "cameraProvider_" + indexInTable;
        var $cameraNameToAdd   = $cameraNameField.clone();
        var $cameraIdToAdd = $cameraIdField.clone();
        var $cameraProviderToAdd = $cameraProviderField.clone();

        $cameraNameToAdd.prop('id', cameraNameProperty); $cameraNameToAdd.prop('name', cameraNameProperty);
        $cameraIdToAdd.prop('id', cameraIdProperty); $cameraIdToAdd.prop('name', cameraIdProperty);
        $cameraProviderToAdd.prop('id', cameraProviderProperty); $cameraProviderToAdd.prop('name', cameraProviderProperty);

        addToCameraTable($cameraNameToAdd, $cameraIdToAdd, $cameraProviderToAdd, indexInTable);
    };

    var removeCamera = function(elem) {
        $(elem).closest('tr').remove();
    };

    var addToCameraTable = function (cameraNameElement, cameraIdElement, cameraProviderElement, indexInTable) {
        $camerasTableBody.append('<tr><td></td><td></td><td class="text-center"></td></tr>');
        $camerasTableBody.find('tr').last().find('td').eq(0).append(cameraNameElement.css('display', 'block'));
        $camerasTableBody.find('tr').last().find('td').eq(1).append(cameraIdElement.css('display', 'block'));
        $camerasTableBody.find('tr').last().find('td').eq(2).append(cameraProviderElement.css('display', 'block'));
        $camerasTableBody.find('tr').last().find('td').eq(3).append($cameraRemoveButton.css('display', 'block'));
        $cameraTableMaxIndex.val(parseInt(indexInTable));
    };

    var getNewCameraTableIndex = function() {
        return parseInt($cameraTableMaxIndex.val()) + 1;
    };
</r:script>
</body>
</html>
