<%@ page import="java.text.SimpleDateFormat; com.matchi.User" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <meta name="showFacebookNagger" content="${true}"/>
    <title><g:message code="userProfile.bookings.title"/> - MATCHi</title>
</head>
<body>

<section class="block vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin"><i class="fa fa-calendar"></i> <g:message code="userProfile.bookings.title"/></h2>

        <div class="row">

            <div class="col-sm-6">
                <div class="row">
                    <!-- RANDOM RESERVATIONS -->
                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <header class="panel-heading">
                                <div class="row">
                                    <div class="col-sm-9 col-xs-9">
                                        <h2 class="h4 no-margin">
                                            <i class="fa fa-calendar"></i> <g:message code="default.upcoming.spareTimeBookings"/>
                                        </h2>
                                    </div>
                                    <div class="col-sm-3 col-xs-3 text-right">
                                        <g:if test="${bookings.size() > 0}">
                                            <span class="badge badge-info">${bookings.size()}</span>
                                        </g:if>
                                    </div>
                                </div>
                            </header>
                            <g:if test="${bookings}">
                                <div class="table-responsive">
                                    <table class="table table-striped text-sm">
                                        <thead>
                                        <tr>
                                            <th width="25%"><g:message code="default.date.label"/></th>
                                            <th width="10%"><g:message code="default.date.time"/></th>
                                            <th width="40%"><g:message code="default.date.place"/></th>
                                            <th width="15%"><g:message code="payment.label"/></th>
                                            <th width="10%"></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:each in="${bookings}">
                                            <tr>
                                                <td class="vertical-padding20">
                                                    <g:humanDateFormat date="${new org.joda.time.DateTime(it.slot.startTime)}"/>
                                                </td>
                                                <td class="vertical-padding20">
                                                    <g:formatDate format="HH:mm" date="${it.slot.startTime}" />
                                                </td>
                                                <td class="vertical-padding20">
                                                    ${it.slot.court.facility.name}
                                                    <span class="block text-sm text-muted">${it.slot.court.name}</span>
                                                </td>
                                                <td class="vertical-padding20">
                                                    <g:if test="${it.order?.isFinalPaid()}">
                                                        <span class="text-success"><i class="fas fa-check"></i> <g:message code="payment.paid.label"/></span>
                                                    </g:if>
                                                    <g:else>
                                                        <span class="text-danger"><i class="fas fa-times"></i> <g:message code="payment.unpaid.label"/></span>
                                                    </g:else>
                                                </td>
                                                <td class="vertical-padding20 text-right">
                                                    <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger" action="cancelConfirm" controller="userBooking"
                                                                  update="userBookingModal"
                                                                  onSuccess="showLayer('userBookingModal')"
                                                                  params="[slotId:it.slot.id,
                                                                           returnUrl: g.createLink(absolute: false, action: 'bookings')]">
                                                        <i class="fas fa-times"></i> <g:message code="button.unbook.label"/>
                                                    </g:remoteLink>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </div><!-- /.table-responsive -->
                            </g:if>
                            <g:else>
                                <div class="panel-body text-center vertical-padding30">
                                    <h4 class="text-muted"><g:message code="default.no.upcoming.bookings"/></h4>
                                </div>
                            </g:else>
                            <div class="panel-footer">
                                <g:link controller="book" action="index" class="btn btn-success btn-sm"><i class="fas fa-plus"></i> <g:message code="button.book.new.label"/></g:link>
                                <g:link controller="userProfile" action="pastBookings" class="btn btn-success btn-sm">
                                    <i class="fa fa-calendar"></i> <g:message code="userProfile.past.viewpastbookings"/>
                                </g:link>
                            </div>
                        </div><!-- /.panel -->
                    </div><!-- /.col-sm-6 -->

                <!-- SUBSCRIPTION RESERVATIONS -->
                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <header class="panel-heading">
                                <div class="row">
                                    <div class="col-sm-9 col-xs-9">
                                        <h2 class="h4 no-margin">
                                            <i class="fa fa-calendar"></i> <g:message code="default.upcoming.subscriptionBookings"/>
                                        </h2>
                                    </div>
                                    <div class="col-sm-3 col-xs-3 text-right">
                                        <g:if test="${subscriptionBookings.size() > 0}">
                                            <span class="badge badge-info">${subscriptionBookings.size()}</span>
                                        </g:if>
                                    </div>
                                </div>
                            </header>
                            <g:if test="${subscriptionBookings}">
                                <div class="table-responsive">
                                    <table class="table table-striped text-sm">
                                        <thead>
                                        <tr>
                                            <th width="25%"><g:message code="default.date.label"/></th>
                                            <th width="15%"><g:message code="default.date.time"/></th>
                                            <th width="30%"><g:message code="default.date.place"/></th>
                                            <th width="30%"></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:each in="${subscriptionBookings}">
                                            <tr>
                                                <td class="vertical-padding20">
                                                    <g:humanDateFormat date="${new org.joda.time.DateTime(it.slot.startTime)}" />
                                                </td>
                                                <td class="vertical-padding20">
                                                    <g:formatDate format="HH:mm" date="${it.slot.startTime}" />
                                                </td>
                                                <td class="vertical-padding20">
                                                    ${it.slot.court.facility.name}
                                                    <span class="block text-sm text-muted">${it.slot.court.name}</span>
                                                </td>
                                                <td class="vertical-padding20 text-right">
                                                    <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger" action="cancelConfirm" controller="userBooking"
                                                                  update="userBookingModal"
                                                                  onSuccess="showLayer('userBookingModal')"
                                                                  params="[slotId:it.slot.id,
                                                                           returnUrl: g.createLink(absolute: false, action: 'bookings')]">
                                                        <i class="fas fa-times"></i> <g:message code="button.unbook.label"/>
                                                    </g:remoteLink>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </div>
                            </g:if>
                            <g:else>
                                <div class="panel-body text-center vertical-padding30">
                                    <h4 class="text-muted"><g:message code="default.no.upcoming.subscriptions"/></h4>
                                </div>
                            </g:else>
                            <div class="panel-footer">
                                <g:link controller="book" action="index" class="btn btn-success btn-sm">
                                    <i class="fas fa-plus"></i> <g:message code=""/> <g:message code="button.book.new.label"/>
                                </g:link>
                            </div>
                        </div><!-- /.panel -->
                    </div><!-- /.col-sm-6 -->
                </div>
            </div>

            <div class="col-sm-6">
                <div class="row">
                    <!-- ACTIVITIES -->
                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <header class="panel-heading">
                                <div class="row">
                                    <div class="col-sm-9 col-xs-9">
                                        <h2 class="h4 no-margin">
                                            <i class="fa fa-calendar"></i> <g:message code="default.upcoming.activities"/>
                                        </h2>
                                    </div>
                                    <div class="col-sm-3 col-xs-3 text-right">
                                        <g:if test="${participations.size() > 0}">
                                            <span class="badge badge-info">${participations.size()}</span>
                                        </g:if>
                                    </div>
                                </div>
                            </header>
                            <g:if test="${participations}">
                                <div class="table-responsive">
                                    <table class="table table-striped text-sm">
                                        <thead>
                                        <tr>
                                            <th width="25%"><g:message code="default.date.label"/></th>
                                            <th width="10%"><g:message code="default.date.time"/></th>
                                            <th width="40%"><g:message code="default.activity"/></th>
                                            <th width="15%"><g:message code="payment.label"/></th>
                                            <th width="10%"></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:each in="${participations}">
                                            <tr>
                                                <td class="vertical-padding20">
                                                    <g:humanDateFormat date="${it.occasion.getStartDateTime()}"/>
                                                </td>
                                                <td class="vertical-padding20">
                                                    <g:formatDate format="HH:mm" date="${it.occasion.getStartDateTime().toDate()}" />
                                                </td>
                                                <td class="vertical-padding20">
                                                    <strong>${it.occasion.activity.facility.name}</strong> - ${it.occasion.activity.name}
                                                </td>
                                                <td class="vertical-padding20">
                                                    <g:if test="${it.order?.isFinalPaid()}">
                                                        <span class="text-success"><i class="fas fa-check"></i> <g:message code="payment.paid.label"/></span>
                                                    </g:if>
                                                    <g:else>
                                                        <span class="text-danger"><i class="fas fa-times"></i> <g:message code="payment.unpaid.label"/></span>
                                                    </g:else>
                                                </td>
                                                <td class="vertical-padding20 text-right">
                                                    <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger" action="confirm" controller="activityPayment"
                                                                  update="userBookingModal"
                                                                  onSuccess="showLayer('userBookingModal')"
                                                                  params="[id: it.occasion.id,
                                                                           returnUrl: g.createLink(absolute: false, action: 'bookings')]">
                                                        <i class="fas fa-times"></i> <g:message code="button.unbook.label"/>
                                                    </g:remoteLink>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </div><!-- /.table-responsive -->
                            </g:if>
                            <g:else>
                                <div class="panel-body text-center vertical-padding30">
                                    <h4 class="text-muted"><g:message code="default.no.upcoming.activities"/></h4>
                                </div>
                            </g:else>
                        </div><!-- /.panel -->
                    </div><!-- /.col-sm-6 -->
                </div>
            </div>

        </div><!-- /.row -->

        <div class="space-40"></div>

    </div><!-- /.container -->
</section><!-- /.vertical-padding40 -->

<r:script>
    $(document).ready(function() {
        $('[rel=tooltip]').tooltip();

        if(!getCookie("hideFacebookNagger")) {
            $("#fbConnect").show();
        }
    });
</r:script>
</body>
</html>
