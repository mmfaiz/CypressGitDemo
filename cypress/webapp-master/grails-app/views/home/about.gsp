<html>
<head>
    <title><g:message code="home.about.message1"/></title>
    <meta name="layout" content="b3main" />
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
    <meta name="classes" content="splash-page"/>
    <r:require modules="leaflet-open-maps"/>
</head>
<body>

<r:script type="text/javascript">

    $(function() {
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

        var position = L.latLng(57.700437, 12.025141);

        L.marker(position, {icon: mIcon}).addTo(map);
        map.setView(position, 12);
    });
</r:script>

<!-- Image -->
<section class="block">
    <div class="splash splash-container about">
        <div class="splash-inner">
            <div class="stripe">
                <div class="container">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12">

                            <!-- BRAGGING SECTION -->
                            <section id="site-stats" class="block text-black text-shadow">
                                <div class="container">
                                    <g:topFooterStats/>
                                </div>
                            </section>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section><!-- /.Image -->

<section class="block block-white">
    <div class="container vertical-padding30">
        <div class="row">
            <div class="col-sm-8">
                <h1><g:message code="home.about.message2"/></h1>
                <p class="text-justify"><g:message code="home.about.message2a"/></p>
                <p class="text-justify"><g:message code="home.about.message2b"/></p>
                <p class="text-justify"><g:message code="home.about.message2c"/></p>

                <h3 class="top-margin40"><g:message code="home.about.message3"/></h3>
                <p class="text-justify"><g:message code="home.about.message3a"/></p>
                <p class="text-justify"><g:message code="home.about.message3b"/></p>
                <p class="text-justify"><g:message code="home.about.message3c"/></p>

                <h3 class="top-margin40"><g:message code="home.about.message4"/></h3>
                <p class="text-justify"><g:message code="home.about.message4a"/></p>
                <p class="text-justify"><g:message code="home.about.message4b"/></p>
                <p class="text-justify"><g:message code="home.about.message4c"/></p>

                <h3 class="top-margin40"><g:message code="home.about.message5"/></h3>

                <h4 class="top-margin20"><g:message code="home.about.message6"/></h4>
                <p class="text-justify"><g:message code="home.about.message6a"/></p>
                <p class="text-justify"><a href="https://www.tennis.se/spela-tennis/hitta-din-bana/"><g:message code="home.about.message6b"/></a></p>

                <h4 class="top-margin20"><g:message code="home.about.message7"/></h4>
                <p class="text-justify"><g:message code="home.about.message7a"/></p>
                <p class="text-justify"><a href="http://smash.matchi.se"><g:message code="home.about.message7b"/></a></p>

                <h4 class="top-margin20"><g:message code="home.about.message8"/></h4>
                <p class="text-justify"><g:message code="home.about.message8a"/></p>
                <p class="text-justify"><a href="http://tennisosterlen.se/"><g:message code="home.about.message8b"/></a></p>

                <p class="top-margin20">
                    <a href="https://www.tennis.se"><img src="${resource(dir:'/images/partners', file:'sv-tennis-association.png' )}" /></a>
                    <a href="https://svenskpadel.se"><img src="${resource(dir:'/images/partners', file:'padel-sweden.png' )}" /></a>
                    <a href="http://tennisosterlen.se/"><img src="${resource(dir:'/images/partners', file:'tennis-osterlen.png' )}" /></a>
                </p>

            </div><!-- ./col-sm-8 -->
            <div class="col-sm-4">

                <div class="fb-like-box" data-href="https://www.facebook.com/matchisports" data-colorscheme="light" data-show-faces="true" data-header="false" data-stream="false" data-show-border="false"></div>

                <blockquote>
                    <p>
                        <em>&ldquo;<g:message code="home.about.message11"/>&rdquo;</em>
                    </p>
                    <small>Magnus ”Gusten” Gustafsson<br><g:message code="home.about.message20"/></small>
                </blockquote>

                <blockquote>
                    <p>
                        <em>&ldquo;<g:message code="home.about.message14"/>&rdquo;</em>
                    </p>
                    <small>Kristoffer Lundqvist<br><g:message code="home.about.message15"/></small>
                </blockquote>

            </div><!-- ./col-sm-4 -->
        </div><!-- ./row -->

    </div><!-- ./container -->
</section>

<section class="block block-white">
    <div class="container vertical-padding30">
        <div class="row">
            <div class="col-sm-8">
                <div class="canvas-wrapper">
                    <div id="map-canvas">

                    </div>
                </div>
            </div>

            <div class="col-sm-4">
                <h4 class="text-muted bottom-padding10 bottom-border"><i class="fas fa-comments"></i> <g:message code="default.contactUs.label"/></h4>

                <address>
                    <strong>MATCHi AB</strong><br>
                    <abbr title="${message(code: 'default.address.label')}"><i class="fas fa-map-marker"></i></abbr> <g:message code="default.matchi.address.street"/><br>
                    <g:message code="default.matchi.address.zip"/> <g:message code="default.matchi.address.city"/><br>
                </address>
                <address>
                    <abbr title="${message(code: 'home.about.message17')}"><i class="fas fa-phone"></i></abbr>
                    <a href="teL:+460313807200">+46 (0)31-380 72 00</a><br>
                    <abbr title="${message(code: 'default.email.label')}"><i class="fas fa-envelope"></i></abbr>
                    <a href="mailto:info@matchi.se">info@matchi.se</a>
                </address>
            </div>
        </div>
    </div>
</section>
</body>
</html>
