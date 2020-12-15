<%@ page import="com.matchi.NotificationService; org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html>
    <head>
        <meta name="layout" content="facilityLayout"/>
        <title><g:message code="facilityInvoice.send.confirm.title"/></title>
        <r:require modules="bootstrap-wysihtml5,zero-clipboard"/>
    </head>

    <body>
        <g:form class="form-inline">
            <g:render template="/templates/wizard"
                    model="[steps: [message(code: 'facilityInvoice.send.wizard.step1')], current: 0]"/>

            <g:if test="${error}">
                <div class="alert alert-error">
                    <a class="close" data-dismiss="alert" href="#">×</a>
                    <h4 class="alert-heading">
                        ${new LocalTime().toString("HH:mm:ss")}:  <g:message code="default.error.heading"/>
                    </h4>
                    ${error}
                </div>
            </g:if>

            <g:if test="${customersWithoutEmail}">
                <div class="alert alert-error">
                    <a class="close" data-dismiss="alert" href="#">×</a>
                    <div class="accordion" id="customersWithoutEmail">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="collapse" data-parent="#customersWithoutEmail" href="#customersWithoutEmailTable">
                                    <g:message code="facilityInvoice.send.confirm.customersWithoutEmail" args="[customersWithoutEmail.size()]"/>
                                </a>
                            </div>
                            <div id="customersWithoutEmailTable" class="accordion-body collapse">
                                <div class="accordion-inner">
                                    <table class="table table-transparent">
                                        <thead>
                                        <tr>
                                            <th width="60"><g:message code="facilityInvoice.send.confirm.customer.number"/></th>
                                            <th width="200"><g:message code="default.name.label"/></th>
                                        </tr>   
                                        </thead>
                                        <g:each in="${customersWithoutEmail}" var="customer">
                                            <tr>
                                                <td>${customer?.number}</td>
                                                <td>${customer?.fullName()}</td>
                                            </tr>
                                        </g:each>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <h1><g:message code="facilityInvoice.send.confirm.heading"/></h1>

            <p class="lead no-bottom-margin">
                <g:message code="facilityInvoice.send.confirm.description"/>
            </p>

            <div class="well">
                <div class="row">
                    <div class="span4">
                        <fieldset>
                            <div class="control-group">
                                <label class="control-label" for="fromMail"><g:message code="facilityInvoice.send.confirm.emailFrom"/></label>
                                <g:textField name="fromMail" value="${fromMail ?: facility?.email}"/>
                            </div>
                        </fieldset>
                    </div>
                </div>

                <div class="accordion" id="accordion2">
                    <div class="accordion-group">
                        <div class="accordion-heading" style="background-color: white">
                            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows">
                                <g:message code="facilityInvoice.send.confirm.selectedReceivers" args="[customers.size()]"/>
                                <em>(<g:message code="facility.customer.invoice.email.help"/>)</em>
                            </a>
                        </div>
                        <div id="rows" class="accordion-body collapse">
                            <div class="accordion-inner">
                                <table class="table table-transparent">
                                    <thead>
                                    <tr>
                                        <th width="60"><g:message code="facilityInvoice.send.confirm.customer.number"/></th>
                                        <th width="200"><g:message code="default.name.label"/></th>
                                        <th width="200"><g:message code="facilityInvoice.send.confirm.customer.email"/></th>
                                    </tr>
                                    </thead>
                                    <g:each in="${customers}" var="customer">
                                        <tr>
                                            <td>${customer?.number}</td>
                                            <td>${customer?.fullName()?.encodeAsHTML()}</td>
                                            <td>${customer?.getCustomerInvoiceEmail()?.encodeAsHTML()}</td>
                                        </tr>
                                    </g:each>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:textArea name="emailMessage" rows="15" cols="100" class="span12" value="${emailMessage}"
                    placeholder="${message(code: 'facilityInvoice.send.confirm.message.placeholder')}"/>
            <div id="inputCounter">
                <p><span id="inputCounterCurrent"></span> / <span id="inputCounterMax"></span></p>
            </div>
            <div class="form-actions">
                <g:link event="cancel" class="btn btn-danger">
                    <g:message code="button.cancel.label"/>
                </g:link>
                <g:if test="${customers}">
                    <g:submitButton name="submit" type="submit" class="btn btn-success pull-right"
                            value="${message(code: 'button.submit.label')}"
                            data-toggle="button" show-loader="${message(code: 'default.loader.label')}"/>
                </g:if>
            </div>
        </g:form>

        <r:script>
            var $editor = $('#emailMessage');

            $editor.wysihtml5({
                "image": false,
                parserRules: wysihtml5ParserRules,
                stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
            });

            var textLimit = ${NotificationService.MAX_INPUT_SIZE};

            $('.wysihtml5-sandbox').contents().find('body').on("keyup",function() {
                textOnChange();
            });

            $('.form-inline').on("submit", function() { //Strip input from trailing space that converts to &nbsp;
                var text = $('.wysihtml5-sandbox').contents().find('body').html().replace(/&nbsp;/g, " ");
                $('.wysihtml5-sandbox').contents().find('body').html(text)
            });

            function textOnChange() {
                var text = $('.wysihtml5-sandbox').contents().find('body').html().replace(/&nbsp;/g, " ");
                $("#inputCounterCurrent").html(text.length)
                $("#inputCounterMax").html(textLimit)
            }

            textOnChange();

            $editor.focus();

            ZeroClipboard.config({
                moviePath: '//cdnjs.cloudflare.com/ajax/libs/zeroclipboard/1.3.5/ZeroClipboard.swf',
                forceHandCursor: true
            });
            var client = new ZeroClipboard( $("#copyBtn") );
        </r:script>
    </body>
</html>