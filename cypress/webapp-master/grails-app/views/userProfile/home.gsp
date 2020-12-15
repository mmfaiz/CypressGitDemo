<%@ page import="com.matchi.mpc.CodeRequest; org.joda.time.LocalDate; org.joda.time.DateTime; com.matchi.messages.FacilityMessage; com.matchi.Sport; com.matchi.coupon.GiftCard; org.springframework.web.servlet.support.RequestContextUtils" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <title>${user.fullName()} - MATCHi</title>
    <meta name="classes" content="splash-page"/>
    <meta name="showFacebookNagger" content="${true}"/>
    <r:require modules="chartist,bootstrap-progressbar,bootstrap-datepicker"/>
</head>
<body>
<g:b3StaticErrorMessage bean="${user}"/>

<section class="dashboard-top-wrapper bg-brand splash splash-container">
    <div class="splash-inner">
        <div class="dashboard-top-content">
            <div class="container">
                <div class="row">
                    <div class="col-sm-4">

                        <!-- USER INFO -->
                        <div class="media vertical-padding20">
                            <div class="media-left">
                                <g:link action="index">
                                    <div class="avatar-circle-sm avatar-bordered">
                                        <g:fileArchiveUserImage size="small" id="${user.id}"/>
                                    </div>
                                </g:link>
                            </div>
                            <div class="media-body full-width">
                                <h4 class="media-heading">
                                    <g:link action="index">${user.fullName()}</g:link>
                                </h4>
                                <g:if test="${user.municipality}">
                                    <small class="block top-margin5">
                                        <i class="fas fa-map-marker"></i> ${user.municipality}${user.city ? " (" + user.city + ")" : ""}
                                    </small>
                                </g:if>
                                <div class="top-margin5">
                                    <g:skillLevel id="${user.id}"/>
                                </div>
                            </div><!-- /.media-body -->
                            <div class="media-right text-right text-xs">
                                <g:link action="edit" class="top-margin10 bottom-margin5 text-right" rel="tooltip" data-original-title="${message(code: 'button.edit.profile')}">
                                    <i class="fa fa-pencil"></i>
                                </g:link>
                            </div><!-- /.media-right -->
                        </div><!-- /.media -->

                    <!-- UPCOMING BOOKING -->
                        <h5 class="bottom-margin5"><g:message code="userProfile.home.message2"/></h5>
                        <div class="row top-border vertical-padding10">
                            <g:if test="${upcomingReservation}">
                                <!-- IF THERE's AN UPCOMING BOOKING -->
                                <div class="col-xs-2 top-padding5 text-lg">
                                    <i class="fa fa-calendar-o fa-lg"></i>
                                </div>
                                <g:render template="/templates/profile/upcomingReservation/${upcomingReservation.articleType.name().toLowerCase()}" model="[upcomingReservation: upcomingReservation]" />
                            </g:if>
                            <g:else>
                                <!-- IF THERE'RE NO UPCOMING BOOKINGS -->
                                <div class="col-xs-12 bottom-margin10">
                                    <span class="text-lg">
                                        <i class="fa fa-calendar-o fa-lg"></i>
                                    </span>
                                    <span class="media-heading left-padding10">
                                        <g:message code="default.no.upcoming.bookings"/>
                                    </span>
                                </div><!-- /.col-xs-6 -->
                            </g:else>

                        </div><!-- /.row -->
                        <div>
                            <g:link controller="book" action="index" class="btn btn-lg btn-success btn-inverse"><i class="fas fa-plus"></i> <g:message code="button.book.new.label"/></g:link>
                        </div>
                    </div><!-- /.col-sm-4 -->

                <!-- USER STATS -->
                    <div class="col-sm-8">
                        <div class="row chart-top">
                            <div class="col-sm-6"><h6 class="left-margin40 weight100"><g:message code="default.booking.plural"/> <g:message code="userProfile.home.message3"/></h6></div>
                            <!--<div class="col-sm-6 text-right">
                                <div class="btn-group" role="group" aria-label="periodButtons">
                                    <button type="button" id="show-one-month" class="btn btn-default active">1 månad</button>
                                    <button type="button" id="show-six-months" class="btn btn-default">6 månader</button>
                                    <button type="button" id="show-one-year" class="btn btn-default">12 månader</button>
                                </div>
                            </div>-->
                        </div>
                        <div id="user-booking-stats" class="ct-chart ct-major-twelfth"></div>
                    </div><!-- /.col-sm-8 -->

                </div><!-- /.row -->
            </div><!-- /.container -->
        </div>
    </div>
