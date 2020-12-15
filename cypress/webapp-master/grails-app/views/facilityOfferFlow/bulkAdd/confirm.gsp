<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facilityOffer.bulkAdd.title"/></title>
    </head>

    <body>
        <ul class="breadcrumb">
            <li>
                <a href="${returnUrl}">
                    <g:message code="customer.label.plural"/>
                </a>
                <span class="divider">/</span>
            </li>
            <li class="active"><g:message code="facilityOffer.bulkAdd.title"/></li>
        </ul>

        <g:render template="/templates/wizard"
                model="[steps: [message(code: 'facilityOffer.bulkAdd.step1'), message(code: 'facilityOffer.bulkAdd.step2')], current: 1]"/>

        <h3><g:message code="facilityOffer.bulkAdd.step2.heading"/></h3>
        <p class="lead">
            <g:message code="facilityOffer.bulkAdd.step2.desc" encodeAs="HTML"
                    args="[selectedOffer.type == 'Coupon' ? 1 : 2, selectedOffer.name, customerInfo.size()]"/>
        </p>

        <g:form name="couponForm">
            <table class="table table-transparent">
                <thead>
                    <tr>
                        <th><g:message code="customer.number.label"/></th>
                        <th><g:message code="default.name.label"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${customerInfo}" var="customer">
                        <tr>
                            <td>${customer.number}</td>
                            <td>${customer.name.encodeAsHTML()}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>

            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'button.next.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
                <g:submitButton name="back" class="btn btn-info pull-right right-margin5"
                    value="${message(code: 'button.back.label')}" data-toggle="button"
                    show-loader="${message(code: 'default.loader.label')}"/>
            </div>
        </g:form>
    </body>
</html>
