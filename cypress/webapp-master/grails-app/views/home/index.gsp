<%@ page import="org.joda.time.LocalDate; com.matchi.Facility; com.matchi.Sport" %>
<html>
<head>
    <title><g:message code="page.home.index.header"/></title>
    <meta name="layout" content="b3main" />
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
    <meta name="classes" content="splash-page"/>
    <r:require modules="waypoints,bootstrap-datepicker"/>
</head>
<body>

<div id="landing">
    <div id="hero" class="splash splash-container start">
    <div class="splash-inner">
        <div class="stripe">
            <div class="container">
                <div class="row">
                    <div class="col-xs-12 col-sm-12 col-md-10 col-md-offset-1 col-lg-8 col-lg-offset-2">
                        <div class="text-center">

                            <h2 class="h1"><g:message code="home.index.message2"/></h2>
                        </div>
                        <div class="space-10"></div>
                        <g:form controller="book" action="index" class="form-inline">
                            <div class="row no-margin no-padding">
                                <div class="form-group col-sm-3 col-xs-12 no-margin-padding">
                                    <select id="sport" name="sport" data-style="form-control">
                                        <option value=""><g:message code="default.choose.sport"/></option>
                                        <g:each in="${Sport.coreSportAndOther.list()}">
                                            <option value="${it.id}" ${cmd?.sport == it.id ? "selected" : ""}
                                                    data-content="<i class='ma ma-${it.id}'></i> <g:message code="sport.name.${it.id}"/>"><g:message code="sport.name.${it.id}"/></option>
                                        </g:each>
                                    </select>
                                </div>
                                <div class="col-sm-3 col-xs-12 no-margin-padding">
                                    <div class="form-group no-margin has-feedback full-width">
                                        <input type="text" class="form-control" id="date" name="date" value="${params.date ? params.date : formatDate(date: new Date(), formatName: 'date.format.dateOnly')}">
                                        <span class="fa fa-calendar form-control-feedback" aria-hidden="true"></span>
                                        <span id="inputSuccess2Status" class="sr-only">(<g:message code="default.status.success"/>)</span>
                                    </div>
                                </div>
                                <div class="form-group col-sm-4 col-xs-12 no-margin-padding">
                                    <g:searchFacilityInput name="q" placeholder="${message(code: 'book.index.search.placeholder')}" class="form-control" value="${cmd?.q}"/>
                                </div>

                                <div class="col-sm-2 col-xs-12 no-margin-padding">
                                    <g:submitButton name="submit" value="${message(code: 'button.smash.label')}" class="btn btn-block btn-success hidden-xs pull-right"/>
                                    <g:submitButton name="submit" value="${message(code: 'button.smash.label')}" class="btn btn-block btn-success col-xs-12 visible-xs"/>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
                <div class="space-20"></div>
                <!-- <div id="learnmore" class="text-center top-margin50 top-padding20 text-lg">
                    <button class="btn btn-link btn-white"><i class="text-xlg ti-arrow-circle-down"></i></button>
                </div> -->
            </div>
        </div>
    </div>
</div>

<!-- ABOUT MATCHI -->
<section id="about" class="block responsive-vertical-padding">
    <div class="container relative">
        <div class="top">
            <div class="">
                <div class="row">
                    <div class="col-sm-5">

                    </div>
                    <div class="col-sm-7 say-hello-to-new-matchi">
                        <h2 class="no-top-margin"><g:message code="home.index.message30"/> <span class="logo-2019"><span>MATCHi</span></span></h2>
                        <p class="lead"><g:message code="home.index.message31"/></p>
                    </div>
                </div>
            </div>
        </div>
        <g:set var="appStoreImage" value="${"appstore-" + (["en", "sv", "da", "es", "no"].contains(g.locale()) ? g.locale() : "en") + ".svg"}" />
        <g:set var="googlePlayImage" value="${"googleplay-" + (["en", "sv", "da", "es", "no"].contains(g.locale()) ? g.locale() : "en") + ".svg"}" />
        <div class="bottom">
            <div class="container">
                <div class="row flex-center">
                    <div class="col-xs-4 position-static">
                        <div class="big-phone-overlay"><div class="inner"></div></div>
                    </div>
                    <div class="col-xs-8 no-visibility-sm-up">
                        <div class="download-app-buttons flex-center wrap">
                            <a href="https://apps.apple.com/se/app/matchi/id720782039"><img src="${resource(dir:'images', file:appStoreImage )}" alt="Download on Appstore" width="121" height="40" /></a>
                            <a href="https://play.google.com/store/apps/details?id=com.matchi"><img src="${resource(dir:'images', file:googlePlayImage )}" alt="Download on google play store" width="138" height="40" /></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="bottom-bg bg-grey-lightest hidden-xs">
        <div class="container">
            <div class="col-sm-5">

            </div>
            <div class="col-sm-7">
                <div class="download-app-buttons">
                    <a href="https://apps.apple.com/se/app/matchi/id720782039"><img src="${resource(dir:'images', file:appStoreImage )}" alt="Download on Appstore" width="121" height="40" /></a>
                    <a href="https://play.google.com/store/apps/details?id=com.matchi"><img src="${resource(dir:'images', file:googlePlayImage )}" alt="Download on google play store" width="138" height="40" /></a>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- BRAGGING SECTION -->
