<%@ page import="com.matchi.sportprofile.SportProfile; com.matchi.Sport; com.matchi.Region" %>
<head>
    <meta name="layout" content="b3main" />
    <title><g:message code="matching.index.message1"/></title>
    <meta name="showMatchingMessage" content="${true}"/>
    <r:require modules="matchi-truncate,bootstrap-progressbar"/>
</head>
<body>

<g:if test="${user.matchable}">
    <!-- IF MATCHING IS ACTIVATED -->

    <!-- INFO ABOUT MATCHING -->
    <section class="block block-grey vertical-padding30">
        <div class="container">

            <h2 class="no-top-margin">
                <g:message code="matching.index.message2"/>
            </h2>
            <!--<p class="lead text-muted">Utmana en likvärdig spelare på en vänskaplig match!</p>-->
            <div id="matchingInfo" class="alert alert-info" style="display: none;">
                <button type="button" class="close" onclick="removeMatchingInfoMessage()" data-dismiss="alert" rel="tooltip" title="<g:message code="default.do.not.show.again"/>"><span aria-hidden="true">&times;</span><span class="sr-only"><g:message code="button.close.label"/></span></button>
                <p>
                    <span class="fa-stack fa-lg pull-left right-margin10">
                        <i class="fas fa-circle fa-stack-2x"></i>
                        <i class="fa fa-lightbulb-o fa-stack-1x fa-inverse"></i>
                    </span>
                    <g:message code="matching.index.message12"
                            args="[createLink(controller: 'userProfile', action: 'edit'), createLink(controller: 'userProfile', action: 'index')]"/>
                </p>
            </div>

            <hr>

            <g:form action="index" class="form-inline">
                <g:hiddenField name="offset" value="0"/>
                <g:hiddenField name="max" value="10" />
                <input type="hidden" name="offset" value="0" id="offset">
                <input type="hidden" name="max" value="10" id="max">
                <div class="row">
                    <div class="col-sm-3">
                        <h3 class="vertical-margin5"><g:message code="matching.index.message13"/></h3>
                    </div>
                    <div class="col-sm-2 form-group">
                        <g:select id="countrySelect" name="country" from="${grailsApplication.config.matchi.settings.available.countries}"
                                  valueMessagePrefix="country" value="${cmd?.country}" noSelection="['':message(code: 'country.select.all')]"
                                  class="form-control"/>
                    </div>
                    <div class="col-sm-3 form-group">
                        <select id="municipalitySelect" name="municipality" data-style="form-control" data-live-search="true" style="width: 200px;">
                            <option value=""><g:message code="municipality.select.all"/></option>
                            <g:each in="${allRegions}">
                                <optgroup label="${it.name}">
                                    <g:each in="${userCountByMunicipalities[it.id]}" var="mun">
                                        <option value="${mun?.municipality?.id}" ${cmd?.municipality == mun?.municipality?.id ? "selected" : ""}>${mun?.municipality?.name} (${mun?.numUsers})</option>
                                    </g:each>
                                </optgroup>
                            </g:each>
                        </select>
                    </div>
                    <div class="col-sm-2 form-group">
                        <select id="sportSelect" name="sport" data-style="form-control col-xs-12" data-live-search="true"  style="width: 260px;">
                            <option value=""><g:message code="sport.select.all"/></option>
                            <optgroup label="Sporter">
                                <g:each in="${allSports}">
                                    <option value="${it.id}" ${cmd?.sport == it.id ? "selected" : ""}><g:message code="sport.name.${it.id}"/></option>
                                </g:each>
                            </optgroup>
                        </select>
                    </div>
                    <div class="col-sm-2">
                        <g:submitButton name="submit" value="${message(code: 'button.search.label')}" class="btn btn-success btn-block"/>
                    </div>
                </div>
            </g:form>

        </div><!-- /.container -->
    </section>

    <!-- MATCHING PLAYERS LIST -->
    <section class="block block-white match-list vertical-padding20">
        <div class="container">
            <ul class="list-unstyled">
                <g:each in="${matches}" var="match" status="index">
                    <li class="list-unstyled">
                        <div class="panel panel-default no-border no-box-shadow bottom-border vertical-padding10">
                            <div class="panel-body">
                                <div class="row">
                                    <!-- USER PROFILE MEDIA OBJECT -->
                                    <div class="col-sm-5">
                                        <div class="media matching">
                                            <div class="media-left">
                                                <g:link controller="userProfile" action="index" id="${match.user.id}">
                                                    <div class="avatar-circle-md">
                                                        <g:fileArchiveUserImage size="medium" id="${match.user.id}" alt="${match.user.fullName()}" class="img-responsive"/>
                                                    </div>
                                                </g:link>
                                            </div><!-- /.media-left -->
                                            <div class="media-body">
                                                <h3 class="h4 no-vertical-margin weight400">
                                                    <ul class="list-table">
                                                        <li class="no-left-padding">
                                                            <!-- USER SKILL-LEVEL -->
                                                            <span class="text-xs"><g:skillLevel id="${match?.user?.id}"/></span>
                                                        </li>
                                                        <li>
                                                            <!-- USER NAME AND LINK -->
                                                            <g:link controller="userProfile" action="index" id="${match.user.id}" class="text-black">
                                                                ${match.user.fullName()}
                                                            </g:link>
                                                        </li>
                                                    </ul>
                                                </h3>

                                            <!-- USER LOCATION -->
                                                <g:if test="${match.user.municipality}">
                                                    <small class="text-xs text-muted">
                                                        <i class="fas fa-map-marker"></i>
                                                        ${match.user.municipality}${match.user.city ? " (" + match.user.city + ")" : ""} - ${match.user.municipality.region}
                                                    </small>
                                                </g:if>
                                                <g:else>
                                                    <small class="text-xs text-muted">
                                                        <i class="fas fa-map-marker"></i> <g:message code="matching.index.message14"/>
                                                    </small>
                                                </g:else>

                                            <!-- MATCHING PROCENTAGE PROGRESS BAR -->
                                                <div class="progress top-margin10 bottom-margin5" rel="tooltip" data-original-title="${message(code: 'matching.index.message15')}">
                                                    <div class="progress-bar progress-bar-brand" role="progressbar" data-transitiongoal="${match.matchingValue}" aria-valuenow="${match.matchingValue}" aria-valuemin="0" aria-valuemax="100">
                                                        <span class="">${match.matchingValue}%</span>
                                                    </div>
                                                </div>
                                                <span class="block text-sm text-muted">${match.user.firstname} <g:message code="matching.index.message16"/> ${match.matchingValue}%</span>

                                            </div><!-- /.media-body-->
                                        </div><!-- /.media -->
                                    </div><!-- /.col-sm-5 -->

                                <!-- USER SPORTS -->
                                    <div class="col-sm-2">
                                        <ul class="list-inline list-icons">
                                            <g:set var="sportProfileSports" value="${match.user.sportProfiles.collect { it.sport.id }}"/>
                                            <g:each in="${Sport.list()}" var="s">
                                                <g:if test="${sportProfileSports.contains(s.id)}">
                                                    <li class="icon"><i class="ma ma-${s.id}" rel="tooltip" title="${g.toRichHTML(text: match.user.firstname)} ${message(code: 'matching.index.message17')} <g:message code="sport.name.${s.id}"/>"></i></li>
                                                </g:if>
                                                <g:else>
                                                    <li class="icon"><i class="ma ma-${s.id}" rel="tooltip" title="<g:message code="sport.name.${s.id}"/>"></i></li>
                                                </g:else>
                                            </g:each>
                                        </ul>
                                    </div><!-- /.col-sm-2 -->

                                    <div class="col-sm-5">
                                        <g:if test="${match.user.description && match.user.description?.trim() != ""}">
                                            <p class="truncate text-sm no-padding">
                                                ${match.user.description}
                                            </p>
                                        </g:if>
                                        <g:else>
                                            <p class="text-sm text-muted"><em><g:message code="matching.index.message18"/></em></p>
                                        </g:else>
                                    </div><!-- /.col-sm-5 -->

                                </div><!-- /.row -->
                            </div><!-- /.panel-body -->
                        </div><!-- /.panel -->
                    </li>
                </g:each>
            </ul>

            <div class="space-20"></div>

            <!-- PAGINATION -->
            <div class="row text-center">
                <g:if test="${totalMatches > 10}">
                    <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" params="${params}" maxsteps="5" max="10" action="index" total="${totalMatches}" />
                    <p class="pagination-help text-muted">${cmd.offset+1}-${end} <g:message code="matching.index.message19"/> ${totalMatches}</p>
                </g:if>
                <g:elseif test="${matches?.size() == 0}">
                    <b><g:message code="matching.index.message20"/></b>
                </g:elseif>
            </div><!-- /. pagination -->

        </div><!-- /.container -->
    </section><!-- /.section block-white -->

