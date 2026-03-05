package com.example.oscsensor

import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPortOut
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OscManager {
    private var oscPort: OSCPortOut? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun connect(ip: String, port: Int) {
        executor.submit {
            try {
                oscPort?.close()
                oscPort = OSCPortOut(InetAddress.getByName(ip), port)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun send(address: String, args: List<Any>) {
        executor.submit {
            try {
                if (oscPort != null) {
                    val message = OSCMessage(address, args)
                    oscPort?.send(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun close() {
        executor.submit {
            oscPort?.close()
            oscPort = null
        }
    }
}
