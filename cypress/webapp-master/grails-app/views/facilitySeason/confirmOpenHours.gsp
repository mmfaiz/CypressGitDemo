<%@ page import="org.joda.time.LocalTime; com.matchi.Season; org.joda.time.DateTimeConstants; com.matchi.Court; com.matchi.Sport; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySeason.openHours.title"/></title>
    <r:require modules="jquery-validate, jquery-timepicker"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="season.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link action="edit" id="${season.id}"><g:message code="facilitySeason.edit.heading"/></g:link> <span class="divider">/</span></li>
    <li><g:link action="editOpenHours" id="${season.id}"><g:message code="facilitySeason.openHours.edit"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySeason.confirmOpenHours.message5"/></li>
</ul>
<g:errorMessage bean="${cmd}"/>

<div id="errorMessage" class="alert alert-error" style="display: none;">
    <a class="close" data-dismiss="alert" href="#">×</a>

    <h4 class="alert-heading">${ new LocalTime().toString("HH:mm:ss") }: <g:message code="default.error.heading"/></h4>

    <g:renderErrors bean="${bean}" as="list" />
</div>

<h4><g:message code="facilitySeason.confirmOpenHours.message6"/></h4>
<p><g:message code="facilitySeason.confirmOpenHours.message7"/></p>
<g:form id="${season.id}"  name="seasonForm" action="saveOpenHours" method="post">
    <fieldset>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th><g:message code="facilitySeason.confirmOpenHours.message8"/></th>
                <th><g:message code="court.label"/></th>
                <th><g:message code="facilitySeason.confirmOpenHours.message10"/></th>
                <th><g:message code="facilitySeason.confirmOpenHours.message11"/></th>
            </tr>
            </thead>

            <g:each in="${newOpeningHours}" var="newOpeningHour">
                <%
                    def oldOpenHours = openingHours.get("${newOpeningHour.value.weekDay}_${newOpeningHour.value.court.id}");
                    def newOpenHours = newOpeningHour.value // map entry
                %>

                <g:if test="${(!oldOpenHours || (oldOpenHours.opens != newOpenHours.opens || oldOpenHours.closes != newOpenHours.closes || oldOpenHours.timeBetween != newOpenHours.timeBetween || oldOpenHours.bookingLength != newOpenHours.bookingLength ))}">
                    <tr>
                        <td><g:message code="time.weekDay.${newOpeningHour?.value?.weekDay}"/></td>
                        <td>${newOpeningHour?.value?.court?.name}</td>
                        <td>${oldOpenHours?.opens?.toString("HH:mm")} - ${oldOpenHours?.closes?.toString("HH:mm")}</td>
                        <td><strong>${newOpeningHour?.value.opens.toString("HH:mm")} - ${newOpeningHour?.value?.closes.toString("HH:mm")}</strong></td>

                    </tr>

                </g:if>
            </g:each>

        </table>

        <g:if test="${existingBookings.size() > 0}">
            <br>
            <h3><g:message code="facilitySeason.confirmOpenHours.message20" args="[existingBookings.size()]"/></h3>
            <p><g:message code="facilitySeason.confirmOpenHours.message12"/></p>

            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th><g:message code="default.date.label"/></th>
                    <th><g:message code="court.label"/></th>
                    <th><g:message code="user.label.plural"/></th>
                    <th><g:message code="facilitySeason.confirmOpenHours.message16"/></th>
                </tr>
                </thead>

                <g:each in="${existingBookings}" var="booking">

                    <tr>
                        <td><g:slotFullTime slot="${booking.slot}"/></td>
                        <td>${booking.slot.court.name}</td>
                        <td>${booking.user.fullName()}</td>
                        <td>
                            <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                          onFailure="handleAjaxError()" onSuccess="showLayer()"
                                          params="['cancelSlotsData': booking.slot.id,
                                                  'id': season.id,
                                                  'returnUrl': g.createLink(absolute: true, action: 'confirmOpenHours', id:season.id),
                                                  'cancelAction': 'confirm']"><g:message code="button.unbook.label"/></g:remoteLink>
                        </td>

                    </tr>
                </g:each>

            </table>
        </g:if>

        <div class="form-actions">
            <g:if test="${existingBookings.size() > 0}">
                <g:submitButton disabled="true" name="save" onclick="alert('${message(code: 'facilitySeason.confirmOpenHours.message19')}')" value="${message(code: 'button.save.label')}" class="btn btn-black"/>
            </g:if>
            <g:else>
                <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success" show-loader="${message(code: 'default.loader.label')}"/>
            </g:else>
            <g:link action="edit" id="${season.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>

<div id="bookingModal" class="modal hide fade"></div>
<g:javascript>
    $(document).ready(function() {
        $('#bookingModal').modal({show:false});
    });
</g:javascript>
</body>
</html>
