<%@ page import="org.joda.time.LocalDate; com.matchi.FacilityProperty.FacilityPropertyKey; com.matchi.membership.MembershipType; org.joda.time.DateTime; org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib; com.matchi.Availability; com.matchi.DateUtil; com.matchi.Court; com.matchi.Sport; com.matchi.Region" %>
<head xmlns="http://www.w3.org/1999/html">

    <meta property="fb:app_id" content="340585979293099"/>
    <meta property="og:type" content="matchifb:facility"/>
    <meta property="og:url"
          content="${createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname]])}"/>
    <meta property="og:title" content="${facility.name}"/>
    <meta property="og:image" content="${createLink([uri: logoUrl, absolute: true])}"/>
    <meta property="matchifb:logo:url" content="${createLink([uri: logoUrl, absolute: true])}"/>
    <meta property="place:location:latitude" content="${facility.lat}"/>
    <meta property="place:location:longitude" content="${facility.lng}"/>
    <meta name="classes" content="splash-page"/>
    <meta name="layout" content="${!params.wl.equals("1") ? "b3main" : "whitelabel"}"/>
    <title>${facility}</title>
    <r:require modules="bootstrap-datepicker,jstorage,jquery-floatThead,datejs,readmore,matchi-truncate,matchi-user-blockbooking,bootstrap-typeahead,leaflet-open-maps"/>
    <r:script type="text/javascript">

        $(document).ready(function() {
            $("[rel=tooltip]").tooltip({
                delay: { show: 750, hide: 10 },
                html: true,
                container: 'body'
            });

            <g:if test="${!params.wl.equals("1")}">

                var map = L.map('map-canvas');

                L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
                    subdomains: 'abcd',
                    maxZoom: 19
                }).addTo( map );

                var mIcon = L.icon({
                    iconUrl: "${g.resource(dir: 'images', file: 'map_icon_100x100.png')}",
                    shadowUrl: "${g.resource(dir: 'images', file: 'map_icon_shadow.png')}",

                    iconSize:     [25, 25], // size of the icon
                    shadowSize:   [16.5, 6], // size of the shadow
                    iconAnchor:   [0, 25], // point of the icon which will correspond to marker's location
                    shadowAnchor: [-12.5, 6],  // the same for the shadow
                    popupAnchor:  [12.5, -25] // point from which the popup should open relative to the iconAnchor
                });

                var position = L.latLng(${facility.lat},${facility.lng});

                L.marker(position, {icon: mIcon}).addTo(map);
                map.setView(position, 13);
            </g:if>
        });
        function filter(sportId) {
            window.location.href = '<g:createLink action="show"
                                              params="[name: facility.shortname, week: params.week, year: params.year, wl: params.wl]"/>&sport=' + sportId;
        }
    </r:script>
</head>

<body>

