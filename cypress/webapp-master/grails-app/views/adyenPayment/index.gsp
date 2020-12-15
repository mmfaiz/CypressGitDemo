<%@ page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
    <r:require modules="jquery-payment"/>
</head>
<body>
<div id="adyen-dialog" class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <h4 class="modal-title">${texts.header}</h4>
        </div>
        <form id="adyen-encrypted-form" name="adyen-encrypted-form">
            <input type="hidden" id="generationtime" value="${timestamp}" data-encrypted-name="generationtime" />

            <span class="payment-errors"></span>

            <div class="modal-body relative">
                <div class="row">
                    <div class="col-sm-10 col-sm-offset-1">
                        <div class="row">
                            <div class="col-sm-12 bottom-margin20">
                                ${texts.ingress}
                                <br><br>
                                <strong><g:message code="payment.form.help"/></strong>
                            </div>
                        </div>

                        <div class="row">
                            <div class="form-group col-sm-12 has-feedback has-feedback-right">
                                <div class="input-group">
                                    <input type="tel" size="20" autocomplete="off"
                                           data-encrypted-name="number" placeholder="${message(code:"payment.form.cardnumber")}" class="form-control"/>
                                    <span class="input-group-addon">
                                        <ul id="accepted-cards" class="list-inline text-right no-bottom-margin"></ul>
                                    </span>
                                </div>
                            </div>
                            <div class="form-group col-sm-12">
                                <input type="text" size="20" autocomplete="off"
                                       data-encrypted-name="holderName" placeholder="${message(code:"payment.form.cardholder.name")}" class="form-control"/>
                            </div>
                            <div class="form-group col-sm-4">
                                <select data-encrypted-name="expiryMonth" class="form-control">
                                    <option><g:message code="payment.form.cardexpire.month.label"/></option>
                                    <option value="01">01</option>
                                    <option value="02">02</option>
                                    <option value="03">03</option>
                                    <option value="04">04</option>
                                    <option value="05">05</option>
                                    <option value="06">06</option>
                                    <option value="07">07</option>
                                    <option value="08">08</option>
                                    <option value="09">09</option>
                                    <option value="10">10</option>
                                    <option value="11">11</option>
                                    <option value="12">12</option>
                                </select>
                            </div>
                            <div class="form-group col-sm-4">
                                <g:select name="expiryYear" from="${selectableYears}" data-encrypted-name="expiryYear" class="form-control"
                                          noSelection="${['null':message(code: "payment.form.cardexpire.year.label")]}"/>
                            </div>
                            <div class="form-group col-sm-4">
                                <div class="input-group">
                                    <input type="tel" size="4" maxlength="4" autocomplete="off"
                                           data-encrypted-name="cvc" placeholder="${message(code:"payment.form.cvc")}" class="form-control"/>
                                    <span class="input-group-addon">
                                        <a id="card-security" href="#">
                                            <i class="fas fa-question-circle text-info"></i>
                                        </a>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-default">
                    <g:message code="button.cancel.label" default="Avbryt"/>
                </a>
                <input type="submit" class="btn btn-md btn-success" value="${texts.payBtn}" disabled/>
            </div>
        </form>
    </div>
</div>

