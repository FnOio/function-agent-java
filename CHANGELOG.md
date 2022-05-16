# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

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

[0.0.1]: https://github.com/FnOio/function-agent-java/releases
[0.0.2]: https://github.com/FnOio/function-agent-java/compare/v0.0.1...v0.0.2
[0.0.3]: https://github.com/FnOio/function-agent-java/compare/v0.0.2...v0.0.3