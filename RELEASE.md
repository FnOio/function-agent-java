# Releasing function-agent-java

Step-by-step instructions for publishing a release. [HANDBOOK.md](HANDBOOK.md) (Release process) explains how the tooling works.

## Before you start

- bash (Git Bash on Windows), Maven, Java 21, and `changefrog` (`npm install -g changefrog`), which writes the version section of `CHANGELOG.md`.
- Push access to `origin` (https://gitlab.ilabt.imec.be/KNoWS/fno/proc/function-component).
- You are on `development`, up to date with `origin/development`, with a clean working tree.
- The tests pass: `mvn verify`.
- `## Unreleased` in `CHANGELOG.md` lists everything since the last release.
- Pick the version `X.Y.Z` with [Semantic Versioning](https://semver.org/). `pom.xml` holds the next version as `<version>-SNAPSHOT`: release that version, or a higher minor or major version when the changelog has new features or breaking changes.

## Release

1. Run `./bump-version.sh <version>` and answer `y` to both questions. The script
   - sets the version in `pom.xml` and the version in `README.md`;
   - turns `## Unreleased` into the version section of `CHANGELOG.md`;
   - commits "Update version to <version>", pushes `development`, and creates and pushes the tag `v<version>`;
   - moves `pom.xml` to the next patch `-SNAPSHOT`, and commits and pushes "Prepare for next development cycle".
2. Move `main` to the release: `git push origin v<version>^{commit}:main`. `main` always points at the latest release; the push succeeds only as a fast-forward.
3. The tag pipeline (https://gitlab.ilabt.imec.be/KNoWS/fno/proc/function-component/-/pipelines) builds the release with the `release` Maven profile, signs it and deploys it to Maven Central. Check that its deploy job succeeds; the new version then appears at https://repo1.maven.org/maven2/be/ugent/idlab/knows/function-agent-java/ (this can take up to an hour).

## After the release

- GitLab mirrors the branches and tags to GitHub (https://github.com/FnOio/function-agent-java); check that the tag is there.
- Update `function-agent.version` in MappingWeaver-java's `pom.xml` and the function-agent-java version in rmlmapper-java's `pom.xml`.

## When something goes wrong

- The script stops at the first failing command. When it stops before pushing, fix the cause, discard its changes (`git reset --hard origin/development`) and run it again. When it stops after pushing `development`, finish by hand: create the tag `v<version>` on the version commit if it is missing, push it (`git push origin v<version>`), then set the next `-SNAPSHOT` (`mvn versions:set -DnewVersion=<next>-SNAPSHOT -DgenerateBackupPoms=false`), commit and push.
- Once the tag is pushed, keep it: fix the cause and retry the failed pipeline job. When the released code itself is broken, release the next patch version.
