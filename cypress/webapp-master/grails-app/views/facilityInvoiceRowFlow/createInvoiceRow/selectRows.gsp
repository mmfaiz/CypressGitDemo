<%@ page import="grails.converters.JSON; com.matchi.Facility; com.matchi.invoice.InvoiceRow"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message1"/></title>
    <r:require modules="select2" />
</head>
<body>

<g:render template="/facilityInvoiceRowFlow/createInvoiceRow/invoiceRowBreadcrumb"/>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message1"/></g:link></li>
</ul>

<g:render template="/templates/wizard" model="[steps: [message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step1'), message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step2'), message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step3')], current: 1]"/>


<g:form class="form-inline">

    <g:hasErrors bean="${rows}">
        <div class="alert alert-error">
            <g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message4"/>
        </div>
    </g:hasErrors>

    <h2><g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message5"/></h2>
    <p class="lead"><g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message14"/></p>

    <g:if test="${availableOrganizations}">
        <div class="control-group">
            <label class="control-label" for="organizationId"><g:message
                code="facilityInvoiceRow.createInvoiceRow.selectCustomers.message14"/></label>

            <div class="controls">
                <g:select
                    noSelection="[null: message(code: 'facilityInvoiceRow.createInvoiceRow.selectCustomers.message15')]"
                    tabIndex="1"
                    name="organizationId" from="${availableOrganizations}" optionKey="id" optionValue="name"
                    value="${organization?.id}"/>
            </div>
        </div>
    </g:if>
    <div class="well">
    <div class="row">
        <div class="span2 ${facility.hasExternalArticles() ? 'right-margin25' : ''}">
            <label>
                <g:if test="${!facility.hasFortnox() && !facility.hasExternalArticles()}"><g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message7"/></g:if>
                <g:else><g:message code="default.article.label"/></g:else>
            </label>
        </div>
        <div class="span3">
            <label><g:message code="invoiceRow.description.label"/></label>
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
        <div class="span2 ${facility.hasExternalArticles() ? 'nowrap right-margin25' : ''}">
            <g:if test="${!facility.hasFortnox() && !facility.hasExternalArticles()}"><g:textField name="account" class="span1" value="1"/></g:if>
            <g:else>
                <g:select name="itemId" from="${items}" optionValue="descr" optionKey="id"
                        noSelection="[0: message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message15')]"/>
                <g:if test="${facility.hasExternalArticles()}">
                    <span class="left-padding5">
                        <input type="checkbox" name="copyDetails" class="copy-details" checked rel="tooltip"
                                title="${message(code: 'facilityInvoiceRow.createInvoiceRow.copyArticleDetails.tooltip')}"/>
                    </span>
                </g:if>
            </g:else>
        </div>
        <div class="span3">
            <g:textField name="description" class="span3" value="" placeholder="${message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message16')}" maxlength="50"/>
        </div>
        <div class="span1">
            <g:textField name="amount" class="span1" value="1"/>
        </div>
        <div class="span1">
            <g:textField name="price" class="span1" value=""/>
        </div>
        <div class="span1">
            <g:textField name="discount" class="span1" value="0"/>
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

    <g:each in="${rows?.rows}" var="row" status="index">

        <div id="row-${index}" class="row invoice-row" style="padding-bottom: 5px">
            <div class="span2 ${facility.hasExternalArticles() ? 'nowrap right-margin25' : ''}">
                <g:if test="${!facility.hasFortnox() && !facility.hasExternalArticles()}"><g:textField name="rows[${index}].account" class="span2" value="${row?.account}"/></g:if>
                <g:else>
                    <g:select name="rows[${index}].itemId" value="${row?.itemId}"  from="${items}" optionValue="descr" optionKey="id"
                            noSelection="[0: message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message15')]"/>
                    <g:if test="${facility.hasExternalArticles()}">
                        <span class="left-padding5">
                            <input type="checkbox" name="copyDetails" class="copy-details" checked rel="tooltip"
                                    title="${message(code: 'facilityInvoiceRow.createInvoiceRow.copyArticleDetails.tooltip')}"/>
                        </span>
                    </g:if>
                </g:else>

            </div>
            <div class="span3">
                <g:textField name="rows[${index}].description" maxlength="50"
                             class="span3 ${hasErrors(bean: row, field: 'description', 'error')}" value="${row?.description}" placeholder="${message(code: 'facilityInvoiceRow.createInvoiceRow.selectRows.message16')}"/>
            </div>
            <div class="span1">
                <g:textField name="rows[${index}].amount"
                             class="span1 ${hasErrors(bean: row, field: 'amount', 'error')}" value="${row?.amount}"/>
            </div>
            <div class="span1">
                <g:textField name="rows[${index}].price"
                             class="span1 ${hasErrors(bean: row, field: 'price', 'error')}" value="${row?.price}"/>
            </div>
            <div class="span1">
                <g:textField name="rows[${index}].discount"
                             class="span1 ${hasErrors(bean: row, field: 'discount', 'error')}" value="${row?.discount}"/>
            </div>
            <div class="span1">
                <g:render template="/templates/facility/discountTypeSelect"
                        model='[rowObj: row, selectName: "rows[${index}].discountType"]'/>
            </div>
            <div class="span1">
                <g:select class="span1  ${hasErrors(bean: row, field: 'vat', 'error')}" name="rows[${index}].vat"
                          from="${vats.entrySet()}" value="${row?.vat}" optionValue="value" optionKey="key"/>
            </div>
            <div class="span1">
                <a href="javascript:void(0)" class="remove-row"><i class="icon-remove"></i> <g:message code="button.delete.label"/></a>
            </div>
        </div>

    </g:each>

    <div class="row add-row-button">
        <div class="span4">
            <a href="javascript:void(0)" class="btn add-row"><i class="icon-plus"></i> <g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message18"/></a>
        </div>
    </div>
    </div>
    <div class="alert alert-warning warning" id="descriptionMemberWarning" role="alert" style="display:none;">
        <table class="notification-content">
            <tr>
                <td class="notification-icon" width="4%">
                    <i class="fa fa-bullhorn"></i>
                </td>
                <td class="notification-message" width="90%">
                    <g:message code="facilityInvoiceRow.createInvoiceRow.selectRows.message19"/>
                </td>
            </tr>
        </table>
    </div>
    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />

        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}" />
        <g:submitButton class="btn right btn-info right-margin5" name="back" value="${message(code: 'button.back.label')}" />
    </div>

