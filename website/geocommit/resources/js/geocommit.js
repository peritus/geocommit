function geocommit_map(z) {
    var c = new google.maps.LatLng(30, -10);
    var m = new google.maps.Map(
            document.getElementById("map_canvas"),
            {zoom: z,
             center: c,
             mapTypeId: google.maps.MapTypeId.SATELLITE});
    $.ajax({url: "/api/geocommits",
            type: "GET",
            success: function(rows, stat, req) {
                rows = jQuery.parseJSON(rows);
                var marker = []
                var info = []
                $.each(rows, function() {
                    var doc = this.value;
                    var pos = new google.maps.LatLng(doc.latitude, doc.longitude);

                    var marker = new google.maps.Marker({
                        map: m,
                        title: doc.commit,
                        position: pos});
                    google.maps.event.addListener(marker, 'click', function () {
                        new google.maps.InfoWindow({
                            content: "<b>Author</b>: " + doc.author
                            + "<br /><br />"
                            + doc.message.replace("\n","<br />")
                            + "<br /><a href=\"http://" + doc.repository + "\">http://" + doc.repository + "</a>"}).open(m, this)});
                });
            }
    });
}

function geocommit_signup () {
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
        $('#indicator').fadeIn(500);
        $.ajax({
            url: "/signup/",
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
}
