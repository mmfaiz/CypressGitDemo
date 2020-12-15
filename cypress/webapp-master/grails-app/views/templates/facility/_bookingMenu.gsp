<div class="btn-group ${menuClass}">
    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
        <g:message code="facilityBooking.index.button.more"/>
        <span class="caret"></span>
    </a>

    <ul class="dropdown-menu">
        <li id="blockBookingPaymentBtn" class="disabled">
            <a tabindex="-1" href="javascript: void(0)">
                <g:message code="facilityBooking.index.menu.pay"/>
            </a>
        </li>
        <g:if test="${facility.hasApplicationInvoice()}">
            <li id="invoiceBtn" class="disabled">
                <a tabindex="-1" href="javascript: void(0)">
                    <g:message code="facilityBooking.index.menu.invoice"/>
                </a>
            </li>
        </g:if>
        <g:if test="${facility?.hasBookingRestrictions()}">
            <li class="dropdown-submenu pull-left">
                <a tabindex="-1" href="#"><g:message code="facilityBooking.index.menu.restriction"/></a>
                <ul class="dropdown-menu">
                    <li id="addBookingRestrictionBtn" class="disabled">
                        <g:remoteLink
                                tabindex="-1"
                                controller="facilityBookingRestrictions"
                                action="create"
                                update="bookingModal"
                                params="{ slotIds: \$('#addRestrictionSlotData').val(), date: '${params.date}' }"
                                onSuccess="showLayer()"
                                before="if(!hasUnRestrictionSlots()) return false;"
                                >${message(code: "button.add.label")}</g:remoteLink>
                    </li>
                    <li id="delBookingRestrictionBtn" class="disabled">
                        <a tabindex="-1" href="javascript: void(0)">
                            ${message(code: "button.delete.label")}
                        </a>
                    </li>
                </ul>
            </li>
        </g:if>
        <g:if test="${facility.hasSMS()}">
            <li class="dropdown-submenu pull-left">
                <a tabindex="-1" href="#"><g:message code="button.submit.label"/></a>
                <ul class="dropdown-menu">
                    <li id="sendEmailBtn" class="disabled">
                        <a tabindex="-1" href="javascript: void(0)">
                            <g:message code="facilityBooking.index.menu.send.email"/>
                        </a>
                    </li>
                    <li id="sendSmsBtn" class="disabled">
                        <a tabindex="-1" href="javascript: void(0)">
                            <g:message code="facilityBooking.index.menu.send.sms"/>
                        </a>
                    </li>
                </ul>
            </li>
        </g:if>
        <g:else>
            <li id="sendEmailBtn" class="disabled">
                <a tabindex="-1" href="javascript: void(0)">
                    <g:message code="facilityBooking.index.menu.send.email"/>
                </a>
            </li>
        </g:else>
        <li class="dropdown-submenu pull-left">
            <a tabindex="-1" href="#"><g:message code="facilityBooking.index.menu.export"/></a>
            <ul class="dropdown-menu">
                <li id="blockBookingExportBookingsBtn" class="disabled">
                    <a tabindex="-1" href="javascript: void(0)">
                        <g:message code="facilityBooking.index.menu.export.bookings"/>
                    </a>
                </li>
                <li id="blockBookingExportCustomersBtn" class="disabled">
                    <a tabindex="-1" href="javascript: void(0)">
                        <g:message code="facilityBooking.index.menu.export.customers"/>
                    </a>
                </li>
            </ul>
        </li>
    </ul>
</div>

<r:script>
    function submitSlotsForm(menuEl, action, target) {
        if (!menuEl.parent().hasClass("disabled")) {
            var form = $("#selectedSlotsForm").get(0);
            var origAction = form.action;
            form.action = typeof extendSubmitSlotsFormAction === "function" ?
                    extendSubmitSlotsFormAction(action) : action;
            if (target) {
                form.target = target;
            }
            form.submit();
            form.action = origAction;
            form.target = "";
        }
    }

    $(function() {
        $("#blockBookingExportBookingsBtn").find("a").click(function() {
            submitSlotsForm($(this), "${g.forJavaScript(data: createLink(action: 'exportBookings'))}", "_blank");
        });

        $("#blockBookingExportCustomersBtn").find("a").click(function() {
            submitSlotsForm($(this), "${g.forJavaScript(data: createLink(action: 'exportBookingCustomers'))}", "_blank");
        });

        $("#sendEmailBtn").find("a").click(function() {
            submitSlotsForm($(this), "${g.forJavaScript(data: createLink(action: 'sendMessage', params: [returnUrl: returnUrl]))}");
        });

        $("#sendSmsBtn").find("a").click(function() {
            submitSlotsForm($(this), "${g.forJavaScript(data: createLink(action: 'sendMessage', params: [type: 'sms', returnUrl: returnUrl]))}");
        });

        $("#delBookingRestrictionBtn").find("a").click(function() {
            submitSlotsForm($(this), "${g.forJavaScript(data: createLink(controller: 'facilityBookingRestrictions', action: 'delete'))}");
        });

        $("#blockBookingPaymentBtn").find("a").click(function() {
            <g:if test="${facility.boxnet}">
                if (!$(this).parent().hasClass("disabled")) {
                    $.ajax({
                        type: "POST",
                        data: $("#selectedSlotsForm").serialize(),
                        url: "${g.forJavaScript(data: createLink(action: 'bookingAlterForm'))}",
                        success: function(data, textStatus) {
                            jQuery('#bookingModal').html(data);
                            showLayer();
                        }
                    });
                }
            </g:if>
            <g:else>
                if (confirm("${message(code: 'facilityBooking.alterPaid.confirm')}")) {
                    submitSlotsForm($(this), "${g.forJavaScript(data: createLink(action: 'alterPaid', params: [returnUrl: returnUrl]))}");
                }
            </g:else>
        });

        <g:if test="${facility.hasApplicationInvoice()}">
            $("#invoiceBtn").find("a").click(function() {
                if (confirm("${message(code: 'facilityBooking.alterPaid.confirm')}")) {
                    submitSlotsForm($(this), "${g.forJavaScript(data: createLink(action: 'alterPaid', params: [useInvoice: true, returnUrl: returnUrl]))}");
                }
            });
        </g:if>
    });
</r:script>
