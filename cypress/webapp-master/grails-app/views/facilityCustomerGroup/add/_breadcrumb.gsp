<%@ page import="org.joda.time.LocalTime" %>
<ul class="breadcrumb">
    <li>
        <g:link controller="facilityCustomer" action="index">
            <g:message code="customer.label.plural"/>
        </g:link>
        <span class="divider">/</span>
    </li>
    <li class="active"><g:message code="facility.customer.addToGroup.title"/></li>
</ul>

<g:if test="${error}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#">×</a>
        <h4 class="alert-heading">
            ${new LocalTime().toString("HH:mm:ss")}: <g:message code="default.error.heading"/>
        </h4>
        ${error}
    </div>
</g:if>

<g:render template="/templates/wizard"
        model="[steps: [message(code: 'group.multiselect.noneSelectedText'),
                message(code: 'facility.customer.addToGroup.confirm')], current: wizardStep]"/>