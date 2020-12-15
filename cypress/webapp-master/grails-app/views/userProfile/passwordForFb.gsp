<!-- <%@ page import="org.joda.time.LocalDate; org.joda.time.DateTime; com.matchi.messages.FacilityMessage; com.matchi.Sport;" %> -->
<html>
<head>
    <meta name="layout" content="b3noFooter"/>
    <title>${user.fullName()} - MATCHi</title>
</head>

<body>
<g:b3StaticErrorMessage bean="${user}"/>

<section class="block vertical-padding30">

    <div class="container">
        <div class="row">
            <div class="col-md-4 col-md-offset-4">

                <div class="page-header text-center">
                    <h1 class="h2"><g:message code="userProfile.passwordForFb.message1"/></h1>
                </div>

                <!-- USER INFO -->
                <div class="media vertical-padding20">
                    <div class="media-left">
                        <g:link action="index">
                            <div class="avatar-circle-sm avatar-bordered">
                                <g:fileArchiveUserImage size="small" id="${user.id}"/>
                            </div>
                        </g:link>
                    </div>

                    <div class="media-body full-width">
                        <h4 class="media-heading top-margin10">
                            ${user.fullName()}
                        </h4>

                        <div class="top-margin5">
                            ${user.email}
                        </div>
                    </div>

                </div><!-- /.media -->

                <label for="password"><g:message code="userProfile.passwordForFb.message2"/></label>
                <g:form controller="userProfile" action="setPassword" method="post">
                    <div class="input-group">
                        <g:passwordField name="password" value="" placeholder="${message(code: 'default.password.label')}" class="form-control"/>
                        <span class="input-group-btn">
                            <g:submitButton name="submit" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                        </span>
                    </div>
                    <p><small><g:message code="user.password.requirements"/></small></p>
                </g:form>
            </div><!-- /.col-sm-4 -->

        </div><!-- /.row -->
    </div><!-- /.container -->
</section>

</body>
</html>