</g:form>
<r:script>
    function copyDetailsAllowed(selectEl) {
        <g:if test="${facility.hasExternalArticles()}">
            return selectEl.parent().find(".copy-details").is(":checked");
        </g:if>
        <g:else>
            return true;
        </g:else>
    }

    memberLanguageVariations = [<g:each in="${grailsApplication.config.i18n.availableLanguages}">
        '<g:message code="default.member.label" locale="${it.key}"/>',
    </g:each>];

    $(document).ready(function() {
        $("#organizationId").select2({width:'250px'}).on("change", function(e) {
                window.location.href = "<g:createLink id="addOrganizationLink" event="addOrganization"/>&organizationId=" + e.val
        });
        $(".add-row").click(function() {
            var newRow = $('#row-template').clone(true);
            var numRows = $(".invoice-row").size()

            newRow.insertBefore('.add-row-button')
                    .attr('id', 'row-' + numRows)
                    .attr('display','block')
                    .addClass("invoice-row").show();
            newRow.find("input,select").each(function() {
                var name = $(this).attr("name")

                if($(this).is("select") && name == "itemId") {
                    $(this).select2({width:'150px'}).on("change", onItemSelect);
                }
                $(this).attr("name", "rows["+numRows+"]."+name);
            });
            newRow.find('.copy-details').tooltip();

            return false;

        });

        $("input[name*='description']").on("keyup", function () {
          var errors = 0
          var input = this;
          for (var i = 0; i < memberLanguageVariations.length; i++) {
            var searchValue = memberLanguageVariations[i];
            if ($(input).val().toLowerCase().indexOf(searchValue.toLowerCase())>=0) {
              errors = 1;
            }
          }

          if (errors) {
            $("#descriptionMemberWarning").show()
          }
          else {
            $("#descriptionMemberWarning").hide()
          }
        })

        $(".remove-row").click(function() {
            $(this).parents(".invoice-row").remove()
        });

        $("select[id$='.itemId']").select2({width:'150px'}).on("change", onItemSelect);
        $("select[id$='itemId']").focus();

        $('[rel=tooltip]').tooltip();
    });

    var onItemSelect = function(e) {
        if(e.val == 0 || !copyDetailsAllowed($(e.target))) {return}
        var name = $(e.target).attr("name")
        var prefix = $(e.target).attr("name").split(".")[0]

        var item = items[e.val]
        $("[name='" + prefix + ".description']").val(item.descr)
        $("[name='" + prefix + ".price']").val(item.firstPrice)
        $("[name='" + prefix + ".vat']").val(item.VAT || 0);
    }

    var items = {}
    <g:each in="${items}" var="item">
    items['${g.forJavaScript(data: item.id)}'] = ${g.forJavaScript(json: item)}
    </g:each>

</r:script>
</body>
</html>