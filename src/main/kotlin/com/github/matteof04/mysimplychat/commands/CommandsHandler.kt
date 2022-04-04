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

package com.github.matteof04.mysimplychat.commands

class CommandsHandler {
    private val registry = mutableListOf<Command>()
    fun registerCommand(command: Command) = registry.add(command)
    fun processCommand(consoleInput: String) : ByteArray?{
        val args = consoleInput.replace("/", "", false).split(' ')
        val command = registry.firstOrNull { it.getCommandString() == args.first() } ?: return null
        val encodedPayload = command.encode(args.drop(1))
        val outputEncodedCommand =  ByteArray(encodedPayload.size+1)
        outputEncodedCommand[0] = command.getCommandByte()
        encodedPayload.copyInto(outputEncodedCommand, 1)
        return outputEncodedCommand
    }
}