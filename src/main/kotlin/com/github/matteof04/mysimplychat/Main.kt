/*
 * Copyright (C) 2022 Matteo Franceschini <matteof5730@gmail.com>
 *
 * This file is part of mysimplychat.
 * mysimplychat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * mysimplychat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with mysimplychat.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.matteof04.mysimplychat

import com.github.matteof04.mysimplychat.commands.CommandsHandler
import com.github.matteof04.mysimplychat.commands.commands
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val HOSTNAME = "127.0.0.1"
val commandsHandler = CommandsHandler()

fun main() {
    println("""
        MySimplyChat  Copyright (C) 2022  Matteo Franceschini
        This program comes with ABSOLUTELY NO WARRANTY.
        This is free software, and you are welcome to redistribute it
        under GNU GPLv3.
    """.trimIndent())
    println("\nMySimplyChat: simple websocket-based chat system\n")
    commands.forEach { commandsHandler.registerCommand(it) }
    print("Enter username: ")
    val username = readLine() ?: ""
    print("Enter password: ")
    val password = System.console()?.readPassword()?.toTypedArray()?.joinToString("","","", -1, "") { it.toString() } ?: readLine() ?: ""
    if(username.isNotBlank()){
        val client = HttpClient {
            install(WebSockets)
        }
        runBlocking {
            client.webSocket(method = HttpMethod.Get, host = HOSTNAME, port = 8080, path = "/chat") {
                send(commandsHandler.processCommand("/setusername $username")!!)
                send(commandsHandler.processCommand("/setpassword $password")!!)
                val messageOutputRoutine = launch { outputMessages() }
                val userInputRoutine = launch { inputMessages() }
                userInputRoutine.join()
                messageOutputRoutine.cancelAndJoin()
            }
        }
        client.close()
        println("Connection closed. Goodbye!")
    }else{
        println("Username blank.")
    }
}

suspend fun DefaultClientWebSocketSession.outputMessages() {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            println(message.readText())
        }
    } catch (e: Exception) {
        //println("Error while receiving: " + e.localizedMessage)
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages() {
    while (true) {
        val message = readLine() ?: ""
        var command : ByteArray? = null
        if (message.equals("exit", true)) return
        if (message.contains('/', true)){
            command = commandsHandler.processCommand(message)
        }
        try {
            if(command == null){
                send(message)
            }else{
                send(command)
            }
        } catch (e: Exception) {
            //println("Error while sending: " + e.localizedMessage)
            return
        }
    }
}