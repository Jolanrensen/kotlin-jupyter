package org.jetbrains.kotlinx.jupyter.test.repl

import org.jetbrains.kotlinx.jupyter.EvalRequestData
import org.jetbrains.kotlinx.jupyter.ReplForJupyter
import org.jetbrains.kotlinx.jupyter.api.Code
import org.jetbrains.kotlinx.jupyter.messaging.DisplayHandler
import org.jetbrains.kotlinx.jupyter.test.getOrFail

abstract class AbstractSingleReplTest : AbstractReplTest() {
    protected abstract val repl: ReplForJupyter

    protected fun eval(code: Code, displayHandler: DisplayHandler? = null, jupyterId: Int = -1, storeHistory: Boolean = true) =
        repl.eval(EvalRequestData(code, displayHandler, jupyterId, storeHistory))

    protected fun completeOrFail(code: Code, cursor: Int) = repl.completeBlocking(code, cursor).getOrFail()

    protected fun listErrors(code: Code) = repl.listErrorsBlocking(code)
}
