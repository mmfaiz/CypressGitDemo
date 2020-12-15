<%@ page import="grails.converters.JSON; org.joda.time.LocalDate; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="invoice.label.plural"/></title>
    <r:require modules="jquery-timepicker, matchi-invoice" />
</head>
<body>

<g:errorMessage bean="${rowsCmd}"/>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="invoice.label.plural"/></g:link><span class="divider">/</span></li>
    <li class="active">${customer.fullName()} (${cmd.invoiceDate})</li>
</ul>


<g:form action="save" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${cmd?.id}" />
    <g:hiddenField name="invoiceIds" value="${cmd?.id}" />
    <g:hiddenField name="returnUrl" value="${params?.returnUrl}" />

    <div class="form-header">
        <g:message code="facilityInvoice.edit.message3"/>
    </div>
    <fieldset>
        <g:if test="${!isEditable}">
            <div class="alert  alert-info">
                <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>
                <strong><g:message code="facilityInvoice.edit.message4"/></strong> <g:message code="facilityInvoice.edit.message22"/>
            </div>
        </g:if>

        <div class="control-group">
            <div class="controls">
                <g:customerInvoiceAddress customer="${invoice.customer}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="expirationDate"><g:message code="default.status.label"/></label>

            <div class="controls">
                <span class="badge ${invoice.status.badgeClass}" title="<g:message code="invoice.status.${invoice.status}"/>">
                    <g:message code="invoice.status.${invoice.status}"/>
                </span>
                <g:if test="${invoice.status == com.matchi.invoice.Invoice.InvoiceStatus.CREDITED}">
                    <i class="fa fa-refresh" rel="tooltip" title="${message(code: 'payment.status.CREDITED')}"></i>
                </g:if>
                <g:if test="${invoice.paidDate}">
                    (<g:formatDate date="${invoice.paidDate.toDate()}" formatName="date.format.dateOnly"/>)
                </g:if>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="expirationDate"><g:message code="invoice.invoiceDate.label"/></label>

            <div class="controls">
                <g:textField class="span2 right-margin10" id="showInvoiceDate" dateFormat1="${message(code: 'date.format.dateOnly.small')}" dateFormat2="${message(code: 'date.format.dateOnly')}"
                             name="showInvoiceDate" value="${cmd?.invoiceDate?.toString("${message(code: 'date.format.dateOnly')}")}" disabled="${!isEditable}"/>
                <g:hiddenField id="invoiceDate" name="invoiceDate" value="${cmd?.invoiceDate}"/>
                <g:message code="default.terms.label"/>
                <g:textField class="span1" name="expirationDays" id="expirationDays"
                             value="${cmd.getExpirationDays()}" disabled="${!isEditable}"/> <g:message code="facilityInvoice.edit.message30"/>
                <span class="help-inline"><g:message code="invoice.expirationDate.label"/>: <span id="expirationDate"><g:formatDate date="${invoice.expirationDate.toDate()}" formatName="date.format.dateOnly"/></span></span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="text"><g:message code="facilityInvoice.edit.message7"/></label>
            <div class="controls">
                <g:textArea rows="2" cols="30" maxlength="${com.matchi.InvoiceService.TEXT_MAX_INPUT_SIZE}" name="text"
                            value="${cmd?.text}" class="span8" disabled="${!isEditable}"/>
                <div id="inputCounter">
                    <label><span id="inputCounterCurrent"></span> / <span id="inputCounterMax"></span></label>
                </div>
            </div>
        </div>
    </fieldset>

    <hr>
    <fieldset>

        <div class="control-group">
            <label class="control-label" for="text"><g:message code="facilityInvoice.edit.message8"/></label>
            <div class="controls">
                <div class="row">
                    <div class="span3 ${customer?.facility?.hasExternalArticles() ? 'right-margin15' : ''}">
                        <label><g:message code="default.description.label"/></label>
                    </div>
                    <div class="span1">
                        <label><g:message code="default.quantity.label"/></label>
                    </div>
                    <div class="span1">
                        <label><g:message code="default.price.label"/></label>
                    </div>
                    <div class="span1">
                        <label><g:message code="default.discount.label"/></label>
                    </div>
                    <div class="span1">
                        <label><g:message code="invoiceRow.discountType.label"/></label>
                    </div>
                    <div class="span1">
                        <label><g:message code="default.vat.label"/></label>
                    </div>
                </div>

                <div id="row-template" class="row" style="padding-bottom: 5px;display: none">
                    <div class="span1 ${customer?.facility?.hasExternalArticles() ? 'nowrap right-margin15' : ''}">
                        <g:if test="${customer?.facility?.hasFortnox() || customer?.facility?.hasExternalArticles()}">
                            <g:select name="itemId" from="${items}" optionKey="id"
                                        optionValue="descr" class="span1"
                                        noSelection="[0: message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message15')]"/>
                            <g:if test="${customer?.facility?.hasExternalArticles()}">
                                <span class="left-padding5">
                                    <input type="checkbox" name="copyDetails" class="copy-details" checked
                                            title="${message(code: 'facilityInvoiceRow.createInvoiceRow.copyArticleDetails.tooltip')}"/>
                                    </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:textField name="account" class="span1" value=""
                                    placeholder="${message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message7')}"/>
                        </g:else>
                    </div>
                    <div class="span2">
                        <g:textField name="description" class="span2" value="" placeholder="${message(code: 'facilityInvoice.edit.message23')}"/>
                    </div>
                    <div class="span1">
                        <g:textField name="amount" class="span1" value=""/>
                    </div>
                    <div class="span1">
                        <g:textField name="price" class="span1" value=""/>
                    </div>
                    <div class="span1">
                        <g:textField name="discount" class="span1" value=""/>
                    </div>
                    <div class="span1">
                        <g:render template="/templates/facility/discountTypeSelect"/>
                    </div>
                    <div class="span1">
                        <g:select class="span1" name="vat"
                                  from="${vats.entrySet()}" value="" optionValue="value" optionKey="key"/>

                    </div>
                    <div class="span1">
                        <a href="javascript:void(0)" class="remove-row"><i class="icon-remove"></i> <g:message code="button.delete.label"/></a>
                    </div>
                </div>

                <g:each in="${rows.rows}" var="row" status="index">

                    <div id="row-${index}" class="row invoice-row" style="padding-bottom: 5px">
                        <g:hiddenField name="rows[${index}].rowId" value="${row.id}"/>
                        <div class="span1 ${customer?.facility?.hasExternalArticles() ? 'nowrap right-margin15' : ''}">
                            <g:if test="${customer?.facility?.hasFortnox() || customer?.facility?.hasExternalArticles()}">
                                <g:select name="rows[${index}].itemId" from="${items}" optionKey="id"
                                        optionValue="descr" value="${row.externalArticleId}"
                                        class="span1 ${hasErrors(bean: row, field: 'externalArticleId', 'error')}"
                                        noSelection="[0: message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message15')]"
                                        disabled="${!isEditable}"/>
                                <g:if test="${customer?.facility?.hasExternalArticles()}">
                                    <span class="left-padding5">
                                        <input type="checkbox" name="copyDetails" class="copy-details" checked rel="tooltip"
                                                title="${message(code: 'facilityInvoiceRow.createInvoiceRow.copyArticleDetails.tooltip')}"/>
                                    </span>
                                </g:if>
                            </g:if>
                            <g:else>
                                <g:textField name="rows[${index}].account" disabled="${!isEditable}"
                                        class="span1 ${hasErrors(bean: row, field: 'account', 'error')}"
                                        value="${row?.account}" placeholder="${message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message7')}"/>
                            </g:else>
                        </div>
                        <div class="span2">
                            <g:textField name="rows[${index}].description" disabled="${!isEditable}"
                                         class="span2 ${hasErrors(bean: row, field: 'description', 'error')}" value="${row?.description}" placeholder="${message(code: 'facilityInvoice.edit.message23')}"/>
                        </div>
                        <div class="span1">
                            <g:textField name="rows[${index}].amount" disabled="${!isEditable}"
                                         class="span1 ${hasErrors(bean: row, field: 'amount', 'error')}" value="${row?.amount}"/>
                        </div>
                        <div class="span1">
                            <g:textField name="rows[${index}].price" disabled="${!isEditable}"
                                         class="span1 ${hasErrors(bean: row, field: 'price', 'error')}"
                                         value="${fieldValue(bean: row, field: 'price')}"/>
                        </div>
                        <div class="span1">
                            <g:textField name="rows[${index}].discount" disabled="${!isEditable}"
                                         class="span1 ${hasErrors(bean: row, field: 'discount', 'error')}"
                                         value="${fieldValue(bean: row, field: 'discount')}"/>
                        </div>
                        <div class="span1">
                            <g:render template="/templates/facility/discountTypeSelect"
                                    model='[rowObj: row, selectName: "rows[${index}].discountType"]'/>
                        </div>
                        <div class="span1">
                            <g:select class="span1  ${hasErrors(bean: row, field: 'vat', 'error')}"
                                      name="rows[${index}].vat"  disabled="${!isEditable}"
                                      from="${vats.entrySet()}"
                                      value="${row?.vat}" optionValue="value" optionKey="key"/>
                        </div>
                        <g:if test="${isEditable}">
                            <div class="span1">
                                <a href="javascript:void(0)" class="remove-row"><i class="icon-remove"></i> <g:message code="button.delete.label"/></a>
                            </div>
                        </g:if>
                    </div>

                </g:each>

                <g:if test="${isEditable}">
                    <div class="row add-row-button">
                        <div class="span4">
                            <a href="javascript:void(0)" class="btn add-row"><i class="icon-plus"></i> <g:message code="facilityInvoice.edit.message25"/></a>
                        </div>
                    </div>
                </g:if>

            </div>
        </div>
    </fieldset>

    <g:if test="${!fortnoxInvoice}">
        <hr>
        <fieldset>

            <div class="control-group">
                <label class="control-label" for="text"><g:message code="payment.label.plural"/></label>
                <div class="controls">
                    <g:textField class="span2 right-margin10" id="showPaymentDate"
                                 name="showPaymentDate" value="${new LocalDate().toString("${message(code: 'date.format.dateOnly')}")}"/>
                    <g:hiddenField id="paymentDate" name="paymentDate" value="${new LocalDate().toString('yyyy-MM-dd')}"/>
                    <g:message code="invoicePayment.amount.label"/> (<g:currentFacilityCurrency facility="${invoice?.customer?.facility}"/>)
                    <g:textField class="span2 right-margin10" id="paymentAmount"
                                 name="paymentAmount" value="${formatNumber(number: invoice.getTotalAmountPaymentRemaining(), type: 'number')}"/>

                    <g:actionSubmit value="${message(code: 'facilityInvoice.edit.message26')}" class="btn" action="payment"/>
                </div>
            </div>

            <g:if test="${invoice.invoicePayments.size() > 0}">
                <div class="control-group">
                    <label class="control-label" for="text"><g:message code="facilityInvoice.edit.message15"/></label>
                    <div class="controls">
                        <table class="table table-condensed" style="width: 70%">
                            <tr>
                                <th><g:message code="default.date.label"/></th>
                                <th><g:message code="invoicePayment.amount.label"/></th>
                                <th></th>
                            </tr>
                            <g:each in="${invoice.invoicePayments}">
                                <tr>
                                    <td>${it.paymentDate.format(g.message(code:'date.format.dateOnly'))}</td>
                                    <td><g:formatMoneyShort value="${it.amount}"/></td>
                                    <td align="right">
                                        <g:link action="removePayment" params="[id: it.id]"><i class="icon-remove"></i></g:link>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </g:if>

        </fieldset>
    </g:if>

    <div class="form-actions">
        <g:if test="${isEditable}">
            <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>

            <g:actionSubmit onclick="return confirm('${message(code: 'facilityInvoice.edit.message27')}')"
                            action="remove" name="btnSumbit" value="${message(code: 'facilityInvoice.index.remove')}" class="btn btn-inverse"/>
            <g:if test="${params.returnUrl}">
                <g:link url="${params.returnUrl}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </g:if>
            <g:else>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </g:else>
        </g:if>
        <g:else>
            <g:if test="${!customer?.facility?.hasFortnox()}">
                <g:actionSubmit action="print" value="${message(code: 'button.print.label')}"
                        class="btn btn-inverse"/>
            </g:if>
            <g:if test="${params.returnUrl}">
                <g:link url="${params.returnUrl}" class="btn btn-danger"><g:message code="button.back.label"/></g:link>
            </g:if>
            <g:else>
                <g:link action="index" class="btn btn-danger"><g:message code="button.back.label"/></g:link>
            </g:else>
        </g:else>
    </div>

