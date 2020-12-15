<%@ page import="com.matchi.FacilityProperty; com.matchi.payment.PaymentMethod; com.matchi.FacilityProperty.FacilityPropertyKey" %>
<div class="row" id="paymentMethodContainer">
    <div class="col-sm-4 col-xs-4">
        <p class="right-margin10 top-margin10"><g:message code="payment.confirm.method"/>:</p>
    </div>

    <div class="col-sm-8 col-xs-8 payment-method-list">

        <g:each in="${methods}" var="paymentMethod">
            <div class="form-group payment-method">
                <div class="radio radio-success">
                    <g:radio id="${paymentMethod}" name="method"
                             value="${paymentMethod}"/>
                    <label for="${paymentMethod}">
                        <g:message code="payment.method.${paymentMethod}"/>
                        <g:if test="${message(code: "payment.method.${paymentMethod}.extraInfo", default: null)}">
                            <span><g:message code="payment.method.${paymentMethod}.extraInfo"/></span>
                        </g:if>
                        <g:if test="${message(code: "payment.method.${paymentMethod}.hint", default: null)}">
                            &nbsp;<g:inputHelp title="${message(code: "payment.method.${paymentMethod}.hint")}"/>
                        </g:if>
                    </label>
                </div>
            </div>
        </g:each>

        <div id="local-payment-methods" class="form-group" style="display: none;">
            <i class="fas fa-spinner fa-spin"></i> <g:message code="payment.loading.paymentMethods"/>
        </div>
    </div>

    <!-- Template to be used above on reload, by jQuery -->
    <div id="hidden-method-template" class="form-group" style="display: none;">
        <div class="radio radio-success">
            <g:radio id="template" name="method" value="template" checked=""/>
            <label for="template">
                <i></i>
            </label>
        </div>
    </div>
</div>

<g:each in="${offers}" var="offer">
    <div class="row additional-payment-info offer-selection" id="${offer.containerId}" rel="${offer.className}">
        <div class="col-sm-4 col-xs-4">
            <p class="right-margin10"><g:message code="payment.confirm.chooseCoupon"/>:</p>
        </div>

        <div class="col-sm-8 col-xs-8">
            <div class="form-group">
                <select id="${offer.selectId}" name="customerCouponId" class="form-control" data-style="form-control"
                        disabled="disabled">
                    <g:render template="/templates/payments/offersOptions"
                              model="[customerOfferGroups: offer.list, units: message(code: 'payment.confirm.message20')]"/>
                </select>
            </div>
        </div>
    </div>
</g:each>

<div class="row additional-payment-info" id="savePaymentInformationContainer"
     rel="${com.matchi.payment.PaymentMethod.CREDIT_CARD}" style="display: none;">
    <div class="col-sm-4 col-xs-4">
        <p class="right-margin10"><g:message code="payment.confirm.saveDetails"/>:</p>
    </div>

    <div class="col-sm-8 col-xs-8">
        <div class="form-group">
            <div class="checkbox checkbox-success">
                <g:checkBox name="savePaymentInformation" checked="true"/>
                <label for="savePaymentInformation">
                    <span id="reccuringPaymentsInfo"><g:message
                            code="payment.confirm.savePaymentInformation.label"/></span>
                </label>
            </div>
        </div>
    </div>
</div>

<g:if test="${promoCodeAvailable}">
    <div class="promo-input">
        <hr/>

        <div class="row promo-input">
            <div class="toggle-collapse-chevron col-sm-12 col-xs-12">
                <a data-toggle="collapse" href="#promo-code" aria-expanded="true" aria-controls="promo-code"
                   class="right-margin10">
                    <i class="fa"></i> <g:message code="promocode.button.add"/>
                </a>
            </div>

            <div class="promo-code collapse" id="promo-code">
                <div class="col-sm-4 col-xs-4">
                    <p class="right-margin10"><g:message code="payment.method.PROMOCODE"/>:</p>
                </div>

                <div class="col-sm-6 col-xs-5" id="promo-code-input">
                    <g:hiddenField id="promoCodeId" name="promoCodeId"/>
                    <g:textField id="promoCode" name="promoCode" class="form-control"/>
                </div>

                <div class="col-sm-2 col-xs-2">
                    <g:submitToRemote name="applyPromoCode"
                                      url="[controller: 'bookingPayment', action: 'applyPromoCode']"
                                      class="btn btn-success" value="${message(code: "button.apply_.label")}"
                                      onSuccess="usePromoCode(data)"
                                      onFailure="handlePromoCodeError(XMLHttpRequest, textStatus, errorThrown)"
                                      id="applyPromoCode"/>
                </div>
            </div>
        </div>
        <hr/>
    </div>
</g:if>

<g:if test="${extraFieldsTemplate}">
    <g:render template="/templates/payments/fields/${extraFieldsTemplate}"/>
</g:if>

<g:if test="${agreements}">
    <g:each in="${agreements}" var="agreement">
        <g:render template="/templates/payments/agreements/${agreement}"/>
    </g:each>
</g:if>

<div class="row">
    <div class="col-xs-1">
        <h3 class="h6 weight400">
            <span class="fa-stack text-info">
                <i class="fas fa-circle fa-stack-2x"></i>
                <i class="fas fa-info fa-stack-1x fa-inverse"></i>
            </span>
        </h3>
    </div>

    <div class="col-xs-11">
        <div class="vertical-padding15">
            <p class="no-bottom-margin"><g:message code="templates.payments.paymentMethod.message5"
                                                   args="[createLink(controller: 'home', action: 'useragreement', fragment: 'Betalning')]"/></p>
        </div>
    </div>
</div>

