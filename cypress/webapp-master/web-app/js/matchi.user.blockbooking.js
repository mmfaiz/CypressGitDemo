Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

Array.prototype.remove = function() {
    var what, a = arguments, L = a.length, ax;
    while (L && this.length) {
        what = a[--L];
        while ((ax = this.indexOf(what)) !== -1) {
            this.splice(ax, 1);
        }
    }
    return this;
};

/**
 * MATCHi User block booking jQuery plugin
 */
(function($) {
    $.fn.userBlockBook = function(options) {

        // Private variables
        var mouseDownEvent = 'mousedown.blockBooking';
        var $blockBookStart, $blockBookControl, $blockBookBtn, $blockCancelBtn;

        var settings;
        var $this;
        var started = false;
        var slotIds = [];
        var noSlotErrorMessage = options.noSlotErrorMessage || 'No bookings chosen';

        /*
        * Private methods
        */
        var getNumSlotIds = function() {
            return Object.size(slotIds);
        };

        var getSlotIds = function() {
            return slotIds;
        };

        var hasSlotIds = function() {
            return (typeof slotIds !== 'null') && slotIds.length >= 1;
        };

        var slotClicked = function(slotCell) {
            var exists = slotExists(slotCell);

            if(!exists && slotCell.hasClass('free')) {
                addSlot(slotCell)
            } else if (exists) {
                removeSlot(slotCell);
            }
        };

        var addSlot = function(slotCell) {
            var slotId = slotCell.attr('slotid');
            slotCell.addClass('marked');
            slotCell.closest('.slots-container').find('.btn-slot.collapse-trigger.active').addClass('marked');

            slotIds.push(slotId);

            settings.onchange();
        };

        var removeSlot = function(slotCell) {
            var slotId = slotCell.attr('slotid');

            slotCell.removeClass('marked');

            if (!slotCell.closest('.list-group').find('.slot.free').hasClass('marked')) {
                slotCell.closest('.slots-container').find('.btn-slot.collapse-trigger.active').removeClass('marked');
            }

            slotIds.remove(slotId);
            settings.onchange();
        };

        var slotExists = function(slotCell) {
            return slotIds.indexOf(slotCell.attr('slotid')) > -1;
        };

        var clear = function() {
            $this.find('.marked').removeClass('marked');
            slotIds = [];
            onchange();
        };

        var stop = function() {
            if(started) {
                started = false;

                $blockBookControl.hide();
                $blockBookStart.show();

                $('td.slot').off(mouseDownEvent);
                $('a.slot').off(mouseDownEvent);
                clear();
            }
        };

        var onchange = function() {
            var text = $blockBookBtn.text();
            var num  = getNumSlotIds();

            $blockBookBtn.text(text.split(" ")[0] + " (" + num + ")");
        };

        var start = function(originator) {
            if(!started) {
                started = true;
                if(originator) {
                    $blockBookStart.hide();
                    $blockBookControl.show();
                    $blockBookControl.css('display', 'inline-block');
                }

                // Schedule
                $('td.slot').on(mouseDownEvent, function(e) {
                    slotClicked($(this));
                    e.preventDefault();
                });

                // List slots
                $('a.slot').on(mouseDownEvent, function(e) {
                    slotClicked($(this));
                    e.preventDefault();
                });
            }
        };

        /*
        * Public methods
        */
        this.isStarted = function() {
            return started;
        };

        this.stop = function() {
            stop();
        };

        this.init = function() {
            stop();
            $this = $(this);
            var $controlParent = $this.parent().closest('div.row');
            $blockBookStart    = $controlParent.find('.block-book-start');
            $blockBookControl  = $controlParent.find('.block-book-controls');

            $blockBookBtn      = $blockBookControl.find('.block-book');
            $blockCancelBtn    = $blockBookControl.find('.block-book-cancel');

            $blockBookStart.on('click', function() {
                start($blockBookStart);
            });
            $blockBookBtn.on('click', function () {
                if(!hasSlotIds()) {
                    alert(noSlotErrorMessage);
                } else {
                    $.ajax({
                        cache: false,
                        method: 'POST',
                        url: settings.url,
                        data: {
                            slotIds: getSlotIds,
                            facilityId: settings.facilityId
                        },
                        success: function (html) {
                            $('#userBookingModal').html(html);
                            showLayer('userBookingModal')
                        },
                        error: function() {
                            handleAjaxError();
                        }
                    });
                }
            });
            $blockCancelBtn.on('click', function () {
                stop();
            });

            settings = $.extend({
                'onchange' : onchange
            }, options);

            return this;
        };

        return this.init();
    }
})(jQuery);