/**
 * Matchi Schedule Table Plugin
 *
 * This plugin handles the layout of the facility booking table.
 * The plugin handles acts on window resize and performs several operations:
 *  - Resizes the table according to the new window size
 *  - Checks that table is not higher than 'maxSlotHeight' times number of rows
 *  - Sets small, medium, large css class on table according to settings
 *
 * note: min height is handled by standard css (td { height: xxx })
 */
(function($){

    $.fn.matchiSchedule = function(options) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).myPlugin(options) });
            return this;
        }

        // private variables
        var settings;
        var $this;
        var $cell;
        var $table;
        var headerHeight = 0;
        var maxTableHeight;
        var numRows = 1;

        // sets the new css class on table
        var setviewclass = function(clazz) {
            if(!$this.hasClass(clazz)) {
                $this.removeClass('large');
                $this.removeClass('medium');
                $this.removeClass('small');
                $this.addClass(clazz);
            }
        };

        // sets right css class according to cell height
        var calculateAndSetCssClass = function(cellHeight) {
            if (cellHeight < settings.small) {
                setviewclass('small');
            } else if (cellHeight <= settings.medium) {
                setviewclass('medium');
            } else if (cellHeight > settings.medium) {
                setviewclass('large');
            }
        };

        // resizing table according to new window size
        var resize = function() {
            var winSize = getWindowSize();
            var cellHeight = $cell.height();
            var newHeight  = winSize.height - settings.marginTop;
            var oldHeight  = $table.height();

            if(cellHeight < settings.maxSlotHeight || (newHeight < oldHeight)) {
                // calculates the new height
                newTableHeight = Math.min(newHeight, maxTableHeight);
                $table.height(newTableHeight);
                cellHeight = $cell.height(); // new cell height from resize
            }
            $table.find("table").height(cellHeight);

            // use court td height as slot height
            var newCellHeight = $table.find("td.court").height();
            $table.find("table").height(newCellHeight);
            $table.find("td.court .name").css("max-height", newCellHeight);

            // set table css class a
            calculateAndSetCssClass(cellHeight);
        };

        var groupHightlight = function() {
            var groupID;

            var $groupCells = $table.find("table").find("[class*=group_]");
            $groupCells.on("mouseover", function() {
                groupID = $(this).attr("class").match(/group_(\d*)/)[0];
                var $thisGroup = $table.find("table").find("[class*=group_]."+groupID);
                $thisGroup.find(".group-status").show();
                $thisGroup.addClass("group-ind");
            }).on("mouseout",function() {
                var $thisGroup = $table.find("table").find("[class*=group_]."+groupID);
                $thisGroup.find(".group-status").hide();
                $thisGroup.removeClass("group-ind");
            });
        };

        // init method
        this.initialize = function() {

            // settings
            settings = $.extend( {
                'large'    : 66,
                'medium'   : 55,
                'small'    : 35,
                'marginTop': 210,
                'maxSlotHeight': 40,
                'minSlotWidth': 50
            }, options);

            $('.slot').tooltip({delay: { show: 650, hide: 100 }});

            $this = $(this);
            headerHeight = $this.find("thead tr:first th:first").height();
            $cell = $this.find("tbody tr:first td:first");
            $table = $this.find("table:first");
            numRows = $this.find("table:first > tbody > tr > td:first-child").size();
            maxTableHeight = headerHeight + (numRows * settings.maxSlotHeight);

            $(window).resize(function() {
                resize();
            });

            resize(); // run resize on init
            groupHightlight(); // add support for group hover highlight

            return this;
        };

        this.redraw = function() {
            resize();
        };

        return this.initialize();
    };
})(jQuery);
