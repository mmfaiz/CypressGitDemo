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
              model="[steps: [message(code: 'facilityCustomerMessage.message.wizard.step1'), message(code: 'default.completed.message')], current: 0]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>

    <h1><g:message code="facilityCustomerMessage.message.createMessage.message2"/></h1>
    <p class="lead no-bottom-margin">
        <g:message code="facilityCustomerMessage.message.createMessage.message14"/>

    </p>
    <g:if test="${customerInfoAll?.size() < 50}">
        <p>

            <br>
            <a id="emailClientLink" class="btn btn-info" href="mailto:${user?.email}?bcc=${params.boolean('includeGuardian') ? receiveStringAll : receiveString}">
                <g:message code="facilityCustomerMessage.message.createMessage.message5"/>
            </a>
            <br>
            <small class="help-block"><g:message code="facilityCustomerMessage.message.createMessage.message4"/></small>
        </p>
    </g:if>
    <g:else>
        <p>
            <small><g:message code="facilityCustomerMessage.message.createMessage.message6"/></small><br>
            <a class="btn btn-info" href="javascript:void(0)" onclick="$('#recipients').toggle();">
                <g:message code="facilityCustomerMessage.message.createMessage.message7"/>
            </a>
        </p>
        <g:textArea id="recipients" name="recip" rows="6" cols="5" class="span12" style="display: none;"
                value="${params.boolean('includeGuardian') ? receiveStringAll : receiveString}"/>

        <p>
            <small><g:message code="facilityCustomerMessage.message.createMessage.message8"/></small><br>
            <a class="btn btn-info" href="mailto:${user?.email}"><g:message code="facilityCustomerMessage.message.createMessage.message9"/></a>
        </p>
    </g:else>
    <div class="well">
        <div class="row">
            <div class="span4">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="fromMail"><g:message code="facilityCustomerMessage.message.createMessage.message10"/></label>
                        <select id="fromMail" name="fromMail">
                            <g:if test="${facility?.email}">
                                <option value="${facility?.email}">${facility?.email}</option>
                            </g:if>
                            <option value="${user?.email}">${user?.email}</option>
                        </select>
                    </div>
                </fieldset>
            </div>
            <div class="span4">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="uniqueEmail">
                            <g:message code="facilityCustomerMessage.message.createMessage.uniqueEmail"/>
                        </label>
                        <g:select name="uniqueEmail" from="[false, true]" class="input-medium"
                                valueMessagePrefix="facilityCustomerMessage.message.createMessage.uniqueEmail"/>
                    </div>
                </fieldset>
            </div>
            <div class="span3">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label top-margin10">
                            <g:message code="facility.customer.message.includeGuardian"/>
                        </label>
                        <g:radioGroup name="includeGuardian" values="[false, true]"
                                      value="${params.includeGuardian ?: 'false'}"
                                      labels="[message(code: 'default.no.label'),
                                               message(code: 'default.yes.label')]">
                            <label class="radio inline">
                                ${it.radio} ${it.label}
                            </label>
                        </g:radioGroup>
                </fieldset>
            </div>
        </div>

        <div class="accordion" id="accordion2">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a id="recipientsCollapseTitle" class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows">
                        <g:message code="facilityCustomerMessage.message.createMessage.selectedRecipients" args="[customerInfo?.size() ?: 0, nMails]"/>
                    </a>
                </div>
                <div id="rows" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <table class="table table-transparent">
                            <thead>
                            <tr>
                                <th width="60"><g:message code="facilityCustomerMessage.message.createMessage.message11"/></th>
                                <th width="200"><g:message code="default.name.label"/></th>
                                <th width="200"><g:message code="facilityCustomerMessage.message.createMessage.message13"/></th>
                            </tr>
                            </thead>
                            <tbody id="recipientsCollapseBody">
                                <g:each in="${customerInfo}" var="customer">
                                    <tr>
                                        <td>${customer?.number}</td>
                                        <td>${customer?.name}</td>
                                        <td>${customer?.email}</td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <g:if test="${cantReceiveCustomerInfo?.size() > 0}">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a class="accordion-toggle text-warning" data-toggle="collapse" data-parent="#accordion2" href="#rows1">
                        <g:message code="facilityCustomerMessage.message.createMessage.cantReceive" args="[cantReceiveCustomerInfo?.size()]"/>
                    </a>
                </div>
                <div id="rows1" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <table class="table table-transparent">
                            <thead>
                            <tr>
                                <th width="60"><g:message code="facilityCustomerMessage.message.createMessage.message11"/></th>
                                <th width="200"><g:message code="default.name.label"/></th>
                                <th width="200"><g:message code="facilityCustomerMessage.message.createMessage.message13"/></th>
                            </tr>
                            </thead>
                            <g:each in="${cantReceiveCustomerInfo}">
                                <tr>
                                    <td>${it?.number}</td>
                                    <td>${it?.name}</td>
                                    <td>${it?.email}</td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>
        </g:if>
    </div>
    <div class="control-group">
        <label for="subject"><strong>${message(code: 'facilityCustomerMessage.message.createMessage.subject.label')}</strong></label>
        <g:textField name="subject" class="span12" placeholder="${message(code: 'facilityCustomerMessage.message.createMessage.subject.placeholder')}"
        value="${message(code: "templates.emails.messageFromClub")}"/>
    </div>
    <g:textArea id="message" name="message" rows="15" cols="100" class="span12" placeholder="${message(code: 'facilityCustomerMessage.message.createMessage.placeholder')}"/>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
        <g:if test="${!error}">
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'facilityCustomerMessage.message.createMessage')}" show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
        </g:if>
    </div>
