<%@ page import="com.matchi.payment.PaymentStatus" %>
<g:set var="occasion" value="${occasions[booking.slot]}"/>
<div id="booking-customer" class="control-group">
    <g:hiddenField name="customerId" value="${customer.id}"/>
    <g:hiddenField name="email" value="${customer.email}"/>
    <div class="controls well" style="padding: 15px">
        <g:bookingFormCustomer customer="${customer}"/>
        <div class="clearfix"></div>

        <hr/>
        <div class="row">
            <div class="${occasion?"span5":"span3"} pull-left">
                <g:if test="${coupon}">
                    <h6><g:message code="payment.label"/></h6>
                    <g:hiddenField id="paid" name="paid" value="${booking?.isFinalPaid()}"/>
                    <g:if test="${promoCode}">
                        <div class="span6">
                            <g:message code="templates.booking.facilityBookingInfo.promoCodeUsed"/>: ${promoCode}
                        </div>
                    </g:if>
                    <div class="span6">
                        <g:message code="templates.booking.facilityBookingInfo.message11"/>:<br/><em>${coupon.coupon.name}</em>
                    </div>
                </g:if>
                <g:elseif test="${booking.order?.isFinalPaid()}">
                    <h6><g:message code="payment.label"/></h6>
                    <g:hiddenField id="paid" name="paid" value="${booking?.isFinalPaid()}"/>

                    <g:if test="${promoCode}">
                        <div class="span6">
                            <g:message code="templates.booking.facilityBookingInfo.promoCodeUsed"/>: ${promoCode}
                        </div>
                    </g:if>
                    <g:each in="${booking.order.payments}">
                    <div class="span6">
                        <g:if test="${it.type=="Cash"}">
                            <g:message code="templates.booking.facilityBookingInfo.markPaid"/> <br/>${it.issuer.fullName()} <br/>${it.lastUpdated}
                        </g:if>
                        <g:else>
                            <g:message code="templates.booking.facilityBookingInfo.message12"/> <g:message code="payment.type.${it.type}"/>
                        </g:else>
                        </div>
                    </g:each>
                </g:elseif>
                <g:elseif test="${occasion}">
                    <div class="span2 left">
                        <h6>${occasion.activity.name}</h6>
                        <g:link controller="facilityActivityOccasion" action="edit" id="${occasion.id}">
                            ${occasion.date} ${occasion.startTime.toString("HH:mm")}
                        </g:link>
                    </div>
                    <div class="span2 left" style="margin-bottom: 0; display: inline">
                        <h6><g:message code="templates.booking.facilityBookingInfo.message3"/></h6>
                        <g:if test="${occasion.isFull()}">
                            <span class="label label-important"><g:message code="templates.booking.facilityBookingInfo.message4"/></span>
                        </g:if>
                        <g:else>
                            <span class="label label-success"><g:message code="templates.booking.facilityBookingInfo.message5"/></span>
                            <g:link controller="facilityActivityOccasion" action="edit" id="${occasion.id}">${occasion.numParticipations()} / ${occasion.maxNumParticipants}</g:link>
                        </g:else>
                    </div>
                </g:elseif>
                <g:elseif test="${booking.payment?.isCreditCard()}">
                    <g:hiddenField id="paid" name="paid" value="${booking?.isFinalPaid()}"/>
                    <h6><g:message code="payment.label"/></h6>
                    <div class="span3" style="margin-bottom: 0;">
                        <g:message code="templates.booking.facilityBookingInfo.message7"/>
                    </div>
                </g:elseif>
                <g:elseif test="${booking.payment?.isInvoice()}">
                    <g:hiddenField id="paid" name="paid" value="${booking?.isFinalPaid()}"/>
                    <h6><g:message code="payment.label"/></h6>
                    <div class="span3" style="margin-bottom: 0;">
                        <g:message code="templates.booking.facilityBookingInfo.message9"/>
                    </div>
                </g:elseif>
                <g:else>
                    <h6><g:message code="payment.label"/></h6>
                    <g:if test="${facility?.hasApplicationCashRegister()}">
                        <div class="space10"></div>
                        <g:if test="${facility?.hasApplicationInvoice() && !booking?.payment}">
                            <g:hiddenField id="useInvoice" name="useInvoice" value=""/>
                            <a id="invoicePaymentBtn" onclick="invoicePayment()" class="btn"><g:message code="templates.booking.facilityBookingInfo.message15"/></a>
                        </g:if>
                        <g:hiddenField id="paid" name="paid" value="${booking?.isFinalPaid()}" disabled="${coupon != null}"/>
                        <div class="space10"></div>
                    </g:if>
                    <g:elseif test="${!facility.hasApplicationCashRegister() && facility?.hasApplicationInvoice() && !booking?.payment}">
                        <g:hiddenField id="useInvoice" name="useInvoice" value=""/>
                        <a id="invoicePaymentBtn" onclick="invoicePayment()" class="btn"><g:message code="templates.booking.facilityBookingInfo.message15"/></a>
                        <div class="space5"></div>
                        <label class="checkbox span3 no-margin" for="paid">
                            <g:checkBox id="paid" name="paid" value="${booking?.isFinalPaid()}" checked="${booking?.isFinalPaid()}" disabled="${coupon != null}"/><g:message code="templates.booking.facilityBookingInfo.message16"/>
                        </label>
                    </g:elseif>
                    <g:else>
                        <div class="span3" style="margin-bottom: 0;margin-left: 0;">
                            <label class="checkbox span3 no-margin" for="paid">
                                <g:checkBox id="paid" name="paid" value="${booking?.isFinalPaid()}" checked="${booking?.isFinalPaid()}" disabled="${coupon != null}"/><g:message code="templates.booking.facilityBookingInfo.message16"/>
                            </label>
                        </div>
                    </g:else>

                    <g:if test="${coupons.size() > 0}">
                        <div class="span3" id="show-usecoupon-control" style="margin-bottom: 0;margin-left: 0; display: inline">
                            <label class="checkbox span3 no-margin" for="useCoupon">
                                <g:checkBox id="useCoupon" name="useCoupon" value="${coupon != null}" checked="${coupon != null}" disabled="${coupon != null}"/><g:message code="facilityBooking.facilityBookingFormPayment.useCoupon" args="[coupons.size()]"/>
                            </label>
                            <select id="customerCouponId" name="customerCouponId" style="display: none; width: 170px" tabindex="8" class="no-margin">
                                <g:render template="/templates/payments/offersOptions" model="[customerOfferGroups: coupons]"/>
                            </select>
                        </div>
                    </g:if>
                    <g:if test="${giftCards.size() > 0}">
                        <div class="span3" id="show-useGiftCard-control" style="margin-bottom: 0;margin-left: 0; display: inline">
                            <label class="checkbox span3 no-margin" for="useGiftCard">
                                <g:checkBox id="useGiftCard" name="useGiftCard" value="${coupon != null}" checked="${coupon != null}" disabled="${coupon != null}"/><g:message code="facilityBooking.facilityBookingFormPayment.useGiftCard" args="[giftCards.size()]"/>
                            </label>
                            <select id="customerGiftCardId" name="customerCouponId" style="display: none; width: 170px" tabindex="8" class="no-margin">
                                <g:render template="/templates/payments/offersOptions" model="[customerOfferGroups: giftCards, units: message(code: 'unit.credits')]"/>
                            </select>
                        </div>
                    </g:if>
                </g:else>
                <g:if test="${booking?.trainers?.size()}">
                    <span><g:message code="templates.booking.facilityBookingInfo.trainer" />: ${booking?.trainers?.first().fullName()}</span>
                </g:if>
            </div>
            <g:if test="${!occasion}">
                <g:bookingFormTotalPrice bookingPrices="${bookingPrices}"/>
                <g:if test="${booking}">
                    <g:bookingFormPaidPrice bookingPayments="${payments}"/>
                </g:if>
            </g:if>
        </div>
        <g:if test="${warnAboutCodeRequest}">
            <span><g:message code="templates.booking.facilityBookingInfo.code_request_warning" /></span>
        </g:if>
        <div class="row"></div>
    </div>
