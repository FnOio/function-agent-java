# Function Agent

This library enables execution of semantically described functions.

The idea is that when:
- software A needs a certain functionality, and;
- software B provides that functionality, and;
- there is a description of the functionality and its implementation;
then Function Agent can be called from A to execute functions of B.

Describing functions can be done with the Function Ontology ([FnO.io](https://fno.io/)),
or by providing another function model.

## Function Composition

This library supports [Function composition as described by FnO](https://fno.io/spec/#composition).

If a function is a composition and also has an implementation, the implementation will be used.

If a function is a composition, only the functions whose results are needed for the output will be executed.
If all functions need execution, set the debug flag of the `Agent`.

### Constructing compositions

A composition is represented internally as a lambda of type [ThrowableFunction](./src/main/java/be/ugent/idlab/knows/functions/agent/functionIntantiation/ThrowableFunction.java).
This interface requires an `Agent` to be passed. This is to evaluate the functions the composition depends upon.
The lambda is built at [getCompositeMethod](./src/main/java/be/ugent/idlab/knows/functions/agent/functionIntantiation/Instantiator.java).
This lambda is built based on the composition and is constructed using the following steps:

1. Initialise and do safety checks
2. Construct the execution stack
3. Construct lambdas

#### Initialise and do safety checks

- Check if the lambda is already constructed and is present in the cache.
- Some safety checks (Function exists and is a composition).
- Initialise some maps which are required later.
- Look over all parameter mappings
  - If it's a literal, make it available in a value map for later use.
  - Check if the functions and parameters used exist.
  - Add parameters to a map for mapping the values.
  - If it maps an output, make the target parameter dependent on the source.
- Make a map for global dependencies: if A is dependent on B and B is dependent on C, then C is a global dependency of A.
- Check the dependencies for cycles

#### Construct execution stack

There is a function dependency _queue_ and an execution _stack_.

- initialization
  - the execution stack is initialized empty
  - the function dependency queue is initialized with the function of which the composition is calculated.
- construct the execution stack ([BFS](https://en.wikipedia.org/wiki/Breadth-first_search) based) (will only add functions necessary to calculate output):
  - repeat until queue is empty
    - Poll a function from the queue and get its dependencies.
      - > TODO clarify what 'dependencies' means
    - If the function is contained in another functions dependencies, add it to the queue again.
    - Push it to the execution stack.
      - > TODO clarify what 'it' means
    - Add the function's dependencies to the queue.
- postprocess
  - normal mode
    - remove the last function on the stack, this is the composite function and can't be executed.
  - debug mode: add all functions in the composition, also the non-required ones for the function output.
    - remove the last element in the deque (original function)
    - get all elements in the composition that aren't on the execution stack yet.
    - Queue based adding: check if the functions dependencies are all on the stack. If yes, add it at the end of the execution stack, else place it at the back of the queue.
    - Add removed function again.

#### Lambda construction

- Make the arguments available in the value map.
- execute the execution stack:
  - Repeat until stack is empty.
  - Take a function from the stack and get its `Function` object.
  - for each of the functions parameters, we will get all the parameters of which it receives values, and get their values from the valueMap.
  - Execute the function with the provided `Agent` and make its result available in the valueMap.
- Get the output of the parameters linked to the output of the composite function and return the first value.
- Cache the constructed lambda.

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

```turtle
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

ex:sumMapping a fno:Mapping ;
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
        <version>v0.2.0</version>
    </dependency>
</dependencies>

```

See also [AgentTest.java](src/test/java/be/ugent/idlab/knows/functions/agent/AgentTest.java).

## Features

- Use the [Function Ontology](https://fno.io/) to describe functions, or implement your own [FunctionModelProvider](src/main/java/be/ugent/idlab/knows/functions/agent/functionModelProvider/FunctionModelProvider.java).
- Include function implementations (see [AgentTest](src/test/java/be/ugent/idlab/knows/functions/agent/AgentTest.java)) for an example of the options):
  - As classes on the classpath (e.g. by using your favorite build tool)
  - By referring to a JAR file.
- Lazy loading of function implementations.
- Use FnO composition.

## Current limitations

- No state supported.
- Implemented in Java, no bindings for non-JVM languages provided.
- Composition: no recursion supported.
