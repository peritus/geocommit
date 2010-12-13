
function geogithub() {
  var data = {};

  $('#gitnotes-content').find('h3').each(function(i, el){
      console.log(arguments);

      if( 'geogit' !== $(el).text() ) {
          return;
      }

      console.log(el);

      var kwblob = $(el).next().text().split('\n');

      $(kwblob).each(function(i, line) {
          if (! line) {
              return;
          }
          var d = line.split(": ");

          console.log(line);

          data[d[0]] = d[1];
      });

  });

  console.log(data);

  /*
  *   <h3/>
  *   <img style=float: right;/>
  *   <pre/>
  *   <br style=clear: right;/>
  */

  $('#gitnotes-content').append(
    '<img style="float: right;" ' +
    'src="http://maps.google.com/maps/api/staticmap?center=' +
    data.Latitute + ',' + data.Longitude +
    '&zoom=14&size=256x256&format=png&sensor=false&markers=' +
    data.Latitute + "," + data.Longitude +
    '"/>'
  );

}

geogithub();

