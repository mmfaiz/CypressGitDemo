<%@ page import="groovy.xml.XmlUtil" %>

<div class="modal-header">
    <h3><g:message code="default.loader.label"/></h3>
</div>
<div id="modal-body" class="modal-body" style="width:600px">
    <g:if test="${payment}">
        ${payment.status}
    </g:if>

    <g:if test="${query}">
    <pre>${XmlUtil.serialize(query).encodeAsHTML()}</pre>
    </g:if>
</div>

<div class="modal-footer">
    <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label"/></a>
</div>
