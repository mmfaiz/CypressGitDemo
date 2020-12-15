<html>
<head>
    <meta name="layout" content="facilityLayout">
    <title><g:message code="couponPrice.label.plural"/></title>
</head>

<body>
    <g:errorMessage bean="${coupon}"/>

    <ul class="breadcrumb">
        <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
        <li class="active"><g:message code="couponPrice.label.plural"/>: ${coupon.name}</li>
    </ul>

    <ul class="nav nav-tabs">
        <li><g:link  mapping="${params.type}" action="sold" id="${coupon.id}"><g:message code="customer.label.plural"/></g:link></li>
        <li class="active">
            <g:link mapping="${params.type + 'Prices'}" action="index" id="${coupon.id}">
                <g:message code="couponPrice.label.plural"/>
            </g:link>
        </li>
        <li>
            <g:link mapping="${params.type}" action="edit" id="${coupon.id}"><g:message code="button.edit.label"/></g:link>
        </li>
        <li><g:link mapping="${params.type + 'Conditions'}" action="list" id="${coupon.id}"><g:message code="default.terms.label"/></g:link></li>
    </ul>

    <g:form mapping="${params.type + 'Prices'}" action="save" >
        <g:hiddenField name="id" value="${coupon.id}"/>
        <g:hiddenField name="type" value="${params.type}"/>
        <table class="table table-striped table-bordered">
            <thead>
                <tr>
                    <g:each in="${priceListCustomerCategories}" var="category">
                        <th>${category.name}</th>
                    </g:each>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <g:each in="${priceListCustomerCategories}" var="category" status="i">
                        <g:set var="couponPrice" value="${coupon.prices.find{it.customerCategory.id == category.id}}"/>
                        <g:if test="${couponPrice}">
                            <g:hiddenField name="prices[${i}].id" value="${couponPrice.id}"/>
                        </g:if>
                        <g:hiddenField name="prices[${i}].customerCategory.id" value="${category.id}"/>

                        <td width="250" style="color: #999">
                            <label><g:currentFacilityCurrency facility="${coupon.facility}"/></label>
                            <g:textField name="prices[${i}].price" value="${couponPrice?.price}"
                                    maxlength="9" style="width: 30px"/>
                        </td>
                    </g:each>
                </tr>
            </tbody>
        </table>

        <div class="form-actions">
            <g:actionSubmit value="${message(code: 'button.save.label')}" action="save" class="btn btn-success"/>
            <g:link mapping="${params.type}" action="index" class="btn btn-danger">
                <g:message code="button.cancel.label"/>
            </g:link>
        </div>
    </g:form>
</body>
</html>
