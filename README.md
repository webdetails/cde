# Community Dashboard Editor

**CDE** is a plugin that allows to create, edit and render dashboards

**CDE** is one of the _tools_ of the **CTools** family and it is shipped with the Pentaho Server

#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

This is a maven project, to build it use the following command
```
mvn clean install
```
The build result will be a Pentaho Plugin located in *assemblies/platform/pentaho-cdf-dd/target/pentaho-cdf-dd-**.zip*. This package can be unzipped and dropped inside your Pentaho Server system folder.


For issue tracking and bug report please use http://jira.pentaho.com/browse/CDE.