<g:if test="${!params.wl.equals("1")}">
    <div id="fb-root"></div>
    <script>
        (function (d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) return;
            js = d.createElement(s);
            js.id = id;
            js.src = "//connect.facebook.net/sv_SE/all.js#xfbml=1&appId=340585979293099";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));
    </script>

    <!-- TOP COVER STRIPE -->
    <section class="block box-shadow user-profile">
        <div class="welcome-wrapper splash splash-container" style="background-image: url(<g:facilityWelcomeUrl id="${facility.id}"/>);">

        </div>
    </section>

    <section class="relative block block-grey">
        <div class="block block-white">
            <div class="container">
                <div class="flex-center wrap">
                    <div class="col-sm-5 col-xs-12 relative flex-center">
                        <!-- FACILITY LOGO -->
                        <div class="profile-avatar-wrapper">
                            <div class="profile-avatar avatar-square-lg">
                                <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}" alt="${facility.name}"
                                                                class="img-responsive"/>
                            </div>
                        </div><!-- /.facility logo-->

                    <!-- FACILITY NAME & LOCATION -->
                        <div class="profile-name-location vertical-padding15">
                            <h2 class="h4 vertical-margin5">
                                ${facility}
                            </h2>
                            <g:if test="${facility.municipality}">
                                <span class="block text-xs text-muted">
                                    <i class="fas fa-map-marker"></i> ${facility.municipality}
                                </span>
                            </g:if>

                        </div><!-- /.facility name & location-->
                    </div>


                    <!--  FACILITY NAVIGATION DESKTOP -->
                    <div class="col-sm-7 col-xs-12 text-right hidden-xs vertical-padding15">
                        <g:render template="/templates/facility/facilityNavigation"/>
                    </div>

                    <!--  FACILITY NAVIGATION MOBILE -->
                    <div class="col-xs-12 text-left top-padding10 visible-xs vertical-padding15">
                        <g:render template="/templates/facility/facilityNavigation"/>
                    </div>

                </div>
            </div><!-- /.container -->
        </div>
        <div class="container vertical-padding50">
            <div class="row">
                <!-- MAIN -->
                <div class="col-md-9 col-sm-12 top-padding20 col-lg-push-3 col-md-push-3">
                <!--  FACILITY MESSAGE DESKTOP -->
                    <g:b3FacilityMessage facility="${facility}" cssClass="hidden-xs hidden-sm"/>
                    <g:b3FacilityMessage facility="${facility}" cssClass="hidden-md hidden-lg"/>

                <!-- FACILITY NAVIGATION ANCHOR -->
                    <span id="bookingNavigation"></span>

                <!-- FACILITY SCHEDULE MOBILE -->
                    <g:if test="${facility.isBookable()}">
                        <g:render template="/templates/facility/mobileBooking"/>
                    </g:if>

                <!-- MEMBERSHIP REQUEST MESSAGE -->
                    <g:if test="${(!membership || purchaseUpcomingMembership) && !user?.hasUpcomingMembershipIn(facility) && !MembershipType.availableForPurchase(facility).list().isEmpty()}">
                        <g:requestMembership facility="${facility}" membership="${membership}" customer="${customer}" />
                    </g:if>

                <!-- FACILITY SCHEDULE DESKTOP -->
                    <g:if test="${facility.isBookable()}">
                        <g:render template="/templates/facility/scheduleBooking"/>
                    </g:if>

                <!-- COUPONS -->
                    <g:if test="${(couponsAvailableForPurchase) && !params.wl.equals("1")}">
                        <section id="coupons" class="panel panel-default">
                            <div class="panel-heading">
                                <h4 class="no-margin"><g:message code="offers.label"/></h4>
                            </div><!-- /.panel-heading -->
                            <div class="panel-body no-padding">
                                <g:render template="/templates/facility/listCouponOnline"
                                          model="[couponsAvailableForPurchase: couponsAvailableForPurchase, customer:customer]"/>
                            </div><!-- /.panel-body -->
                        </section><!-- /.panel -->
                    </g:if>

                <!-- COURSES -->
                    <g:if test="${courses}">
                        <section id="courses" class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="no-margin"><g:message code="course.label.plural"/></h3>
                            </div><!-- /.panel-heading -->
                            <div class="panel-body no-vertical-padding">
                                <g:render template="/templates/facility/listCourses"/>
                            </div><!-- /.panel-body -->
                        </section><!-- /.panel -->
                    </g:if>

                <!-- EVENTS -->
                <g:if test="${events}">
                    <section id="events" class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="no-margin"><g:message code="eventActivity.label.plural"/></h3>
                        </div>
                        <div class="panel-body no-vertical-padding">
                            <g:render template="/templates/facility/listEvents"/>
                        </div>
                    </section>
                </g:if>

                <!-- TRAINERS -->
                    <g:if test="${trainers}">
                        <section id="trainers" class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="no-margin"><g:message code="trainer.label"/></h3>
                            </div><!-- /.panel-heading -->
                            <div class="panel-body no-padding">
                                <g:render template="/templates/facility/listTrainers" model="[trainers: trainers]"/>
                            </div><!-- /.panel-body -->
                        </section><!-- /.panel -->
                    </g:if>

                <!-- ACTIVITIES -->
                    <g:if test="${!activities.isEmpty() && !params.wl.equals("1")}">
                        <section id="activities" class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="no-margin"><g:message code="default.activity.plural"/></h3>
                            </div><!-- /.panel-heading -->
                            <div class="panel-body">
                                <g:render template="/templates/facility/listActivities" model="[activities: activities, facility: facility]"/>
                            </div><!-- /.panel-body -->
                        </section><!-- /.panel -->
                    </g:if>

                </div><!-- /.col-sm-9 (MAIN) -->

                <!-- SIDEBAR -->
                <div class="col-md-3 col-sm-12 top-padding20 col-lg-pull-9 col-md-pull-9">
                    <g:if test="${membership || activeMemberships.size() > 0 || upcomingMemberships.size() > 0 || remotelyPayableMemberships.size() > 0}">
                        <div class="panel panel-default">
                            <div class="panel-body">
                                <h5><span class="fa-stack fa-lg text-info right-margin5 small-type">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fas fa-user-plus fa-stack-1x fa-inverse"></i>
                                </span><g:message code="facility.membership.info.label" /></h5>
                                <div class="small">
                                    <!-- If the user has an active or ending membership you will always get here -->
                                    <g:if test="${!activeMemberships.isEmpty()}">
                                        <g:set var="userCustomer" value="${user.getCustomer(facility)}" />

                                        <g:each in="${activeMemberships}" var="activeMembership">
                                            <p>
                                                <!-- If the membership is ending soon, we will inform the user -->
                                                <g:if test="${activeMembership.isEnding()}">
                                                    <g:message code="facility.membership.user.endingInfo" args="[activeMembership.gracePeriodEndDate.toDate()]"/>
                                                </g:if>
                                                <g:else>
                                                    <g:message code="facility.membership.user.info" args="[activeMembership.customer.facility.name]"/>
                                                </g:else>
                                                <br><g:message code="facility.membership.user.type" args="[activeMembership.type]"/>
                                            </p>

                                            <!-- And if they have available for purchase membership, we can allow the user to pay for it immediately-->
                                            <g:if test="${activeMembership?.isUnpaidPayable(activeMembership.customer.facility.yearlyMembershipPurchaseDaysInAdvance ?: 0) && activeMembership?.isRemotePayable() && activeMembership.customer.facility.hasEnabledRemotePaymentsFor(com.matchi.orders.Order.Article.MEMBERSHIP)}">
                                                <p><g:message code="facility.membership.user.unpaidStartedInfo" args="[activeMembership.startDate.toDate()]"/></p>
                                                <g:render template="remotePaymentButton" model="[facility: facility, remoteMembership: activeMembership]" />
                                            </g:if>
                                            <hr/>
                                        </g:each>
                                    </g:if>
                                    <g:if test="${!remotelyPayableMemberships.isEmpty()}">
                                        <g:each in="${remotelyPayableMemberships}" var="currentMembership">
                                            <g:message code="facility.membership.user.info" args="[currentMembership.customer.facility.name]"/>.
                                            <br><br><strong>
                                                <g:if test="${currentMembership?.inStartingGracePeriod}">
                                                    <g:message code="facility.membership.user.remotePayInfoStartGracePeriod"
                                                               args="[currentMembership?.startDate.plusDays(currentMembership?.startingGracePeriodDays).toDate(),currentMembership?.endDate?.toDate()]"/>
                                                    <p><g:message code="facility.membership.user.type" args="[currentMembership.type]"/></p>
                                                </g:if>
                                                <g:else>
                                                    <g:message code="facility.membership.user.remotePayInfo"/>
                                                </g:else>
                                            </strong><br><br>
                                            <g:render template="remotePaymentButton" model="[facility: facility, remoteMembership: currentMembership]" />
                                            <hr/>
                                        </g:each>
                                    </g:if>
                                    <g:if test="${!upcomingMemberships.isEmpty()}">
                                        <g:each in="${upcomingMemberships}" var="upcomingMembership">
                                            <g:message code="facility.membership.user.upcomingInfo"
                                                    args="[upcomingMembership.startDate.toDate()]"/>.
                                            <span><g:message code="facility.membership.user.type" args="[upcomingMembership.type]"/></span>
                                            <g:if test="${facility.hasEnabledRemotePaymentsFor(com.matchi.orders.Order.Article.MEMBERSHIP) && upcomingMembership.isRemotePayable()}">
                                                <br><br><strong><g:message code="facility.membership.user.remotePayInfo"/></strong><br><br>
                                                <g:render template="remotePaymentButton" model="[facility: facility, remoteMembership: upcomingMembership]" />
                                            </g:if>
                                            <hr/>
                                        </g:each>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </g:if>

                    <!-- ABOUT -->
                    <div class="panel panel-default">
                        <div class="panel-heading vertical-padding10">
                            <h3 class="h6"><g:message code="facility.show.message26"/> ${facility.name}</h3>

                            <!-- FAVORITES BTN -->
                            <g:if test="${user?.hasFavourite(facility)}">
                                <g:link class="btn btn-xs btn-primary btn-fav" controller="userFavorites"
                                        action="removeFavourite"
                                        params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'show', params: params)]">
                                    <span rel="tooltip" title="<g:message code="user.favorites.remove"/>">
                                        <i class="fa fa-bookmark"></i> <g:message code="user.favorites.btn"/> <i class="fa fa-check"></i>
                                    </span>
                                </g:link>
                            </g:if>
                            <g:elseif test="${!user?.hasFavourite(facility)}">
                                <g:link class="btn btn-xs btn-default btn-fav" controller="userFavorites"
                                        action="addFavourite"
                                        params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'show', params: params)]">
                                    <span rel="tooltip" title="<g:message code="user.favorites.add"/>">
                                        <i class="fa fa-bookmark-o"></i> <g:message code="user.favorites.btn"/> <i class="fa fa-plus"></i>
                                    </span>
                                </g:link>
                            </g:elseif>
                            <g:elseif test="${!user}">
                                <g:link class="btn btn btn-xs btn-default btn-fav" controller="userFavorites"
                                        action="addFavourite"
                                        params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'show', params: params)]">
                                    <span rel="tooltip" title="<g:message code="user.favorites.login"/>">
                                        <i class="fa fa-bookmark-o"></i> <g:message code="user.favorites.btn"/> <i class="fa fa-plus"></i>
                                    </span>
                                </g:link>
                            </g:elseif><!-- /.favorites btn -->

                            <!-- FACEBOOK -->
                                <g:if test="${facility.facebook}">
                                    <a href="${facility.facebook}" class="btn btn-xs btn-default" target="_blank"><i class="fab fa-facebook fa-lg"></i></a>
                                </g:if>
                            <!-- TWITTER -->
                                <g:if test="${facility.twitter}">
                                    <a href="${facility.twitter}" class="btn btn-xs btn-default" target="_blank"><i class="fab fa-twitter fa-lg"></i></a>
                                </g:if>
                            <!-- INSTAGRAM -->
                                <g:if test="${facility.instagram}">
                                    <a href="${facility.instagram}" class="btn btn-xs btn-default" target="_blank"><i class="fab fa-instagram fa-lg"></i></a>
                                </g:if>

                        </div>
                        <!-- GOOGLE MAP -->
                        <div class="canvas-wrapper">
                            <a href="https://www.google.com/maps/place/${facility.lat},${facility.lng}" target="_blank">
                                <div id="map-canvas"></div>
                            </a>
                        </div>
                        <!-- FACILITY DESCRIPTION -->
                        <div class="panel-body">
                            <article class="facility-desc">
                                <p class="text-sm"><g:toHTML text="${facility.description}"/></p>
                            </article>
                        </div><!-- /.panel-body -->
                    </div><!-- /.panel -->

                    <!-- COURTS INFO -->
                    <div class="panel panel-default">
                        <div class="panel-heading vertical-padding10">
                            <h3 class="h6"><g:message code="facility.show.message14"/></h3>
                        </div>

                        <div class="panel-body">
                            <dl class="dl-horizontal dl-info text-sm">
                                <g:if test="${courtinfo?.size() > 0}">
                                    <g:each in="${courtinfo}" var="info">
                                        <g:if test="${info?.indoors?.size() > 0}">
                                            <dt class="wide courtinfo"><g:message code="sport.name.${info?.sport?.id}"/> <g:message code="facility.show.message23"/></dt>
                                            <dd class="court">
                                                <g:each in="${info?.indoors}" var="indoor">
                                                    <g:message code="court.surface.${indoor?.surface?.name()}"/>
                                                    (${indoor.count}<g:message code="unit.st"/>)<br>
                                                </g:each>
                                            </dd>
                                        </g:if>
                                        <g:if test="${info?.outdoors?.size() > 0}">
                                            <dt class="wide courtinfo"><g:message code="sport.name.${info?.sport?.id}"/> <g:message code="facility.show.message24"/></dt>
                                            <dd class="court">
                                                <g:each in="${info?.outdoors}" var="outdoor">
                                                    <g:message code="court.surface.${outdoor?.surface?.name()}"/>
                                                    (${outdoor.count}<g:message code="unit.st"/>)<br>
                                                </g:each>
                                            </dd>
                                        </g:if>
                                    </g:each>
                                </g:if>
                            </dl>
                        </div><!-- /.panel-body -->
                    </div><!-- /.panel -->

                    <!-- OPENING HOURS -->
                    <div class="panel panel-default">
                        <div class="panel-heading vertical-padding10">
                            <h3 class="h6"><g:message code="facility.openingHoursType.${facility.openingHoursType.name()}"/></h3>
                        </div>

                        <div class="panel-body">
                            <dl class="dl-horizontal dl-info">
                                <g:each in="${1..7}" var="day" status="i">
                                    <% def av = availabilities.find { it.weekday == day } %>
                                    <dt class="wide"><g:message code="time.weekDay.${day}"/></dt>
                                    <dd>
                                        <g:if test="${av?.active}">
                                            ${av.begin.toString("HH:mm")} -
                                            ${av.end.toString("HH:mm") == "23:59" ? "24:00" : av.end.toString("HH:mm")}<br>
                                            <% //ugly hack to show 24:00 because Time is date-agnostic and 24:00 == 00:00 %>
                                        </g:if>
                                        <g:else>
                                            <g:message code="facility.show.message13"/>
                                        </g:else>
                                    </dd>
                                </g:each>
                            </dl>
                        </div><!-- /.panel-body -->
                    </div><!-- /.panel -->

                    <!-- CONTACT INFO -->
                    <div class="panel panel-default">
                        <div class="panel-heading vertical-padding10">
                            <h3 class="h6"><g:message code="facility.show.message27"/></h3>
                        </div>

                        <div class="panel-body">
                            <dl class="dl-horizontal dl-info">
                                <g:if test="${facility.website}">
                                    <dt class="icon"><i class="fas fa-link"></i></dt>
                                    <dd class="info ellipsis">
                                        <a href="${facility.website}" target="_blank">${facility.website}</a>
                                    </dd>
                                </g:if>
                                <g:if test="${facility.email}">
                                    <dt class="icon"><i class="fas fa-envelope"></i></dt>
                                    <dd class="info ellipsis">
                                        <a href="mailto:${facility.email}" target="_blank">${facility.email}</a>
                                    </dd>
                                </g:if>
                                <g:if test="${facility.telephone}">
                                    <dt class="icon"><i class="fas fa-phone"></i></dt>
                                    <dd class="info ellipsis">${facility.telephone}</dd>
                                </g:if>
                                <g:if test="${facility.address}">
                                    <dt class="icon"><i class="fas fa-map-marker"></i></dt>
                                    <dd class="info"
                                        title="${facility.address}${facility.zipcode ? ', ' + facility.zipcode : ''} ${facility.city ? facility.city : ''}">
                                        ${facility.address}<br>
                                        ${facility.zipcode ? facility.zipcode : ''}${facility.city ? ', ' + facility.city : ''}
                                    </dd>
                                </g:if>
                            </dl>
                        </div><!-- /.panel-body -->
                    </div><!-- /.panel -->

                </div><!-- /.col-sm-3 (SIDEBAR) -->



            </div><!-- /.row -->
        </div><!-- /.container -->
    </section>
