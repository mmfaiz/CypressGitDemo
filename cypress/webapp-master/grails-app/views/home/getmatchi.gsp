<html>
<head>
    <title><g:message code="page.home.index.header"/></title>
    <meta name="layout" content="b3main" />
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
    <meta name="classes" content="splash-page"/>
    <r:require modules="jquery-validate, waypoints"/>
</head>
<body>
<!-- ClickTale Top part -->
<script type="text/javascript">
    var WRInitTime=(new Date()).getTime();
</script>
<!-- ClickTale end of Top part -->

<g:b3StaticErrorMessage bean="${cmd}"/>

<!-- Image -->
<section class="block">
    <div class="splash splash-container promo">
        <div class="splash-inner">
            <div class="stripe">
                <div class="container">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12">
                            <h1><g:message code="home.getmatchi.message2"/></h1>
                            <p class="lead">
                                <g:message code="home.getmatchi.message3"/>
                            </p>
                            <p>
                                <a id="call2action" class="btn btn-lg btn-success" href="#getInTouch"><g:message code="default.contactUs.label"/></a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section><!-- /.Image -->

<!-- Facility logos -->
<section class="block bg-white top-padding30 bottom-padding60">
    <div class="container text-center vertical-padding20">
        <h2>
            <g:message code="home.getmatchi.message5"/><br/>
            <small><g:link class="btn btn-link" action="customers"><g:message code="home.getmatchi.message6"/></g:link></small>
        </h2>

    </div>
    <div class="container text-center bottom-padding40">
        <g:each in="${facInfo}" status="i" var="fac">
            <g:if test="${i % 4 == 0 || i == 0}">
                <div class="row">
            </g:if>

            <div class="col-sm-3">
                <div class="vertical-padding30 horizontal-padding30">
                    <div class="padding10">
                        <div class="logotype" style="background: url('${fac.logotype}') no-repeat center center;background-size: contain;width: 100%;height: 125px;"></div>
                    </div>
                    <div class="text-center">
                        ${fac.nrCourts} <span><g:message code="court.label.plural2"/></span>
                    </div>
                </div>
            </div>

            <g:if test="${((i+1) % 4 == 0 && i > 0) || i == facInfo.size() -1}">
                </div>
            </g:if>
        </g:each>
    </div><!-- /.container -->
</section>
<!-- Facility logos -->

