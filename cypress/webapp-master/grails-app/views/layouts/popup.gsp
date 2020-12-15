<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><g:layoutTitle default="MATCHi" /></title>
    <script type="text/javascript" src="${grailsApplication.config.adyen.library}"></script>
    <r:layoutResources/>
</head>
<body style="background: #fff;">
<g:layoutBody />
<g:render template="/templates/payments/dialogLoader"/>
<div class="clear"></div>
<r:layoutResources/>
</body>
</html>