<%@ page import="org.joda.time.LocalTime; com.matchi.Season; org.joda.time.DateTimeConstants; com.matchi.Court; com.matchi.Sport; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySeason.openHours.title"/></title>
    <r:require modules="jquery-validate, jquery-timepicker"/>
</head>
<body>

<g:errorMessage bean="${cmd}"/>

<div id="errorMessage" class="alert alert-error" style="display: none;">
    <a class="close" data-dismiss="alert" href="#">×</a>

    <h4 class="alert-heading">${ new LocalTime().toString("HH:mm:ss") }: <g:message code="default.error.heading"/></h4>

    <g:renderErrors bean="${bean}" as="list" />
</div>


<div class="hero-unit">
    <h2><g:message code="default.modal.done"/></h2>


    <h3><g:message code="facilitySeason.receiptOpenHours.message5" args="[season.name]" encodeAs="HTML"/></h3>
    <p><g:message code="facilitySeason.receiptOpenHours.message3"/></p>

    <p>
        <g:link action="edit" id="${season.id}" class="btn btn-success btn-large"><g:message code="facilitySeason.receiptOpenHours.message4"/></g:link>
    </p>

</div>


</body>
</html>
