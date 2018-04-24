package newsstand.components.boss

import OSPABA.Agent
import OSPABA.Manager
import OSPABA.MessageForm
import OSPABA.Simulation
import abaextensions.WrongMessageCode
import abaextensions.toAgent
import abaextensions.withCode
import newsstand.constants.id
import newsstand.constants.mc
import newsstand.constants.mc.customerArrivalTerminalOne
import newsstand.constants.mc.customerArrivalTerminalTwo
import newsstand.constants.mc.init
import newsstand.constants.mc.terminalOneMinibusArrival

class BossManager(
    mySim: Simulation,
    myAgent: Agent
) : Manager(id.BossManager, mySim, myAgent) {

    override fun processMessage(message: MessageForm) = when (message.code()) {

        init -> message
            .createCopy()
            .toAgent(id.SurroundingAgent)
            .withCode(init)
            .let { notice(it) }

        customerArrivalTerminalOne -> message
            .createCopy()
            .toAgent(id.TerminalAgentID)
            .let { notice(it) }

        customerArrivalTerminalTwo -> message
            .createCopy()
            .toAgent(id.TerminalAgentID)
            .let { notice(it) }

        terminalOneMinibusArrival -> message
            .createCopy()
            .toAgent(id.TerminalAgentID)
            .let { notice(it) }

        else -> throw WrongMessageCode(message)
    }
}