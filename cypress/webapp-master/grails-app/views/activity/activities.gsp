<%@ page import="com.matchi.activities.*; com.matchi.Sport; com.matchi.Facility; org.joda.time.LocalDate" %>
<g:each in="${occasionsByActivityByDate}" var="occasionsByActivityByDateHolder" status="a">
    <g:set var="date" value="${occasionsByActivityByDateHolder.key}" />
    <g:set var="occasionsByActivity" value="${occasionsByActivityByDateHolder.value}" />
    <div class="panel panel-default no-border no-box-shadow">
        <div class="panel-body">
            <g:each in="${occasionsByActivity}" var="occasionsByActivityHolder" status="b">
                <div class="panel panel-default no-border no-box-shadow bottom-border">
                    <div class="panel-body no-padding">
                        <div class="row">
                            <g:set var="activity" value="${occasionsByActivityHolder.key}" />
                            <g:set var="activityOccasions" value="${occasionsByActivityHolder.value}" />
                            <g:set var="facility" value="${activity.facility}" />
                            <div class="col-sm-5">
                                <div class="media">
                                    <div class="media-left">
                                        <div class="thumbnail bg-white avatar-square-md">
                                            <g:link controller="facility" action="show" params="[name: facility.shortname]">
                                                <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}" alt="${facility.name}" class="img-responsive"/>
                                            </g:link>
                                        </div>
                                    </div>
                                    <div class="media-body facility-info-text">
                                        <h4 class="media-heading h4">
                                            <g:link controller="facility" action="show" params="[name: facility.shortname, date: params.date, sport: params.sport]">
                                                ${facility.name}
                                            </g:link>
                                        </h4>
                                        <p>${facility.municipality.name}</p>
                                    </div><!-- /.media-body -->
                                </div><!-- /.media -->
                            </div><!-- /.col-sm-5 -->

                            <div class="col-sm-7 activity-container">
                                <div class="panel panel-default no-border no-box-shadow">
                                    <div class="panel-body no-padding">
                                        <h4 class="no-top-margin">
                                            <a href="<g:createLink controller="facility" action="show" params="[name: facility.shortname, date: params.date, sport: params.sport]"/>#${activity.getClass().simpleName + activity.id}" >
                                                ${activity.name}
                                            </a>
                                            <g:render template="/templates/activity/activityLevel" model="[activity: activity]"/>
                                        </h4>
                                        <g:set var="descriptionText" value="${activity.teaser ? "<p>${activity.teaser}</p>" : ""}" />
                                        <g:render template="/templates/general/collapsableText" model="[text: descriptionText]" />
                                        <g:render template="/templates/activity/listActivityOccasions" model="[activityOccasions: activityOccasions, facility: facility, user: user]"/>
                                    </div>
                                </div>
                            </div><!-- /.col-sm-7 -->
                        </div><!-- /.row -->
                    </div>
                </div>

            </g:each>
            <div class="clearfix"></div>
        </div><!-- /.panel-body -->
    </div><!-- /.panel -->
</g:each>

<g:if test="${occasionsByActivityByDate?.size() == 0}">
    <div class="row text-center">
        <div class="col-md-12 vertical-margin40">
            <strong><g:message code="activities.results.empty"/></strong>
        </div>
    </div>
</g:if>
