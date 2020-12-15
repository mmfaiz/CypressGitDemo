<%@ page import="org.joda.time.DateTime; com.matchi.User; com.matchi.Facility; com.matchi.coupon.GiftCard" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="offer.${params.type}.label"/></title>
    <r:require modules="matchi-customerselect" />
</head>
<body>
<g:set var="cardType" value="${message(code: "offer.${params.type}.label2")}"/>
<ul class="breadcrumb">
    <li><g:link mapping="${params.type}" action="index"><g:message code="offer.${params.type}.label"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCoupon.sold.message30" args="[coupon.name]"/></li>
</ul>

<ul class="nav nav-tabs">
    <li class="active"><g:link mapping="${params.type}" action="sold" id="${coupon?.id}"><g:message code="customer.label.plural"/></g:link></li>
    <li>
        <g:link mapping="${params.type + 'Prices'}" action="index" id="${coupon?.id}">
            <g:message code="couponPrice.label.plural"/>
        </g:link>
    </li>
    <li>
        <g:link mapping="${params.type}" action="edit" id="${coupon?.id}"><g:message code="button.edit.label"/></g:link>
    </li>
    <li><g:link mapping="${params.type + 'Conditions'}" controller="facilityCouponCondition" action="list" id="${coupon.id}" ><g:message code="default.terms.label"/></g:link></li>
</ul>

