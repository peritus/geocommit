#!/usr/bin/env bash

echo "Warning this script will delete your geocommit and geocommit-web databases in couchdb on localhost:5984"
read -s -p "Password for user geo@dsp.couchone.com: " passwd;
echo

localdb="http://localhost:5984"
remotedb="http://geo:$passwd@dsp.couchone.com"

echo "Deleting geocommit and geocommit-web databases on $localdb"
curl -X DELETE $localdb/geocommit > /dev/null 2>&1
curl -X DELETE $localdb/geocommit-web > /dev/null 2>&1

echo "Creating geocommit and geocommit-web databases on $localdb"
curl -X PUT $localdb/geocommit
curl -X PUT $localdb/geocommit-web

echo "Replicating geocommit database"
curl -X POST $localdb/_replicate -H"Content-Type: application/json" -d"{\"source\":\"$remotedb/geocommit\",\"target\":\"$localdb/geocommit\"}"
echo "Replicating geocommit-web database"
curl -X POST $localdb/_replicate -H"Content-Type: application/json" -d"{\"source\":\"$remotedb/geocommit-web\",\"target\":\"$localdb/geocommit-web\"}"

