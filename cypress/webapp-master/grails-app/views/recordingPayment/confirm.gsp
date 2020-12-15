<%@ page import="com.matchi.payment.PaymentMethod;" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: facility]" />

<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" class="no-margin form-horizontal">
            <g:hiddenField name="bookingId" value="${recording.bookingId}"/>
            <g:hiddenField name="orderId" value="${order.id}"/>

            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="recording.buy.confirm"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>

            <div class="modal-body">
                <g:b3StaticFlashError/>
                <g:b3StaticErrorMessage bean="${command}"/>

                <div class="row">
                    <div class="col-sm-6">
                        <h1 class="h6 ellipsis">
                            <span class="fa-stack fa-lg text-warning">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-video-camera fa-stack-1x fa-inverse"></i>
                            </span>
                            <g:message code="recording.buy.title" />
                        </h1>
                    </div>
                    <div class="col-sm-6">
                        <h2 class="h6">
                            <span class="fa-stack fa-lg text-grey-light">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-map-marker fa-stack-1x fa-inverse"></i>
                            </span>
                            ${facility.name}
                        </h2>
                    </div>
                </div>

                <hr>

                <div class="row">
                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                            </span>
                            <span rel="tooltip"> <g:formatDate date="${recording?.start}" formatName="date.format.dateOnly"/></span>
                        </p>
                    </div>

                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fa fa-clock fa-stack-1x fa-inverse"></i>
                            </span>
                            <span rel="tooltip"> <g:formatDate date="${recording?.start}" formatName="date.format.timeOnly"/></span> -
                            <span rel="tooltip"> <g:formatDate date="${recording?.end}" formatName="date.format.timeOnly"/></span>
                        </p>
                    </div>

                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                          </span>
                          <span id="bookingPrice" rel="tooltip"><g:formatMoney value="${recording?.price}" facility="${facility}"/></span>
                        </p>
                    </div>
                </div>

                <hr>

                <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}" />
            </div>

            <div class="modal-footer">
                <button class="btn btn-md btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
                <g:render template="/templates/payments/submitBtnAndHandler"
                        model="[targetController: 'recordingPayment', facility: facility, targetAction: formAction]"/>
            </div>
        </g:form>
    </div>
</div>

<r:script>
    $(document).ready(function () {
        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
