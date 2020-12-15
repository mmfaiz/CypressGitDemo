/*
*
* jQuery plugins with functionality to send an extra parameter "allselected" true/false
* to a form surrounding a table.
* The plugin adds functionlity to the checkbox that is used to select all checkboxes in a list.
*
* Requirements:
* <table> element needs to have a surrounding <form> element.
*
* */
(function($) {
    var defaults = {
        max : 200,
        count: 0,
        name: ""
    };

    var settings = {};//global settings

    $.fn.selectAll = function( options ) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).selectAll(options); });
            return this;
        }

        var $this;
        var $trigger;
        var $form;
        var $colspan;
        var $checkboxes;
        var bulkactionClass = ".bulk-action";

        this.init = function() {
            settings = $.extend( {
                'max': defaults.max,
                'count': defaults.count,
                'name':defaults.name
            }, options);

            $this = $(this);
            $form = $this.closest("form");
            $trigger = $this.find("thead th input[type='checkbox']").eq(0);
            $colspan = $this.find("thead th").length;
            $checkboxes = $this.find("input[type='checkbox'].selector");

            if (parseInt(settings.count) > parseInt(settings.max)) {
                addParams();
                addTableRow();
            }

            connectTrigger();
            updateBulkActionButton();
            enableCheckboxes();
        };

        var addParams = function() {
            $form.prepend("<input type='hidden' id='allselected' name='allselected' value=''/>");
        };

        var addTableRow = function() {
            var $thead = $this.find("thead");
            var $content = "<tr id='select-all-row' style='display: none;background: #eee;'><td class='text-center center-text' colspan='"+$colspan+"'>" +
                            "<span id='select-all-desc'>" + $L('js.customers.marked1') + " <strong>" + settings.max + "</strong> "
                            + settings.name +  ".</span> <a id='select-all-link' href='javascript:void(0);'>"+ $L('js.customers.marked2')+ " " + settings.count + " " + $L('js.customers.marked3') + ".</a>" +
                            "</td></tr>";

            $($content).insertAfter($thead);
            enableSelectAllRow();
        };

        var enableSelectAllRow = function() {
            var $selectAll = $("#allselected");

            $this.find("#select-all-link").on("click", function() {
                if($selectAll.val() == "true") {
                    $selectAll.val(false);
                    setUnselectedContent();
                } else {
                    $selectAll.val(true);
                    setSelectedContent();
                }
            });
        };

        var connectTrigger = function() {
            var $selectAll = $("#allselected");

            $trigger.on("change", function() {
                if($(this).is(':checked')) {
                    $("#select-all-row").show();
                    $($checkboxes).prop('checked', true);

                } else {
                    $selectAll.val(false);
                    setUnselectedContent();
                    $("#select-all-row").hide();
                    $($checkboxes).prop('checked', false);
                }

                updateBulkActionButton();
            });
        };

        var setUnselectedContent = function() {
            var $allSelector = $("#select-all-link");
            var prefix = $("#select-all-desc");

            var $content = $L('js.customers.marked1') + " <strong>" + settings.max + "</strong> " + settings.name +  ".";
            prefix.html($content);
            $allSelector.html($L('js.customers.marked2') + " " + settings.count + " " + $L('js.customers.marked3'));
            $($checkboxes).removeAttr('disabled');

        };
        var setSelectedContent = function() {
            var $allSelector = $("#select-all-link");
            var prefix = $("#select-all-desc");

            var $content = $L('js.customers.marked.all') + " <strong>" + settings.count + "</strong> " + settings.name +  " " + $L('js.customers.marked.all.end');
            prefix.html($content);
            $allSelector.html($L('js.customers.marked.all.undo'));
            $($checkboxes).prop('disabled', true);
        };

        var updateBulkActionButton = function() {
            var selected = 0;
            $($checkboxes).each(function() {
                if($(this).prop('checked'))
                    selected++;
            });

            if(selected > 0) {
                $(bulkactionClass).removeClass("disabled").removeAttr("disabled");
            } else {
                $(bulkactionClass).addClass("disabled").attr("disabled", "true");
            }
        };

        var enableCheckboxes = function() {
            $checkboxes.on("change", function() {
                updateBulkActionButton();
            });
        };

        return this.init();
    };
})(jQuery);