</g:if>

<!-- WHITE LABEL -->
<g:if test="${params.wl.equals("1")}">
    <g:loginBar/>

    <g:if test="${facility.isBookable()}">
        <div class="row visible-xs visible-sm">
            <div class="col-sm-12 top-padding20">
                <g:render template="/templates/facility/mobileBooking"/>
            </div>
        </div>

        <div class="row hidden-xs hidden-sm">
            <div class="col-sm-12 top-padding20">
                <g:render template="/templates/facility/scheduleBooking"/>
            </div>
        </div>
    </g:if>

    <g:userWhiteLabelBookings facility="${facility}" user="${user}"/>

    <g:if test="${couponsAvailableForPurchase}">
        <section id="coupons" class="panel panel-default">
            <div class="panel-heading">
                <h3 class="h3 no-margin"><g:message code="offers.label"/></h3>
            </div>
            <div class="panel-body no-padding">
                <g:render template="/templates/facility/listCouponOnline"
                          model="[couponsAvailableForPurchase: couponsAvailableForPurchase, customer: customer]"/>
            </div>
        </section>
    </g:if>

    <g:if test="${courses}">
        <section id="courses" class="panel panel-default">
            <div class="panel-heading">
                <h3 class="h3 no-margin"><g:message code="course.label.plural"/></h3>
            </div>
            <div class="panel-body no-vertical-padding">
                <g:render template="/templates/facility/listCourses" model="[courses: courses]"/>
            </div>
        </section>
    </g:if>

    <g:if test="${activities}">
        <section id="activities" class="panel panel-default">
            <div class="panel-heading">
                <h3 class="h3 no-margin"><g:message code="default.activity.plural"/></h3>
            </div>
            <div class="panel-body">
                <g:render template="/templates/facility/listActivities" model="[activities: activities, facility: facility]"/>
            </div>
        </section>
    </g:if>
