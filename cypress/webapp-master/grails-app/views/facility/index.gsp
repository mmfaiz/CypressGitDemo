<%@ page import="com.matchi.MFile; com.matchi.Facility; com.matchi.Municipality; com.matchi.Sport; com.matchi.Region" %>
<head>
    <meta name="layout" content="b3main" />
    <title><g:message code="facility.index.message1"/></title>
    <meta name="classes" content="splash-page splash-page-no-mobile"/>
    <r:require modules="leaflet-open-maps, matchi-truncate"/>
</head>
<body>
<!-- MAP -->
<div id="map_canvas" class="hidden-xs full-width-map splash-container" style="position: relative;overflow: hidden"></div>

<!-- SEARCH -->
<section class="block block-grey vertical-padding20">
    <div class="container">
        <h2><g:message code="facility.index.message2"/></h2>
        <div id="facilityListInfo" class="alert alert-info" style="display: none;">
            <button type="button" class="close" onclick="removeFacilityInfoMessage()" data-dismiss="alert" rel="tooltip" title="<g:message code="default.do.not.show.again"/>"><i class="fa fa-times-circle"></i><span class="sr-only"><g:message code="button.close.label"/></span></button>
            <p>
                <span class="fa-stack fa-lg">
                    <i class="fas fa-circle fa-stack-2x"></i>
                    <i class="fa fa-lightbulb-o fa-stack-1x fa-inverse"></i>
                </span>
                <g:message code="facility.index.message3"/>
            </p>
        </div>
        <hr>
        <g:formRemote name="findFacilityForm" class="form" url="[action: 'findFacilities']"
                      update="facilities-result" before="startSearch(this)" after="finishedSearch(this)">
            <g:hiddenField name="lat" value=""/>
            <g:hiddenField name="lng" value=""/>
            <g:hiddenField name="offset" value="${cmd.offset ?: 0}"/>

            <div class="row">
                <div class="col-sm-4 form-group">
                    <g:searchFacilityInput name="q" placeholder="${message(code: 'book.index.search.placeholder')}" class="form-control" value="${cmd?.q}"/>
                </div>
                <div class="col-sm-3 form-group">
                    <select id="municipality" name="municipality" data-style="form-control" data-live-search="true" style="display: none;">
                        <option value=""><g:message code="municipality.select.all"/></option>
                        <g:each in="${regions}">
                            <optgroup label="${it.name}">
                                <g:each in="${it.municipalities}" var="mun">
                                    <option value="${mun.id}" ${cmd.municipality == mun.id ? "selected" : ""}>${mun.name} (${mun.numFacilities})</option>
                                </g:each>
                            </optgroup>
                        </g:each>
                    </select>
                </div>
                <div class="col-sm-3 form-group">
                    <select id="sport" name="sport" data-style="form-control">
                        <option value=""><g:message code="sport.select.all"/></option>
                        <g:each in="${sports}">
                            <option value="${it.id}" ${cmd.sport == it.id ? "selected" : ""}
                                    data-content="<i class='ma ma-${it.id}'></i> <g:message code="sport.name.${it.id}" />"><g:message code="sport.name.${it.id}" /></option>
                        </g:each>
                    </select>
                </div>
                <div class="col-sm-2">
                    <g:submitButton name="submit" value="${message(code: 'button.search.label')}" class="btn btn-success btn-block"/>
                </div>
            </div>
        </g:formRemote>
    </div>
</section>

<!-- SEARCH RESULTS / FACILITIES -->
<section class="block block-white facility-list vertical-padding20">
    <div id="findFacilitiesLoader" class="text-center vertical-padding40"
         style="width: 100%; height: 100%; top: 0;">
        <i class="fas fa-spinner fa-spin fa-3x"></i>
    </div>

    <div id="facilities-result"></div>
</section>

<div id="userBookingModal" class="modal hide fade"></div>
<div id="fb-root"></div>
<script>(function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s); js.id = id;
    js.src = "//connect.facebook.net/sv_SE/all.js#xfbml=1&appId=340585979293099";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
