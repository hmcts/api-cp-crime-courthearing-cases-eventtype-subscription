# Dependencies Notes
The gradle.build pulls in various dependencies that are required to compile the openapi spec yml file 
into java objects and Api interface

The Api interface definition created is a spring interface and must be implemented by spring controllers

Therefore consuming clients must provide their own spring implementation


## Swagger Annotations
We add swagger annotations to the dependency list so that consuming clients will automatically pull in
implementation "io.swagger.core.v3:swagger-annotations:$version"

This is required to prevent errors such as
reason: class file for io.swagger.v3.oas.annotations.media.Schema$RequiredMode not found

This is the only dependency that we want to expose to the consuming client

Any other dependencies that we need for build, we set as compileOnly to ensure the consuming client does not pull them in
which may cause conflicts 

## Debugging dependencies Locally
When the artifact is published to gradle repository we end up with a jarfile and a pom file that contains dependencies
The files can be seen in .gradle i.e.
```
find ~/.gradle -name \*.pom -ls |grep api-cp-crime | grep subscription
... finds api-cp-crime-hearing-results-document-subscription-2.0.5.pom

find ~/.gradle -name \*.jar -ls |grep api-cp-crime | grep subscription
... finds api-cp-crime-hearing-results-document-subscription-2.0.5.jar
```

To locally create a pom file showing the dependencies that will be added to the published artifact
```
gradle generatePomFileForMavenJavaPublication
```

This will create a pom file such as
./build/publications/mavenJava/pom-default.xml
This can be converted to a gradle file and imported directly into the consuming service to pull in dependencies directly
This may help local testing of any conflicts
Convert pom to gradle at site such as https://sagioto.github.io/maven2gradle/

i.e. build.gradle may pull in the local jarfile and dependencies
```
  implementation(files("../api-cp-crime-hearing-results-document-subscription/./build/libs/api-cp-crime-hearing-results-document-subscription-0.0.999.jar"))
  
  implementation "io.swagger.core.v3:swagger-annotations:2.2.41"
```