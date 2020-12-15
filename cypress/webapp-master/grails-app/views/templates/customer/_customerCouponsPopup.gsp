<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="templates.customer.customerCouponsPopup.message8" args="[customer.fullName()]"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped table-fixed">
        <thead>
        <tr>
            <th width="130" class="ellipsis" style="text-align: left;"><g:message code="coupon.name.label"/></th>
            <th class="center-text" width="100"><g:message code="coupon.nrOfTickets.label2"/></th>
            <th class="center-text" width="100"><g:message code="templates.customer.customerCouponsPopup.message3"/></th>
            <th class="center-text" width="120"><g:message code="default.status.label"/></th>
            <th class="center-text" width="80"><g:message code="coupon.availableOnline.label2"/></th>
            <th style="width: 14px"></th>
            <th style="width: 14px"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${coupons}" var="coupon">
            <tr>
                <td class="ellipsis">${coupon.coupon.name}</td>
                <td class="center-text">
                    ${coupon.coupon.unlimited ? "-" : coupon.nrOfTickets+" " + message(code: 'unit.st')}
                </td>
                <td class="center-text" width="100">${coupon.expireDate ? coupon.expireDate.toString("${message(code: 'date.format.dateOnly')}"):""}</td>
                <td class="center-text">
                    <g:if test="${!coupon.dateLocked}">
                        <span class="label label-success"><g:message code="templates.customer.customerCoupon.active"/></span>
                    </g:if>
                    <g:else>
                        <span class="label label-important"><g:message code="templates.customer.customerCouponsPopup.message7"/></span>
                    </g:else>
                </td>
                <td class="center-text">${coupon.payment? message(code: 'default.yes.label') : message(code: 'default.no.label')}</td>
                <td class="center-text">
                    <g:remoteLink class="pull-right" controller="facilityOffer" action="editCouponForm" update="customerModal"
                                  onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                                  params="[ 'id': coupon.id, customerId: customer.id ]"><i class="icon-edit"></i></g:remoteLink>
                </td>
                <td>
                    <g:if test="${!coupon.coupon.unlimited}">
                        <g:remoteLink controller="facilityOffer" action="showUsageHistory" id="${coupon.id}"
                                update="customerModal" onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                                title="${message(code: 'customerCoupon.usageHistory.tooltip')}" rel="tooltip"
                                ><i class="icon-search"></i></g:remoteLink>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
<div class="modal-footer">
    <div class="pull-left">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.close.label" default="Stäng"/></a>
    </div>
</div>