<script type="text/javascript">
  $(document).ready(function () {
    <g:if test="${facility}">
    var $localMethods = $('#local-payment-methods');
    <g:if test="${localPaymentMethods}">
    $localMethods.show();
    </g:if>

    $.ajax({
      url: "${g.forJavaScript(data: createLink(controller: 'adyenPayment', action: 'directory'))}?facilityId=${g.forJavaScript(data: facility.id)}",
      dataType: 'json',
      success: function (data) {
        var html = '';

        _.each(data.methods, function (m) {
          html += '<div class="radio radio-success">';
          html += '<input id="' + m.brandCode + '" type="radio" name="method" value="' + m.brandCode.toUpperCase() + '"/>';
          //html += '<label for="'+m.brandCode+'"><img src="'+m.logos.small+'" /> '+m.i18code+'</label>';
          html += '<label for="' + m.brandCode + '">' + m.i18code + '</label>';
          html += "</div>";
          if (m.issuers && m.issuers.length > 0) {
            var options = '';

            options += '<div class="row additional-payment-info" id="paymentInformationGiftCard" rel="' + m.brandCode + '">';
            options += '<div class="col-sm-4 col-xs-4">';
            options += '<p class="right-margin10">${message(code: "payment.confirm.chooseIssuer")}:</p></div>';
            options += '<div class="col-sm-8 col-xs-8"><div class="form-group">';


            options += '<select id="select-' + m.brandCode + '" name="issuerId" class="form-control" data-style="form-control" disabled="disabled">';
            _.each(m.issuers, function (issuer) {
              if (issuer.state == 'online') {
                options += '<option value="' + issuer.issuerId + '">' + issuer.name + '</option>';
              }
            });

            options += '</select>';
            options += '</div></div></div>';

            <g:if test="${methods.contains(PaymentMethod.CREDIT_CARD)}">
            $('div.additional-payment-info[rel=${g.forJavaScript(data: PaymentMethod.CREDIT_CARD)}]').after(options);
            </g:if>
            <g:else>
            $('div.additional-payment-info[rel=${g.forJavaScript(data: PaymentMethod.CREDIT_CARD_RECUR)}]').after(options);
            </g:else>
          }
        });

        $localMethods.html(html);

        $('input:radio[name=method]').change(function () {
          var selected = layoutPaymentMethods(data);
          togglePricesByMethod(selected, data);
        });

        layoutPaymentMethods();
      },
      error: function (error) {
        console.log(error);
      }
    });
    </g:if>
  });

  function layoutPaymentMethods() {
    var selected = $('input:radio[name=method]:checked').val();

    if (!selected) {
      free = $('input#${g.forJavaScript(data: PaymentMethod.FREE)}');
      coupon = $('input#${g.forJavaScript(data: PaymentMethod.COUPON)}');
      giftCard = $('input#${g.forJavaScript(data: PaymentMethod.GIFT_CARD)}');
      creditCardRecur = $('input#${g.forJavaScript(data: PaymentMethod.CREDIT_CARD_RECUR)}');
      creditCard = $('input#${g.forJavaScript(data: PaymentMethod.CREDIT_CARD)}');

      if (free.length > 0) {
        free.attr("checked", "checked");
      } else if (coupon.length > 0) {
        coupon.attr("checked", "checked");
      } else if (giftCard.length > 0) {
        giftCard.attr("checked", "checked");
      } else if (creditCardRecur.length > 0) {
        creditCardRecur.attr("checked", "checked");
      } else {
        creditCard.attr("checked", "checked");
      }
      selected = $('input:radio[name=method]:checked').val();
    }
    if ($('input#${g.forJavaScript(data: PaymentMethod.COUPON)}').is(':checked') || $('input#${g.forJavaScript(data: PaymentMethod.GIFT_CARD)}').is(':checked')) {
      $('#btnSubmit').val('${message(code: "button.book.label", default: "Boka")}');
    } else {
      if ($('input#${g.forJavaScript(data: PaymentMethod.CREDIT_CARD_RECUR)}').is(':checked')) {
        $('#btnSubmit').val('${message(code: "button.finish.purchase.label", default: "Boka")}');
      } else {
        $('#btnSubmit').val('${message(code: "button.next.label", default: "Nästa")}');
      }
    }

    $('div.additional-payment-info[rel!=' + selected + ']').hide();
    $('div.additional-payment-info[rel!=' + selected + ']').find('select').attr('disabled', 'disabled');
    $('div.additional-payment-info[rel=' + selected + ']').fadeIn();
    $('div.additional-payment-info[rel=' + selected + ']').find('select').removeAttr('disabled');


    if (selected == '${g.forJavaScript(data: PaymentMethod.COUPON)}' || selected == '${g.forJavaScript(data: PaymentMethod.GIFT_CARD)}') {
      $('.promo-input').hide();
    } else {
      $('.promo-input').show();
    }

    return selected;
  };

  function usePromoCode(data) {
    $('#promoCodeStatus').remove();
    if (data.status == 200) {
      $('#promoCodeId').val(data.id).trigger("change");
      $('#promoCode').attr('disabled', 'disabled');
      $('#applyPromoCode').attr('disabled', 'disabled');
      $('#promo-code-input').append($('<div id ="promoCodeStatus" class="text-success text-sm">')
        .text(data.message));
    } else {
      $('#promo-code-input').append($('<div id ="promoCodeStatus" class="text-danger text-sm">')
        .text(data.message));
    }
  }

  function handlePromoCodeError(XMLHttpRequest, textStatus, errorThrown) {
    console.log("ERROR" + textStatus)
  }


  var onLoading = function () {
    $("#btnSubmit").attr("disabled", "disabled");

    if ($("#${g.forJavaScript(data: PaymentMethod.CREDIT_CARD_RECUR)}").is(':checked')) {
      $("#processingPaymentLoading").show()
    }
  };
</script>
