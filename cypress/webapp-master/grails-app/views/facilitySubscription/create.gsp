<%@ page import="com.matchi.enums.BookingGroupType; com.matchi.Season; com.matchi.Court; com.matchi.Facility; org.joda.time.LocalTime; org.joda.time.DateTimeConstants; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title>${ form.facility } - <g:message code="subscription.label"/></title>
    <r:require modules="jquery-timepicker, matchi-customerselect"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="subscription.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySubscription.create.message2"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:errorMessage bean="${cmd}"/>

    <g:form name="subscriptionForm" action="confirm" method="post" class="form-horizontal form-well">
        <g:hiddenField name="customerId" value="${cmd?.customerId}"/>
        <g:hiddenField name="returnUrl" value="${params?.returnUrl}" />
        <div class="form-header">
            <g:message code="facilitySubscription.create.message2"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
        </div>
        <fieldset>
            <div class="control-group ${hasErrors(bean:cmd, field:'customerId', 'error')}">
                <label class="control-label"><g:message code="customer.search.placeholder"/></label>
                <div class="controls">
                    <input type="hidden" id="search" name="search" value="${cmd?.customerId ?: params?.customerId}"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="dateFrom"><g:message code="season.multiselect.noneSelectedText"/></label>
                <div class="controls">
                    <g:select id="season" name="season" from="${form.seasons}" optionValue="name" optionKey="id"
                              value="${cmd?.season ?: form.seasons[0].id}"/>
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
                    <p class="help-block top-margin5"><g:message code="facilitySubscription.create.message16"/></p>
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:cmd, field:'courtId', 'error')}">
                <label class="control-label" for="courtId"><g:message code="court.label"/>*</label>
                <div class="controls">
                    <g:each in="${com.matchi.Court.available(form.facility).list()}">
                        <label class="radio inline">
                            <g:radio name="courtId" id="courtId" value="${it.id}" checked="${cmd?.courtId == it.id ?: false}" /> ${it.name}
                        </label>
                    </g:each>
                </div>
            </div>
            <div class="control-group form-inline ${hasErrors(bean:cmd, field:'time', 'error')}">
                <label class="control-label" for="time"><g:message code="facilitySubscription.create.message9"/>*</label>
                <div class="controls">
                    <select class="span2 timepicker center-text" value="${cmd?.time ?: ''}" name="time" id="time"></select>
                    <p class="help-block"><g:message code="createSubscriptionCommand.time.hint"/></p>
                </div>
            </div>
            <div class="control-group form-inline ${hasErrors(bean:cmd, field:'interval', 'error')}">
                <label class="control-label" for="interval"><g:message code="facilitySubscription.create.message11"/></label>
                <div class="controls">
                    <select id="interval" name="interval" class="span1">
                        <g:each in="${1..7}">
                            <option value="${it}">${it}</option>
                        </g:each>
                    </select>
                    <g:message code="bookingGroupFrequency.interval.WEEKLY" />
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:cmd, field:'description', 'error')}">
                <label class="control-label" for="description"><g:message code="subscription.description.label"/></label>
                <div class="controls">
                    <g:textArea rows="3" cols="50" id="description" name="description" value="${cmd?.description}" class="span8"/>
                    <div class="space5">&nbsp;</div>
                    <label class="checkbox" style="display: ${!cmd?.showComment ? "none":""}">
                        <g:checkBox name="showComment" checked="${cmd?.showComment}"/><g:message code="facilitySubscription.create.message17"/>
                    </label>
                </div>
            </div>
            <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.name()}">
                <div class="control-group ${hasErrors(bean:cmd, field:'accessCode', 'error')}">
                    <label class="control-label" for="accessCode">
                        <g:message code="subscription.accessCode.label"/>
                    </label>
                    <div class="controls">
                        <g:textField name="accessCode" value="${cmd?.accessCode}" class="span3"/>
                    </div>
                </div>
            </g:ifFacilityPropertyEnabled>
            <div class="form-actions">
                <g:submitButton name="submit" value="${message(code: 'button.next.label')}" class="btn btn-success" show-loader="${message(code: 'default.loader.label')}"/>
                <g:if test="${params.returnUrl}">
                    <g:link url="${params.returnUrl}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </g:if>
                <g:else>
                    <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </g:else>
            </div>
        </fieldset>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

<r:script>
    var seasons = new Array();
    var $dateFrom;
    var $dateTo;
    var $timePicker;
    var $courts;

    var $comments = $("#description");
    var $showComment = $("#showComment");

    var $search = $('#search');

    $(document).ready(function() {
        $dateFrom = $("#showDateFrom");
        $dateTo = $("#showDateTo");
        $timePicker = $('.timepicker');
        $courts = $("input:radio");

        $search.matchiCustomerSelect({width:'250px', onchange: onCustomerSelectChange});

        <g:each in="${form.seasons}" var="season" status="i">
            seasons[${g.forJavaScript(data: i)}] = {
                id: ${g.forJavaScript(data: season.id)}, name: "${g.forJavaScript(data: season.name)}",
                start: "${g.forJavaScript(data: new DateTime(season.startTime).toString('yyyy-MM-dd'))}",
                end: "${g.forJavaScript(data: new DateTime(season.endTime).toString('yyyy-MM-dd'))}"
            }
        </g:each>

        <g:if test="${cmd?.courtId}">
            setTimePickerRange();
        </g:if>

        $('#season').on('change', function() {
            var season = getSeason($(this).val());

            $dateFrom.datepicker("setDate", $.datepicker.formatDate('<g:message code="date.format.dateOnly.small"/>', new Date(season.start)) );
            $dateTo.datepicker("setDate", $.datepicker.formatDate('<g:message code="date.format.dateOnly.small"/>', new Date(season.end)) );
        });

        $courts.change(function() {
            setTimePickerRange();
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

                setTimePickerRange();
            }
        });

        $dateTo.datepicker({
            beforeShow: customRange,
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>',
            altField: '#dateTo',
            altFormat: 'yy-mm-dd'
        });

        $comments.on("keyup", function() {
            var val = $(this).val();
            var showCommentVisible = $showComment.is(":visible");

            if(!val && showCommentVisible) {
                $showComment.parent().slideUp();
                $showComment.attr("checked", false);
            } else if(!showCommentVisible) {
                $showComment.parent().slideDown();
            }
        });
    });

    function setTimePickerRange() {
        $.ajax({
            url: "<g:createLink controller="autoCompleteSupport" action="courtHours"/>",
            dataType: "json",
            data: {
                courtId: $('#courtId:checked').val(),
                date: $('#dateFrom').val()
            },
            success: function( data ) {
                $timePicker.empty()
                $.each(data, function(key, value) {
                     $timePicker
                         .append($("<option></option>")
                         .attr("value",value)
                         .text(value));
                });
            }
        });
    }

    function onCustomerSelectChange(customer) {
        $("#customerId").val(customer.id);
        $("#email").text(customer.email);
    }

    function customRange(input) {
        var dateMin = new Date(2008, 11 - 1, 1); //Set this to your absolute minimum date;;
        var dateMax = '';
        var currentSeason = getSeason($('#season').val());

        if (input.id == "showDateFrom") {
            if(${form.seasons != null}) {
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
</r:script>
</body>
</html>
