The following libraries are used by the Gemini Blueprint distribution either for building
or running the framework and its samples. 

Most (if not all) libraries are available from SpringSource Enterprise Bundle Repository:
http://www.springsource.com/repository/app/

Note that each
of these libraries is subject to the respective license; check the respective project
distribution/website before using any of them in your own applications.

* aopalliance.jar
- AOP Alliance 1.0 (http://aopalliance.sourceforge.net)
- required for building the framework

* asm.jar
- ObjectWeb ASM bytecode library 2.2.3 (http://asm.objectweb.org)
- required for building the testing framework
- required for running the framework's test suite

* catalina.jar
- Apache Tomcat 5.5.23 (http://tomcat.apache.org)
- required for building and (optionally) running Spring-DM web support

* cglib-nodep-2.1_3.jar
- CGLIB 2.1_3 with ObjectWeb ASM 1.5.3 (http://cglib.sourceforge.net)
- required at runtime when proxying full target classes via Spring AOP

* easymock.jar
- EasyMock 1.2 (JDK 1.3 version) (http://www.easymock.org)
- required for building and running the framework's test suite

* framework.jar
- Knopflerfish 3.x OSGi platform implementation (http://www.knopflerfish.org) 
- required for building and running the framework's test suite
 
* jcl104-over-slf4j.jar
- SLF4J 1.5.6 Jakarta Commons Logging wrapper (http://www.slf4j.org)
- required for building and running the framework's test suite

* jetty-6.1.9.jar
- Jetty 6.1.9 (http://jetty.mortbay.org)
- required for building and (optionally) running Spring-DM web support

* jetty-util-6.1.9.jar
- Jetty Util 6.1.9 (http://jetty.mortbay.org)
- required for building and (optionally) running Spring-DM web support
  
* junit-3.8.2.jar
- JUnit 3.8.2 (http://www.junit.org)
- required for building and running the framework's test suite

* log4j-1.2.15.jar
- Log4J 1.2.15 (http://logging.apache.org/log4j)
- required for building running the framework's test suite

* multithreadedtc.jar
- MultithreadedTC framework 1.01 (http://code.google.com/p/multithreadedtc)
- required for running the framework's test suite

* org.apache.felix.main.jar
- Apache Felix 2.x OSGi platform implementation (http://felix.apache.org)
- required for building and running the framework's test suite

* org.eclipse.osgi.jar
- Eclipse Equinox 3.5.x OSGi platform implementation (http://www.eclipse.org/equinox)
- required for building and running the framework's test suite

* osgi_R4_compendium.jar
- OSGi Compendium API 1.0 (http://www.osgi.org)
- required for building and running the framework's test suite

* servlet-api-2.5.jar
- Servlet API version 2.5 (http://java.sun.com/products/servlet/)
- required for building and running Spring-DM web support
- compiled against JDK 1.4

* slf4j-api.jar
- SLF4J API 1.5.6 (http://www.slf4j.org)
- required for building and running the framework's test suite

* slf4j-log4j.jar
- SLF4J 1.5.6 adapter for log4j (http://www.slf4j.org) 
- required for running the framework's test suite

* spring-aop.jar
- Spring Framework 3.0.x AOP library (http://www.springframework.org)
- required for building and running the framework's test suite

* spring-beans.jar
- Spring Framework 3.0.x beans library (http://www.springframework.org)
- required for building and running the framework's test suite

* spring-context.jar
- Spring Framework 3.0.x context library (http://www.springframework.org)
- required for building and running the framework's test suite

* spring-context-support.jar
- Spring Framework 3.0.x context support library (http://www.springframework.org)
- required for building and running the framework's test suite

* spring-core.jar
- Spring Framework 3.0.x core library (http://www.springframework.org)
- required for building and running the framework's test suite

* spring-test.jar
- Spring Framework 3.0.x test library (http://www.springframework.org)
- required for building and running the framework's test suite

* spring-web.jar
- Spring Framework 3.0.x web library (http://www.springframework.org)
- required for running the samples and the framework's test suite

* spring-webmvc.jar
- Spring Framework 3.0.x mvc library (http://www.springframework.org)
- required for running the samples and the framework's test suite
