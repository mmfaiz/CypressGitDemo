<%@ page import="com.matchi.Facility; com.matchi.Sport" %>

<%
    def off = cmd.offset > 0 ? cmd.offset : 0
    def ind = 0 + off
%>
<div class="container">
    <g:each in="${facilities}" var="fac" status="i">
        <%
            def facility = Facility.findById(fac.id)
            def membersOnly = facility?.isMembersOnly()
        %>

        <div class="panel panel-default no-border no-box-shadow bottom-border">
            <div class="panel-body">
                <div class="row">
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
                                <h3 class="media-heading h4 top-margin10">
                                    <g:link controller="facility" action="show" params="[name: facility.shortname]">
                                        ${facility.name}
                                    </g:link>
                                </h3>
                                <p class="text-muted text-sm"><i class="fas fa-map-marker"></i> ${facility.municipality}</p>

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
                            <div class="media-right">
                                <span class="text-right text-sm">
                                    <g:if test="${user?.hasFavourite(facility)}">
                                        <g:link controller="userFavorites" action="removeFavourite" params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'index', params: params)]" class="btn btn-xs btn-primary">
                                            <span rel="tooltip" title="<g:message code="user.favorites.remove"/>"><i class="fas fa-bookmark"></i> <g:message code="user.favorites.btn"/> <i class="fa fa-check"></i></span>
                                        </g:link>
                                    </g:if>
                                    <g:elseif test="${!user?.hasFavourite(facility)}">
                                        <g:link controller="userFavorites" action="addFavourite" params="[facilityId: facility.id, returnUrl: g.createLink(absolute: false, action: 'index', params: params)]" class="btn btn-xs btn-primary btn-outline">
                                            <span rel="tooltip" title="<g:message code="user.favorites.add"/>"><i class="fa fa-bookmark-o"></i> <g:message code="user.favorites.btn"/> <i class="fas fa-plus"></i></span>
                                        </g:link>
                                    </g:elseif>
                                </span>
                            </div>
                        </div><!-- /.media -->
                    </div><!-- /.col-sm-5 -->

                    <div class="col-sm-2">
                        <!-- LIST SPORT ICONS -->
                        <ul class="list-inline list-icons">
                            <g:each in="${Sport.list()}">
                                <li class="icon">
                                    <i class="${!facility.sports.contains(it) ? 'text-grey-lighter':''} ma ma-${it.id} ma-lg" rel="${facility.sports.contains(it)?'tooltip':''}" title="<g:message code="sport.name.${it.id}"/>"></i>
                                </li>
                            </g:each>
                        </ul>
                    </div><!-- /.col-sm-2 -->

                    <div class="col-sm-5">
                    <!-- DESCRIPTION -->
                        <g:if test="${facility.description && facility.description?.trim() != ""}">
                            <p class="text-sm truncate">
                                ${facility.description}
                            </p>
                        </g:if>
                    </div><!-- /.col-sm-5 -->

                </div><!-- /.row -->

                <div class="clearfix"></div>

            </div><!-- /.panel-body -->
        </div><!-- /.panel -->
        <% ind++ %>
    </g:each>

    <div class="space-20"></div>

    <!-- PAGINATION -->
    <div class="row text-center">
        <div class="col-md-12">
            <g:if test="${totalCount > cmd.max}">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" params="${params}"
                                              maxsteps="5" max="10" action="index" total="${totalCount}" />
                <p class="pagination-help text-muted"><g:message code="book.facilities.results"
                                                                 args="[cmd.offset+1, ind, totalCount]"/></p>
            </g:if>
            <g:elseif test="${facilities?.size() == 0}">
                <div class="vertical-margin40">
                    <strong><g:message code="book.facilities.results.empty"/></strong>
                </div>
            </g:elseif>
        </div>
    </div><!-- /.pagination -->

</div><!-- /.container -->

<script type="text/javascript">
    $(function() {
        var facility;

        <g:each in="${facilities}" var="facility">
            facility = {
                id: "${g.forJavaScript(data: facility.id)}",
                shortname: "${g.forJavaScript(data: facility.shortname)}",
                name: "${g.forJavaScript(data: facility.name)}",
                address: "${g.forJavaScript(data: facility.address)}",
                zipcode: "${g.forJavaScript(data: facility.zipcode)}",
                city: "${g.forJavaScript(data: facility.city)}",
                lat: "${g.forJavaScript(data: facility.lat)}",
                lng: "${g.forJavaScript(data: facility.lng)}"
            };

            plotMarker(facility, false);
        </g:each>
        <g:each in="${restOfFacilities}" var="facility" status="i">
            facility = {
                id: "${g.forJavaScript(data: facility.id)}",
                shortname: "${g.forJavaScript(data: facility.shortname)}",
                name: "${g.forJavaScript(data: facility.name)}",
                address: "${g.forJavaScript(data: facility.address)}",
                zipcode: "${g.forJavaScript(data: facility.zipcode)}",
                city: "${g.forJavaScript(data: facility.city)}",
                lat: "${g.forJavaScript(data: facility.lat)}",
                lng: "${g.forJavaScript(data: facility.lng)}"
            };

            plotMarker(facility, true);
        </g:each>

        <g:if test="${cmd.q || cmd.municipality || cmd.sport}">
            if (bounds) {
                map.fitBounds(bounds);
            } else {
                setCenter();
            }
        </g:if>
        <g:else>
            setCenter();
        </g:else>
    });
</script>