<g:each in="${address.names}">
    <b>${it}</b><br/>
</g:each>
${address.address1}<br/>
<g:if test="${address.address2}">
    ${address.address2}
    <br/>
</g:if>
${address.zipcode} ${address.city}<br/>
