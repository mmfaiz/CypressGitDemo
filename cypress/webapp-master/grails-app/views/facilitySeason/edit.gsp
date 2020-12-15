<%@ page import="com.matchi.MatchiConfigKey; com.matchi.MatchiConfig;  com.matchi.Season; org.joda.time.DateTimeConstants; com.matchi.Court; com.matchi.Sport; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="season.label"/></title>
    <r:require modules="jquery-validate,jquery-timepicker"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="season.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySeason.edit.message21" args="[season.name]"/></li>
</ul>
<g:errorMessage bean="${seasonCommand}"/>


<g:form name="seasonForm" action="update" method="post" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${seasonCommand.id}"/>

    <div class="form-header">
        <g:message code="facilitySeason.edit.heading"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="name"><g:message code="season.name.label2"/>*<br><g:message code="season.name.example"/></label>
            <div class="controls">
                <g:textField name="name" value="${seasonCommand?.name}" tabindex="1" class="span8"/>
            </div>
        </div>
        <div class="control-group form-inline">
            <label class="control-label" for="showStartDate"><g:message code="season.startTime.label"/>*</label>
            <div class="controls controls-row">
                <input class="span2 center-text" readonly="true" type="text" name="showStartDate" id="showStartDate"
                       value="<g:formatDate formatName="date.format.dateOnly" date="${seasonCommand.startDate}" />" />
                <g:hiddenField name="startDate" id="startDate" value="${formatDate(format: 'yyyy-MM-dd', date: seasonCommand.startDate)}"/>
                <g:hiddenField name="originalStartDate" id="originalStartDate" value="${formatDate(format: 'yyyy-MM-dd', date: seasonCommand.startDate)}"/>
                <label class="span1 control-label" for="showEndDate" style="width: 10px;"><g:message code="default.to.label"/>*</label>
                <input class="span2 center-text" readonly="true" type="text" name="showEndDate" id="showEndDate"
                       value="<g:formatDate formatName="date.format.dateOnly" date="${seasonCommand?.endDate}" />" />
                <g:hiddenField name="endDate" id="endDate" value="${formatDate(format: 'yyyy-MM-dd', date: seasonCommand?.endDate)}"/>
                <g:hiddenField name="originalEndDate" id="originalEndDate" value="${formatDate(format: 'yyyy-MM-dd', date: seasonCommand.endDate)}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="description"><g:message code="season.description.label"/></label>
            <div class="controls">
                <g:textArea rows="3" cols="50" name="description" value="${seasonCommand?.description}" class="span8"/>
            </div>
        </div>
        <div class="control-group hidden" id="deviationWarning">
            <div class="alert alert-error controls span7">
                <g:message code="facilitySeason.update.deviationWarning" />
            </div>
        </div>
        <div class="form-actions">
            <g:if test="${!season.initializing && !facility.anySeasonUpdating}">
                <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            </g:if>
            <g:if test="${!season.hasBookings() && !season.initializing}">
                <g:actionSubmit action="delete" class="btn btn-inverse"
                        onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                        value="${message(code: 'button.delete.label')}"/>
            </g:if>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>

<div class="vertical-margin10">
    <!--<h4 style="display: inline-block;">Öppetider</h4>&nbsp;&nbsp;<g:link action="editOpenHours" id="${seasonCommand.getId()}">Redigera</g:link><br>-->
    <span><g:message code="facilitySeason.edit.message8"/></span>
</div>
<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <th class="center-text"><g:message code="court.label"/></th>
        <g:each in="${form.weekDays}">
            <th class="center-text"><g:message code="time.shortWeekDay.${it}"/></th>
        </g:each>
    </tr>
    </thead>


    <tbody>
    <g:each in="${com.matchi.Court.available(form.facility).list()}" var="court">
        <tr>
            <td class="center-text">${court.name}</td>
            <g:each in="${form.weekDays}">
                <td class="center-text"><joda:format pattern="HH:mm"  value="${openingHours.get("${it}_${court.id}")?.opens}" />-<joda:format pattern="HH:mm"  value="${openingHours.get("${it}_${court.id}")?.closes}" /></td>
            </g:each>

        </tr>
    </g:each>

    </tbody></table>

<div class="vertical-padding10">
    <h4 style="display: inline-block;"><g:message code="facilitySeason.edit.message10"/></h4>
    <g:if test="${!season.initializing}">&nbsp;&nbsp;
        <g:set var="disableDeviationConfig" value="${MatchiConfig.findByKey(MatchiConfigKey.DISABLE_DEVIATION)}" />
        <g:if test="${!disableDeviationConfig.isBlocked()}">
            <g:link action="create" controller="facilitySeasonDeviation" params="[seasonId: seasonCommand.id]"><g:message code="facilitySeason.edit.message11"/></g:link>
        </g:if>
        <g:else>
            ${disableDeviationConfig.isBlockedMessage()}
        </g:else>
    </g:if><br>
    <span><g:message code="facilitySeason.edit.message12"/></span>