</section>

<section class="user-profile block vertical-padding20">
    <div class="container">
        <div class="row">
            <!-- ASIDE -->
            <div class="col-sm-4">

                <g:if test="${federationsCustomers}">
                    <!-- FEDERATIONS -->
                    <div class="panel panel-default">
                        <header class="panel-heading">
                            <h4 class="h5 no-margin">
                                <i class="fas fa-bookmark"></i> <g:message code="user.federations.label"/>
                            </h4>
                        </header>
                        <ul class="list-favorites horizontal-padding15">
                            <g:each in="${federationsCustomers}">
                                <li class="favorite-item">
                                    <div class="media">
                                        <div class="media-left">
                                            <div class="avatar-square-xs avatar-bordered">
                                                <g:link controller="facility" action="show" params="[name: it.facility.shortname]">
                                                    <g:fileArchiveFacilityLogoImage file="${it.facility.facilityLogotypeImage}" alt="${it.facility.name.encodeAsHTML()}"/>
                                                </g:link>
                                            </div>
                                        </div>
                                        <div class="media-body">
                                            <h4 class="media-heading weight400">
                                                <g:link controller="facility" action="show" params="[name:it.facility.shortname]">${it.facility.name.encodeAsHTML()}</g:link>
                                            </h4>
                                            <div class="text-muted">
                                                <g:message code="user.licence.label"/>:
                                                ${it.license}
                                            </div>
                                        </div>
                                    </div>
                                </li>
                            </g:each>
                        </ul>
                    </div>
                </g:if>

                <!-- FAVORITES -->
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <h4 class="h5 no-margin">
                            <i class="fas fa-bookmark"></i> <g:message code="user.favorites.facilities.label"/>
                        </h4>
                    </header>
                    <g:if test="${user.favourites.size() > 0}">
                        <ul class="list-favorites horizontal-padding15">
                            <g:each in="${user.favourites}">
                                <li class="favorite-item">
                                    <div class="media">
                                        <div class="media-left">
                                            <div class="avatar-square-xs avatar-bordered">
                                                <g:link controller="facility" action="show" params="[name: it.facility.shortname]">
                                                    <g:fileArchiveFacilityLogoImage file="${it.facility.facilityLogotypeImage}" alt="${it.facility.name}"/>
                                                </g:link>
                                            </div>
                                        </div>
                                        <div class="media-body">
                                            <h4 class="media-heading weight400">
                                                <g:link controller="facility" action="show" params="[name:it.facility.shortname]">${it.facility.name}</g:link>
                                            </h4>
                                            <small class="text-muted">
                                                <i class="fas fa-map-marker"></i> ${it.facility.municipality}
                                            </small>
                                        </div>
                                    </div>
                                </li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <div class="panel-body">
                            <p class="text-muted text-sm">
                                <span class="bold block"><g:message code="userProfile.home.message4"/></span>
                                <g:message code="userProfile.home.message5"/>
                            </p>
                        </div>
                    </g:else>
                </div><!-- /. FAVORITES -->

                <!-- MEMBERSHIPS -->
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <h4 class="h5 no-margin">
                            <i class="fa fa-id-card-o"></i> <g:message code="membership.label.plural"/>
                        </h4>
                    </header>
                    <g:if test="${memberships}">
                        <ul class="list-favorites horizontal-padding15">
                            <g:each in="${memberships}">
                                <li class="favorite-item">
                                    <div class="media">
                                        <div class="media-left">
                                            <div class="avatar-square-xs avatar-bordered">
                                                <g:link controller="facility" action="show" params="[name: it.customer.facility.shortname]">
                                                    <g:fileArchiveFacilityLogoImage file="${it.customer.facility.facilityLogotypeImage}"
                                                            alt="${it.customer.facility.name}"/>
                                                </g:link>
                                            </div>
                                        </div>
                                        <div class="media-body">
                                            <h4 class="media-heading weight400">
                                                <g:link controller="facility" action="show" params="[name:it.customer.facility.shortname]">
                                                    ${it.customer.facility.name}
                                                </g:link>
                                            </h4>

                                            <small class="text-muted">
                                                <i class="fa fa-calendar"></i>
                                                <g:if test="${it.isUpcoming()}">
                                                    <g:message code="userProfile.home.message22"/>:
                                                    <g:formatDate date="${it.startDate.toDate()}" formatName="date.format.daterangepicker"/>
                                                </g:if>
                                                <g:elseif test="${it.isActive()}">
                                                    <g:message code="userProfile.home.message18"/>:
                                                    <g:formatDate date="${it.gracePeriodEndDate > it.endDate ? it.gracePeriodEndDate.toDate() : it.endDate.toDate()}" formatName="date.format.daterangepicker"/>
                                                </g:elseif>
                                                <g:if test="${it.autoPay}">
                                                    <i class="fa fa-repeat text-sm" rel="tooltip"
                                                            title="${message(code: 'membershipType.paidOnRenewal.recurring.tooltip')}"></i>
                                                </g:if>
                                            </small>

                                        </div>
                                    </div>
                                </li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <div class="panel-body">
                            <p class="text-muted text-sm">
                                <span class="bold block"><g:message code="userProfile.home.noMemberships"/></span>
                            </p>
                        </div>
                    </g:else>
                </div>

            <!-- MATCHING -->
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <h4 class="h5 no-margin">
                            <i class="fa fa-exchange"></i> <g:message code="userProfile.home.message6"/>
                            <g:if test="${user.matchable}">
                                <small>
                                    <g:link controller="matching" action="index" class="pull-right" params="[country: user.municipality?.region?.country ?: user.country]"><g:message code="userProfile.home.message7"/></g:link>
                                </small>
                            </g:if>
                        </h4>
                    </header>
                    <g:if test="${user.matchable}">
                        <g:getRandomMatches number="5" />
                    </g:if>
                    <g:else>
                        <div class="panel-body">
                            <p class="text-muted text-sm">
                                <g:link class="" controller="userProfile" action="updateMatchable">
                                    <g:message code="userProfile.home.message8"/>
                                </g:link>
                            </p>
                        </div>
                    </g:else>
                </div><!-- /.MATCHING -->

            </div><!-- /.ASIDE -->

        <!-- MAIN CONTENT -->
            <div class="col-sm-8">

                <!-- USER BOOKINGS -->
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <h4 class="h5 no-margin">
                            <i class="fa fa-calendar"></i> <g:message code="default.upcoming.bookings"/>
                            <g:if test="${bookings || participations}">
                                <small class="pull-right">
                                    <g:link controller="userProfile" action="bookings">
                                        <g:message code="default.see.all.booking"/>
                                    </g:link>
                                </small>
                            </g:if>
                        </h4>
                    </header>
                    <g:if test="${bookings.size() > 0}">
                        <div class="table-responsive">
                            <table class="table table-striped text-sm">
                                <thead>
                                <tr>
                                    <th width="40%"><g:message code="default.date.place"/></th>
                                    <th width="20%"><g:message code="default.date.label"/> / <g:message code="default.date.time"/></th>
                                    <th width="20%"><g:message code="court.label"/></th>
                                    <th width="10%"><g:message code="default.code"/></th>
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
                                                    <div class="avatar-square-xs avatar-bordered">
                                                        <g:link controller="facility" action="show" params="[name: bookings[it].slot.court.facility.shortname]">
                                                            <g:fileArchiveFacilityLogoImage file="${bookings[it].slot.court.facility.facilityLogotypeImage}" alt="${bookings[it].slot.court.facility.name}"/>
                                                        </g:link>
                                                    </div>
                                                </div>
                                                <div class="media-body">
                                                    <h6 class="media-heading">
                                                        <g:link controller="facility" action="show" params="[name: bookings[it].slot.court.facility.shortname]">${bookings[it].slot.court.facility.name}</g:link>
                                                    </h6>
                                                    <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${bookings[it].slot.court.facility.municipality}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="vertical-padding10">
                                            <g:humanDateFormat date="${new DateTime(bookings[it].slot.startTime)}"/>
                                            <span class="block text-sm text-muted"><g:formatDate format="HH:mm" date="${bookings[it].slot.startTime}" /></span>
                                        </td>
                                        <td class="vertical-padding10">${bookings[it].slot.court.name}</td>
                                        <td class="vertical-padding10">
                                            <g:if test="${bookings[it].getAccessCode()}">
                                                <span rel="tooltip" data-original-title="${message(code: 'userProfile.accessCode.tooltip')}"><strong>${bookings[it].getAccessCode()}</strong></span>
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
                                                                   returnUrl: g.createLink(absolute: false, action: 'home')]"><i class="fas fa-times"></i> <g:message code="button.unbook.label"/></g:remoteLink>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div><!-- /.table-responsive -->
                    </g:if>
                    <g:else>
                        <div class="panel-body bg-grey-light">
                            <div class="top-padding20">

                                <g:form name="findFacilityForm" class="form" controller="book" action="index">
                                    <div class="row">

                                        <div class="col-sm-3">
                                            <div class="form-group">
                                                <select id="sport" name="sport" data-style="form-control">
                                                    <option value=""><g:message code="default.choose.sport"/></option>
                                                    <g:each in="${Sport.coreSportAndOther.list()}">
                                                        <option value="${it.id}"
                                                                data-content="<i class='ma ma-${it.id}'></i> <g:message code="sport.name.${it.id}"/>"><g:message code="sport.name.${it.id}"/></option>
                                                    </g:each>
                                                </select>
                                            </div>
                                        </div>

                                        <div class="col-sm-3">
                                            <div class="form-group has-feedback">
                                                <input type="text" class="form-control date" id="date" name="date" aria-describedby="date" value="${params.date ? params.date : formatDate(date: new Date(), formatName: 'date.format.dateOnly')}">
                                                <span class="fa fa-calendar form-control-feedback" aria-hidden="true"></span>
                                                <span id="inputSuccess2Status" class="sr-only">(<g:message code="default.status.success"/>)</span>
                                            </div>
                                        </div>

                                        <div class="col-sm-3">
                                            <div class="form-group">
                                                <g:searchFacilityInput name="q" placeholder="${message(code: 'book.index.search.placeholder')}" class="form-control" value="${cmd?.q}"/>
                                            </div>
                                        </div>

                                        <div class="col-sm-3">
                                            <button name="submit" class="btn btn-block btn-success"><g:message code="button.smash.label"/></button>
                                        </div>
                                    </div>
                                </g:form>
                            </div>
                        </div>
                    </g:else>
                    <div class="panel-footer bg-white">
                        <g:if test="${bookings.size() > 0}">
                            <g:link controller="book" action="index" class="btn btn-success btn-xs"><i class="fas fa-plus"></i> <g:message code="button.book.new.label"/></g:link>
                        </g:if>
                        <g:else>
                            <p class="text-muted"><g:message code="default.no.upcoming.bookings"/>. <g:message code="userProfile.home.message13"/></p>
                        </g:else>
                    </div>
                </div><!-- /.USER BOOKINGS -->

            <!-- USER ACTIVITIES -->
            <div class="panel panel-default">
                <header class="panel-heading">
                    <h4 class="h5 no-margin">
                        <i class="fa fa-calendar"></i> <g:message code="default.upcoming.activities"/>
                        <g:if test="${participations || bookings}">
                            <small class="pull-right">
                                <g:link controller="userProfile" action="bookings">
                                    <g:message code="default.see.all.booking"/>
                                </g:link>
                            </small>
                        </g:if>
                    </h4>
                </header>
                <g:if test="${participations}">
                    <div class="table-responsive">
                        <table class="table table-striped text-sm">
                            <thead>
                            <tr>
                                <th width="40%"><g:message code="default.date.place"/></th>
                                <th width="20%"><g:message code="default.date.label"/> / <g:message code="default.date.time"/></th>
                                <th width="30%"><g:message code="default.activity"/></th>
                                <th width="10%"></th>
                            </tr>
                            </thead>
                            <tbody>
                            <% int maxParticipations = participations.size() > 5 ? 4 : participations.size()-1 %>
                            <g:each in="${0..maxParticipations}" var="it">
                                <tr>
                                    <td class="vertical-padding10">
                                        <div class="media">
                                            <div class="media-left">
                                                <div class="avatar-square-xs avatar-bordered">
                                                    <g:link controller="facility" action="show" params="[name: participations[it].occasion.activity.facility.shortname]">
                                                        <g:fileArchiveFacilityLogoImage file="${participations[it].occasion.activity.facility.facilityLogotypeImage}" alt="${participations[it].occasion.activity.facility.name}"/>
                                                    </g:link>
                                                </div>
                                            </div>
                                            <div class="media-body">
                                                <h6 class="media-heading">
                                                    <g:link controller="facility" action="show" params="[name: participations[it].occasion.activity.facility.shortname]">${participations[it].occasion.activity.facility.name}</g:link>
                                                </h6>
                                                <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${participations[it].occasion.activity.facility.municipality}</span>
                                            </div>
                                        </div>
                                    </td>

                                    <td class="vertical-padding10">
                                        <g:humanDateFormat date="${participations[it].occasion.getStartDateTime()}"/>
                                        <span class="block text-sm text-muted"><g:formatDate format="HH:mm" date="${participations[it].occasion.getStartDateTime().toDate()}" /></span>
                                    </td>
                                    <td class="vertical-padding10">
                                        ${participations[it].occasion.activity.name}
                                    </td>
                                    <td class="vertical-padding10 text-right">
                                        <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger" action="confirm" controller="activityPayment"
                                                      update="userBookingModal"
                                                      onSuccess="showLayer('userBookingModal')"
                                                      params="[id: participations[it].occasion.id,
                                                               returnUrl: g.createLink(absolute: false, action: 'home')]"><i class="fas fa-times"></i> <g:message code="button.unbook.label"/></g:remoteLink>
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div><!-- /.table-responsive -->
                </g:if>
                <g:else>
                    <div class="panel-footer bg-white">
                        <p class="text-muted"><g:message code="default.no.upcoming.activities"/>. <g:message code="userProfile.home.goToVenueForActivity"/></p>
                    </div>
                </g:else>
            </div><!-- /.USER ACTIVITIES -->

                <!-- USER SLOT WATCHES -->
                <g:if test="${slotWatches?.size() > 0}">
                    <div class="panel panel-default">
                        <header class="panel-heading">
                            <h4 class="h5 no-margin">
                                <i class="fa fa-clock-o"></i> <g:message code="default.queueSlots"/>
                            </h4>
                        </header>
                        <div class="table-responsive">
                            <table class="table table-striped text-sm">
                                <thead>
                                <tr>
                                    <th width="40%"><g:message code="default.date.place"/></th>
                                    <th width="20%"><g:message code="default.date.label"/> / <g:message code="default.date.time"/></th>
                                    <th width="30%"><g:message code="default.court.label"/></th>
                                    <th width="10%"></th>
                                </tr>
                                </thead>
                                <tbody>
                                <% def maxQueue = slotWatches.size() > 5 ? 4 : slotWatches.size()-1 %>
                                <g:each in="${0..maxQueue}" var="it">
                                    <tr id="slotwatch_${slotWatches[it]?.id}">
                                        <td class="vertical-padding10">
                                            <div class="media">
                                                <% def swFacility = slotWatches[it]?.facility %>
                                                <div class="media-left">
                                                    <div class="avatar-square-xs avatar-bordered">
                                                        <g:link controller="facility" action="show" params="[name: swFacility?.shortname]">
                                                            <g:fileArchiveFacilityLogoImage file="${swFacility?.facilityLogotypeImage}" alt="${swFacility?.name}"/>
                                                        </g:link>
                                                    </div>
                                                </div>
                                                <div class="media-body">
                                                    <h6 class="media-heading">
                                                        <g:link controller="facility" action="show" params="[name: swFacility?.shortname]">${swFacility?.name}</g:link>
                                                    </h6>
                                                    <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${swFacility?.municipality}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="vertical-padding10">
                                            <g:humanDateFormat date="${new DateTime(slotWatches[it]?.fromDate)}"/>
                                            <span class="block text-sm text-muted">
                                                <g:formatDate format="HH:mm" date="${slotWatches[it]?.fromDate}" /> - <g:formatDate format="HH:mm" date="${slotWatches[it]?.toDate}" />
                                            </span>
                                        </td>
                                        <td class="vertical-padding10">
                                            <g:if test="${slotWatches[it].court}">
                                                ${slotWatches[it].court.name}
                                            </g:if>
                                            <g:else>
                                                <g:message code="queueForm.allCourts"/>
                                                <g:if test="${slotWatches[it].sport}">
                                                    (<g:message code="sport.name.${slotWatches[it].sport.id}"/>)
                                                </g:if>
                                            </g:else>
                                        </td>
                                        <td class="vertical-padding10 text-right">
                                            <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger"
                                                          action="remove" controller="slotWatch" method="DELETE" mapping="slotWatch"
                                                          onSuccess="\$('#slotwatch_${slotWatches[it]?.id}').remove()"
                                                          before="if(!confirm('${message(code: "default.confirm")}')) return false"
                                                          id="${slotWatches[it]?.id}"><i class="fas fa-times"></i> <g:message code="button.delete.label"/></g:remoteLink>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div><!-- /.table-responsive -->
                    </div>
                </g:if><!-- /.USER SLOT WATCHES -->

            <g:if test="${activityWatches}">
                <g:render template="activityWatches"/>
            </g:if>

        <!-- USER TRAINER REQUESTS -->
            <g:if test="${requests?.size() > 0}">
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <h4 class="h5 no-margin">
                            <i class="fas fa-question-circle"></i> <g:message code="request.trainer.list.plural.label"/>
                        </h4>
                    </header>
                    <div class="table-responsive">
                        <table class="table table-striped text-sm">
                            <thead>
                            <tr>
                                <th width="40%"><g:message code="default.facility.label"/></th>
                                <th width="20%"><g:message code="default.trainer.label"/></th>
                                <th width="20%"><g:message code="default.date.label"/> / <g:message code="default.date.time"/></th>
                                <th width="10%"><g:message code="default.status.label"/></th>
                                <th width="10%"></th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${requests}" var="request">
                                <tr id="request_${request?.id}">
                                    <td class="vertical-padding10">
                                        <div class="media">
                                            <% def rFacility = request?.trainer?.facility %>
                                            <div class="media-left">
                                                <div class="avatar-square-xs avatar-bordered">
                                                    <g:link controller="facility" action="show" params="[name: rFacility?.shortname]">
                                                        <g:fileArchiveFacilityLogoImage file="${rFacility?.facilityLogotypeImage}" alt="${rFacility?.name}"/>
                                                    </g:link>
                                                </div>
                                            </div>
                                            <div class="media-body">
                                                <h6 class="media-heading">
                                                    <g:link controller="facility" action="show" params="[name: rFacility?.shortname]">${rFacility?.name}</g:link>
                                                </h6>
                                                <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${rFacility?.municipality}</span>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="vertical-padding10">${request.trainer}</td>
                                    <td class="vertical-padding10">
                                        <g:humanDateFormat date="${new DateTime(request?.start)}"/>
                                        <span class="block text-sm text-muted">
                                            <g:formatDate format="HH:mm" date="${request?.start}" /> - <g:formatDate format="HH:mm" date="${request?.end}" />
                                        </span>
                                    </td>
                                    <td class="vertical-padding10">
                                        <g:message code="request.status.${request.status}"/>
                                    </td>
                                    <td class="vertical-padding10 text-right">
                                        <g:remoteLink class="btn btn-link btn-xs btn-danger text-danger"
                                                      action="remove" controller="userTrainer" method="DELETE"
                                                      onSuccess="\$('#request_${request?.id}').remove()"
                                                      before="if(!confirm('${message(code: "default.confirm")}')) return false"
                                                      id="${request?.id}"><i class="fas fa-times"></i> <g:message code="button.unbook.label"/></g:remoteLink>
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div><!-- /.table-responsive -->
                </div>
            </g:if><!-- /.USER TRAINING REQUESTS -->

            <!-- USER COUPONS -->
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <h4 class="h5 no-margin">
                            <i class="fa fa-ticket"></i> <g:message code="userProfile.home.offers"/>
                        </h4>
                    </header>
                <!-- check if the user has coupons -->
                    <g:if test="${availableCoupons?.size() > 0}">
                        <ul class="list-group alt">
                            <g:each in="${availableCoupons}">
                                <li class="list-group-item">
                                    <div class="media">
                                        <div class="pull-right">
                                            <div class="bg-grey-light border-radius vertical-padding5 horizontal-padding10 text-center">
                                                <g:if test="${!it.coupon.isUnlimited()}">
                                                    <g:remoteLink controller="userProfile" action="showOfferHistory" id="${it.id}"
                                                            update="messageModal" onFailure="handleAjaxError()" onSuccess="showLayer('messageModal')"
                                                            title="${message(code: 'customerCoupon.usageHistory.tooltip')}" rel="tooltip">
                                                        <span class="block h3 no-margin bold">${it.nrOfTickets}</span>
                                                    </g:remoteLink>
                                                    <span class="block">
                                                        <g:if test="${it.coupon.instanceOf(GiftCard)}">
                                                            <g:message code="userProfile.home.giftCard.left"
                                                                    args="[it.coupon.facility.currency]"/>
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="userProfile.home.message15"/>
                                                        </g:else>
                                                    </span>
                                                </g:if>
                                                <g:else>
                                                    <div rel="tooltip" title="${message(code: 'userProfile.home.message16')}*">
                                                        <span class="block h3 no-margin bold"><i class="fa fa-repeat" alt="eternal"></i></span>
                                                        <small class="block weight400"><g:message code="userProfile.home.message17"/>*</small>
                                                    </div>
                                                </g:else>
                                            </div>
                                        </div>
                                        <div class="pull-left right-margin10">
                                            <span class="avatar-square-xs icon-lg">
                                                <i class="fa fa-ticket text-warning"></i>
                                            </span>
                                        </div>
                                        <div class="media-body weight400">
                                            <div class="block">
                                                <h4 class="bottom-margin5 bold">${it.coupon.name}</h4>
                                                <span class="block">
                                                    <g:link controller="facility" action="show" params="[name: it.coupon.facility.shortname]" class="coupon-facility-name">${it.coupon.facility.name}</g:link>
                                                </span>
                                            </div>
                                            <span class="text-sm text-muted bottom-margin5">
                                                <g:if test="${it.expireDate}">
                                                    <i class="fa fa-clock-o"></i>
                                                    <g:message code="userProfile.home.message18"/>
                                                    <g:formatDate date="${it.expireDate.toDate()}" formatName="date.format.readable.year2"/>
                                                    <g:message code="date.inclusive.label"/>
                                                </g:if>
                                                <g:else>
                                                    <i class="fa fa-clock-o"></i> <g:message code="userProfile.home.message19"/>
                                                </g:else>
                                            </span>
                                        </div>
                                    </div>
                                </li>
                            </g:each>
                        </ul>
                    </g:if>
                    <g:else>
                        <div class="panel-body">
                            <p><g:message code="userProfile.home.message20"/><br>
                                <g:link controller="facility" action="index"><g:message code="userProfile.home.message21"/></g:link>
                            </p>
                        </div>
                    </g:else>
                </div><!-- /.USER COUPONS -->

            </div><!-- /.MAIN CONTENT -->

        </div><!-- /.row -->
    </div><!-- /.container -->