</g:form>

<r:script>
    var items = {}
    <g:each in="${items}" var="item">
    items['${g.forJavaScript(data: item.id)}'] = ${g.forJavaScript(json: item)}
    </g:each>

    function copyDetailsAllowed(selectEl) {
        <g:if test="${customer?.facility?.hasExternalArticles()}">
            return selectEl.parent().find(".copy-details").is(":checked");
        </g:if>
        <g:else>
            return true;
        </g:else>
    }

    $(document).ready(function() {
        $(".add-row").click(function() {
            var newRow = $('#row-template').clone(true);
            var numRows = $(".invoice-row").size();

            newRow.insertBefore('.add-row-button')
                    .attr('id', 'row-' + numRows)
                    .attr('display','block')
                    .addClass("invoice-row").show();
            newRow.find("input,select").each(function() {
                var name = $(this).attr("name");
                $(this).attr("name", "rows["+numRows+"]."+name);
            });
            newRow.find('.copy-details').tooltip();

            return false;
        });

        $(".remove-row").click(function() {
            $(this).parents(".invoice-row").remove()
        });

        $("#showPaymentDate").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}',
            altField: '#paymentDate',
            altFormat: 'yy-mm-dd'
        });

        $('[rel=tooltip]').tooltip();

        $("select[id$='itemId']").on("change", function() {
            if ($(this).val() && copyDetailsAllowed($(this))) {
                var item = items[$(this).val()];
                var prefix = $(this).attr("name").split(".")[0];
                $("[name='" + prefix + ".description']").val(item.descr);
                $("[name='" + prefix + ".price']").val(item.firstPrice);
                $("[name='" + prefix + ".vat']").val(item.VAT || 0);
            }
        });

        var $text = $("#text");
        var textLimit = ${com.matchi.InvoiceService.TEXT_MAX_INPUT_SIZE};

        function textOnChange() {
            var text = $text.val();
            $("#inputCounterCurrent").html(text.length);
            $("#inputCounterMax").html(textLimit);
        }

        $text.on("keyup input", function() {
            textOnChange();
        });

        textOnChange();
    });
</r:script>

</body>
</html>
