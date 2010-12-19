$(document).ready(function() {
    var resetted = false;
    var mdialog = $('<div></div>')
    .html('This dialog will show every time!')
    .dialog({
        autoOpen: false,
        title: "Registration",
        buttons: { "Ok":
            function() {
                $(this).dialog("close");
            }
        }
    });

    function regsuccess(data, stat, req) {
        if (req.status == 201) {
            mdialog.html('Thank you for registering. Please check your mail to verify your email address.');
            mdialog.dialog('open');
            $('#inviteFormBox').fadeOut(1000, function() {
                $('#inviteFormBox').empty();
                });
            $('#indicator').fadeOut(500);
        } else {
            regfail();
        }
    }

    function regfail() {
        mdialog.html('Cannot register. Please verify your email address or try again later.');
        mdialog.dialog('open');
        $('#indicator').fadeOut(500);
    }

    $('#invite').click(function() { 
        $('#indicator').css('visibility', 'visible');
        $.ajax({
            url: "http://hooks.geocommit.com/signup/",
            type: "POST",
            context: document.body,
            data: {mailaddr: $('#mail').val()},
            success: regsuccess,
            error: regfail
        });
    });

    $('#mail').click(function() {
        if (!resetted) {
            $('#mail').val("");
            resetted = true;
        }
    });
});
