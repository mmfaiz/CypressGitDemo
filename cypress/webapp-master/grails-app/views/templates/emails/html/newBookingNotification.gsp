<%@ page import="com.matchi.FacilityProperty; com.matchi.messages.FacilityMessage" contentType="text/html;charset=UTF-8" %>
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
                                                    <g:message code="templates.emails.greeting"/>
                                                    <g:if test="${greetingPerson == null}">
                                                        ${booking.customer?.user ? booking.customer?.user?.fullName() : booking.customer?.fullName()}
                                                    </g:if>
                                                    <g:else>
                                                        ${greetingPerson.encodeAsHTML()}
                                                    </g:else>
                                                </h3>
                                                <!-- /Welcome block -->

                                                <!-- Welcome ingress -->
                                                <p class="lead" style="font-weight: normal;font-size: 17px;line-height: 1.6;">
                                                    <g:message code="templates.emails.html.newBookingNotification.text" args="[booking.slot.court.facility.name]"/>
                                                </p>
                                                <!-- /Welcome ingress -->

                                                <!-- Message body -->
                                                <g:message code="default.date.label"/>: <strong><g:formatDate date="${booking.slot.startTime}" format="yyyy-MM-dd"/></strong><br>
                                                <g:message code="default.date.time"/>:  <strong><g:formatDate date="${booking.slot.startTime}" format="HH:mm"/>-<g:formatDate date="${booking.slot.endTime}" format="HH:mm"/></strong><br>
                                                <g:message code="court.label"/>: <strong>${booking.slot.court.name}</strong><br>
                                                <g:if test="${booking?.slot?.court?.description && booking?.slot?.court?.showDescriptionOnline}">
                                                    <strong><i>${booking?.slot?.court?.description?.encodeAsHTML()}</i></strong>
                                                </g:if>

                                                <g:if test="${booking.getAccessCode()}">
                                                    <br><g:message code="templates.emails.accessCode"/>: <strong>${booking.getAccessCode()}</strong>
                                                </g:if>

                                                <g:if test="${booking.players}">
                                                    <br/><g:message code="player.label.plural"/>:
                                                    <strong><g:bookingPlayers players="${booking.players}"/></strong>
                                                </g:if>

                                                <g:if test="${payment}">
                                                    <hr>
                                                    <g:message code="templates.emails.paymentMethod"/>: <strong><g:message code="payment.method.${payment.method}"/></strong><br>
                                                    <g:message code="default.amount.label"/>: <strong>${payment.amountFormatted()}</strong><br>
                                                    <hr>
                                                </g:if>

                                                <g:if test="${!hidePayment && booking.order?.isFinalPaid()}">
                                                    <hr>
                                                    <g:message code="payment.label"/>:<br>
                                                    <strong><g:paymentShortSummary order="${booking.order}"/></strong>
                                                    <hr>
                                                </g:if>
                                            <!-- /Message body -->
                                            </td>
                                        </tr>
                                        <g:if test="${booking.showRemotePaymentNotificationInEmail()}">
                                            <tr>
                                                <td>
                                                    <!-- Remote payment section -->
                                                    <table width="100%" cellpadding="10" bgcolor="#e4eed6" style="background-color: #e4eed6;width:100%;">
                                                        <tr>
                                                            <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                                <p><g:message code="templates.emails.link.to.your.pending.payment"/>
                                                                    <a href="${remotePaymentLink}">${remotePaymentLink}</a> </p>
                                                                <p><g:message code="templates.emails.link.to.all.pending.payments"/>
                                                                    <a href="${remotePaymentPageLink}">${remotePaymentPageLink}</a></p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </g:if>
                                        <g:if test="${!hidePayment && (booking.order?.isFinalPaid() || booking.slot.court.facility.bookingNotificationNote)}">
                                            <tr>
                                                <td>
                                                    <!-- Callout Panel -->
                                                    <table width="100%" cellpadding="10" bgcolor="#e4eed6" style="background-color: #e4eed6;width:100%;">
                                                        <tr>
                                                            <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">

                                                                <g:cancellationTerms slot="${booking.slot}"/>

                                                                <g:if test="${booking?.slot?.court?.description && booking?.slot?.court?.showDescriptionOnline}">
                                                                    <br><br>
                                                                    ${booking?.slot?.court?.description?.encodeAsHTML()}
                                                                </g:if>

                                                                <g:if test="${booking?.slot?.court?.facility?.bookingNotificationNote}">
                                                                    <br><br>
                                                                    <strong><g:message code="templates.emails.messageFrom"/> ${booking?.slot?.court?.facility?.name}:</strong><br>
                                                                    ${booking.slot?.court?.facility?.bookingNotificationNote?.encodeAsHTML()}
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
                                                <td>
                                                    <!-- Invite -->
                                                    <table width="100%" cellpadding="10" bgcolor="#e4eed6" style="background-color: #e4eed6;width:100%;">
                                                        <tr>
                                                            <td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                                                                <g:if test="${booking?.customer?.isCompany()}">
                                                                    <g:message code="templates.emails.invite.company" args="[booking?.customer?.fullName()]"/><br>
                                                                </g:if>
                                                                <g:else>
                                                                    <g:message code="templates.emails.invite"/><br>
                                                                </g:else>
                                                                <a href="${inviteLink}" target="_blank">${inviteLink}</a>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    <!-- /Invite -->
                                                </td>
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
                                                <g:render template="/templates/emails/facilityContactInfo"
                                                          model="[facility: booking.slot.court.facility]"/>
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
