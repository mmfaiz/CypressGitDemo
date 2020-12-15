<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title>${ municipality } - <g:message code="municipality.label"/></title>

    <r:script>
        var map = null;
        var geocoder = null;
        var marker = null;

        $(document).ready(function() {
            var mapOptions = {
                center: new google.maps.LatLng(${g.forJavaScript(data: cmd?.lat ?: municipality?.lat)}, ${g.forJavaScript(data: cmd?.lng ?: municipality?.lng)}),
                zoom: ${g.forJavaScript(data: cmd?.zoomlv ?: municipality?.zoomlv)},
                mapTypeId: google.maps.MapTypeId.ROADMAP
            };

            map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
            geocoder = new google.maps.Geocoder();

            marker = new google.maps.Marker({
                position: new google.maps.LatLng(${g.forJavaScript(data: cmd?.lat ?: municipality?.lat)}, ${g.forJavaScript(data: cmd?.lng ?: municipality?.lng)}),
                map: map,
                title: "${g.forJavaScript(data: cmd?.name ?: municipality.name ?: "")}",
                draggable: true
            });

            google.maps.event.addListener(marker, "dragend", function() {
                $("input[name=lat]").val(marker.getPosition().lat());
                $("input[name=lng]").val(marker.getPosition().lng());
                var location = new google.maps.LatLng($("input[name=lat]").val(), $("input[name=lng]").val());

                var infowindow = new google.maps.InfoWindow({
                    content: marker.getPosition().toUrlValue(6),
                    size: new google.maps.Size(50,50)
                });

                infowindow.open(map, marker);
                map.setCenter(location);
            });

            google.maps.event.addListener(marker, "click", function() {
                var infowindow = new google.maps.InfoWindow({
                    content: marker.getPosition().toUrlValue(6),
                    size: new google.maps.Size(50,50)
                });

                infowindow.open(map, marker);
            });

            google.maps.event.addListener(map, 'zoom_changed', function() {
                var zoomLevel = map.getZoom();
                $("[name=zoomlv]").val(zoomLevel);
                infowindow.setContent('Zoom: ' + zoomLevel);
            });
        });

        function showAddress() {
            var address = $("input[name=name]").val();

            if( geocoder ) {
                geocoder.geocode({ 'address': address }, function(results, status) {
                    if (status == google.maps.GeocoderStatus.OK) {
                        var location = results[0].geometry.location;
                        var zoom = $("[name=zoomlv]").val();

                        //map.setZoom(zoom);
                        map.setCenter(location);
                        marker.setPosition(location);

                        $("input[name=lat]").val(marker.getPosition().lat());
                        $("input[name=lng]").val(marker.getPosition().lng());
                    } else {
                        alert("${message(code: 'adminRegion.editMunicipality.geocodeError')} " + status);
                    }
                });
            }
        }

        function moveMarker() {
            var location = new google.maps.LatLng($("input[name=lat]").val(), $("input[name=lng]").val());

            map.setCenter(location);
            marker.setPosition(location);
        }
    </r:script>
</head>
<body>
<g:errorMessage bean="${cmd}"/>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="region.label.plural"/></g:link><span class="divider">/</span></li>
    <li><g:link action="edit" id="${cmd?.regionId ?: municipality.regionId}"><g:message code="adminRegion.edit.editRegion"/></g:link><span class="divider">/</span></li>
    <li class="active"><g:message code="adminRegion.editMunicipality.editMunicipality"/></li>
</ul>

<g:form name="municipalityForm" action="updateMunicipality" method="post" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${cmd?.id ?: municipality.id}"/>
    <g:hiddenField name="regionId" value="${cmd?.regionId ?: municipality.regionId}"/>
    <g:hiddenField name="zoomlv" value="${cmd?.zoomlv ?: municipality.zoomlv ?: ""}"/>

    <div class="form-header">
        <g:message code="adminRegion.editMunicipality.editMunicipality"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean: cmd, field:'name', 'error')}">
            <label class="control-label" for="name"><g:message code="default.name.label"/></label>
            <div class="controls">
                <g:textField name="name" value="${cmd?.name ?: municipality?.name}" onchange="showAddress()" class="span8" tabindex="1"/>
            </div>
        </div>
        <hr>
        <div class="control-group ${hasErrors(bean: cmd, field:'lat', 'error')}">
            <label class="control-label" for="lat"><g:message
                    code="default.latitude.label" default="Latitude" /></label>
            <div class="controls">
                <g:textField id="lat" name="lat" value="${cmd?.lat ?: municipality.lat ?: ""}" onchange="moveMarker()" class="span3" tabindex="2"/>
            </div>

        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'lng', 'error')}">
            <label class="control-label" for="lng"><g:message code="default.longitude.label" default="Longitude" /></label>
            <div class="controls">
                <g:textField id="lng" name="lng" value="${cmd?.lng ?: municipality.lng ?: ""}" onchange="moveMarker()" class="span3" tabindex="3"/>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label"><g:message code="default.map.label" default="Karta" /></label>
            <div class="controls">
                <div id="map_canvas" class="pull-left" style="width: 666px; height: 400px;"></div>
            </div>
        </div>
        <div class="form-actions">
            <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success" tabindex="4"/>
            <g:actionSubmit onclick="return confirm('${message(code: 'adminRegion.editMunicipality.delete.confirm')}')"
                                                    action="deleteMunicipality" id="${municipality.id}" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse" tabindex="5"/>
            <g:link action="edit" id="${cmd?.regionId ?: municipality?.regionId}" class="btn btn-danger" tabindex="6"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>

<g:render template="/templates/googleMaps"/>
</body>
</html>
