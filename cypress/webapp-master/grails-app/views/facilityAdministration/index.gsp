<%@ page import="org.joda.time.format.DateTimeFormatter; com.matchi.FacilityProperty; com.matchi.DateUtil; groovy.time.*; com.matchi.enums.MembershipRequestSetting; org.joda.time.DateTime; com.matchi.Facility;"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="adminHome.index.title"/></title>
    <r:require modules="select2"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="facilityAdministration.index.title"/></li>
</ul>

<g:renderErrors bean="${facility}"/>

<ul class="nav nav-tabs">
    <li class="active"><g:link action="index"><g:message code="facility.label2"/></g:link></li>
    <li>
        <g:link action="settings"><g:message code="adminFacility.adminFacilityMenu.settings"/></g:link>
    </li>
    <!--
    <li>
        <g:link controller="facilityMessage" action="index"><g:message code="facilityAdministration.index.message11"/></g:link>
    </li>
    -->
    <g:if test="${facility.hasMPC()}">
        <li>
            <g:link controller="facilityControlSystems" action="index"><g:message code="facilityControlSystems.label"/></g:link>
        </li>
    </g:if>
    <li>
        <g:link controller="facilityAccessCode" action="index"><g:message code="facilityAccessCode.label.plural"/></g:link>
    </li>

</ul>

