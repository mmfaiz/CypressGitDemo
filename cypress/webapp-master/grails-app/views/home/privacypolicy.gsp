<html>
<head>
    <title><g:message code="home.integritypolicy.title"/> - MATCHi</title>
    <meta name="layout" content="${params.wl == '1'?'whitelabel':'b3main'}" />
</head>
<body>

<div class="block block-white">
  <div class="container top-margin20">

    <h1 class="vertical-padding20"><g:message code="home.integritypolicy.title"/></h1>

    <div class="row">

      <div class="col-md-8 vertical-padding20">

        <!-- Paragraph 1 -->
        <h3><legend><g:message code="home.integritypolicy.header1"/></legend></h3>
          <ul>
            <li><p><g:message code="home.integritypolicy.message1a"/></p></li>
            <li><p><g:message code="home.integritypolicy.message1b"/></p></li>
            <li><p><g:message code="home.integritypolicy.message1c"/></p></li>
            <li><p><g:message code="home.integritypolicy.message1d"/></p></li>
            <li><p><g:message code="home.integritypolicy.message1e"/></p></li>
          </ul>
        <br>

        <!-- Paragraph 2 -->
        <h3><legend><g:message code="home.integritypolicy.header2"/></legend></h3>
        <p><g:message code="home.integritypolicy.message2a"/></p><br>
        <p><g:message code="home.integritypolicy.message2b"/></p><br>
        <p><g:message code="home.integritypolicy.message2c"/></p><br>

        <!-- Paragraph 3 -->
        <h3><legend><g:message code="home.integritypolicy.header3"/></legend></h3>
        <p><g:message code="home.integritypolicy.message3a"/></p><br>
        <p><g:message code="home.integritypolicy.message3b"/></p>
        <ul>
          <li><p><g:message code="home.integritypolicy.message3b1"/></p></li>
          <li><p><g:message code="home.integritypolicy.message3b2"/></p></li>
          <li><p><g:message code="home.integritypolicy.message3b3"/></p></li>
          <li><p><g:message code="home.integritypolicy.message3b4"/></p></li>
        </ul>
        <br>

        <!-- Paragraph 4 -->
        <h3><legend><g:message code="home.integritypolicy.header4"/></legend></h3>
        <p><g:message code="home.integritypolicy.message4"/></p><br>

        <!-- Paragraph 5 -->
        <h3><legend><g:message code="home.integritypolicy.header5"/></legend></h3>
        <p><g:message code="home.integritypolicy.message5a"/></p><br>
        <p><g:message code="home.integritypolicy.message5b"/></p><br>
        <p><g:message code="home.integritypolicy.message5c"/></p><br>
        <p><g:message code="home.integritypolicy.message5d"/></p><br>
        <p><g:message code="home.integritypolicy.message5e"/></p><br>
        <p><g:message code="home.integritypolicy.message5f"/></p><br>

        <!-- Paragraph 6 -->
        <h3><legend><g:message code="home.integritypolicy.header6"/></legend></h3>
        <p><g:message code="home.integritypolicy.message6a"/></p><br>
        <p><g:message code="home.integritypolicy.message6b"/></p><br>
        <p><g:message code="home.integritypolicy.message6c"/></p><br>

        <!-- Paragraph 7 -->
        <h3><legend><g:message code="home.integritypolicy.header7"/></legend></h3>
        <p><g:message code="home.integritypolicy.message7"/></p><br>

        <!-- Paragraph 8 -->
        <h3><legend><g:message code="home.integritypolicy.header8"/></legend></h3>
        <p><g:message code="home.integritypolicy.message8"/></p><br>

        <!-- Paragraph 9 -->
        <h3><legend><g:message code="home.integritypolicy.header9"/></legend></h3>
        <p><g:message code="home.integritypolicy.message9a"/></p><br>
        <p><g:message code="home.integritypolicy.message9b"/></p><br>
        <p><g:message code="home.integritypolicy.message9c"/></p><br>
        <p><g:message code="home.integritypolicy.message9d"/></p><br>
        <p><g:message code="home.integritypolicy.message9e"/></p><br>

        <!-- Paragraph 10 -->
        <h3><legend><g:message code="home.integritypolicy.header10"/></legend></h3>
        <p><g:message code="home.integritypolicy.message10a"/></p><br>
        <p><g:message code="home.integritypolicy.message10b"/></p><br>

        <!-- Table with personal data categories-->
        <h3><legend><g:message code="home.integritypolicy.header11"/></legend></h3>
        <table class="table table-striped table-bordered table-hover">
          <thead>
            <tr>
              <th class="text-nowrap"><g:message code="home.integritypolicy.tableheader1"/></th>
              <th class="text-nowrap"><g:message code="home.integritypolicy.tableheader2"/></th>
              <th class="text-nowrap"><g:message code="home.integritypolicy.tableheader3"/></th>
              <th class="text-nowrap"><g:message code="home.integritypolicy.tableheader4"/></th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${(1..17).toList()}" var="row" >
              <tr>
                <td><g:message code="home.integritypolicy.tablerow${row}a" default=""/></td>
                <td class="text-nowrap"><g:message code="home.integritypolicy.tablerow${row}b" default=""/></td>
                <td><g:message code="home.integritypolicy.tablerow${row}c" default=""/></td>
                <td><g:message code="home.integritypolicy.tablerow${row}d" default=""/></td>
              </tr>
            </g:each>
          </tbody>
        </table>
        <small><g:message code="home.integritypolicy.tableexplanations"/></small>

        <!-- Table with subcontractors -->
        <h3><legend><g:message code="home.integritypolicy.header12"/></legend></h3>
        <table class="table table-striped table-bordered table-hover">
          <thead>
          <tr>
            <th class="text-nowrap"><g:message code="home.integritypolicy.subcontractorheader1"/></th>
            <th class="text-nowrap"><g:message code="home.integritypolicy.subcontractorheader2"/></th>
            <th class="text-nowrap"><g:message code="home.integritypolicy.subcontractorheader3"/></th>
          </tr>
          </thead>
          <tbody>
            <g:each in="${(1..4).toList()}" var="row">
              <tr>
                <td><g:message code="home.integritypolicy.subcontractorrow${row}a" default=""/></td>
                <td><g:message code="home.integritypolicy.subcontractorrow${row}b" default=""/></td>
                <td><g:message code="home.integritypolicy.subcontractorrow${row}c" default=""/></td>
              </tr>
            </g:each>
          </tbody>
        </table>

      </div><!-- /.col-md-8 -->

      <div class="col-md-4">

      </div><!-- /.col-md-4 -->

    </div><!-- /.row -->

  </div><!-- /.container -->

</div>
</body>
</html>
