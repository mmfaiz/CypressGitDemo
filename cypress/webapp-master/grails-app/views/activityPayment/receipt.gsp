<%@ page import="org.joda.time.DateTime; com.matchi.Facility" %>

<%
    Facility facility = occasion.activity?.facility
%>

<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: "parent.location.href = removeParam('comeback', parent.location.href);"]}">

<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: facility, orders: [order]]" />

    <h1 class="h3"><g:message code="default.modal.thankYou"/></h1>
    <p><g:message code="activityPayment.receipt.description"/></p>

    <g:if test="${occasion.message}">
        <p><strong><g:message code="activityPayment.receipt.message"/>:</strong>
            <br>${g.toRichHTML(text: occasion.message.replaceAll('\r\n', '<br/>'))}
        </p>
    </g:if>

    <hr>

    <h2 class="h4">${occasion.activity.name} (${occasion.lengthInMinutes()}${message(code: 'unit.min')})</h2>

    <div class="row">

        <div class="col-sm-4">
            <h6 class="text-muted"><g:message code="default.date.label"/></h6>
            <h4><g:humanDateFormat date="${new org.joda.time.DateTime(occasion.date.toDate())}"/></h4>
        </div>
        <div class="col-sm-3">
            <h6 class="text-muted"><g:message code="default.date.time"/></h6>
            <h4 class="ellipsis">${occasion.startTime.toString("HH:mm")}</h4>
        </div>
        <div class="col-sm-5">
            <h6 class="text-muted"><g:message code="default.price.label"/></h6>
            <h4 class="ellipsis">
                <g:formatMoney value="${order.total()}" facility="${facility}" /><br>
                <small>(<g:message code="activityPayment.confirm.vat"/>, ${order.vat()})</small>
            </h4>
        </div>
    </div>

    <hr>
</g:applyLayout>