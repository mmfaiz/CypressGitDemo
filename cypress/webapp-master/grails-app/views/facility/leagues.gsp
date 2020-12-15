<%@ page import="org.joda.time.LocalDate; org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib; com.matchi.Availability; com.matchi.DateUtil; com.matchi.Court; com.matchi.Sport; com.matchi.Region" %>
<head xmlns="http://www.w3.org/1999/html">
    <meta property="fb:app_id" content="340585979293099"/>
    <meta property="og:type" content="matchifb:facility"/>
    <meta property="og:url"
          content="${new ApplicationTagLib().createLink([controller: 'facility', action: 'league', absolute: 'true', params: ['name': facility.shortname]]).toString()}"/>
    <meta property="og:title" content="${facility.name}"/>
    <meta property="og:image" content="${new ApplicationTagLib().createLink([uri: logoUrl, absolute: true])}"/>
    <meta property="matchifb:logo:url" content="${new ApplicationTagLib().createLink([uri: logoUrl, absolute: true])}"/>
    <meta property="place:location:latitude" content="${facility.lat}"/>
    <meta property="place:location:longitude" content="${facility.lng}"/>
    <meta name="layout" content="${!params.wl.equals("1") ? "b3main" : "whitelabel"}"/>
    <meta name="classes" content="splash-page"/>
    <title>${facility}</title
    <r:script type="text/javascript">

        $(document).ready(function() {
            $("[rel=tooltip]").tooltip({
                delay: { show: 750, hide: 10 },
                html: true,
                container: 'body'
            });
        });
    </r:script>
</head>

<body>

