<%
def percentageDone = ((status?.getNumDone() / status?.getTotal()) * 100).doubleValue().round()
%>

<g:if test="${status.hasErrors()}">
    <div class="alert alert-warning" role="alert">
        <a class="close" data-dismiss="alert" href="#">×</a>
        <g:message code="facilityCustomerSMSMessage.message.status.message1"/>
        <br>
    </div>
</g:if>

<div class="progress progress-striped ${percentageDone == 100?:"active"}">
    <div class="bar" style="width: ${percentageDone}%;">${percentageDone}%</div>
</div>

<table class="table table-transparent">
    <thead>
    <tr>
        <th width="60"><g:message code="facilityCustomerSMSMessage.message.status.message2"/></th>
        <th width="200"><g:message code="default.name.label"/></th>
        <th width="200"><g:message code="customer.telephone.label"/></th>
        <th><g:message code="default.status.label"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${status?.statuses?.values()}" var="s">
        <tr>
            <td>${s?.customerNumber}</td>
            <td>${s?.customerName}</td>
            <td>${s?.customerPhone}</td>
            <td>
                <g:if test="${s?.status == "sending"}">
                    <span class="label label-info"><i class="fas fa-spinner fa-spin absolute-center"></i> <g:message code="facilityCustomerSMSMessage.message.status.message6"/></span>

                </g:if>
                <g:if test="${s?.status == "ok"}">
                    <span class="label label-success"><g:message code="facilityCustomerSMSMessage.message.status.message7"/></span>
                </g:if>
                <g:if test="${s?.status == "error"}">
                    <span class="label label-warning"><g:message code="facilityCustomerSMSMessage.message.status.message8"/></span>
                    <span class="text-hint">${s?.text}</span>
                </g:if>
                <g:if test="${s?.status == "waiting"}">
                    <span class="label label-default"><g:message code="facilityCustomerSMSMessage.message.status.message9"/></span>
                </g:if>
            </td>
        </tr>
    </g:each>

    </tbody>
</table>