<section id="site-stats" class="block responsive-vertical-padding bg-brand">
    <div class="container">
        <g:topFooterStats/>
    </div>
</section>

<!-- WHATS IN IT FOR USERS -->
<section id="for-users" class="block block-white responsive-vertical-padding no-bottom-padding">
    <div class="container">
        <div class="row relative">
            <div class="col-xs-12 col-sm-6 intro">
                <h2><g:message code="home.index.message13"/></h2>

                <p class="lead">
                    <g:message code="home.index.message14"/>
                </p>

                <ul class="checked">
                    <li><g:message code="home.index.message16"/></li>
                    <li><g:message code="home.index.message17"/></li>
                    <li><g:message code="home.index.message18"/></li>
                </ul>

                <p>
                    <g:link class="btn btn-lg btn-success btn-outline" controller="userRegistration" action="index"><g:message code="home.index.message19"/></g:link>
                </p>
            </div>

            <div class="col-xs-12 col-sm-6 side-image">
                <img src="${resource(dir:'images', file:'iphone_and_macbook-v2.png' )}">
            </div>
        </div>
    </div>
    <div class="bottom bg-grey-lightest">

    </div>
</section>
    <!-- WHATS IN IT FOR CLUBS -->
<section id="for-admins" class="block bg-brand responsive-vertical-padding">
    <div class="container">
        <div class="row relative">
            <div class="col-xs-12 col-sm-6 side-image">
                <!-- img src="/images/matchi-desktop-showcase-presentation.png" class="center-block showcase-06" -->
                <img src="${resource(dir:'images', file:'imac-optimized-venueschedule2.png' )}">
            </div>

            <div class="col-xs-12 col-sm-6 intro">
                <div class="inner">
                    <h2><g:message code="home.index.message20"/></h2>

                    <p class="lead">
                        <g:message code="home.index.message21"/>
                    </p>
                    <p class="text-md bottom-margin20">
                        <g:message code="home.index.message22"/>
                    </p>

                    <ul class="checked">
                        <li><g:message code="home.index.message23"/></li>
                        <li><g:message code="home.index.message24"/></li>
                        <li><g:message code="home.index.message25"/></li>
                    </ul>

                    <p>
                        <g:link class="btn btn-lg btn-success btn-outline" action="getmatchi"><g:message code="default.readmore.label"/></g:link>
                    </p>
                </div>
            </div>
        </div>
    </div>
</section>

</div>

<r:script>
    $(document).ready(function() {
        $("#sport").selectpicker({
            title: "${message(code: 'default.choose.sport')}"
        });

        $('#date').datepicker({
            format: "${message(code: 'date.format.dateOnly.small2')}",
            startDate: new Date(),
            weekStart: 1,
            todayBtn: "linked",
            autoclose: true,
            language: "${g.locale()}",
            calendarWeeks: true,
            todayHighlight: true
        });

        // Waypoints
        $('.wp1').waypoint(function() {
    		$('.wp1').addClass('animated fadeIn');
    	}, {
    		offset: '95%'
    	});
    	$('.wp2').waypoint(function() {
    		$('.wp2').addClass('animated fadeInUp');
    	}, {
    		offset: '85%'
    	});
    	$('.wp3').waypoint(function() {
    		$('.wp3').addClass('animated fadeInRight');
    	}, {
    		offset: '95%'
    	});
    	$('.wp4').waypoint(function() {
    		$('.wp4').addClass('animated fadeInDown');
    	}, {
    		offset: '95%'
    	});
    	$('.wp5').waypoint(function() {
    		$('.wp5').addClass('animated fadeInLeft');
    	}, {
    		offset: '95%'
    	});

        $(function() {
            $.datepicker.__proto__.__generateHTML = $.datepicker.__proto__._generateHTML;
            $.extend($.datepicker.__proto__, {
                _generateHTML: function(inst){
                	var html	= this.__generateHTML.apply(this, arguments);
                	html = html.replace('ui-datepicker-calendar', 'ui-datepicker-calendar table table-condensed');
                	html = html.replace('ui-datepicker-header', 'ui-datepicker-header nav');
                	html = html.replace('ui-datepicker-prev', 'ui-datepicker-prev pull-left');
                	html = html.replace('ui-datepicker-next', 'ui-datepicker-next pull-right');
                	return html;
                }
            });
        });
    });
</r:script>
</body>

</html>
