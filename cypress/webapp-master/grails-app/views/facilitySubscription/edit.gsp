<%@ page import="com.matchi.Subscription; org.joda.time.DateTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title>${ form.facility } - <g:message code="subscription.label"/></title>
    <link rel="stylesheet" href="${resource(dir:'js/jquery.timepicker',file:'jquery.ui.timepicker.css')}" />
    <r:require modules="jquery-timepicker"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="subscription.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySubscription.edit.heading"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form name="subscriptionForm" action="confirm" method="post" class="form-horizontal form-well" >
        <g:hiddenField name="id" value="${cmd?.id}" />
        <g:hiddenField name="customerId" value="${cmd?.customerId}" />
        <g:hiddenField name="season" value="${cmd?.season}" />
        <g:hiddenField name="returnUrl" value="${params?.returnUrl}" />
        <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.name()}">
            <g:hiddenField name="accessCode" value="${cmd?.accessCode}" />
        </g:ifFacilityPropertyEnabled>
        <div class="form-header">
            <g:message code="facilitySubscription.edit.heading"/><span class="ingress"><g:message code="facilitySubscription.edit.message3"/></span>
        </div>
        <fieldset>
            <div class="control-group form-inline">
                <label class="control-label"><g:message code="customer.label"/>:</label>
                <div class="controls strong-inline">
                    <strong>${subscription.customer.number} - ${subscription.customer.fullName()}</strong>
                </div>
            </div>
            <div class="control-group form-inline">
                <label class="control-label"><g:message code="default.day.label"/>:</label>
                <div class="controls strong-inline">
                    <strong><g:message code="time.weekDay.plural.${subscription.weekday}" /></strong>
                </div>
            </div>
            <div class="control-group form-inline ${hasErrors(bean:cmd, field:'dateFrom', 'error')} ${hasErrors(bean:cmd, field:'dateTo', 'error')}">
                <label class="control-label" for="dateFrom"><g:message code="createSubscriptionCommand.dateFrom.label"/>*</label>
                <div class="controls controls-row">
                    <input class="span2 center-text" readonly="true" type="text" name="showDateFrom" id="showDateFrom"
                           value="${cmd?.dateFrom ? new DateTime(cmd?.dateFrom).toString("${message(code: 'date.format.dateOnly')}") :
                                   new DateTime().toString("${message(code: 'date.format.dateOnly')}")}"/>
                    <g:hiddenField name="dateFrom" id="dateFrom" value="${cmd?.dateFrom ?: new DateTime().toString('yyyy-MM-dd')}"/>
                    <label class="span1 control-label" for="dateTo" style="width: 10px;"><g:message code="default.to.label"/>*</label>
                    <input class="span2 center-text" readonly="true" type="text" name="showDateTo" id="showDateTo"
                           value="${cmd?.dateTo ? new DateTime(cmd?.dateTo).toString("${message(code: 'date.format.dateOnly')}") :
                                   form.seasons[0] != null ? new DateTime(form.seasons[0].endTime).toString("${message(code: 'date.format.dateOnly')}") :
                                           new DateTime().toString("${message(code: 'date.format.dateOnly')}") }" />
                    <g:hiddenField name="dateTo" id="dateTo"
                                   value="${cmd?.dateTo ?: form.seasons[0] != null ? new DateTime(form.seasons[0].endTime).toString('yyyy-MM-dd') :
                                           new DateTime().toString('yyyy-MM-dd') }" />
                    <div class="clearfix"></div>
                    <p class="help-block top-margin5"><g:message code="facilitySubscription.edit.message8"/></p>
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:cmd, field:'courtId', 'error')}">
                <label class="control-label" for="courtId"><g:message code="court.label"/>*</label>
                <div class="controls">
                    <g:each in="${com.matchi.Court.available(form.facility).list()}">
                        <label class="radio inline">
                            <g:radio name="courtId" id="courtId" value="${it.id}" checked="${cmd?.courtId == it.id ?: false}" />
                            ${it.name}
                        </label>
                    </g:each>
                </div>
            </div>
            <div class="control-group form-inline ${hasErrors(bean:subscription, field:'time', 'error')}">
                <label class="control-label" for="time"><g:message code="facilitySubscription.edit.message10"/>*</label>
                <div class="controls">
                    <select class="span2 timepicker center-text" value="${subscription?.time ? subscription.time.toString("HH:mm"):''}" name="time" id="time"></select>
                    <p class="help-block"><g:message code="createSubscriptionCommand.time.hint"/></p>
                </div>
            </div>
            <div class="control-group form-inline ${hasErrors(bean:cmd, field:'interval', 'error')}">
                <label class="control-label" for="interval"><g:message code="facilitySubscription.edit.message12"/></label>
                <div class="controls">
                    <select id="interval" name="interval" class="span1">
                        <g:each in="${1..7}">
                            <option value="${it}" ${subscription.timeInterval == it ? "selected" : ""}>${it}</option>
                        </g:each>
                    </select>
                    <g:message code="bookingGroupFrequency.interval.WEEKLY" />
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="description"><g:message code="subscription.description.label"/></label>
                <div class="controls">
                    <g:textArea rows="3" cols="50" name="description" value="${subscription?.description}" class="span8"/>
                    <div class="space5">&nbsp;</div>
                    <label class="checkbox" style="display: ${!subscription?.description ? "none":""}">
                        <g:checkBox name="showComment" checked="${subscription?.showComment}"/><g:message code="facilitySubscription.edit.message22"/>
                    </label>
                </div>
            </div>
            <div class="form-actions">
                <g:submitButton name="submit" value="${message(code: 'button.next.label')}" class="btn btn-success" show-loader="${message(code: 'default.loader.label')}"/>
                <g:actionSubmit action="cancel" class="btn btn-inverse" value="${message(code: 'button.delete.label')}" onclick="return confirm('${message(code: 'facilitySubscription.edit.message23')}');"/>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <g:link class="btn btn-inverse pull-right" action="updatePrice" params="[id: subscription.id]"><g:message code="facility.subscription.bulkUpdate"/></g:link>
                </sec:ifAnyGranted>
                <g:if test="${params.returnUrl}">
                    <g:link url="${params.returnUrl}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </g:if>
                <g:else>
                    <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </g:else>
            </div>
        </fieldset>
    </g:form>

    <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.name()}">
        <g:form name="accessCodeForm" action="updateAccessCode" method="post" class="form-horizontal form-well" >
            <g:hiddenField name="id" value="${cmd?.id}" />
            <div class="form-header">
                <g:message code="facilitySubscription.edit.accessCode.heading"/><span class="ingress"><g:message code="facilitySubscription.edit.accessCode.instructions"/></span>
            </div>
            <fieldset>
                <div class="control-group ${hasErrors(bean:cmd, field:'accessCode', 'error')}">
                    <label class="control-label" for="accessCode">
                        <g:message code="subscription.accessCode.label"/>
                    </label>
                    <div class="controls">
                        <g:textField name="accessCode" value="${cmd?.accessCode}" class="span3"/>
                    </div>
                </div>
                <div class="form-actions">
                    <g:submitButton name="submit" value="${message(code: 'facilitySubscription.edit.accessCode.submit')}" class="btn btn-success" show-loader="${message(code: 'default.loader.label')}"/>
                    <g:if test="${cmd?.accessCode?.size() > 0}">
                        <g:actionSubmit action="removeAccessCode" class="btn btn-inverse" value="${message(code: 'button.delete.label')}" onclick="return confirm('${message(code: 'facilitySubscription.edit.removeAccessCodeConfirm')}');"/>
                    </g:if>
                </div>
            </fieldset>
        </g:form>
    </g:ifFacilityPropertyEnabled>

    <h4><g:message code="facilitySubscription.edit.message24" args="[subscription.slots.size()]"/></h4>
    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <th><g:message code="customer.label"/></th>
            <th><g:message code="default.date.label"/></th>
            <th><g:message code="default.date.time"/></th>
            <th><g:message code="court.label"/></th>
            <th class="center-text"><g:message code="facilitySubscription.edit.message20"/></th>
        </tr>
        </thead>

        <g:each in="${subscription.slots}" var="slot">
            <tr>
                <td>${slot.booking?.customer ? slot.booking.customer.number+" - "+slot.booking.customer.fullName() : message(code: 'facilitySubscription.edit.message25')}</td>
                <td><g:formatDate date="${slot.startTime}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${slot.startTime}" format="HH:mm"/>-<g:formatDate date="${slot.endTime}" format="HH:mm"/></td>
                <td>${slot.court.name}</td>
                <td class="center-text">
                    <g:if test="${slot.booking != null}">
                        <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                      onFailure="handleAjaxError()" onSuccess="showLayer()"
                                      params="['cancelSlotsData': slot.id,
                                              'returnUrl': g.createLink(absolute: true, action: 'edit', id:subscription.id)
                                      ]" class="right-margin10"><i class="icon-remove"></i> <g:message code="button.unbook.label"/></g:remoteLink>
                    </g:if>
                    <g:if test="${slot.booking && slot.booking.customer == subscription.customer}">
                        <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                onFailure="handleAjaxError()" onSuccess="showLayer()"
                                before="if (!confirm('${message(code: 'facilitySubscription.edit.cancelDeleteConfirm')}')) return false"
                                params="[cancelSlotsData: slot.id,
                                        returnUrl: createLink(absolute: true, action: 'removeSlot', id: subscription.id, params: [slotId: slot.id])
                                ]"><i class="icon-trash"></i> <g:message code="button.delete.label"/></g:remoteLink>
                    </g:if>
                    <g:else>
                        <g:link action="removeSlot" id="${subscription.id}" params="[slotId: slot.id]"
                                onclick="return confirm('${message(code: 'default.confirm')}')">
                            <i class="icon-trash"></i> <g:message code="button.delete.label"/>
                        </g:link>
                    </g:else>
                </td>
            </tr>
        </g:each>

    </table>

    <div id="bookingModal" class="modal hide fade"></div>
    <g:javascript>
        var seasons = new Array();
        var $dateFrom;
        var $dateTo;
        var $timePicker;
        var $courts;

        var $comments = $("#description");
        var $showComment = $("#showComment");

        $(document).ready(function() {
            $('#bookingModal').modal({show:false});

            $dateFrom = $("#showDateFrom");
            $dateTo = $("#showDateTo");
            $timePicker = $('.timepicker');
            $courts = $("input:radio");

            $timePicker.append($("<option></option>")
                       .attr("value","${g.forJavaScript(data: subscription.time.toString("HH:mm"))}")
                       .text("${g.forJavaScript(data: subscription.time.toString("HH:mm"))}"));

            <g:each in="${form.seasons}" var="season" status="i">
                seasons[${g.forJavaScript(data: i)}] = {
                id: ${g.forJavaScript(data: season.id)}, name: "${g.forJavaScript(data: season.name)}",
                start: "${g.forJavaScript(data: new DateTime(season.startTime).toString('yyyy-MM-dd'))}",
                end: "${g.forJavaScript(data: new DateTime(season.endTime).toString('yyyy-MM-dd'))}" }
            </g:each>

            $courts.change(function() {
                setTimePickerRange({courtId: $('#courtId:checked').val()});
            });

            $dateFrom.datepicker({
                beforeShow: customRange,
                autoSize: true,
                dateFormat: '<g:message code="date.format.dateOnly.small"/>',
                altField: '#dateFrom',
                altFormat: 'yy-mm-dd',
                onClose: function() {
                    var dateFrom = $(this).datepicker("getDate");
                    var $dateToPicker = $dateTo;

                    if( $dateTo.datepicker("getDate") != null && $dateTo.datepicker("getDate") < dateFrom) {
                        $dateTo.datepicker("setDate", dateFrom);
                    }

                    $dateToPicker.datepicker("option", "minDate", dateFrom);
                }
            });

            $dateTo.datepicker({
                beforeShow: customRange,
                autoSize: true,
                dateFormat: '<g:message code="date.format.dateOnly.small"/>',
                altField: '#dateTo',
                altFormat: 'yy-mm-dd'
            });

            setTimePickerRange({courtId: "${g.forJavaScript(data: cmd?.courtId)}", default: "${g.forJavaScript(data: subscription.time.toString("HH:mm"))}"});
        });

        function setTimePickerRange(_obj) {
            $.ajax({
                url: "<g:createLink controller="autoCompleteSupport" action="courtHours"/>",
                dataType: "json",
                data: {
                    courtId: _obj.courtId,
                    date: $dateFrom.val()
                },
                success: function( data ) {
                    $timePicker.empty()
                    $.each(data, function(key, value) {
                         $timePicker
                             .append($("<option></option>")
                             .attr("value",value)
                             .text(value));
                    });

                    if(_obj && _obj.default) {
                        $timePicker.val(_obj.default);
                    }
                }
            });
        }

        function customRange(input) {
            var dateMin = new Date(2008, 11 - 1, 1); //Set this to your absolute minimum date;;
            var dateMax = '';
            var currentSeason = getSeason("${g.forJavaScript(data: cmd.season)}");

            if (input.id == "showDateFrom") {
                if(${g.forJavaScript(data: form.seasons != null)}) {
                    dateMin = new Date(currentSeason.start);
                    dateMax = new Date(currentSeason.end);
                }
            }

            if (input.id == "showDateTo") {
                if ($dateTo.datepicker("getDate") != null) {
                    dateMin = $dateFrom.datepicker("getDate");
                }

                if(${g.forJavaScript(data: form.seasons != null)}) {
                    dateMax = new Date(currentSeason.end);
                }
            }
            return {
                minDate: dateMin,
                maxDate: dateMax
            };
        }

        function getSeason(id) {
            for(var i=0;i < seasons.length;i++) {
                if(seasons[i].id == id) {
                    return seasons[i];
                }
            }
            return null
        }
    </g:javascript>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

</body>
</html>
