<html>
<head>
    <meta name="layout" content="b3noFooter">
    <title><g:message code="userBooking.showByTicket.title"/></title>
</head>

<body>
<div class="block top-margin50 vertical-padding40">
    <div class="container">
        <div class="panel panel-default top-margin50">
            <div class="panel-heading">
                <h1 class="h2"><g:message code="userBooking.showByTicket.heading"/> <strong>${booking.slot.court.facility.name.encodeAsHTML()}</strong></h1>
            </div>
            <div class="panel-body">
                <div class="row vertical-margin20">
                    <div class="col-md-3">
                        <h5><i class="fa fa-calendar"></i> <g:message code="default.date.label"/></h5>
                            <hr class="no-margin">
                            <h3><g:humanDateFormat date="${new org.joda.time.DateTime(booking.slot.startTime)}"/></h3>
                    </div>
                    <div class="col-md-3">
                        <h5><i class="fa fa-clock-o"></i> <g:message code="default.date.time"/></h5>
                            <hr class="no-margin">
                            <h3><g:formatDate format="HH:mm" date="${booking.slot.startTime}" />-<g:formatDate format="HH:mm" date="${booking.slot.endTime}"/></h3>
                    </div>
                    <div class="col-md-3">
                        <h5><i class="fas fa-map-marker"></i> <g:message code="court.label"/></h5>
                            <hr class="no-margin">
                            <h3 class="ellipsis" title="${booking.slot.court.name.encodeAsHTML()}">${booking.slot.court.name.encodeAsHTML()}</h3>
                    </div>
                    <div class="col-md-3">
                        <g:form name="cancelByTicketForm" action="cancelByTicket" class="form-horizontal top-margin50">
                            <g:hiddenField name="ticket" value="${ticket.key}"/>
                            <g:submitButton name="sumbit" class="btn btn-danger"
                                            value="${message(code: 'userBooking.showByTicket.submit.label')}"/>
                        </g:form>
                    </div>
                </div>

                <hr>
                <span class="text-muted">
                    <ul class="list-inline no-bottom-margin">
                        <li>
                            <g:message code="default.contact.label"/> ${booking.slot.court.facility.name.encodeAsHTML()}:
                        </li>
                        <li>
                            <i class="fas fa-phone"></i> <a href="javascript:void(0)">${booking.slot.court.facility.telephone}</a>
                        </li>
                        <li>
                            <i class="fas fa-envelope"></i> <a href="mailto:${booking.slot.court.facility.email}" target="_blank">${booking.slot.court.facility.email}</a>
                        </li>
                        <li>
                            <i class="fas fa-map-marker"></i> <a href="http://maps.google.com/maps?q=${(booking.slot.court.facility.address + ", " + booking.slot.court.facility.zipcode + " " + booking.slot.court.facility.city).encodeAsURL()}" target="_blank">${booking.slot.court.facility.address},  ${booking.slot.court.facility.zipcode} ${booking.slot.court.facility.city}</a>
                        </li>
                    </ul>
                </span>

            </div><!-- /.panel-body -->
        </div>
        <div class="top-margin50">
            <div class="">
                <g:link controller="userProfile" action="home" class="btn btn-success btn-large"><g:message code="userBooking.showByTicket.back"/></g:link>
            </div>
        </div>
        <div class="space-100"></div>
    </div>
</div>
<g:javascript>
    $(document).ready(function() {
        $("#cancelByTicketForm").preventDoubleSubmission({});
     });
</g:javascript>
</body>
</html>
