<%@ page import="com.matchi.coupon.GiftCard; com.matchi.coupon.Coupon; org.joda.time.DateTime; com.matchi.membership.Membership" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityCoupon.add.heading"/></h3>
    <div class="clearfix"></div>
</div>
<g:form name="couponForm" class="no-margin" controller="facilityOffer" action="addToCustomer">
    <g:hiddenField name="customerId" value="${customer.id}"/>
    <div class="modal-body">
        <div class="alert alert-error" style="display: none;">
            <a class="close" data-dismiss="alert" href="#">×</a>
        </div>

        <p><g:message code="facilityCoupon.addCouponForm.message2"/></p><br>
        <fieldset>
            <div class="control-group">
                <div class="controls">
                    <select id="couponId" name="couponId" class="span3 required" tabindex="0">
                        <g:if test="${coupons[Coupon.class]}">
                            <option value=""> ---- <g:message code="payment.confirm.chooseCoupon"/> ---- </option>
                            <g:each in="${coupons[Coupon.class]}" var="coupon">
                                <option value="${coupon.id}">
                                    ${coupon.name}
                                </option>
                            </g:each>
                        </g:if>
                        <g:if test="${coupons[GiftCard.class]}">
                            <option value=""> ---- <g:message code="payment.confirm.chooseGiftCard"/> ---- </option>
                            <g:each in="${coupons[GiftCard.class]}" var="coupon">
                                <option value="${coupon.id}">
                                    ${coupon.name}
                                </option>
                            </g:each>
                        </g:if>
                    </select>
                </div>
            </div>
            <div id="coupon-info" style="display: none;">
                <div class="control-group">
                    <ul class="inline">
                        <li>
                            <label class="control-label" for="nrOfTickets">
                                <g:if test="${coupons[Coupon.class]}">
                                    <g:message code="facilityCoupon.addCouponForm.message4"/>
                                </g:if>
                                <g:if test="${coupons[GiftCard.class]}">
                                    <g:message code="facilityCoupon.addCouponForm.message6"/>
                                </g:if>
                            </label>

                            <g:textField name="nrOfTickets" class="span1 required"/>
                        </li>
                        <li>
                            <label for="expireDate">
                                <strong><g:message code="facilityCoupon.addCouponForm.message5"/></strong>
                                (<g:message code="date.inclusive.label"/>)
                            </label>
                            <g:textField name="showExpireDate" class="span1" readonly="true"/>
                            <g:hiddenField name="expireDate" value="" />
                        </li>
                    </ul>
                </div>
                <div class="control-group">
                    <label class="control-label" for="note"><g:message code="customerCoupon.note.label"/></label>
                    <g:textArea cols="50" rows="2" name="note" class="span6" placeholder="${message(code: 'facilityCoupon.addCouponForm.message7')}"/>
                </div>
            </div>
        </fieldset>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'button.add.label')}" class="btn btn-md btn-success"/>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></a>
        </div>
    </div>
</g:form>
<script type="text/javascript">
    var coupons = [];

    $(document).ready(function() {
        <g:each in="${coupons.values()?.flatten()}">
        var coupon = { id: "${g.forJavaScript(data: it.id)}", nrOfTickets: "${g.forJavaScript(data: it.nrOfTickets)}", expireDate: "${g.forJavaScript(data: it.getExpireDate() ? it.getExpireDate().toString("${message(code: 'date.format.dateOnly')}"):"")}", unlimited: "${g.forJavaScript(data: it.unlimited)}"};
        coupons.push(coupon);
        </g:each>

        $("#couponId").on("change", function() {
            var id = $(this).val();

            if(id && coupons.length > 0) {
                var coupon = $.grep(coupons, function(e){ return e.id == id; })[0];
                var $nrOfTickets = $("#nrOfTickets");

                console.log(coupon);

                if(coupon.unlimited.toLowerCase() == "true") {
                    $nrOfTickets.attr("disabled", "disabled");
                    $nrOfTickets.attr("title", "${message(code: 'facilityCoupon.addCouponForm.message9')}");
                }

                $nrOfTickets.val(coupon.nrOfTickets);
                $("#coupon-info").slideDown();

                var $showExpireDate = $("#showExpireDate");
                var $expireDate = $("#expireDate");
                $showExpireDate.val(coupon.expireDate);
                $expireDate.val(coupon.expireDate);

                $showExpireDate.datepicker({
                    autoSize: true,
                    dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                    altField: '#expireDate',
                    altFormat: 'yy-mm-dd'
                });
            } else {
                $("#coupon-info").slideUp();
            }
        });

        $("#couponForm").validate({
            errorLabelContainer: ".alert-error",
            errorPlacement: function(error, element) {},
            highlight: function (element, errorClass) {
                $(element).addClass("invalid-input");
            },
            unhighlight: function (element, errorClass) {
                $(element).removeClass("invalid-input");
            },
            messages: {
                couponId: "${message(code: 'facilityCoupon.addCouponForm.message10')}",
                nrOfTickets: "${message(code: 'facilityCoupon.addCouponForm.message11')}"
            }
        });

        $('#customerModal').on('shown', function () {
            $('#couponId').focus();
        });
    });
</script>