</g:form>
<r:script>
    var $message = $('#message');
    $message.wysihtml5({
        "image": false,
        parserRules: wysihtml5ParserRules,
        stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
    });
    $message.focus();

    ZeroClipboard.config({
        moviePath: '//cdnjs.cloudflare.com/ajax/libs/zeroclipboard/1.3.5/ZeroClipboard.swf',
        forceHandCursor: true
    });
    var client = new ZeroClipboard( $("#copyBtn") );

    $(function() {
        $(":radio[name=includeGuardian]").change(function() {
            <g:if test="${customerInfoAll?.size() >= 50}">
                $("#recipients").val($(this).val() == "true" ? "${g.forJavaScript(data: receiveStringAll)}" : "${g.forJavaScript(data: receiveString)}");
            </g:if>

            if ($(this).val() == "true") {
                $("#emailClientLink").attr("href", "mailto:${g.forJavaScript(data: user?.email)}?bcc=${g.forJavaScript(data: receiveStringAll)}");
                $("#recipientsCollapseTitle").html("<g:message code="facilityCustomerMessage.message.createMessage.selectedRecipients" args="[customerInfoAll?.size() ?: 0, nMailsAll]"/>");
                $("#recipientsCollapseBody").html("<g:each in="${customerInfoAll}" var="customer"><tr><td>${g.forJavaScript(data: customer?.number)}</td><td>${g.forJavaScript(data: customer?.name)}</td><td>${g.forJavaScript(data: customer?.email)}</td></tr></g:each>");
            } else {
                $("#emailClientLink").attr("href", "mailto:${user?.email}?bcc=${receiveString}");
                $("#recipientsCollapseTitle").html("<g:message code="facilityCustomerMessage.message.createMessage.selectedRecipients" args="[customerInfo?.size() ?: 0, nMails]"/>");
                $("#recipientsCollapseBody").html("<g:each in="${customerInfo}" var="customer"><tr><td>${g.forJavaScript(data: customer?.number)}</td><td>${g.forJavaScript(data: customer?.name)}</td><td>${g.forJavaScript(data: customer?.email)}</td></tr></g:each>");
            }
        });
    });
</r:script>
</body>
</html>