package org.jetbrains.kotlinx.jupyter.test.repl

import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.libraries.GitHubRepoName
import org.jetbrains.kotlinx.jupyter.libraries.GitHubRepoOwner
import org.jetbrains.kotlinx.jupyter.libraries.LibrariesDir
import org.jetbrains.kotlinx.jupyter.libraries.LibraryDescriptorExt
import org.jetbrains.kotlinx.jupyter.libraries.LibraryResolutionInfo
import org.jetbrains.kotlinx.jupyter.libraries.LocalSettingsPath
import org.jetbrains.kotlinx.jupyter.test.TestDisplayHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Execution(ExecutionMode.SAME_THREAD)
class ReplWithStandardResolverTests : AbstractSingleReplTest() {
    override val repl = makeReplWithStandardResolver()

    @Test
    @Disabled
    fun testResolverRepoOrder() {
        val res = eval(
            """
            @file:Repository("https://repo.osgeo.org/repository/release/")
            @file:DependsOn("org.geotools:gt-shapefile:[23,)")
            @file:DependsOn("org.geotools:gt-cql:[23,)")
            
            %use lets-plot@cfcf8257116ad3753b176a9f779eaaea4619dacd(api=2.0.1)
            
            @file:DependsOn("org.jetbrains.lets-plot:lets-plot-kotlin-geotools:2.0.1")
            
            import jetbrains.letsPlot.toolkit.geotools.toSpatialDataset
            """.trimIndent()
        )

        Assertions.assertTrue(res.metadata.newClasspath.size >= 2)
    }

    @Test
    fun testStandardLibraryResolver() {
        val res = eval(
            """
            %use krangl(0.16.2)
            val df = DataFrame.readCSV("src/test/testData/resolve-with-runtime.csv")
            df.head().rows.first().let { it["name"].toString() + " " + it["surname"].toString() }
            """.trimIndent()
        )
        assertEquals("John Smith", res.resultValue)
    }

    @Test
    fun testDefaultInfoSwitcher() {
        val infoProvider = repl.resolutionInfoProvider

        val initialDefaultResolutionInfo = infoProvider.fallback
        Assertions.assertTrue(initialDefaultResolutionInfo is LibraryResolutionInfo.ByDir)

        eval("%useLatestDescriptors")
        Assertions.assertTrue(infoProvider.fallback is LibraryResolutionInfo.ByGitRef)

        eval("%useLatestDescriptors -off")
        Assertions.assertTrue(infoProvider.fallback === initialDefaultResolutionInfo)
    }

    @Test
    fun testUseFileUrlRef() {
        val commit = "cfcf8257116ad3753b176a9f779eaaea4619dacd"
        val libraryPath = "src/test/testData/test-init.json"

        val res1 = eval(
            """
            %use @file[$libraryPath](name=x, value=42)
            x
            """.trimIndent()
        )
        assertEquals(42, res1.resultValue)

        val res2 = eval(
            """
            %use @url[https://raw.githubusercontent.com/$GitHubRepoOwner/$GitHubRepoName/$commit/$libraryPath](name=y, value=43)
            y
            """.trimIndent()
        )
        assertEquals(43, res2.resultValue)

        val displays = mutableListOf<Any>()
        val handler = TestDisplayHandler(displays)

        val res3 = eval("%use lets-plot@$commit", handler)
        assertEquals(1, displays.count())
        assertNull(res3.resultValue)
        displays.clear()

        val res4 = eval(
            """
            %use @$libraryPath(name=z, value=44)
            z
            """.trimIndent()
        )
        assertEquals(44, res4.resultValue)
    }

    @Test
    fun testLocalLibrariesStorage() {
        @Language("json")
        val descriptorText = """
            {
              "init": [
                "val y = 25"
              ]
            }
        """.trimIndent()

        val libName = "test-local"
        val file = LocalSettingsPath.resolve(LibrariesDir).resolve("$libName.$LibraryDescriptorExt").toFile()
        file.delete()

        file.parentFile.mkdirs()
        file.writeText(descriptorText)

        val result = eval(
            """
            %use $libName
            y
            """.trimIndent()
        )

        assertEquals(25, result.resultValue)
        file.delete()
    }
}
