Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

/**
 * Block booking jQuery plugin
 */

(function( $ ){

    var special = jQuery.event.special,
        uid1 = 'D' + (+new Date()),
        uid2 = 'D' + (+new Date() + 1);

    special.scrollstart = {
        setup: function() {

            var timer,
                handler =  function(evt) {

                    var _self = this,
                        _args = arguments;

                    if (timer) {
                        clearTimeout(timer);
                    } else {
                        evt.type = 'scrollstart';
                        jQuery.event.handle.apply(_self, _args);
                    }

                    timer = setTimeout( function(){
                        timer = null;
                    }, special.scrollstop.latency);

                };

            jQuery(this).bind('scroll', handler).data(uid1, handler);

        },
        teardown: function(){
            jQuery(this).unbind( 'scroll', jQuery(this).data(uid1) );
        }
    };

    special.scrollstop = {
        latency: 200,
        setup: function() {

            var timer,
                    handler = function(evt) {

                    var _self = this,
                        _args = arguments;

                    if (timer) {
                        clearTimeout(timer);
                    }

                    timer = setTimeout( function(){

                        timer = null;
                        evt.type = 'scrollstop';
                        jQuery.event.handle.apply(_self, _args);

                    }, special.scrollstop.latency);

                };

            jQuery(this).bind('scroll', handler).data(uid2, handler);

        },
        teardown: function() {
            jQuery(this).unbind( 'scroll', jQuery(this).data(uid2) );
        }
    };

})( jQuery );

/**
 * MATCHi Facility block booking jQuery plugin
 */
(function($){

    $.fn.bb = function(options) {
        // support multiple elements
        if (this.length > 1){
            this.each(function() { $(this).myPlugin(options) });
            return this;
        }

        // private variables
        var blockBookingControlClass = ".blockbooking-control";
        var mouseDownEvent = "mousedown.blockBooking";
        var mouseDownDragEvent = "mousedown.dragAndMark";
        var mouseUpDragEvent = "mouseup.dragAndMark";

        var settings;
        var $this;
        var $originator;
        var started = false;
        var markedFreeSlotIds = {};
        var markedNonPaidBookedSlotIds = {};
        var markedBookedSlotIds = {};
        var cancelledSlotIds = {};
        var restrictedSlotIds = {};
        var unRestrictedSlotIds = {};

        var slotclicked = function(slotCell) {
            if(!slotexists(slotCell)) {
                addslot(slotCell)
            } else {
                removeslot(slotCell);
            }
        };

        var slothovered = function() {
            addslot($(this));
        };

        var addslot = function(slotCell) {

            if(!slotexists(slotCell) && !slotCell.hasClass("not-available")) {
                slotId = slotCell.attr("slotid");
                if(slotCell.hasClass("free")) {
                    slotCell.addClass("marked");
                    markedFreeSlotIds[slotId] = true;
                    if (slotCell.hasClass("lightBlue")) {
                        cancelledSlotIds[slotId] = true;
                    }
                } else {
                    slotCell.addClass("marked");
                    markedBookedSlotIds[slotId] = true;

                    if((slotCell.hasClass("OK") || slotCell.hasClass("PENDING") || slotCell.hasClass("PARTLY")) && !slotCell.hasClass("online")) {
                        markedNonPaidBookedSlotIds[slotId] = true;
                    }
                }

                if(slotCell.hasClass("restricted")) {
                    restrictedSlotIds[slotId] = true;
                } else {
                    unRestrictedSlotIds[slotId] = true;
                }

            }
            settings.onchange()
        };

        var removeslot = function(slotCell) {
            slotId = slotCell.attr("slotid");
            if(slotCell.hasClass("free")) {
                slotCell.removeClass("marked");
                delete markedFreeSlotIds[slotId];
            } else {
                slotCell.removeClass("marked");
                delete markedBookedSlotIds[slotId];

                if(slotCell.hasClass("OK") || (slotCell.hasClass("PENDING") || slotCell.hasClass("PARTLY"))) {
                    delete markedNonPaidBookedSlotIds[slotId];
                }
            }

            if(slotCell.hasClass("restricted")) {
                delete restrictedSlotIds[slotId];
            } else {
                delete unRestrictedSlotIds[slotId];
            }
            settings.onchange();
        };

        var slotexists = function(slotCell) {
            if(slotCell.hasClass("free")) {
                return slotCell.attr("slotid") in markedFreeSlotIds;
            } else {
                return slotCell.attr("slotid") in markedBookedSlotIds || slotCell.attr("slotid") in markedNonPaidBookedSlotIds;
            }

        };

        var clear = function() {
            $this.find("td.marked").removeClass("marked");
            markedFreeSlotIds = [];
            markedBookedSlotIds = [];
            markedNonPaidBookedSlotIds = [];
            cancelledSlotIds = [];
            restrictedSlotIds = [];
            unRestrictedSlotIds = [];
        };

        var onchange = function() {
        };

        // public methods
        this.initialize = function() {
            $this = $(this);

            settings = $.extend( {
                'large'    : 35,
                'medium'   : 30,
                'small'    : 25,
                'onchange' : onchange
            }, options);

            return this;
        };

        this.isStarted = function() {
            return started;
        };

        this.start = function(originator) {
            if(!started) {
                started = true;
                $(blockBookingControlClass).css("display", "inline-block");
                if(originator) {
                    $originator = $(originator);
                    $originator.parent().hide();
                }

                $('td.slot').on(mouseDownEvent, function(e) {
                    slotclicked($(this));
                    e.preventDefault();
                });


                $(document).on(mouseDownDragEvent, function() {
                    $("td.slot").bind('mouseover.matchiBlockBooking', slothovered);
                }).on(mouseUpDragEvent,function() {
                    $("td.slot").unbind('mouseover.matchiBlockBooking');
                });

            }
        };

        this.stop = function() {
            if(started) {
                started = false;

                $(blockBookingControlClass).hide();

                if($originator) {
                    $originator.parent().show();
                }

                $('td.slot').off(mouseDownEvent);
                $(document).off(mouseDownDragEvent);
                $(document).off(mouseUpDragEvent);

                clear();
            }

        };

        this.numSelectedFreeSlots = function() {
            return Object.size(markedFreeSlotIds);
        };

        this.getSelectedFreeSlots = function() {
            return markedFreeSlotIds;
        };

        this.numSelectedNonPaidBookedSlots = function() {
            return Object.size(markedNonPaidBookedSlotIds);
        };

        this.getSelectedNonPaidBookedSlots = function() {
            return markedNonPaidBookedSlotIds;
        };

        this.numSelectedBookedSlots = function() {
            return Object.size(markedBookedSlotIds);
        };

        this.getSelectedBookedSlots = function() {
            return markedBookedSlotIds;
        };

        this.numSelectedCancelledSlots = function() {
            return Object.size(cancelledSlotIds);
        };

        this.getSelectedCancelledSlots = function() {
            return cancelledSlotIds;
        };

        this.numSelectedRestrictedSlots = function () {
            return Object.size(restrictedSlotIds);
        };

        this.getSelectedRestrictedSlots = function () {
            return restrictedSlotIds
        };

        this.numSelectedUnRestrictedSlots = function () {
            return Object.size(unRestrictedSlotIds);
        };

        this.getSelectedUnRestrictedSlots = function () {
            return unRestrictedSlotIds
        };

        return this.initialize();
    }
})(jQuery);