<!-- Quotes  -->
<section class="block block-white vertical-padding60" id="quotes-section">
    <div class="row">

        <!-- Quotes Carousel -->
        <div class="col-sm-8 col-sm-offset-2">
            <div id="quotes" class="carousel slide no-bottom-margin">
                <!-- Carousel items -->
                <div class="carousel-inner">

                <div class="active item horizontal-padding20">
                    <div class="media">
                        <div class="media-left">
                            <div class="avatar-circle-md avatar-bordered">
                                <img src="${resource(dir:'images/quotes',file: 'padelcenter-stenungsund-henrik-fredin.png')}" alt="Henrik Frendin" class="img-responsive">
                            </div>
                        </div><!-- ./media-left -->
                        <div class="media-body">
                            <blockquote>
                                <g:message code="home.getmatchi.stenungsundpadel.quote"/>
                                <footer><cite title="Henrik Frendin, Padelcenter Stengungsund">Henrik Frendin</cite>, <g:message code="home.getmatchi.stenungsundpadel.referer"/> <a href="https://www.padelcenterstd.se/" target="_blank" class="bold">Padelcenter Stenungsund</a></footer>
                            </blockquote>
                        </div><!-- ./media-body -->
                    </div><!-- ./media -->
                </div><!-- ./item -->

                    <div class="item horizontal-padding20">
                        <div class="media">
                            <div class="media-left">
                                <div class="avatar-circle-md avatar-bordered">
                                    <img src="${resource(dir:'images/quotes',file: 'malmo-badminton-center-kristoffer-lundqvist.png')}" alt="Kristoffer Lundqvist" class="img-responsive">
                                </div>
                            </div><!-- ./media-left -->
                            <div class="media-body">
                                <blockquote>
                                    <g:message code="home.getmatchi.message8"/>
                                    <!-- Vi och våra kunder är väldigt nöjda och har vi frågor är MATCHi alltid flexibla och lyhörda! -->
                                    <footer><cite title="hallchef Malmö BadmintonCenter">Kristoffer Lundqvist</cite>, <g:message code="home.getmatchi.message9"/> <a href="http://malmobadmintoncenter.se/" target="_blank" class="bold">Malmö BadmintonCenter</a></footer>
                                </blockquote>
                            </div><!-- ./media-body -->
                        </div><!-- ./media -->
                    </div><!-- ./item -->

                    <div class="item horizontal-padding20">
                        <div class="media">
                            <div class="media-left">
                                <div class="avatar-circle-md avatar-bordered">

                                    <img src="${resource(dir:'images/quotes',file: 'gltk-ulf-borjeson.png')}" alt="Ulf Börjeson" class="img-responsive">
                                </div>
                            </div><!-- ./media-left -->
                            <div class="media-body">
                                <blockquote>
                                    <g:message code="home.getmatchi.message10"/>
                                    <footer><cite title="klubbdirektör GLTK">Ulf Börjeson</cite>, <g:message code="home.getmatchi.message11"/> <a href="http://www5.idrottonline.se/GoteborgsLawnTK-Tennis/" target="_blank" class="bold">GLTK</a></footer>
                                </blockquote>
                            </div><!-- ./media-body -->
                        </div><!-- ./media -->
                    </div><!-- ./item -->

                    <div class="item horizontal-padding20">
                        <div class="media">
                            <div class="media-left">
                                <div class="avatar-circle-md avatar-bordered">
                                    <img src="${resource(dir:'images/quotes',file: 'aby-badmintonhall-henric-andersson.png')}" alt="Henric Andersson" class="img-responsive">
                                </div>
                            </div><!-- ./media-left -->
                            <div class="media-body">
                                <blockquote>
                                    <g:message code="home.getmatchi.message12"/>
                                    <footer><cite title="styrelseledamot Åby Badmintonhall">Henric Andersson</cite>, <g:message code="home.getmatchi.message13"/> <a href="http://abybadmintonhall.se/" target="_blank" class="bold">Åby Badmintonhall</a></footer>
                                </blockquote>
                            </div><!-- ./media-body -->
                        </div><!-- ./media -->
                    </div><!-- ./item -->

                    <div class="item horizontal-padding20">
                        <div class="media">
                            <div class="media-left">
                                <div class="avatar-circle-md avatar-bordered">
                                    <img src="${resource(dir:'images/quotes',file: 'avesta-tk-olle-ekengren.png')}" alt="Olle Ekengren" class="img-responsive">
                                </div>
                            </div><!-- ./media-left -->
                            <div class="media-body">
                                <blockquote>
                                    <g:message code="home.getmatchi.message14"/>
                                    <footer><cite title="kassör och sekreterare, Avesta TK">Olle Ekengren</cite>, <g:message code="home.getmatchi.message15"/>, <a href="http://www1.idrottonline.se/AvestaTK-Tennis/" target="_blank" class="bold">Avesta TK</a></footer>
                                </blockquote>
                            </div><!-- ./media-body -->
                        </div><!-- ./media -->
                    </div><!-- ./item -->
                </div><!-- /.Carousel items -->

                <!-- Indicators -->
                <ol class="carousel-indicators">
                    <li data-target="#quotes" data-slide-to="0" class="active"></li>
                    <li data-target="#quotes" data-slide-to="1"></li>
                    <li data-target="#quotes" data-slide-to="2"></li>
                    <li data-target="#quotes" data-slide-to="3"></li>
                    <li data-target="#quotes" data-slide-to="4"></li>
                </ol>
            </div><!-- /.#quotes -->
        </div><!-- /.col-sm-8 -->

    </div><!-- /.row -->
