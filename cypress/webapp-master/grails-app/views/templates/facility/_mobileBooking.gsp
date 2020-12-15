<%@ page import="java.text.ParseException; java.text.SimpleDateFormat;" %>

        <div class="panel panel-default visible-xs visible-sm">
            <div class="panel-heading vertical-padding10">
                <div class="row">
                    <div class="col-xs-6 col-sm-6">
                        <h3 class="h6"><g:message code="templates.facility.mobileBooking.message1"/></h3>
                        <span>
                            <a href="javascript:void(0)" id="picker-mobile">
                                <%
                                    def date = new Date()
                                    if (params.date && params.date instanceof String) {
                                        try {
                                            date = new SimpleDateFormat("yyyy-MM-dd").parse(params.date)
                                        } catch (ParseException e) {
                                        }
                                    }
                                %>
                                <g:formatDate format="EEEE dd MMMM" date="${date}"/>
                            </a>
                        </span>
                    </div>
                    <g:if test="${facility.isIndoorOutdoor()}">
                        <div class="col-xs-6 col-sm-2">
                            <div class="checkbox vertical-margin5">
                                <g:checkBox name="outdoor-switch-mobile" checked="${params.indoor == 'false'}"/>
                                <label for="outdoor-switch-mobile">
                                    <g:message code="court.outdoors.label"/>
                                </label>
                            </div>
                            <div class="checkbox vertical-margin5">
                                <g:checkBox name="indoor-switch-mobile" checked="${params.indoor == 'true'}"/>
                                <label for="indoor-switch-mobile">
                                    <g:message code="court.indoor.label"/>
                                </label>
                            </div>
                        </div>
                    </g:if>
                    <div class="col-xs-${facility.isIndoorOutdoor() ? '12' : '6'} col-sm-${facility.isIndoorOutdoor() ? '4' : '6'} text-right">
                        <div class="mobile-slot-filters list-inline">
                            <select id="sport-picker-mobile" name="sport" data-style="form-control sport-slot-select">
                                <option value=""><g:message code="sport.select.all"/></option>
                                <g:each in="${facility.sports}" var="s">
                                    <option value="${s?.id}" ${sport == s?.id ? "selected" : ""}
                                            data-content="<i class='ma ma-${s?.id}'></i> <g:message code="sport.name.${s?.id}"/>"> <g:message code="sport.name.${s?.id}"/></option>
                                </g:each>
                            </select>
                        </div>
                    </div>
                    <div class="col-xs-12 col-sm-12 text-right top-margin5">
                        <g:render template="/templates/booking/blockBook"/>
                    </div>
                </div>
            </div>
            <div id="mobile-booking" class="panel-body slots-container" style="display: none;">
                <i class="fas fa-spinner fa-spin"></i> <g:message code="schedule.get.free.slots"/>
            </div><!-- /.panel-body -->
            <div id="mobile-booking-spinner" class="panel-body">
                <i class="fas fa-spinner fa-spin"></i> <g:message code="schedule.get.free.slots"/>
            </div><!-- /.panel-body -->
        </div>
