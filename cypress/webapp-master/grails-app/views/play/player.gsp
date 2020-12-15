<%@ page import="org.joda.time.DateTime; com.matchi.DateUtil; com.matchi.play.Recording" %>
<html>
<head>
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <meta name="layout" content="b3plain"/>
    <meta name="classes" content="recording video-player"/>

    <r:require module="video-player"/>

    <title><g:message code="facilityBooking.facilityBookingForm.viewRecording"/></title>
</head>

<body>
<g:if test="${recording && recording.hasStarted()}">
    <div class="container">
        <g:if test="${recording.hasLive()}">
            <video class="mejs__player"
                   data-mejsoptions='{ "alwaysShowControls": "true", "forceLive": "true", "stretching": "fill" }'
                   src="${recording.liveStreamUrl}" width="100%" height="100%" autoplay controls></video>
        </g:if>
        <g:elseif test="${recording.hasRecording()}">
            <video class="mejs__player"
                   data-mejsoptions='{"alwaysShowControls": "true", "stretching": "fill" }'
                   src="${recording.archiveUrl}"
                   width="100%" height="100%" autoplay controls></video>
        </g:elseif>
        <g:else>
            <p><g:message code="facilityBooking.facilityBookingForm.RecordingHasExpired"/></p>
        </g:else>
        <div class="meta">
            <img class="svg-logo matchi-green" src="/images/logo-2019-green.svg"/>

            <g:if test="${recording.recordingPurchase && recording.recordingPurchase.isFinalPaid()}">
                <a class="btn btn-success" href="${recording.archiveUrl}"
                   target="_blank"
                   title="<g:message
                           code="facilityBooking.facilityBookingForm.downloadRecording"/>">
                    <i class="fa fa-download" aria-hidden="true"></i> <g:message
                        code="facilityBooking.facilityBookingForm.downloadRecording"/>
                </a>
            </g:if>
        </div>
    </div>
</g:if>

</body>
</html>