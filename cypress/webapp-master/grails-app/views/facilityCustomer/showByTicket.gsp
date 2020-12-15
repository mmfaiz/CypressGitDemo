<html>
<head>
    <meta name="layout" content="b3main">
    <title><g:message code="facilityCustomer.showByTicket.title"/></title>
</head>

<body>
<div class="block top-margin50 vertical-padding40">
    <div class="container">
        <div class="row">
            <div class="col-md-6 col-md-offset-3 col-xs-12 text-center">
                <div class="page-header">
                    <h1 class="h2"><g:message code="facilityCustomer.showByTicket.title"/></h1>
                </div>

                <g:if test="${ticket}">
                    <p class="lead"><g:message code="facilityCustomer.showByTicket.description"
                                               args="[ticket.customer.facility.name]" encodeAs="HTML"/></p>

                    <p class="">
                        <g:form action="disableClubMessagesByTicket" class="form-horizontal form-well text-center">
                            <g:hiddenField name="ticket" value="${ticket.key}"/>
                            <g:submitButton name="sumbit" class="btn btn-success"
                                            value="${message(code: 'facilityCustomer.showByTicket.submit.label')}"/>
                        </g:form>
                    </p>
                </g:if>
                <g:else>
                    <div class="page-header">
                        ${errorMessage}
                    </div>
                </g:else>
            </div><!-- /.col-md-6 -->
        </div>
    </div>
</div>
</body>
</html>
