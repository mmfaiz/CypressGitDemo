/**
 * Matchi Customer Select plugin
 *
 * This plugin handles the selections of a customer
 *  - Based on select2
 *  - Dependent on AutoCompleteSupportController (backend)
 *
 */
(function($){

    $.fn.matchiCustomerSelect = function(options) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).matchiCustomerSelect(options); });
            return this;
        }

        // private variables
        var settings;
        var $this;

        // sets the new css class on table
        var init = function() {

            $this.select2({
                placeholder: settings.placeholder,
                minimumInputLength: 3,
                width: settings.width,
                allowClear: true,
                openOnEnter: false,
                ajax: {
                    url: settings.searchUrl,
                    dataType: 'json',
                    quietMillis: 100,
                    data: function (term, page) {
                        return {
                            query: term, //search term
                            page_limit: 10, // page size
                            page: page, // page number
                            availableCustomerIds: $('input[name=playerCustomerId]').map(function() { // available players
                                if (this.value) {
                                    return this.value;
                                }
                            }).get(),
                            excludeCustomersInGroup: settings.excludeCustomersInGroup, // exclude group
                            customersWithConnectedUsers: settings.customersWithConnectedUsers
                        };
                    },
                    results: function (data, page) {
                        var regex = new RegExp($.ui.autocomplete.escapeRegex(data.query), "i");
                        var sorted = $.grep(data.results, function(value) {
                            return value.fullname && regex.test(value.fullname);
                        });
                        sorted = sorted.concat($.grep(data.results, function(value) {
                            return value.email && regex.test(value.email) && $.inArray(value, sorted) == -1;
                        }));
                        sorted = sorted.concat($.grep(data.results, function(value) {
                            return value.number && regex.test(value.number) && $.inArray(value, sorted) == -1;
                        }));
                        sorted = sorted.concat($.grep(data.results, function(value) {
                            return value.telephone && regex.test(value.telephone) && $.inArray(value, sorted) == -1;
                        }));
                        sorted = sorted.concat($.grep(data.results, function(value) {
                            return $.inArray(value, sorted) == -1;
                        }));

                        return { results: sorted, more: data.more };
                    }
                },
                formatResult: function(customer) {
                    var markup   = "<table><tr>";
                    markup      += "<td><div><b>" + customer.fullname + "</b></div><div>" + customer.email + "</div></td>";
                    markup      += "</tr></table>";
                    return markup;
                },
                formatSelection: function(customer) {
                    return customer.fullname;
                },
                initSelection: function(element, callback) {
                    var id = element.val();

                    return $.ajax({
                        type: "POST",
                        url: settings.searchUrl,
                        dataType: 'json',
                        data: { id: id }
                    }).done(function(data) {
                            settings.onchange(data)
                            return callback(data);
                        });
                },
                formatInputTooShort: function(term, minLength) {
                    return $L('js.search.customer.minimum1') + " " + (minLength-term.length) + " " + $L('js.search.customer.minimum2');
                }
            });

            $this.on("change", function(e) {
                //settings.onchange(e, $this.select2("data"));
                settings.onchange($this.select2("data"));
            });
        };

        this.open = function() {
            $this.select2('open');
        };

        // init method
        this.initialize = function() {

            // settings
            settings = $.extend( {
                'key': 'value',
                'placeholder': $L('js.search.customer'),
                'searchUrl': '/autoCompleteSupport/customerSelect2',
                'width': '250px',
                'onchange': this.onchange,
                'excludeCustomersInGroup': '',
                'customersWithConnectedUsers': '',
                'initSelection': true
            }, options);

            $this = $(this);
            init();

            return this;
        };

        this.onchange = function(e, data) {
            // override
        };

        return this.initialize();
    };
})(jQuery);