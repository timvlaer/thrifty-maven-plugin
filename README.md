# thrifty-maven-plugin

Advantages of using Thrifty over regular Thrift code generation:
* All compilation happens on the JVM, no need to install Thrift binaries.
* Thrifty generates better java code: immutable objects with builders.

## Usage

Add the following plugin to the `<build>` part of your Maven pom.xml file
```xml
<plugin>
    <groupId>com.github.timvlaer</groupId>
    <artifactId>thrifty-maven-plugin</artifactId>
    <version>0.2.0</version>
    <configuration>
        <thriftFiles>
            <file>thrift-schema/internal.thrift</file>
        </thriftFiles>
        <enableConvenienceMethods>true</enableConvenienceMethods>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>thrifty-compiler</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The generated code depends on `thrifty-runtime`, so add the following to your dependency list. 
```xml
<dependency>
    <groupId>com.microsoft.thrifty</groupId>
    <artifactId>thrifty-runtime</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Generated convenience methods
This Maven plugin adds two methods to classes that are based on Thrift `union` types  if you set `<enableConvenienceMethods>true</en...`.
* `public String tag()` which returns the name of the filled field
* `public Object value()` which returns the value of the filled field (untyped)