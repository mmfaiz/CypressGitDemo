<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title>MATCHi - <g:message code="facilityControlSystems.label"/></title>
    <r:script>

    </r:script>
</head>

<body>
    <div class="container content-container">

        <ul class="breadcrumb col-sm-12">
            <li><g:link action="index" controller="facilityAdministration"><g:message code="facilityAdministration.index.title"/></g:link></li>
            <li class="active"><g:message code="facilityControlSystems.label"/></li>
        </ul>


        <div class="tabs tabs-style-underline" style="width: 100%;">
            <nav>
                <ul>
                    <li><g:link controller="facilityAdministration" action="index"><g:message code="facility.label2"/></g:link></li>
                    <li>
                        <g:link controller="facilityAdministration" action="settings"><g:message code="adminFacility.adminFacilityMenu.settings"/></g:link>
                    </li>
                    <!--
                    <li>
                        <g:link controller="facilityMessage" action="index"><g:message code="facilityAccessCode.index.message11"/></g:link>
                    </li>
                    -->
                    <li class="active">
                        <g:link controller="facilityControlSystems" action="index"><g:message code="facilityControlSystems.label"/></g:link>
                    </li>
                    <li>
                        <g:link controller="facilityAccessCode" action="index"><g:message code="facilityAccessCode.label.plural"/></g:link>
                    </li>
                </ul>
            </nav>
        </div>

        <g:if test="${status != null}">
            <div class="well col-sm-12">
                <div class="row">
                    <div class="col-sm-12">
                        <h3><g:message code="facilityControlSystems.information.label" /></h3>
                    </div>
                    <div class="col-sm-12">
                        <strong><g:message code="facilityControlSystems.provider.label" />: </strong> <g:message code="facilityControlSystems.provider.${type}.label" />
                    </div>
                    <div class="col-sm-12">
                        <strong><g:message code="facilityControlSystems.connection.label" />: </strong> <p class="label <g:message code="facilityControlSystems.connection.${status}.label" />"><g:message code="facilityControlSystems.connection.${status}.text" /></p>
                    </div>
                </div>
            </div>
            <div class="well col-sm-12">
                <div class="row">
                    <div class="col-sm-12">
                        <h3><g:message code="facilityControlSystems.warningsAndNotifications.label" /></h3>
                    </div>
                    <div class="col-sm-12">
                        <g:form action="updateWarnings" method="POST">
                            <div class="form-group">
                                <label for="emailAddresses"><g:message code="facilityControlSystems.emailAddresses.label" /></label>
                                <input type="text" class="form-control" id="emailAddresses" name="emailAddresses" value="${facility.getMpcNotificationMails().join(',')}">
                            </div>
                            <g:if test="${facility.isSwedish()}">
                                <div class="form-group">
                                    <label for="phoneNumber"><g:message code="facilityControlSystems.phoneNumber.label" /></label>
                                    <input type="tel" class="form-control" id="phoneNumber" name="phoneNumber" pattern="<g:message code="facilityControlSystems.phoneNumber.pattern" />" value="${facility.getMpcNotificationPhoneNumber()}">
                                </div>
                            </g:if>
                            <button type="submit" class="btn btn-primary pull-right"><g:message code="button.save.label" /></button>
                        </g:form>
                    </div>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="well col-sm-12">
               <div class="row">
                   <div class="col-sm-12">
                       <h3><g:message code="facilityControlSystems.missing.label" /></h3>
                    </div>
                   <div class="col-sm-12">
                       <p><g:message code="facilityControlSystems.missing.description" /></p>
                   </div>
               </div>
            </div>
        </g:else>
    </div>
</body>
</html>
