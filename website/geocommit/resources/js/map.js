function geocommit_map() {
    var c = new google.maps.LatLng(0, 0);
    var m = new google.maps.Map(
            document.getElementById("map_canvas"),
            {zoom: 2,
             center: c,
             mapTypeId: google.maps.MapTypeId.TERRAIN});
    var marker = [];
    $.ajax({url: "/api/geocommits",
            type: "GET",
            success: function(rows, stat, req) {
                rows = jQuery.parseJSON(rows);
                var marker = []
                for (i in rows) {
                    var doc = rows[i].value;
                    marker[doc.id] = new google.maps.Marker(
                        {map: m,
                        title: doc.commit,
                        position: new google.maps.LatLng(doc.latitude, doc.longitude)});
                }
            }
    }); 
}
