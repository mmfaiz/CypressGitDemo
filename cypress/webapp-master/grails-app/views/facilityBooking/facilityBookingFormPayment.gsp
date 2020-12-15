<div id="booking-user" class="control-group">
    <g:hiddenField name="customerId" id="customerId" value="${customer.id}"/>
    <g:hiddenField name="email" id="email" value="${customer.email}"/>
    <g:hiddenField name="bookingWarning" value="${bookingWarning?.encodeAsHTML()}"/>

    <div class="controls well" style="padding: 15px">
        <g:bookingFormCustomer customer="${customer}"/>
        <div class="clearfix"></div>

        <hr/>
        <div class="row">
            <span class="span3 left">
                <h6><g:message code="payment.label"/></h6>
                <ul class="inline">
                <g:if test="${facility?.hasApplicationInvoice()}">
                    <li>
                        <a class="btn transparent-60" onclick="javascript:void(0)"><g:message code="facilityBooking.facilityBookingFormPayment.message3"/></a>
                    </li>
                </g:if>
                </ul>
                <div class="span3" style="margin-bottom: 0;${facility?.hasApplicationCashRegister()?"display:none;":""}">
                    <label class="checkbox span3 no-margin" for="paid">
                        <g:checkBox id="paid" name="paid" value="${booking?.paid}" checked="${booking?.paid}" disabled="${customerCoupon != null}" tabindex="4"/><g:message code="facilityBooking.facilityBookingFormPayment.message4"/>
                    </label>
                </div>
                <g:if test="${coupons.size() > 0}">
                    <g:if test="${facility?.hasApplicationCashRegister()}">
                        <div class="space10"></div>
                    </g:if>
                    <div class="span3" id="show-usecoupon-control" style="margin-bottom: 0; ${facility?.hasApplicationCashRegister()?"margin-left: 0;":""}display: inline">
                        <label class="checkbox span3 no-margin" for="useCoupon">
                            <g:checkBox id="useCoupon" name="useCoupon" value="${customerCoupon != null}" checked="${customerCoupon != null}" disabled="${customerCoupon != null}" tabindex="7" /><g:message code="facilityBooking.facilityBookingFormPayment.useCoupon" args="[coupons.size()]"/>
                        </label>
                        <select id="customerCouponId" name="customerCouponId" style="display: none; width: 170px" tabindex="8" class="no-margin" disabled="disabled">
                            <g:render template="/templates/payments/offersOptions" model="[customerOfferGroups: coupons]"/>
                        </select>
                    </div>
                </g:if>
                <g:if test="${giftCards?.size() > 0}">
                    <g:if test="${facility?.hasApplicationCashRegister()}">
                        <div class="space10"></div>
                    </g:if>
                    <div class="span3" id="show-useGiftCard22-control" style="margin-bottom: 0; ${facility?.hasApplicationCashRegister()?"margin-left: 0;":""}display: inline">
                        <label class="checkbox span3 no-margin" for="useGiftCard">
                            <g:checkBox id="useGiftCard" name="useGiftCard" value="${customerCoupon != null}" checked="${customerCoupon != null}" disabled="${customerCoupon != null}" tabindex="7" /><g:message code="facilityBooking.facilityBookingFormPayment.useGiftCard" args="[giftCards.size()]"/>
                        </label>
                        <select id="customerGiftCardId" name="customerCouponId" style="display: none; width: 170px" tabindex="8" class="no-margin" disabled="disabled">
                            <g:render template="/templates/payments/offersOptions" model="[customerOfferGroups: giftCards, units: message(code: 'unit.credits')]"/>
                        </select>
                    </div>
                </g:if>
            </span>
            <g:bookingFormTotalPrice bookingPrices="${bookingPrices}"/>
        </div>
</div>

<script type="text/javascript">

  var $couponSelected = $("#useCoupon");
  var $giftCardSelected = $("#useGiftCard");
  var $paid = $("#paid");

  $('#paymentHint').tooltip('show');

  $couponSelected.on('click', function () {
    if ($giftCardSelected.is(":checked")) {
      $('#customerGiftCardId').toggle();
      $giftCardSelected.prop('checked', false);
    }

    $('#customerGiftCardId').prop('disabled', true);
    $('#customerCouponId').prop('disabled', false).toggle();

    if (!$paid.is(":checked") && $(this).is(":checked")) {
      $paid.prop('checked', true);
    } else if ($paid.is(":checked") && !$(this).is(":checked")) {
      $paid.prop('checked', false);
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
      $paid.prop('checked', true);
    } else if ($paid.is(":checked") && !$(this).is(":checked")) {
      $paid.prop('checked', false);
    }

  });

  $paid.on('click', function () {
    if (!$(this).is(":checked") && ($couponSelected.is(":checked") || $giftCardSelected.is(":checked"))) {
      if ($giftCardSelected.is(":checked")) {
        $('#customerGiftCardId').toggle().attr('disabled', 'disabled');;
        $giftCardSelected.prop('checked', false);
      } else if ($couponSelected.is(":checked")) {
        $('#customerCouponId').toggle().attr('disabled', 'disabled');;
        $couponSelected.prop('checked', false);
      }
    }
  });
</script>
