Geocommit allows you to attach geolocation information to your commits for
later analysis.

Everything happens on your machine to protect your privacy, you you need to push the geo annotations explicitly if you want to share them with others.

Installation
============

Use pip (or easy_install)::

    pip install --upgrade geocommit

(If you don't have pip yet, use ``easy_install pip`` to get it.)

Usage
=====

Geo-enable a repository (installs a few hooks that enable keep track of your location for this repository)::

    git geo setup

Attach geo-information to HEAD::

    git geo note

Fetch geolocation information from remote::

    git geo fetch

Fetch and merge geolocation information::

    git geo sync

Developing
==========

::

    git clone https://github.com/peritus/geocommit.git
    python bootstrap.py
    ./bin/buildout

Crazy ideas
===========

* foursquare integration
* 3rd party service with badges:
  * jetsetter badge (commits at five different airports)
  * mountain badge (commits at over 4000ft altitude)
* geocommit + git-remote-couch + geocouch === awesome!
* git geolog foo..bar > foobar.kml
* Chrome/Safari extension to display google maps all over github

Geocommit data format (v1.0)
============================
We store a number of keys with values in git notes or hg commits.
There is a long and a short format. Both define a set of key/value
pairs in no particular order. The format version defines the allowed
keys.

<version> is a version number of the format x.y
<key> is an alphanumeric lowercase identifier without spaces or other special characters except _ and -
<value> must not contain a linebreak, "," or ";"

The short format is:
geocommit(<version>): <key> <value>, ..., <key> <value>;

The long format is, terminated by an empty line:

geocommit (<version)
<key>: <value>
...
<key>: <value>

Version 1.0 of the format defines the keys:
 * long (required) contains longitude value of a coordinate in WGS84
 * lat (required) contains latitude value of a coordinate in WGS84
 * src (required) contains the name of the data provider used to generate the geodata
 * alt (optional) contains altitude in metres
 * speed (optional) speed in metres / second
 * dir (optional) direction of travel
 * hacc (optional) horizontal accuracy of long/lat values in metres
 * vacc (optional) vertical accuracy of altitude value in metres

Authors
=======

Geocommit is a crazy idea that was initially hacked together on an even crazier
weekend in December 2010 in Berlin.

 * Nils Adermann (cli client, linux support, website, web service, github hook)
 * Filip Noetzel (original idea, cli client, python packaging, initial Mac OS X support, chrome extension)
 * David Soria Parra (website, mercurial support, web service, bitbucket hook)
 * Andrew Wooster (significant Mac OS X improvements)
