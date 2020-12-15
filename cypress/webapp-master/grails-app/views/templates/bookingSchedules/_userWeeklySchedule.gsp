<%@ page import="com.matchi.DateUtil; com.matchi.schedule.TimeSpan; com.matchi.schedule.Schedule; org.joda.time.DateTime" %>
<div class="schedule">
    <div class="schedule2 schedule-wrapper">
        <div class="schedule-filters">
            <div class="row row-full tabmenu">
                <div class="tabs tabs-style-underline tabs-sports full-width">
                    <nav class="line">
                        <ul id="sportTabs" class="list-sports">
                            <g:each in="${sportsWithIndoor}" var="s">
                                <li class="${(sport?.id?.equals(s?.sport?.id) && indoor == s?.indoor) ? "active tab-current":""}">
                                    <g:remoteLink update="schedule" method="GET" controller="book" action="schedule" params="${params+[sport: s?.sport?.id, indoor: s?.indoor, facilityId: facility.id]}">
                                        <g:if test="${s.sport?.id != 6}">
                                            <i class="ma ma-${s.sport?.id}"></i>
                                        </g:if>
                                        <g:else> <!-- Övrigt -->
                                            <i class="fa fa-calendar" style="font-size: 0.9em"></i>
                                        </g:else>
                                        <span><g:message code="sport.name.${s?.sport?.id}"/>${!s.indoor?" <small>(" + message(code: 'court.outdoors.label') + ")</small>":""}</span>
                                    </g:remoteLink>
                                </li>
                            </g:each>
                        </ul>
                    </nav>
                </div>
            </div>
            <div class="row row-full vertical-padding20">
                <div class="col-sm-7 col-xs-12 no-horizontal-padding">
                    <ul class="list-inline list-date pull-left no-bottom-margin">
                        <li class="icon">
                            <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                                          class="btn btn-primary btn-outline btn-circle btn-icon"
                                          params="[facilityId: facility.id, week: previous.getWeekOfWeekyear(), year: previous.getWeekyear(), sport: params.sport, indoor: indoor, wl: params.wl]">
                                <i class="ti-angle-left"></i>
                            </g:remoteLink>
                        </li>
                        <li class="icon">
                            <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                                          class="btn btn-primary btn-outline btn-circle btn-icon"
                                          params="[name: facility.shortname, week: next.getWeekOfWeekyear(), year: next.getWeekyear(), sport: params.sport, indoor: indoor, wl: params.wl]">
                                <i class="ti-angle-right"></i>
                            </g:remoteLink>
                        </li>
                        <li>
                            <a href="javascript:void(0)" id="picker_weekly">
                                <g:formatDate format="d MMM"  date="${dates[0].toDate()}"/> - <g:formatDate format="d MMM"  date="${dates[6].toDate()}"/>
                                <g:formatDate format="yyyy"  date="${dates[6].toDate()}"/>
                            </a>
                        </li>
                        <li class="text-muted">|</li>
                        <li class="text-muted">
                            <g:message code="default.week.label"/> <g:formatDate format="w" date="${dates[0].toDate()}"/>
                        </li>
                        <li id="schedule-spinner"  style="display: none;">
                            <i class="fas fa-spinner fa-spinner fa-spin"></i>
                        </li>
                    </ul>
                </div>
                <div class="col-sm-5 col-xs-12 no-horizontal-padding text-right">
                    <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                            params="${switchToDateLinkParams.findAll {it.value != null}}" class="btn btn-xs btn-primary btn-outline"
                                  before="stopBlockBooking()"
                                  onComplete="startBlockBooking()">
                        <i class="fa fa-calendar"></i>
                        <g:message code="templates.bookingSchedules.userFacilitySchedule.show.day"/>
                    </g:remoteLink>
                </div>
            </div>
        </div>

        <g:set var="bookingLimit" value="${facility.getBookingRuleNumDaysBookableForUser(user)}" />
        <g:if test="${schedules.schedules.every{!it.getAllSlots()}}">
            <div class="text-center">
                <g:if test="${memberSchedules?.schedules?.any{it.getAllSlots()}}">
                    <h4><g:message code="templates.bookingSchedules.userFacilitySchedule.membersBookingOnly" encodeAs="HTML"/></h4>
                    <g:if test="${facility.recieveMembershipRequests && !membership}">

                        <sec:ifLoggedIn>
                            <g:message code="templates.bookingSchedules.userFacilitySchedule.membersBookingOnlyWithLink"
                                       args="[facility.name.encodeAsHTML(), createLink(controller: 'membershipRequest', action: 'index', params: [name: facility.shortname])]"/>
                        </sec:ifLoggedIn>
                        <sec:ifNotLoggedIn>
                            <g:message code="templates.bookingSchedules.userFacilitySchedule.membersBookingOnlyWithLoginLink"
                                       args="[createLink(controller: 'login', action: 'auth', params: [returnUrl: createLink(controller: 'facility', action: 'show', params: params)])]"/>
                        </sec:ifNotLoggedIn>
                    </g:if>
                </g:if>
                <g:else>
                    <h4><g:message code="templates.bookingSchedules.userFacilitySchedule.noSlots"/></h4>
                </g:else>
            </div>
        </g:if>

        <g:elseif test="${schedules.schedules.every{ s -> s.allSlots.every {!it || !facility.isBookableForLimit(it.interval.start, bookingLimit)}}}">
            <div class="text-center">
                <h4>
                    <g:message code="templates.bookingSchedules.userFacilitySchedule.bookingDaysAhead"
                               args="[facility.name, bookingLimit]" encodeAs="HTML"/>
                </h4>
            </div>
        </g:elseif>

        <g:else>

            <table class="table-bordered weekly" width="100%" border="0" cellpadding="0" cellspacing="0">
                <thead>
                <tr height="50">
                    <th class="court" width="120">
                    </th>
                    <g:each in="${startHour..endHour}" var="span">
                        <th>
                            ${span}
                        </th>
                    </g:each>
                </tr>
                </thead>
                <tbody>
                <tr class="span-indicators">
                    <td class="court" width="120"></td>
                    <g:each in="${startHour..endHour}" var="span">
                        <td>
                            <table><tr><td></td><td></td></tr></table>
                        </td>
                    </g:each>
                </tr>
                <g:set var="bookingLimit" value="${facility.getBookingRuleNumDaysBookableForUser(user)}" />
                <g:each in="${dates}" var="date" status="i">
                    <tr height="50">
                        <td class="date">
                            <% switchToDateLinkParams.date = date.toString('yyyy-MM-dd') %>

                            <g:remoteLink update="schedule" method="GET" controller="book" action="schedule" params="${switchToDateLinkParams}" class="top-padding2"
                                          before="stopBlockBooking()"
                                          onComplete="startBlockBooking()">
                                <span class="day pull-left top-margin2"><g:message code="time.weekDay.${date.getDayOfWeek()}"/></span>
                                <small class="text-muted pull-right"><g:formatDate date="${date.toDate()}" format="dd-MMM"/></small>
                            </g:remoteLink>
                        </td>
                        <g:each in="${startHour..endHour}" var="slot" status="ind">
                            <g:if test="${DateUtil.hourOfDayExist(date.toDate(), slot)}">
                                <%
                                    def schedule = schedules.schedules[i]
                                    def timeSpanStart = new DateTime(date).withHourOfDay(slot)
                                    def timeSpanEnd   = timeSpanStart.plusHours(1)
                                    def timeSpan = new TimeSpan(timeSpanStart, timeSpanEnd)
                                    //def status = schedule.status(timeSpan)
                                    def id = new DateTime(date.toDate()).getMillis().toString() + ind
                                %>
                                <g:userScheduleSlot id="${id}" bookingLimit="${bookingLimit}" schedule="${schedule}" span="${timeSpan}" sport="${sport}"/>
                            </g:if>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:else>

        <g:render template="/templates/bookingSchedules/userFacilityScheduleFooter"/>
    </div>
</div>
<g:if test="${request.xhr}">
    <r:layoutResources disposition="defer"/>
</g:if>
