SPRING DYNAMIC MODULES FOR OSGI(tm) SERVICE PLATFORMS 
-----------------------------------------------------
http://www.springframework.org/osgi

1. Spring DM BUILDING REQUIREMENT

Spring DM 2.x requires at least JDK 1.5, Spring 3.x and Maven 2 for building.
Spring DM 1.x requires at least JDK 1.4, Spring 2.5.x and Maven 2 for building.
Currently, Maven 2.0.10 is used for building the framework.

1. BUILDING Spring DM

At the moment, Spring Dynamic Modules uses Maven 2 to handle the building
process. Since Spring DM runs on multiple OSGi platforms and can be
compiled on various JDKs (currently Sun 1.4, 1.5 and 1.6 have been tested),
Maven profiles have been used to allow the selection of the building
environment.

For more info on Maven profiles, please see this page: 
http://maven.apache.org/guides/introduction/introduction-to-profiles.html

1a. Selecting OSGi platform

The following Maven profiles are available for selecting an OSGi platform:

equinox - Equinox 3.5.x
knopflerfish - Knopflerfish 2.0.x/2.1.x/2.2.x/3.x
felix - Apache Felix 1.0.x/1.4.x/2.x

The OSGi platform should be always specified otherwise the project will not compile.
We recommend that new users try building using Eclipse Equinox platform which is
considered the default platform.

1b. Running the integration tests

By default the project builds only the distributable modules without running any
integration tests. To run them, one should select the 'it' profile.
Note that 1a) applies, so an OSGi platform still has to be specified:

# mvn -P equinox,it clean install

1c. Running the samples

To compile and install the samples, use the 'samples' profile:

# mvn -P equinox,samples clean install