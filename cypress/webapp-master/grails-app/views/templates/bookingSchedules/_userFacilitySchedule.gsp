<%@ page import="com.matchi.FacilityProperty; org.joda.time.DateTime; com.matchi.schedule.TimeSpan" %>
<div class="schedule">
    <div class="schedule2 schedule-wrapper">
        <div class="schedule-filters">
            <div class="row row-full tabmenu">
                <div class="tabs tabs-style-underline tabs-sports full-width">
                    <nav class="line">
                        <ul id="sportTabs" class="list-sports">
                            <g:each in="${sportsWithIndoor}" var="s">
                                <li class="${(sport?.id?.equals(s?.sport?.id) && indoor == s?.indoor) ? "active tab-current" : ""}">
                                    <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                                                  params="${params + [sport: s?.sport?.id, indoor: s?.indoor, facilityId: facility.id]}"
                                                  before="stopBlockBooking()"
                                                  onComplete="startBlockBooking()">
                                        <g:if test="${s.sport?.id != 6}">
                                            <i class="ma ma-${s.sport?.id}"></i>
                                        </g:if>
                                        <g:else><!-- Övrigt -->
                                            <i class="fa fa-calendar" style="font-size: 0.9em"></i>
                                        </g:else>
                                        <span><g:message
                                                code="sport.name.${s?.sport?.id}"/>${!s.indoor ? " <small>(" + message(code: 'court.outdoors.label') + ")</small>" : ""}</span>
                                    </g:remoteLink>
                                </li>
                            </g:each>
                        </ul>
                    </nav>
                </div>
            </div>

            <div class="row row-full vertical-padding20">
                <div class="no-horizontal-padding">
                    <ul class="list-inline list-date pull-left no-bottom-margin">
                        <li class="icon">
                            <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                                          class="btn btn-primary btn-outline btn-circle btn-icon"
                                          params="[facilityId: facility.id, date: date.minusDays(1).toString('yyyy-MM-dd'), sport: params.sport, indoor: indoor, wl: params.wl]"
                                          before="stopBlockBooking()"
                                          onComplete="startBlockBooking()">
                                <i class="ti-angle-left"></i>
                            </g:remoteLink>
                        </li>
                        <li class="icon">
                            <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                                          class="btn btn-primary btn-outline btn-circle btn-icon"
                                          params="[facilityId: facility.id, date: date.plusDays(1).toString('yyyy-MM-dd'), sport: params.sport, indoor: indoor, wl: params.wl]"
                                          before="stopBlockBooking()"
                                          onComplete="startBlockBooking()">
                                <i class="ti-angle-right"></i>
                            </g:remoteLink>
                        </li>
                        <li>
                            <a href="javascript:void(0)" id="picker_daily">
                                <g:formatDate format="EEEE dd MMMM" date="${date.toDate()}"/>
                            </a>
                        </li>
                        <li class="text-muted">|</li>
                        <li class="text-muted">
                            <g:message code="default.week.label"/> <g:formatDate format="w" date="${date.toDate()}"
                                                                                 locale="sv"/>
                        </li>
                        <li id="schedule-spinner" style="display: none;">
                            <i class="fas fa-spinner fa-spinner fa-spin"></i>
                        </li>
                    </ul>
                </div>

                <div class="text-right">
                    <g:render template="/templates/booking/blockBook"/>

                    <g:remoteLink update="schedule" method="GET" controller="book" action="schedule"
                                  params="${switchToWeekLinkParams}" class="btn btn-sm btn-primary">
                        <i class="fa fa-calendar"></i>
                        <g:message code="templates.bookingSchedules.userFacilitySchedule.show.week"/>
                    </g:remoteLink>
                </div>
            </div>
        </div>

        <g:set var="bookingLimit" value="${facility.getBookingRuleNumDaysBookableForUser(user)}"/>
        <g:if test="${!schedule.getAllSlots()}">
            <div class="text-center">
                <g:if test="${memberSchedule?.getAllSlots()}">
                    <h4><g:message code="templates.bookingSchedules.userFacilitySchedule.membersBookingOnly"
                                   encodeAs="HTML"/></h4>

                    <g:if test="${facility.recieveMembershipRequests && !membership}">
                        <div class="help-block">
                            <sec:ifLoggedIn>
                                <g:message
                                        code="templates.bookingSchedules.userFacilitySchedule.membersBookingOnlyWithLink"
                                        args="[facility.name.encodeAsHTML(), createLink(controller: 'membershipRequest', action: 'index', params: [name: facility.shortname])]"/>
                            </sec:ifLoggedIn>
                            <sec:ifNotLoggedIn>
                                <g:message
                                        code="templates.bookingSchedules.userFacilitySchedule.membersBookingOnlyWithLoginLink"
                                        args="[createLink(controller: 'login', action: 'auth', params: [returnUrl: createLink(controller: 'facility', action: 'show', params: params)])]"/>
                            </sec:ifNotLoggedIn>
                        </div>

                    </g:if>
                </g:if>
                <g:else>
                    <h4><g:message code="templates.bookingSchedules.userFacilitySchedule.noSlots"/></h4>
                </g:else>
            </div>
        </g:if>

        <g:elseif
                test="${schedule.allSlots.every { !it || !facility.isBookableForLimit(it.interval.start, bookingLimit) }}">
            <div class="text-center">
                <h4>
                    <g:message code="templates.bookingSchedules.userFacilitySchedule.bookingDaysAhead"
                               args="[facility.name, bookingLimit]" encodeAs="HTML"/>
                </h4>
            </div>
        </g:elseif>

        <g:else>
            <table class="table-bordered daily" width="100%" border="0" cellpadding="0" cellspacing="0">
                <thead>
                <tr height="30">
                    <th class="court"></th>
                    <g:each in="${timeSpans}" var="span">
                        <th>${span.getStartTimeFormatted("HH")}</th>
                    </g:each>
                </tr>
                </thead>
                <tbody>
                <tr class="span-indicators">
                    <td class="court"></td>
                    <g:each in="${timeSpans}" var="span">
                        <td>
                            <table><tr><td></td><td></td></tr></table>
                        </td>
                    </g:each>
                </tr>
                <g:if test="${!schedule.getAllSlots()}">
                    <tr height="50">
                        <td colspan="${timeSpans.size()}">
                            <h3 class="vertical-padding10 bottom-margin20 text-center"><g:message
                                    code="templates.bookingSchedules.userFacilitySchedule.noSlots"/></h3>
                        </td>
                    </tr>
                </g:if>
                <g:else>
                    <g:each in="${courts}" var="court" status="cidx">
                        <tr height="50">
                            <td class="court">
                                <span rel="tooltip" title="${g.toRichHTML(text: court.name)}"
                                      data-delay="150"
                                      data-html="true"
                                      data-container="body">
                                    <g:if test="${court?.facility?.hasCameraFeature() && court.hasCamera()}">
                                        <i class="fas fa-video"></i>&nbsp
                                    </g:if>${court.name}</span>
                            </td>
                            <td colspan="${timeSpans.size()}" width="40">
                                <%
                                    def start = timeSpans.first().start
                                    def end = timeSpans.last().end
                                    def slots = schedule.getSlots(new TimeSpan(start, end), court)
                                    def period = end.millis - start.millis // period in millis
                                %>
                                <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>

                                        <g:set var="lastSlotEndMillis" value="${start.millis}"/>
                                        <g:set var="bookingLimit"
                                               value="${facility.getBookingRuleNumDaysBookableForUser(user)}"/>

                                        <g:if test="${!slots.isEmpty()}">
                                            <g:each in="${slots}" var="slot" status="idx">
                                                <%
                                                    def slotStart = slot.start.millis
                                                    def slotEnd = slot.end.millis

                                                    def slotStartPixel = ((slotStart - start.millis) / period) * 100
                                                    def slotWidthPixel = ((slotEnd - slotStart) / period) * 100
                                                    def timeDiffToLast = slotStart - lastSlotEndMillis

                                                    def spaceToEnd = (end.millis - slotEnd)
                                                %>

                                            <%--
                                                Adding a "not available" slot if there is a time
                                                difference to last drawed slot (or starttime)
                                            --%>
                                                <g:if test="${timeDiffToLast > 0}">
                                                    <g:userDailyScheduleSlot
                                                            width="${((slotStart - lastSlotEndMillis) / period) * 100}%"
                                                            schedule="${schedule}" slot="${null}" timespan="${span}"
                                                            bookingLimit="${bookingLimit}"/>
                                                </g:if>

                                                <g:userDailyScheduleSlot left="${slotStartPixel}%"
                                                                         schedule="${schedule}"
                                                                         width="${slotWidthPixel}%" slot="${slot}"
                                                                         bookingLimit="${bookingLimit}"/>

                                            <%--
                                                Adding a "not available" slot if this is the last slot and current time span
                                                is stretching further in time.
                                            --%>
                                                <g:if test="${idx.intValue() == (slots.size() - 1) && spaceToEnd > 0}">
                                                    <g:userDailyScheduleSlot width="${(spaceToEnd / period) * 100}%"
                                                                             schedule="${schedule}" slot="${null}"
                                                                             timespan="${span}"
                                                                             bookingLimit="${bookingLimit}"/>
                                                </g:if>

                                                <g:set var="lastSlotEndMillis" value="${slotEnd}"/>
                                            </g:each>
                                        </g:if>
                                        <g:else>
                                            <g:userDailyScheduleSlot width="100%" schedule="${schedule}" slot="${null}"
                                                                     timespan="${span}" bookingLimit="${bookingLimit}"/>
                                        </g:else>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </g:each>
                </g:else>
                </tbody>
            </table>
        </g:else>

        <g:render template="/templates/bookingSchedules/userFacilityScheduleFooter"/>

        <g:if test="${timeSpans?.size()}">
            <g:ifFacilityPropertyEnabled facility="${facility}"
                                         name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
                <g:addSlotWatch date="${new DateTime(timeSpans?.first()?.start)}" facility="${facility}"
                                sports="${params.sport}"/>
            </g:ifFacilityPropertyEnabled>
        </g:if>
    </div>
</div>
<g:if test="${request.xhr}">
    <r:layoutResources disposition="defer"/>
</g:if>
