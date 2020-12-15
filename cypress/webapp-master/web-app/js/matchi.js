/* Closes message box */
/*
function closeMessage() {
    $('#message').slideUp(300);
}
function closeSuccessMessage() {
    $('#message.success-box').slideUp(300);
}
function closeErrorMessage() {
    $('#message.error-box').slideUp(300);
}
function closeWarningMessage() {
    $('#message.warning-box').slideUp(300);
}
*/
function hideNotification(cookieName) {
    var now = new Date();
    var numberOfDaysToAdd = 1;
    now.setDate(now.getDate() + numberOfDaysToAdd);

    var year  = now.getFullYear();
    var month = now.getMonth() + 1;
    var day   = now.getDate();
    setCookie(cookieName, "true", year, month, day);
}

function removeWelcomeMessage() {
    $('#welcomemessage').fadeOut();

    setCookie("welcomeMessage", "true", "2100", "01", "01");
}
function removeFacilityInfoMessage() {
    setCookie("hideFacilityInfo", "true", "2100", "01", "01");
}
/* Hide the news section on landing page */
function removeNewsInfoMessage() {
    setCookie("hideNewsInfoSection", "true", "2100", "01", "01");
}
/* Hide the alert-info on Account EDIT page */
function removeAccountEditInfoMessage() {
    setCookie("hideAccountEditInfo", "true", "2100", "01", "01");
}
/* Hide the alert-info on Account SETTINGS page */
function removeAccountSettingsInfoMessage() {
    setCookie("hideAccountSettingsInfo", "true", "2100", "01", "01");
}

function removeMatchingInfoMessage() {
    setCookie("hideMatchingInfo", "true", "2100", "01", "01");
}
function removeFacebookNagger() {
    setCookie("hideFacebookNagger", "true");

}
/* Called from facilityBlockBooking/index.remote.gsp */
function showLayer(obj) {
    if(obj) {
        var $obj = $('#' + obj);
        var template = $obj.html();
        $obj.modal({show: true, dynamic: true});
        $obj.on('hidden', function() {
            if(this.html) {
                this.html(template);
            }
        })
    } else {
        $('#bookingModal').modal({show: true, dynamic: true, backdrop: 'static', keyboard: false});
    }
}
function abortBooking() {
    $('#bookingModal').modal('hide');

    $('div.bookingContainer table td').removeClass('active');
}

/* Called from facilityBlockBooking/index.remote.gsp */
function nrSelectedVisible(show) {
    var $nrSelected = $('#nrSelected');

    if(show)
        $nrSelected.show();
    else
        $nrSelected.hide();
}
function resetBooking() {
    var $nrSelected = $('#nrSelected span');

    $nrSelected.attr('nr', 0);
    $nrSelected.html('Valda tider: <b>0</b>');
    $('td.free').removeClass('grey');
}
function increaseNrSelected() {
    var $nrSelected = $('#nrSelected span');
    var newNr = $nrSelected.attr('nr');
    newNr++;
    $nrSelected.attr('nr', newNr);
    $nrSelected.html('Valda tider: <b>' + newNr + '</b>');
}
function decreaseNrSelected() {
    var $nrSelected = $('#nrSelected span');
    var newNr = $('#nrSelected span').attr('nr');
    newNr--;
    $nrSelected.attr('nr', newNr);
    $nrSelected.html('Valda tider: <b>' + newNr + '</b>');
}

/* parseBookings */
function parseBookings(map) {
    var str = "";

    for(var i=0; i>map.length;i++) {
        str =+ map[i] + ",";
    }

    return str;
}

function getObjectSize(myObject) {
    var count = 0;

    for (var key in myObject)
        count++;

    return count;
}

function goFullScreen(animate) {
    var winSize = getWindowSize();

    if(winSize != null) {
        var scrollBarWidth = 80;
        var contentSize = winSize.width - scrollBarWidth;
        var columnSize = contentSize;
        var wrapperContainerSize = contentSize + 20;
        var containerSize = columnSize - 40;
        var tableSize = containerSize;

        if(animate) {
            //$('#content').animate({width: contentSize}, 150, function(){});
            //$('div.one-column').animate({ 'margin': '0 10px', 'width': columnSize}, 150, function(){});
            $('div.bookingContainer_wrapper').animate({width: wrapperContainerSize}, 150, function(){});
            $('div.bookingContainer').animate({width: containerSize}, 150, function(){});
            $('div.bookingContainer table').animate({width: tableSize}, 150, function(){});
        } else {
            //$('#content').width(contentSize);
            //$('div.one-column').width(columnSize);
            $('div.bookingContainer_wrapper').width(wrapperContainerSize);
            $('div.bookingContainer').width(containerSize);
            $('div.bookingContainer table').width(tableSize);
        }

    } else {
        //$('#content').animate({width: '1400px'}, 150, function(){});
        //$('div.one-column').animate({ 'margin': '0 10px', 'width': '1360px'}, 150, function(){});
        $('div.bookingContainer_wrapper').animate({width: '1380px'}, 150, function(){});
        $('div.bookingContainer').animate({width: '1320px'}, 150, function(){});
    }

    $('#sizeToggle').attr('onclick', 'goNormal()');

    setCookie("fullscreen", "true");
}
function goNormal() {
    $('#content').animate({width: '960px'}, 150, function(){});
    $('div.one-column').animate({ 'margin': '0', 'width': '898px'}, 150, function(){});
    $('div.bookingContainer').animate({width: '898px'}, 150, function(){});
    $('div.bookingContainer table').animate({width: '898px'}, 150, function(){});
    $('#sizeToggle').attr('onclick', 'goFullScreen(true)');

    setCookie("fullscreen", "false");
}