</g:if>


<g:else>
    <!-- IF MATCHING IS NOT ACTIVATED -->
    <section class="block block-white vertical-padding60">
        <div class="container">
            <h2><g:message code="matching.index.message21"/></h2>

            <p class="lead"><g:message code="matching.index.message22"/></p>

            <hr>

            <!--<p class="vertical-padding10"><i class="fas fa-info-circle text-info"></i> Matching är... Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
            </p>

            <div class="space-10"></div>-->

            <g:link class="btn btn-success CalvertMTRegular" controller="userProfile" action="updateMatchable"
                    params="['returnUrl': g.createLink(absolute: true, controller: 'matching', action: 'index', params: [] )]"><g:message code="matching.index.message23"/></g:link>

        </div>

        <div class="space-100"></div>
        <div class="space-100"></div>
        <div class="space-100"></div>
        <div class="space-100"></div>
    </section>

</g:else>
<div class="modal fade" id="messageModal" tabindex="-1" role="dialog" aria-labelledby="messageModal" aria-hidden="true"></div>
<r:script>
    $(document).ready(function() {
        $("[rel=tooltip]").tooltip({ delay: { show: 1000, hide: 100 } });
        $(".truncate").truncateText({max:300});

        $('.progress .progress-bar').progressbar({
            transition_delay: 1000
        });

        $("#municipalitySelect").selectpicker({
            title: "${message(code: 'municipality.multiselect.noneSelectedText')}"
        });

         $("#countrySelect").selectpicker({
            title: "${message(code: 'municipality.multiselect.noneSelectedText')}"
        });

        $("#sportSelect").selectpicker({
            title: "${message(code: 'default.choose.sport')}"
        });

        if(!getCookie("hideMatchingInfo")) {
            $("#matchingInfo").show();
        }
    });
</r:script>
</body>
