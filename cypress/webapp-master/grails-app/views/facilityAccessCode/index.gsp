<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityAccessCode.label.plural"/></title>
    <r:require modules="jquery-timepicker, datejs, jquery-multiselect-widget"/>
    <r:style>
        .ui-multiselect-menu {
            width: auto !important;
        }
    </r:style>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilityAdministration" action="index"><g:message code="facilityAccessCode.index.message2"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityAccessCode.label.plural"/></li>
</ul>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityAdministration" action="index"><g:message code="facility.label2"/></g:link></li>
    <li>
        <g:link controller="facilityAdministration" action="settings"><g:message code="adminFacility.adminFacilityMenu.settings"/></g:link>
    </li>
    <!--
    <li>
        <g:link controller="facilityMessage" action="index"><g:message code="facilityAccessCode.index.message11"/></g:link>
    </li>
    -->
    <g:if test="${activeFacility?.hasMPC()}">
        <li>
            <g:link controller="facilityControlSystems" action="index"><g:message code="facilityControlSystems.label"/></g:link>
        </li>
    </g:if>
    <li class="active">
        <g:link controller="facilityAccessCode" action="index"><g:message code="facilityAccessCode.label.plural"/></g:link>
    </li>
</ul>

<g:hasErrors bean="${cmd}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#">×</a>

        <h4 class="alert-heading">
            <g:message code="facilityAccessCode.index.message7"/>
        </h4>

    </div>
</g:hasErrors>

<div id="save-notification" class="alert alert-info hidden">
    <a class="close" data-dismiss="alert" href="#">×</a>

    <h4 class="alert-heading">
        <g:message code="facilityAccessCode.index.message8"/>
    </h4>

</div>

<div class="action-bar">
    <div class="btn-toolbar-left">
        <div class="btn-group">
            <button class="btn btn-inverse dropdown-toggle" data-toggle="dropdown">
                <g:message code="button.actions.label"/>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li><a href="javascript:void(0)" onclick="submitFormTo('#codes', '${createLink(controller: 'facilityAccessCode', action: 'export')}')"><g:message code="button.export.label"/></a></li>
                <li><a href="javascript:void(0)" onclick="if (confirm('${message(code: 'button.delete.confirm.message')}')) {submitFormTo('#codes', '${createLink(controller: 'facilityAccessCode', action: 'delete')}')}"><g:message code="button.delete.label"/></a></li>
            </ul>
        </div>
    </div>
    <div class="btn-toolbar-right">
        <g:link controller="facilityAccessCodeImport" action="import" class="btn btn-inverse">
            <span><g:message code="facilityAccessCodeImport.import.title"/></span>
        </g:link>
        <g:link action="deleteUsed" class="btn btn-inverse" onclick="return confirm('${message(code: 'button.delete.confirm.message')}')">
            <span><g:message code="facilityAccessCode.index.deleteUsedCodes"/></span>
        </g:link>
    </div>
</div>
<g:form name="codes" action="update">
    <table id="access-code-table" class="table table-striped table-bordered table-hover">
        <thead>
        <tr height="34">
            <th width="20" class="center-text"><g:checkBox name="checkall-accesscodes" class="checkall-accesscodes" checked="false"/></th>
            <g:sortableColumn property="active" params="${params}" titleKey="facilityAccessCode.index.message12"/>
            <g:sortableColumn property="active" params="${params}" titleKey="default.court.label" class="court-cell"/>
            <g:sortableColumn property="active" params="${params}" titleKey="default.from.label" width="220" />
            <g:sortableColumn property="active" params="${params}" titleKey="facilityAccessCode.index.message14" width="220" />
            <th class="center-text"></th>
        </tr>
        </thead>
        <tbody>

        <g:each in="${cmd.codes}" status="index" var="code">
            <tr class="accessCodeRow <g:hasErrors bean="${code}">error</g:hasErrors>">
                <td class="center-text">
                    <g:checkBox name="ids" value="${code.id}" checked="false"/>
                </td>
                <td><input type="text" name="codes[${index}].code" style="margin-bottom: 0" value="${code?.code}"
                                                                                                        class="span2 ${hasErrors(bean:code, field:'code', 'error')}"></td>
                <td class="court-cell">
                    <select class="court-picker" name="codes[${index}].courts" multiple="true" size="1">
                        <g:each in="${courts}">
                            <option value="${it.id}" ${(code.courts?.contains(it.id) ? "selected" : "")}>${it.name}</option>
                        </g:each>
                    </select>
                </td>
                <td>
                    <input type="text" name="codes[${index}].validFromDate" style="margin-bottom: 0"
                           class="validFromDate span2 ${hasErrors(bean:code, field:'validFromDate', 'error')}" value="${code?.validFromDate?.toString("yyyy-MM-dd")}"/>

                    <input type="text" name="codes[${index}].validFromTime" style="margin-bottom: 0"
                           class="validFromTime span1 ${hasErrors(bean:code, field:'validFromTime', 'error')}" value="${code?.validFromTime?.toString("HH:mm")}"/>
                </td>
                <td>
                    <input type="text" name="codes[${index}].validToDate" style="margin-bottom: 0"
                           class="span2 validToDate ${hasErrors(bean:code, field:'validToDate', 'error')}" value="${code?.validToDate?.toString("yyyy-MM-dd")}"/>

                    <input type="text" name="codes[${index}].validToTime" style="margin-bottom: 0"
                           class="span1 validToTime ${hasErrors(bean:code, field:'validToTime', 'error')}" value="${code?.validToTime?.toString("HH:mm")}"/>
                </td>
                <td>
                    <div class="span1 nowrap" style="padding-top: 5px;margin-left:5px">
                        <a href="javascript:void(0)"  class="remove-row"><i class="icon-remove"></i> <g:message code="button.delete.label"/></a>
                    </div>
                </td>
            </tr>
        </g:each>


        </tbody>
        <tfoot>
        <tr>
            <td colspan="5"><div class="span6">
                <a href="javascript:void(0)" class="add-row btn"><i class="icon-plus"></i> <g:message code="facilityAccessCode.index.message16"/></a>
                <span class="help-inline"><g:message code="facilityAccessCode.index.message9"/></span>
            </div></td>
        </tr>
        </tfoot>
    </table>

    <div class="form-actions">
        <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
        <g:if test="${params.id}">
            <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
        </g:if>
        <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
    </div>