function getWindowSize() {
    var winW = 0, winH = 0;
    var windowProps = { width: 0, height: 0 };
    if (document.body && document.body.offsetWidth) {
        windowProps.width  = document.body.offsetWidth;
        windowProps.height = document.body.offsetHeight;
    }
    if (document.compatMode=='CSS1Compat' &&
        document.documentElement &&
        document.documentElement.offsetWidth ) {
        windowProps.width = document.documentElement.offsetWidth;
        windowProps.height = document.documentElement.offsetHeight;
    }
    if (window.innerWidth && window.innerHeight) {
        windowProps.width = window.innerWidth;
        windowProps.height = window.innerHeight;
    } else {
        return null;
    }

    return windowProps;
}

function setCookie ( name, value, exp_y, exp_m, exp_d, path, domain, secure )
{
    var cookie_string = name + "=" + escape ( value );

    if ( exp_y ) {
        var expires = new Date ( exp_y, exp_m, exp_d );
        console.log(expires);

        cookie_string += "; expires=" + expires.toGMTString();
    }

    if ( path )
        cookie_string += "; path=" + escape ( path );

    if ( domain )
        cookie_string += "; domain=" + escape ( domain );

    if ( secure )
        cookie_string += "; secure";

    document.cookie = cookie_string;
}

function getCookie ( cookie_name ) {
    var results = document.cookie.match ( '(^|;) ?' + cookie_name + '=([^;]*)(;|$)' );

    if ( results )
        return ( unescape ( results[2] ) );
    else
        return null;
}

function submitForm ( id ) {
    $(id).submit();
}

function submitFormTo ( id, to ) {
    $(id).attr("action", to);
    $(id).submit();
}

function selector() {
    if($("input[type='checkbox'].checkbox-selector").is(':checked')) {
        $("input[type='checkbox'].selector").attr('checked', true);
    } else {
        $("input[type='checkbox'].selector").attr('checked', false);
    }
}

function handleAjaxError(XMLHttpRequest,textStatus,errorThrown) {

    if(XMLHttpRequest) {
        if(XMLHttpRequest.status == 401) {
            alert("Du har blivit utloggad, var god logga in igen.");
            location.reload();
        }
    } else {
        alert("Något gick fel! Försök igen senare eller kontakta MATCHi-support.");
    }
}

// jQuery plugin to prevent double submission of forms
jQuery.fn.preventDoubleSubmission = function(/*Object*/_args) {
    $(this).bind('submit',function(e) {

        var $form = $(this);

        if ($form.data('allowDoubleSubmission') === true) {
            $form.removeData('allowDoubleSubmission');
        } else if ($form.data('submitted') === true) {
            // Previously submitted - don't submit again
            e.preventDefault();
        } else {
            // Mark it so that the next submit can be ignored
            $form.data('submitted', true);

            // disabled buttons do not work since the action is wrong, but this simulates it somewhat
            // the double prevention logic is still in place by the above logic anyway
            $(this).find(':submit').css({ 'pointer-events': 'none', 'opacity': '0.5' });
        }
    });

    // Keep chainability
    return $(this);
};

// jQuery plugin to prevent double submission of forms
jQuery.fn.enableSubmission = function() {
    var $form = $(this);

    $form.data('submitted', false);
    $form.find(':submit').removeAttr('disabled');
    $form.find(':submit').css({ 'pointer-events': 'auto', 'opacity': '1' });

};

// Allows to ignore form double submission restriction if was triggered by a specific button
jQuery.fn.allowDoubleSubmission = function() {
    $(this).on("click", function() {
        $(this).closest("form").data('allowDoubleSubmission', true);
        return true;
    });
};


$( "[show-loader]" ).on('click', function(e) {
    var $this = $(this);

    if ($this.attr("type") == "submit" ) {
        var disabled = $this.attr('disabled');
        if (disabled === 'disabled') {
            // Previously submitted - don't submit again
            e.preventDefault();
        } else {
            // Mark this so that the next submit can be ignored
            setTimeout(function() {
                $this.attr('disabled', 'disabled');
            }, 10);
        }
    }

    //var text = $this.attr("show-loader") ? $this.attr("show-loader") :  "Laddar ...";

    //$dataloader = $("#data-loader");
    //$dataloader.find("text").text(text);

    $("#data-loader").slideDown(100);
});

function showLoading(text) {

}

function getMinutes(timeStr, defValue) {
    if (timeStr) {
        var m = timeStr.match(/^(\d{1,2}):(\d{1,2})$/);
        if (m) {
            return parseInt(m[1]) * 60 + parseInt(m[2]);
        }
    }
    return defValue;
}

function getTime(minutes) {
    var hours = Math.floor(minutes / 60);
    return formatHour(hours) + ":" + formatHour(minutes - hours * 60);
}

function formatHour(hour) {
    return (hour > 9 ? hour : "0" + hour);
}

function updateTimeRangeLabel(start, end, timeLabelEl) {
    timeLabelEl.html(getTime(start) + " - " + getTime(end));
}

function removeParam(key, sourceURL) {
    var rtn = sourceURL.split("?")[0],
        param,
        params_arr = [],
        queryString = (sourceURL.indexOf("?") !== -1) ? sourceURL.split("?")[1] : "";
    if (queryString !== "") {
        params_arr = queryString.split("&");
        for (var i = params_arr.length - 1; i >= 0; i -= 1) {
            param = params_arr[i].split("=")[0];
            if (param === key) {
                params_arr.splice(i, 1);
            }
        }
        rtn = rtn + "?" + params_arr.join("&");
    }
    return rtn;
}

function setParam(sourceURL, key, value) {
    var result = removeParam(key, sourceURL);
    return result + (result.indexOf("?") == -1 ? "?" : "&") +
            encodeURIComponent(key) + "=" + encodeURIComponent(value);
}