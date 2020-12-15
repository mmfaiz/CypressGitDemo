(function($) {
    var defaults = {
        max : 200,
        link: ''
    };

    var settings = {};//global settings

    $.fn.truncateText = function( options ) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).truncateText(options); });
            return this;
        }

        var $this;

        this.init = function() {
            settings = $.extend( {
                'max': defaults.max,
                'html': false
            }, options);

            $this = $(this);

            var len = $(this).text().length;
            if(len > settings.max) {
                $(this).text($(this).text().substr(0,settings.max)+'...');
            }
        };

        return this.init();
    };
})(jQuery);
