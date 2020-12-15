<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title>Adyen payment errors</title>
</head>
<body>
<ul class="breadcrumb">
    <li>Adyen payment errors (${payments?.getTotalCount()})</li>
</ul>

<div id="container">
    <div class="content">
        <ul id="errors-tab" class="nav nav-tabs">
            <li><g:link action="index">Orders</g:link></li>
            <li class="active"><g:link action="errors">Errors</g:link></li>
            <li><g:link action="notifications">Notifications</g:link></li>
        </ul>

        <g:form action="errors" name="filterForm" class="form-inline well" style="padding:12px 10px 4px 10px;">
            <fieldset>
                <div class="control-group">
                    <ul class="inline filter-list">
                        <li>
                            <g:textField tabindex="1" name="q" placeholder="Search" value="${params.q}"/>
                        </li>
                        <li class="pull-right">
                            <button tabindex="3" id="filterSubmit" class="btn" type="submit">
                                <g:message code="button.filter.label"/>
                            </button>
                        </li>
                    </ul>
                </div>
            </fieldset>
        </g:form>

        <div class="row">
            <div class="span12">
                <table class="table" style="border-collapse:collapse;">
                    <thead>
                    <tr height="34">
                        <th>Order id / Payment id</th>
                        <th>Order status / Payment status</th>
                        <th>Error created</th>
                        <th>Error action</th>
                        <th>Error reason</th>
                    </tr>
                    </thead>
                    <tbody data-provides="rowlink">
                    <g:each in="${payments}" var="payment">
                        <g:set var="order" value="${payment.orders.first()}"/>
                        <g:set var="error" value="${payment.error}"/>
                        <tr>
                            <td>
                                <g:link action="index" params="[q: order.id]">${order.id} / ${payment.id}</g:link>
                            </td>
                            <td>
                                <span class="badge <g:message code="order.status.badge.${order.status}"/>" title="<g:message code="order.status.${order.status}"/>">
                                    <g:message code="order.status.${order.status}"/>
                                </span>
                                <span class="badge <g:message code="payment.status.badge.${payment.status}"/>" title="<g:message code="payment.status.${payment.status}"/>">
                                    <g:message code="payment.status.${payment.status}"/>
                                </span>
                            </td>
                            <td><g:formatDate date="${error.dateCreated}" format="yyyy-MM-dd HH:mm"/></td>
                            <td>${error.action}</td>
                            <td>${error.reason}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>

            </div>
        </div>
        <div class="row">
            <div class="span12">
                <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                            maxsteps="0" max="${cmd.max}" action="index" total="${payments?.getTotalCount()}"
                                            params="[q: params.q]"/>
            </div>
        </div>
        <div class="row">
            <div class="span12 center-text">
                <span class="">
                    ${payments.getTotalCount()}<g:message code="unit.st"/>
                </span>
            </div>
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function() {
        $("#q").focus();

        $("[rel=tooltip]").tooltip();
    })
</r:script>
</body>
</html>
