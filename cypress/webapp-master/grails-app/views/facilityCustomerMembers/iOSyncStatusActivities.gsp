<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="facilityCustomer.iosync.activities.label"/></title>
    <r:require modules="jquery-multiselect-widget, matchi-selectall"/>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="facilityCustomer.iosync.activities.label"/></li>
    </ol>
    <div class="panel panel-default panel-admin">
        <div class="panel-heading no-padding">
            <div class="tabs tabs-style-underline" style="width: 100%;">
                <nav>
                    <ul>
                        <li>
                            <g:link controller="facilityCustomer">
                                <g:message code="customer.select.all"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link action="index">
                                <g:message code="default.member.label.plural"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link controller="facilityCustomerArchive" action="index">
                                <g:message code="default.archive.label"/>
                            </g:link>
                        </li>
                        <li class="pull-right">
                            <g:link action="iOSyncStatusMembers">
                                <g:message code="facilityCustomer.iosync.label"/>
                            </g:link>
                        </li>
                        <li class="active tab-current pull-right">
                            <g:link action="iOSyncStatusActivities">
                                <g:message code="facilityCustomer.iosync.activities.label"/>
                            </g:link>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>

        <g:if test="${facility?.hasIdrottOnlineActivitySync()}">

            <div class="panel-body">
                <div class="row vertical-padding30">
                    <div class="col-sm-12 text-center">
                        <h2><g:message code="default.activity.plural"/></h2>
                    </div>
                </div>
                <div class="row vertical-padding30">
                    <form>
                        <div class="col-sm-2 col-sm-offset-4 text-center">
                            <div class="form-group has-feedback">
                                <input type="text" class="form-control" id="showDate" name="date" value="${params.date ? params.date : new org.joda.time.LocalDate().toString("yyyy-MM-dd")}">
                                <span class="fa fa-calendar form-control-feedback" aria-hidden="true"></span>
                                <span id="inputSuccess2Status" class="sr-only">(<g:message code="default.status.success"/>)</span>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group has-feedback">
                                <g:actionSubmit class="btn btn-primary" value="${message(code:'button.filter.label')}" action="iOSyncStatusActivities"/>
                                <g:actionSubmit class="btn btn-primary" value="${message(code:'facilityCustomer.iosync.activities.resend.btn')}" action="ioActivitiesSync"/>
                            </div>
                        </div>
                    </form>
                </div>
                </div>
                <div class="row vertical-padding30">
                    <div class="col-sm-8 col-sm-offset-2">
                        <p>
                            <g:message code="facilityCustomer.iosync.activities.info" />
                        </p>
                    </div>
                </div>
                <div class="row vertical-padding30">
                    <div class="col-sm-6 text-center text-success">
                        <h1>${validatedActivites?.size()} <i class="fas fa-thumbs-up"></i></h1>
                        <h3><g:message code="facilityCustomer.iosync.validated"/></h3>
                    </div>
                    <div class="col-sm-6 text-center text-danger">
                        <h1>${notValidatedActivities?.size()} <i class="fas fa-thumbs-down"></i></h1>
                        <h3><g:message code="facilityCustomer.iosync.notvalidated"/></h3>
                    </div>
                </div>
            </div>
            <table class="table table-striped table-hover table-bordered">
                <tbody>
                <g:each in="${notValidatedActivities}" var="activityCommand">
                    <tr>
                        <td width="30%">
                            <g:message code="time.weekDay.${activityCommand.dayOfWeek}"/> ${activityCommand.prettyName} <br/>
                            <strong> ${courseActivityLookupTable[activityCommand.activityOccassionId].name} </strong>
                        </td>
                        <td width="70%">

                            <g:each in="${activityCommand.getErrorsCascading(message(code: 'facilityCustomer.iosync.missingcustomerreference').toString())}" var="map">
                                <ul>
                                    <li>
                                        <g:if test="${map.key?.personsCommand?.person?.customerId}">
                                            <g:link controller="facilityCustomer" action="edit" id="${map.key?.personsCommand?.person?.customerId}"
                                                    params="[returnUrl: createLink(controller: params.controller, action: params.action)]">
                                                ${map.key?.name ?: message(code: 'courseParticipant.label.plural')}
                                            </g:link>
                                            (<g:link controller="facilityCustomer" action="show" id="${map.key?.personsCommand?.person?.customerId}" target="_blank"><g:message code="userProfile.account.message2"/></g:link>)
                                        </g:if>
                                        <g:elseif test="${map.key?.trainerId}">
                                            <g:link controller="trainer" action="edit" id="${map.key?.trainerId}"
                                                    params="[returnUrl: createLink(controller: params.controller, action: params.action)]">
                                                ${map.key?.name ?: message(code: 'courseParticipant.label.plural')}
                                            </g:link>
                                        </g:elseif>
                                        <g:else>
                                            ${map.key?.name ?: message(code: 'courseParticipant.label.plural')}
                                        </g:else>
                                        <g:if test="${isArchived.contains(map.key?.personsCommand?.person?.customerId?.toLong())}">
                                            - <span rel="tooltip" title="${message(code: "facilityCustomer.iosync.archivedWarning")}"><i class="fa fa-warning text-danger"></i> <g:message code="default.archived.label" /></span>
                                        </g:if>
                                        <ul>
                                            <g:each in="${map.value}" var="error">
                                                <li>${message(message: error)}</li>
                                            </g:each>
                                        </ul>
                                    </li>
                                </ul>

                            </g:each>
                        </td>
                    </tr>
                </g:each>
                </tbody>
                <g:if test="${!notValidatedActivities}">
                    <tfoot>
                    <tr>
                        <td colspan="5" class="vertical-padding20">
                            <span class="text-muted text-md"><g:message code="default.noElements"/></span>
                        </td>
                    </tr>
                    </tfoot>
                </g:if>
            </table>

        </g:if>

    </div>
</div>
<r:script>
    $("[rel='tooltip']").tooltip();

    $(document).ready(function() {
       $('#showDate').datepicker({
            format: "yyyy-mm-dd",
            startDate: new Date(),
            weekStart: 1,
            language: "${RequestContextUtils.getLocale(request).language}",
            autoclose: true,
            todayHighlight: true
        });

       initializeIdrottOnlineDatePicker("#showSyncDate");

        function initializeIdrottOnlineDatePicker(selector){
            $(selector).datepicker({
                autoSize: true,
                dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                altFormat: 'yy-mm-dd'
            });
        }
    });
</r:script>
</body>
</html>
