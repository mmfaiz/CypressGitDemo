<%@ page import="org.joda.time.LocalDate; com.matchi.Facility; com.matchi.Sport; org.springframework.web.servlet.support.RequestContextUtils; com.matchi.activities.Activity" %>
<html>
<head>
    <title><g:message code="page.activities.index.header"/> - <g:message code="default.matchi.label"/></title>
    <meta name="layout" content="b3main" />
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
    <r:require modules="matchi-watch"/>
    <r:require modules="bootstrap-datepicker"/>
</head>
<body>
<section class="block block-grey vertical-padding20">
    <div class="container">
        <h2><g:message code="page.activities.index.header"/></h2>
        <hr>
        <g:formRemote name="findActivityForm" class="form" action="findActivities" update="activities-result" url="[action: 'findActivities']"
                      before="startSearch(this)" onSuccess="finishedSearch(this)">
            <g:hiddenField name="lat" value=""/>
            <g:hiddenField name="lng" value=""/>

            <div class="row">
                <div class="col-sm-2">
                    <div class="form-group">
                        <select id="sportIds" name="sportIds" data-style="form-control" multiple="multiple">
                            <option value=""><g:message code="default.choose.sport"/></option>
                            <g:each in="${Sport.coreSportAndOther.list()}">
                                <option value="${it.id}" ${cmd.sportIds.contains(it.id) ? "selected" : ""}
                                        data-content="<i class='ma ma-${it.id}'></i> <g:message code="sport.name.${it.id}" /> "><g:message code="sport.name.${it.id}" /></option>
                            </g:each>
                        </select>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group has-feedback">
                        <input type="text" class="form-control" id="showDate" name="startDate" value="${params.startDate ? new LocalDate(params.date('startDate')).toDate().format(g.message(code:'date.format.dateOnly')) : formatDate(date: new Date(), formatName: 'date.format.dateOnly')}">
                        <span class="fa fa-calendar form-control-feedback" aria-hidden="true"></span>
                        <span id="inputSuccess2Status" class="sr-only">(<g:message code="default.status.success"/>)</span>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group">
                        <g:set var="selectedLevel" value="${cmd.level}" />
                        <select id="level" name="level" data-style="form-control">
                            <option value=""><g:message code="default.choose.activityLevel"/></option>
                            <g:each in="${(Activity.LEVEL_RANGE_MIN..Activity.LEVEL_RANGE_MAX)}">
                                <option value="${it}" ${selectedLevel == it ? "selected" : ""}>
                                    <g:message code="default.activityLevel.label" />: <g:message code="${it}" />
                                </option>
                            </g:each>
                        </select>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group">
                        <g:searchFacilityInput name="locationSearch" placeholder="${message(code: 'book.index.search.placeholder')}" class="form-control" value="${cmd?.locationSearch}"/>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group">
                        <g:textField name="querySearch" placeholder="${message(code: 'activities.index.search.query.placeholder')}" class="form-control" value="${cmd?.querySearch}" autocomplete="off"/>
                    </div>
                </div>

                <div class="col-sm-2">
                    <button name="submit" class="btn btn-success col-xs-12"><g:message code="button.smash.label"/></button>
                </div>
            </div>
        </g:formRemote>
    </div>
</section>

<section class="block block-white facility-list vertical-padding20 relative">
    <div class="container">
        <div id="activities-result" class="row">
            <div class="col-md-12 vertical-margin40"></div>
        </div>
    </div>

    <div id="findActivitiesLoader" class="absolute text-center vertical-padding40"
         style="background: rgba(0,0,0,0.25);
         width: 100%;
         height: 100%;
         top: 0;
         color: #f8f8f8;">
        <i class="fas fa-spinner fa-spin fa-3x"></i>
    </div>
</section>
<r:script>
    function finishedSearch() {
        $('#findActivitiesLoader').hide();
        $('#findActivityForm').find("button[name='submit']").removeAttr('disabled');
    }
    function startSearch() {
        $('#findActivitiesLoader').show();
        $('#findActivityForm').find("button[name='submit']").attr('disabled','');
    }

    $(document).ready(function() {
        <g:if test="${paymentFlow}">
            <g:remoteFunction controller="${paymentFlow.paymentController}" action="${paymentFlow.getFinalAction()}" params="${paymentFlow.getModalParams()}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
        </g:if>
        <g:elseif test="${params.comeback}">
            <g:remoteFunction controller="activityPayment" action="confirm" params="${params}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
        </g:elseif>

        $('#sportIds').selectpicker({
            title: '<g:message code="default.choose.sport"/>'
        });

        $('#level').selectpicker({
            title: '<g:message code="default.choose.sport"/>'
        });

        $('#showDate').datepicker({
            format: "${message(code: 'date.format.dateOnly.small2')}",
            startDate: new Date(),
            weekStart: 1,
            language: "${g.locale()}",
            autoclose: true,
            todayHighlight: true
        }).on('changeDate', function() {
            $('#findActivityForm').submit();
        });

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                $("#lat").val(position.coords.latitude);
                $("#lng").val(position.coords.longitude);
                $('#findActivityForm').submit();
            }, function() {
                $('#findActivityForm').submit();
            }, { enableHighAccuracy: true, maximumAge: 300000, timeout: 3000 });
        } else {
            $('#findActivityForm').submit();
        }
    });
</r:script>
</body>
</html>
