<g:set var="facility" value="${order.customer.facility}"/>
<g:set var="customer" value="${order.customer}"/>

<div class="list-group-item row row-full">
    <div class="col-sm-2">
        <g:link controller="facility" action="show" params="[name: facility.shortname]">
            <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}"
                                            alt="${facility.name}"
                                            class="img-responsive center-block"
                                            height="50"/>
        </g:link>
    </div>

    <div class="col-sm-6">
        <strong>
            <g:message code="order.article.${order.article}"/>
        </strong><br>

        <p class="text-sm">
            ${order.description}
        </p>
    </div>

    <div class="col-sm-4">
        <div class="btn-group full-width">
            <g:remoteLink
                    controller="remotePayment"
                    params="[id: order.id, finishUrl: createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname, 'orderId': order.id]])]"
                    action="confirm"
                    update="userBookingModal"
                    onFailure="handleAjaxError()"
                    onSuccess="showLayer('userBookingModal')">
                <button type="button"
                        class="btn btn-sm btn-success">
                    <g:message code="button.finish.purchase.label"/>
                </button>
            </g:remoteLink>
        </div>
    </div>
</div>