</div>

<script type="text/javascript">

    var $couponSelected = $("#useCoupon");
    var $giftCardSelected = $("#useGiftCard");
    var $paid = $("#paid");

    $(document).ready(function() {
        <g:if test="${facility?.hasApplicationInvoice() && !booking?.payment}">
            // Keyboard shortcuts for invoicepayment
            Mousetrap.bindGlobal('f', function(e) {
                invoicePayment();
            });
        </g:if>
    });

    $couponSelected.on('click', function() {
      if ($giftCardSelected.is(":checked")) {
        $('#customerGiftCardId').toggle();
        $giftCardSelected.prop('checked', false);
      }

        $('#customerGiftCardId').prop('disabled', true);
        $('#customerCouponId').prop('disabled', false).toggle();

        if((!$paid.is(":checked") || !$paid.val()) && $couponSelected.is(":checked")) {
            $paid.attr('checked', true);
            $paid.val(true);
        } else if (($paid.is(":checked") || $paid.val()) && !$couponSelected.is(":checked")) {
            $paid.attr('checked', false);
            $paid.val(false);
        }

    });

    $giftCardSelected.on('click', function () {
      if ($couponSelected.is(":checked")) {
        $('#customerCouponId').toggle();
        $couponSelected.prop('checked', false);
      }

      $('#customerCouponId').prop('disabled', true);
      $('#customerGiftCardId').prop('disabled', false).toggle();

      if (!$paid.is(":checked") && $(this).is(":checked")) {
        $paid.attr('checked', true);
        $paid.val(true);
      } else if ($paid.is(":checked") && !$(this).is(":checked")) {
        $paid.attr('checked', false);
        $paid.val(false);
      }

    });

    $paid.on('click', function() {
        if(!$(this).is(":checked") && ($couponSelected.is(":checked") || $giftCardSelected.is(":checked"))) {
          if ($giftCardSelected.is(":checked")) {
            $('#customerGiftCardId').toggle();
            $giftCardSelected.prop('checked', false);
          } else if ($couponSelected.is(":checked")) {
            $('#customerCouponId').toggle();
            $couponSelected.prop('checked', false);
          }
        }
    });

    function invoicePayment() {
        $paid.attr('checked', true);
        $paid.val(true);
        $("#useInvoice").val(true);
        $("#bookingForm").submit();
    }
</script>