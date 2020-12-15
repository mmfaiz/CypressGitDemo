<%@ page import="com.matchi.payment.PaymentMethod; com.matchi.orders.AdyenOrderPayment; com.matchi.orders.OrderPayment; com.matchi.orders.Order; com.matchi.User; org.joda.time.DateTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title><g:message code="adminOrder.index.title"/></title>
</head>
<style type="text/css">
td.hiddenRow {
    padding: 0 !important;
}

div.order-detail {
    background-color: #ededed;
    padding: 10px 10px 10px 45px;

}
</style>
<body>

<ul class="breadcrumb">
    <li><g:message code="adminOrder.index.order"/> (${totalCount})</li>
</ul>
<div id="container">
    <div class="content">
        <ul id="errors-tab" class="nav nav-tabs">
            <li class="active"><g:link action="index">Orders</g:link></li>
            <li><g:link action="errors">Errors</g:link></li>
            <li><g:link action="notifications">Notifications</g:link></li>
        </ul>

        <g:form method="get" name="filterForm" class="form-inline well" style="padding:12px 10px 4px 10px;">
            <fieldset>
                <div class="control-group">
                    <ul class="inline filter-list">
                        <li>
                            <g:textField tabindex="1" name="q" placeholder="Search" value="${cmd.q}"/>
                        </li>
                        <li>
                            <g:render template="/templates/datePickerFilter"/>
                        </li>
                        <li>
                            <g:select name="article" from="${Order.Article.list()}"
                                      value="${cmd.article }" noSelection="['':'--Select article--']"/>
                        </li>

                        <li>
                            <g:select name="orderStatus" from="${Order.Status.list()}"
                                      value="${cmd.orderStatus}" noSelection="['':'--Order status--']"/>
                        </li>

                        <li>
                            <g:select name="orderPaymentStatus" from="${OrderPayment.Status.list()}"
                                      value="${cmd.orderPaymentStatus}" noSelection="['':'--Payment status--']"/>
                        </li>
                        <li class="form-group">
                            <div class="control-group">
                                <label for="idSearch" class="checkbox"><g:checkBox name="idSearch" value="${cmd.idSearch}" class="form-control" /> <g:message code="admin.order.index.idSearch" /> </label>
                            </div>

                        </li>
                        <li class="form-group">
                            <div class="control-group">
                                <label for="originSearch" class="checkbox"><g:checkBox name="originSearch" value="${cmd.originSearch}" class="form-control" /> <g:message code="admin.order.index.originSearch" /> </label>
                            </div>
                        </li>
                        <li class="form-group">
                            <div class="control-group">
                                <label for="noArticle" class="checkbox"><g:checkBox name="noArticle" value="${cmd.noArticle}" class="form-control" /> <g:message code="admin.order.index.noArticle" /> </label>
                            </div>
                        </li>
                        <li class="form-group">
                            <div class="control-group">
                                <label for="doublePayment" class="checkbox"><g:checkBox name="doublePayment" value="${cmd.doublePayment}" class="form-control" /> <g:message code="admin.order.index.doublePayment" /> </label>
                            </div>
                        <li class="pull-right">
                            <button tabindex="3" id="filterSubmit" class="btn" type="submit" name="submit" value="1">
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
                        <th><g:message code="adminOrder.index.number"/></th>
                        <th><g:message code="facility.label"/></th>
                        <th><g:message code="customer.label"/></th>
                        <th><g:message code="default.status.label"/></th>
                        <th><g:message code="default.created.label"/></th>
                        <th><g:message code="payment.paid.label"/></th>
                        <th><g:message code="default.amount.label"/></th>
                    </tr>
                    </thead>

                    <g:each in="${orders}" var="order">
                        <tr data-toggle="collapse" data-target="#order_detail_${order.id}">
                            <td>${order.id}</td>
                            <td>${order.facility}</td>
                            <td>${order.customer} <small>(${order.origin})</small></td>
                            <td>
                                <span class="badge <g:message code="order.status.badge.${order.status}"/>" title="${order.status}">
                                    ${order.status}
                                </span>
                                <g:if test="${!order.retrieveArticleItem()}">
                                    <i class="fas fa-exclamation-triangle" rel="tooltip" title="No article item"></i>
                                </g:if>
                            </td>
                            <td><g:formatDate date="${order.dateCreated}" format="yyyy-MM-dd HH:mm"/></td>
                            <td>${order.isFinalPaid() ? message(code: 'default.yes.label') : message(code: 'default.no.label')} <g:if test="${!order.refunds.isEmpty()}"><i class="icon-refresh"></i></g:if></td>
                            <td><g:formatMoney value="${order.total()}" facility="${order.facility}" />
                            (<g:formatMoney value="${order.vat()}" facility="${order.facility}" />)</td>
                        </tr>
                        <tr>
                            <td colspan="8" class="hiddenRow">
                                <div class="collapse" id="order_detail_${order.id}">
                                    <div class="order-detail">
                                        <div class="row-fluid">
                                            <div class="span12">
                                                <b><g:message code="adminOrder.index.order"/> ${order.id} - ${order.article}</b>
                                                <div class="row-fluid">
                                                    <div class="span5">${order.description}</div>
                                                    <div class="span7">
                                                        <g:if test="${order.payments.isEmpty()}">
                                                            <i><g:message code="adminOrder.index.noPayments"/></i>
                                                        </g:if>
                                                        <g:else>
                                                            <b><g:message code="payment.label.plural"/></b><br>
                                                            <table class="table table-condensed" style="background-color: inherit">


                                                            <g:each in="${order.getPayments()}" var="payment">
                                                                <tr>
                                                                    <td><g:formatDate date="${payment.dateCreated}" format="yyyy-MM-dd HH:mm:ss"/> </td>
                                                                    <td>
                                                                        <i class="fa fa-${payment.type.equals("Coupon")?"ticket":"credit-card"}"></i>
                                                                        ${payment.method ?: payment.type}
                                                                    </td>
                                                                    <td>
                                                                        <span class="badge <g:message code="payment.status.badge.${payment.status}"/>" title="${payment.status}">
                                                                            ${payment.status}
                                                                        </span>

                                                                        <g:if test="${payment.instanceOf(AdyenOrderPayment)}">
                                                                            <g:link controller="adminOrder" action="creditAdyenOrder" id="${payment.id}" params="${params}" title="${message(code: 'adminOrder.index.credit')}" onclick="return confirm('${ order.isStillRefundable() ? message(code: 'default.confirm') : message(code: 'adminOrder.index.creditOrderWarning')}')">
                                                                                <span><i class="icon-remove"></i></span>
                                                                            </g:link>
                                                                        </g:if>
                                                                    </td>
                                                                    <td><g:formatNumber number="${payment.amount}" maxFractionDigits="2" minFractionDigits="2"></g:formatNumber>
                                                                    </td>
                                                                    <td><b><g:formatNumber number="${payment.total()}" maxFractionDigits="2" minFractionDigits="2"></g:formatNumber>
                                                                    </b></td>
                                                                </tr>

                                                                <g:if test="${payment.status.equals(OrderPayment.Status.FAILED)}">
                                                                    </tr>
                                                                    <td colspan="5">
                                                                        <blockquote>
                                                                            <small>${payment.errorMessage} ${payment.lastUpdated}</small>
                                                                        </blockquote>
                                                                    </td>
                                                                    </tr>
                                                                </g:if>
                                                                <g:if test="${payment?.credited == 0 && order.refunds.isEmpty()}">
                                                                    <i><g:message code="adminOrder.index.notRefundable"/></i>
                                                                </g:if>

                                                            </g:each>
                                                            </table>
                                                        </g:else>

                                                        <g:if test="${order.refunds.isEmpty()}">
                                                            <!-- <i><g:message code="adminOrder.index.noPayments"/></i> -->
                                                        </g:if>
                                                        <g:else>
                                                            <b><g:message code="adminOrder.index.repayments"/></b><br>
                                                            <table class="table table-condensed" style="background-color: inherit">


                                                                <g:each in="${order.getRefunds()}" var="refund">
                                                                    <tr>
                                                                        <td><g:formatDate date="${refund.dateCreated}" format="yyyy-MM-dd HH:mm:ss"/> </td>
                                                                        <td><small>${refund.note}</small></td>
                                                                        <td>
                                                                            <g:if test="${refund.issuer}">
                                                                                <g:link controller="adminUser" action="edit" id="${refund?.issuer?.id}" title="${refund.issuer}">
                                                                                    <i class="icon-user"></i>
                                                                                </g:link>
                                                                            </g:if>
                                                                        </td>
                                                                        <td>-<g:formatMoney value="${refund.amount}" facility="${order.facility}" /></td>
                                                                    </tr>


                                                                </g:each>
                                                            </table>
                                                        </g:else>


                                                    </div>
                                                    <g:if test="${order.hasPayments() && !order.isPaidByCoupon() && !order.article.equals(Order.Article.PAYMENT_UPDATE)}">
                                                        <a href="${createLink(controller: 'userProfile', action: 'printReceipt', params: [id: order.id])}" target="_blank" class="btn btn-primary"><i class="fas fa-print"></i> <g:message code="adminOrder.index.printReceipt" /></a>
                                                    </g:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </g:each>
                </table>
                <div class="row">
                    <div class="span12">
                        <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                                    maxsteps="0" max="${cmd.max}" action="index" total="${totalCount}"
                                                    params="[q: cmd.q, start: cmd.start, end: cmd.end, article: cmd.article,
                                                             orderStatus: cmd.orderStatus, orderPaymentStatus: cmd.orderPaymentStatus, originSearch:cmd.originSearch, doublePayment:cmd.doublePayment, noArticle:cmd.noArticle]"/>
                    </div>
                </div>
                <div class="row">
                    <div class="span12 center-text">
                        <span class="">
                            ${totalCount}<g:message code="unit.st"/>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="modal" class="modal hide fade" style="display:none;width:600px" data-dynamic="true">

    <div class="modal-header">
        <h3><g:message code="default.loader.label"/></h3>
    </div>
    <div id="modal-body" class="modal-body" style="height: 338px;width:600px">
        <div id="paymentFrameLoading" style="position: absolute;top: 100px; left: 220px; font-size: 30px; color: #cccccc"><br>
            <br><br>
            <br><g:message code="default.loader.label"/></div>
    </div>

    <div class="modal-footer">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label"/></a>
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