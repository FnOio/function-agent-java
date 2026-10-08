# Function Agent (function-agent-java) Handbook

Written for a CS student who wants to understand the Function Agent as code.

## Contents

- [Preface](#preface)
- [Agent request contract (for AI agents/LLMs)](#agent-request-contract-for-ai-agentsllms)
- [Architecture and package layout](#architecture-and-package-layout)
- [Build and test](#build-and-test)
- [Test resources](#test-resources)
- [Release process](#release-process)

## Preface

The Function Agent is a Java library that executes semantically described functions. A function description (by default in the Function Ontology, [FnO](https://fno.io/)) states what a function expects and returns, and maps it to an implementation, such as a static method of a Java class on the classpath or in a JAR file. Calling code asks an `Agent` to execute a function by its IRI with a set of `Arguments`; the agent loads the implementation lazily and invokes it. FnO compositions (functions built from other functions) are executed as well.

The project is a library: it has no command-line interface and no main entry point. It runs on the JVM only, keeps no state between calls, and supports compositions without recursion (see "Current limitations" in `README.md`).

- Maven coordinates: `be.ugent.idlab.knows:function-agent-java` (see `pom.xml`); Java 21.
- Source: `src/main/java/be/ugent/idlab/knows/functions/agent/` plus the helper `src/main/java/be/ugent/idlab/knows/misc/FileFinder.java`.
- Tests: `src/test/java/` (JUnit 5), with fixtures in `src/test/resources/`.
- User documentation: `README.md` (usage example, features, how composition execution works). Changes are recorded in `CHANGELOG.md`.

## Agent request contract (for AI agents/LLMs)

<!-- software-handbook contract: 2026-10-08 -->

Every implementation request handled by an AI agent/LLM follows these constraints:

- If the request is a feature or bugfix:
  - fix the specific failing case or issue named in the request;
  - preserve existing passing behavior unless explicitly asked not to;
  - add or update a regression test when needed.
- Make the smallest coherent patch. A documentation error found along the way is fixed in the same patch.
- Leave the code leaner after every request: remove what the change makes redundant (duplicate tests, parameters and options that no longer do anything, helpers that duplicate each other, comments that only repeat the code), and reuse shared functionality instead of adding a local variant. Use SpotBugs (`mvn compile spotbugs:check`), compiler warnings (`mvn compile`) and IDE inspection to find unused code.
- Fix a transient environment problem (a stale PATH, a shell or editor that needs a restart) in the environment, by restarting or reconfiguring it; add no code that works around it.
- **Push back** when a request would violate an established principle (e.g. breaking test hermeticity). Explain the principle and suggest a documentation-only fix instead of silently implementing the harmful change.
- Update this handbook so the change is documented as well as implemented.
  - Document only the latest state, integrated in the surrounding narrative (principles, behavior, rationale), including the choices made and why.
  - This contract holds only general rules for handling a request; project-specific guidance goes in the chapter on that topic.
- Do not stop at making tests green; align the implementation with the specification or intended design, and document the semantic reason in this handbook.
- Never remove or change existing tests (code or fixtures) without explicit permission. A change to an existing fixture (expected output, input, or data) is validated by the maintainer before it is kept, also when a tool writes it: propose the change with its reason, and keep it only after approval.
- Update `CHANGELOG.md` for implementation changes: keep `## Unreleased` a short summary of what changed since the last release. A feature that is new since the last release is one Added line, which later fixes update instead of getting lines of their own; lines are for what a user of the last release notices.
- Check whether `README.md` needs updates for user-visible behavior or workflow changes, and update it when needed.
- Write documentation (this handbook, READMEs, `TODO.md`, `CHANGELOG.md`, code comments) as plain positive statements: say what is true and leave out the contrast ("X, not Y"). Keep a negative only when it is the point itself, such as a prohibition, a warning, or a known limitation.
- If there are difficulties during fulfillment, document them in the most appropriate existing handbook location (create a new chapter only when truly necessary) so future requests start with better context.
- A preference or principle that the maintainer states while handling a request is documented so that every later request follows it: a general one in this contract (and in the software-handbook skill it comes from), a project-specific one in the handbook chapter it belongs to. When it is unclear which, ask.
- When a request is a list of feedback (such as a `TODO.md`), clean up after handling it: remove the items that are done, keep every open item as a clear task (an open question or an offered follow-up is an open item), and remove temporary files created along the way.

## Architecture and package layout

All packages live under `be.ugent.idlab.knows.functions.agent` unless noted.

- **Entry points** (package root):
  - `Agent`: the public interface. `execute(functionId, arguments)`, `execute(functionId, arguments, debug)` and `getFunctions()`. An `Agent` is `AutoCloseable` and closes loaded function libraries.
  - `AgentFactory`: `createFromFnO(String... pathToFnoDocs)` and an overload taking a map of implementation locations. Paths may be local files, URLs, classpath resources or an FnO document as a string.
  - `AgentImpl`: the implementation; it also offers `writeModel(path)` and `executeToFile(...)` to serialize the loaded model or an execution as RDF.
  - `Arguments`: the parameter-IRI-to-value(s) container passed to `execute`.
  - `DescriptionGenerator`: builds RDF descriptions from the internal model.
- **`functionModelProvider`**: `FunctionModelProvider` is the extension point for function description formats. `fno.FnOFunctionModelProvider` reads FnO documents with Apache Jena; `fno.NAMESPACES` holds the vocabulary IRIs; `fno.exception` has one exception per missing or malformed part of a description.
- **`model`**: the format-independent function model (`Function`, `Parameter`, `FunctionMapping`, `MethodMapping`, `Implementation`, `FunctionComposition` and the composition mapping classes). `model.fno.FnOParameter` adds the resource IRI and type IRI that FnO parameters carry.
- **`functionInstantiation`**: `Instantiator` resolves a `Function` to a callable `ThrowableFunction`, via Java reflection for implementations and via `getCompositeMethod` for compositions. The composition algorithm (safety checks, cycle detection, execution stack, lambda construction, caching) is described step by step in `README.md` under "Function Composition - how it currently works".
- **`dataType`**: `DataTypeConverter` implementations convert argument values to the Java types the implementation expects; `DataTypeConverterProvider` selects one per type.
- **`exception`**: general agent exceptions.
- **`be.ugent.idlab.knows.misc.FileFinder`**: resolves a path as a remote URL, then as a file relative to the working directory, then as a classpath resource.

Runtime dependencies are `jena-arq`, `jena-core`, `commons-collections4` and `slf4j-api`. `jena-core` shares the `jena.version` property with `jena-arq`.

## Build and test

- Build: `mvn compile`; `mvn package` builds the library jar.
- Test: `mvn test`. CI (`.gitlab-ci.yml`) runs `mvn $MAVEN_CLI_OPTS test` on `maven:3-eclipse-temurin-21-alpine` for every branch except `main` and `development`, using `.m2/settings.xml`.
- CI also includes the shared `rml/util/ci-templates` `CHANGELOG.gitlab-ci.yml` check (the `lint` stage), which requires `CHANGELOG.md` to be updated, and `Maven-Central.gitlab-ci.yml` for deployment.
- Linter: SpotBugs 4.10.3 (`spotbugs-maven-plugin` 4.10.3.0, configured in `<pluginManagement>` as in MappingWeaver-java and not bound to a phase). Run `mvn compile spotbugs:check`. Known state: `spotbugs:check` reports 24 findings (mostly EI_EXPOSE_REP and EI_EXPOSE_REP2 in the model classes) and fails the build, so it runs on demand and CI does not run it. No formatter is configured.

Test classes follow the main packages; the test package for `functionInstantiation` is `functionInstantiator`. They are `AgentTest` (end-to-end execution, compositions, partial application, overloads, writing models), `ArgumentsTest`, `GeneratorTest`, `dataType/DataTypeConverterTest`, `functionInstantiator/InstantiatorTest`, `functionModelProvider/fno/FnOFunctionProviderTest`, and `misc/FileFinderTest` and `misc/JarFileTest`. `internalfunctions/InternalTestFunctions` holds the Java methods that test FnO documents map to.

Some tests write files into the working directory (the repository root when run with Maven): `test.txt` and `test0.txt` are produced by print side effects in `sum-composition.ttl` and `complex_side_path.ttl`, `test1.txt` by `AgentTest.functionWithoutReturnValue`, and `testFileWrite.ttl` and `testExecution.ttl` by `AgentTest.testWriteModel` and `testWriteExecutionToFile`. `.gitignore` excludes them (`test*.txt`, `test*.ttl`).

## Test resources

`src/test/resources/` is flat. It contains:

- FnO documents (`*.ttl`), one per scenario: basic functions (`internalTestFunctions.ttl`, `generalFunctions.ttl`, `identityInteger.ttl`), compositions (`sum-composition.ttl`, `squareOfSum.ttl`, `computation.ttl`, `complex_side_path.ttl`, `weirdComposition*.ttl`, `cyclic.ttl`), partial application (`add10*.ttl`, `partialApplicationNoMappings.ttl`, `badPartialApplication*.ttl`), invalid descriptions (`badFunction.ttl`, `badParameter.ttl`), RDF sequences (`rdfSeq*.ttl`), aliases and optional-parameter overloads.
- External implementation JARs, `GrelFunctions.jar` and `AaabimFunctions.jar`, with their FnO descriptions `functions_grel.ttl` and `aaabim_java_mapping.ttl`. The `grel-functions-java` test dependency (via JitPack) provides GREL functions on the classpath as well.
- `simplelogger.properties` for test logging.

## Release process

Step-by-step instructions are in [RELEASE.md](RELEASE.md).

`bump-version.sh` tags a release `v<version>`; a `testrelease-*` name is used as-is for the tag and gets no next `-SNAPSHOT`. The `release` Maven profile builds source and Javadoc jars, signs them with GPG, and publishes to Maven Central through `central-publishing-maven-plugin`; the shared CI template runs that deployment.
