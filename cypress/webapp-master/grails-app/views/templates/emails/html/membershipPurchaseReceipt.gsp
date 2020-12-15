<%@ page contentType="text/html;charset=UTF-8" import="com.matchi.FacilityProperty.FacilityPropertyKey" %>
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
                                    margin: 15px 10px;"><g:message code="templates.emails.html.membershipPurchaseRequest.title"/></h6>
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
                                                    <g:message code="templates.emails.greeting"/> ${membership.customer?.user ? membership.customer?.user?.fullName() : membership.customer?.fullName()}
                                                </h3>
                                                <!-- /Welcome block -->

                                                <!-- Welcome ingress -->
                                                <p class="lead" style="font-weight: normal;font-size: 17px;line-height: 1.6;">
                                                    <g:message code="templates.emails.html.membershipPurchaseRequest.message1"
                                                            args="[membership.type.name, membership.customer.facility.name]" encodeAs="HTML"/>
                                                </p>
                                                <!-- /Welcome ingress -->

                                                <!-- Message body -->
                                                <g:if test="${order}">
                                                    <hr>
                                                    <g:message code="payment.label"/>:<br>
                                                    <strong><g:paymentShortSummary order="${order}"/></strong>
                                                    <hr>
                                                </g:if>

                                                <g:if test="${membership.customer}">
                                                    <g:ifFacilityPropertyEnabled name="${FacilityPropertyKey.FEATURE_FEDERATION}" facility="${membership.customer.facility}">
                                                        <g:ifFacilityPropertyEnabled name="${FacilityPropertyKey.FEATURE_USE_CUSTOMER_NUMBER_AS_LICENSE}"facility="${membership.customer.facility}">
                                                            <g:if test="${!order}"><hr></g:if>
                                                            <g:message code="facility.property.category.FEDERATION.label"/>:<br>
                                                            <strong><g:message code="user.licence.label"/>: ${membership.customer.license}</strong>
                                                            <hr>
                                                        </g:ifFacilityPropertyEnabled>
                                                    </g:ifFacilityPropertyEnabled>
                                                </g:if>
                                                <!-- /Message body -->
                                            </td>
                                        </tr>

                                        <tr>
                                            <td>
                                                <!-- social & contact -->
                                                <g:render template="/templates/emails/facilityContactInfo"
                                                        model="[facility: membership.customer.facility]"/>
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
