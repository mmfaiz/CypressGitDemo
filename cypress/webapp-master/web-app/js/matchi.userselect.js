/**
 * Matchi User Select plugin
 *
 * This plugin handles the selections of a user
 *  - Based on select2
 *  - Dependent on AutoCompleteSupportController (backend)
 *
 */
(function($){

    $.fn.matchiUserSelect = function(options) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).matchiUserSelect(options) });
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
                            page: page // page number
                        };
                    },
                    results: function (data) {
                        var more = data.more;
                        return {results: data.results, more: more};
                    }
                },
                formatResult: function(user) {
                    var markup = "<table><tr>";
                            markup += "<td><div><b>" + user.fullname + "</b></div><div>" + user.email + "</div></td>";
                            markup += "</tr></table>";
                    return markup
                },
                formatSelection: function(user) {
                    return user.fullname;
                },
                initSelection: function(element, callback) {

                    return $.ajax({
                        type: "POST",
                        url: settings.searchUrl,
                        dataType: 'json',
                        data: { id: (element.val())}
                    }).done(function(data) {
                        settings.onchange({}, data);
                        return callback(data);
                    });

                },
                formatInputTooShort: function(term, minLength) {
                    return $L('js.search.customer.minimum1') + " " + (minLength-term.length) + " " + $L('js.search.customer.minimum2');
                }
            });

            $this.on("change", function(e) {
                settings.onchange(e, $this.select2("data"));
            });

        };

        // init method
        this.initialize = function() {

            // settings
            settings = $.extend( {
                'key': 'value',
                'placeholder': $L('js.search.user'),
                'searchUrl': '/autoCompleteSupport/usersSelect2',
                'width': '250px',
                'onchange': this.onchange
            }, options);

            $this = $(this);
            init();

            return this;
        };

        this.onchange = function(e, data) {
            // override
        };

        this.open = function() {
            $this.select2('open');
        };

        return this.initialize();
    }
})(jQuery);