<r:script>
    $(document).ready(function() {
        // The form element to encrypt
        var $form = document.getElementById('adyen-encrypted-form');

        // Form and encryption options. See adyen.encrypt.simple.html for details
        var options = {
            enableValidations: true
        };
        // Bind encryption to the form
        var cseInstance = adyen.createEncryptedForm( $form, options);

        var $genTime    = $('[data-encrypted-name=generationtime]');
        var $expMonth   = $('[data-encrypted-name=expiryMonth]');
        var $expYear    = $('[data-encrypted-name=expiryYear]');
        var $cardHolder = $('[data-encrypted-name=holderName]');
        var $cardNumber = $('[data-encrypted-name=number]');
        var $cardCVC    = $('[data-encrypted-name=cvc]');

        $("#adyen-encrypted-form").keypress(function(e){
            if ( e.keyCode == 13 ) {
                return false;
            }
        });

        // jquery.payment
        var $cardInput = $cardNumber.payment('formatCardNumber').on('blur', function() {
            var validCards = ${g.forJavaScript(json: acceptedCards)};
            var cardType   = $.payment.cardType($(this).val()) === 'mastercard' ? 'mc' : $.payment.cardType($(this).val());
            var valid = $.payment.validateCardNumber($(this).val()) && _.contains(validCards, cardType);
            hintCardType(cardType);

            if(!valid) {
                $cardNumber.closest('.form-group').addClass('has-error');
            } else {
                $cardNumber.closest('.form-group').removeClass('has-error');
            }
        });
        /*var $expiry    = $('[rel=expiry]').payment('formatCardExpiry').on('blur', function() {
            var result = $.payment.cardExpiryVal($(this).val());

            if (result.month < 10) {
                result.month = "0" + result.month;
            }
            $expMonth.val(result.month);
            $expYear.val(result.year);
        });*/
        var $cvc      = $cardCVC.payment('formatCardCVC').on('blur', function() {
            var valid = $.payment.validateCardCVC($(this).val());

            if(!valid) {
                $cardCVC.closest('.form-group').addClass('has-error');
            } else {
                $cardCVC.closest('.form-group').removeClass('has-error');
            }
        });

        $('#adyen-dialog').on('submit', $form, function(e) {
            // jquery.payment
            var cardValid = $.payment.validateCardNumber($cardInput.val());
            if (!cardValid) {
                $cardNumber.closest('.form-group').addClass('has-error');
                return false;
            }
            var cvcValid = $.payment.validateCardCVC($cardCVC.val());
            if (!cvcValid) {
                $cardCVC.closest('.form-group').addClass('has-error');
                return false;
            }

            loadingSubmit();

            // Manually encrypt for ajax request to be fulfilled
            var cardData = {
                number : $cardInput.val(),
                cvc : $cardCVC.val(),
                holderName : $cardHolder.val(),
                expiryMonth : $expMonth.val(),
                expiryYear : $expYear.val(),
                generationtime : $genTime.val()
            };
            e.preventDefault();
            $.ajax({
                type:'POST',
                data: {
                    'orderId': "${g.forJavaScript(data: orderId)}",
                    'promoCodeId': "${g.forJavaScript(data: promoCodeId)}",
                    'adyen-encrypted-data': cseInstance.encrypt(cardData),
                    'savePaymentInfo': "${g.forJavaScript(data: params.savePaymentInfo)}",
                    'ignoreAssert': "${g.forJavaScript(data: params.ignoreAssert)}",
                    'userAgent': navigator.userAgent,
                    'method': "${g.forJavaScript(data: params.method)}"
                },
                url: '${g.forJavaScript(data: createLink(action: 'pay'))}',
                success:function(html) {
                    $('#userBookingModal').html(html);
                },
                error: function (error) {
                    console.log(error);
                    normalSubmit();
                }
            });

            // Prevent the form from being submitted:
            return false;
        });

        $.ajax({
            url: "${g.forJavaScript(data: createLink(controller: 'adyenPayment', action: 'directory'))}?facilityId=${g.forJavaScript(data: facilityId)}",
            dataType : 'json',
            success: function (data) {
                var acceptedCards = '';
                _.each(data.cards, function(c) {
                    acceptedCards += '<li rel="'+c.brandCode+'"><img src="'+c.logos.tiny+'"/></li>';
                });

                $('#accepted-cards').html(acceptedCards);
            },
            error: function(error) {
                console.log(error);
            }
        });

        var $cvcHelp = $('#card-security').popover({
            container: 'body',
            html: true,
            trigger: 'hover',
            content: '<img src="${resource(file: "mc.gif", dir: "images/card-security")}" />',
            placement: 'left'
        });

        var hintCardType = function(cardType) {
            var $hintItems = $('#accepted-cards');
            $hintItems.find('li').css("opacity", "0.2");
            $hintItems.find('li[rel="'+cardType+'"]').css("opacity", "1");

            $cvcHelp.data('bs.popover').options.content = '<img src="${resource(dir: "images/card-security/")}'+cardType+'.gif" />';;
        };

        var loadingSubmit = function() {
            var $adyenDialog = $('#adyen-dialog');
            $adyenDialog.find('input[type=submit],input[type=text],input[type=tel],select').prop('disabled', 'disabled');
            onLoading($adyenDialog.find('input[type=submit]'));
        };
        var normalSubmit = function() {
            $('#adyen-dialog').find('input[type=submit],input[type=text],input[type=tel],select').removeProp('disabled');
        };
    });
</r:script>
</body>
</html>

