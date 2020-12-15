function updateTranslatableLang(langEl) {
    var inp = langEl.closest(".translatable-field").find("textarea");
    var name = inp.attr("name");
    inp.attr("name", name.substr(0, name.length - 4) + "[" + langEl.val() + "]");
}

function disableAddButton(wrapperEl) {
    var tf = wrapperEl.find(".translatable-field");
    if (tf.length == 5) {
        wrapperEl.parent().find(".add-translatable").addClass("disabled");
    }
}

function disableRemoveButton(wrapperEl) {
    var tf = wrapperEl.find(".translatable-field");
    if (tf.length == 1) {
        tf.find(".remove-translatable").addClass("disabled");
    }
}

$(function() {
    $(".translatable-wrapper").each(function() {
        disableAddButton($(this));
        disableRemoveButton($(this));
    });

    $("form").on("click", ".remove-translatable", function() {
        var wrapper = $(this).closest(".translatable-wrapper");
        $(this).closest(".translatable-field").remove();
        wrapper.parent().find(".add-translatable").removeClass("disabled");
        disableRemoveButton(wrapper);
    }).on("change", ".translatable-field select", function() {
        updateTranslatableLang($(this));
    });

    $(".add-translatable").on("click", function() {
        var wrapper = $(this).parent().parent().find(".translatable-wrapper");
        var tfields = wrapper.find(".translatable-field");
        if (tfields.length < 5) {
            var tf = $(tfields[0]).clone();

            var selectedLangs = [];
            tfields.find("select").each(function () {
                selectedLangs.push($(this).val());
            });

            var options = tf.find("select option").filter(function (i, el) {
                return selectedLangs.indexOf($(el).attr("value")) == -1;
            });
            $(options[0]).prop("selected", true);

            tf.find(".textarea-wrapper").html("");
            tf.find(".textarea-wrapper").append($(tfields[0]).find(".textarea-wrapper textarea").clone());
            tf.find("textarea").val("").show();

            updateTranslatableLang(tf.find("select"));

            wrapper.append(tf);

            tf.find("textarea").focus();
            tf.find("textarea").wysihtml5({
                stylesheets: [wysihtml5Stylesheets]
            });

            wrapper.find(".remove-translatable").removeClass("disabled");
            disableAddButton(wrapper);
        }
    });
});