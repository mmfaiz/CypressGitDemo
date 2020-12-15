<%@ page import="org.joda.time.DateTime; grails.util.GrailsUtil; com.matchi.Facility; java.io.File" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li>MPC Settings</li>
    </ol>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 5]"/>

        <div class="panel-body">
            <h3>MPC Settings for ${facility.name}</h3>

            <g:if test="${!mpcSettings}">
                <g:form action="createMpc" class="well">
                    <g:hiddenField name="facilityId" value="${facility.id}"/>

                    <div class="form-group">
                        <label for="provider">Set provider</label>
                        <select id="provider" name="provider" class="form-control">
                            <g:each in="${providers}">
                                <option value="${it.id}">${it.type}</option>
                            </g:each>
                        </select>
                    </div>
                    <button name="submit" class="btn btn-success">Create MPC Entry</button>
                </g:form>

            </g:if>
            <g:else>
                <g:form action="updateMpc" class="well">
                    <g:hiddenField name="facilityId" value="${facility.id}"/>
                %{--<g:each in="${mpcSettings}">
                    <g:if test="${it.key != 'provider'}">
                        ${it.key}: <strong>${it.value}</strong><br>
                    </g:if>
                </g:each>--}%
                    <h4>Provider</h4>
                    <g:each in="${mpcSettings?.provider}">
                        <g:if test="${it.key != 'configuration'}">
                            ${it.key}: <strong>${it.value}</strong><br>
                        </g:if>
                    </g:each>
                    configuration:
                    <table id="configurationTable" class="table table-striped table-bordered">
                        <g:hiddenField name="maxIndex" value="${mpcSettings?.provider?.configuration?.size() ?: 0}"/>
                        <thead>
                        <tr>
                            <th>key</th>
                            <th>value</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${mpcSettings?.provider?.configuration}" var="conf" status="idx">
                            <tr>
                                <g:if test="${conf.key.contains("objectId_")}">
                                    <td>
                                        <select name="configurationKey_${idx}">
                                            <g:each in="${facility.courts}">
                                                <option value="objectId_${it.id}" ${conf.key == "objectId_" + it.id ? "selected":""}>
                                                    ${it.name}
                                                </option>
                                            </g:each>
                                        </select>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td><input type="text" name="configurationKey_${idx}" value="${conf.key}"/></td>
                                </g:else>
                                <td><input type="text" name="configurationValue_${idx}" value="${conf.value}"/></td>
                                <td class="text-center"><a href="javascript:void(0)" onclick="removeEntry($(this))">Remove</a></td>
                            </tr>
                        </g:each>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td colspan="2">
                                <a href="javascript:void(0)" onclick="addCourt()" class="btn btn-sm btn-default"><i class="fas fa-plus"></i> Add court</a>
                                <a href="javascript:void(0)" onclick="addGeneric()" class="btn btn-sm btn-default"><i class="fas fa-plus"></i> Add setting</a>
                            </td>
                        </tr>
                        <tr><td><button name="submit" class="btn btn-success">Update MPC Settings</button></td></tr>
                        </tfoot>
                    </table>
                </g:form>

                <div class="well">
                    <h4>Resend code requests</h4>
                    <p>
                        <i class="fa fa-warning text-warning"></i> <strong>Note:</strong>
                        This resends all existing bookings/code requests in MPC to the provider. <strong>This must be done after the provider (e.g. QT/Parakey) has cleared their hardware.</strong>
                    </p>
                    <p>
                        <g:form action="resendMPC" method="POST">
                            <g:hiddenField name="facilityId" value="${facility.id}"/>
                            <button name="submit" class="btn btn-primary" onclick="return confirm('Have you read the info text and are you sure you want to proceed?');">Resend code requests</button>
                        </g:form>
                    </p>
                </div>
            </g:else>

            <div class="well">
                <h4>Reset MPC settings to a clean slate</h4>
                <p>
                    <i class="fa fa-warning text-danger"></i> <strong>Warning:</strong>
                    This deletes all code requests, users and settings in MPC and their connection in MATCHi.
                    This means that you will have to start from the beginning (a clean slate) and reconfigure everything,
                    just like you do when installing the MPC integration for the first time. Customers will get new codes
                    also, since their connections in MPC will be recreated with new random codes.

                    <strong>This does not delete the code requests at the provider (e.g. QT/Parakey). You must talk to them after doing this so they clear the hardware on their part.</strong>
                </p>
                <p>
                    <g:form action="resetMPC" method="POST">
                        <g:hiddenField name="facilityId" value="${facility.id}"/>
                        <button name="submit" class="btn btn-danger" onclick="return confirm('Have you read the warning text and are you sure you want to proceed?');">Reset MPC settings</button>
                    </g:form>
                </p>
            </div>

            <div class="well">
                <h4>Courts</h4>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>ID</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${facility.courts}">
                        <tr>
                            <td>${it.name}</td>
                            <td>${it.id}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div style="display: none;">
    <select id="courtSelector" style="display: none;">
        <g:each in="${facility.courts}">
            <option value="objectId_${it.id}">
                ${it.name}
            </option>
        </g:each>
    </select>
