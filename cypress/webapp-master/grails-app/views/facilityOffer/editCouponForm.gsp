<%@ page import="org.joda.time.DateTime; com.matchi.membership.Membership; com.matchi.coupon.GiftCard" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="default.edit.label" args="[customerCoupon.coupon.name]"/></h3>
    <div class="clearfix"></div>
</div>
<g:form name="couponForm" class="no-margin"
        controller="facilityOffer" action="updateCustomerCoupon"
        params="[ returnUrl: g.createLink(absolute: true, controller: 'facilityCustomer', action: 'show', id: customerId)]">
    <g:hiddenField name="id" value="${customerCoupon.id}"/>
    <div class="modal-body">
        <div class="alert alert-error" style="display: none;">
            <a class="close" data-dismiss="alert" href="#">×</a>
        </div>
        <fieldset>
            <div class="control-group">
                <ul class="inline">
                    <g:if test="${customerCoupon.coupon.instanceOf(GiftCard) && !customerCoupon.coupon.unlimited}">
                        <li>
                            <label class="control-label" for="nrOfTickets">
                                <g:message code="facilityCoupon.addCouponForm.message6"/>
                            </label>
                            <g:textField name="nrOfTickets" class="span1 required"
                                    value="${customerCoupon.nrOfTickets}"/>
                        </li>
                    </g:if>
                    <li>
                        <label for="expireDate">
                            <strong><g:message code="facilityCoupon.addCouponForm.message5"/></strong>
                            (<g:message code="date.inclusive.label"/>)
                        </label>
                        <g:textField name="showExpireDate" class="span1" readonly="true"
                                     value="${formatDate(date: customerCoupon.expireDate?.toDate(), formatName: 'date.format.dateOnly')}"/>
                        <g:hiddenField name="expireDate" value="${formatDate(date: customerCoupon.expireDate?.toDate(), format: 'yyyy-MM-dd')}"/>
                    </li>
                </ul>
            </div>
            <div class="control-group">
                <label for="note"><g:message code="facilityCoupon.editCouponForm.message1"/></label>
                <g:textArea rows="2" cols="50" name="note" class="span6" value="${customerCoupon.note}"/>
            </div>
        </fieldset>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" class="btn btn-md btn-success" value="${message(code: 'button.save.label')}"/>


            <g:if test="${!customerCoupon.dateLocked}">
                <g:actionSubmit controller="facilityOffer" action="lockCustomerCoupon" id="${customerCoupon.id}"
                                returnUrl="${g.createLink(absolute: true, controller: 'facilityCustomer', action: 'show', id: customerId)}"
                                onclick="return confirm('${message(code: 'facilityCoupon.editCouponForm.message5')}');"
                                class="btn btn-md btn-inverse" value="${message(code: 'facilityCoupon.editCouponForm.message6')}"/>
            </g:if>
            <g:else>
                <g:actionSubmit controller="facilityOffer" action="unlockCustomerCoupon" id="${customerCoupon.id}"
                                returnUrl="${g.createLink(absolute: true, controller: 'facilityCustomer', action: 'show', id: customerId)}"
                                onclick="return confirm('${message(code: 'facilityCoupon.editCouponForm.message7')}');"
                                class="btn btn-md btn-inverse" value="${message(code: 'facilityCoupon.editCouponForm.message8')}"/>
            </g:else>

            <g:if test="${customerCoupon.couponTickets.size() == customerCoupon.nrOfTickets}">
                <g:link action="removeFromCustomer" title="${message(code: 'button.delete.label')}"
                        params="[id: customerCoupon.id,
                                 returnUrl: g.createLink(absolute: true, controller: 'facilityCustomer', action: 'show', id: customerId)]"
                        onclick="return confirm('${message(code: 'facilityCoupon.editCouponForm.message9')}')" class="btn btn-md btn-danger">
                    <g:message code="button.delete.label"/>
                </g:link>
            </g:if>
            <g:if test="${!customerCoupon.dateLocked && !customerCoupon.coupon.instanceOf(GiftCard)}">
                <g:actionSubmit controller="facilityOffer" action="removeCouponTicket" id="${customerCoupon.id}"
                                returnUrl="${g.createLink(absolute: true, controller: 'facilityCustomer', action: 'show', id: customerId)}"
                                onclick="return confirm('${message(code: 'facilityCoupon.editCouponForm.message10')}');"
                                class="btn btn-md btn-info" value="-1"/>
                <g:actionSubmit controller="facilityOffer" action="addCouponTicket" id="${customerCoupon.id}"
                                returnUrl="${g.createLink(absolute: true, controller: 'facilityCustomer', action: 'show', id: customerId)}"
                                onclick="return confirm('${message(code: 'facilityCoupon.editCouponForm.message11')}');"
                                class="btn btn-md btn-info" value="+1"/>
            </g:if>
        </div>
        <div class="pull-right">
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></a>
        </div>
    </div>
</g:form>

<script type="text/javascript">
    $(function() {
        $("#showExpireDate").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#expireDate',
            altFormat: 'yy-mm-dd'
        });
    });
</script>