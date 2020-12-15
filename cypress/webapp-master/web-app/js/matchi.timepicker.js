(function( $ ) {
    var settings;

    var methods = {
        init : function( options ) {
            settings = $.extend( {
                'showMinutes' : true,
                'showLeadingZero' : true,
                'hourText' : 'Hour',
                'minuteText' : 'Minute',
                'hoursStart': 0,
                'hoursEnd': 23,
                'rows': 4
            }, options);

            $(this).timepicker({
                // Options
                timeSeparator: ':',
                showPeriod: false,            // Define whether or not to show AM/PM with selected time. (default: false)
                showPeriodLabels: false,       // Define if the AM/PM labels on the left are displayed. (default: true)
                showLeadingZero: settings.showLeadingZero,
                periodSeparator: ' ',         // The character to use to separate the time from the time period.
                //altField: '#alternate_input', // Define an alternate input to parse selected time to
                defaultTime: '00:00',         // Used as default time when input field is empty or for inline timePicker
                // (set to 'now' for the current time, '' for no highlighted time, default value: now)

                // trigger options
                showOn: 'focus',                // Define when the timepicker is shown.
                // 'focus': when the input gets focus, 'button' when the button trigger element is clicked,
                // 'both': when the input gets focus and when the button is clicked.
                button: null,                   // jQuery selector that acts as button trigger. ex: '#trigger_button'

                // Localization
                hourText: settings.hourText,               // Define the locale text for "Hours"
                minuteText: settings.minuteText,           // Define the locale text for "Minute"
                amPmText: ['AM', 'PM'],             // Define the locale text for periods

                // Position
                myPosition: 'left top',       // Corner of the dialog to position, used with the jQuery UI Position utility if present.
                atPosition: 'left bottom',    // Corner of the input to position

                // Events
                //beforeShow: beforeShowCallback, // Callback function executed before the timepicker is rendered and displayed.
                //onSelect: onSelectCallback        // Define a callback function when an hour / minutes is selected.
                //onClose: onCloseCallback,     // Define a callback function when the timepicker is closed.
                //onHourShow: onHourShow,       // Define a callback to enable / disable certain hours. ex: function onHourShow(hour)
                //onMinuteShow: onMinuteShow,   // Define a callback to enable / disable certain minutes. ex: function onMinuteShow(hour, minute)

                // custom hours and minutes
                hours: {
                    starts: settings.hoursStart, // First displayed hour
                    ends: settings.hoursEnd   // Last displayed hour
                },
                minutes: {
                    starts: 0,                // First displayed minute
                    ends: 55,                 // Last displayed minute
                    interval: 5               // Interval of displayed minutes
                },
                rows: settings.rows,                      // Number of rows for the input tables, minimum 2, makes more sense if you use multiple of 2
                showHours: true,              // Define if the hours section is displayed or not. Set to false to get a minute only dialog
                showMinutes: settings.showMinutes // Define if the minutes section is displayed or not. Set to false to get an hour only dialog
            });
        }
    };

    $.fn.addTimePicker = function() {
        // Method calling logic
        return methods.init.apply( this, arguments );
    };
})( jQuery );