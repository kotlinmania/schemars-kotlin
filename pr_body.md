## Summary

This PR wires the Kotlin → Swift Export → SPM → `swift test` loop into
this repository's CI, mirroring the canonical pattern established by
[libc-kotlin](https://github.com/KotlinMania/libc-kotlin) and proven
end-to-end with a passing `swift test` invocation against the
`embedSwiftExportForXcode`-produced Swift Package Manager package.

The full recipe for the rollout — including the rationale for each
change, the two upstream Kotlin Multiplatform plugin gaps it works
around, and the precondition / verification checks the Claude agent
ran before opening this PR — is checked in at the repo root as
[`SWIFT_EXPORT_ROLLOUT.md`](./SWIFT_EXPORT_ROLLOUT.md).

## What this PR changes

- `build.gradle.kts` — `iosSimulatorArm64` framework binary now has
  `isStatic = true` so the Swift Export SPM bridge can link against
  it.
- `.github/workflows/swift.yml` (new) — `workflow_call:` platform
  workflow that sets the full Xcode-style environment, runs
  `./gradlew embedSwiftExportForXcode`, then runs `swift test` from
  `swift-test-harness/`. Uploads the static archive and `.swiftmodule`
  bundles as artifacts on success-or-failure.
- `.github/workflows/ci.yml` — adds the `swift:` job alongside
  `wasm:` and `js:`.
- `swift-test-harness/` (new) — a Swift Package Manager package with
  one smoke test that asserts the Swift module imports cleanly.
- `.gitignore` — Swift/SPM workspace state section.

## Test plan

- [ ] The `Build (Swift)` job in CI lands BUILD SUCCESSFUL.
- [ ] The `Run swift test against Kotlin-exported module` step prints
      1 test executed, 0 failures.
- [ ] The uploaded `swift-export-artifacts` archive contains
      `lib<Module>.a` and the four `.swiftmodule/` directories.

## Known upstream gaps

Two issues live in the upstream Kotlin Multiplatform plugin and are
worked around per-repo in this PR. Both would, if fixed upstream,
allow every Kotlin Swift Export consumer to drop the local workaround
at once:

1. **`Package.swift` doesn't reference its own static archive.** The
   Kotlin plugin emits Swift source files for the SPM package but
   does not declare `lib<Module>.a` as a binary target or expose its
   link path. `swift-test-harness/Package.swift` compensates with
   `unsafeFlags(["-L", "../build/swift-test", "-l<Module>"])`.
2. **`BuildSPMSwiftExportPackage` validates
   `deploymentTargetSettingName` as a non-optional input** but
   provides no default. Outside Xcode the env var
   `DEPLOYMENT_TARGET_SETTING_NAME` isn't set, so the task fails
   property validation. `swift.yml` sets it explicitly.

🤖 Opened by the Claude GitHub Agent following
[`SWIFT_EXPORT_ROLLOUT.md`](./SWIFT_EXPORT_ROLLOUT.md).