</g:form>

<r:script>
    var courtPickerOptions = {
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'facilityAccessCode.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'facilityAccessCode.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityAccessCode.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'facilityAccessCode.multiselect.selectedText')}"
    };

    $(document).ready(function() {
        $(".court-picker").multiselect(courtPickerOptions);

        $('.checkall-accesscodes').click(function () {
            $("input[name=ids]").attr('checked', this.checked);
        });

        $("#access-code-table").on("focus",
                "input.validToDate:not(.hasDatepicker), input.validFromDate:not(.hasDatepicker)",
                function() {
            $(this).datepicker({
                autoSize: true,
                dateFormat: 'yy-mm-dd',
                minDate: new Date()
            });
        }).on("focus",
                "input.validFromTime:not(.hasTimepicker), input.validToTime:not(.hasTimepicker)",
                function() {
            $(this).addTimePicker({
                hourText: '${message(code: 'default.timepicker.hour')}',
                minuteText: '${message(code: 'default.timepicker.minute')}'
            });
        });

        $("#access-code-table").on('click', '.remove-row', function(){
            $(this).closest(".accessCodeRow").remove();
            $("#save-notification").show();
        });

        $(".add-row").click(function() {
            if($(".accessCodeRow").length > 0) {
                addCodes();
                $("#save-notification").show();
            } else {
                window.location.href = '<g:createLink action="index" params="[add:true]"/>'
            }

        });
    });

    var selectedRows = function() {
        $.fn.reverse = [].reverse;
        return $("input[name=ids]:checked").parents(".accessCodeRow").reverse();
    };


    var addCodes = function() {

        var rows = selectedRows();

        if(rows.length > 0) {

            rows.each(function() {
                addCode(getInterval($(this)));
            });

        } else {
            addCode();
        }

    };

    var addCode = function(period) {
        var numrows = $(".accessCodeRow").length;

        console.log("There are " + numrows + " rows");

        var $tr    = $('.accessCodeRow:last');
        var $clone = $tr.clone(false);

        if(!period) {
            period = getInterval($tr);
        }

        $clone.find(':text').val('');

        $clone.find(':text').each(function() {
            $(this).attr("id", unique());
        });

        $clone.find('input:checkbox').attr("checked", false).val("0");
        $clone.find('input:text').each(function() {
            var currentname = $(this).attr("name");

            var prefix = currentname.substr(currentname.indexOf("."), currentname.length -1);
            $(this).attr("name", "codes["+numrows+"]" + prefix);

        });

        $clone.find('.validFromDate, .validToDate').removeClass("hasDatepicker");

        $clone.find("button.ui-multiselect").remove();
        $clone.find(".court-picker").attr("name", "codes["+numrows+"].courts");
        $clone.find(".court-picker").multiselect(courtPickerOptions);

        guessNextInterval($tr, $clone, period);

        $tr.after($clone);

    };

    var guessNextInterval = function(source, dest, period) {
        var nextFromDate = getToDate(source);

        if(nextFromDate) {
            dest.find(".validFromDate").val(nextFromDate.toString("yyyy-MM-dd"));
            dest.find(".validFromTime").val(nextFromDate.toString("HH:mm"));


            if(period) {
                var nextToDate = nextFromDate.add({milliseconds: period});

                dest.find(".validToDate").val(nextToDate.toString("yyyy-MM-dd"));
                dest.find(".validToTime").val(nextToDate.toString("HH:mm"));
            }

        }

    };

    var parseInterval = function(source) {

        var value = source.find(".validToDate").val();

        if(value && value != '') {
            var date = new Date.parse(source.find(".validToDate").val());
        }


        return date
    };

    var getInterval = function(row) {
        var fromDate = getFromDate(row);
        var toDate   = getToDate(row);

        if(fromDate && toDate) {
            return toDate - fromDate
        }

        return null;
    };

    var getToDate = function(row) {
        return getRowDate(row, "To");
    };

    var getFromDate = function(row) {
        return getRowDate(row, "From");
    };

    var getRowDate = function(row, name) {

        var toDate = row.find(".valid" + name + "Date" ).val();
        var toTime = row.find(".valid" + name + "Time").val();

        if(toDate && toTime && toDate != '' && toTime != '') {
            return Date.parse(toDate + " " + toTime);
        }
        return null;
    };

    var unique = function() {
        var text = "";
        var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for( var i=0; i < 5; i++ )
            text += possible.charAt(Math.floor(Math.random() * possible.length));

        return text;
    };

</r:script>
</body>
</html>
