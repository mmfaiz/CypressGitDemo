$(document).ready( function() {
    var $showInvoiceDate = $("#showInvoiceDate");
    var dateFormat = $showInvoiceDate.attr('dateFormat1');
    var $invoiceDate = $("#invoiceDate");

    $("#expirationDays").blur(function() {
        calculateExpirationDate();
    });

    $showInvoiceDate.datepicker({
        autoSize: true,
        dateFormat: dateFormat,
        altField: '#invoiceDate',
        altFormat: 'yy-mm-dd'
    });

    $invoiceDate.change(function() {
        calculateExpirationDate();
    });

    calculateExpirationDate();
});

var calculateExpirationDate = function() {
    var $showInvoiceDate = $("#showInvoiceDate");
    var dateFormat = $showInvoiceDate.attr('dateFormat2');
    var $expirationDays = $("#expirationDays");
    var $expirationDate = $("#expirationDate");

    console.log(dateFormat);

    var expirationDaysValue = $expirationDays.val();
    var expirationDays = 30;

    if(isNaN(expirationDaysValue) || expirationDaysValue == "") {
        $expirationDays.addClass("error");
        $expirationDate.html("-");
        return
    } else {
        $expirationDays.removeClass("error");
        expirationDays = Number(expirationDaysValue);
    }

    var invoiceDate = Date.parse($("#invoiceDate").val());
    var expirationDate = invoiceDate.add(Number(expirationDays)).days();

    $expirationDate.html(expirationDate.toString(dateFormat));
};