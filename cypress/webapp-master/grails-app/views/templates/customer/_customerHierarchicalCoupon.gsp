<%@ page import="com.matchi.coupon.GiftCard; com.matchi.coupon.Coupon" %>
<g:if test="${active || locked || archive}">
    <div class="info-box">
        <p class="lead header">
            <span class="${active?.size() < 1 ? "transparent-60" : ""}">
                <g:message
                        code="offer.label.global"/> ${active?.size() > 0 ? " - ${active?.size()}${message(code: 'unit.st')}" : ""}
            </span>
            <br>
            <small class="empty"><g:message code="templates.customer.globalMembership.message"/></small>
            <g:if test="${active?.size() > 4}">
                <g:remoteLink controller="facilityCustomer" action="showCoupons" update="customerModal"
                              class="btn btn-small"
                              style="vertical-align: text-bottom;"
                              title="${message(code: 'templates.customer.customerCoupon.message12')}"
                              onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                              params="['couponIds': active?.collect { it.id }, 'customerId': customer.id]"><g:message
                        code="default.multiselect.checkAllText"/></g:remoteLink>
            </g:if>
        </p>
        <g:if test="${active?.size() > 0 || locked?.size() > 0 || archive.size() > 0}">
            <table class="table table-transparent table-condensed table-noborder table-striped table-fixed no-bottom-margin">
                <thead class="table-header-transparent">
                <th class="ellipsis"><g:message code="coupon.name.label"/></th>
                <th class="ellipsis"><g:message code="home.customers.message8"/></th>
                <th class="center-text break-word-text" style="width: 18%">
                    <g:message code="coupon.nrOfTickets.label2"/>/<g:message code="coupon.nrOfTickets.label3"/>
                </th>
                <th class="center-text" style="width: 22%"><g:message
                        code="templates.customer.customerCoupon.message5"/></th>
                <th class="center-text" style="width: 14%"><g:message code="default.status.label"/></th>
                <th style="width: 14px"></th>
                <th style="width: 14px"></th>
                </thead>
                <tbody>
                <g:set var="activeCoupons" value="${active.findAll { it.coupon instanceof Coupon }}"/>
                <g:set var="activeGiftCards" value="${active.findAll { it.coupon instanceof GiftCard }}"/>
                <g:if test="${activeCoupons}">
                    <tr><td colspan="6"><strong><g:message code="offer.Coupon.label"/></strong></td></tr>
                    <g:each in="${activeCoupons}" var="coupon" status="i">
                        <tr class="${i > 3 ? "hidden" : ""}">
                            <td class="ellipsis">${coupon.coupon.name}</td>
                            <td class="ellipsis">${coupon.customer.facility.name}</td>
                            <td class="center-text">
                                ${coupon.coupon.unlimited ? "-" : coupon.nrOfTickets + " " + message(code: 'unit.st')}
                            </td>
                            <td class="center-text">${coupon.expireDate ? coupon.expireDate.toString("${message(code: 'date.format.dateOnly')}") : ""}</td>
                            <td class="center-text">
                                <g:if test="${!coupon.dateLocked}">
                                    <span class="label label-success"><g:message
                                            code="templates.customer.customerCoupon.active"/></span>
                                </g:if>
                                <g:else>
                                    <span class="label label-important"><g:message
                                            code="templates.customer.customerCoupon.message8"/></span>
                                </g:else>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:if test="${activeGiftCards}">
                    <tr><td colspan="6"><strong><g:message code="offer.GiftCard.label"/></strong></td></tr>
                    <g:each in="${activeGiftCards}" var="coupon" status="i">
                        <tr class="${i > 3 ? "hidden" : ""}">
                            <td class="ellipsis">${coupon.coupon.name}</td>
                            <td class="ellipsis">${coupon.customer.facility.name}</td>
                            <td class="center-text">
                                ${coupon.coupon.unlimited ? "-" : coupon.nrOfTickets + " " + message(code: 'unit.credits')}
                            </td>
                            <td class="center-text">${coupon.expireDate ? coupon.expireDate.toString("${message(code: 'date.format.dateOnly')}") : ""}</td>
                            <td class="center-text">
                                <g:if test="${!coupon.dateLocked}">
                                    <span class="label label-success"><g:message
                                            code="templates.customer.customerCoupon.active"/></span>
                                </g:if>
                                <g:else>
                                    <span class="label label-important"><g:message
                                            code="templates.customer.customerCoupon.message8"/></span>
                                </g:else>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:if test="${locked?.size() > 0}">
                    <tr>
                        <td class="lockedControl" onclick="toggleLocked()" style="cursor: pointer;" colspan="6">
                            <em><g:message code="templates.customer.customerCoupon.message13"
                                           args="[locked?.size()]"/> <i id="locked-indicator"
                                                                        class="icon-chevron-right"></i></em>
                        </td>
                    </tr>
                    <g:each in="${locked}" var="lockedCoupon">
                        <tr class="lockedCoupon" style="display: none;">
                            <td class="ellipsis">${lockedCoupon.coupon.name}</td>
                            <td class="ellipsis">${lockedCoupon.customer.facility.name}</td>
                            <td class="center-text">${lockedCoupon.coupon.unlimited ? "~" : lockedCoupon.nrOfTickets} <g:message
                                    code="unit.st"/></td>
                            <td class="center-text">${lockedCoupon.expireDate ? lockedCoupon.expireDate.toString("${message(code: 'date.format.dateOnly')}") : ""}</td>
                            <td class="center-text">
                                <span class="label label-important"><g:message
                                        code="templates.customer.customerCoupon.message8"/></span>
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <g:if test="${archive?.size() > 0}">
                    <tr>
                        <td class="archiveControl" onclick="toggleArchive()" style="cursor: pointer;" colspan="6">
                            <em><g:message code="templates.customer.customerCoupon.message14"
                                           args="[archive?.size()]"/> <i id="archive-indicator"
                                                                         class="icon-chevron-right"></i></em>
                        </td>
                    </tr>
                    <g:each in="${archive}" var="usedCoupon">
                        <tr class="usedCoupon" style="display: none;">
                            <td class="ellipsis">${usedCoupon.coupon.name}</td>
                            <td class="ellipsis">${usedCoupon.customer.facility.name}</td>
                            <td class="center-text">${usedCoupon.coupon.unlimited ? "~" : usedCoupon.nrOfTickets} st</td>
                            <td class="center-text"
                                width="100">${usedCoupon.expireDate ? usedCoupon.expireDate.toString("${message(code: 'date.format.dateOnly')}") : ""}</td>
                            <td class="center-text">
                                <span class="label label-info"><g:message
                                        code="templates.customer.customerCoupon.message10"/></span>
                            </td>
                            <td class="center-text">&nbsp;</td>
                        </tr>
                    </g:each>
                </g:if>
                </tbody>
            </table>
        </g:if>
        <r:script>
            function toggleArchive() {
                if ($("#archive-indicator").hasClass("icon-chevron-right")) {
                    $("#archive-indicator").removeClass("icon-chevron-right");
                    $("#archive-indicator").addClass("icon-chevron-down");
                } else {
                    $("#archive-indicator").removeClass("icon-chevron-down");
                    $("#archive-indicator").addClass("icon-chevron-right");
                }

                $(".usedCoupon").toggle();
            }
            function toggleLocked() {
                if ($("#locked-indicator").hasClass("icon-chevron-right")) {
                    $("#locked-indicator").removeClass("icon-chevron-right");
                    $("#locked-indicator").addClass("icon-chevron-down");
                } else {
                    $("#locked-indicator").removeClass("icon-chevron-down");
                    $("#locked-indicator").addClass("icon-chevron-right");
                }

                $(".lockedCoupon").toggle();
            }
        </r:script>
    </div>
</g:if>