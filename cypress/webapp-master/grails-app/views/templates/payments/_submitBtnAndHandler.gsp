<g:submitButton id="btnSubmit" name="next" class="btn btn-md btn-success"
        value="${btnText ?: message(code: 'button.next.label')}"/>

<script type="text/javascript">
    $(function() {
        $("#confirmForm").submit(function() {
            onLoading($("#btnSubmit"));
            $.ajax({
                type: "POST",
                data: $(this).serialize(),
                url: "${createLink(controller: targetController, action: targetAction ?: 'pay')}",
                success: function (data) {
                    $('#userBookingModal').html(data);
                },
                error: function (jqXHR) {
                    if (jqXHR.status == 401) {
                        location.href = "${createLink(controller: 'login', action: 'auth',
                                params: [returnUrl: createLink(controller: 'facility', action: 'show',
                                        params: [name: facility.shortname])])}";
                    }
                }
            });
            return false;
        });
    });
</script>