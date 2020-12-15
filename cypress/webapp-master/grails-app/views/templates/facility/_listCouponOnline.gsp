<%@ page import="com.matchi.coupon.GiftCard; com.matchi.coupon.Coupon; org.joda.time.DateTime" %>
<div class="row">
    <div class="col-sm-6">
        <ul class="nav nav-pills nav-stacked list-coupons" id="couponTab">
            <g:if test="${couponsAvailableForPurchase[Coupon.class]}">
                <div class="panel-heading">
                    <h5 class="no-margin"><g:message code="offer.Coupon.label"/></h5>
                </div>
                <g:each in="${couponsAvailableForPurchase[Coupon.class]}" var="coupon">
                    <g:render template="/templates/facility/coupon" model="[coupon: coupon]"/>
                </g:each>
            </g:if>
            <g:if test="${couponsAvailableForPurchase[GiftCard.class]}">
                <div class="panel-heading">
                    <h5 class="no-margin"><g:message code="offer.GiftCard.label"/></h5>
                </div>
                <g:each in="${couponsAvailableForPurchase[GiftCard.class]}" var="coupon">
                    <g:render template="/templates/facility/coupon" model="[coupon: coupon]"/>
                </g:each>
            </g:if>
        </ul>
    </div>

    <div class="col-sm-6 horizontal-padding30">
        <div class="tab-content">
            <g:each in="${couponsAvailableForPurchase.values()?.flatten()}" var="coupon">
                <div class="tab-pane fade in active" id="coupon_${coupon.id}">
                <h5 class="top-margin20 weight400">${coupon.name}</h5>

                <p class="text-sm text-wrap">${coupon.description}</p>

                <div class="text-muted">
                    <span class="block">
                        <g:set var="couponEndDate" value="${coupon.expireDate?.toDate()}"/>
                        <g:if test="${couponEndDate}">
                            <i class="fa fa-clock-o"></i>
                            <g:message code="templates.facility.listCouponOnline.message8"
                                    args="[formatDate(date: couponEndDate, formatName: 'date.format.readable.year')]"/>
                        </g:if>
                        <g:else>
                            <i class="fas fa-info-circle"></i> <g:message code="templates.facility.listCouponOnline.message9"/>
                        </g:else>
                    </span>
                </div>
                <hr class="vertical-margin15">

                <p>
                <ul class="list-table">
                    <g:set var="price" value="${coupon.getPrice(customer, true)}"/>
                    <li>
                        <sec:ifLoggedIn>
                            <g:if test="${price != null}">
                                <g:remoteLink class="btn btn-success purchase-coupon btn-lg"
                                              controller="couponPayment"
                                              action="confirm"
                                              params="[id: coupon.id]"
                                              update="userBookingModal"
                                              onFailure="handleAjaxError(XMLHttpRequest, textStatus, errorThrown)"
                                              onSuccess="showLayer('userBookingModal')"><g:message code="button.buy.label"/></g:remoteLink>
                            </g:if>
                            <g:else>
                                <div class="bottom-padding10"><g:message code="templates.facility.listCouponOnline.message10"/></div>
                            </g:else>
                        </sec:ifLoggedIn>
                        <sec:ifNotLoggedIn>
                            <g:link controller="login" class="btn btn-success btn-sm" action="index"
                                    params="[returnUrl: request.forwardURI, wl: params?.wl]" rel="tooltip"
                                    title="${message(code: 'templates.facility.listCouponOnline.message12')}"><g:message code="default.navigation.login"/></g:link>
                        </sec:ifNotLoggedIn>
                    </li>
                    <g:if test="${price != null}">
                        <li><h5><g:formatMoney value="${coupon.getPrice(customer)}" facility="${coupon.facility}"/></h5></li>
                    </g:if>
                </ul>
                </p>
            </div>
            </g:each>
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function () {
        $('[rel=tooltip]').tooltip();

        $('#couponTab a:first').tab('show');
    });
</r:script>