</div>

<input id="settingField" type="text" style="display: none;"/>
<a id="removeBtn" href="javascript:void(0)" onclick="removeEntry($(this))" style="display: none;">Remove</a>

<r:script>
    var $courtSelector, $settingField, $removeBtn, $maxIndex, $confTable, $appendToElem;
    var CONFIGURATION_KEY = 'configurationKey_', CONFIGURATION_VALUE = 'configurationValue_';

    $(document).ready(function() {
        $('select').selectpicker();

        $courtSelector = $('#courtSelector');
        $settingField  = $('#settingField');
        $removeBtn     = $('#removeBtn');
        $maxIndex      = $('#maxIndex');
        $appendToElem  = $('#configurationTable').find('tbody');
    });

    var removeEntry = function(elem) {
        $(elem).closest('tr').remove();
    };

    var addCourt = function() {
        var confIndex   = getNewIndex();
        var keyProp     = CONFIGURATION_KEY + confIndex;
        var valueProp   = CONFIGURATION_VALUE + confIndex;
        var $keyToAdd   = $courtSelector.clone();
        var $valueToAdd = $settingField.clone();

        $keyToAdd.prop('id', keyProp); $keyToAdd.prop('name', keyProp);
        $valueToAdd.prop('id', valueProp); $valueToAdd.prop('name', valueProp);

        addToConfiguration($keyToAdd, $valueToAdd, confIndex);

        $('select').selectpicker();
    };

    var addGeneric = function() {
        var confIndex   = getNewIndex();
        var keyProp     = CONFIGURATION_KEY + confIndex;
        var valueProp   = CONFIGURATION_VALUE + confIndex;
        var $keyToAdd   = $settingField.clone();
        var $valueToAdd = $settingField.clone();

        $keyToAdd.prop('id', keyProp); $keyToAdd.prop('name', keyProp);
        $valueToAdd.prop('id', valueProp); $valueToAdd.prop('name', valueProp);

        addToConfiguration($keyToAdd, $valueToAdd, confIndex);

        $('select').selectpicker();
    };

    var addToConfiguration = function (keyElem, valueElem, confIndex) {
        addNewRow();
        $appendToElem.find('tr').last().find('td').eq(0).append(keyElem.css('display', 'block'));
        $appendToElem.find('tr').last().find('td').eq(1).append(valueElem.css('display', 'block'));
        $appendToElem.find('tr').last().find('td').eq(2).append($removeBtn.css('display', 'block'));

        updateConfSize(confIndex);
    };

    var addNewRow = function () {
        $appendToElem.append('<tr><td></td><td></td><td class="text-center"></td></tr>');
    };

    var updateConfSize = function (newIndex) {
        $maxIndex.val(parseInt(newIndex));
    };

    var getNewIndex = function() {
        return parseInt($maxIndex.val()) + 1;
    };
</r:script>
</body>
</html>
