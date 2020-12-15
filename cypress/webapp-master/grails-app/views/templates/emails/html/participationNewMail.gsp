<%@ page import="com.matchi.messages.FacilityMessage" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>MATCHi</title>
</head>

<body bgcolor="#efefef" style="font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif;margin:0;padding:0;background-color: #efefef;">
<!-- Wrapper -->
<table cellspacing="0" cellpadding="10" border="0" width="100%" style="width: 100%;margin:0;padding:0;">
    <tr>
        <td bgcolor="#efefef" width="100%" style="font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif;background-color: #efefef;width: 100%;">

            <!-- Header -->
            <table width="100%" style="width: 100%;">
                <tr>
                    <td></td>
                    <td style="display: block!important;max-width: 600px!important;margin: 0 auto!important;">
                        <table cellpadding="5" bgcolor="#efefef" width="100%" style="width: 100%;">
                            <tr>
                                <td align="left">
                                    <h6 class="collapse" style="font-weight: 900;
                                    font-size: 18px;
                                    text-transform: uppercase;
                                    color: #444;
                                    font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif;
                                    line-height: 1.1;
                                    margin: 15px 10px;"><g:message code="templates.emails.html.newBookingNotification.title"/></h6>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td></td>
                </tr>
            </table><!-- /Header -->

        <!-- Content -->
            <table width="100%" style="width: 100%;">
                <tr>
                    <td></td>
                    <td bgcolor="#FFFFFF" style="display: block!important;max-width: 600px!important;margin: 0 auto!important;background-color: #fff;">
                        <table cellpadding="5" width="100%" style="width: 100%;">
                            <tr>
                                <td>
                                    <table cellspacing="10" width="100%" style="width: 100%;">
                                        <tr>
                                            <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                <!-- Welcome block -->
                                                <h3 style="font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif;line-height: 1.1;margin:0 0 15px;color: #000;font-weight: 500;font-size: 27px;">
                                                    <g:message code="templates.emails.greeting"/> ${participation.customer?.user ? participation.customer?.user?.fullName() : participation.customer?.fullName()}
                                                </h3>
                                                <!-- /Welcome block -->

                                                <!-- Welcome ingress -->
                                                <p class="lead" style="font-weight: normal;font-size: 17px;line-height: 1.6;">
                                                    <g:message code="templates.emails.html.participationNewMail.text" args="[participation.occasion.activity.name, participation.occasion.startTime.toString('HH:mm'), participation.occasion.date.toString()]"/>
                                                </p>
                                                <!-- /Welcome ingress -->

                                                <!-- Message body -->
                                                <g:if test="${participation.payment}">
                                                    <hr>
                                                    <g:message code="templates.emails.paymentMethod"/>: <strong><g:message code="payment.method.${participation.payment.method}"/></strong><br>
                                                    <g:message code="default.amount.label"/>: <strong>${participation.payment.amountFormatted()}</strong><br>
                                                    <hr>
                                                </g:if>

                                                <g:if test="${participation.order}">
                                                    <hr>
                                                    <g:message code="payment.label"/>:<br>
                                                    <strong><g:paymentShortSummary order="${participation.order}"/></strong>
                                                    <hr>
                                                </g:if>

                                                <g:ifFacilityPropertyEnabled name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_ACTIVITY_ACCESS_CODE.name()}" facility="${participation.occasion.activity.facility}">
                                                    <g:set var="accessCode" value="${ participation?.occasion?.bookings ? participation?.occasion?.bookings?.first()?.getAccessCode() : NULL}"/>
                                                    <g:if test="${accessCode!=NULL &&
                                                                    (participation.occasion.bookings.size() == 1 || (participation.occasion.bookings.size() > 1 && participation.occasion.bookings.every { it.getAccessCode() == accessCode }))}">
                                                            <g:message code="templates.emails.accessCode"/>: <strong>${participation.occasion.bookings.first().getAccessCode()}</strong><br/>
                                                    </g:if>
                                                    <g:elseif test="${participation.occasion.bookings.size() > 1 && participation.occasion.bookings.any { it.getAccessCode() != NULL }}">
                                                        <table width="100%">
                                                            <tr>
                                                                <td><b><g:message code="default.league.matches.court"/></b></td>
                                                                <td><b><g:message code="default.time.label"/></b></td>
                                                                <td><b><g:message code="facility.property.category.ACCESSCODE.label"/></b></td>
                                                            </tr>
                                                        <g:each in="${participation.occasion.bookings}" var="booking">
                                                            <g:if test="${booking.getAccessCode() != NULL}">
                                                                <tr>
                                                                    <td>${booking.slot.court.name}</td>
                                                                    <td>${booking.slot.timeSpan.getFormatted("HH:mm", " - ")}</td>
                                                                    <td>${booking.getAccessCode()}</td>
                                                                </tr>
                                                            </g:if>
                                                        </g:each>
                                                        </table>
                                                    </g:elseif>
                                                </g:ifFacilityPropertyEnabled>
                                            </td>
                                        </tr>
                                        <g:if test="${participation.payment || participation.occasion}">
                                            <tr>
                                                <td>
                                                    <!-- Callout Panel -->
                                                    <table cellpadding="10" width="100%" bgcolor="#e4eed6" style="background-color: #e4eed6;width: 100%;">
                                                        <tr>
                                                            <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                                <em><g:message code="templates.emails.html.participationNewMail.cancellationPolicy" args="[cancelRule]"/></em>
                                                                <g:if test="${participation.payment && participation.occasion.message}">
                                                                    <br><br>
                                                                </g:if>
                                                                <g:if test="${participation.occasion.message}">
                                                                    <g:message code="templates.emails.message"/>:<br>
                                                                    <g:toRichHTML text="${participation.occasion.message.replaceAll('\r\n', '<br/>')}" />
                                                                </g:if>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    <!-- /Callout Panel -->
                                                </td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${sendInvite}">
                                            <tr>
                                                <!-- Invite -->
                                                <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                    <g:if test="${participation?.customer?.isCompany()}">
                                                        <g:message code="templates.emails.invite.company" args="[participation?.customer?.fullName()]"/><br>
                                                    </g:if>
                                                    <g:else>
                                                        <g:message code="templates.emails.invite"/><br>
                                                    </g:else>
                                                    <a href="${inviteLink}" target="_blank">${inviteLink}</a>
                                                </td>
                                                <!-- /Invite -->
                                            </tr>
                                        </g:if>
                                        <tr>
                                            <td>
                                                <!-- social & contact -->
                                                <g:render template="/templates/emails/facilityContactInfo"
                                                        model="[facility: participation.occasion.activity.facility]"/>
                                                <!-- /social & contact -->
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>

                    </td>
                    <td></td>
                </tr>
            </table><!-- /Content -->

        <!-- Footer -->
            <table width="100%" style="width: 100%;">
                <tr>
                    <td></td>
                    <td style="display: block!important;max-width: 600px!important;margin: 0 auto!important;">
                        <table width="100%" cellspacing="15" style="width: 100%;">
                            <tr>
                                <td align="center" style="font-weight: bold;font-size: 12px;line-height: 1.6;text-align:center;color:#999;">
                                    Powered by MATCHi.se
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td></td>
                </tr>
            </table><!-- /Footer -->
        </td>
    </tr>
</table><!-- /Wrapper -->

</body>
</html>

