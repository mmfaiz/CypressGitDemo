<%@ page import="grails.converters.JSON; com.matchi.coupon.CustomerCoupon; com.matchi.payment.PaymentMethod; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
    <r:require modules="select2"/>
</head>
<body>
<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: facility]" />

<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" action="${formAction}" class="no-margin">
            <g:each in="${hiddenFields}" var="field">
                <g:hiddenField name="${field.name}" value="${field.value}" />
            </g:each>

            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="payment.confirm.message4"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>
            <div class="modal-body">

                <g:b3StaticFlashError/>

                <div class="row">
                    <div class="col-sm-6 col-xs-6">
                        <h2 class="h6">
                            <span class="fa-stack fa-lg">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fas fa-map-marker fa-stack-1x fa-inverse"></i>
                            </span>
                            ${facility.name}
                        </h2>
                    </div>
                    <div class="col-sm-6 col-xs-6">
                        <h2 class="h6">
                            <span class="fa-stack fa-lg">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                            </span>
                            <g:each in="${dates}" var="date">
                                <g:humanDateFormat date="${new org.joda.time.DateTime(date)}"/>
                            </g:each>
                        </h2>
                    </div>

                    <g:each in="${slots*.court.unique()}" var="court">
                        <g:if test="${court.description && court.showDescriptionOnline}">
                            <div class="col-sm-12">
                                <strong>${court.name}:</strong> ${court.description.encodeAsHTML()}
                            </div>
                        </g:if>
                    </g:each>
                </div>

                <hr>

                <g:each in="${adjacentSlotGroups}" var="slotGroup">
                    <g:set var="firstSlot" value="${slotGroup.selectedSlots.first()}" />
                    <g:set var="lastSlot" value="${slotGroup.selectedSlots.last()}" />
                    <g:set var="court" value="${firstSlot.court}"/>
                    <g:set var="totalSlotPrice" value="${pricePerSlotRow[firstSlot.id]}" />
                    <div class="row">
                        <div class="col-sm-4 col-xs-12">
                            <p class="flex-center">
                                <span class="fa-stack text-success no-shrink">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fas fa-flag fa-stack-1x fa-inverse"></i>
                                </span>

                                <span class="break-word">
                                    <span>${court.name}</span>
                                    <g:each in="${court.courtTypeAttributes}" var="courtTypeAttribute">
                                        <br/><small><g:message code="court.type.${courtTypeAttribute.courtTypeEnum.name()}" />: <g:message code="court.type.${courtTypeAttribute.courtTypeEnum.name()}.${courtTypeAttribute.value}" /></small>
                                    </g:each>
                                </span>
                            </p>
                        </div>
                        <div class="col-sm-4 col-xs-12">
                            <p>
                                <span class="fa-stack">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fa fa-clock-o fa-stack-1x fa-inverse"></i>
                                </span> <g:formatDate format="HH:mm" date="${firstSlot.startTime}" /> -
                                <g:if test="${slotGroup.selectedSlots.size() > 1 || slotGroup.subsequentSlots.size() > slotGroup.selectedSlots.size()}">
                                    <select name="trailingSlots_${firstSlot.id}" class="trailingSlotSelector" rel="${firstSlot.id}" id="trailingSlots_${firstSlot.id}" data-selectedids="${g.expectJsonInTag(json: slotGroup.selectedSlots*.id)}" data-slotids="${g.expectJsonInTag(json: slotGroup.subsequentSlots*.id)}">
                                        <g:each in="${slotGroup.subsequentSlots}">
                                            <option value="${it.id}" ${it.id == lastSlot.id ? 'selected' : ''}><g:formatDate format="HH:mm" date="${it.endTime}" /> </option>
                                        </g:each>
                                    </select>
                                </g:if>
                                <g:else>
                                    <g:formatDate format="HH:mm" date="${lastSlot.endTime}" />
                                </g:else>

                            </p>
                        </div>

                        <div class="col-sm-4 col-xs-12">
                            <p>
                                <span class="fa-stack">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                                </span>
                                <span id="bookingPrice_${firstSlot.id}"><g:formatMoney value="${totalSlotPrice}" facility="${facility}" forceZero="true"/></span>
                            </p>
                        </div>
                    </div>
                </g:each>

                <div id="total-price" class="row" style="display: ${nPrices > 1 ? 'block' : 'none'};">
                    <div class="col-xs-12">
                        <p>
                            <g:message code="payment.confirm.multiple.withdrawals1" /> <strong class="total-price-span"><g:formatMoney value="${totalPrice}" facility="${facility}"/></strong>. <g:message code="payment.confirm.multiple.withdrawals2" /> <strong class="n-withdrawals">${nPrices}</strong> <g:message code="payment.confirm.multiple.withdrawals3" />.
                        </p>
                    </div>

                </div>

                <hr>
                <!-- Changing referer header will most likely not happen with the modal up. If no referer header, the redirect payment methods will not appear. -->
                <g:if test="${!hasRefererHeader}">
                    <div>
                        <i class="text-muted"><g:message code="payment.confirm.refererWarning"/></i>
                        <div class="space-10"/>
                    </div>
                </g:if>
                <g:else>
                    <div id="book-many-warning" style="display: ${slots.size() > 1 ? 'block' : 'none'};">
                        <i class="text-muted"><g:message code="payment.confirm.multiple.bookings.note" args="[createLink(controller: 'userProfile', action: 'account')]"/></i>
                        <div class="space-10"/>
                    </div>
                </g:else>
                <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}"/>
                <hr>

                <g:if test="${selectableCustomerCategories}">
                    <div class="row">
                        <div class="col-sm-4 col-xs-4">
                            <p class="right-margin10">
                                <g:message code="payment.confirm.customerCategorySelect"/>
                                <g:inputHelp title="${message(code: 'payment.confirm.customerCategorySelect.help')}"/>
                            </p>
                        </div>
                        <div class="col-sm-8 col-xs-8">
                            <div class="form-group">
                                <g:select name="selectedCustomerCategory" from="${selectableCustomerCategories}"
                                          optionKey="id" optionValue="name" class="select-picker form-control" noSelection="['': message(code: 'payment.confirm.customerCategorySelect.noneSelected')]"/>
                            </div>
                        </div>
                    </div>
                    <hr>
                </g:if>

                <g:if test="${facility.showBookingHolder}">
                    <div class="row">
                        <div class="col-sm-4 col-xs-4"></div>
                        <div class="col-sm-8 col-xs-8">
                            <div class="form-group">
                                <div class="checkbox checkbox-success">
                                    <input type="checkbox" id="hideBookingHolder" name="hideBookingHolder"
                                           value="true" ${user?.anonymouseBooking ? 'checked' : ''}/>
                                    <label for="hideBookingHolder">
                                        <g:message code="payment.confirm.hideBookingHolder.label"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                </g:if>

                <g:if test="${facility.hasBookATrainer()}">
                    <div class="row" id="book-trainer-row">
                        <div class="col-sm-1 col-xs-2 hidden-xs">
                            <p>
                                <span class="fa-stack text-success">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fas fa-question fa-stack-1x fa-inverse"></i>
                                </span>
                            </p>
                        </div>
                        <div class="col-sm-3 col-xs-12">
                            <p>
                                <g:message code="default.request.trainer.label"/>
                                <g:inputHelp title="${message(code: 'default.request.trainer.help')}"/>
                            </p>
                        </div>
                        <div class="col-sm-8 col-xs-12">

                            <g:if test="${bookTrainerModel.slotsAreSameCourt}">
                                <g:set var="hasTrainers" value="${bookTrainerModel.availableTrainers?.size() > 0}" />
                                <div class="book-trainer-dropdown" style="${hasTrainers ? 'display: block;' : 'display: none;'}">
                                    <g:select name="trainer" from="${bookTrainerModel.availableTrainers ?: []}" class="select-picker"
                                              optionKey="id" optionValue="name" noSelection="${['' : message(code: 'request.trainer.noSelection')]}" />
                                </div>
                                <div class="book-trainer-error" style="${hasTrainers ? 'display: none;' : 'display: block;'}">
                                    <p class="no-trainer-available"  style="${bookTrainerModel.slotsAreConsecutive ? 'display: block;' : 'display: none;' }"><g:message code="request.trainer.noneAvailable" /></p>
                                    <p class="consecutive-slots-required" style="${bookTrainerModel.slotsAreConsecutive ? 'display: none;' : 'display: block;' }"><g:message code="request.trainer.consecutiveSlotsRequired" /></p>
                                </div>

                            </g:if>
                            <g:else>
                                <div id="bookTrainerError">
                                    <p><g:message code="request.trainer.consecutiveSlotsRequired" /></p>
                                </div>
                            </g:else>
                        </div>
                    </div>
                    <hr>
                </g:if>

                <div class="row">
                    <div class="col-sm-4 col-xs-4 toggle-collapse-chevron">
                        <a data-toggle="collapse" href="#players-list" aria-expanded="true" aria-controls="players-list" class="right-margin10">
                            <i class="fas fa-users"></i> <g:message code="player.button.add"/>
                        </a>
                    </div>
                    <div id="players-list" class="col-sm-8 col-xs-8 players-list collapse">
                        <g:if test="${flash.playerEmails}">
                            <g:each in="${flash.playerEmails}" var="email">
                                <g:render template="/templates/booking/playerInput" model="[emailValue: email]"/>
                            </g:each>
                        </g:if>
                        <g:elseif test="${customer}">
                            <g:render template="/templates/booking/playerInput"
                                    model="[customerName: customer.fullName(), emailValue: customer.email]"/>
                        </g:elseif>
                        <g:render template="/templates/booking/playerInput"/>
                    </div>
                </div>

                <hr>

                <div class="row">
                    <div class="col-xs-1">
                        <p>
                            <span class="fa-stack text-info">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fas fa-info fa-stack-1x fa-inverse"></i>
                            </span>
                        </p>
                    </div>
                    <div class="col-xs-11">
                        <div class="vertical-padding5">
                            <p><g:cancellationTerms slot="${slots.sort().first()}"/></p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-default">
                    <g:message code="button.cancel.label" default="Avbryt"/>
                </a>
                <g:render template="/templates/payments/submitBtnAndHandler"
                        model="[targetController: 'bookingPayment', targetAction: formAction]"/>
            </div>
        </g:form>
    </div>
