package org.jetbrains.kotlinx.jupyter.test

import org.jetbrains.kotlinx.jupyter.HMAC
import org.jetbrains.kotlinx.jupyter.JupyterSockets
import org.jetbrains.kotlinx.jupyter.KernelConfig
import org.jetbrains.kotlinx.jupyter.defaultRuntimeProperties
import org.jetbrains.kotlinx.jupyter.iKotlinClass
import org.jetbrains.kotlinx.jupyter.kernelServer
import org.jetbrains.kotlinx.jupyter.libraries.EmptyResolutionInfoProvider
import org.jetbrains.kotlinx.jupyter.messaging.Message
import org.jetbrains.kotlinx.jupyter.messaging.MessageContent
import org.jetbrains.kotlinx.jupyter.messaging.MessageData
import org.jetbrains.kotlinx.jupyter.messaging.MessageType
import org.jetbrains.kotlinx.jupyter.messaging.makeHeader
import org.jetbrains.kotlinx.jupyter.receiveMessage
import org.jetbrains.kotlinx.jupyter.sendMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ
import java.io.File
import java.io.IOException
import java.net.DatagramSocket
import java.net.ServerSocket
import java.util.ArrayList
import java.util.Random
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

open class KernelServerTestsBase {

    private val config = KernelConfig(
        ports = JupyterSockets.values().map { randomPort() },
        transport = "tcp",
        signatureScheme = "hmac1-sha256",
        signatureKey = "",
        scriptClasspath = classpath,
        resolverConfig = null,
        homeDir = File(""),
        resolutionInfoProvider = EmptyResolutionInfoProvider,
    )

    private val sessionId = UUID.randomUUID().toString()

    protected val hmac = HMAC(config.signatureScheme, config.signatureKey)

    // Set to false to debug kernel execution
    protected val runInSeparateProcess = true
    private var serverProcess: Process? = null
    private var serverThread: Thread? = null

    protected val messageId = listOf(byteArrayOf(1))

    private var testLogger: Logger? = null
    private var fileOut: File? = null
    private var fileErr: File? = null

    open fun beforeEach() {}
    open fun afterEach() {}

    @BeforeEach
    fun setupServer(testInfo: TestInfo) {
        if (runInSeparateProcess) {
            val testName = testInfo.displayName
            val args = config.toArgs(testName).argsList().toTypedArray()
            val command = ArrayList<String>().apply {
                add(javaBin)
                add("-cp")
                add(classpathArg)
                add(iKotlinClass.name)
                addAll(args)
            }

            testLogger = LoggerFactory.getLogger("testKernel_$testName")
            fileOut = File.createTempFile("tmp-kernel-out-$testName", ".txt")
            fileErr = File.createTempFile("tmp-kernel-err-$testName", ".txt")

            serverProcess = ProcessBuilder(command)
                .redirectOutput(fileOut)
                .redirectError(fileErr)
                .start()
        } else {
            serverThread = thread { kernelServer(config, defaultRuntimeProperties) }
        }
        beforeEach()
    }

    @AfterEach
    fun teardownServer() {
        afterEach()
        if (runInSeparateProcess) {
            serverProcess?.run {
                destroy()
                waitFor()
            }
            testLogger?.apply {
                fileOut?.let {
                    debug("Kernel output:")
                    it.forEachLine { line -> debug(line) }
                    it.delete()
                }
                fileErr?.let {
                    debug("Kernel errors:")
                    it.forEachLine { line -> debug(line) }
                    it.delete()
                }
            }
        } else {
            serverThread?.interrupt()
        }
    }

    inner class ClientSocket(context: ZMQ.Context, private val socket: JupyterSockets) : ZMQ.Socket(context, socket.zmqClientType) {
        fun connect() = connect("${config.transport}://*:${config.ports[socket.ordinal]}")
    }

    fun ZMQ.Socket.sendMessage(msgType: MessageType, content: MessageContent) {
        sendMessage(Message(id = messageId, MessageData(header = makeHeader(msgType, sessionId = sessionId), content = content)), hmac)
    }

    fun ZMQ.Socket.receiveMessage() = receiveMessage(recv(), hmac)

    companion object {
        private val rng = Random()
        private val usedPorts: MutableSet<Int> = ConcurrentHashMap.newKeySet()
        private const val portRangeStart = 32768
        private const val portRangeEnd = 65536
        private const val maxTrials = portRangeEnd - portRangeStart
        private val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        private val classpathArg = System.getProperty("java.class.path")

        private fun isPortAvailable(port: Int): Boolean {
            var tcpSocket: ServerSocket? = null
            var udpSocket: DatagramSocket? = null
            try {
                tcpSocket = ServerSocket(port)
                tcpSocket.reuseAddress = true
                udpSocket = DatagramSocket(port)
                udpSocket.reuseAddress = true
                return true
            } catch (_: IOException) {
            } finally {
                tcpSocket?.close()
                udpSocket?.close()
            }
            return false
        }

        fun randomPort() =
            generateSequence { portRangeStart + rng.nextInt(portRangeEnd - portRangeStart) }.take(maxTrials).find {
                isPortAvailable(it) && usedPorts.add(it)
            } ?: throw RuntimeException("No free port found")
    }
}
