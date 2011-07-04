# Welcome to PAX-WICKET

## Introduction

PAX-WICKET is a small framework which helps to integrate [Apache Wicket](http://wicket.apache.org) 
into the [OSGi](http://www.osgi.org) component framework. PAX-WICKET provides the following features:

* Full integration at HttpService Level
* Bean and Service injection on bundle level from Spring and Blueprint
* A delegating classloader and injection model.

For a full list of features and a more detailed documentation see the 
[PAX-WICKET wiki](http://http://ops4j1.jira.com/wiki/display/paxwicket/Pax+Wicket).

## Get in contact

Since the code is always moving faster than the documentation it is well possible that your use-case is
possible with PAX-WICKET although not documented by know. Feel free to jump on to our [mailing-lists](http://ops4j1.jira.com/wiki/display/ops4j/listinfo)
or [IRC channels](https://ops4j1.jira.com/wiki/display/ops4j/ircinfo) and ask your questions there.

## Quickstart

This is a VERY short summary for developer how to build PAX-WICKET and howto run the samples. For
a full documentation see the [PAX-WICKET wiki](http://http://ops4j1.jira.com/wiki/display/paxwicket/Pax+Wicket).

### Build PAX-WICKET

PAX-WICKET uses [Apache Maven](http://maven.apache.org) as it's build system. Simply checkout the sources and run
"mvn clean install". This will build PAX-WICKET, the samples and run all integration tests.

### Code on PAX-WICKET

PAX-WICKET is developed using Intellij and Eclipse. Either use the plugins in the IDEs or simply run "mvn idea:idea" 
or "mvn eclipse:eclipse" before and import them into your IDE.

### Run the samples

Basically it is as easy as:

* mvn clean install -Dmaven.test.skip=true
* cd samples
* mvn pax:provision
* point the browser to [http://localhost:8080/deptStore](http://localhost:8080/deptStore) or 
[http://localhost:8080/springDeptStore](http://localhost:8080/springDeptStore) or 
[http://localhost:8080/blueprintDeptStore](http://localhost:8080/blueprintDeptStore).

For more detailed examples and descriptions plesae visit the [PAX-WICKET wiki](http://http://ops4j1.jira.com/wiki/display/paxwicket/Pax+Wicket).