<r:script>
    var max = 12;
    var offset = 0;

    // Leaflet
    var map;
    var bounds;
    var mCluster;
    var mIcon = L.icon({
        iconUrl: "${g.resource(dir: 'images', file: 'map_icon_100x100.png')}",
        shadowUrl: "${g.resource(dir: 'images', file: 'map_icon_shadow.png')}",

        iconSize:     [50, 50], // size of the icon
        shadowSize:   [33, 12], // size of the shadow
        iconAnchor:   [0, 50], // point of the icon which will correspond to marker's location
        shadowAnchor: [-25, 12],  // the same for the shadow
        popupAnchor:  [25, -50] // point from which the popup should open relative to the iconAnchor
    });
    var mIconSmall = L.icon({
        iconUrl: "${g.resource(dir: 'images', file: 'map_icon_100x100.png')}",
        shadowUrl: "${g.resource(dir: 'images', file: 'map_icon_shadow.png')}",

        iconSize:     [25, 25], // size of the icon
        shadowSize:   [16.5, 6], // size of the shadow
        iconAnchor:   [0, 25], // point of the icon which will correspond to marker's location
        shadowAnchor: [-12.5, 6],  // the same for the shadow
        popupAnchor:  [12.5, -25] // point from which the popup should open relative to the iconAnchor
    });

    map = L.map('map_canvas', {
      zoomControl: false
    });

    setTimeout(map.invalidateSize.bind(map),200)

    L.control.zoom({
     position:'bottomleft'
    }).addTo(map);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 19
    }).addTo( map );


    mCluster = L.markerClusterGroup({
        maxClusterRadius: 30
    });

    var zoomLvl = ${g.forJavaScript(data: municipality?.zoomlv ?: 4)};
    map.setView([61.3, 14.87], zoomLvl);

    $(document).ready(function() {
        $("[rel=tooltip]").tooltip();

        $("#municipality").selectpicker({
            title: "${message(code: 'municipality.multiselect.noneSelectedText')}"
        });
        $("#sport").selectpicker({
            title: "${message(code: 'default.choose.sport')}"
        });
        $("[name=q]").focus();
        $(".truncate").truncateText({max:200});

        if(!getCookie("hideFacilityInfo")) {
             $("#facilityListInfo").show();
        }

        var $findFacilityForm = $('#findFacilityForm');
        /*$findFacilityForm.find("input[type='submit']").trigger('click', function() {
            $findFacilityForm.find("#offset").val("0");
        });*/


        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    $("#lat").val(position.coords.latitude);
                    $("#lng").val(position.coords.longitude);
                    $findFacilityForm.submit();
                },
                function(error) {
                    $findFacilityForm.submit();
                }
            );
        } else {
            $findFacilityForm.submit();
        }
    });

    function plotMarker(facility, small) {
        var position = L.latLng(facility.lat,facility.lng);
        //var marker   = L.marker(position, {icon: (small ? mIconSmall : mIcon)}).addTo(map);
        var marker   = L.marker(position, {icon: (small ? mIconSmall : mIcon)});

        var markerInfo = "<strong>" + facility.name + "</strong>" +
                        "<br>" + facility.address + "<br>" +
                        facility.zipcode + ", " +  facility.city + "<br>" +
                        "<div class='space5'></div>" +
                        "<a href='" + facility.shortname + "'><g:message code="facility.index.showFacility" /></a>";

        marker.bindPopup(markerInfo);
        marker.on('click', function() {
            this.openPopup()
        });

        mCluster.addLayer(marker);

        if (!small) {
            bounds.extend(position);
        }
    }

    function setCenter() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                map.setView([position.coords.latitude, position.coords.longitude]);
            }, function(error) {
                handleLocationError(true, error);
                map.setView([${g.forJavaScript(data: municipality?.lat ?: "60.71085972180809")}, ${g.forJavaScript(data: municipality?.lng ?: "14.8974609375")}]);
            }, { timeout: 3000 });
        } else {
            handleLocationError(false);
        }
    }

    function handleLocationError(browserHasGeolocation, error) {
        if (!browserHasGeolocation) {
            alert('Error: Your browser doesn\'t support geolocation.');
            return
        }

        switch(error.code) {
            case error.PERMISSION_DENIED:
                alert('User denied the request for Geolocation');
                break;
            case error.POSITION_UNAVAILABLE:
                alert('Location information is unavailable');
                break;
            case error.TIMEOUT:
                alert('The request to get user location timed out');
                break;
            case error.UNKNOWN_ERROR:
                alert('An unknown error occurred');
                break;
            default:
                alert('Geolocation service failed');
                break;
        }
    }

    function finishedSearch() {
        map.addLayer(mCluster);

        $('#findFacilitiesLoader').hide();
        $('#facilities-result').show();
        $('#findFacilityForm').find("input[name='submit']").removeAttr('disabled');
    }

    function startSearch() {
        mCluster.clearLayers();
        bounds = L.latLngBounds();

        $('#facilities-result').hide();
        $('#findFacilitiesLoader').show();
        $('#findFacilityForm').find("input[name='submit']").attr('disabled','');
    }
</r:script>
</body>
