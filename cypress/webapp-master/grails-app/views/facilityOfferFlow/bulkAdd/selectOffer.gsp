<%@ page import="com.matchi.coupon.GiftCard; com.matchi.coupon.Coupon" %>

<html>
    <head>
        <meta name="layout" content="facilityLayout">
        <title><g:message code="facilityOffer.bulkAdd.title"/></title>
        <r:script>
            var coupons = [];

            $(document).ready(function() {
                <g:each in="${coupons.values()?.flatten()}">
                    coupons.push({id: "${g.forJavaScript(data: it.id)}", nrOfTickets: "${g.forJavaScript(data: it.nrOfTickets)}",
                            expireDate: "${g.forJavaScript(data: it.expireDate ? it.expireDate.toString("${message(code: 'date.format.dateOnly')}") : '')}",
                            unlimited: "${g.forJavaScript(data: it.unlimited)}"});
                </g:each>

                var $showExpireDate = $("#showExpireDate");

                $("#couponId").on("change", function() {
                    var id = $(this).val();

                    if(id && coupons.length > 0) {
                        var coupon = $.grep(coupons, function(e){ return e.id == id; })[0];
                        var $nrOfTickets = $("#nrOfTickets");

                        if(coupon.unlimited.toLowerCase() == "true") {
                            $nrOfTickets.prop("disabled", true);
                            $nrOfTickets.attr("title", "${message(code: 'facilityCoupon.addCouponForm.message9')}");
                        } else {
                            $nrOfTickets.prop("disabled", false);
                            $nrOfTickets.attr("title", "");
                        }

                        $nrOfTickets.val(coupon.nrOfTickets);
                        $("#coupon-info").slideDown();

                        var $expireDate = $("#expireDate");
                        $showExpireDate.val(coupon.expireDate);
                        $expireDate.val(coupon.expireDate);
                    } else {
                        $("#coupon-info").slideUp();
                    }
                });

                $showExpireDate.datepicker({
                    autoSize: true,
                    dateFormat: '${message(code: 'date.format.dateOnly.small')}',
                    altField: '#expireDate',
                    altFormat: 'yy-mm-dd'
                });

                <g:if test="${cmd?.couponId}">
                    $("#coupon-info").slideDown();
                </g:if>
            });
        </r:script>
    </head>

    <body>
        <ul class="breadcrumb">
            <li>
                <a href="${returnUrl}">
                    <g:message code="customer.label.plural"/>
                </a>
                <span class="divider">/</span>
            </li>
            <li class="active"><g:message code="facilityOffer.bulkAdd.title"/></li>
        </ul>

        <g:errorMessage bean="${cmd}"/>

        <g:render template="/templates/wizard"
                model="[steps: [message(code: 'facilityOffer.bulkAdd.step1'), message(code: 'facilityOffer.bulkAdd.step2')], current: 0]"/>

        <h3><g:message code="facilityOffer.bulkAdd.step1.heading"/></h3>
        <p class="lead">
            <g:message code="facilityOffer.bulkAdd.step1.desc"/>
        </p>

        <g:form name="couponForm">
            <div class="well">
                <div class="control-group">
                    <div class="controls">
                        <select id="couponId" name="couponId" class="span3 required">
                            <g:if test="${coupons[Coupon.class]}">
                                <option value=""> ---- <g:message code="payment.confirm.chooseCoupon"/> ---- </option>
                                <g:each in="${coupons[Coupon.class]}" var="coupon">
                                    <option value="${coupon.id}" ${cmd?.couponId == coupon.id ? 'selected' : ''}>
                                        ${coupon.name.encodeAsHTML()}
                                    </option>
                                </g:each>
                            </g:if>
                            <g:if test="${coupons[GiftCard.class]}">
                                <option value=""> ---- <g:message code="payment.confirm.chooseGiftCard"/> ---- </option>
                                <g:each in="${coupons[GiftCard.class]}" var="coupon">
                                    <option value="${coupon.id}" ${cmd?.couponId == coupon.id ? 'selected' : ''}>
                                        ${coupon.name.encodeAsHTML()}
                                    </option>
                                </g:each>
                            </g:if>
                        </select>
                    </div>
                </div>
                <div id="coupon-info" style="display: none">
                    <div class="control-group">
                        <ul class="inline">
                            <li>
                                <label class="control-label" for="nrOfTickets">
                                    <g:if test="${coupons[Coupon.class]}">
                                        <g:message code="facilityCoupon.addCouponForm.message4"/>
                                    </g:if>
                                    <g:if test="${coupons[GiftCard.class]}">
                                        <g:message code="facilityCoupon.addCouponForm.message6"/>
                                    </g:if>
                                </label>

                                <g:textField name="nrOfTickets" class="span1 required"
                                        value="${cmd?.nrOfTickets}"/>
                            </li>
                            <li>
                                <label for="expireDate">
                                    <strong><g:message code="facilityCoupon.addCouponForm.message5"/></strong>
                                    (<g:message code="date.inclusive.label"/>)
                                </label>
                                <g:textField name="showExpireDate" class="span2" readonly="true"
                                        value="${formatDate(date: cmd?.expireDate?.toDate(), formatName: 'date.format.dateOnly')}"/>
                                <g:hiddenField name="expireDate"
                                        value="${cmd?.expireDate?.toString('yyyy-MM-dd')}"/>
                            </li>
                        </ul>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="note"><g:message code="customerCoupon.note.label"/></label>
                        <g:textArea cols="50" rows="2" name="note" class="span11" value="${cmd?.note}"
                                placeholder="${message(code: 'facilityCoupon.addCouponForm.message7')}"/>
                    </div>
                </div>
            </div>

            <table class="table table-transparent">
                <thead>
                    <tr>
                        <th><g:message code="customer.number.label"/></th>
                        <th><g:message code="default.name.label"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${customerInfo}" var="customer">
                        <tr>
                            <td>${customer.number}</td>
                            <td>${customer.name.encodeAsHTML()}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>

            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                        value="${message(code: 'button.next.label')}" data-toggle="button"
                        show-loader="${message(code: 'default.loader.label')}"/>
            </div>
        </g:form>
    </body>
</html>
