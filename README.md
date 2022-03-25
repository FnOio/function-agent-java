# Function Agent

This library enables execution of semantically described functions.

The idea is that when:
* software A needs a certain functionality, and;
* software B provides that functionality, and;
* there is a description of the functionality and its implementation;
then Function Agent can be called from A to execute functions of B.

Describing functions can be done with the Function Ontology ([FnO.io](https://fno.io/)),
or by providing another function model.

## Example

Suppose you have this great library with a function you want to use in your code, 
in this case the method `sum` that sums parameters `a` and `b`.
(see [InternalTestFunctions.java](src/test/java/be/ugent/idlab/knows/functions/internalfunctions/InternalTestFunctions.java))


```java
public class InternalTestFunctions
{
    /**
     * Returns the sum of a and b
     * @param a your lucky number
     * @param b my lucky number
     * @return  our lucky number (a + b)
     */
    public static long sum(long a, long b) {
        return a + b;
    }
}

```

And you have a FnO document describing that function and its implementation (see [internalTestFunctions.ttl](src/test/resources/internalTestFunctions.ttl)):

```
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix fno:     <https://w3id.org/function/ontology#> .
@prefix fnoi:    <https://w3id.org/function/vocabulary/implementation#> .
@prefix fnom:    <https://w3id.org/function/vocabulary/mapping#> .
@prefix ex:      <http://example.org/> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xs:      <http://www.w3.org/2001/XMLSchema#> .

# Function description

ex:sum a fno:Function ;
  fno:name "Sum of two integers" ;
  rdfs:label "Sum of two integers" ;
  dcterms:description "Returns the sum of two given integers" ;
  fno:expects ( ex:int1 ex:int2 ) ;    # The function expects these two input parameters
  fno:returns ( ex:intOut ) .          # ... and returns this output parameter.

# Parameter descriptions:

ex:int1 a fno:Parameter ;
    fno:name "integer 1" ;             # name of the parameter
    rdfs:label "integer 1" ;
    fno:predicate ex:p_int1 ;          # The id of the parameter used in the code calling the function
    fno:type xs:integer ;              # The data type of the parameter.
    fno:required "true"^^xs:boolean .  # In this case the parameter is mandatory.

ex:int2 a fno:Parameter ;
    fno:name "integer 2" ;
    rdfs:label "integer 2" ;
    fno:predicate ex:p_int2 ;
    fno:type xs:integer ;
    fno:required "true"^^xs:boolean .

ex:intOut a fno:Output ;
    fno:name "integer output" ;
    rdfs:label "integer output" ;
    fno:predicate ex:o_int ;
    fno:type xs:integer .

# Method mapping: maps the function to an actual implementation

ex:sumMapping a fnoi:Mapping ;
  fno:function ex:sum ;                             # The function being mapped
  fno:implementation ex:internalTestFunctions ;     # A description of the implementation; see below
  fno:methodMapping [ a fnom:StringMethodMapping ; fnom:method-name "sum" ] . # The method "sum" implements the ex:sum function 

# Implementation

ex:internalTestFunctions a fnoi:JavaClass ;         # The method is part of a Java class
  fnoi:class-name "be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions" .  #... with this name.
```

Now we first need to initialize a Function Agent with a path to the FnO document. 
This path can be local or a URL to such document, or a big String containing the document.
Then the function can be called with the required arguments:

```java
import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;

public static void main(String[] args) {
    // Initialize a function agent
    Agent agent = AgentFactory.createFromFnO("src/test/resources/internalTestFunctions.ttl");

    // prepare the parameters for the function
    Arguments arguments = new Arguments()
        .add("http://example.org/p_int1", "5")
        .add("http://example.org/p_int2", "1");
    // execute the function
    long result = (Long) agent.execute("http://example.org/sum", arguments);
    assert (result == 6l);
}


```

## Download

### Jar file
Grab the latest [release](https://github.com/FnOio/function-agent-java/releases) from GitHub.

### Maven
Use [JitPack](https://jitpack.io/) to include the latest version. In your `pom.xml` add
the following repository:

```xml

<repositories>
    ...
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

then add this dependency:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.fnoio</groupId>
        <artifactId>function-agent-java</artifactId>
        <version>v0.0.1</version>
    </dependency>
</dependencies>

```

See also [AgentTest.java](src/test/java/be/ugent/idlab/knows/functions/agent/AgentTest.java).

## Features
* Use the [Function Ontology](https://fno.io/) to describe functions, or implement your own [FunctionModelProvider](src/main/java/be/ugent/idlab/knows/functions/agent/functionModelProvider/FunctionModelProvider.java);
* Include function implementations (see [AgentTest](src/test/java/be/ugent/idlab/knows/functions/agent/AgentTest.java)) for an example of the options):
  * As classes on the classpath (e.g. by using your favorite build tool)
  * By referring to a JAR file.
* Lazy loading of function implementations. 

## Current limitations
* Does not support FnO's [function composition](https://fno.io/spec/#composition) yet.
* No state supported.
* Implemented in Java, no bindings for non-JVM languages provided.
