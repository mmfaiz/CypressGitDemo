<%@ page import="com.matchi.coupon.CustomerCoupon; org.joda.time.DateTime; com.matchi.coupon.Coupon" %>
<g:set var="offerType" value="${customerCoupon.coupon.getOfferTypeString()}"/>
<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: "parent.location.href = removeParam('comeback', parent.location.href);"]}">

<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: customerCoupon?.coupon?.facility, orders: [order]]" />

    <h1 class="h3"><g:message code="default.modal.thankYou"/></h1>
    <p><g:message code="${offerType}Payment.receipt.description1" args="[order?.user?.email]"/></p>
    <p><g:message code="${offerType}Payment.receipt.description2" args="[createLink(controller: 'userProfile', action: 'home')]"/></p>

    <hr>

    <div class="row">
        <div class="col-sm-4">
            <h6><g:message code="${offerType}.label"/></h6>
            <p class="text-muted ellipsis">${customerCoupon}</p>
        </div>
        <g:if test="${customerCoupon.getExpireDate()}">
            <div class="col-sm-4">
                <h6><g:message code="couponPayment.confirm.validTo"/></h6>
                <p class="text-muted ellipsis"><g:formatDate date="${customerCoupon?.getExpireDate()?.toDate()}" formatName="date.format.dateOnly"/></p>
            </div>
        </g:if>
        <div class="col-sm-4">
            <h6><g:message code="${offerType}.nrOfTickets.label2"/></h6>
            <p class="text-muted ellipsis">
                <g:if test="${customerCoupon.coupon.unlimited}">
                    <span>&infin;</span>
                </g:if>
                <g:else>
                    ${customerCoupon.nrOfTickets} <g:message code="unit.st"/>
                </g:else>
            </p>
        </div>
    </div>

    <g:if test="${customerCoupon.coupon.couponConditionGroups}">
        <hr>
        <div>
            <h6><g:message code="default.terms.label"/></h6>
        </div>
        <div>
            <g:each in="${customerCoupon.coupon.couponConditionGroups}" var="conditionGroup">
                <g:each in="${conditionGroup.slotConditionSets}" var="conditionSet">
                    <div class="condition">
                        <g:each in="${conditionSet.slotConditions}" var="condition">
                            <g:slotConditionEntry condition="${condition}"/>
                        </g:each>
                    </div>
                </g:each>
            </g:each>
        </div>
    </g:if>
</g:applyLayout>