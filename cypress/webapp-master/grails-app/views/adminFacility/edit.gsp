<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<%@ page import="com.matchi.FacilityProperty; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminFacility.edit.title"/></title>
    <r:require modules="matchi-customerselect"/>
</head>
<body>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li class="active"><g:message code="adminFacility.edit.title"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${facility}"/>

    <div class="panel panel-default">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 0]"/>

        <g:uploadForm action="update" id="${facility.id}" name="facilityForm" class="form panel-body no-top-padding" enctype="application/x-www-form-urlencoded;charset=UTF-8">
            <div class="row well no-bottom-margin">
                <div class="form-group col-sm-6">
                    <label for="name"><g:message code="facility.name.label"/>*</label>
                    <g:textField name="name" value="${facility?.name}" class="form-control"/>
                </div>
                <div class="form-group col-sm-6">
                    <label for="shortname"><g:message code="facility.shortname.label"/>*</label>
                    <g:textField name="shortname" value="${facility?.shortname}" class="form-control"/>
                </div>

                <div class="form-group col-sm-12">
                    <label for="description"><g:message code="facility.description.label"/>*</label>
                    <g:textArea id="description" rows="5" cols="" name="description" value="${facility?.description}" class="form-control"/>
                </div>
            </div>

            <div class="row well">
                <div class="form-group col-sm-6">
                    <label for="address"><g:message code="facility.address.label"/>*</label>
                    <g:textField name="address" value="${facility?.address}" onchange="preProssesAddress()" class="form-control"/>
                </div>
                <div class="form-group col-sm-6">
                    <label for="zipcode"><g:message code="facility.zipcode.label"/></label>
                    <g:textField name="zipcode" value="${facility?.zipcode}" class="form-control"/>
                </div>

                <div class="form-group col-sm-3">
                    <label for="city"><g:message code="facility.city.label"/></label>
                    <g:textField name="city" value="${facility?.city}" class="form-control" />
                </div>
                <div class="form-group col-sm-3">
                    <label for="municipality"><g:message code="facility.municipality.label"/>*</label>
                    <select id="municipality" name="municipality" class="chzn-select form-control" style="width: 230px;">
                        <g:each in="${regions}">
                            <optgroup label="${it.name}">
                                <g:each in="${it.municipalities}" var="municipality">
                                    <option value="${municipality.id}" ${facility.municipality.id == municipality.id ? "selected" : ""}>${municipality.name}</option>
                                </g:each>
                            </optgroup>
                        </g:each>
                    </select>
                </div>
                <div class="form-group col-sm-3">
                    <label for="country"><g:message code="facility.country.label"/></label>
                    <g:select name="country" from="${grailsApplication.config.matchi.settings.facility.countries}"
                              optionKey="key" optionValue="value" value="${facility?.country}" onchange="preProssesAddress()"
                              class="form-control"/>
                </div>
                <div class="form-group col-sm-3">
                    <label for="language"><g:message code="facility.language.label"/></label>
                    <g:select name="language" from="${grailsApplication.config.i18n.availableLanguages}"
                            optionKey="key" optionValue="value" value="${facility?.language}" class="form-control"/>
                </div>
                <div class="form-group col-sm-3">
                    <label for="currency"><g:message code="facility.currency.label"/></label>
                    <g:select name="currency" from="${grailsApplication.config.matchi.settings.currency.keySet()}"
                            value="${facility?.currency}" class="form-control"/>
                </div>

                <div class="form-group col-sm-6">
                    <label for="lat"><g:message code="facility.latitude.label"/>*</label>
                    <g:textField id="lat" name="lat" value="${facility?.lat}" onchange="moveMarker()" class="form-control"/>
                </div>
                <div class="form-group col-sm-6">
                    <label for="lng"><g:message code="facility.longitude.label"/>*</label>
                    <g:textField id="lng" name="lng" value="${facility?.lng}" onchange="moveMarker()" class="form-control"/>
                </div>
                <div class="form-group col-sm-6">
                    <label for="salesPerson"><g:message code="facility.salesPerson.label"/></label>
                    <g:textField name="salesPerson" value="${facility?.salesPerson}" class="form-control"/>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12">
                    <label class="control-label"><g:message code="default.map.label"/></label>
                    <div id="map_canvas" class="pull-left" style="width: 1078px; height: 400px;"></div>
                </div>
            </div>

            <div class="row well no-bottom-margin">
                <div class="form-group col-sm-6">
                    <label for="telephone"><g:message code="facility.telephone.label"/></label>
                    <g:textField name="telephone" value="${facility?.telephone}" class="form-control"/>
                </div>
                <div class="form-group col-sm-6">
                    <label for="fax"><g:message code="facility.fax.label"/></label>
                    <g:textField name="fax" value="${facility?.fax}" class="form-control"/>
                </div>
                <div class="form-group col-sm-12">
                    <label for="telephone"><g:message code="facility.email.label"/></label>
                    <g:textField name="email" value="${facility?.email}" class="form-control"/>
                </div>
            </div>

            <div class="row well">
                <div class="form-group col-sm-6">
                    <label for="plusgiro"><g:message code="facility.plusgiro.label"/></label>
                    <g:textField name="plusgiro"
                                 value="${facilityProperty(property: com.matchi.FacilityProperty.FacilityPropertyKey.PAYOUT_PLUSGIRO.name(), facility: facility)}"
                                 class="form-control" />
                </div>
                <div class="form-group col-sm-6">
                    <label for="bankgiro"><g:message code="facility.bankgiro.label"/></label>
                    <g:textField name="bankgiro"
                                 value="${facilityProperty(property: com.matchi.FacilityProperty.FacilityPropertyKey.PAYOUT_BANKGIRO.name(), facility: facility)}"
                                 class="form-control"/>
                </div>

                <div class="form-group col-sm-6">
                    <label for="iban"><g:message code="facility.iban.label"/></label>
                    <g:textField name="iban" value="${facility?.iban}" class="form-control" />
                </div>

                <div class="form-group col-sm-6">
                    <label for="bic"><g:message code="facility.bic.label"/></label>
                    <g:textField name="bic" value="${facility?.bic}" class="form-control" />
                </div>

                <div class="form-group col-sm-12">
                    <label for="orgnr"><g:message code="facility.orgnr.label"/></label>
                    <g:textField name="orgnr" value="${facility?.orgnr}" class="form-control" />
                </div>

                <div class="form-group col-sm-4">
                    <label for="facebook"><g:message code="facility.facebook.label"/></label>
                    <g:textField id="facebook" name="facebook" value="${facility?.facebook}" class="form-control" />
                </div>
                <div class="form-group col-sm-4">
                    <label for="twitter"><g:message code="facility.twitter.label"/></label>
                    <g:textField name="twitter" value="${facility?.twitter}" class="form-control" />
                </div>
                <div class="form-group col-sm-4">
                    <label for="twitter"><g:message code="facility.instagram.label"/></label>
                    <g:textField name="instagram" value="${facility?.instagram}" class="form-control" />
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12">
                    <g:render template="/templates/facility/openhours"/>
                </div>
            </div>

            <div class="row well no-bottom-margin">
                <div class="form-group col-sm-4">
                    <label for="defaultBookingCustomerId"><g:message code="facility.defaultBookingCustomer.label"/></label>
                    <input type="hidden" id="defaultBookingCustomerId" name="defaultBookingCustomerId" value="${facility?.defaultBookingCustomer?.id}" class="form-control"/>
                </div>

                <div class="form-group col-sm-4">
                    <label for="relatedBookingsCustomerId"><g:message code="facility.relatedBookingsCustomer.label"/></label>
                    <g:hiddenField name="relatedBookingsCustomerId" value="${facility?.relatedBookingsCustomer?.id}" class="form-control"/>
                </div>

                <div class="form-group col-sm-12">
                    <div class="checkbox">
                        <g:checkBox class="openHoursControl" name="whetherToSendEmailConfirmationByDefault" value="${facility?.whetherToSendEmailConfirmationByDefault}"/>
                        <label for="whetherToSendEmailConfirmationByDefault"><g:message code="facility.whetherToSendEmailConfirmationByDefault.label"/></label>
                    </div>
                </div>

                <div class="form-group col-sm-12">
                    <div class="checkbox">
                        <g:checkBox class="openHoursControl" name="requireSecurityNumber" value="${facility?.requireSecurityNumber}"/>
                        <label for="requireSecurityNumber"><g:message code="facility.requireSecurityNumber.label"/></label>
                    </div>
                </div>
            </div>

            <div class="row well">
                <div class="form-group col-sm-4">
                    <label for="bookingRuleNumDaysBookable"><g:message code="facility.bookingRuleNumDaysBookable.label"/></label>
                    <g:textField name="bookingRuleNumDaysBookable"
                                 value="${facility?.bookingRuleNumDaysBookable}" class="form-control"/>
                </div>
                <div class="form-group col-sm-4">
                    <label for="vat"><g:message code="facility.vat.label"/></label>
                    <g:select id="vat" name="vat" value="${facility?.vat}" from="${Facility.POSSIBLE_VATS}" class="form-control" />
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12">
                    <label for="facilityLogotypeImage" style="height:35px"><g:message code="adminFacility.create.logo"/></label>
                    <input id="facilityLogotypeImage" type="file" name="facilityLogotypeImage" class="form-control"/>
                    <g:fileArchiveAdminPreviewImage
                            file="${facility.facilityLogotypeImage}"
                            deleteAction="deleteImage"
                            parameters="[id:facility.id]"/>
                </div>

                <div class="form-group col-sm-12">
                    <label for="facilityOverviewImage" style="height:35px"><g:message code="adminFacility.create.overviewImage"/></label>
                    <input id="facilityOverviewImage" type="file" name="facilityOverviewImage" class="form-control"/>
                    <g:fileArchiveAdminPreviewImage
                            file="${facility.facilityOverviewImage}"
                            deleteAction="deleteImage"
                            parameters="[id:facility.id]"/>
                </div>

                <div class="form-group col-sm-12">
                    <label for="facilityWelcomeImage" style="height:35px"><g:message code="adminFacility.create.welcomeImage"/></label>

                    <input id="facilityWelcomeImage" type="file" name="facilityWelcomeImage" class="form-control"/>
                    <g:fileArchiveAdminPreviewImage
                            file="${facility.facilityWelcomeImage}"
                            deleteAction="deleteImage"
                            parameters="[id:facility.id]"/>
                </div>
            </div>

            <div class="row well">
                <div class="form-group col-sm-12">
                    <div class="checkbox">
                        <g:checkBox name="multisport" value="${facility?.multisport}"/>
                        <label for="multisport"><g:message code="adminFacility.multisport.label"/></label>
                    </div>
                </div>

                <div class="form-group col-sm-6">
                    <label><g:message code="adminFacility.create.sports"/></label>
                    <div id="sportSelect">
                        <g:each var="sport" in="${sports.toList()}">
                            <div class="checkbox">
                                <input type="checkbox" id="sport_${sport.id}" name="sports" value="${sport.id}" ${facility.sports?.contains(sport) ?"checked":""}/>
                                <label for="sport_${sport.id}"><g:message code="sport.name.${sport.id}"/></label>
                            </div>
                        </g:each>
                    </div>
                </div>

                <div class="form-group col-sm-6">
                    <label for="active"><g:message code="adminFacility.create.other"/></label>
                    <div class="checkbox">
                        <g:checkBox name="enabled" value="${facility?.enabled}"/>
                        <label for="enabled"><g:message code="facility.enabled.label"/></label>
                    </div>
                    <div class="checkbox">
                        <g:checkBox name="active" value="${facility?.active}"/>
                        <label for="active"><g:message code="facility.active.label"/></label>
                    </div>
                    <div class="checkbox">
                        <g:checkBox name="bookable" value="${facility?.bookable}" />
                        <label for="bookable"><g:message code="facility.bookable.label"/></label>
                    </div>
                    <div class="checkbox">
                        <g:checkBox name="boxnet" value="${facility?.boxnet}" />
                        <label for="boxnet"><g:message code="facility.boxnet.label"/></label>
                    </div>
                    <div class="checkbox">
                        <g:checkBox name="invoicing" value="${facility?.invoicing}" />
                        <label for="invoicing"><g:message code="facility.invoicing.label"/></label>
                    </div>
                </div>
            </div>

            <div class="row no-bottom-margin">
                <div class="form-group col-sm-12">
                    <g:facilityProperty facility="${facility}" property="${com.matchi.FacilityProperty.FacilityPropertyKey.FORTNOX3_AUTHORIZATION_CODE.name()}" resultVar="authCode"/>
                    <label for="fortnox3Token"><g:message code="facility.fortnoxV3.label"/></label>
                    <ul class="list-inline">
                        <li class="col-sm-4"><input type="text" placeholder="${message(code: 'facility.fortnox3AuthCode.placeholder')}" id="fortnox3AuthCode" value="${authCode}" class="form-control fortnox-value" ${authCode ? 'disabled = ""' : ''}/></li>
                        <li class="col-sm-4"><g:textField name="fortnox3Token" disabled=""
                                                          value="${facilityProperty(property: com.matchi.FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(), facility: facility)}"
                                                          class="form-control fortnox-value"/></li>
                        <li class="col-sm-2">
                            <a href="javascript:void(0)" id="confirmRenewAccessToken" ${authCode ? '' : 'style = display:none'} class="btn btn-success">
                                <g:message code="facility.fortnoxV3.confirmRenewAccessToken"/>
                            </a>
                            <a href="javascript:void(0)" id="renewAccessToken" ${authCode ? 'style = display:none' : ''} class="btn btn-success">
                                <g:message code="facility.fortnoxV3.renewToken"/>
                            </a>

                            <div class="panel-body renewAccessTokenSpinner" style="display: none; margin-left: 20px;">
                                <i class="fas fa-spinner fa-spin"></i>
                            </div>
                        </li>
                        <li class="col-sm-2">
                            <g:remoteLink action="testFortnox3Values" params="[id: facility.id]" update="fortnoxV3Result" class="btn btn-success"
                                          onLoading="\$('#fortnoxV3Result').html('${message(code: 'default.loader.label')}')">(<g:message
                                    code="facility.fortnoxV3.testConnection"/> )</g:remoteLink>
                        </li>
                    </ul>
                    <span id="fortnoxV3Result" class="help-block"></span>
                </div>
            </div>

            <div class="row well">
                <div class="form-group col-sm-12">
                    <label for="apiKey"><g:message code="facility.apikey.label"/></label>
                    <g:textField name="apiKey" value="${facility?.apikey}" readonly="readonly" class="form-control" />
                </div>
            </div>

            <g:ifFacilityPropertyEnabled facility="${facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE.name()}">
            <div class="row well">
                <div class="form-group col-sm-12">
                    <g:message code="adminFacility.edit.title.ioMembers"/><br/>
                    <g:link action="ioActiveOrTerminatedMembersSync" id="${facility.id}" class="btn btn-success">
                        <g:message code="adminFacility.edit.button.ioSyncMembersOnly"/>
                    </g:link>
                    <g:link action="ioAllMembersSync" id="${facility.id}" params="[includeAll: true]" class="btn btn-success">
                        <g:message code="adminFacility.edit.button.ioSyncAllCustomer"/>
                    </g:link>

                    <br><br>

                    <g:ifFacilityPropertyEnabled facility="${facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE_ACTIVITIES.name()}">
                    <g:message code="adminFacility.edit.title.ioActivities"/><br/>
                    <div class="form-group col-sm-2">
                        <label for="showStartDate"><g:message code="default.from.label"/></label>
                        <input class="form-control" readonly="true" type="text" name="showStartDate" id="showStartDate"
                               value="<g:formatDate formatName="date.format.dateOnly" date="${new java.util.Date()}" />" />
                    </div>
                    <div class="form-group col-sm-2">
                        <label for="showEndDate"><g:message code="default.to.label"/></label>
                        <input class="form-control" readonly="true" type="text" name="showEndDate" id="showEndDate"
                               value="<g:formatDate formatName="date.format.dateOnly" date="${new java.util.Date()}"/>" />
                    </div>
                    <div class="form-group col-sm-2 bottom-align-text">
                        <br>
                        <g:link action="ioActivitiesSync" id="${facility.id}" name="sendActivitiesLink" class="btn btn-success">
                            <g:message code="adminFacility.edit.button.ioActivitiesSync"/>
                        </g:link>
                    </div>
                    </g:ifFacilityPropertyEnabled>
                </div>
            </div>
            </g:ifFacilityPropertyEnabled>

            <div class="form-group col-sm-12">
                <g:submitButton name="submit" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:if test="${facility?.hasMPC()}">
                    <g:actionSubmit action="addCodeRequest" class="btn btn-info"
                            value="${message(code: 'adminFacility.addCodeRequest.label')}"/>
                </g:if>
                <g:if test="${facility?.hasMPC()}">
                    <g:actionSubmit action="clearMissingCodeRequests" class="btn btn-info"
                                    value="Clear missing code requests"/>
                </g:if>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </g:uploadForm>
    </div>
