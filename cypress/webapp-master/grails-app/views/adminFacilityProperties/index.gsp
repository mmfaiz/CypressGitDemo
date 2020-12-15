<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page import="com.matchi.FacilityProperty; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminFacility.edit.title"/></title>
    <r:require modules="bootstrap3-wysiwyg"/>
</head>

<body>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li class="active"><g:message code="adminFacilityProperties.index.editProperties"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${facility}"/>

    <div class="panel panel-default">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 1]"/>

        <g:form action="update" id="${facility.id}" name="facilityPropertiesForm" class="form panel-body no-padding">
            <g:each in="${FacilityProperty.FacilityPropertyKey.values().toList().groupBy { it.category }.sort()}"
                    var="propertiesByCategory">
                <div class="form-group col-sm-12 well">

                    <h3><g:message code="facility.property.category.${propertiesByCategory.key}.label"/></h3>

                    <g:each in="${propertiesByCategory.value}">
                        <div class="form-group col-sm-12">

                            <g:if test="${it?.type == FacilityProperty.FacilityPropertyType.TEXTAREA}">
                                <label for="${it}"><g:message code="facility.property.${it}.label"/> - <g:message
                                        code="adminFacilityProperties.index.default"/>: ${it.defaultValue}</label>
                                <g:textArea id="${it}" name="${it}" rows="10" cols="100" class="form-control textarea"
                                            value="${facility.getFacilityProperty(it)?.value}"/>
                            </g:if>

                            <g:elseif test="${it?.type == FacilityProperty.FacilityPropertyType.BOOLEAN}">
                                <div class="checkbox">
                                    <input type="checkbox" id="${it}" value="1" class="form-control" name="${it}"
                                           <g:if test="${facility.isFacilityPropertyTrue(it)}">checked</g:if>/>
                                    <label for="${it}"><g:message code="facility.property.${it}.label"/> - <g:message
                                            code="adminFacilityProperties.index.default"/>: ${it.defaultValue}</label>
                                </div>
                            </g:elseif>
                            <g:elseif test="${it?.type == FacilityProperty.FacilityPropertyType.INTEGER}">
                                <label for="${it}"><g:message code="facility.property.${it}.label"/> - <g:message
                                        code="adminFacilityProperties.index.default"/>: ${it.defaultValue}</label>
                                <g:field type="number" name="${it}" value="${facility.getFacilityProperty(it)?.value}"
                                         class="form-control"/>
                            </g:elseif>
                            <g:else>
                                <label for="${it}"><g:message code="facility.property.${it}.label"/> - <g:message
                                        code="adminFacilityProperties.index.default"/>: ${it.defaultValue}</label>
                                <g:if test="${it?.readOnly}">
                                    <br>${facility.getFacilityProperty(it)?.value}
                                </g:if>
                                <g:elseif test="${it == FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER}">
                                    <g:render template="field/multiple_players_number"/>
                                </g:elseif>
                                <g:elseif test="${it == FacilityProperty.FacilityPropertyKey.FACILITY_DEFAULT_SPORT}">
                                    <g:select name="${it}"
                                              value="${facility.getFacilityProperty(it)?.value}"
                                              from="${com.matchi.Sport.list()}"
                                              optionKey="id"
                                              optionValue="name"
                                              class="form-control"
                                              noSelection="${['':'None']}"
                                    />
                                </g:elseif>
                                <g:else>
                                    <g:textField name="${it}" value="${facility.getFacilityProperty(it)?.value}"
                                                 class="form-control"/>
                                </g:else>
                            </g:else>

                            <span class="help-block"><g:message code="facility.property.${it}.description"/></span>
                        </div>
                    </g:each>

                </div>
            </g:each>

            <div class="form-group col-sm-12">
                <g:submitButton name="submit" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            </div>
        </g:form>
    </div>
</div>

<r:script>
    $(document).ready(function() {
        <g:each in="${FacilityProperty.FacilityPropertyKey.values()}">
    <g:if test="${it.toString().contains("_TEXT")}">
        $('#${g.forJavaScript(data: it)}').wysihtml5({
                    "image": false,
                    stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
                });
    </g:if>
</g:each>
    });
</r:script>
</body>
</html>
