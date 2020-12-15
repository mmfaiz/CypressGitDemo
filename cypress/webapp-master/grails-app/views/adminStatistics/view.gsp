<%@ page import="com.matchi.Payment; org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
    <r:script>
        $(".modal").css("top", "30%");
    </r:script>
</head>
<body>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="adminStatistics.view.title"/> ${payment.transactionId}</h3>
</div>
<div class="modal-body">
    <g:if test="${slot}">
        <g:message code="adminStatistics.view.bookedSlot"/>: <g:formatDate date="${slot.getStartTime()}" format="yyyy-MM-dd"/>
        <g:link action="refund" id="${payment.id}"><g:message code="adminStatistics.view.refund"/></g:link>
    </g:if>
    <p>
        <!--
        String merchantId
        String paymentVersion
        String orderNr
        String transactionId
        String status
        String statusCode
        String authCode
        String my3Dsec
        String batchId
        String currency
        String paymentMethod
        String expDate
        String cardType
        String riskScore
        String issueingBank
        String iPCountry
        String issueingCountry
        String amount
        String feeAmount
        String mac

        String slotId

        Date dateCreated
        Date lastUpdated
        -->
        <table width="100%" cellpadding="2" cellspacing="2" border="1">
            <%
                def d = new DefaultGrailsDomainClass(Payment.class)
            d.persistentProperties.each {
                println "<tr>"
                println "<td>"+it.name+"</td>"
                println "<td>"+payment.properties.get(it.name)+"</td>"
                println "</tr>"
            }
                //println payment.properties.get("orderNr")
            %>
        </table>
    </p>
</div>
<div class="modal-footer">
    <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-success"><g:message code="button.closewindow.label" default="Stäng fönstret"/></a>
</div>
</body>
</html>

