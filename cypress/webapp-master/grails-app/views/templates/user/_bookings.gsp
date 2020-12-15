<%@ page import="com.matchi.mpc.CodeRequest; com.matchi.FacilityAccessCode" %>
<g:if test="${!bookings.isEmpty()}">
    <!-- USER BOOKINGS -->
    <div class="panel panel-default">
        <header class="panel-heading">
            <h4 class="h5 no-margin">
                <i class="fa fa-calendar"></i> <g:message code="default.upcoming.bookings"/>
            <!-- <g:if test="${bookings.size() > 0}">
                <small class="text-sm badge badge-info left-margin5">${bookings.size()}</small>
            </g:if> -->
            </h4>
        </header>
        <g:if test="${bookings.size() > 0}">
            <div class="table-responsive">
                <table class="table table-striped text-sm">
                    <thead>
                    <tr>
                        <th width="40%"><g:message code="default.date.place"/></th>
                        <th width="20%"><g:message code="court.label"/></th>
                        <th width="20%"><g:message code="default.date.label"/> / <g:message code="default.date.time"/></th>
                        <th width="10%"><g:message code="templates.user.bookings.message7"/></th>
                        <th width="10%"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <% def max = bookings.size() > 5 ? 4 : bookings.size()-1 %>
                    <g:each in="${0..max}" var="it">
                        <tr>
                            <td class="vertical-padding10">
                                <div class="media">
                                    <div class="media-left">
                                        <g:link controller="facility" action="show" params="[name: bookings[it].slot.court.facility.shortname]">
                                            <div class="avatar-square-xs avatar-bordered">
                                                <g:fileArchiveFacilityLogoImage file="${bookings[it].slot.court.facility.facilityLogotypeImage}" alt="${bookings[it].slot.court.facility.name}" class=""/>
                                            </div>
                                        </g:link>
                                    </div>
                                    <div class="media-body">
                                        <h6 class="media-heading">
                                            <g:link controller="facility" action="show" params="[name: bookings[it].slot.court.facility.shortname]">${bookings[it].slot.court.facility.name}</g:link>
                                        </h6>
                                        <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${bookings[it].slot.court.facility.municipality}</span>
                                    </div>
                                </div>
                            </td>
                            <td class="vertical-padding10">${bookings[it].slot.court.name}</td>
                            <td class="vertical-padding10">
                                <g:humanDateFormat date="${new org.joda.time.DateTime(bookings[it].slot.startTime)}"/>
                                <span class="block text-sm text-muted"><g:formatDate format="HH:mm" date="${bookings[it].slot.startTime}" /></span>
                            </td>
                            <td class="vertical-padding10">
                                <g:set var="accessCode" value="${com.matchi.FacilityAccessCode.validAccessCodeFor(bookings[it]?.slot)?.content}"/>
                                <%
                                    def accessCode
                                    if (bookings[it].slot.court.facility) {
                                        accessCode = CodeRequest.findByBooking(bookings[it])?.code
                                    } else {
                                        accessCode = FacilityAccessCode.validAccessCodeFor(bookings[it]?.slot)?.content
                                    }
                                %>
                                <g:if test="${accessCode}">
                                    <span rel="tooltip" data-original-title="${message(code: 'facilityAccessCode.content.label')}"><strong>${accessCode}</strong></span>
                                </g:if>
                                <g:else>
                                    -
                                </g:else>
                            </td>
                            <td class="vertical-padding10 text-right">
                                <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger" action="cancelConfirm" controller="userBooking"
                                              update="userBookingModal"
                                              onSuccess="showLayer('userBookingModal')"
                                              params="[slotId:bookings[it].slot.id,
                                                       returnUrl: g.createLink(absolute: true, controller: params.controller, action: params.action, params: [name: bookings[it]?.slot?.court?.facility?.shortname, wl:params.wl, week: params.week, year: params.year, sport: params.sport ])]"><i class="fas fa-times"></i> <g:message code="button.unbook.label"/></g:remoteLink>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div><!-- /.table-responsive -->
        </g:if>
    </div><!-- /.USER BOOKINGS -->
</g:if>

<r:script>
    $(document).ready(function() {
        $(".cancelInfo").tooltip({
            trigger: "hover",
            title: "${message(code: 'templates.user.bookings.message5')}"
        });
    });
</r:script>
