<%@ page import="java.sql.Timestamp; org.joda.time.Interval; org.joda.time.Period; org.joda.time.DateTime; com.matchi.Facility" %>
<g:set var="returnUrl" value="${createLink(action: actionName, absolute: true, params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="priceList.label.plural"/></title>
    <r:require modules="jquery-multiselect-widget, matchi-selectall"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="facilityPriceList.index.message17" args="[pricelists.size()]"/></li>
</ul>
<g:if test="${!facility?.isMasterFacility()}">
    <g:errorMessage bean="${ priceInstance }" />

    <g:render template="/templates/messages/webflowMessage"/>

    <ul class="nav nav-tabs">
        <li class="${actionName == 'index' ? 'active' : ''}"><g:link action="index"><g:message code="facilityPriceList.index.pricelist.active"/></g:link></li>
        <li class="${actionName == 'upcoming' ? 'active' : ''}"><g:link action="upcoming"><g:message code="facilityPriceList.index.pricelist.upcoming"/></g:link></li>
        <li class="${actionName == 'inactive' ? 'active' : ''}"><g:link action="inactive"><g:message code="facilityPriceList.index.pricelist.inactive"/></g:link></li>
    </ul>

    <div class="action-bar">
        <div class="btn-toolbar-left">
            <div class="btn-group">
                <button class="btn btn-inverse dropdown-toggle bulk-action" data-toggle="dropdown">
                    <g:message code="button.actions.label"/>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#priceLists', '<g:createLink controller="facilityPriceListFlow" action="copy"
                            params="['returnUrl': returnUrl]"/>');"><g:message code="facilityPriceList.index.copy"/></a></li>
                </ul>
            </div>
        </div>
        <div class="btn-toolbar-right">
            <g:link action="index" controller="facilityCustomerCategory" class="btn btn-inverse">
                <span><g:message code="priceListCustomerCategory.label.plural"/></span>
            </g:link>
            <g:link action="create" class="btn btn-inverse">
                <span><g:message code="facilityPriceList.index.message4"/></span>
            </g:link>
        </div>
    </div>

    <g:form name="priceLists" controller="facilityPriceListFlow" action="copy" class="no-margin">
    <table id="priceLists-table" class="table table-striped table-bordered" data-provides="rowlink">
        <thead>
        <tr height="34">
            <th class="center-text" width="20">
                <g:checkBox class="checkbox-selector" name="" value="" checked="false"/>
            </th>
            <td width="140"><g:message code="priceList.name.label"/></td>
            <td><g:message code="facilityPriceList.index.message6"/></td>
            <td><g:message code="facilityPriceList.index.message8"/></td>
            <td width="140" class="center-text"><g:message code="default.status.label"/></td>
            <td class="center-text"><g:message code="facilityPriceList.index.message11"/></td>
        </tr>
        </thead>
        <tbody>
        <g:if test="${pricelists.size() == 0}">
            <tr>
                <td colspan="4"><i><g:message code="facilityPriceList.index.message12"/></i></td>
            </tr>
        </g:if>
        <g:each in="${pricelists}" var="pricelist">

            <tr>
                <td class="center-text nolink">
                    <g:checkBox name="priceListId" value="${pricelist.id}" checked="false" class="selector"/>
                </td>
                <td>
                    <g:link class="edit rowLink" controller="facilityPriceListCondition" action="index" params="[id: pricelist.id]">
                        ${pricelist.name}
                    </g:link>
                </td>
                <td>
                    <g:formatDate date="${pricelist.startDate}" format="${g.message(code:"date.format.dateOnly")}"/>
                </td>
                <td><g:message code="sport.name.${pricelist.sport.id}"/></td>
                <td class="center-text">
                    <g:if test="${pricelist.numMissingPrices() > 0}">
                        <span class="label label-important"><g:message code="facilityPriceList.index.message18" args="[pricelist.numMissingPrices()]"/></span>
                    </g:if>
                    <g:else>
                        <span class="label label-success"><g:message code="default.status.ok"/></span>
                    </g:else>
                </td>
                <td class="center-text">
                    <g:if test="${pricelist.subscriptions}">
                        <span class="label label-warning"><g:message code="subscription.label"/></span>
                    </g:if>
                    <g:else>
                        <span class="label label-info"><g:message code="facilityPriceList.index.message16"/></span>
                    </g:else>
                </td>

            </tr>

        </g:each>

        </tbody>
    </table>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>

<r:script>
    $(document).ready(function() {
        $(".search").focus();

        $("#priceLists-table").selectAll({ max: "${g.forJavaScript(data: pricelists.size())}", count: "${g.forJavaScript(data: pricelists.size())}", name: "pricelist" });
    });
    function showForm() {
        $('tbody form').parent().show();
    }
</r:script>
</body>
</html>
