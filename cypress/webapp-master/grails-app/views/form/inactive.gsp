<g:set var="closed" value="${new Date().clearTime() > formInstance.activeTo}"/>
<html>
<head>
  <meta name="layout" content="b3noFooter" />
  <title><g:message code="form.inactive.title${closed ? '1' : '2'}"/></title>
</head>
<body>
  <div class="overlay">
    <div class="content">
      <div class="container">

        <div class="row">

          <div class="col-sm-3">
            <div class="icon-xxlg">
              <span class="fa-stack fa-lg">
                <i class="fas fa-circle fa-stack-2x"></i>
                <i class="fas fa-bug fa-stack-1x fa-inverse"></i>
              </span>
            </div>
          </div><!-- /.col-sm-3 -->

          <div class="col-sm-9">
            <h1 class="page-header top-margin20"><g:message code="form.inactive.title${closed ? '1' : '2'}"/></h1>
            <p class="lead no-margin">
                <g:message code="form.inactive.message${closed ? '1' : '2'}"
                    args="[formatDate(date: closed ? formInstance.activeTo : formInstance.activeFrom, formatName: 'date.format.readable')]"/>
            </p>
            <p class="bottom-margin20">
              <g:if test="${flash.error}">
                  <p>${flash.error}</p>
              </g:if>
            </p>
            <a href="javascript:history.go(-1)" class="btn btn-success btn-lg"><g:message code="button.back.label"/></a>
          </div><!-- /.col-sm-9 -->

        </div><!-- /.row -->

      </div><!-- /.container -->
    </div><!-- /.content -->
  </div><!-- /.overlay -->
</body>
</html>
