<%@ page import="com.matchi.FacilityAccessCode;" contentType="text/html;charset=UTF-8" %>
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
                                    margin: 15px 10px;"><g:message code="templates.emails.html.newBookingGroupNotification.title"/></h6>
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
                                                    <g:message code="templates.emails.greeting"/>
                                                    <g:if test="${greetingPerson == null}">
                                                        ${bookings[0].customer?.user ? bookings[0].customer?.user?.fullName() : bookings[0].customer?.fullName()}
                                                    </g:if>
                                                    <g:else>
                                                        ${greetingPerson.encodeAsHTML()}
                                                    </g:else>
                                                </h3>
                                                <!-- /Welcome block -->

                                                <!-- Welcome ingress -->
                                                <p class="lead" style="font-weight: normal;font-size: 17px;line-height: 1.6;">
                                                    <g:message code="templates.emails.html.salk.newBookingGroupNotification.text1"/>
                                                </p>
                                            <!-- /Welcome ingress -->

                                            <!-- Message body -->
                                                <g:each in="${bookings}" var="booking">
                                                    <g:message code="default.date.label"/>: <strong><g:formatDate date="${booking.slot.startTime}" format="yyyy-MM-dd"/></strong><br>
                                                    <g:message code="default.date.time"/>:  <strong><g:formatDate date="${booking.slot.startTime}" format="HH:mm"/>-<g:formatDate date="${booking.slot.endTime}" format="HH:mm"/></strong><br>
                                                    <g:message code="court.label"/>: <strong>${booking.slot.court.name}</strong><br>
                                                    <g:if test="${booking?.slot?.court?.description && booking?.slot?.court?.showDescriptionOnline}">
                                                        <strong><i>${booking?.slot?.court?.description?.encodeAsHTML()}</i></strong>
                                                    </g:if>
                                                    <g:set var="accessCode" value="${com.matchi.FacilityAccessCode.validAccessCodeFor(booking.slot)?.content}"/>
                                                    <g:if test="${accessCode}">
                                                        <br><g:message code="templates.emails.accessCode"/>: <strong>${accessCode}</strong>
                                                    </g:if>

                                                    <g:if test="${booking.players}">
                                                        <br/><g:message code="player.label.plural"/>:
                                                        <strong><g:bookingPlayers players="${booking.players}"/></strong>
                                                    </g:if>
                                                    <br>
                                                    <br>
                                                </g:each>
                                            <!-- /Message body -->
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <!-- Callout Panel -->
                                                <table width="100%" cellpadding="10" bgcolor="#e4eed6" style="background-color: #e4eed6;width: 100%;">
                                                    <tr>
                                                        <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                            <em>
                                                                <g:message code="templates.emails.html.salk.newBookingGroupNotification.text2" args="[bookings[0]?.slot?.court?.facility?.getBookingCancellationLimit()]"/>
                                                            </em>

                                                            <g:if test="${bookings[0]?.slot?.court?.description && bookings[0]?.slot?.court?.showDescriptionOnline}">
                                                                <br><br>
                                                                ${bookings[0]?.slot?.court?.description?.encodeAsHTML()}
                                                            </g:if>

                                                            <g:if test="${bookings[0].slot.court.facility.bookingNotificationNote}">
                                                                <br><br>
                                                                ${bookings[0].slot.court.facility.bookingNotificationNote}
                                                            </g:if>
                                                        </td>
                                                    </tr>
                                                </table>
                                                <!-- /Callout Panel -->
                                            </td>
                                        </tr>
                                        <g:if test="${sendInvite}">
                                            <tr>
                                                <!-- Invite -->
                                                <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                    <g:if test="${bookings[0]?.customer?.isCompany()}">
                                                        <g:message code="templates.emails.invite.company" args="[bookings[0]?.customer?.fullName()]"/><br>
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
                                                <g:render template="/templates/emails/recording"
                                                          model="[booking: booking]"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <!-- social & contact -->
                                                <table width="100%" cellpadding="15" bgcolor="#ebebeb" style="background-color: #ebebeb;">
                                                    <tr>
                                                        <!-- column 1 -->
                                                        <td width="100%" valign="top" style="font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif;line-height: 1.1;color: #000;font-weight: 900;font-size: 17px;">
                                                        <g:message code="templates.emails.contactSalk"/>:
                                                        <table width="100%" cellpadding="0" cellspacing="0" style="width: 100%;">
                                                            <tr><td style="height: 5px;"></td></tr>
                                                            <tr><td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                                <strong><g:message code="templates.emails.html.salk.newBookingNotification.indoorCourtsHeadline"/>:</strong><br/>
                                                                Salkhallen<br/>
                                                                Gustavslundsvägen 159, 167 51 Bromma<br/>
                                                                08-564 356 00, <a href="mailto:info@salk.se" style="color: #7faf3e;">info@salk.se</a>, <a href="http://www.salk.se" style="color: #7faf3e;">http://www.salk.se</a><br>
                                                                Org.nr: 556035-3624<br/><br/>
                                                                <strong><g:message code="templates.emails.html.salk.newBookingNotification.outdoorCourtsHeadline"/>:</strong><br/>
                                                                SALK Tennis Park<br/>
                                                                Riksbyvägen 43 (<g:message code="templates.emails.html.salk.newBookingNotification.byLocation" /> Brommaplan)<br/>
                                                                08-634 00 00<br/>
                                                            </td></tr>
                                                        </table>
                                                        </td><!-- /column 1 -->
                                                    </tr>
                                                </table><!-- /social & contact -->
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
                                    Powered by MATCHi
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