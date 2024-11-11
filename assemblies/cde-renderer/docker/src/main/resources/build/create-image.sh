#!/bin/bash
# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2029-07-20
# ******************************************************************************

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
