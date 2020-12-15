<%@ page import="org.joda.time.LocalTime; com.matchi.Season; org.joda.time.DateTimeConstants; com.matchi.Court; com.matchi.Sport; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="season.label"/></title>
    <r:require modules="jquery-validate,jquery-timepicker"/>
</head>
<body>

<g:errorMessage bean="${seasonInstance}"/>
<g:errorMessage bean="${cmd}"/>
<div id="errorMessage" class="alert alert-error" style="display: none;">
    <a class="close" data-dismiss="alert" href="#">×</a>

    <h4 class="alert-heading"><g:message code="default.error.heading"/></h4>
    <g:renderErrors bean="${bean}" as="list" />
</div>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="season.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySeason.create.message4"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
<g:form id="seasonForm" name="seasonForm" action="save" method="post" class="form-horizontal form-well">
    <div class="form-header">
        <g:message code="facilitySeason.create.message4"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="name"><g:message code="season.name.label2"/>*<br><g:message code="season.name.example"/></label>
            <div class="controls">
                <g:textField name="name" value="${cmd?.name}" tabindex="1" class="span8"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="startTime"><g:message code="season.startTime.label"/>*</label>
            <div class="controls controls-row">
                <input class="span2 center-text" readonly="true" type="text" name="showStartTime" id="showStartTime"
                       value="${params["showStartTime"] ?: formatDate(format: message(code: "date.format.dateOnly"), date: form.nextStartDate)}"/>
                <g:hiddenField name="startTime" id="startTime" value="${params["endTime"] ?: formatDate(format: 'yyyy-MM-dd', date: form.nextStartDate)}"/>
                <label class="span1 control-label" for="endTime" style="width: 10px;"><g:message code="default.to.label"/>*</label>
                <input class="span2 center-text" readonly="true" type="text" name="showEndTime" id="showEndTime"
                       value="${params["showEndTime"] ?: formatDate(format: message(code: "date.format.dateOnly"), date: form.nextEndDate)}" />
                <g:hiddenField name="endTime" id="endTime" value="${params["endTime"] ?: formatDate(format: 'yyyy-MM-dd', date: form.nextEndDate)}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="description"><g:message code="season.description.label"/></label>
            <div class="controls">
                <g:textArea rows="3" cols="50" name="description" value="${cmd?.description}" class="span8"/>
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label style="font-size: 20px;"><g:message code="facilitySeason.create.message9"/></label>
            <table class="weekdays" width="900" class="table table-bordered">
                <thead>
                <tr>
                    <th width="180"></th>
                    <g:each in="${form.weekDays}">
                        <th style="text-align: left;"><g:message code="time.shortWeekDay.${it}"/></th>
                    </g:each>
                </tr>
                </thead>
                <g:each in="${Sport.list()}" var="sport">
                    <%
                        def courtCriteria = Court.createCriteria()
                        def courts = courtCriteria {
                            eq("facility", form.facility)
                            eq("sport", sport)
                            eq("externalScheduling", false)
                            eq("archived", false)
                        }

                        def startHour = new DateTime().withHourOfDay(form.openingHour).withMinuteOfHour(0).toString("HH:mm")
                        def endHour = new DateTime().withHourOfDay(form.closingHour).withMinuteOfHour(0).toString("HH:mm")
                    %>
                    <g:if test="${courts}">
                        <tr>
                            <td colspan="8">
                                <span>
                                    <big><strong>
                                        <a id="${sport.id}" onclick="toggleShowCourtsField(${sport.id})">
                                            <g:message code="sport.name.${sport.id}"/> (${courts.size()} ${courts.size()==1 ? message(code: 'facilitySeason.create.message17') : message(code: 'facilitySeason.create.message18')})</a> <i id="toggleMarker_${sport.id}" class="icon-chevron-right"></i></strong></big>
                                    <input type="hidden" class="showCourts" data-sportid="${sport.id}" id="showCourts_${sport.id}" name="showCourts/${sport.id}" value="${params["showCourts/${sport.id}"] ?: "hide"}" />
                                </span>
                                - <g:message code="facilitySeason.create.manage.all"/>
                            </td>
                        </tr>
                        <tr class="${sport.id}_courts">
                            <td>
                                <div class="padding10">
                                    <label for="_courtinfo"><g:message code="facilitySeason.create.message10"/></label>
                                    <g:textField readonly="true" name="_courtinfo/${sport.id}/1" class="timepicker _courtinfo courtlength span1 all-courts" id="${'1/' + sport.id}" value="${params["_courtinfo/${sport.id}/1"] ?: "01:00"}"/><br><br>
                                    <label for="_courtinfo"><g:message code="facilitySeason.create.message11"/></label>
                                    <g:textField readonly="true" name="_courtinfo/${sport.id}/2" class="timepicker _courtinfo courtinterval span1 all-courts" id="${'2/' + sport.id}" value="${params["_courtinfo/${sport.id}/2"] ?: "00:00"}"/>
                                </div>
                            </td>
                            <g:each in="${form.weekDays}">
                                <td>
                                    <div>
                                        <label for="_sports"><g:message code="default.from.label"/></label>
                                        <g:textField readonly="true" name="_sports/${sport.id}/1/${it}" class="timepicker _sports sport span1 all-courts" id="${sport.id + '/' + it +'/1'}" value="${params["_sports/${sport.id}/1/${it}"] ?: startHour}"/><br><br>
                                        <label for="_sports"><g:message code="facilitySeason.create.message13"/></label>
                                        <g:textField readonly="true" name="_sports/${sport.id}/2/${it}" class="timepicker _sports sport span1 all-courts" id="${sport.id + '/' + it +'/2'}" value="${params["_sports/${sport.id}/2/${it}"] ?: endHour}"/>
                                    </div>
                                </td>
                            </g:each>
                        </tr>
                        <tr class="${sport.id}_courts individual" style="display: none;" bgcolor="#CCCCCC">
                            <td colspan="8" style="padding-left: 10px; margin-bottom: 10px; line-height: 30px;">
                                <strong><g:message code="facilitySeason.create.manage.individual"/></strong>
                            </td>
                        </tr>
                        <g:each in="${courts}" var="court" status="i">
                            <tr class="${sport.id}_courts individual" style="display: none;" bgcolor="${i%2 == 0 ? '#EEEEEE' : ''}">
                                <td colspan="8" style="padding: 10px 0 0 10px;">
                                    <strong>${court.name}</strong>
                                </td>
                            </tr>
                            <tr class="${sport.id}_courts individual" style="display: none;" bgcolor="${i%2 == 0 ? '#EEEEEE' : ''}">
                                <td>
                                    <div class="courtrow" style="padding: 10px 0 0 10px;">
                                        <g:textField readonly="true" name="_bookingLength/${court.id}" class="timepicker _courtinfo span1"
                                                     id="${court.id + '/1/' + sport.id + '/length'}" value="${params["_bookingLength/${court.id}"] ?: "01:00"}"/><br><br>
                                        <g:textField readonly="true" name="_timeBetween/${court.id}" class="timepicker _courtinfo span1"
                                                     id="${court.id + '/2/' + sport.id + '/interval'}" value="${params["_timeBetween/${court.id}"] ?: "00:00"}"/>
                                    </div>
                                </td>
                                <g:each in="${form.weekDays}">
                                    <td>
                                        <div class="courtrow vertical-padding10">
                                            <g:textField readonly="true" name="_courts/${court.id + '/' + it + '/1'}" class="timepicker _courts span1"
                                                         id="${'court_' + sport.id + '/' + it + '/1' + court.id}" value="${params["_courts/${court.id + '/' + it + '/1'}"] ?: startHour}"/><br><br>
                                            <g:textField readonly="true" name="_courts/${court.id + '/' + it + '/2'}" class="timepicker _courts span1"
                                                         id="${'court_' + sport.id + '/' + it + '/2' + court.id}" value="${params["_courts/${court.id + '/' + it + '/2'}"] ?: endHour}"/>
                                        </div>
                                    </td>
                                </g:each>
                            </tr>
                        </g:each>
                    </g:if>
                </g:each>
            </table>
        </div>
        <div class="form-actions">
            <g:submitButton name="save" id="btnSubmit" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

