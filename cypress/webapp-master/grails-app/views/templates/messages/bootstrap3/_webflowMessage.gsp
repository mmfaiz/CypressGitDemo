<%@ page import="org.joda.time.LocalTime" %>

<g:if test="${params.message || params.error}">
    <div class="alert ${params.message ? 'alert-info' : 'alert-danger danger'} alert-notification alert-dismissible" role="alert">
        <table class="notification-content">
            <tr>
                <td class="notification-icon" width="10%">
                    <i class="fas fa-bullhorn"></i>
                </td>
                <td class="notification-message" width="80%">
                    <strong>
                        <g:ifFacilityFullRightsGranted>
                            ${ new LocalTime().toString("HH:mm:ss") }:
                        </g:ifFacilityFullRightsGranted>
                    </strong>
                    ${params.message ?: params.error}
                </td>
                <td class="notification-close" width="10%">
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                </td>
            </tr>
        </table>
    </div>
</g:if>
