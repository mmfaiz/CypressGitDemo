<%@ page import="org.joda.time.LocalTime" %>
<g:if test="${flash.message}">
    <div class="alert alert-dismissible no-alert-style message-container" role="alert">
        <div class="container flash-message">
            <div class="flash-inner">
                <strong class="text-success text-md right-margin10"><i class="fas fa-bullhorn"></i></strong>
                <strong>
                    <g:ifFacilityFullRightsGranted>
                        ${ new LocalTime().toString("HH:mm:ss") }:
                    </g:ifFacilityFullRightsGranted>
                </strong>
                ${g.toRichHTML(text: flash.message)}
            </div>
        </div>
        <button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="fa fa-times-circle"></i></button>
    </div>
</g:if>
