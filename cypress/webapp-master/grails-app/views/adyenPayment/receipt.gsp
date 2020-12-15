<html>
<head>
    <meta name="layout" content="b3main">
    <title>Receipt</title>
</head>

<body>
<section class="vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin">Card saved</h2>

        <div class="row">
            <div class="col-md-12">
                <h4>${order.id}</h4>
                <h4>${order.article}</h4>
                <h4>${order.description}</h4>
                <h4><g:formatMoney value="${order.total()}" facility="${order?.customer?.facility}"/></h4>
            </div>
        </div>
    </div>
</section>
</body>
</html>
