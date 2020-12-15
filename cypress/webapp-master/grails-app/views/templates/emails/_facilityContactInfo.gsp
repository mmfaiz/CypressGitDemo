<table width="100%" cellpadding="15" bgcolor="#ebebeb" style="background-color: #ebebeb;">
    <tr>
        <td width="${facility.facebook || facility.twitter ? '50' : '100'}%" valign="top" style="font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif;line-height: 1.1;color: #000;font-weight: 900;font-size: 17px;">
            <g:message code="templates.emails.contact"/> ${facility.name.encodeAsHTML()}:
            <table width="100%" cellpadding="0" cellspacing="0" style="width: 100%;">
                <tr><td style="height: 5px;"></td></tr>
                <tr><td style="font-weight: normal;font-size: 14px;line-height: 1.6;">
                    <g:if test="${facility.address}"><strong>${facility.address}</strong><br/></g:if>
                    <strong><g:if test="${facility.zipcode}">${facility.zipcode}</g:if><g:if test="${facility.city}">, ${facility.city}</g:if></strong><br/>
                    <g:if test="${facility.telephone}"><g:message code="default.phone.label"/>: <strong>${facility.telephone}</strong><br/></g:if>
                    <g:if test="${facility.email}"><g:message code="templates.emails.contact.email"/>: <strong><a href="mailto:${facility.email}" style="color: #7faf3e;">${facility.email}</a></strong><br></g:if>
                    <g:if test="${facility.orgnr}"><em><g:message code="templates.emails.contact.orgnr"/>: ${facility.orgnr}</em></g:if>
                </td></tr>
            </table>
        </td>
        <g:if test="${facility.facebook || facility.twitter || facility.instagram}">
            <td width="50%" valign="top" style="font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif;line-height: 1.1;color: #000;font-weight: 900;font-size: 17px;">
                <g:message code="templates.emails.followFacility" args="[facility.name]" encodeAs="HTML"/>:
                <table width="100%" cellpadding="5" cellspacing="0" style="width: 100%;">
                    <g:if test="${facility.facebook}">
                        <tr><td></td></tr>
                        <tr><td style="background-color: #3B5998;padding: 5px 7px;font-weight: normal;font-size: 14px;line-height: 1.6;">
                            <a href="${facility.facebook}" class="soc-btn fb" style="font-size: 12px;text-decoration: none;color: #FFF;font-weight: bold;display: block;text-align: center;">
                                Facebook
                            </a>
                        </td></tr>
                    </g:if>
                    <g:if test="${facility.twitter}">
                        <tr><td></td></tr>
                        <tr><td style="background-color: #1daced;padding: 5px 7px;font-weight: normal;font-size: 14px;line-height: 1.6;">
                            <a href="${facility.twitter}" class="soc-btn tw" style="font-size: 12px;text-decoration: none;color: #FFF;font-weight: bold;display: block;text-align: center;">
                                Twitter
                            </a>
                        </td></tr>
                    </g:if>
                    <g:if test="${facility.instagram}">
                        <tr><td></td></tr>
                        <tr><td style="background-color: #8a3ab9;padding: 5px 7px;font-weight: normal;font-size: 14px;line-height: 1.6;">
                            <a href="${facility.instagram}" class="soc-btn in" style="font-size: 12px;text-decoration: none;color: #FFF;font-weight: bold;display: block;text-align: center;">
                                Instagram
                            </a>
                        </td></tr>
                    </g:if>
                </table>
            </td>
        </g:if>
    </tr>
</table>