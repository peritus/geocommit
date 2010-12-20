function geogithub() {
  var data = {};

  $('#gitnotes-content').find('h3').each(function(i, el){
      if( 'geocommit' !== $(el).text() && 'geogit' !== $(el).text() ) {
          return;
      }

      var kwblob = $(el).next().text().split('\n');

      $(kwblob).each(function(i, line) {
          if (! line) {
              return;
          }
          var d = line.split(": ");

          data[d[0]] = d[1];
      });

  });

  var point = (data.Latitute || data.lat) + ',' + (data.Longitude || data.long);

  $('#gitnotes-content').find('br').css('clear', 'right');
  $('#gitnotes-content').find('pre').prepend(
    '<img style="float: right;" ' +
    'src="http://maps.google.com/maps/api/staticmap?center=' + point +
    '&zoom=14&size=256x256&format=png&sensor=false&markers=' + point +
    '"/>'
  );

}

geogithub();
