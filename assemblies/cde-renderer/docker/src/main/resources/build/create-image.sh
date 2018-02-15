#!/bin/bash

#Build the docker image
docker build -t ${docker.image.tag} .

if [[ -d ${docker.image.output.directory} ]] ; then
    rm -rf ${docker.image.output.directory}
fi

mkdir ${docker.image.output.directory}

#Create the docker image tar
docker save -o ${docker.image.output.file}.tar ${docker.image.tag}

#Gzip the docker tarball
gzip -f ${docker.image.output.file}.tar
chmod a+r ${docker.image.output.file}.tar.gz
