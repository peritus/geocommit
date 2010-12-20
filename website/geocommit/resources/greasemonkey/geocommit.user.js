// ==UserScript==
// @name Geocommit Script
// @namespace http://geocommit.com/
// @description This script visualizes geocommits on github and bitbucket
// @include https://github.com/*
// @include http://github.com/*
// @include https://bitbucket.org/*
// @include http://bitbucket.org/*
// @require http://www.geocommit.com/greasemonkey/jquery-1.4.4-greasemonkey.js
// ==/UserScript==

function getGeocommitLink() {
    return '<a href="http://www.geocommit.com" title="geocommit">geocommit</a>';
}

function getMapHtml(point, width, height, style) {

    if (style) {
        style = 'style="' + style + '" ';
    }
    else {
        style = '';
    }

    return '<a href="http://maps.google.com/maps?q=' + point +
        '" title="geocommit location"><img ' + style +
        'src="http://maps.google.com/maps/api/staticmap?center=' + point +
        '&zoom=14&size=' + width + 'x' + height +
        '&format=png&sensor=false&markers=' + point +
        '"/></a>';
}

function geocommitVisualize() {
    var data = {};

    // github
    if ($('#gitnotes-content').length) {

        $('#gitnotes-content').find('h3').each(function(i, el) {
            if ('geocommit' !== $(el).text() && 'geogit' !== $(el).text()) {
                return;
            }

            var geostr = $(el).next().text().substr('geocommit (1.0)\n'.length);;
            var kwblob = geostr.split('\n');

            $(kwblob).each(function(i, line) {
                if (! line) {
                    return;
                }
                var d = line.split(": ");

                data[d[0]] = d[1];
            });

            $(el).next().text(geostr)
            $(el).next().prepend(getGeocommitLink() + ' location: \n');

        });

        var point = (data.Latitute || data.lat) + ',' + (data.Longitude || data.long);

        $('#gitnotes-content').find('br').css('clear', 'right');
        $('#gitnotes-content').find('pre').prepend(
            getMapHtml(point, 512, 256, 'float: right;')
        );
    }
    // bitbucket
    else if ($('#source-summary').length) {

        $('#source-summary').find('p').each(function(i, el) {
            if (!$(el).text().match('^geocommit')) {
                return;
            }

            var geostr = $(el).text().substr('geocommit(1.0): '.length);
            var kwblob = geostr.split(', ');

            $(kwblob).each(function(i, keyval) {
                if (! keyval) {
                    return;
                }
                var d = keyval.split(" ");

                data[d[0]] = d[1];
            });

            $(el).text(geostr);
            $(el).prepend(getGeocommitLink() + ' location: ');

        });

        var point = data.lat + ',' + data.long;

        $('#source-summary dl.relations').append(
            '<dt>location</dt><dd>' + getMapHtml(point, 256, 192, null) + '</dd>'
        );
    }
}

geocommitVisualize();
