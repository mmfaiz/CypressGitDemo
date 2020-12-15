<%@ page import="com.matchi.FacilityProperty" %>
<r:require modules="matchi-watch"/>
<%
    int nOccasions = facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FACILITY_UPCOMING_OCCASIONS_NUMBER.toString()).toInteger()
%>
<ul class="list-activity">
    <g:each in="${activities}" var="activity">
        <g:if test="${activity.upcomingOnlineOccasions}">
            <li class="activity-item" id="${activity.getClass().simpleName + activity.id}">
                <div class="row">
                    <!-- ACTIVITY IMG -->
                    <div class="col-sm-2">
                        <div class="thumbnail">
                            <g:fileArchiveImage file="${activity.largeImage}" class="img-responsive"/>
                        </div>
                    </div>
                    <!-- ACTIVITY DESC -->
                    <div class="col-sm-4">
                        <h4 class="no-top-margin bottom-margin5">${activity.name}</h4>
                        <div class="bottom-padding5"><g:render template="/templates/activity/activityLevel" model="[activity: activity]"/></div>
                        <g:toRichHTML text="${activity.teaser?.replaceAll('\r\n', '<br/>')}" />
                    </div>
                    <!-- ACTIVITY BOOKING TABLE -->
                    <div class="col-sm-6">
                        <h4 class="no-top-margin"><g:message code="templates.booking.listActivities.bookDirectly" /></h4>
                        <g:render template="/templates/activity/listActivityOccasions" model="[activityOccasions: activity.upcomingOnlineOccasions, facility: facility, user: user, nOccasions: nOccasions]"/>
                        <a href="javascript:void(0)" class="load-activity-occasions full-width text-center block">
                            <g:message code="templates.booking.listActivities.showMoreOccasions" args="[activity.upcomingOnlineOccasions.size()]" />
                        </a>
                    </div>
                </div>
            </li>
        </g:if>
    </g:each>
</ul>

<r:script>
    $(document).ready(function() {
       var OCCASION_PAGINATION_NUMBER = ${g.forJavaScript(data: nOccasions)};
       var $showOccasionsButtons = $('.activity-item .load-activity-occasions');

       function hideButtonIfNeeded($element) {
           var nOccasionsLeft = $element.parent().find('tr.activity-occasion.hidden').length;

           if(nOccasionsLeft === 0) {
               $element.hide();
           }
       };

       $showOccasionsButtons.on('click', function (e) {
           var $element = $(e.target);
           $element.parent().find('tr.activity-occasion.hidden').slice(0,OCCASION_PAGINATION_NUMBER).removeClass('hidden');
           $element.parent().find('tr.hidden').slice(0,OCCASION_PAGINATION_NUMBER).removeClass('hidden');
           hideButtonIfNeeded($element);
       });

       $showOccasionsButtons.each(function (i, el) {
            hideButtonIfNeeded($(el));
       });
    });
</r:script>