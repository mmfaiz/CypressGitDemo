<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityCustomerMessage.message.createMessage"/></title>
    <r:require modules="bootstrap-wysihtml5,zero-clipboard"/>
</head>
<body>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerSMSMessage.message.wizard.step1'), message(code: 'default.completed.message')], current: 0]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>
    <g:if test="${cantRecieve?.size() > 0}">
        <div class="alert alert-warning" role="alert">
            <a class="close" data-dismiss="alert" href="#">×</a>
            <g:message code="facilityCustomerSMSMessage.message.createMessage.cantRecieve" args="[cantRecieve?.size()]"/>
            <br>
        </div>
    </g:if>

    <h1><g:message code="facilityCustomerSMSMessage.message.createMessage.message3"/></h1>
    <p class="lead no-bottom-margin">
        <g:message code="facilityCustomerSMSMessage.message.createMessage.message4"/>
    </p>
    <br>

    <div class="well">


        <div class="accordion" id="accordion2">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows">
                        <g:message code="facilityCustomerMessage.message.createSmsMessage.selectedRecipients" args="[customerInfo?.size() ?: 0]"/>
                    </a>
                </div>
                <div id="rows" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <table class="table table-transparent">
                            <thead>
                            <tr>
                                <th width="60"><g:message code="facilityCustomerSMSMessage.message.createMessage.message6"/></th>
                                <th width="200"><g:message code="default.name.label"/></th>
                                <th width="200"><g:message code="customer.telephone.label"/></th>
                            </tr>
                            </thead>
                            <g:each in="${customerInfo}" var="customer">
                                <tr>
                                    <td>${customer?.number}</td>
                                    <td>${customer?.name}</td>
                                    <td>${customer?.phone}</td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>



    <g:textArea onkeyup="countChar(this)" id="message" name="message" rows="3" cols="100" class="span12" maxlength="160" placeholder="${message(code: 'facilityCustomerMessage.message.createMessage.placeholder')}"/>
    <span class="help-block"><g:message code="facilityCustomerSMSMessage.message.createMessage.message10"/></span>


    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
        <g:if test="${!error}">
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'facilityCustomerMessage.message.createMessage')}" show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
        </g:if>
    </div>
</g:form>
<script>
    function countChar(val) {
        var len = val.value.length;
        $('#numChar').text(len);
    };

    function updateProgress() {
        console.log("upd");

        $("#upd").html("hej") ;
        setTimeout(updateProgress, 1000)
    }
</script>

</body>
</html>