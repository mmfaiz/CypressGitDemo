<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <title><g:message code="default.lessonsreport.label"/></title>
</head>

<body>
<div class="container content-container">
    <g:each in="${rows}" var="row" >
        <g:b3StaticErrorMessage bean="${row}"/>
    </g:each>
    <ol class="breadcrumb">
        <li> <g:message code="default.lessonsreport.invoice"/></li>
    </ol>

    <g:form class="form-inline">
        <div class="row">
            <div class="col-sm-2"><h5><g:message code="default.article.label"/>:</h5></div>
            <div class="col-sm-4">
                <g:select name="articleId" value="${articleId}"  from="${fortnoxItems}" optionValue="descr" optionKey="id" data-live-search="true"/>
            </div>
        </div>
        <div class="row" style="padding-bottom: 5px">
            <div class="col-sm-4"><h5><g:message code="customer.label"/></h5></div>
            <div class="col-sm-5"><h5><g:message code="invoiceRow.description.label"/></h5></div>
            <div class="col-sm-3"><h5 class="pull-right"><g:message code="templates.customer.customerInvoiceRow.message4"/></h5></div>
        </div>
        <g:each in="${rows}" var="row" status="index">

            <div id="row-${index}" class="row" style="padding-bottom: 5px">
                <g:hiddenField name="rows[${index}].bookingId" value="${row.bookingId}" />
                <g:hiddenField name="rows[${index}].customerName" value="${row.customerName}" />
                <g:hiddenField name="rows[${index}].description" value="${row.description}" />
                <div class="col-sm-4">
                    ${row.customerName}
                </div>
                <div class="col-sm-5">
                    ${row.description}
                </div>
                <div class="col-sm-3">
                    <g:field type="number" name="rows[${index}].price" value="${row.price}"
                             min="1" max="${Integer.MAX_VALUE}" class="span1 pull-right ${hasErrors(bean: row, field: 'price', 'error')}"/>
                </div>
            </div>
        </g:each>
        <div class="form-actions" style="margin-top: 10px">
            <g:actionSubmit action="createInvoice" class="btn right btn-success pull-right" name="create" value="${message(code: 'button.create.label')}" />

            <g:actionSubmit action="report" class="btn btn-danger pull-right" style="margin-right: 5px" name="cancel" value="${message(code: 'button.cancel.label')}" />
        </div>
    </g:form>
</div>
<r:script>
    $('#articleId').selectpicker();
</r:script>
</body>