<r:style>
    .disabled {
        opacity: 0.5;
        cursor: default;
    }
</r:style>

<r:script>
    $(document).ready(function() {
        var $seasonForm = $('#seasonForm');

        $("#name").focus();

        $("#btnSubmit").on("click", function(event) {
          $('.all-courts').prop('disabled', false);
        })

        $seasonForm.preventDoubleSubmission({});

        $('.timepicker').addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });

        var startDate = new Date($("#startTime").val());

        $("#showStartTime").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#startTime',
            altFormat: 'yy-mm-dd',
            minDate: new Date()
        });

        $("#showEndTime").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#endTime',
            altFormat: 'yy-mm-dd',
            minDate: new Date()
        });

        $(".courtlength").on('change', function() {
            var id  = $(this).prop("id");
            var val = $(this).prop("value");

            $("input[id*='" + id + "/length']").val(val);
        });
        $(".courtinterval").on('change', function() {
            var id  = $(this).prop("id");
            var val = $(this).prop("value");

            $("input[id*='" + id + "/interval']").val(val);
        });
        $("._sports").on('change', function() {
            var id  = $(this).prop("id");
            var val = $(this).prop("value");

            $("input[id*='court_" + id + "']").val(val);
        });
    });



    function toggleCourts(id, value) {

        var toggleMarker = $('#toggleMarker_'+ id);
        if(value) {
            toggleMarker.removeClass('icon-chevron-right');
            toggleMarker.addClass('icon-chevron-down');
        } else {
            toggleMarker.removeClass('icon-chevron-down');
            toggleMarker.addClass('icon-chevron-right');
        }

        $('.' + id + '_courts .all-courts').prop('disabled', value);
        $('.' + id + '_courts .all-courts').toggleClass("disabled", value);

        $('.' + id + '_courts.individual').toggle(value);
    }

    $('.showCourts').on("change", function(event) {
      var sportId = $(this).data("sportid");
      toggleCourts(sportId, $(this).prop("value") === "show");
    });

    $('.showCourts').each(function(index) {
      var sportId = $(this).data("sportid");
      toggleCourts(sportId, $(this).prop("value") === "show");
    });

    function toggleShowCourtsField(id) {
      var checkbox = $("#showCourts_" + id);
      val = checkbox.val();
      checkbox.val(val === "show" ? "hide" : "show");
      checkbox.change();
    }

</r:script>
</body>
</html>
