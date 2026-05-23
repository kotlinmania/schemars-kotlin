import XCTest
import Schemars
import ExportedKotlinPackages

typealias SchemaSettings = ExportedKotlinPackages.io.github.kotlinmania.schemars.generate.SchemaSettings

// Smoke test for the Kotlin → Swift Export → SPM → swift test pipeline.
//
// The file's mere existence and successful compilation prove three layers
// of the pipeline:
//
//   1. `embedSwiftExportForXcode` produced `Schemars.swiftmodule/`
//      and the supporting KotlinRuntimeSupport / ExportedKotlinPackages /
//      KotlinRuntime swiftmodule bundles. If any of them were missing,
//      `import Schemars` above would fail at compile time.
//
//   2. The static archive `libSchemars.a` (produced by the
//      `linkSwiftExportBinaryDebugStaticMacosArm64` and
//      `mergeMacosDebugSwiftExportLibraries` tasks) supplied every
//      `__root____*` and `KotlinError`-related symbol the Swift modules
//      reference. If the archive were missing or empty, this test
//      executable would fail to link with "undefined symbols for
//      architecture arm64".
//
//   3. The Kotlin `swiftExport { moduleName = "Schemars" }` and
//      `flattenPackage = "io.github.kotlinmania.schemars"` configuration in
//      build.gradle.kts produced a module name that's both syntactically
//      valid as a Swift identifier and reachable from this Package.swift
//      via the `SchemarsLibrary` product.
//
// Add more meaningful per-API tests below as the Swift Export surface
// grows. For now the import + a single passing assertion is the
// canary that the pipeline is green for this repo.
final class SchemarsExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Schemars swift module imported cleanly")
    }

    func testSchemaFromBool() throws {
        // Test that companion functions bridge as `.Companion.shared`
        let schemaTrue = Schema.Companion.shared.from(b: true)
        XCTAssertEqual(schemaTrue.asBool(), true)
        
        let schemaFalse = Schema.Companion.shared.from(b: false)
        XCTAssertEqual(schemaFalse.asBool(), false)
    }

    func testSchemaDefault() throws {
        let schema = Schema.Companion.shared.default()
        XCTAssertNil(schema.asBool(), "Default schema is an object, so asBool() should be nil")
    }

    func testSchemaSettings() throws {
        // Test data class properties
        let settings = SchemaSettings.Companion.shared.draft07()
        XCTAssertEqual(settings.inlineSubschemas, false)
        XCTAssertEqual(settings.untaggedEnumVariantTitles, false)
        XCTAssertEqual(settings.definitionsPath, "/definitions")
    }
}
