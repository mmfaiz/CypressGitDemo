<li>
  <a data-toggle="tab" href="#coupon_${coupon.id}" class="padding10">
    <div class="media">
      <div class="pull-right">
        <div class="coupon-tickets bg-grey-light border-radius vertical-padding10 horizontal-padding10 text-center">
          <g:if test="${coupon.unlimited}">
            <div rel="tooltip" title="${message(code: 'templates.facility.listCouponOnline.message5')}*">
              <span class="block h3 no-margin bold"><i class="fa fa-repeat"></i></span>
              <small class="block"><g:message code="templates.facility.listCouponOnline.message6"/>*</small>
            </div>
          </g:if>
          <g:else>
            <span class="block h3 no-margin bold">${coupon.nrOfTickets}</span>
            <span class="block"><g:message code="coupon.nrOfTickets.label${coupon.instanceOf(com.matchi.coupon.GiftCard) ? '3' : '2'}"/></span>
          </g:else>
        </div>
      </div><!-- pull-right -->
      <span class="pull-left icon-lg">
        <i class="fa fa-ticket text-warning"></i>
      </span>

      <div class="media-body">
        <div class="top-margin5">
          <h5 class="coupon-name block weight400">
            ${coupon.name}
          </h5>
        </div>

        <div class="text-muted text-xs right-margin5">
          <span class="block">
            <i class="fas fa-credit-card"></i> <g:formatMoney value="${coupon.getPrice(customer)}" facility="${coupon.facility}"/>
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
      </div>
    </div>
  </a>
</li>