<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title>${ region } - <g:message code="adminRegion.edit.region"/></title>

    <r:script>
        var map = null;
        var geocoder = null;
        var marker = null;

        $(document).ready(function() {
            var mapOptions = {
                center: new google.maps.LatLng(${g.forJavaScript(data: cmd?.lat ?: region?.lat)}, ${g.forJavaScript(data: cmd?.lng ?: region?.lng)}),
                zoom: ${g.forJavaScript(data: cmd?.zoomlv ?: region?.zoomlv ?: 13 )},
                mapTypeId: google.maps.MapTypeId.ROADMAP
            };

            map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
            geocoder = new google.maps.Geocoder();

            marker = new google.maps.Marker({
                position: new google.maps.LatLng(${g.forJavaScript(data: cmd?.lat ?: region?.lat)}, ${g.forJavaScript(data: cmd?.lng ?: region?.lng)}),
                map: map,
                title: "${g.forJavaScript(data: cmd?.name ?: region.name ?: "")}",
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
                        alert("${message(code: 'adminRegion.editMunicipality.geocodeError')}: " + status);
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
    <li class="active"><g:message code="adminRegion.edit.editRegion"/></li>
</ul>

<g:form name="regionForm" action="update" method="post" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${cmd?.id ?: region.id}"/>
    <g:hiddenField name="zoomlv" value="${cmd?.zoomlv ?: region.zoomlv}"/>

    <div class="form-header">
        <g:message code="adminRegion.edit.editRegion"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean: cmd, field:'name', 'error')}">
            <label class="control-label" for="name"><g:message code="default.name.label"/></label>
            <div class="controls">
                <g:textField name="name" value="${cmd?.name ?: region?.name}" onchange="showAddress()" class="span8" tabindex="1"/>
            </div>
        </div>
        <hr>
        <div class="control-group ${hasErrors(bean: cmd, field:'country', 'error')}">
            <label class="control-label" for="country"><g:message code="default.country.label"/>*</label>
            <div class="controls">
                <g:select name="country" from="${grailsApplication.config.matchi.settings.facility.countries}"
                          optionKey="key" optionValue="value" value="${cmd?.country ?: region?.country}" onchange="showAddress()"
                          class="form-control"/>
            </div>
        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'lat', 'error')}">
            <label class="control-label" for="lat"><g:message
                    code="default.latitude.label" default="Latitude" /></label>
            <div class="controls">
                <g:textField id="lat" name="lat" value="${cmd?.lat ?: region.lat ?: ""}" onchange="moveMarker()" class="span3" tabindex="2"/>
            </div>

        </div>
        <div class="control-group ${hasErrors(bean: cmd, field:'lng', 'error')}">
            <label class="control-label" for="lng"><g:message code="default.longitude.label" default="Longitude" /></label>
            <div class="controls">
                <g:textField id="lng" name="lng" value="${cmd?.lng ?: region.lng ?: ""}" onchange="moveMarker()" class="span3" tabindex="3"/>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label"><g:message code="default.map.label" default="Karta" /></label>
            <div class="controls">
                <div id="map_canvas" class="pull-left" style="width: 666px; height: 400px;"></div>
            </div>
        </div>
        <div class="form-actions">
            <g:submitButton name="save" value="${g.message(code: 'button.save.label')}" class="btn btn-success" tabindex="4"/>
            <g:actionSubmit onclick="return confirm('${message(code: 'adminRegion.edit.delete.confirm')}')"
                                                    action="delete" id="${region.id}" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse" tabindex="5"/>
            <g:link action="index" class="btn btn-danger" tabindex="6"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<div class="action-bar">
    <div class="btn-toolbar-left">
        <p>Kommuner (${region.municipalities.size()} st)</p>
    </div>
    <div class="btn-toolbar-right">
        <g:link class="btn btn-inverse" action="createMunicipality" id="${cmd?.id ?: region.id}"><g:message code="adminRegion.edit.addToMunicipality"/></g:link>
    </div>
</div>
<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <th width="150"><g:message code="default.name.label"/></th>
        <th class="center-text"><g:message code="default.latitude.label"/></th>
        <th class="center-text"><g:message code="default.longitude.label"/></th>
        <th class="center-text"><g:message code="adminRegion.index.zoom"/></th>
        <th width="60" class="center-text"><g:message code="button.edit.label"/></th>
    </tr>
    </thead>


    <tbody>
    <g:if test="${region.municipalities.size() < 1}">
        <tr>
            <td colspan="4"><i><g:message code="adminRegion.edit.noMunicipalities"/></i></td>
        </tr>
    </g:if>
    <g:each in="${region.municipalities}" var="municipality">
        <tr>
            <td><g:link action="editMunicipality" id="${municipality.id}">${municipality.name}</g:link></td>
            <td class="center-text">${municipality.lng}</td>
            <td class="center-text">${municipality.lat}</td>
            <td class="center-text">${municipality.zoomlv}</td>
            <td class="center-text"><g:link action="editMunicipality" params="[id: municipality.id]"><img src="${resource(dir:'images', file:'edit_btn.png')}"/></g:link></td>
        </tr>
    </g:each>

    </tbody>
</table>

<g:render template="/templates/googleMaps"/>
</body>
</html>
