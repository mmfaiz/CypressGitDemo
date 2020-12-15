<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerCategory.confirmDelete.message1"/></title>
</head>
<body>
<div class="hero-unit">
    <h2><g:message code="facilityCustomerCategory.confirmDelete.message6" args="[category.name]"/></h2>
    <div class="ingress">
        <ul>
            <li><g:message code="facilityCustomerCategory.confirmDelete.message2"/></li>
            <g:each in="${pricelists}" var="pricelist">
                <li style="font-size: 14px"> - <g:link controller="facilityPriceListCondition" action="index" id="${pricelist.id}"> ${pricelist.name}</g:link> <span class="help-inline">(<g:message code="facilityCustomerCategory.confirmDelete.message7"/> <g:formatDate date="${pricelist.startDate}" format="${g.message(code:"date.format.dateOnly")}"/>)</span></li>
            </g:each>
        </ul>
    </div>
    <span class="help-inline">
        <g:message code="facilityCustomerCategory.confirmDelete.message3"/>
    </span><br><br>
    <g:link controller="facilityCustomerCategory" action="edit" id="${category.id}" class="btn btn-danger btn-large"><g:message code="facilityCustomerCategory.confirmDelete.message4"/></g:link>&nbsp;&nbsp;<g:link controller="facilityCustomerCategory" action="delete" id="${category.id}" params="[confirmed:true]" class="btn btn-success btn-large"><g:message code="facilityCustomerCategory.confirmDelete.message5"/></g:link>
</div>
</body>
</html>