</g:if>
<!-- /.WHITE LABEL -->

<!-- WHITE LABEL FOOTER FOR SALK -->
<g:if test="${params.wl.equals("1") && params.name.equals("SALK")}">
    <br><br>
    <div class="container text-center">
        <img src="${resource(dir:'/images/facilities/salk', file:'salk_footer.png' )}" />
    </div>
</g:if>

<r:script>
    var blockBooking;
    var indoor = "${g.forJavaScript(data: params.indoor == 'false' ? '&indoor=false' : '')}";

    $('.facility-desc').readmore({
        speed: 250,
        maxHeight: 200,
        moreLink: '<a href="javascript:void(0)"><g:message code="default.readmore.label"/> <i class="fas fa-chevron-right"></i></a>',
        lessLink: '<a href="javascript:void(0)"><g:message code="button.close.label"/></a>',
        embedCSS: false
    });

    $(function() {
        $('a.smooth-anchor').click(function() {
            if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && location.hostname == this.hostname) {
                var target = $(this.hash);
                target = target.length ? target : $('[name=' + this.hash.slice(1) +']');
                if (target.length) {
                    $('html,body').animate({
                    scrollTop: (target.offset().top - 80)
                }, 1000);
                    return false;
                }
            }
        });
    });

    $(document).ready(function() {
        var today = '${g.forJavaScript(data: params.date)}' ? "${formatDate(date: new LocalDate(params.date('date')).toDate(), formatName: 'date.format.dateOnly')}" : "${formatDate(date: new Date(), formatName: 'date.format.dateOnly')}";
        var dateParam = today;
        var sport = '${g.forJavaScript(data: sport)}';
        var doit;
        $(window).resize(function() {
            clearTimeout(doit);
            doit = setTimeout(getSchedule(sport, dateParam), 100);
        });
        getSchedule(sport, today);

        var $datePickerMobile = $('#picker-mobile');
        var $sportPickerMobile = $('#sport-picker-mobile');

        $sportPickerMobile.selectpicker();

        $datePickerMobile.datepicker({
            format: "yyyy-mm-dd",
            weekStart: 1,
            todayBtn: "linked",
            calendarWeeks: true,
            autoclose: true,
            todayHighlight: true,
            startDate: new Date()
        }).on('changeDate', function(e) {
            var dateFormatted = e.format("DD dd MM");
            dateParam = e.date.toString('yyyy-MM-dd');

            $(this).html(dateFormatted);
            getFreeSlots(sport, dateParam);
        });
        $datePickerMobile.datepicker('update', '${g.forJavaScript(data: params.date)}');

        $sportPickerMobile.on('change', function() {
            sport = $(this).val();
            getFreeSlots($(this).val(), dateParam);
        });

        $('#outdoor-switch-mobile').on('change', function() {
            sport = $sportPickerMobile.val();
            getFreeSlots(sport, dateParam);
        });

        $('#indoor-switch-mobile').on('change', function() {
            sport = $sportPickerMobile.val();
            getFreeSlots(sport, dateParam);
        });

        <g:if test="${paymentFlow}">
            <g:remoteFunction controller="${paymentFlow.paymentController}" action="${paymentFlow.getFinalAction()}" params="${paymentFlow.getModalParams()}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
        </g:if>
    });

    var getSchedule = function(sport, date) {
        if (typeof blockBooking !== 'undefined') blockBooking.stop();
        var $mobileBooking = $('#mobile-booking');
        var $schedule = $('#schedule');

        if($(window).width()<=992) {
            if($mobileBooking.find('#collapse-items').length == 0) {
                getFreeSlots(sport, date);
            } else {
                $schedule.html('<div class="padding20"><i class="fas fa-spinner fa-spin"></i> <g:message code="schedule.get.schedule"/></div>');
            }
        } else {
            if($schedule.find('.schedule').length == 0) {
                $.ajax({
                    cache: false,
                    url: "${g.forJavaScript(data: createLink(controller: 'book', action: 'schedule', params: [wl: params.wl, facilityId: facility.id]))}&date="+date+"&sport="+sport + "&week=${g.forJavaScript(data: params.week ?: '')}&year=${g.forJavaScript(data: params.year ?: '')}" + indoor,
                    dataType : 'html',
                    success: function (data) {
                        $schedule.html(data);
                        blockBooking = $schedule.find('table.daily').userBlockBook({ facilityId: "${g.forJavaScript(data: facility.id)}", url: "${g.forJavaScript(data: createLink(controller: "bookingPayment", action: "confirm"))}", noSlotErrorMessage: "<g:message code="default.multiselect.noneSelectedText" />"  });
                    },
                    error: function() {
                        $schedule.html('<div class="padding20"><g:message code="schedule.error.getting.slots"/></div>');
                    }
                });
            } else {
                $mobileBooking.html('<i class="fas fa-spinner fa-spin"></i> <g:message code="schedule.get.free.slots"/>');
            }
        }
    };

    var getFreeSlots = function(sport, date) {
        if (typeof blockBooking !== 'undefined') blockBooking.stop();
        var $mobileBooking = $('#mobile-booking');
        var $mobileBookingSpinner = $("#mobile-booking-spinner");

        $mobileBooking.hide();
        $mobileBookingSpinner.show();

        var $outdoorSwitchMobile = $('#outdoor-switch-mobile');

        var $indoorSwitchMobile = $('#indoor-switch-mobile');


        if ($indoorSwitchMobile.length && $indoorSwitchMobile.is(":checked") && $outdoorSwitchMobile.length && $outdoorSwitchMobile.is(":checked")) {
            indoor = "";
        } else if (!$indoorSwitchMobile.is(":checked") && $outdoorSwitchMobile.is(":checked")) {
            indoor = "&indoor=false";
        } else if (!$outdoorSwitchMobile.is(":checked") && $indoorSwitchMobile.is(":checked")) {
            indoor = "&indoor=true";
        } else {
            indoor = "";
        }

        $.ajax({
            cache: false,
            url: "${g.forJavaScript(data: createLink(controller: 'book', action: 'listSlots', params: [wl: params.wl, facility: facility.id]))}&date="+date+"&sport="+sport + "&week=${g.forJavaScript(data: params.week ?: '')}&year=${g.forJavaScript(data: params.year ?: '')}" + indoor,
            dataType : 'html',
            success: function (data) {
                $mobileBooking.html(data);
                $mobileBookingSpinner.hide();
                $mobileBooking.show();
                if (typeof blockBooking === 'undefined')
                    blockBooking = $mobileBooking.userBlockBook({ facilityId: "${g.forJavaScript(data: facility.id)}", url: "${g.forJavaScript(data: createLink(controller: "bookingPayment", action: "confirm"))}", noSlotErrorMessage: "<g:message code="default.multiselect.noneSelectedText" />"  });
            },
            error: function() {
                $mobileBooking.html('<g:message code="schedule.error.getting.slots"/>');
                $mobileBookingSpinner.hide();
                $mobileBooking.show();
            }
        });
    };
</r:script>
<r:script>
    $(function () {
        $('.coupon-name').truncateText({
            max: 70
        });
    });
</r:script>
<g:if test="${request.xhr}">
    <r:layoutResources disposition="defer"/>
</g:if>

</body>
