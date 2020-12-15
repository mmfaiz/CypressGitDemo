<%@ page import="org.joda.time.LocalTime" %>
<g:hasErrors bean="${bean}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#"><i class="fas fa-times"></i></a>

        <h4 class="alert-heading">
            <g:ifFacilityFullRightsGranted>
                ${ new LocalTime().toString("HH:mm:ss") }:
            </g:ifFacilityFullRightsGranted>
        </h4>
        <g:renderErrors bean="${bean}" as="list" />
    </div>
</g:hasErrors>
