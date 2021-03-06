package newsstand.components.rental

import OSPABA.Agent
import OSPABA.Simulation
import OSPDataStruct.SimQueue
import OSPStat.Stat
import OSPStat.WStat
import abaextensions.addOwnMessages
import newsstand.clearStat
import newsstand.components.entity.Customer
import newsstand.components.entity.Employee
import newsstand.components.entity.Group
import newsstand.constants.Clearable
import newsstand.constants.id
import newsstand.constants.mc

class AirCarRentalAgent(
    mySim: Simulation,
    parent: Agent,
    employees: Int
) : Agent(id.AirCarRentalAgentID, mySim, parent), Clearable {


    var totalCustomers       = 0
    val queue                = SimQueue<Group>   (WStat(mySim))
    val queueStat            = Stat()
    val queueToTerminal3     = SimQueue<Customer>(WStat(mySim))
    val queueToTerminal3Stat = Stat()
    val employees: List<Employee> = List(employees) { Employee(it) }.onEach { it.setStat(mySim) }



    init {
        AirCarRentalManager(mySim, this)
        CustomerServiceScheduler(mySim, this)
        MoveCustomerToQueueAction(mySim, this)
        addOwnMessages(
            mc.airCarRentalMinibusArrival,
            mc.getCustomerFromBusResponse,
            mc.customerArrivalTerminalAcr,
            mc.enterMinibusResponse,
            mc.customerServed,
            mc.clearLengthStat
        )
    }



    override fun clear(){
        queue.clearStat()
        queueToTerminal3.clearStat()
        queueToTerminal3Stat.clear()
        queueStat.clear()
        employees.forEach(Employee::clear)
    }

}