</div>
<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <th class="center-text"><g:message code="default.name.label"/></th>
        <th class="center-text"><g:message code="court.label"/></th>
        <th class="center-text"><g:message code="default.date.label"/></th>
        <th class="center-text"><g:message code="default.date.time"/></th>
        <th class="center-text"><g:message code="facilitySeason.edit.message17"/></th>
        <th class="center-text"><g:message code="facilitySeason.edit.message18"/></th>
    </tr>
    </thead>


    <tbody>
    <g:if test="${deviations.isEmpty()}">
        <tr>
            <td colspan="7"><em><g:message code="facilitySeason.edit.message19"/></em></td>
        </tr>
    </g:if>
    <g:each in="${deviations}" var="deviation">
        <tr>
            <td class="center-text">
                ${deviation.name}
            </td>
            <td class="center-text">
                <g:each in="${deviation.courtIds.split(",")}">
                    ${Court.read(it)?.name}
                </g:each>
            </td>
            <td class="center-text"><g:formatDate date="${deviation.fromDate.toDate()}" formatName="date.format.dateOnly"/> - <g:formatDate date="${deviation.toDate.toDate()}" formatName="date.format.dateOnly"/></td>
            <td class="center-text"><joda:format value="${deviation.fromTime}" pattern="HH:mm"/> - <joda:format value="${deviation.toTime}" pattern="HH:mm"/></td>
            <td class="center-text">
                <g:each in="${deviation.weekDays.split(",")}">
                    <g:message code="time.shortWeekDay.${it}"/>
                </g:each>
            </td>
            <td class="center-text">${deviation.open ? message(code: 'facilitySeason.edit.message25') : message(code: 'facilitySeason.edit.message26')}</td>
        </tr>
    </g:each>

    </tbody>
</table>

<r:script>
    $(document).ready(function() {
        var $seasonForm = $('#seasonForm');
        $seasonForm.bind('submit',function(e) {
            var $form = $(this);

            if ($form.data('submitted') === true) {
                // Previously submitted - don't submit again
                e.preventDefault();
                return false
            } else {
                // Mark it so that the next submit can be ignored
                $form.data('submitted', true);
                $form.find('#save').attr('disabled', 'disabled');
            }
        });

        $('.timepicker').addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });

        var startDate = new Date($("#startDate").val());

        var showExtensionWarning = function () {
            $('#deviationWarning').removeClass('hidden');
        }

        $("#showStartDate").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#startDate',
            altFormat: 'yy-mm-dd',
            minDate: new Date(),
            onClose: function (dateString) {
                var newDate = new Date(dateString), oldDate = new Date($('#originalStartDate').val());
                if(+newDate < +oldDate) {
                    showExtensionWarning();
                }
            }
        });

        $("#showEndDate").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#endDate',
            altFormat: 'yy-mm-dd',
            minDate: new Date(),
            onClose: function (dateString) {
                var newDate = new Date(dateString), oldDate = new Date($('#originalEndDate').val());
                if(+newDate > +oldDate) {
                    showExtensionWarning();
                }
            }
        });

        $(".courtlength").on('change', function() {
            var id  = $(this).attr("id");
            var val = $(this).attr("value");

            $("input[id*='" + id + "/length']").val(val);
        });
        $(".courtinterval").on('change', function() {
            var id  = $(this).attr("id");
            var val = $(this).attr("value");

            $("input[id*='" + id + "/interval']").val(val);
        });
        $("._sports").on('change', function() {
            var id  = $(this).attr("id");
            var val = $(this).attr("value");

            $("input[id*='court_" + id + "']").val(val);
        });

        $("input.timepicker").each(function() {
            $(this).rules("add", {firstTimeLargerThenSecond:true});
        });
    });

    $.validator.addMethod(
        "firstTimeLargerThenSecond",
        function(value,element) {
            var td = $(element).closest("td");

            var first = new Date(new Date().toDateString() + ' ' + td.children().find("input").eq(0).val());
            var second = new Date(new Date().toDateString() + ' ' + td.children().find("input").eq(1).val());

            if($(element).hasClass('_courtinfo')) {
                return true;
            }

            if (second >= first) {
                return true;
            }

            return false;
        }, "First time can't be later/greater then second one");

    function toggleCourts(id) {

        var toggleMarker = $('#toggleMarker_'+ id);
        if(toggleMarker.hasClass('icon-chevron-right')) {
            toggleMarker.removeClass('icon-chevron-right');
            toggleMarker.addClass('icon-chevron-down');
        } else {
            toggleMarker.removeClass('icon-chevron-down');
            toggleMarker.addClass('icon-chevron-right');
        }

        $('.' + id + '_courts').toggle();
    }

</r:script>

</body>
</html>
