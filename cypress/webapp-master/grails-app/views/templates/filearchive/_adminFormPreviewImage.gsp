<g:if test="${imageFile}">
    <img height="35" src="<g:fileArchiveURL file="${imageFile}"/>" style="float:left;margin-left:20px"/>
    <div style="float:left;margin:5px 0 0 10px">
        <b>${imageFile?.originalFileName}</b>
        &nbsp;&nbsp;&nbsp;<g:link action="${deleteAction}" params="${parameters}"><g:message code="button.remove.image"/></g:link>
        <br><g:message code="default.created.label"/>: <g:formatDate date="${imageFile.dateCreated}" format="yyyy-MM-dd HH:mm"/>
    </div>
</g:if>