</section>
<!-- /.Quotes -->

<!-- Section 1 - Vi deliver... -->
<section class="block bg-brand vertical-padding60">
    <div class="container">
        <div class="row">
            <div class="col-sm-8 col-sm-offset-2">
                <p class="lead">
                    <g:message code="home.getmatchi.message16"/>
                </p>

                <p>
                    <g:message code="home.getmatchi.message17"/>
                </p>
            </div>
        </div>
    </div>
</section>

<!-- Reason 1 -->
<section class="block block-grey vertical-padding60 no-top-border">
    <div class="container">
        <div class="row">

            <div class="col-sm-6 vertical-padding20">
                <div class="center-block wp5" style="max-width:500px;">
                    <img src="${resource(dir:'images', file:'matchi-desktop-ios-showcase-presentation.png' )}" class="center-block img-responsive">
                </div>
            </div>

            <div class="col-sm-6">
                <div>
                    <h2 class="no-bottom-padding"><g:message code="home.getmatchi.message18"/></h2>
                    <p class="lead"><g:message code="home.getmatchi.message19"/></p>
                </div>
                <ul class="checked">
                    <li><g:message code="home.getmatchi.message20"/></li>
                    <li><g:message code="home.getmatchi.message21"/></li>
                    <li><g:message code="home.getmatchi.message22"/></li>
                    <li><g:message code="home.getmatchi.message23"/></li>
                </ul>
            </div>

        </div><!-- /.row -->
    </div><!-- /.container -->
</section><!-- /.Reason 1 -->

<!-- Reason 2 -->
<section class="block block-white vertical-padding60 no-top-border">
    <div class="container">
        <div class="row">

            <div class="col-sm-6 vertical-padding20">
                <div>
                    <h2 class="no-bottom-padding"><g:message code="home.getmatchi.message24"/></h2>
                    <p class="lead"><g:message code="home.getmatchi.message25"/></p>
                </div>
                <ul class="checked">
                    <li><g:message code="home.getmatchi.message26"/></li>
                    <li><g:message code="home.getmatchi.message27"/></li>
                    <li><g:message code="home.getmatchi.message28"/></li>
                    <li><g:message code="home.getmatchi.message29"/></li>
                </ul>
            </div>

            <div class="col-sm-6">
                <div class="center-block wp3" style="max-width:400px;">
                    <img src="${resource(dir:'images', file:'get-matchi-promo.png' )}" class="center-block img-responsive">
                </div>
            </div>

        </div><!-- /.row -->
    </div><!-- /.container -->
</section><!-- /.Reason 2 -->

<!-- Reason 3 -->
<section class="block block-grey vertical-padding60 no-top-border">
    <div class="container">
        <div class="row">

            <div class="col-sm-6">
                <div class="center-block wp5" style="max-width:500px;">
                    <img src="${resource(dir:'images', file:'get-matchi-promo-3.png' )}" class="img-responsive vertical-padding20">
                </div>
            </div>

            <div class="col-sm-6vertical-padding20">
                <div>
                    <h2 class="no-bottom-padding"><g:message code="home.getmatchi.message30"/></h2>
                    <p class="lead"><g:message code="home.getmatchi.message31"/></p>
                </div>
                <ul class="checked">
                    <li><g:message code="home.getmatchi.message32"/></li>
                    <li><g:message code="home.getmatchi.message33"/></li>
                    <li><g:message code="home.getmatchi.message34"/></li>
                    <li><g:message code="home.getmatchi.message35"/></li>
                </ul>
            </div>
        </div>
    </div>
</section>
<!-- /Reason 3 -->

