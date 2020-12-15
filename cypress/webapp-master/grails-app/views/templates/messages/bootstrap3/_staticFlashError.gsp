<%@ page import="org.joda.time.LocalTime" %>
<g:if test="${flash.error}">
    <div class="alert alert-danger alert-notification danger alert-dismissible message-container" role="alert">
        <div class="container flash-message">
            <div class="flash-inner">
                <strong class="text-danger text-md right-margin10"><i class="fas fa-exclamation-triangle"></i></strong>
                <strong>
                    <g:ifFacilityFullRightsGranted>
                        ${ new LocalTime().toString("HH:mm:ss") }:
                    </g:ifFacilityFullRightsGranted>
                </strong>
                ${flash.error}
            </div>
        </div>
        <button type="button" class="close" data-dismiss="alert" aria-label="Close"><i class="fa fa-times-circle"></i></button>
    </div>
</g:if>