</div>
<g:render template="/templates/googleMaps"/>
<r:script>
    var map = null;
    var geocoder = null;
    var marker = null;

    $(document).ready(function() {
        $(".chzn-select").select2({
            placeholder: "${message(code: 'municipality.multiselect.noneSelectedText')}..."
        });

        $("#defaultBookingCustomerId").matchiCustomerSelect({width:'250px', onchange: selectCustomer});
        $("#relatedBookingsCustomerId").matchiCustomerSelect({width:'250px', onchange: selectRelatedBookingsCustomer});

        var mapOptions = {
            center: new google.maps.LatLng(${g.forJavaScript(data: facility?.lat)}, ${g.forJavaScript(data: facility?.lng)}),
            zoom: 13,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };

        map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
        geocoder = new google.maps.Geocoder();

        marker = new google.maps.Marker({
            position: new google.maps.LatLng(${g.forJavaScript(data: facility?.lat)}, ${g.forJavaScript(data: facility?.lng)}),
            map: map,
            title: "${g.forJavaScript(data: facility.name)}",
            draggable: true
        });

        google.maps.event.addListener(marker, "dragend", function() {
            $("input[name=lat]").val(marker.getPosition().lat());
            $("input[name=lng]").val(marker.getPosition().lng());

            var infowindow = new google.maps.InfoWindow({
                content: marker.getPosition().toUrlValue(6),
                size: new google.maps.Size(50,50)
            });

            infowindow.open(map, marker);
        });

        google.maps.event.addListener(marker, "click", function() {
            var infowindow = new google.maps.InfoWindow({
                content: marker.getPosition().toUrlValue(6),
                size: new google.maps.Size(50,50)
            });

            infowindow.open(map, marker);
        });

        $('#migrateFortnoxCustomerRecords').click(function(){
           if(!confirm("${g.message(code: 'facility.fortnoxV3.confirm.migrateProcess')}")) {
              return false;
           }
           $('.migrateFortnoxCustomerRecordsSpinner').show();
           $.ajax({
            type:'POST',
            data:{'id': '${g.forJavaScript(data: facility?.id)}' },
            url:'${g.forJavaScript(data: createLink(controller: 'adminFacility', action: 'migrateTablesFromFortnoxV1ToV3'))}',
            success:function(data,textStatus){
              $('#migrateFortnoxCustomerRecordsResult').html(data);
              $('.migrateFortnoxCustomerRecordsSpinner').hide();
            }
          })
        });

        $('#renewAccessToken').click(function(){
    $('.renewAccessTokenSpinner').show();
    var authCode = $('#fortnox3AuthCode').val();
    if(authCode === "${g.forJavaScript(data: facilityProperty(property: com.matchi.FacilityProperty.FacilityPropertyKey.FORTNOX3_AUTHORIZATION_CODE.name()))}"){
            alert("${message(code: 'facility.fortnoxV3.specifyOtherAuthCode')}");
            $('.renewAccessTokenSpinner').hide();
            return false;
          }
          $.ajax({
            type:'POST',
            data:{'id': '${g.forJavaScript(data: facility?.id)}','authCode': authCode },
            url:'${g.forJavaScript(data: createLink(controller: 'adminFacility', action: 'renewAccessToken'))}',
            success:function(data,textStatus){
              if(data){
                $('#fortnox3Token').val(data);
                if(data == 'ERR') {
                  showRenewTokenPossibility();
                } else {
                  hideRenewTokenPossibility();
                }
              }
              $('.renewAccessTokenSpinner').hide();
            }
          })
        });

        $('#confirmRenewAccessToken').click(function(){
          if(confirm("${g.message(code:'facility.fortnoxV3.confirmRenewAccessToken.description')}")){
            showRenewTokenPossibility();
          }
        });

        initializeIdrottOnlineDatePicker("#showStartDate");
        initializeIdrottOnlineDatePicker("#showEndDate");
        initializeIdrottOnlineSendActivitiesButton("a[name='sendActivitiesLink']");

        $('#multisport').on('change', function () {
            var sportSelector = $("#sportSelect");

            $.ajax({
                cache: false,
                url: "${g.forJavaScript(data: createLink(controller: 'adminFacility', action: 'sports', params: [id: facility.id]))}?multisport=" + this.checked,
                dataType : 'json',
                success: function (data) {
                    sportSelector.empty();
                    $.each(data, function(key, value) {
                        var checked = (value.selected  ? ' checked="checked"' : '');
                        var div = $('<div class="checkbox"></div>').appendTo(sportSelector);
                        var box = $('<input name="sports" id="sports_' + value.sport.id+ '" type="checkbox" value="' + value.sport.id + '"' + checked + '">').appendTo(div);
                        var label = $('<label for="sports_' + value.sport.id+ '"></label').appendTo(div)
                        $(document.createTextNode($L('sport.name.'+value.sport.id))).appendTo(label)
                    })
                },
                error: function() {
                    alert('<g:message code="adminFacility.multisport.error"/>');
                }
            });
        });
    });

    function initializeIdrottOnlineDatePicker(selector){
        $(selector).datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altFormat: 'yy-mm-dd'
        });
    }
    function initializeIdrottOnlineSendActivitiesButton(selector){
        $(function() {
            $(selector).bind('click', function() {
                $(this).attr('href', $(this).attr('href') + '?startDateString=' + $("#showStartDate").val() + '&endDateString=' + $("#showEndDate").val());
            });
        });
    }

    function showRenewTokenPossibility(){
        $('#fortnox3AuthCode').removeAttr('disabled');
        $('#renewAccessToken').show();
        $('#confirmRenewAccessToken').hide();
    }

    function hideRenewTokenPossibility(){
        $('#fortnox3AuthCode').attr('disabled', 'disabled');
        $('#renewAccessToken').hide();
        $('#confirmRenewAccessToken').show();
    }

    function showAddress(address) {
        if( geocoder ) {
            geocoder.geocode({ 'address': address }, function(results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    var location = results[0].geometry.location;

                    map.setCenter(location);
                    marker.setPosition(location);

                    $("input[name=lat]").val(marker.getPosition().lat());
                    $("input[name=lng]").val(marker.getPosition().lng());
                } else {
                    alert("${message(code: 'adminRegion.createMunicipality.geocodeError')}: " + status);
                }
            });
        }
    }

    function moveMarker() {
        var location = new google.maps.LatLng($("input[name=lat]").val(), $("input[name=lng]").val());

        map.setCenter(location);
        marker.setPosition(location);
    }

    function preProssesAddress() {
        var address = $("input[name=address]").val()+","+$("#municipality option:selected").html()+","+$("select[name=country]").val();
        showAddress(address);
    }

    function selectCustomer(customer) {
        if(!customer || !customer.id) {
            $("#defaultBookingCustomerId").val("");
        }
    }

    function selectRelatedBookingsCustomer(customer) {
        if(!customer || !customer.id) {
            $("#relatedBookingsCustomerId").val("");
        }
    }
</r:script>
</body>
</html>
