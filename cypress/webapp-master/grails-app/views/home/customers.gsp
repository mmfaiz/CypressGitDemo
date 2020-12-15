<html>
<head>
    <title><g:message code="page.home.index.header"/></title>
    <meta name="layout" content="b3main" />
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
    <r:require modules="jquery-validate"/>
</head>
<body>
    <!-- ClickTale Top part -->
    <script type="text/javascript">
        var WRInitTime=(new Date()).getTime();
    </script>
    <!-- ClickTale end of Top part -->

    <g:b3StaticErrorMessage bean="${cmd}"/>

    <!-- Customers -->
    <section class="block block-white vertical-padding40">
        <div class="container text-center">
            <div class="vertical-padding20">
                <h1 class="page-header"><g:message code="home.customers.message2"/></h1>
                <p class="lead">
                    <g:message code="home.customers.message3"/>
                </p>
            </div>

            <g:each in="${facInfo}" status="i" var="fac">
                <g:if test="${i % 4 == 0 || i == 0}">
                    <div class="row">
                </g:if>

                <div class="col-sm-3 padding5">
                    <div class="vertical-padding30 horizontal-padding30 bg-grey-light">
                        <div class="padding10">
                            <div class="logotype" style="background: url('${fac.logotype}') no-repeat center center;background-size: contain;width: 100%;height: 125px;">

                            </div>
                        </div>
                        <div class="text-center">
                            ${fac.nrCourts} <span><g:message code="court.label.plural2"/></span>
                        </div>
                    </div>
                </div>

                <g:if test="${((i+1) % 4 == 0 && i > 0) || i == facInfo.size() -1}">
                    </div>
                </g:if>
            </g:each>

        </div><!-- /.container -->
    </section><!-- /Customers -->

    <!-- Contact form -->
    <section class="block block-grey text-center vertical-padding60" id="getInTouch">
        <div class="container">

            <div class="interestFormContainer vertical-padding20">
                <h2 class="bottom-padding20">
                    <g:message code="home.customers.message4"/><br/>
                    <small><g:message code="home.customers.message5"/></small>
                </h2>

                <g:form id="interestForm" name="interestForm" class="form-inline" role="form" action="interested">
                    <div class="form-group relative">
                        <g:textField class="form-control text" name="name" placeholder="${message(code: 'interestedCommand.name.placeholder')}" />
                    </div>
                    <div class="form-group relative">
                        <g:textField class="form-control required email" id="email" name="email" placeholder="${message(code: 'interestedCommand.email.placeholder')}" />
                    </div>
                    <div class="form-group relative">
                        <g:textField class="form-control" name="facility" placeholder="${message(code: 'home.customers.message8')}" />
                    </div>
                    <div class="form-group relative">
                        <g:textField class="form-control number" name="phone" placeholder="${message(code: 'default.phoneNumber.label')}" />
                    </div>
                    <button type="submit" class="btn btn-small btn-success btn-outline"><g:message code="button.smash.label"/></button>
                </g:form>
                <div class="space-10"></div>
            </div>
        </div><!-- /.container -->
    </section><!-- /.Contact form -->

    <!-- ClickTale Bottom part -->
    <div id="ClickTaleDiv" style="display: none;"></div>
    <script type="text/javascript">
        if(document.location.protocol!='https:')
            document.write(unescape("%3Cscript%20src='http://s.clicktale.net/WRe0.js'%20type='text/javascript'%3E%3C/script%3E"));

        if(typeof ClickTale=='function') ClickTale(20340,1,"www14");
    </script>
    <!-- ClickTale end of Bottom part -->

</body>

<r:script>
$(document).ready(function() {

    $("#interestForm").validate({
        errorPlacement: function(error, element) { },
        highlight: function (element, errorClass) {
            $(element).addClass("invalid");
            $(element).after( '<i class="fas fa-times validation-icon"></i>');
            $(".fa-check").hide();
            <!-- $(element).css("background-color","#FFE5DA"); -->

        },
        unhighlight: function (element, errorClass) {
            $(element).addClass("valid");
            $(element).after( '<i class="fas fa-check validation-icon"></i>');
            $(".fa-times").hide();
            <!-- $(element).css("background-color","#CFFFCD"); -->
        }
    });

    $("#call2action").click(function() {
        $('html, body').animate({
            scrollTop: $("#getInTouch").offset().top
        }, 2000);
    });
});
</r:script>

</html>