<g:if test="${!params.wl.equals("1")}">
    <!-- TOP COVER STRIPE -->
    <section class="block box-shadow user-profile">
        <div class="welcome-wrapper splash splash-container" style="background-image: url(<g:facilityWelcomeUrl id="${facility.id}"/>);">

        </div>
    </section>
    <section class="relative block block-grey">
        <!-- WHITE STRIPE -->
        <div class="block block-white">
            <div class="container">
                <div class="flex-center wrap">
                    <div class="col-sm-5 col-xs-12 relative flex-center">
                        <!-- FACILITY LOGO -->
                        <div class="profile-avatar-wrapper">
                            <div class="profile-avatar avatar-square-lg">
                                <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}" alt="${facility.name}"
                                                                class="img-responsive"/>
                            </div>
                        </div><!-- /.facility logo-->

                    <!-- FACILITY NAME & LOCATION -->
                        <div class="profile-name-location vertical-padding15">
                            <h2 class="h4 vertical-margin5">
                                ${facility}
                            </h2>
                            <g:if test="${facility.municipality}">
                                <span class="block text-xs text-muted">
                                    <i class="fas fa-map-marker"></i> ${facility.municipality}
                                </span>
                            </g:if>
                        </div><!-- /.facility name & location-->
                    </div>

                    <!--  FACILITY NAVIGATION DESKTOP -->
                    <div class="col-sm-7 col-xs-12 text-right hidden-xs vertical-padding15">
                        <g:render template="/templates/facility/facilityNavigation"/>
                    </div>

                    <!--  FACILITY NAVIGATION MOBILE -->
                    <div class="col-xs-12 text-left top-padding10 visible-xs vertical-padding15">
                        <g:render template="/templates/facility/facilityNavigation"/>
                    </div>
                </div>
            </div><!-- /.container -->
        </div>

        <div class="container vertical-padding50">
            <div class="row">
                <!-- SIDEBAR -->
                <div class="col-md-3 col-sm-12 top-padding20">
                    <!-- LEAGUES -->
                    <div class="panel panel-default">
                        <div class="panel-heading vertical-padding10">
                            <h3 class="h4"><g:message code="default.league.plural"/></h3>
                        </div>
                        <div id="league-list" class="panel-body">
                            <ul class="league-list">
                                <g:each in="${excelLeague.leagues}">
                                    <li class=""><i class="fas fa-chevron-right text-success"></i> <a href="javascript:void(0);" onclick="showLeague('${it.id}')">${it.name}</a></li>
                                </g:each>
                            </ul>
                        </div><!-- /.panel-body -->
                    </div><!-- /.panel -->
                </div><!-- /.col-sm-3 (SIDEBAR) -->

            <!-- MAIN -->
                <div class="col-sm-9 top-padding20">
                    <section class="panel panel-default">
                        <div id="league-display" class="panel-body">
                            <g:each in="${excelLeague.leagues}" var="league">
                                <div class="row league" leagueID="${league.id}" style="display: none;">
                                    <div class="col-sm-12"><h2>${league.name}</h2>
                                        ${league.description}

                                        <g:if test="${league.linkToRules}">
                                            <br><br><a href="${league.linkToRules}" target="_blank"><g:message code="default.league.rules.label"/></a>
                                        </g:if>

                                        <g:each in="${league.divisions}" var="division">

                                            <div class="row">
                                                <hr style="margin-bottom: 0px;">
                                                <div class="col-sm-12"><h3>${division.name}</h3></div>
                                            </div>

                                            <div class="row bottom-margin30">
                                                <div class="col-sm-12 table-responsive">
                                                    <table class="table-striped table-bordered" width="100%">
                                                        <tr class="league-info-header"><th colspan="5"><g:message code="default.league.standings.label"/></th></tr>
                                                        <tr class="league-column-titles">
                                                            <th><g:message code="default.league.standings.team"/></th>
                                                            <th><g:message code="default.league.standings.points"/></th>
                                                            <th><g:message code="default.league.standings.matches"/></th>
                                                            <th><g:message code="default.league.standings.contactPerson"/></th>
                                                            <th><g:message code="default.league.standings.contactInfo"/></th>
                                                        </tr>
                                                        <g:each in="${division.standings}" var="standing">
                                                            <tr class="league-column-content">
                                                                <td class="text-nowrap"><strong>${standing.team}</strong></td>
                                                                <td class="text-nowrap"><strong>${standing.points}</strong></td>
                                                                <td class="text-nowrap">${standing.matchesPlayed} / ${standing.matchesTotal}</td>
                                                                <td class="text-nowrap">${standing.contactPerson}</td>
                                                                <td class="text-nowrap">${standing.contactInformation}</td>
                                                            </tr>
                                                        </g:each>
                                                    </table>
                                                </div>
                                            </div>
                                            <div class="row">
                                                <div class="col-sm-12 table-responsive">
                                                    <table class="table-striped table-bordered" width="100%">
                                                        <tr class="league-brand-header"><th colspan="8"><g:message code="default.league.matches.label"/></th></tr>
                                                        <tr class="league-column-titles">
                                                            <th class="padding5"><g:message code="default.league.matches.date"/></th>
                                                            <th class="padding5"><g:message code="default.league.matches.court"/></th>
                                                            <th class="padding5"><g:message code="default.league.matches.home"/></th>
                                                            <th class="padding5"><g:message code="default.league.matches.away"/></th>
                                                            <th class="padding5 text-center"><g:message code="default.league.matches.set1"/></th>
                                                            <th class="padding5 text-center"><g:message code="default.league.matches.set2"/></th>
                                                            <th class="padding5 text-center"><g:message code="default.league.matches.set3"/></th>
                                                            <th class="padding5 text-center"><g:message code="default.league.matches.points"/></th>
                                                        </tr>
                                                        <g:each in="${division.matches}" var="match">
                                                            <tr>
                                                                <td class="padding5 text-nowrap">${match.date}</td>
                                                                <td class="padding5 text-nowrap">${match.court}</td>
                                                                <td class="padding5 text-nowrap"><strong>${match.homeTeam}</strong></td>
                                                                <td class="padding5 text-nowrap"><strong>${match.awayTeam}</strong></td>
                                                                <td class="padding5 text-center text-nowrap">${match.set1HomeTeam} - ${match.set1AwayTeam}</td>
                                                                <td class="padding5 text-center text-nowrap">${match.set2HomeTeam} - ${match.set2AwayTeam}</td>
                                                                <td class="padding5 text-center text-nowrap">${match.set3HomeTeam} - ${match.set3AwayTeam}</td>
                                                                <td class="padding5 text-center text-nowrap"><strong>${match.pointsHomeTeam}</strong> - <strong>${match.pointsAwayTeam}</strong></td>
                                                            </tr>
                                                        </g:each>
                                                    </table>
                                                </div>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </g:each>
                        </div><!-- /.panel-body -->
                    </section><!-- /.panel -->
                </div><!-- /.col-sm-9 (MAIN) -->
            </div><!-- /.row -->
        </div><!-- /.container -->
    </section>
</g:if>


<r:script>
    $(function() {
        $('a.smooth-anchor').click(function() {
            if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && location.hostname == this.hostname) {
                var target = $(this.hash);
                target = target.length ? target : $('[name=' + this.hash.slice(1) +']');
                if (target.length) {
                    $('html,body').animate({
                        scrollTop: (target.offset().top - 80)
                    }, 1000);
                    return false;
                }
            }
        });

        var loadID = getHashValue('id');
        if(loadID && getLeagueContainer(loadID).length) {
            showLeague(loadID)
        } else {
            $('.league').first().show();
        }
    });

    var showLeague = function(id) {
        $('.league').hide();
        getLeagueContainer(id).show();
    };

    var getLeagueContainer = function(leagueId) {
        return $('[leagueID='+leagueId+']');
    };

    var getHashValue = function(key) {
      var matches = location.hash.match(new RegExp(key+'=([^&]*)'));
      return matches ? matches[1] : null;
    }
</r:script>
<g:if test="${request.xhr}">
    <r:layoutResources disposition="defer"/>
</g:if>

</body>
