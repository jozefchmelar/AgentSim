package newsstand.components.rental

import OSPABA.Agent
import OSPABA.Simulation
import OSPDataStruct.SimQueue
import OSPStat.WStat
import abaextensions.addOwnMessages
import newsstand.components.entity.Customer
import newsstand.components.entity.Employee
import newsstand.components.entity.Group
import newsstand.constants.id
import newsstand.constants.mc

class AirCarRentalAgent(
    mySim: Simulation,
    parent: Agent,
    employees: Int
) : Agent(id.AirCarRentalAgentID, mySim, parent) {

    val queue = SimQueue<Group>(WStat(mySim))
    val queueToTerminal3 = SimQueue<Customer>(WStat(mySim))
    val employees = List(employees) { Employee(it) }

    init {
        AirCarRentalManager(mySim, this)
        CustomerServiceScheduler(mySim, this)
        MoveCustomerToQueueAction(mySim, this)
        addOwnMessages(
            mc.airCarRentalMinibusArrival,
            mc.getCustomerFromBusResponse,
            mc.customerArrivalTerminalAcr,
            mc.enterMinibusResponse,
            mc.customerServed
        )
    }

}



