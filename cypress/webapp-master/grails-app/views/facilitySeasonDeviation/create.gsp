<%@ page import="com.matchi.Season; org.joda.time.DateTimeConstants; com.matchi.Court; com.matchi.Sport; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySeasonDeviation.create.message1"/></title>
    <r:require modules="jquery-timepicker"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilitySeason" action="index"><g:message code="season.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilitySeason" action="edit" id="${form.seasonId}"><g:message code="facilitySeason.edit.heading"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySeasonDeviation.create.message4"/></li>
</ul>
<g:errorMessage bean="${form}"/>

<g:form name="seasonDeviationForm" action="confirm" id="${form.id}" method="POST" class="form-horizontal form-well">
    <g:hiddenField name="seasonId" value="${form.seasonId}"/>

    <div class="form-header">
        <g:message code="facilitySeasonDeviation.create.message23"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="fromDate"><g:message code="facilitySeasonDeviation.create.message6"/></label>
            <div class="controls">
                <g:textField class="span8" name="name" value="${form.name}" maxlength="255" required="required"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="fromDate"><g:message code="seasonDeviation.fromDate.label"/>*</label>
            <div class="controls controls-row">
                <input class="span2 center-text" type="text" name="showFromDate" id="showFromDate"
                       value="<g:formatDate formatName="date.format.dateOnly" date="${form.fromDate}" />" readonly="true"/>
                <g:hiddenField name="fromDate" id="fromDate" value="${new DateTime(form.fromDate).toString("yyyy-MM-dd")}"/>
                <label class="span1 control-label" for="toDate" style="width:10px;"><g:message code="default.to.label"/>*</label>
                <input class="span2 center-text" type="text" name="showToDate" id="showToDate"
                       value="<g:formatDate formatName="date.format.dateOnly" date="${form.toDate}" />" readonly="false"/>
                <g:hiddenField name="toDate" id="toDate" value="${new DateTime(form.toDate).toString("yyyy-MM-dd")}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="toTime"><g:message code="facilitySeasonDeviation.create.message9"/>*</label>
            <div class="controls controls-row">
                <input class="span2 timePicker center-text" type="text" name="fromTime" id="fromTime"
                       value="${form.fromTime}" readonly="true"/>
                <label class="span1 control-label" for="toTime" style="width: 10px;"><g:message code="default.to.label"/>*</label>
                <input class="span2 timePicker center-text" type="text" name="toTime" id="toTime"
                       value="${form.toTime}" readonly="true"/>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="courtIds"><g:message code="court.label.plural"/>*</label>
            <div class="controls">
                <g:each in="${courts}">
                    <label class="checkbox inline">
                        <g:checkBox name="courtIds" value="${it.id}" checked="${form.courtIds?.contains((it.id))}"/>
                        ${it.name}
                    </label>
                </g:each>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="courtIds"><g:message code="facilitySeasonDeviation.create.message12"/>*</label>
            <div class="controls">
                <g:each in="${(1..7)}">
                    <label class="checkbox inline">
                        <g:checkBox name="weekDays" id="weekDays" class="styled" value="${it}" checked="${form.weekDays?.contains((it))}"/>
                        <g:message code="time.weekDay.${it}"/>
                    </label>
                </g:each>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="length"><g:message code="facilitySeasonDeviation.create.message13"/>*</label>
            <div class="controls controls-row">
                <input class="span2 timePickerMin center-text" type="text" name="timeBetween" id="length"
                       value="${form.timeBetween}" readonly="true"/>
                <label class="span1 control-label" for="bookingLength" style="width: 30px;"><g:message code="facilitySeasonDeviation.create.message14"/>*</label>
                <input class="span2 timePickerMin center-text" type="text" name="bookingLength" id="bookingLength"
                       value="${form.bookingLength}" readonly="true"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="courtIds"><g:message code="facilitySeasonDeviation.create.message15"/></label>
            <div class="controls">
                <label class="radio inline">
                    <g:radio name="open" id="open" value="true" checked="${form.open}" /><g:message code="facilitySeasonDeviation.create.message24"/>
                </label>
                <label class="radio inline">
                    <g:radio name="open" id="open" class="styled" value="false" checked="${!form.open}" /><g:message code="button.close.label"/>
                </label>
            </div>
        </div>


        <br>

        <g:if test="${delta}">
            <%
                def overlaps = delta.rightOverlaps(delta.left)
            %>
            <h3><g:message code="facilitySeasonDeviation.create.message16"/></h3>
            <p><g:message code="facilitySeasonDeviation.create.message17"/></p>
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th><g:message code="default.date.label"/></th>
                    <th><g:message code="court.label"/></th>
                    <th><g:message code="default.status.label"/></th>
                </tr>
                </thead>

                <tbody>
                <g:each in="${delta.left}" var="item">
                    <tr>
                        <td><g:slotFullTime slot="${item.slot}"/> </td>
                        <td>${item.slot.court.name}</td>
                        <td>${overlaps.get(item.slot) ? message(code: 'facilitySeasonDeviation.create.message26') : message(code: 'facilitySeasonDeviation.create.message27')}</td>
                    </tr>
                </g:each>
                </tbody>

            </table>
        </g:if>
        <div class="form-actions">
            <g:submitButton name="save" value="${message(code: 'facilitySeasonDeviation.create.message28')}" class="btn btn-success"/>
            <g:if test="${form.id}">
                <g:link action="delete" onclick="return confirm('${message(code: 'facilitySeasonDeviation.create.message29')}');" controller="facilitySeasonDeviation" id="${form.id}"
                        class="btn btn-inverse"><g:message code="button.delete.label"/></g:link>
            </g:if>
            <g:link action="edit" controller="facilitySeason" id="${form.seasonId}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<g:javascript>
    $(document).ready(function() {
        $("#seasonDeviationForm").preventDoubleSubmission({});

        $(".timePicker").addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });
        $(".timePickerMin").addTimePicker({
            hourText: "${message(code: 'facilitySeasonDeviation.create.message30')}",
            minuteText: "${message(code: 'facilitySeasonDeviation.create.message31')}"
        });

        $("#showFromDate").datepicker({
            minDate: new Date("${g.forJavaScript(data: minDate.toString("yyyy-MM-dd"))}"),
            maxDate: new Date("${g.forJavaScript(data: maxDate.toString("yyyy-MM-dd"))}"),
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#fromDate',
            altFormat: 'yy-mm-dd'
        });

        $("#showToDate").datepicker({
            minDate: new Date("${g.forJavaScript(data: minDate.toString("yyyy-MM-dd"))}"),
            maxDate: new Date("${g.forJavaScript(data: maxDate.toString("yyyy-MM-dd"))}"),
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#toDate',
            altFormat: 'yy-mm-dd'
        });
     });
</g:javascript>
</body>
</html>
