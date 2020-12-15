<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facilityOffer.copy.title.${offerType == 'Coupon' ? 'coupon' : 'giftCard'}"/></title>
    </head>

    <body>
        <ul class="breadcrumb">
            <li>
                <a href="${returnUrl}">
                    <g:message code="${offerType == 'Coupon' ? 'offers.coupon.label' : 'offers.giftCard.label'}"/>
                </a>
                <span class="divider">/</span>
            </li>
            <li class="active"><g:message code="facilityOffer.copy.title.${offerType == 'Coupon' ? 'coupon' : 'giftCard'}"/></li>
        </ul>

        <g:render template="/templates/wizard"
                model="[steps: [message(code: 'facilityOffer.copy.step1'), message(code: 'facilityOffer.copy.step2')], current: 0]"/>

        <g:if test="${error}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert" href="#">×</a>
                <h4 class="alert-heading">
                    ${new org.joda.time.LocalTime().toString("HH:mm:ss")}:  <g:message code="default.error.heading"/>
                </h4>
                ${error}
            </div>
        </g:if>

        <h3><g:message code="facilityOffer.copy.step1.heading"/></h3>
        <p class="lead">
            <g:message code="facilityOffer.copy.step1.desc"/>
        </p>

        <g:form name="couponForm">
            <div class="well">
                <table class="table table-transparent">
                    <thead>
                        <tr>
                            <th class="span3"></th>
                            <th class="span9"><g:message code="coupon.name.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${cmd?.items}" var="item" status="i">
                            <g:hiddenField name="items[$i].offerId" value="${item.offerId}"/>
                            <g:hiddenField name="items[$i].offerName" value="${item.offerName}"/>
                            <tr>
                                <td>${item.offerName.encodeAsHTML()}</td>
                                <td>
                                    <g:textField name="items[$i].name" class="span4" value="${item.name}"
                                            maxlength="255" required="required"/>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </div>

            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'button.copy.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
            </div>
        </g:form>
    </body>
</html>
