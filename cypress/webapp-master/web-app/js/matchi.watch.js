$(document).ready(function() {
    $("body").on("click", ".add-activity-watch", function() {
        var occasionId = $(this).attr("data-occasion-id");
        var confirmUrl = $(this).attr("href");
        $.ajax({
            data: {occasionId: occasionId},
            url: confirmUrl,
            success: function(data, textStatus) {
                $('#userBookingModal').html(data);
                showLayer('userBookingModal');
            },
            error: function(jqXHR, textStatus, errorThrown) {
                handleAjaxError(jqXHR, textStatus, errorThrown);
            }
        });
        return false;
    }).on("click", ".remove-activity-watch", function() {
        if (confirm($(this).attr("data-confirm-message"))) {
            var occasionId = $(this).attr("data-occasion-id");
            var watchId = $(this).attr("data-watch-id");
            var removeUrl = $(this).attr("href");
            $.ajax({
                type: "DELETE",
                url: removeUrl + "?id=" + watchId,
                success: function(data, textStatus) {
                    $('#occasion-unwatch_' + occasionId).hide();
                    $('#occasion-watch_' + occasionId).show();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    handleAjaxError(jqXHR, textStatus, errorThrown);
                }
            });
        }
        return false;
    });
});
