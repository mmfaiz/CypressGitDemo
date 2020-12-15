<g:if test="${facility.hasOrganization()}">
    <div class="control-group">
        <g:select name="${name}" from="${organizations}" value="${currentOrganizationId}" optionKey="id" optionValue="name"
                  noSelection="['': message(code: 'default.organization.select.noneSelectedText')]" />
    </div>
    <script>
        $(document).ready(function() {
           $("#${g.forJavaScript(data: name)}").change(function() {
               var organizationId = $(this).find(":selected").val();
               var articleName = "${g.forJavaScript(data: name.contains("Organization") ? name.replace('OrganizationId', '').concat('ExternalArticleId') : 'externalArticleId')}";
                $.ajax({
                    type: "POST",
                    dataType: "html",
                    url: "${g.forJavaScript(data: createLink(controller: 'facilityAdministration', action: 'listOrganizationArticles'))}?organizationId=" + organizationId + "&articleName=" + articleName
                }).done(function(data) {
                    if (data !== "400") {
                        $("[name=" + articleName + "]").replaceWith(data);
                    } else {
                        location.reload();
                    }
                });
           });
        });
    </script>
</g:if>