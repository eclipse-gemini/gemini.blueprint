Eclipse Gemini Blueprint
------------------------
http://www.eclipse.org/gemini/blueprint

1. INTRODUCTION

Eclipse Gemini Blueprint is the reference implementation for the OSGi Alliance Blueprint Service 
(chapter 121 of the OSGi 4.2 Compendium Specification).

Gemini Blueprint project makes it easy to build Java applications that run in an OSGi framework.
By using Gemini Blueprint, applications benefit from using a better separation of modules, the
ability to dynamically add, remove, and update modules in a running system, the ability to deploy
multiple versions of a module simultaneously (and have clients automatically bind to the 
appropriate one), and a dynamic service model. 

For enterprise applications, we consider that Eclipse Gemini Blueprint offers the following benefits:

    * Better separation of application logic into modules
    * The ability to deploy multiple versions of a module concurrently
    * The ability to dynamically discover and use services provided by other modules in 
        the system
    * The ability to dynamically deploy, update and undeploy modules in a running system
    * Use of the Spring Framework to instantiate, configure, assemble, and decorate components 
        within and across modules.
    * A simple and familiar programming model for enterprise developers to exploit the
        features of the OSGi platform.

We believe that the combination of OSGi and Spring (as the underlying IoC container) offers the most
comprehensive model available for building enterprise applications.

It is not a goal of Gemini Blueprint to provide a universal model for the development
of any OSGi-based application, though some OSGi developers may of course find the Spring model
attractive and choose to adopt it. Existing OSGi bundles and any services they may export are
easily integrated into applications using the Eclipse Gemini Blueprint framework, as are existing Spring
configurations.

2. RELEASE INFO

The Eclipse Gemini Blueprint is targeted at OSGi R4 and above, and JDK level 5.0 and above.

Release contents:
* "src" contains the Java source files for the framework
* "dist" contains various Eclipse Gemini distribution jar files
* "docs" contains general documentation and API javadocs

Maven 2 pom.xml are provided for building the sources.
 
Latest info is available at the public website: http://www.eclipse.org/gemini/blueprint

Eclipse Gemini Blueprint is released under the terms of the Eclipse Public License v1.0 and 
the Apache Software License. The Eclipse Public License is available at 
http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0 is available at 
http://www.opensource.org/licenses/apache2.0.php. Additionally, they are included in file license.txt.
You may elect to redistribute this code under either of these licenses.

This product includes software developed by the Apache Software Foundation (http://www.apache.org) and 
OSGi Alliance (http://www.osgi.org).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list specifies the respective contents and
third-party dependencies. Libraries in [brackets] are optional, i.e. just necessary for certain functionality. For an 
exact list of Eclipse Gemini Blueprint project dependencies see the respective Maven2 pom.xml files.

* gemini-blueprint-core-${version}.jar
- Contents: The Eclipse Gemini Blueprint Core
- Dependencies: slf4j, spring-aop, spring-beans, spring-core, spring-context, aop-alliance, gemini-blueprint-io
                [Log4J]

* gemini-blueprint-extender-${version}.jar
- Contents: The Eclipse Gemini Blueprint Extender
- Dependencies: sl4fj, gemini-blueprint-core
			    [Log4J, gemini-blueprint-annotation]

* gemini-blueprint-io-${version}.jar
- Contents: The Eclipse Gemini Blueprint IO library
- Dependencies: sl4fj, spring-core
                [Log4J]

* gemini-blueprint-mock-${version}.jar
- Contents: The Eclipse Gemini Blueprint Mock library
- Dependencies: OSGi API

* gemini-blueprint-test-${version}.jar
- Contents: The Eclipse Gemini Blueprint Integration Testing framework
- Dependencies: asm, junit, slf4j, gemini-blueprint-core, gemini-blueprint-extender
                [Equinox, Felix, Knopflerfish, Log4J]

4. WHERE TO START

This distribution contains API documentation and several sample applications illustrating the current features of Gemini Blueprint.
The Eclipse Gemini Blueprint reference documentation can be found at http://www.eclipse.org/gemini/blueprint

A great way to get started is to review and run the sample applications, supplementing with reference manual
material as needed. You will require Maven 2.0.x, which can be downloaded from http://maven.apache.org/, for building
Gemini Blueprint.

5. ADDITIONAL RESOURCES

The Eclipse Gemini Blueprint homepage is located at:

    http://www.eclipse.org/gemini/blueprint

The Spring Framework portal is located at:

	http://www.springframework.org