<g:form action="save" method="post" class="form-horizontal form-well" name="facilityAdministrationEditFrm">
    <g:hiddenField name="id" value="${facility?.id}" />
    <g:hiddenField name="version" value="${facility?.version}" />

    <div class="form-header">
        <g:message code="facilityAdministration.index.message12"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="name"><g:message code="facility.name.label"
                                                               default="Namn" /></label>
            <div class="controls">
                <g:textField name="name" value="${facility?.name}" class="span8" maxlength="255"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="description"><g:message code="facility.description.label" default="Beskrivning" /></label>
            <div class="controls">
                <g:textArea rows="3" cols="5" name="description" value="${facility?.description}" class="span8" maxlength="1000"/>
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="address"><g:message code="facility.address.label" default="Adress" /></label>
            <div class="controls">
                <g:textField name="address" value="${facility?.address}" class="span8" maxlength="255"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="zipcode"><g:message code="facility.zipcode.label" default="Postnummer" /></label>
            <div class="controls controls-row">
                <g:textField name="zipcode" value="${facility?.zipcode}" class="span3" maxlength="255"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="city"><g:message code="facility.city.label" default="Ort" /></label>
            <div class="controls">
                <g:textField name="city" value="${facility?.city}" class="span8" maxlength="255"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="municipality"><g:message code="facility.municipality.label" default="Kommun" />*</label>
            <div class="controls">
                <select id="municipality" name="municipality" class="chzn-select" style="width: 230px;">
                    <g:each in="${regions}">
                        <optgroup label="${it.name}">
                            <g:each in="${it.municipalities}" var="municipality">
                                <option value="${municipality.id}" ${facility?.municipality?.id == municipality.id ? "selected" : ""}>${municipality.name}</option>
                            </g:each>
                        </optgroup>
                    </g:each>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="country"><g:message code="facility.country.label" default="Land" /></label>
            <div class="controls">
                <g:select name="country" from="${grailsApplication.config.matchi.settings.facility.countries}"
                          optionKey="key" optionValue="value" value="${facility?.country}"
                          class="chzn-select" style="width: 230px;"/>
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="email"><g:message code="facility.email.label" default="Email" /></label>
            <div class="controls">
                <g:field type="email" name="email" value="${facility?.email}" class="span8" maxlength="255"/>
            </div>
        </div>
        <g:if test="${facility.hasApplicationInvoice()}">
            <div class="control-group">
                <label class="control-label" for="invoiceEmail"><g:message code="facility.invoiceEmail.label"/></label>
                <div class="controls">
                    <g:field type="email" name="invoiceEmail" value="${facility?.getFacilityPropertyValue(com.matchi.FacilityProperty.FacilityPropertyKey.INVOICE_EMAIL.toString())}" class="span8" maxlength="1000"/>
                </div>
            </div>
        </g:if>
        <div class="control-group">
            <label class="control-label" for="fax"><g:message code="facility.fax.label" default="Fax" /></label>
            <div class="controls">
                <g:textField name="fax" value="${facility?.fax}" class="span8" maxlength="255"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="telephone"><g:message code="facility.telephone.label" default="Telefon" /></label>
            <div class="controls">
                <g:textField name="telephone" value="${facility?.telephone}" class="span8" maxlength="255"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="website"><g:message code="facility.website.label"/></label>
            <div class="controls">
                <g:textField name="website" value="${facility?.website}" class="span8" maxlength="255"/>
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="plusgiro"><g:message code="facility.plusgiro.label" default="Plusgiro" /></label>
            <div class="controls">
                <g:textField name="plusgiro" value="${facility?.plusgiro}" class="span3" maxlength="255"/>&nbsp;&nbsp;&nbsp;<strong><g:message code="facility.bankgiro.label"/></strong> <g:textField name="bankgiro" value="${facility?.bankgiro}" class="span3" maxlength="255"/>
                <p class="help-block"><g:message code="facilityAdministration.index.payoutInfo"/></p>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="orgnr"><g:message code="facility.orgnr.label" default="Organisationsnr" /></label>
            <div class="controls">
                <g:textField name="orgnr" value="${facility?.orgnr}" class="span8" maxlength="255"/>
            </div>
        </div>

        <hr>
        <div class="control-group form-inline">
            <g:render template="/templates/facility/openhoursOLD"/>
        </div>
        <div class="control-group">
            <label class="control-label" for="openingHoursType"><g:message code="facility.openingHoursType.label"/></label>
            <div class="controls">
                <g:select name="openingHoursType" from="${Facility.OpeningHoursType.values()}"
                        value="${facility?.openingHoursType}" valueMessagePrefix="facility.openingHoursType"/>
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label" for="sports"><g:message code="adminFacility.create.sports"/></label>
            <div class="controls" id="sportSelect">
                <g:each var="sport" in="${sports.toList()}">
                    <label class="checkbox">
                        <g:checkBox name="sports" class="styled" checked="${facility?.sports?.contains(sport)}" value="${sport.id}" />
                        <g:message code="sport.name.${sport.id}"/>
                    </label>
                </g:each>
            </div>
        </div>

        <g:set var="mlcsOffline" value="${facility?.mlcsOffline()}"/>

        <g:if test="${mlcsOffline!=null}">
        <hr>
        <div class="control-group">
            <label class="control-label no-padding">
                <g:message code="facilityAdministration.index.mlcsStatus"/>
            </label>
            <div class="controls">
                <g:if test="${!mlcsOffline}">
                    <span class="label label-success">
                        <g:message code="default.status.ok"/>
                    </span>
                </g:if>
                <g:else>
                    <span class="label label-important">
                        <g:message code="facilityAdministration.index.mlcsStatus.noConnection"/>
                    </span>
                </g:else>
                <g:if test="${mlcsOffline}">
                    <g:message code="facilityAdministration.index.mlcsLastHeartbeat"
                               args="[formatDate(date: facility?.getMlcsLastHeartBeat(), format: 'yyyy-MM-dd HH:mm:ss')]"/>
                </g:if>

            </div>
        </div>
        </g:if>

        <!--
        <hr>
        <div class="control-group">
            <label class="control-label" for="apiKey"><g:message code="facility.apikey.label" default="API nyckel" /></label>
            <div class="controls">
                <g:textField name="apiKey" value="${facility?.apikey}" readonly="true" class="span8" />
            </div>
        </div>
        -->
        <div class="form-actions">
            <g:submitButton name="submit" value="${g.message(code: 'button.save.label')}" class="btn btn-success"/>
            <a href="javascript:history.go(-1)" class="btn btn-danger"><g:message code="button.back.label"/></a>
        </div>
    </fieldset>
</g:form>
<r:script>
    $(document).ready(function() {
        $("[rel=tooltip]").tooltip();

        $(".chzn-select").select2();

        $(".chzn-select[name='municipality']").select2({
            placeholder: "${message(code: 'municipality.multiselect.noneSelectedText')}..."
        });

        $("#facilityAdministrationEditFrm").preventDoubleSubmission({});
        //$.datepicker.regional[ "en" ];

    });
</r:script>
</body>
</html>