<%@ page import="org.joda.time.LocalDate; com.matchi.Facility; com.matchi.Sport" %>
<html>
<head>
    <title><g:message code="page.book.index.header"/> - <g:message code="default.matchi.label"/></title>
    <meta name="layout" content="b3main" />
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
    <r:require modules="bootstrap-datepicker"/>
</head>
<body>
<section class="block block-grey vertical-padding20">
    <div class="container">
        <h2><g:message code="page.book.index.header"/></h2>
        <hr>
        <g:formRemote name="findFacilityForm" class="form" action="findFacilities" update="facilities-result" url="[action: 'findFacilities']"
                      before="startSearch(this)" onSuccess="finishedSearch(this)">
            <g:hiddenField name="lat" value=""/>
            <g:hiddenField name="lng" value=""/>
            <g:hiddenField name="offset" value="${cmd.offset?:0}"/>

            <div class="row">
                <div class="col-sm-2">
                    <div class="form-group">
                        <select id="inOutCourt" name="outdoors" data-style="form-control">
                            <option value=""><g:message code="court.indoorOutdoor.label"/></option>
                            <g:each in="[false, true]">
                                <option value="${it}"  ${cmd.outdoors == it ? "selected" : ""}>
                                    <g:message code="court.${!it ? 'indoor' : 'outdoors'}.label"/>
                                </option>
                            </g:each>
                        </select>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group">
                        <select id="sport" name="sport" data-style="form-control">
                            <option value=""><g:message code="default.choose.sport"/></option>
                            <g:each in="${Sport.coreSportAndOther.list()}">
                                <option value="${it.id}" ${cmd.sport == it.id ? "selected" : ""}
                                        data-content="<i class='ma ma-${it.id}'></i> <g:message code="sport.name.${it.id}" /> "><g:message code="sport.name.${it.id}" /></option>
                            </g:each>
                        </select>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group has-feedback">
                        <input type="text" class="form-control" id="showDate" name="date" value="${params.date ? new LocalDate(params.date('date')).toDate().format(g.message(code:'date.format.dateOnly')) : formatDate(date: new Date(), formatName: 'date.format.dateOnly')}">
                        <span class="fa fa-calendar form-control-feedback" aria-hidden="true"></span>
                        <span id="inputSuccess2Status" class="sr-only">(<g:message code="default.status.success"/>)</span>
                    </div>
                </div>

                <div class="col-sm-6">
                    <div class="form-group">
                        <g:searchFacilityInput name="q" placeholder="${message(code: 'book.index.search.placeholder')}" class="form-control" value="${cmd?.q}"/>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-6">
                    <span/>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <select id="hasCamera" name="hasCamera">
                            <option value=""><g:message code="court.camera.nopreference.label"/></option>
                            <g:each in="[true, false]">
                                <option value="${it}"  ${cmd.hasCamera == it ? "selected" : ""}>
                                    <g:message code="court.${it ? 'withCamera' : 'withoutCamera'}.label"/>
                                </option>
                            </g:each>
                        </select>
                    </div>
                </div>
                <div class="col-sm-2">
                    <button name="submit" onclick="return searchFromFirstPage()" class="btn btn-success col-xs-12"><g:message code="button.smash.label"/></button>
                </div>
            </div>
        </g:formRemote>
    </div>
</section>

<section class="block block-white facility-list vertical-padding20 relative">
    <div class="container">
        <div id="facilities-result" class="row">
            <div class="col-md-12 vertical-margin40"></div>
        </div>
    </div>

    <div id="findFacilitiesLoader" class="absolute text-center vertical-padding40"
         style="background: rgba(0,0,0,0.25);
         width: 100%;
         height: 100%;
         top: 0;
         color: #f8f8f8;">
        <i class="fas fa-spinner fa-spin fa-3x"></i>
    </div>
</section>
<r:script>
    function searchFromFirstPage() {
         $("#offset").val(0);
         return true;
    }

    function finishedSearch() {
        $('#findFacilitiesLoader').hide();
        $('#findFacilityForm').find("button[name='submit']").removeAttr('disabled');
    }
    function startSearch() {
        $('#findFacilitiesLoader').show();
        $('#findFacilityForm').find("button[name='submit']").attr('disabled','');
    }

    $(document).ready(function() {
        <g:if test="${paymentFlow}">
            <g:remoteFunction controller="${paymentFlow.paymentController}" action="${paymentFlow.getFinalAction()}" params="${paymentFlow.getModalParams()}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
        </g:if>
        <g:elseif test="${params.comeback}">
            <g:remoteFunction controller="bookingPayment" action="confirm" params="${params}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
        </g:elseif>

        $('#inOutCourt').selectpicker({
            title: '<g:message code="court.indoorOutdoor.label"/>'
        });

        $('#sport').selectpicker({
            title: '<g:message code="default.choose.sport"/>'
        });

        $('#hasCamera').selectpicker({
            title: '<g:message code="court.camera.nopreference.label"/>'
        });


        $('#showDate').datepicker({
            format: "${message(code: 'date.format.dateOnly.small2')}",
            startDate: new Date(),
            weekStart: 1,
            todayBtn: "linked",
            autoclose: true,
            language: "${g.locale()}",
            calendarWeeks: true,
            todayHighlight: true
        }).on('changeDate', function() {
            $('#findFacilityForm').submit();
        });

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                $("#lat").val(position.coords.latitude);
                $("#lng").val(position.coords.longitude);
                $('#findFacilityForm').submit();
            }, function() {
                $('#findFacilityForm').submit();
            }, { enableHighAccuracy: true, maximumAge: 300000, timeout: 3000 });
        } else {
            $('#findFacilityForm').submit();
        }
    });
</r:script>
</body>
</html>
