<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilitySubscriptionCopy.copy.title"/></title>
</head>
<body>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form class="form-inline">
        <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionCopy.copy.wizard.step1'), message(code: 'facilitySubscriptionCopy.copy.wizard.step2'), message(code: 'default.completed.message')], current: 0]"/>

        <g:if test="${error}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert" href="#">×</a>

                <h4 class="alert-heading">
                    ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
                </h4>
                ${error}
            </div>
        </g:if>
        <g:if test="${nonActiveSubscriptionsCount}">
            <div class="alert alert-warning" role="alert">
                <a class="close" data-dismiss="alert" href="#">×</a>
                <g:message code="facilitySubscriptionCopy.copy.enterDetails.skippedSubscriptions"
                        args="[nonActiveSubscriptionsCount]"/>
                <br>
            </div>
        </g:if>

        <h1><g:message code="facilitySubscriptionCopy.copy.title"/></h1>
        <p class="lead"><g:message code="facilitySubscriptionCopy.copy.enterDetails.message3"/></p>

        <div class="well">
            <div class="row">
                <div class="span12">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="fromDate"><g:message code="season.multiselect.noneSelectedText"/></label>
                            <g:select id="season" name="season" from="${upcomingSeasons}" optionValue="name" optionKey="id" value="${upcomingSeasons?.size() > 0 ? upcomingSeasons[0].id : ""}"/>

                            &nbsp;&nbsp;<label class="control-label" for="fromDate"><g:message code="facilitySubscriptionCopy.copy.enterDetails.message5"/></label>
                            <input class="span2 center-text" readonly="true" type="text" name="fromDate" id="fromDate"
                                   value="${dateFrom ? dateFrom.toString('yyyy-MM-dd') : upcomingSeasons?.size() > 0 ? new DateTime(upcomingSeasons[0].startTime).toString('yyyy-MM-dd') :""}"/>

                            &nbsp;<label class="control-label" for="toDate"><g:message code="facilitySubscriptionCopy.copy.enterDetails.message6"/></label>
                            <input class="span2 center-text" readonly="true" type="text" name="toDate" id="toDate"
                                   value="${dateTo ? dateTo.toString('yyyy-MM-dd'): upcomingSeasons?.size() > 0 ? new DateTime(upcomingSeasons[0].endTime).toString('yyyy-MM-dd') :""}"/>
                        </div>
                    </fieldset>
                </div>
            </div>
        </div>

        <table class="table table-transparent">
            <thead>
            <tr>
            <th width="40"></th>
            <th><g:message code="customer.label"/></th>
            <th><g:message code="default.day.label"/></th>
            <th><g:message code="court.label"/></th>
            <th><g:message code="default.date.time"/></th>
            <th><g:message code="subscription.startDate.label"/></th>
            <th><g:message code="subscription.endDate.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${subscriptionInfo}" var="subscription">
                <tr>
                    <td class="center-text"><g:checkBox name="subscriptionId" value="${subscription.id}" checked="${subscriptionInfo.collect{ it.id }.contains(subscription.id)}"/></td>
                    <td>${subscription.customer}</td>
                    <td><g:message code="time.weekDay.plural.${subscription.weekDay}"/></td>
                    <td>${subscription.court}</td>
                    <td>${subscription.startTime}</td>
                    <td>${subscription.startDate}</td>
                    <td>${subscription.endDate}</td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}"/>
        </div>

    </g:form>
    <r:script>
        var seasons = new Array();
        var $dateFrom;
        var $dateTo;

        $(document).ready(function() {
            $dateFrom = $("#fromDate");
            $dateTo = $("#toDate");

            $("[rel='tooltip']").tooltip();
            <g:each in="${upcomingSeasons}" var="season" status="i">
                seasons[${g.forJavaScript(data: i)}] = {
                    id: ${g.forJavaScript(data: season.id)}, name: "${g.forJavaScript(data: season.name)}",
                    start: "${g.forJavaScript(data: new DateTime(season.startTime).toString('yyyy-MM-dd'))}",
                    end: "${g.forJavaScript(data: new DateTime(season.endTime).toString('yyyy-MM-dd'))}"
                }
            </g:each>


            $('#season').on('change', function() {
                var season = getSeason($(this).val());
                $dateFrom.val(season.start);
                $dateTo.val(season.end);
            });

            $dateFrom.datepicker({
                beforeShow: customRange,
                autoSize: true,
                dateFormat: 'yy-mm-dd',
                onClose: function() {
                    var dateFrom = $(this).datepicker("getDate");
                    var $dateToPicker = $dateTo;

                    if( $dateTo.datepicker("getDate") != null && $dateTo.datepicker("getDate") < dateFrom) {
                        $dateTo.datepicker("setDate", dateFrom);
                    }

                    $dateToPicker.datepicker("option", "minDate", dateFrom);
                }
            });

            $dateTo.datepicker({
                beforeShow: customRange,
                autoSize: true,
                dateFormat: 'yy-mm-dd'
            });
        });

        function customRange(input) {
            var dateMin = new Date(2008, 11 - 1, 1); //Set this to your absolute minimum date;;
            var dateMax = '';
            var currentSeason = getSeason($('#season').val());

            if (input.id == "fromDate") {
                if(${upcomingSeasons?.size() > 0}) {
                    dateMin = new Date(currentSeason.start);
                    dateMax = new Date(currentSeason.end);
                }
            }

            if (input.id == "toDate") {
                if ($dateTo.datepicker("getDate") != null) {
                    dateMin = $dateFrom.datepicker("getDate");
                }

                if(${upcomingSeasons?.size() > 0}) {
                    dateMax = new Date(currentSeason.end);
                }
            }
            console.log("DateMin: " + dateMin);
            console.log("DateMax: " + dateMax);

            return {
                minDate: dateMin,
                maxDate: dateMax
            };
        }

        function getSeason(id) {
            for(var i=0;i < seasons.length;i++) {
                if(seasons[i].id == id) {
                    return seasons[i];
                }
            }
            return null
        }
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

</body>
</html>