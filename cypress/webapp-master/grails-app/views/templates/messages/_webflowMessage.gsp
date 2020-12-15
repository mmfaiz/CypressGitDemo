<%@ page import="org.joda.time.LocalTime" %>

<g:if test="${params.message}">
    <div class="alert alert-success">
        <a class="close" data-dismiss="alert" href="#"><i class="fas fa-times"></i></a>
        ${params.message}
    </div>
</g:if>
<g:if test="${params.error}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#"><i class="fas fa-times"></i></a>
        <h4 class="alert-heading">
            ${new LocalTime().toString("HH:mm:ss")}:  <g:message code="default.error.heading"/>
        </h4>
        ${params.error}
    </div>
</g:if>
