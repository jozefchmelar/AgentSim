package newsstand.components.rental

import OSPABA.Agent
import OSPABA.IdList.finish
import OSPABA.Manager
import OSPABA.MessageForm
import OSPABA.Simulation
import abaextensions.toAgent
import abaextensions.toAgentsAssistant
import abaextensions.withCode
import newsstand.components.convert
import newsstand.components.entity.Building
import newsstand.components.entity.Group
import newsstand.components.entity.Terminal
import newsstand.components.entity.isOneFree
import newsstand.constants.id
import newsstand.constants.mc
import java.util.*

class AirCarRentalManager(
    mySim: Simulation,
    myAgent: Agent
) : Manager(id.AirCarRentalManagerID, mySim, myAgent) {

    override fun processMessage(msg: MessageForm) = when (msg.code()) {

        mc.airCarRentalMinibusArrival -> requestGroup(msg)

        mc.customerArrivalTerminalAcr -> {
            moveCustomerToQueue(msg)
            tryToServeCustomer(msg)
        }

        mc.getCustomerFromBusResponse -> {
            if (msg.convert().group == null)
                if (myAgent().queueToTerminal3.isEmpty())
                    goToNextStop(msg)
                else
                    sendGroupToT3(msg)
            else {
                moveCustomerToQueue(msg)
                tryToServeCustomer(msg)
                requestGroup(msg)
            }
        }

        finish -> when (msg.sender()) {
            is CustomerServiceScheduler -> serviceFinished(msg)
            else -> TODO()
        }
    //   mc.enterMinibusRequest -> sendGroupToT3(msg)

        else -> println(msg)

    }

    private fun sendGroupToT3(msg: MessageForm) {
//        val msg = msg.createCopy().convert()
//        val queue = myAgent().queueToTerminal3
//        val minibus = msg.minibus!!
//        val mapa = mutableMapOf<Double, Group>()
//        queue.forEach {
//            mapa[it.arrivedToSystem]?.add(it) ?: mapa.put(it.arrivedToSystem, Group(it))
//        }
//        if (queue.isNotEmpty()) {
//            val groupx = mapa.map { it.value }.toMutableList() //as Queue<Group>
//            val groups : Queue<Group> = LinkedList(groupx)
//            val group = groups.peek()
//            if (group.size() <= minibus.freeSeats()) {
//                group.everyone().forEach { queue.remove(it) }
//                requestLoading(msg, group)
//            }
//        } else {
//            msg.createCopy().toAgent(id.MinibusAgentID).withCode(mc.minibusGoTo).let { notice(it) }
//        }
    }

    /** @see CustomerServiceScheduler **/
    private fun tryToServeCustomer(msg: MessageForm) {
        val msg = msg.createCopy()
        if (myAgent().queue.isNotEmpty() && myAgent().employees.isOneFree()) {
            val employee = myAgent().employees.first { it.isNotBusy() }
            val group = myAgent().queue.pop()
            val customer = group.leader
            employee.serveCustomer(customer)
            msg.convert().group = group
            serveCustomer(msg)
        }
    }

    private fun serviceFinished(msg: MessageForm) {
        when (msg.convert().group!!.building()) {
            Building.TerminalOne,
            Building.TerminalTwo  -> customerLeavesWithCar(msg)
            Building.AirCarRental -> moveCustomerToQueue(msg)
        }
        val msg = msg.createCopy()
        msg.convert().group = null
        tryToServeCustomer(msg)
    }


    private fun customerLeavesWithCar(msg: MessageForm) = msg
        .createCopy()
        .toAgent(id.SurroundingAgent)
        .withCode(mc.customerLeaving)
        .let { notice(it) }

    private fun serveCustomer(msg: MessageForm) = msg
        .createCopy()
        .convert()
        .toAgentsAssistant(myAgent(), id.CustomerServiceSchedulerID)
        .let { startContinualAssistant(it) }

    //region works
    private fun requestGroup(msg: MessageForm) = msg
        .createCopy()
        .withCode(mc.getCustomerFromBusRequest)
        .toAgent(id.MinibusAgentID)
        .let { request(it) }


    /** @see MoveCustomerToQueueAction  **/
    private fun moveCustomerToQueue(msg: MessageForm) = msg
        .createCopy()
        .convert()
        .withCode(mc.moveCustomerToQueueAtAirCarRental)
        .toAgentsAssistant(myAgent(), id.AirCarRentalMoveCustomerToQueueAction)
        .let { execute(it) }


    private fun goToNextStop(msg: MessageForm) = msg
        .createCopy()
        .toAgent(id.MinibusMovementID)
        .withCode(mc.minibusGoTo)
        .let { notice(it) }

    private fun requestLoading(msg: MessageForm, group: Group) = msg
        .createCopy()
        .convert()
        .apply {
            this.group = group
            this.building = Building.AirCarRental
        }
        .withCode(mc.enterMinibusRequest)
        .toAgent(id.MinibusAgentID)
        .let { request(it) }

    //endregion

    override fun myAgent() = super.myAgent() as AirCarRentalAgent

}
