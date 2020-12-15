<%@ page import="com.matchi.DateUtil" %>
<g:if test="${courtGroups}">
    <div style="margin-left:100px;" class="row-fluid">
        <ul class="nav nav-tabs" style="margin-bottom:0px;">
            <g:if test="${facility.isAllCourtsTabDefault}">
                <li role="presentation" class="${params.tab == 'main' || !params.tab ? 'active' : ''}">
                    <a href="#main" aria-controls="main" role="tab" data-toggle="tab"><g:message code="facilityBooking.index.message2"/></a>
                </li>
            </g:if>
            <g:each in="${courtGroups}" var="courtGroup" status="i">
                <g:set var="tabId" value="schedule-tab-${i + 1}"/>
                <li role="presentation" class="${params.tab == tabId || (!facility.isAllCourtsTabDefault && !params.tab && i == 0) ? 'active' : ''}">
                    <a href="#${tabId}" aria-controls="${tabId}" role="tab" data-toggle="tab">${courtGroup.name}</a>
                </li>
            </g:each>
            <g:if test="${!facility.isAllCourtsTabDefault}">
                <li role="presentation" class="${params.tab == 'main' ? 'active' : ''}">
                    <a href="#main" aria-controls="main" role="tab" data-toggle="tab"><g:message code="facilityBooking.index.message2"/></a>
                </li>
            </g:if>
        </ul>
    </div>
    <div class="tab-content">
    <g:each in="${courtGroups}" var="courtGroup" status="i">
        <g:set var="tabId" value="schedule-tab-${i + 1}"/>
        <div role="tabpanel" class="tab-pane ${params.tab == tabId || (!facility.isAllCourtsTabDefault && !params.tab && i == 0) ? 'active' : ''}" id="${tabId}">
            <div class="daily-schedule schedule large">
                <g:render template="/templates/bookingSchedules/facilityDailySchedule"
                          model="[facility:facility, timeSpans: timeSpans, schedule: schedule, courtGroup: courtGroup, date: new DateUtil().formatDate(date.toDate())]"/>
            </div>
        </div>
    </g:each>
    <div role="tabpanel" class="tab-pane ${params.tab == 'main' || (facility.isAllCourtsTabDefault && !params.tab) ? 'active' : ''}" id="main">
        <div class="daily-schedule schedule large">
            <g:render template="/templates/bookingSchedules/facilityDailySchedule"
                      model="[facility:facility, timeSpans: timeSpans, schedule: schedule, date: new DateUtil().formatDate(date.toDate())]"/>
        </div>
    </div>
    </div>
</g:if>
<g:else>
    <div class="daily-schedule schedule large">
        <g:render template="/templates/bookingSchedules/facilityDailySchedule"
                  model="[facility:facility, timeSpans: timeSpans, schedule: schedule, date: new DateUtil().formatDate(date.toDate())]"/>
    </div>
</g:else>