<div class="space10"></div>
<div class="tabbable">
    <ul class="nav nav-pills" style="margin-bottom: 5px;">
        <li class="active"><a href="#tab1" data-toggle="tab"><g:message code="facilityCoupon.sold.${params.type == 'GiftCard' ? 'activeGiftCards' : 'message31'}" args="[active.size()]"/></a></li>
        <li><a href="#tab2" data-toggle="tab"><g:message code="facilityCoupon.sold.${params.type == 'GiftCard' ? 'lockedGiftCards' : 'message32'}" args="[locked.size()]"/></a></li>
        <li><a href="#tab3" data-toggle="tab"><g:message code="facilityCoupon.sold.message33" args="[archive.size()]"/></a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="tab1">
            <p><g:message code="facilityCoupon.sold.message6" args="[cardType]"/></p>
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <g:sortableColumn property="customer.lastname" params="${params}" titleKey="customer.label"/>
                    <th width="150" class="center-text"><g:message code="facilityCoupon.sold.${params.type == 'GiftCard' ? 'remainingGiftCardTickets' : 'message7'}"/></th>
                    <g:sortableColumn property="note" params="${params}" titleKey="facilityCoupon.sold.message15" width="120" class="center-text"/>
                    <g:sortableColumn property="expireDate" params="${params}" titleKey="coupon.nrOfDaysValid.label" width="120" class="center-text"/>
                    <g:sortableColumn property="dateCreated" params="${params}" titleKey="facilityCoupon.sold.message17" width="150" class="center-text"/>
                </tr>
                </thead>


                <tbody>
                <g:if test="${active.size() < 1}">
                    <tr>
                        <td class="nolink" colspan="5"><em><g:message code="facilityCoupon.sold.message8" args="[cardType]"/></em></td>
                    </tr>
                </g:if>
                <g:each in="${active}" var="customerCoupon">
                    <tr id="${customerCoupon.id}" class="rowlink">
                        <td><g:link controller="facilityCustomer" params="[id:customerCoupon.customer.id]" action="show">${customerCoupon.customer}</g:link></td>
                        <td class="center-text">
                            <span class="label label-${customerCoupon.coupon?.unlimited ? "warning" : customerCoupon.nrOfTickets > 0 ? 'success' : 'important'}">
                                ${customerCoupon.coupon?.unlimited ? "-" : customerCoupon.nrOfTickets}</span>
                        </td>
                        <td class="center-text">
                            <g:if test="${customerCoupon.note}">
                                <span class="label label-info" rel="tooltip" title="${g.toRichHTML(text: customerCoupon.note)}">
                                    ...
                                </span>
                            </g:if>
                        </td>
                        <td class="center-text">${customerCoupon.expireDate ? customerCoupon.expireDate.toString("${message(code: "date.format.dateOnly")}"): ""}</td>
                        <td class="center-text"><g:formatDate date="${customerCoupon.dateCreated}" formatName="date.format"/></td>
                    </tr>
                    <tr id="${customerCoupon.id}_details" style="display: none;" class="nolink">
                        <td colspan="5" class="vertical-padding10 nolink">
                            <g:form controller="facilityOffer" action="updateCustomerCoupon" id="${customerCoupon.id}" mapping="${params.type}">
                                <g:if test="${customerCoupon.coupon.instanceOf(GiftCard) && !customerCoupon.coupon.unlimited}">
                                    <label class="control-label" for="nrOfTickets">
                                        <g:message code="facilityCoupon.addCouponForm.message6"/>
                                    </label>
                                    <g:textField name="nrOfTickets" class="span1 required"
                                            value="${customerCoupon.nrOfTickets}"/>
                                </g:if>
                                <label for="note"><strong><g:message code="customerCoupon.note.label"/></strong></label>
                                <g:textArea rows="2" cols="50" name="note" class="span6" value="${customerCoupon.note}"/>

                                <div class="">
                                    <g:submitButton name="submit" class="btn btn-success" value="${message(code: 'facilityCoupon.sold.message38')}"/>
                                    <g:actionSubmit controller="facilityOffer" action="lockCustomerCoupon" id="${customerCoupon.id}"
                                            onclick="return confirm('${message(code: 'facilityCoupon.sold.message34', args: [cardType])}');"
                                            class="btn btn-inverse" value="${message(code: 'facilityCoupon.sold.message44')}"/>

                                    <g:if test="${customerCoupon.couponTickets.size() == customerCoupon.nrOfTickets}">
                                        <g:link action="removeFromCustomer" id="${customerCoupon.id}" title="${message(code: 'button.delete.label')}"
                                                onclick="return confirm('${message(code: 'facilityCoupon.sold.message35', args: [cardType])}')" class="btn btn-danger">
                                                <g:message code="button.delete.label"/>
                                        </g:link>
                                    </g:if>

                                    <g:if test="${!customerCoupon.coupon.instanceOf(GiftCard)}">
                                        <g:actionSubmit controller="facilityOffer" action="removeCouponTicket" id="${customerCoupon.id}"
                                                        onclick="return confirm('${message(code: 'facilityCoupon.sold.message36')}');"
                                                        class="btn btn-info" value="-1"/>
                                        <g:actionSubmit controller="facilityOffer" action="addCouponTicket" id="${customerCoupon.id}"
                                                        onclick="return confirm('${message(code: 'facilityCoupon.sold.message37')}');"
                                                        class="btn btn-info" value="+1"/>
                                    </g:if>
                                </div>
                            </g:form>
                        </td>
                    </tr>
                </g:each>

                </tbody>
            </table>
        </div>
        <div class="tab-pane" id="tab2">
            <p><g:message code="facilityCoupon.sold.message12"/></p>
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th><g:message code="customer.label"/></th>
                    <th width="150" class="center-text"><g:message code="facilityCoupon.sold.${params.type == 'GiftCard' ? 'remainingGiftCardTickets' : 'message7'}"/></th>
                    <th width="120" class="center-text"><g:message code="facilityCoupon.sold.message15"/></th>
                    <th width="120" class="center-text"><g:message code="coupon.nrOfDaysValid.label"/></th>
                    <th width="150" class="center-text"><g:message code="facilityCoupon.sold.message17"/></th>
                </tr>
                </thead>


                <tbody>
                <g:if test="${locked.size() == 0}">
                    <tr>
                        <td colspan="5"><em><g:message code="facilityCoupon.sold.message18"/></em></td>
                    </tr>
                </g:if>
                <g:each in="${locked}" var="customerCoupon">
                    <tr id="${customerCoupon.id}" class="rowlink">
                        <td>${customerCoupon.customer}</td>
                        <td class="center-text">
                            <span class="label label-${customerCoupon.coupon?.unlimited ? "warning" : customerCoupon.nrOfTickets > 0 ? 'success' : 'important'}">
                                ${customerCoupon.coupon?.unlimited ? "-" : customerCoupon.nrOfTickets}
                            </span>
                        </td>
                        <td class="center-text">
                            <g:if test="${customerCoupon.note}">
                                <span class="label label-info" rel="tooltip" title="${g.toRichHTML(text: customerCoupon.note)}">
                                    ...
                                </span>
                            </g:if>
                        </td>
                        <td class="center-text">${customerCoupon.expireDate ? customerCoupon.expireDate.toString("${message(code: "date.format.dateOnly")}"): ""}</td>
                        <td class="center-text"><g:formatDate date="${customerCoupon.dateCreated}" formatName="date.format"/></td>
                    </tr>
                    <tr id="${customerCoupon.id}_details" style="display: none;" class="nolink">
                        <td colspan="5" class="vertical-padding10 nolink">
                            <g:form controller="facilityOffer" action="updateCustomerCoupon" id="${customerCoupon.id}" mapping="${params.type}">
                                <label for="note"><strong><g:message code="facilityCoupon.sold.message20"/></strong></label>
                                <g:textArea rows="2" cols="50" name="note" class="span6" value="${customerCoupon.note}"/>

                                <div class="">
                                    <g:submitButton name="submit" class="btn btn-success" value="${message(code: 'facilityCoupon.sold.message38')}"/>
                                    <g:actionSubmit controller="facilityOffer" action="unlockCustomerCoupon" id="${customerCoupon.id}"
                                            onclick="return confirm('${message(code: 'facilityCoupon.sold.message39')}');"
                                            class="btn btn-inverse" value="${message(code: 'facilityCoupon.sold.message40')}"/>

                                    <g:if test="${customerCoupon.couponTickets.size() == customerCoupon.nrOfTickets}">
                                        <g:link action="removeFromCustomer" id="${customerCoupon.id}" title="${message(code: 'button.delete.label')}"
                                                onclick="return confirm('${message(code: 'facilityCoupon.sold.message41')}')" class="btn btn-danger">
                                            <g:message code="button.delete.label"/>
                                        </g:link>
                                    </g:if>
                                </div>
                            </g:form>
                        </td>
                    </tr>
                </g:each>

                </tbody>
            </table>
        </div>
        <div class="tab-pane" id="tab3">
            <p><g:message code="facilityCoupon.sold.message42"/></p>
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th><g:message code="customer.label"/></th>
                    <th width="150" class="center-text"><g:message code="facilityCoupon.sold.${params.type == 'GiftCard' ? 'remainingGiftCardTickets' : 'message7'}"/></th>
                    <th width="120" class="center-text"><g:message code="facilityCoupon.sold.message15"/></th>
                    <th width="120" class="center-text"><g:message code="coupon.nrOfDaysValid.label"/></th>
                    <th width="150" class="center-text"><g:message code="facilityCoupon.sold.message17"/></th>
                </tr>
                </thead>

                <tbody>
                <g:if test="${archive.size() == 0}">
                    <tr>
                        <td colspan="5"><em><g:message code="facilityCoupon.sold.message28"/></em></td>
                    </tr>
                </g:if>
                <g:each in="${archive}" var="customerCoupon">
                    <tr>
                        <td>${customerCoupon.customer}</td>
                        <td class="center-text">
                            <span class="label label-${customerCoupon.coupon?.unlimited ? "warning" : customerCoupon.nrOfTickets > 0 ? 'success' : 'important'}">
                                ${customerCoupon.coupon?.unlimited ? "-" : customerCoupon.nrOfTickets}
                            </span>
                        </td>
                        <td class="center-text">
                            <g:if test="${customerCoupon.note}">
                                <span class="label label-info" rel="tooltip" title="${g.toRichHTML(text: customerCoupon.note)}">
                                    ...
                                </span>
                            </g:if>
                        </td>
                        <td class="center-text">${customerCoupon.expireDate ? customerCoupon.expireDate.toString("${message(code: "date.format.dateOnly")}"): ""}</td>
                        <td class="center-text"><g:formatDate date="${customerCoupon.dateCreated}" formatName="date.format"/></td>
                    </tr>
                </g:each>

                </tbody>
            </table>
        </div>
    </div>
</div>


<g:javascript>
    var $search = $('#customerSearch');

    $(document).ready(function () {
        $("[rel=tooltip]").tooltip();
        $(".rowlink").on("click", function() {
            var id = $(this).attr("id");
            var $details = $("#" + id + "_details");

            $details.toggle();
        });
    });
</g:javascript>

</body>
</html>
