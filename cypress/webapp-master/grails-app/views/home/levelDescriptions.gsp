<%@ page import="com.matchi.Sport"%>
<html>
<head>
    <title><g:message code="page.sport.skillLevel.header"/></title>
    <meta name="layout" content="b3main" />
</head>
<body>

<section class="block block-grey vertical-padding20">
    <div class="container no-horizontal-padding">
        <h2><g:message code="page.sport.skillLevel.header"/></h2>
        <hr>
        <h4><g:message code="page.sport.skillLevel.generalDescription"/></h4>
    </div>
</section>

<section class="block bg-white top-padding30 bottom-padding60">
    <div class="container no-horizontal-padding">
        <g:each in="${Sport.coreSportAndOther.list()}" var="sport">
            <h5><legend class="text-muted"><g:message code="sport.name.${sport.id}" default=""/></legend></h5>
            <g:each in="${(1..10)}" var="i">
                <div class="vertical-margin5">
                    <span class="fa-stack">
                        <span class="fas fa-certificate fa-stack-2x" style="color: grey;"></span>
                        <span class="fa-stack-1x text-white">${i}</span>
                    </span>
                    <g:message code="sport.skillLevel.${sport.id}.${i}" default=""/><br/>
                </div>
            </g:each>
            <br/>
        </g:each>
    </div>
</section>
</body>
</html>