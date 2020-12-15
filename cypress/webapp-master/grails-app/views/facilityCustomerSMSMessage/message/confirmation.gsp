<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerMessage.message.createMessage"/></title>
</head>
<body>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerSMSMessage.message.wizard.step1'), message(code: 'default.completed.message')], current: 1]"/>

<h3 id="statusLabel"><g:message code="facilityCustomerSMSMessage.message.confirmation.message2"/></h3>



<p id="stat" class="lead" style="display: none">
    <g:message code="facilityCustomerSMSMessage.message.confirmation.message3"/>
</p>



<g:if test="${failed.size() > 0}">
    <div class="alert alert-warning" role="alert">
        <a class="close" data-dismiss="alert" href="#">×</a>
        <g:message code="facilityCustomerSMSMessage.message.createMessage.cantRecieve" args="[failed?.size()]"/>
        <br>

    </div>
</g:if>

<div id="textMessageStatuses"></div>

<div class="form-actions">
    <div class="btn-toolbar pull-right">
        <g:link id="btnContinue" url="${returnUrl}" class="btn btn-info">${message(code: 'facilityCustomerSMSMessage.message.confirmation.message5', args: [originTitle])}</g:link>
    </div>
</div>
<r:script>

    var updater

    $(document).ready(function () {
        updateProgress();
    });

    function updateProgress() {

$('#btnContinue').dis

        var jqxhr = $.ajax( '<g:createLink controller="facilityCustomerSMSMessage" action="status"/>' )
                .done(function(text, textStatus, xhr) {
                    $("#textMessageStatuses").html(text) ;

                    if(xhr.status == 200) {
                        updater = setTimeout(updateProgress, 1000)
                    } else {
                        $("#statusLabel").html("${message(code: 'facilityCustomerSMSMessage.message.confirmation.message6')}")
                    }
                })

                .fail(function() {
                    $("#textMessageStatuses").html("${message(code: 'facilityCustomerSMSMessage.message.confirmation.message7')}") ;
                });


    }
</r:script>
</body>
</html>
