<%@ page import="org.joda.time.LocalTime" %>
<g:if test="${flash.message}">
    <div id="alertMsg" class="alert alert-success">
        <a class="close" data-dismiss="alert" href="javascript:void(0)"><i class="fas fa-times"></i></a>

        <h4 class="alert-heading">
            <g:ifFacilityFullRightsGranted>
                ${ new LocalTime().toString("HH:mm:ss") }:
            </g:ifFacilityFullRightsGranted>
            ${g.toRichHTML(text: flash.message)}
        </h4>
    </div>
</g:if>
