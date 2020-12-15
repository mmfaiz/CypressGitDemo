<%@ page import="com.matchi.Sport; com.matchi.Facility" %>
<g:each in="${facilities}" var="fac" status="i">
    <%
        def facility = fac["slotsData"].facility
        def membersOnly = facility?.isMembersOnly()
    %>
    <div class="panel panel-default no-border no-box-shadow bottom-border">
        <div class="panel-body">
            <div class="row">
                <div class="col-sm-5">
                    <div class="media">
                        <div class="media-left">
                            <div class="thumbnail bg-white avatar-square-md">
                                <g:link controller="facility" action="show" params="[name: facility.shortname, date: params.date, sport: params.sport]">
                                    <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}" alt="${facility.name}" class="img-responsive"/>
                                </g:link>
                            </div>
                        </div>
                        <div class="media-body facility-info-text">
                            <h3 class="media-heading h4">
                                <g:link controller="facility" action="show" params="[name: facility.shortname, date: params.date, sport: params.sport]">
                                    ${facility.name}
                                </g:link>
                            </h3>
                            <p class="text-muted text-sm"><i class="fas fa-map-marker"></i> ${facility.municipality}</p>

                            <div class="text-sm bottom-margin10">
                                <g:if test="${user?.hasFavourite(facility)}">
                                    <g:link controller="userFavorites" action="removeFavourite" params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'index', params: params)]" class="btn btn-xs btn-primary">
                                        <span rel="tooltip" title="<g:message code="user.favorites.remove"/>"><i class="fa fa-bookmark"></i> <g:message code="user.favorites.btn"/> <i class="fa fa-check"></i></span>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${!user?.hasFavourite(facility)}">
                                    <g:link controller="userFavorites" action="addFavourite" params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'index', params: params)]" class="btn btn-xs btn-primary btn-outline">
                                        <span rel="tooltip" title="<g:message code="user.favorites.add"/>"><i class="fa fa-bookmark-o"></i> <g:message code="user.favorites.btn"/> <i class="fa fa-plus"></i></span>
                                    </g:link>
                                </g:elseif>
                            </div>

                            <g:if test="${!membersOnly && facility?.allPublicBookableCourts}">
                                <span class="text-sm">
                                    <g:message code="book.facilities.allPublicBookableCourts"
                                               args="[facility.allPublicBookableCourts.size()]"/>
                                </span>
                            </g:if>

                            <!-- AVAILIBILITY -->
                            <g:if test="${facility.bookable}">
                                <g:if test="${membersOnly}">
                                    <span class="block text-sm text-info vertical-margin10">
                                        <i class="fa fa-info-circle" rel="tooltip" data-title="${message(code: 'book.facilities.availability')}"></i>
                                        <g:message code="facility.bookable.status.members.only"/>
                                    </span>
                                </g:if>
                            </g:if>

                            <g:else>
                                <span class="block text-sm text-danger vertical-margin10">
                                    <i class="fa fa-info-circle" rel="tooltip" data-title="${message(code: 'book.facilities.availability')}"></i>
                                    <g:message code="facility.bookable.status.not.bookable"/>
                                </span>
                            </g:else>
                        </div><!-- /.media-body -->
                    </div><!-- /.media -->
                </div><!-- /.col-sm-5 -->

                <div id="slots_${facility.id}" class="col-sm-7 slots-container top-margin10">
                    <g:render template="listSlots" model="${fac["slotsData"]}" />
                </div><!-- /.col-sm-7 -->

            </div><!-- /.row -->

            <div class="clearfix"></div>

        </div><!-- /.panel-body -->
    </div><!-- /.panel -->
</g:each>

<!-- PAGINATION -->
<div class="row text-center">
    <div class="col-md-12">
        <g:if test="${count > cmd.max}">
            <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" params="${params}"
            maxsteps="5" max="10" action="index" total="${count}" />
            <p class="pagination-help text-muted">
                <g:set var="countResult" value="${(cmd.offset+cmd.max) > count?count:(cmd.offset+cmd.max)}"/>
                <g:message code="book.facilities.results" args="[cmd.offset+1, countResult, count]"/>
            </p>
        </g:if>
        <g:elseif test="${facilities?.size() == 0}">
            <div class="vertical-margin40">
                <strong><g:message code="book.facilities.results.empty"/></strong>
            </div>
        </g:elseif>
    </div>
</div><!-- /.pagination -->

<g:if test="${request.xhr}">
    <r:layoutResources disposition="defer"/>
</g:if>
