<%@ page import="com.matchi.payment.PaymentMethod; com.matchi.FacilityProperty.FacilityPropertyKey" %>
<div class="modal-content">
    <script type="text/javascript" src="${grailsApplication.config.adyen.library}"></script>
    <g:form name="confirmForm" class="no-margin">
        <g:hiddenField name="orderId" value="${order.id}"/>
        <g:hiddenField name="id" value="${membershipType.id}"/>

        <div class="modal-header flex-center separate">
            <h4 class="modal-title"><g:message code="membership.buy.confirm"/></h4>
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
        </div>

        <div class="modal-body">

            <div class="clearfix vertical-padding10">
                <g:b3StaticFlashError/>
                <g:b3StaticErrorMessage bean="${command}"/>
            </div>

            <div class="row">
                <div class="col-sm-6">
                    <h1 class="h6">
                        <span class="fa-stack fa-lg text-info">
                          <i class="fas fa-circle fa-stack-2x"></i>
                          <i class="fas fa-user fa-stack-1x fa-inverse"></i>
                        </span> ${membershipType.name}
                    </h1>
                </div>
                <div class="col-sm-6">
                    <h2 class="h6">
                        <span class="fa-stack fa-lg ">
                          <i class="fas fa-circle fa-stack-2x"></i>
                          <i class="fas fa-map-marker fa-stack-1x fa-inverse"></i>
                        </span>
                        ${membershipType.facility.name}
                    </h2>
                </div>
            </div>

            <hr>

            <div class="row bottom-margin20">
                <g:if test="${order.price}">
                    <div class="col-sm-6">
                        <p>
                            <span class="fa-stack fa-lg ">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                            </span>
                            <g:formatMoney value="${order.price}" facility="${membershipType.facility}"/>
                        </p>
                    </div>
                </g:if>
                <div class="col-sm-6">
                    <p>
                        <span class="fa-stack fa-lg ">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                        </span>
                        <g:formatDate date="${startDate}" formatName="date.format.daterangepicker.short"/>
                        -
                        <g:formatDate date="${endDate}" formatName="date.format.daterangepicker.short"/>
                    </p>
                </div>
                <g:if test="${membershipType.recurring}">
                    <div class="col-sm-12">
                        <p>
                            <span class="fa-stack fa-lg ">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fa fa-repeat fa-stack-1x fa-inverse"></i>
                            </span>
                            ${message(code: 'membershipType.paidOnRenewal.recurring.tooltip')}
                        </p>
                    </div>
                </g:if>
                <g:if test="${membershipType.description}">
                    <div class="col-sm-12">
                        <p>
                            <span class=""><i class="fas fa-info-circle"></i></span>
                            ${membershipType.description}
                        </p>
                    </div>
                </g:if>
            </div>

            <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}"/>
        </div>

        <div class="modal-footer">
            <button class="btn btn-md btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
            <g:render template="/templates/payments/submitBtnAndHandler"
                      model="[targetController: 'membershipPayment', facility: membershipType.facility]"/>
        </div>
    </g:form>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();

        $("#btnSubmit").focus();
    });

    $(".modal-body").css( "max-height", "2024px" );

    var onLoading = function() {
        $("#btnSubmit").attr("disabled", "disabled");
    };
</script>
