sf-massupdater
==============

## Libraries used

* commons-lang3-3.3.2-javadoc.jar
* force-wsc-31.0.0.jar
* partner.jar (generated from partner.wsdl)


### to generate partner.jar

* Libraries: js-1.7R2.jar, ST-4.0.8.jar
* java -classpath force-wsc-31.0.0.jar:js-1.7R2.jar:ST-4.0.8.jar com.sforce.ws.tools.wsdlc partner.wsdl.xml ./partner.jar
