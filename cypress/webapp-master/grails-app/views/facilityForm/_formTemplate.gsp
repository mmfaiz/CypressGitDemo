<g:each in="${formTemplate?.templateFields}" var="formField" status="i">
    <g:render template="/templates/dynamicForms/formField" model="[formField: formField, fieldInputPrefix: 'fields[' + i + '].']"/>
</g:each>