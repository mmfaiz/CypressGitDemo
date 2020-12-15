<%@ page import="com.matchi.coupon.CustomerCoupon; org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="default.modal.done"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>

        <div class="modal-body">
            <g:layoutBody />
        </div>

        <div class="modal-footer">
            <a href="javascript:void(0)" onclick="${onclick}" data-dismiss="modal" class="btn btn-md btn-success">
                <g:message code="button.closewindow.label" default="Stäng fönstret"/>
            </a>
        </div>
    </div>

</div>

</body>
</html>
