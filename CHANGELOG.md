# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

### Changed
- Updated JUnit from 4.13.2 to 5.9.1 (tests)
- Updated grel-functions-java from v0.7.3 to v0.9.0 (tests)
- Updated jena-arq from 3.17.0 to 4.6.1
- Updated Lombok from 1.18.22 to 1.18.24
- Updated slf4j from 1.7.36 to 2.0.5
- Java language version changed to 11; requires JRE >= 11 to run

### Fixed
- be.ugent.idlab.knows.functions.agent.AgentTest.testGrelClassesOnClassPathRemoteFnODoc: point remote `grel.ttl` to specific GitHub commit to keep function definitions and implementations in sync.

## [0.2.1] - 2022-10-10

### Fixed
- If the `DataTypeConverter` of the generic type parameter of a `Collection` is not found, try to find a `DataTypeConverter` that can process a subtype of the generic type ((see GitLab [issue 14](https://gitlab.ilabt.imec.be/fno/proc/function-component/-/issues/14))). 

## [0.2.0] - 2022-09-01

### Added

- Support for `rdf:seq` parameters.
- `FnoFunctionModelProvider`: Can now parse Function Composition.
- `FnoFunctionModelProvider`: Can now parse Partial Function Application.
- `Agent`: Can execute Function Compositions.
- `Agent`: Can execute Partial Function Application.
- `Agent`: Has a debug mode.
- `Instantiator`: In debug mode it will execute all nodes of a composition, otherwise only those necessary for the output (works non-recursive).
- `DescriptionGenerator`: Can now output Function triples (both execution and functions/mappings from JAVA methods)

### Fixed

- `Instantiator`: fixed Windows path resolving issues.
- `Instantiator`: fixed URL decoding for special characters in path.

## [0.1.0] - 2022-05-25

### Added
- `FnoFunctionModelProvider`: Implementation locations can be changed w.r.t. what's in the function descriptions by
  providing a map `old location -> new location`. 
  This feature allows to use the right location at runtime without changing the function descriptions.

## [0.0.4] - 2022-05-17

### Fixed
- A raw collections used as parameter in an implementation was not recognised as collection
  (see GitLab [issue 6](https://gitlab.ilabt.imec.be/fno/proc/function-component/-/issues/6)). 

## [0.0.3] - 2022-05-13

### Changed
- Let GitLab CI use JDK 8 instead of JDK 11, because that's the lowest supported Java version.
- Improved compatibility checks between parameter descriptions and implementations.

### Added
- Converters for all primitive types.

### Fixed
- Compatibility in data types between (FnO) description and implementation. (see GitLab [issue 5](https://gitlab.ilabt.imec.be/fno/proc/function-component/-/issues/5))

## [0.0.2] - 2022-03-29

### Changed
- Update dependency `grel-functions-java` to bugfix release version `v0.7.2`.

### Fixed
- FnO implementation mappings have class `fno:Mapping` instead of `fnoi:Mapping`.

## [0.0.1] - 2022-03-25

[0.2.1]: https://github.com/FnOio/function-agent-java/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/FnOio/function-agent-java/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/FnOio/function-agent-java/compare/v0.0.4...v0.1.0
[0.0.4]: https://github.com/FnOio/function-agent-java/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/FnOio/function-agent-java/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/FnOio/function-agent-java/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/FnOio/function-agent-java/releases