<!-- Rest of reasons -->
<section class="block block-white vertical-padding60">
    <div class="container">
        <h2 class="vertical-padding20 text-center"><g:message code="home.getmatchi.message36"/></h2>

        <div class="row no-margin">

            <div class="col-sm-6">

                <dl class="checked">
                    <dt><g:message code="home.getmatchi.message37"/></dt>
                    <dd>
                        <g:message code="home.getmatchi.message38"/>
                    </dd>

                    <dt><g:message code="home.getmatchi.message39"/></dt>

                    <dd>
                        <g:message code="home.getmatchi.message40"/>
                    </dd>

                    <dt><g:message code="home.getmatchi.message41"/></dt>
                    <dd>
                        <g:message code="home.getmatchi.message42"/>
                    </dd>

                    <dt><g:message code="home.getmatchi.message43"/></dt>
                    <dd>
                        <g:message code="home.getmatchi.message44"/>
                    </dd>

                </dl>

            </div><!-- /.col-sm-6 -->

            <div class="col-sm-6">

                <dl class="checked">

                    <dt><g:message code="home.getmatchi.message45"/></dt>
                    <dd>
                        <g:message code="home.getmatchi.message46"/>
                    </dd>

                    <dt><g:message code="home.getmatchi.message47"/></dt>
                    <dd>
                        <g:message code="home.getmatchi.message48"/>
                    </dd>

                    <dt><g:message code="home.getmatchi.message49"/></dt>
                    <dd>
                        <g:message code="home.getmatchi.message50"/>
                    </dd>

                </dl>

            </div><!-- /.col-sm-6 -->

        </div><!-- /.row -->
    </div><!-- /.container -->
</section><!-- /.Rest of reasons -->

<!-- Contact form -->
<section class="block block-grey text-center vertical-padding60" id="getInTouch">
    <div class="container">

        <div class="interestFormContainer vertical-padding20">
            <h2 class="bottom-padding20">
                <g:message code="home.getmatchi.message51"/>
            </h2>

            <g:form id="interestForm" name="interestForm" class="form-inline" role="form" action="interested">
                <div class="form-group relative">
                    <g:textField class="form-control text" name="name" placeholder="${message(code: 'interestedCommand.name.placeholder')}" />
                </div>
                <div class="form-group relative">
                    <g:textField class="form-control required email" id="email" name="email" placeholder="${message(code: 'interestedCommand.email.placeholder')}" />
                </div>
                <div class="form-group relative">
                    <g:textField class="form-control required" name="facility" placeholder="${message(code: 'home.getmatchi.message54')}" />
                </div>
                <div class="form-group relative">
                    <g:textField class="form-control number" name="phone" placeholder="${message(code: 'default.phoneNumber.label')}" />
                </div>
                <button type="submit" class="btn btn-small btn-success btn-outline"><g:message code="button.smash.label"/></button>
            </g:form>
            <div class="space-10"></div>
        </div>
    </div><!-- /.container -->
</section><!-- /.Contact form -->

<!-- ClickTale Bottom part -->
<div id="ClickTaleDiv" style="display: none;"></div>
<script type="text/javascript">
    if(document.location.protocol!='https:')
        document.write(unescape("%3Cscript%20src='http://s.clicktale.net/WRe0.js'%20type='text/javascript'%3E%3C/script%3E"));

    if(typeof ClickTale=='function') ClickTale(20340,1,"www14");
</script>
<!-- ClickTale end of Bottom part -->

</body>

<r:script>
    $(document).ready(function() {
        $('.carousel').carousel({
            interval: 6000
        });

        $("#interestForm").validate({
            errorPlacement: function(error, element) { },
            highlight: function (element, errorClass) {
                $(element).addClass("invalid");
                $(element).after( '<i class="fas fa-times validation-icon"></i>');
                $(".fa-check").hide();
                <!-- $(element).css("background-color","#FFE5DA"); -->

            },
            unhighlight: function (element, errorClass) {
                $(element).addClass("valid");
                $(element).after( '<i class="fas fa-check validation-icon"></i>');
                $(".fa-times").hide();
                <!-- $(element).css("background-color","#CFFFCD"); -->
            }
        });

        $("#call2action").click(function() {
            $('html, body').animate({
                scrollTop: $("#getInTouch").offset().top
            }, 2000);
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
    });
</r:script>

</html>
