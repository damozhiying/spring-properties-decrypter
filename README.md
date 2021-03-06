[![Coverage Status](https://coveralls.io/repos/lukashinsch/spring-properties-decrypter/badge.svg?branch=master)](https://coveralls.io/r/lukashinsch/spring-properties-decrypter?branch=master)
[![Build Status](https://travis-ci.org/lukashinsch/spring-properties-decrypter.svg?branch=master)](https://travis-ci.org/lukashinsch/spring-properties-decrypter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/eu.hinsch/spring-properties-decrypter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/eu.hinsch/spring-properties-decrypter/)

# spring-properties-decrypter
Transparently decrypt property values to be used in spring environment using [jasypt](http://www.jasypt.org/) library.

# Howto use

## Gradle dependency
```
runtime('eu.hinsch:spring-properties-decrypter:0.1.9')
```

## Maven dependency
```
<dependency>
  <groupId>eu.hinsch</groupId>
  <artifactId>spring-properties-decrypter</artifactId>
  <version>0.1.9</version>
</dependency>
```

## Decryption password
Set decryption key via environment or system property
```
propertyDecryption.password=SECRETPASSWORD
```

## Encrpted properties
Define any spring environment property anywhere it can be defined (application*.properties/yaml, system, environment, command line...) and insert encrypted value like this:
```
myProperty={encrypted}ENCRYPTEDVALUERAWDATA
```

## Alternative prefix
To use a prefix other than '{encrypted}' define via
```
propertyDecryption.prefix=MY-PREFIX
```

## Encryption algorithm
By default uses PBEWithMD5AndDES (to allow running in default JRE). Note that this encryption is (at least with lots of dedicated resources) breakable. Stronger encryption is currently not working due to a bug in the underlying library.

To configure alternative algorithm:
```
propertyDecryption.algorithm=ALGORITHMNAME
```

## Howto encrypt passwords
Download the jasypt distribution from http://www.jasypt.org/download.html
Run from the bin folder
```
encrypt [verbose=true] algorithm=PBEWithMD5AndDES saltGeneratorClassName=org.jasypt.salt.ZeroSaltGenerator password=... input=...
```
For more details http://www.jasypt.org/cli.html
