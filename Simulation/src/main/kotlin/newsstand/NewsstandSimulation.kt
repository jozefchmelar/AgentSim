package newsstand

import OSPABA.Simulation
import OSPStat.Stat
import newsstand.components.boss.BossAgent
import newsstand.components.minibus.MinibusAgent
import newsstand.components.rental.AirCarRentalAgent
import newsstand.components.surrounding.SurroundingAgent
import newsstand.components.terminal.TerminalAgent

data class Config(val minibuses: Int = 10, val employees: Int = 10)

class NewsstandSimulation(config: Config = Config()) : Simulation() {

   private val boss = BossAgent(this)
   private val surrounding = SurroundingAgent(this, boss)
   private val terminal = TerminalAgent(this, boss)
   private val minibus = MinibusAgent(this, boss, config.minibuses)
   private val airCarRentalAgent = AirCarRentalAgent(this, boss, config.employees)

    internal var maxSimTime: Double = 0.0
    internal var warmUpTime = 0.0 //60 * 60 * 60 * 5.0

    override fun prepareReplication() {
        super.prepareReplication()
        boss.start()
    }

    override fun replicationFinished() {
        super.replicationFinished()
        println(surrounding.timeInSystem.mean() / 60.0)

    }

    fun start(replicationCount: Int = 1, simEndTime: Double = 60 * 60 * 24 * 30.0) {
        maxSimTime = simEndTime + warmUpTime
        simulate(replicationCount, simEndTime)
    }


    fun getState() = SimState(
        queueT1 = terminal.terminalOne.queue,
        queueT2 = terminal.terminalTwo.queue,
        timeStatQueueT1 = terminal.terminalOne.timeInQueue,
        timeStatQueueT2 = terminal.terminalTwo.timeInQueue,
        queueAcr = airCarRentalAgent.queue,
        acrEmployees = airCarRentalAgent.employees,
        minibuses = minibus.minibuses
    )
}

private fun Stat.clearStat() {
    clear()
}

fun main(args: Array<String>) {
    val s = NewsstandSimulation()
    s.start()
}//19.58008985