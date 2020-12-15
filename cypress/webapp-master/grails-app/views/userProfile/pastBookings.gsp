<%@ page import="org.joda.time.DateTime; com.matchi.DateUtil; java.text.SimpleDateFormat; com.matchi.User;" %>
<html>
<head>
    <meta name="layout" content="b3main"/>
    <meta name="showFacebookNagger" content="${true}"/>
    <title><g:message code="userProfile.past.bookings"/> - MATCHi</title>
</head>

<body>
<section class="block block-white vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin"><i class="fa fa-calendar"></i> <g:message
                code="userProfile.past.bookings"/></h2>

        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <div class="row">
                            <div class="col-sm-3"><strong><g:message code="default.date.place"/></strong></div>

                            <div class="col-sm-2"><strong><g:message code="default.court.label"/></strong></div>

                            <div class="col-sm-2"><strong><g:message code="default.date.label"/> / <g:message
                                    code="default.date.time"/></strong></div>

                            <div class="col-sm-3"><strong><g:message code="userProfile.past.bookings.players"/></strong>
                            </div>

                            <div class="col-sm-2"><strong>MATCHi Play</strong></div>
                        </div>
                    </header>

                    <div class="list-group alt">
                        <g:each in="${bookings}">
                            <div class="list-group-item row row-full">
                                <div class="col-sm-3">
                                    <div class="media">
                                        <div class="media-left">
                                            <div class="avatar-square-xs avatar-bordered">
                                                <g:link controller="facility" action="show"
                                                        params="[name: it.slot.court.facility.shortname]">
                                                    <g:fileArchiveFacilityLogoImage
                                                            file="${it.slot.court.facility.facilityLogotypeImage}"
                                                            alt="${it.slot.court.facility.name}"/>
                                                </g:link>
                                            </div>
                                        </div>

                                        <div class="media-body">
                                            <h6 class="media-heading">
                                                <g:link controller="facility" action="show"
                                                        params="[name: it.slot.court.facility.shortname]">${it.slot.court.facility.name}</g:link>
                                            </h6>
                                            <span class="block text-sm text-muted"><i
                                                    class="fas fa-map-marker"></i> ${it.slot.court.facility.municipality}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-sm-2">
                                    ${it.slot.court.name}
                                </div>

                                <div class="col-sm-2">
                                    <g:formatDate date="${it.slot.startTime}" formatName="date.format.dateOnly"/>
                                    <span class="block text-sm text-muted">
                                        <g:weekDay date="${new DateTime(it.slot.startTime)}"/>
                                        <g:formatDate format="HH:mm" date="${it.slot.startTime}"/>
                                    </span>
                                </div>

                                <div class="col-sm-3 playerNames">
                                    <g:if test="${it.players}">
                                        <g:bookingPlayers players="${it.players}" var="playerName">
                                            <div>${playerName}</div>
                                        </g:bookingPlayers>
                                    </g:if>
                                    <g:else>
                                        <div>${it.customer?.firstname} ${it.customer?.lastname}</div>
                                    </g:else>
                                </div>

                                <div class="col-sm-2" >
                                    <g:set var="recording" value="${recordingsByBookingId[it.id]}"/>
                                    <g:if test="${recording && recording.hasStarted()}">
                                        <g:if test="${recording.isLive()}">
                                            <a class="btn btn-danger btn-xs" href="${recording.internalPlayerUrl}"
                                               target="_blank"
                                               title="<g:message
                                                       code="facilityBooking.facilityBookingForm.viewLiveStream"/>">
                                                <i class="fa fa-video-camera fa-sm" aria-hidden="true"></i> Stream
                                            </a>
                                        </g:if>
                                        <g:if test="${recording.hasRecording() && recording.isPossiblyAccessed()}">
                                            <g:if test="${recording.recordingPurchase && recording.recordingPurchase.isFinalPaid()}">

                                                <a class="btn btn-success btn-xs" href="${recording.internalPlayerUrl}"
                                                   target="_blank"
                                                   title="<g:message
                                                           code="facilityBooking.facilityBookingForm.viewRecording"/>">
                                                    <i class="fa fa-video-camera fa-sm" aria-hidden="true"></i>
                                                </a>
                                                <a class="btn btn-success btn-xs" href="${recording.archiveUrl}"
                                                   target="_blank"
                                                   title="<g:message
                                                           code="facilityBooking.facilityBookingForm.downloadRecording"/>">
                                                    <i class="fa fa-download fa-sm" aria-hidden="true"></i>
                                                </a>
                                            </g:if>
                                            <g:else>
                                                <g:remoteLink class="btn btn-warning btn-xs"
                                                              controller="recordingPayment"
                                                              action="confirm"
                                                              params="[bookingId: recording.bookingId]"
                                                              update="userBookingModal"
                                                              onFailure="handleAjaxError(XMLHttpRequest, textStatus, errorThrown)"
                                                              onSuccess="showLayer('userBookingModal')">
                                                    <i class="fa fa-video-camera fa-sm" aria-hidden="true"></i>
                                                    <g:message code="button.buy.label"/>
                                                </g:remoteLink>
                                            </g:else>
                                        </g:if>
                                    </g:if>
                                </div>
                            </div>
                        </g:each>
                    </div>

                    <div class="text-center">
                        <g:b3PaginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                                                      maxsteps="0" max="${10}" params="${params}"
                                                      action="pastBookings" total="${bookings?.getTotalCount()}"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="space-40"></div>
</section>

<r:script>
    $(document).ready(function () {
        $("*[rel=tooltip]").tooltip();

        if (!getCookie("hideFacebookNagger")) {
            $("#fbConnect").show();
        }
    });
</r:script>
</body>
</html>