</div>
<r:script>
    /**
    * General error handling for JavaScript errors.
    * Alerts user that something has gone wrong and reloads the page.
    * Reason is that if the JavaScript breaks in any way, we cannot guarantee the state of the modal.
    * The last thing we want is players claiming the price presented and the price they paid differ.
    */
    window.onerror = function () {
        alert('${message(code: "payment.confirm.javascriptError")}');
        location.reload(true);
    };

    function togglePricesByMethod(method, data) {
        $('.price-container .gift-card-price').hide();
        $('.price-container .coupon-price').hide();
        $('.price-container .free-price').hide();
        $('.price-container .regular-price').hide();

        if (method === '${g.forJavaScript(data: PaymentMethod.GIFT_CARD)}') {
            $('.price-container .gift-card-price').show();
        }
        else if (method === '${g.forJavaScript(data: PaymentMethod.COUPON)}') {
            $('.price-container .coupon-price').show();
        }
        else if (data.isFree === true) {
            $('.price-container .free-price').show();
        }
        else {
            $('.price-container .regular-price').show();
        }
    }

    $(document).ready(function() {
        var $confirmForm  = $("#confirmForm");
        // Disable enter on form (since we submit remote)
        $confirmForm.bind("keypress", function(e) {
            if (e.keyCode == 13) return false;
        });

        // Lookup table for which slot a trailing slot belongs to
        var firstSlotPerSlot = ${g.forJavaScript(json: firstSlotPerSlot)};
        var firstSlots = ${g.forJavaScript(json: firstSlotIds)};
        var currency = "${facility.currency}";

        $('[rel="tooltip"]').tooltip();

        $('.select-picker').selectpicker();
        $("#slotSelector").on("change", function(e) {
             jQuery.ajax({
                type:'POST',
                data:jQuery(this).parents('form:first').serialize(),
                url:'/payment/confirm',
                success: function(data) {
                    jQuery('#userBookingModal').find('.modal-dialog').replaceWith(data);
                },
                error: function(XMLHttpRequest,textStatus,errorThrown){
                    console.log(errorThrown);
                }});
        });

        $(".modal-dialog").on("input", "input[name=playerEmailLabel]", function() {
            var email = $(this).val();
            var row = $(this).closest(".player-row");
            var playerList = $(this).closest(".players-list");
            var playerEmailEl = $(this).parent().find("input[name=playerEmail]");

            if (playerList.find('.player-row:last').is(row)) {
                var newRow = row.clone();
                newRow.find("input").val("");
                playerList.find('.player-row:last').after(newRow);
                addTypeahead(newRow.find("input"));
            }
            if (removeDuplicateEmail(playerEmailEl, email)) {
                row.remove();
            }

            if (playerEmailEl.val() && !(email && /.+@.+\..{2,}/.test(email))) {
                playerEmailEl.val("");
            } else if (email && /.+@.+\..{2,}/.test(email)) {
                playerEmailEl.val(email);
            }
            safeUpdateModalOnMultiplePrices();
        }).on("click", ".delete-player-row", function() {
            var row = $(this).closest(".player-row");
            var list = $(this).closest(".players-list");
            if (row.find("input[name=playerEmailLabel]").val() == "") {
                return;
            }

            if (list.find(".player-row").length > 1) {
                row.remove();
            } else {
                row.find("input").val("");
            }
            safeUpdateModalOnMultiplePrices();
        }).on('change', "input[name=playerEmailLabel]", function() {
            var playerEmailEl = $(this).parent().find("input[name=playerEmail]");
            var email = playerEmailEl.val();
            if (!email || /.+@.+\..{2,}/.test(email)) {
                safeUpdateModalOnMultiplePrices();
            }
        });

        /**
         * Method to update the prices when changing such circumstances
         * @param data
         */
        var updatePrices = function(data) {
            var pricePerSlot = data.prices;
            var totalPrice = data.totalPrice;
            var punchCardPricePerSlotRow = data.punchCardPricePerSlotRow;
            var pricePerSlotRow = data.pricePerSlotRow;
            var pricePerPlayer = data.pricePerPlayer;

            for(var key in pricePerSlotRow) {
                $("#bookingPrice_" + key).text(pricePerSlotRow[key] + " " + currency);
                $("#bookingGiftCardPrice_" + key).text(pricePerSlotRow[key]);
            }

            for(var key in punchCardPricePerSlotRow) {
                $("#bookingNPrice_" + key).text(punchCardPricePerSlotRow[key]);
            }

            $(".players-list").find(".player-price").remove();
            if (pricePerPlayer) {
                for (var courtName in pricePerPlayer) {
                    var i = 0;
                    for (var playerEmail in pricePerPlayer[courtName].players) {
                        var playerInput = $(".player-row").find("input[name=playerEmail]").filter(function() {
                            return $(this).val() && $.trim($(this).val()) == playerEmail
                        });
                        if (playerInput.length) {
                            playerInput.parent().append($('<div class="text-success text-sm player-price">')
                                    .text(courtName + ' ${message(code: "payment.confirm.playerPrice")}: ')
                                    .append(getAditionalPlayerPrices(pricePerPlayer[courtName].players[playerEmail], false, i > 0 ? 0 : pricePerPlayer[courtName].slotsCount))
                                    .prepend('<i class="fas fa-info-circle"></i> '));
                        }
                        i++;
                    }



                    if (pricePerPlayer[courtName].remainingPlayers) {
                        $(".players-list").append($('<div class="text-success text-sm player-price">')
                                .text('${message(code: "payment.confirm.remainingPlayersPrice.msg1")} ' + courtName
                                        + ' ${message(code: "payment.confirm.remainingPlayersPrice.msg2")} '
                                        + pricePerPlayer[courtName].remainingPlayers
                                        + ' ${message(code: "payment.confirm.remainingPlayersPrice.msg3")} ')
                                .append(getAditionalPlayerPrices(pricePerPlayer[courtName].remainingPlayerPrice, true, pricePerPlayer[courtName].slotsCount))
                        );
                    }
                }
            }
        };

        var getAditionalPlayerPrices = function(price, forAdditionalPlayer, slotsCount) {
          return '<span class="price-container">' +
'<span class="regular-price">' + price + ' ${currentFacilityCurrency(facility: facility)}</span>' +
(forAdditionalPlayer || slotsCount == 0 ?
'<span class="coupon-price">${message(code: "facilityBooking.list.payment.method.FREE")}</span>' :
'<span class="coupon-price">' + slotsCount + ' ${message(code: "unit.coupon")}</span>'
) +
'<span class="gift-card-price">' + price + ' ${message(code: "unit.credits")}</span>' +
'<span class="free-price">${message(code: "facilityBooking.list.payment.method.FREE")}</span>' +
'</span>'
        }

        /**
         * Returns a string of concatenated player emails
         */
        var getAddedPlayerEmailsForParameters = function () {
            var emails = [];

            $("input[name=playerEmail]").each(function() {
                emails.push($(this).val());
            });

            return emails.join(',');
        };

        /**
        * Get full info parameters for url including slots, customer category and added player emails (if applicable)
        */
        var getFullParams = function (slotIds) {
            var selectedCustomerCategory = $('#selectedCustomerCategory').val();
            var base = "?slotIds=" + (slotIds ? slotIds.join(',') : getSelectedSlotIds().join(',')) + "&firstSlotIds=" + firstSlots.join(',');

            if(selectedCustomerCategory && !isNaN(parseInt(selectedCustomerCategory))) {
                base += "&selectedCustomerCategory=" + $('#selectedCustomerCategory').val();
            }
            var promoCodeId =  $('#promoCodeId').val();
            if (promoCodeId) {
                base += "&promoCodeId=" + promoCodeId;
            }

            <g:ifFacilityPropertyEnabled facility="${facility}"
                                 name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name()}">
            return base + "&playerEmails=" + getAddedPlayerEmailsForParameters();
            </g:ifFacilityPropertyEnabled>

            return base;
        };

        addTypeahead($("input[name=playerEmailLabel]"));

        /**
         * Returns currently selected slots from all the trailing selectors and the non-trailable (no select) as well
         */
        var getSelectedSlotIds = function(exclude) {
            var allTrailingSlotSelectors = $('.trailingSlotSelector' + (exclude ? ('[rel!=' + exclude + ']') : ''));

            var selectedSlotIds = [];

            allTrailingSlotSelectors.each(function (index) {
                var $thisRow = $(this);

                var toSlotId = $thisRow.val();
                var allTrailingIds = $thisRow.data('slotids');
                var selectedSlotIdsThisRow = allTrailingIds.slice(0, allTrailingIds.indexOf(toSlotId) + 1);

                for(var i = 0; i < selectedSlotIdsThisRow.length; i++) {
                    selectedSlotIds.push(selectedSlotIdsThisRow[i]);
                }
            });

            // Make sure all first slots are included
            for(var i = 0; i < firstSlots.length; i++) {
                if(selectedSlotIds.indexOf(firstSlots[i]) === -1) {
                    selectedSlotIds.push(firstSlots[i]);
                }
            }

            return selectedSlotIds;
        };

        function getSelectedPaymentMethod() {
            var $paymentMethodContainer = $('#paymentMethodContainer');
            var $methodList =  $paymentMethodContainer.find('.payment-method-list');

            var currentSelection = null;

            // Currently selected payment method
            var $methodRadioButtons = $methodList.find('input[type=radio][name=method]');

            $methodRadioButtons.each(function () {
                var $thisButton = $(this);
                if(!currentSelection && $thisButton.prop('checked')) {
                    currentSelection = $thisButton.val();
                }
            });

            return currentSelection;
        }

        function processSlotSelection() {
            $('.trailingSlotSelector').each(function() {
                var toSlotId = $(this).val();
                var allTrailingIds = $(this).data('slotids');
                $(this).data('tempSelectedIds', allTrailingIds.slice(0, allTrailingIds.indexOf(toSlotId) + 1));
            });

            var selectedSlotIds = getSelectedSlotIds();

            // Only show warning on disabled payment methods if booking many slots
            if(selectedSlotIds.length >= 2) {
                $('#book-many-warning').show();
            } else {
                $('#book-many-warning').hide();
            }

            updateModal();
        }

        function processPaymentMethods(data) {
            var currentSelection = getSelectedPaymentMethod();
            var $paymentMethodContainer = $('#paymentMethodContainer');
            var $methodList =  $paymentMethodContainer.find('.payment-method-list');
            var $methodTemplate = $paymentMethodContainer.find('#hidden-method-template');

            $methodList.find('div.payment-method').remove();

            var methods = data.methods, $newRow, name;
            var translations = data.translations, persistSelection = false, htmlToSet, extraInfoKey, hintKey;
            var SELECTOR_METHOD_RADIO_BUTTON = 'input[type=radio][name=method]';

            for(var i = 0; i < methods.length; i++) {
                name = methods[i].name;
                $newRow = $methodTemplate.clone();

                $newRow.removeAttr("style").removeAttr("id").addClass("payment-method");
                $newRow.find(SELECTOR_METHOD_RADIO_BUTTON).attr('id', name).val(name);

                htmlToSet = [translations[name]];
                extraInfoKey = name + ".extraInfo";
                hintKey = name + ".hint"

                if(!!translations[extraInfoKey]) {
                    htmlToSet.push(translations[extraInfoKey]);
                }

                if(!!translations[hintKey]) {
                    htmlToSet.push('<i rel="tooltip" title="" class="fas fa-question-circle text-info" data-original-title="' + translations[hintKey] + '"></i>');
                }

                // So that hint works
                $('[rel="tooltip"]').tooltip();

                $newRow.find('label').attr('for', name).html(htmlToSet.join(" "));

                $methodList.find('#local-payment-methods').before($newRow);

                if(!persistSelection && currentSelection) {
                    persistSelection = (name == currentSelection);
                    if(persistSelection) {
                        $newRow.find(SELECTOR_METHOD_RADIO_BUTTON).prop('checked', true);
                    }
                }
            }

            if(!persistSelection) {
                $methodList.find(SELECTOR_METHOD_RADIO_BUTTON).first().prop('checked', true);
            }

            if(currentSelection && persistSelection) {
                $('.additional-payment-info[rel!="' + currentSelection + '"]').hide();
            } else {
                $('.additional-payment-info').hide();
            }

            toggleLocalPayments(data);
        }

        function toggleLocalPayments(data) {
            if(data.localPaymentMethods) {
                $('#local-payment-methods').show();
            } else {
                $('#local-payment-methods').hide();
            }
        }

        function togglePaymentInfo(data) {
            var totalPrice = data.totalPrice;
            var nPrices = data.nPrices;


            var selectedPaymentMethod = layoutPaymentMethods();

            var $totalPriceInfo = $('#total-price');
            $totalPriceInfo.find(".total-price-span").text(totalPrice + " " + currency);

            var $nWithdrawals = $("#total-price .n-withdrawals");

            if((nPrices !== 1 && totalPrice !== 0) && (selectedPaymentMethod === "CREDIT_CARD_RECUR" || selectedPaymentMethod === "CREDIT_CARD")) {
                $nWithdrawals.text(nPrices);
                $totalPriceInfo.show();
            } else {
                $totalPriceInfo.hide();
            }
        }

        function processBookTrainer(data) {
            var $bookTrainerRow = $('#book-trainer-row');
            if($bookTrainerRow.length) {
                var bookTrainerModel = data.bookTrainerModel;

                var $bookTrainerError = $bookTrainerRow.find('.book-trainer-error');
                var $bookTrainerDropdown = $bookTrainerRow.find('.book-trainer-dropdown');
                var $trainerSelect = $bookTrainerDropdown.find('select[name=trainer]');

                var currentSelectedId = $trainerSelect.selectpicker('val');

                /**
                 * A reason for the chaos here is that different errors messages should be shown if different reasons to not find a trainer to book.
                 * For example if slots are on the same court but not consecutive, they could potentially become.
                 * But if different courts, they can never be consecutive. Or everything is correct but trainer found, that is explained.
                 */
                if(bookTrainerModel.slotsAreConsecutive && bookTrainerModel.slotsAreSameCourt && bookTrainerModel.availableTrainers.length) {

                    $trainerSelect.find('option[value!=""]').remove();

                    var $newTrainerRow, trainer;
                    var newIds = [];

                    for(var i = 0; i < bookTrainerModel.availableTrainers.length; i++) {
                        trainer = bookTrainerModel.availableTrainers[i];
                        $newTrainerRow = $('<option value="' + trainer.id + '">' + trainer.name + '</option>');
                        $newTrainerRow.appendTo($trainerSelect);
                        newIds.push(trainer.id);
                    }

                    $trainerSelect.selectpicker('refresh');

                    if(newIds.indexOf(parseInt(currentSelectedId)) !== -1) {
                        $trainerSelect.selectpicker('val', currentSelectedId);
                    } else {
                        $trainerSelect.selectpicker('val', '');
                    }

                    $bookTrainerError.hide();
                    $bookTrainerDropdown.show();
                } else {
                    var $consecutiveSlotsRequired = $bookTrainerError.find('.consecutive-slots-required');
                    var $noTrainerAvailable = $bookTrainerError.find('.no-trainer-available');
                    $trainerSelect.selectpicker('val', '');

                    if(bookTrainerModel.slotsAreConsecutive && bookTrainerModel.slotsAreSameCourt) {
                        $noTrainerAvailable.show();
                        $consecutiveSlotsRequired.hide();
                    } else {
                        $noTrainerAvailable.hide();
                        $consecutiveSlotsRequired.show();
                    }

                    $bookTrainerError.show();
                    $bookTrainerDropdown.hide();
                }
            }
        }

        function updateModal() {
            $('#btnSubmit').prop('disabled', true);

            var requestedSlotIds = getSelectedSlotIds();

            var paymentMethodsUrl = "${createLink(controller: 'bookingPayment', action: 'updateConfirmModalModel')}" + getFullParams(requestedSlotIds);

            $.ajax({
                cache: false,
                url: paymentMethodsUrl,
                success: function(data) {
                    // Immediately after server accepts the slots as "buyable" by the customer, we update the form
                    $('.trailingSlotSelector').each(function() {
                        $(this).data('selectedids', $(this).data('tempSelectedIds'))
                    });

                    var selectedSlotIds = getSelectedSlotIds();
                    $('[name="slotIds"]').val(selectedSlotIds.join(','));

                    processPaymentMethods(data);

                    // Update the prices so every looks good
                    updatePrices(data);

                    // Trigger functionality as we do when loading the page
                    $('input:radio[name=method]').change(function() {
                        var selected = layoutPaymentMethods();
                        togglePricesByMethod(selected, data);
                        togglePaymentInfo(data);
                    });

                    togglePaymentInfo(data);

                    // Works because loaded in payment method template
                    var selected = layoutPaymentMethods();
                    togglePricesByMethod(selected, data);

                    processBookTrainer(data);

                    $('#btnSubmit').prop('disabled', false);
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    // This means the slot selected is not bookable for the user due to either max bookings or booking restrictions
                    if(!!XMLHttpRequest && XMLHttpRequest.status == 405) {
                        alert(XMLHttpRequest.responseText);

                        // Set selection back to what was selected before trying the new selection
                        $('.trailingSlotSelector').each(function(index) {
                            var selectedIds = $(this).data('selectedids');
                            $(this).val(selectedIds[selectedIds.length - 1]);
                        });
                        $('#btnSubmit').prop('disabled', false);

                    // If user was logged out
                    } else if(!!XMLHttpRequest && XMLHttpRequest.status == 401) {
                        alert('${message(code: "payment.confirm.loggedOut")}');
                        location.href = '${createLink(controller: 'login', action: 'auth', absolute: true)}';
                    } else {

                        /**
                          * All other errors will cause a reload of the page after an error message is displayed.
                          * This is because we cannot guarantee the state of the modal anymore. Should only happen
                          * if disconnected or any other server error occurring.
                          */

                        alert('${message(code: "payment.confirm.ajaxError")}');
                        location.reload(true);
                    }
                }
            });
        };

        $('.trailingSlotSelector').on("change", processSlotSelection);
        $("#promoCodeId").on("change", updateModal);

        $("a[href='#players-list']").one("click", function() {
           safeUpdateModalOnMultiplePrices();
        });

        /**
         * When user changes their customer category
         */
        $('#selectedCustomerCategory').on("change", safeUpdateModalOnMultiplePrices);

        /**
         * When changing added players
         */
        function safeUpdateModalOnMultiplePrices() {
        <g:ifFacilityPropertyEnabled facility="${facility}"
                                     name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name()}">
            updateModal();
        </g:ifFacilityPropertyEnabled>
        };

        updateModal()
});

function removeDuplicateEmail($input, email) {
    var emails = $("input[name=playerEmail]").map(function() {
        if (!$(this).is($input)) {
            return this.value;
        }
    }).get();
    if ($.inArray(email, emails) != -1) {
        $input.val("");
        return true;
    }
    return false;
}

function addTypeahead(el) {
    el.typeahead({
        source: function (query, process) {
            $.ajax({
                cache: false,
                url: "${g.forJavaScript(data: createLink(controller: 'bookingPayment', action: 'bookingPlayers', params: [facilityId: facility.id]))}&q=" +
                            query + "&" + $("#players-list :input[name=playerEmail]").serialize(),
                    success: function(data) {
                        process(data);
                    }
                });
            },
            matcher: function (item) {
                return true;
            },
            sorter: function (items) {
                return items.sort();
            },
            highlighter: function (item) {
                return item;
            },
            updater: function (item) {
                if (item.id != item.fieldValue) {
                    this.$element.prop("readonly", true).attr("data-title", item.id).tooltip({trigger: "hover"});
                }
                this.$element.parent().find("input[name=playerEmail]").val(item.id);
                return item.fieldValue;
            }
        });
    }
</r:script>
</body>
</html>