<%@ page import="com.matchi.FacilityContractItem" %>
<r:require modules="matchi-selectpicker"/>

<div class="form-group col-sm-12">
    <label for="articleNumber"><g:message code="facilityContractItem.article.label"/>*</label>
    <g:select name="articleNumber"
              from="${articles}"
              value="${item.articleNumber}"
              optionValue="${{it.articleNumber + ' - ' +it.descr}}"
              optionKey="articleNumber"
              noSelection="[0: message(code: 'facilityContractItem.article.empty')]"
              class="form-control"
              required="required"
              autofocus="autofocus"/>
</div>

<div class="form-group col-sm-12 description">
    <label for="description"><g:message code="facilityContractItem.description.label"/></label>
    <g:textArea name="description" rows="3" cols="30" value="${item.description}" class="form-control"/>
</div>

<div class="form-group col-sm-12 account">
    <label for="account"><g:message code="facilityContractItem.account.label"/></label>
    <g:textField name="account" class="form-control" value="${fieldValue(bean: item, field: 'account')}"/>
</div>

<div class="form-group col-sm-12 price">
    <label for="price"><g:message code="facilityContractItem.price.label"/></label>
    <g:textField name="price" class="form-control" value="${fieldValue(bean: item, field: 'price')}" required="required"/>
</div>

<div class="form-group col-sm-12">
    <label for="type"><g:message code="facilityContractItem.type.label"/></label>
    <g:select name="type"
              from="${FacilityContractItem.RecurringType.values()}"
              value="${item.type}"
              valueMessagePrefix="facilityContractItem.type"
              class="form-control"/>
</div>

<div class="form-group col-sm-12 charge-dates ${FacilityContractItem.RecurringType.MONTHLY.name()}" style="display: none">
    <label for="chargeMonths"><g:message code="facilityContractItem.chargeMonths.label"/></label>
    <g:select name="chargeMonths"
              from="${1..12}"
              multiple="true"
              value="${item.chargeMonths}"
              valueMessagePrefix="time.month"
              title="${message(code: 'adminFacilityContracts.createItem.chargeMonths.multiselect.noneSelectedText')}"/>
</div>

<div class="form-group col-sm-12 charge-dates ${FacilityContractItem.RecurringType.YEARLY.name()}" style="display: none">
    <label for="chargeMonth"><g:message code="facilityContractItem.chargeMonth.label"/></label>
    <g:select name="chargeMonth"
              from="${1..12}"
              value="${item.chargeMonth}"
              valueMessagePrefix="time.month"
              class="form-control"/>
</div>

<div class="form-group col-sm-12 charge-dates ${FacilityContractItem.RecurringType.ONE_TIME_CHARGE.name()}" style="display: inline">
    <label for="chargeDate"><g:message code="facilityContractItem.chargeDate.label"/></label>
    <g:textField name="chargeDate"
                 value="${formatDate(date: item.chargeDate, formatName: 'date.format.dateOnly')}"
                 class="form-control"
                 readonly="true"/>
</div>

<r:script>
    $(function() {
        $("#chargeMonths").allselectpicker({
            selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
            countSelectedText: "{0} ${message(code: 'adminFacilityContracts.createItem.chargeMonths.multiselect.selectedText')}",
            selectedTextFormat: 'count'
        });

        $("#chargeDate").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });

        $("#type").change(function() {
            $(".charge-dates").hide().filter("." + $(this).val()).show();
        }).trigger("change");

        hideElements();
    });

    var hideElements = function() {
        var el = $("select[name='articleNumber'] option:selected");

        if (el.val() > 0) {
            $(".description").hide();
            $(".account").hide();
        } else {
            $(".description").show();
            $(".account").show();
        }
    }

    var selectArticle = function() {
        hideElements();

        var el = $("select[name='articleNumber'] option:selected");
        var selectedArticle = articles[el.val()];

        if (el.val() > 0) {
            $("[name='description']").val(selectedArticle.descr);
            $("[name='price']").val(selectedArticle.firstPrice);
        } else {
            $("[name='price']").val("");
        }
    }

    $("select[name='articleNumber']").change(function(e) {
        selectArticle();
    });

    var articles = {}
    <g:each in="${articles}" var="article">
        articles['${g.forJavaScript(data: article.articleNumber)}'] = ${g.forJavaScript(json: article)}
    </g:each>
</r:script>