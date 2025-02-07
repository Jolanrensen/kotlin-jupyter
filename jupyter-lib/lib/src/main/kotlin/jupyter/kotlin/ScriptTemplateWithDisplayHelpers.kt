package jupyter.kotlin

import org.jetbrains.kotlinx.jupyter.api.KotlinKernelHost
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.ResultsAccessor
import org.jetbrains.kotlinx.jupyter.api.libraries.CodeExecution
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.LibraryDefinition

abstract class ScriptTemplateWithDisplayHelpers(
    val notebook: Notebook,
    private val hostProvider: KotlinKernelHostProvider
) {
    private val host: KotlinKernelHost get() = hostProvider.host!!

    fun DISPLAY(value: Any) = host.display(value)

    fun UPDATE_DISPLAY(value: Any, id: String?) = host.updateDisplay(value, id)

    fun EXECUTE(code: String) = host.scheduleExecution(CodeExecution(code).toExecutionCallback())

    fun USE(library: LibraryDefinition) = host.addLibrary(library)

    fun USE(builder: JupyterIntegration.Builder.() -> Unit) {
        val o = object : JupyterIntegration() {
            override fun Builder.onLoaded() {
                builder()
            }
        }
        USE(o.getDefinitions(notebook).single())
    }

    fun USE_STDLIB_EXTENSIONS() = host.loadStdlibJdkExtensions()

    val Out: ResultsAccessor get() = notebook.resultsAccessor

    val JavaRuntimeUtils get() = notebook.jreInfo
}
