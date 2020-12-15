<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <g:if test="${userProfile}">
        <h4 class="modal-title"><g:message code="customerCoupon.usageHistory"/></h4>
    </g:if>
    <g:else>
        <h3><g:message code="customerCoupon.usageHistory"/></h3>
        <div class="clearfix"></div>
    </g:else>
</div>

<div class="modal-body">
    <g:if test="${tickets}">
        <table style="width: 100%" class="${userProfile ? 'table' : ''} table-striped">
            <thead>
                <tr>
                    <th class="text-left"><g:message code="coupon.nrOfTickets.label2"/>/<g:message code="coupon.nrOfTickets.label3"/></th>
                    <th class="text-left"><g:message code="default.date.label"/></th>
                    <th class="text-left"><g:message code="default.description.label"/></th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${tickets}" var="ticket">
                    <tr>
                        <td>
                            <g:formatNumber number="${ticket.nrOfTickets}" format="+#,##0;-#"/>
                        </td>
                        <td>
                            <g:formatDate date="${ticket.dateCreated}" formatName="date.format.daterangepicker.short"/>
                        </td>
                        <td>
                            <g:if test="${userProfile && ticket.type == com.matchi.coupon.CustomerCouponTicket.Type.CUSTOMER_PURCHASE}">
                                <g:message code="customerCouponTicket.type.CUSTOMER_PURCHASE.user"/>
                            </g:if>
                            <g:else>
                                <g:message code="customerCouponTicket.type.${ticket.type.name()}"
                                        /><g:if test="${ticket.description}">: ${ticket.description}</g:if>
                            </g:else>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <p><g:message code="customerCoupon.usageHistory.empty"/></p>
    </g:else>
</div>

<div class="modal-footer">
    <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md ${userProfile ? 'btn-default' : 'btn-danger'}">
        <g:message code="button.close.label"/>
    </a>
</div>