/**
 * Matchi extension to bootstrap-select
 *
 * This plugin handles the selections of all options
 *  - Based on bootstrap-select
 *
 */
(function($){

    $.fn.allselectpicker = function(options) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).allselectpicker(options) });
            return this;
        }

        // private variables
        var settings;
        var $this;

        // sets the new css class on table
        var init = function() {
            $this.prepend('<option value="All" style="background: #e9e9e9;">'+options.selectAllText+'</option>');
            $this.selectpicker(options);

            $this.selectpicker().on('change', function(){ toggleSelectAll(); });
        };

        var toggleSelectAll = function() {
            var allOptionIsSelected = ($this.val() || []).indexOf('All') > -1;

            function valuesOf(elements) {
                return $.map(elements, function(element) {
                    return element.value;
                });
            }

            if (allOptionIsSelected) {
                var nrOfElements = $this.find('option[value!=All]').length;
                var allSelected = nrOfElements == ($this.selectpicker('val').length - 1);

                if(allSelected) {
                    $this.selectpicker('val', []);
                } else {
                    // Can't use .selectpicker('selectAll') because multiple "change" events will be triggered
                    $this.selectpicker('val', valuesOf($this.find('option[value!="All"]')));
                }
            }

            $this.data('allOptionIsSelected', allOptionIsSelected);
        };

        // init method
        this.initialize = function() {

            // extend settings
            settings = $.extend( {
                'selectAllText': options.selectAllText
            }, options);

            $this = $(this);
            init();

            return this;
        };

        return this.initialize();
    }
})(jQuery);