</section><!-- /.user-profile -->

<div class="modal fade" id="messageModal" tabindex="-1" role="dialog" aria-labelledby="messageModal" aria-hidden="true"></div>

<r:script>
    $( document ).ready(function() {
        $("[rel=tooltip]").tooltip({ delay: { show: 1000, hide: 100 } });

        $('.progress .progress-bar').progressbar({
            transition_delay: 1000
        });

        $('#sport').selectpicker({
            title: '<g:message code="default.choose.sport"/>'
        });

        $('#date').datepicker({
            format: "${message(code: 'date.format.dateOnly.small2')}",
            startDate: new Date(),
            firstDay: 1,
            todayBtn: "linked",
            calendarWeeks: true,
            weekStart: 1,
            language: "${g.locale()}",
            autoclose: true,
            todayHighlight: true
        });

        $('#findFacilityForm').submit();
    });
</r:script>
<r:script>

    var labels       = [];
    var seriesData   = [];

    <g:each in="${activity}">
    labels.push('${g.forJavaScript(data: it.date)}');
        seriesData.push('${g.forJavaScript(data: it.count)}');
</g:each>

    /* docs: http://gionkunz.github.io/chartist-js/ */
    /* Add a basic data series with six labels and values */
    /* Need data from backend: labels: dates (input as labels here below) */
    /* data: get User Activity (show last month + current week separated into 5 weeks, enter each as datapoint) */
    var data = {
        labels: labels,
        series: [
            {
                name: 'facility name',
                data: seriesData
            }
        ]
    };

    var plugins = {
        plugins: [
        Chartist.plugins.ctPointLabels({
            labelClass: 'ct-point-label',
            labelOffset: {
                x: 0,
                y: 3
            },
            textAnchor: 'middle'
        })
        ]
    };

    /* Set some base options (settings will override the default settings in Chartist.js *see default settings*).*/
    /* We are adding a basic label interpolation function for the xAxis labels. */
    var options = {
        axisX: {
            labelInterpolationFnc: function(value) {
                return 'Date Week ' + value;
            }
        },
        showLine: true
    };

    /* Now we can specify multiple responsive settings that will override the base settings based on order */
    /* and if the media queries match. In this example we are changing the visibility of dots */
    /* and lines as well as use different label interpolations for space reasons. */
    var responsiveOptions = [
        ['screen and (min-width: 641px) and (max-width: 1024px)', {
            showPoint: false,
            axisX: {
                labelInterpolationFnc: function(value) {
                    return 'Week ' + value;
                }
            }
        }]
    ];

    /* Initialize the chart with the above settings */
    new Chartist.Line('#user-booking-stats', data, plugins, options, responsiveOptions);

    var easeOutQuad = function (x, t, b, c, d) {
        return -c * (t /= d) * (t - 2) + b;
    };

    var $chart = $('#user-booking-stats');

    var $toolTip = $chart
    .append('<div class="tooltip"></div>')
    .find('.tooltip')
    .hide();

    $chart.on('mouseenter', '.ct-point', function() {
        var $point = $(this),
        value = $point.attr('ct:value'),
        seriesName = $point.parent().attr('ct:series-name');

        $point.animate({'stroke-width': '50px'}, 300, easeOutQuad);
        $toolTip.html(seriesName + '<br>' + value).show();
    });

    $chart.on('mouseleave', '.ct-point', function() {
        var $point = $(this);

        $point.animate({'stroke-width': '20px'}, 300, easeOutQuad);
        $toolTip.hide();
    });

    $chart.on('mousemove', function(event) {
        $toolTip.css({
            left: (event.offsetX || event.originalEvent.layerX) - $toolTip.width() / 2 - 10,
            top: (event.offsetY || event.originalEvent.layerY) - $toolTip.height() - 40
        });
    });
</r:script>
</body>
</html>
