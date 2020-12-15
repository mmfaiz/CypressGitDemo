<%@ page import="com.matchi.orders.Order" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityBooking.extraInfo.label" /></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <div class="well">
        <div class="container-fluid">
            <div class="row">
                <h4><g:message code="facilityBooking.extraInfo.general.label" /></h4>
            </div>
            <div class="row">
                <div><g:message code="facilityBooking.extraInfo.general.bookingId" /> <strong>${booking.id}</strong></div>
            </div>
            <div class="row">
                <div><g:message code="facilityBooking.extraInfo.general.createdAt" args="[booking.dateCreated.format('YYYY-MM-dd HH:mm')]" /></div>
            </div>
            <div class="row">
                <div><g:message code="facilityBooking.extraInfo.general.bookedBy.${booking.order.origin}" args="[booking.order.issuer.fullName()]" /></div>
            </div>
        </div>
    </div>
    <g:if test="${mpcInfo.size() > 0}">
        <div class="well">
            <div class="container-fluid">
                <div class="row">
                    <h4 class="center"><g:message code="facilityBooking.extraInfo.mpc.label" /></h4>
                </div>
                <div class="row">
                    <div class="col-sm-5 pull-left"><i class="fa fa-calendar"></i> <g:message code="facilityBooking.extraInfo.mpc.dateAndCourt" args="[mpcInfo.bookingStart.format('YYYY-MM-dd'), mpcInfo.court?.name]" /> </div>
                </div>
                <div class="row">
                    <div class="col-sm-5 pull-left"><i class="fa fa-lightbulb-o"></i> <g:message code="facilityBooking.extraInfo.mpc.timeFromTo" args="[mpcInfo.bookingStart.format('HH:mm'), mpcInfo.bookingEnd.format('HH:mm')]" /> </div>
                </div>
                <div class="row">
                    <div class="col-sm-5 pull-left"><i class="fas fa-key"></i> <g:message code="facilityBooking.extraInfo.mpc.codeOnOff" args="[mpcInfo.codeValidStart.format('HH:mm'), mpcInfo.codeValidEnd.format('HH:mm')]" /> </div>
                </div>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <g:if test="${mpcInfo.status}">
                        <div class="row">
                            <div class="col-sm-5 pull-left"> <g:message code="facilityBooking.extraInfo.mpc.${mpcInfo.status}"/> </div>
                        </div>
                    </g:if>
                </sec:ifAnyGranted>
            </div>
        </div>
    </g:if>
</div>
<div class="modal-footer">
    <div class="pull left">
        <button type="button" class="btn btn-md btn-danger" data-dismiss="modal"><g:message code="button.close.label" /></button>
    </div>
    <div class="pull right">
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <g:if test="${!booking.order.origin.equals(com.matchi.orders.Order.ORIGIN_FACILITY) && !booking.isSubscription()}">
                <g:link controller="adminOrder" action="index" params="[q: booking.order.id, start: booking.order.dateCreated.format('YYYY-MM-dd'), end: booking.order.dateCreated.plus(1).format('YYYY-MM-dd')]" class="btn btn-md btn-primary" target="_blank" >
                    <g:message code="facilityBooking.extraInfo.seeOrderInfo" />
                </g:link>
            </g:if>
        </sec:ifAnyGranted>
    </div>
</div>

<script type="text/javascript">
</script>