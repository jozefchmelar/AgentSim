package newsstand

import OSPABA.SimComponent
import OSPABA.Simulation
import OSPDataStruct.SimQueue
import newsstand.components.boss.BossAgent
import newsstand.components.entity.BusType
import newsstand.components.entity.Employee
import newsstand.components.entity.Minibus
import newsstand.components.minibus.MinibusAgent
import newsstand.components.rental.AirCarRentalAgent
import newsstand.components.surrounding.SurroundingAgent
import newsstand.components.terminal.TerminalAgent
import newsstand.constants.Clearable
import newsstand.constants.const

class NewsstandSimulation(val config: Config = Config()) : Simulation(), Clearable {

    private val boss = BossAgent(this, config)
    private val surrounding = SurroundingAgent(this, boss)
    private val terminal = TerminalAgent(this, boss)
    private val minibus = MinibusAgent(this, boss, config)
    private val airCarRentalAgent = AirCarRentalAgent(this, boss, config.employees)

    val timeInSystemLeaving = Result("Čas v systéme odchádzajúci", ResultType.Time)
    val timeInSystemIncoming = Result("Čas v systéme prichádzajúci", ResultType.Time)
    val timeInSystemTotal = Result("Čas v systéme spoločný", ResultType.Time)
    val queueT1 = Result("Dĺžka fronty Terminál 1")
    val timeStatQueueT1 = Result("Čas čakania Terminál 1", ResultType.Time)
    val queueT2 = Result("Dĺžka fronty Terminál 2")
    val timeStatQueueT2 = Result("Čas čakania Terminál 2", ResultType.Time)
    val queueAcr = Result("Dĺžka fronty na obsluhu")
    val timeStatQueueAcr = Result("Čas čakania na obsluhu", ResultType.Time)

    val queueAcrToT3 = Result("Dĺžka fronty na odvoz")
    val timeStatQueueAcrToT3 = Result("Čas čakania na odvoz", ResultType.Time)
    val acrQueueLength = Result("")

    val employeeOccupancy = Result("Vyťaženosť obsluhujúceho")

    val busOccupancy = Result("Vyťaženosť autobusov")
    val busKiloneters = Result("Počet najazdených km")
    val priceKilometers = Result("Cena za najazdene km")
    val priceBusDriver = Result("Cena prace soferov")
    val priceService = Result("Cena prace obsluhujúci")
    val priceAll = Result("Cena spolu")
    val simTime = Result("Prevádzka")

    private var maxSimTime = 0.0

    var warmedUp = false

    override fun prepareReplication() {
        super.prepareReplication()
        clear()
        warmedUp = false
        boss.start()
    }

    override fun replicationFinished() {
        super.replicationFinished()
        val repState = getState()
        simTime.addSample( (currentTime()-const.WarmUpTime)/60/60)
        repState.timeInSystemIncoming.mean().let(timeInSystemIncoming::addSample)
        repState.timeInSystemLeaving.mean().let(timeInSystemLeaving::addSample)
        repState.timeInSystemTotal.mean().let(timeInSystemTotal::addSample)
        repState.queueT1.mean().let(queueT1::addSample)
        repState.timeStatQueueT1.mean().let { timeStatQueueT1.addSample(it / 60) }
        repState.queueT2.mean().let(queueT2::addSample)
        repState.timeStatQueueT2.mean().let { timeStatQueueT2.addSample(it / 60) }
        repState.queueAcr.mean().let(queueAcr::addSample)
        repState.timeStatQueueAcr.mean().let { timeStatQueueAcr.addSample(it / 60) }
        repState.queueAcrToT3.mean().let(queueAcrToT3::addSample)
        repState.timeStatQueueAcrToT3.mean().let { timeStatQueueAcrToT3.addSample(it / 60) }
        repState.employeeOccupancy.let(employeeOccupancy::addSample)
        repState.busOccupancy.let(busOccupancy::addSample)
        minibus
            .minibuses
            .map { it.meters / 1000 }
            .sum()
            .let(busKiloneters::addSample)
        var price = .0
        minibus.minibuses.map { it.meters / 1000 * it.pricePerKm }.sum().let { priceKilometers.addSample(it); price += it }
        minibus.minibuses.map { (it.meters / 1000) / 35 * config.driverRate }.sum().let { priceBusDriver.addSample(it); price += it }
        val servicePrice = (currentTime() - const.WarmUpTime) / 60 / 60 * config.serviceRata * config.employees
        priceService.addSample(servicePrice)
        price += servicePrice
        priceAll.addSample(price)
        airCarRentalAgent.queue.lengthStatistic().mean().let(acrQueueLength::addSample)
        clear()
    }

    override fun clear() = listOf<Clearable>(boss, surrounding, terminal, minibus, airCarRentalAgent)
        .forEach(Clearable::clear)

    fun start(simEndTime: Double = 4.5 * 3600) {
        maxSimTime = simEndTime + const.WarmUpTime
        simulate(config.replicationCount, maxSimTime * 1.5)
    }

    fun getState() = SimState(
        timeInSystemIncoming = surrounding.timeInSystemIncoming,
        timeInSystemLeaving = surrounding.timeInSystemLeaving,
        timeInSystemTotal = surrounding.timeInSystemTotal,
        queueT1 = terminal.terminalOne.queue,
        timeStatQueueT1 = terminal.terminalOne.timeInQueueStat,
        queueT2 = terminal.terminalTwo.queue,
        timeStatQueueT2 = terminal.terminalTwo.timeInQueueStat,
        queueAcr = airCarRentalAgent.queue,
        timeStatQueueAcr = airCarRentalAgent.queueStat,
        queueAcrToT3 = airCarRentalAgent.queueToTerminal3,
        timeStatQueueAcrToT3 = airCarRentalAgent.queueToTerminal3Stat,
        acrEmployees = airCarRentalAgent.employees,
        minibuses = minibus.minibuses,
        employeeOccupancy = airCarRentalAgent.employees.sumByDouble(Employee::occupancy).div(config.employees),
        busOccupancy = minibus.minibuses.sumByDouble(Minibus::occupancy)
    )

    val allResults = listOf(
        timeInSystemIncoming,
        timeInSystemLeaving,
        timeInSystemTotal,
        spacer(),
        queueT1,
        timeStatQueueT1,
        queueT2,
        timeStatQueueT2,
        spacer(),
        queueAcr,
        timeStatQueueAcr,
        queueAcrToT3,
        timeStatQueueAcrToT3,
        // acrQueueLength,
        employeeOccupancy,
        busOccupancy,
        busKiloneters,
        priceKilometers,
        priceBusDriver,
        priceService,
        priceAll
    )

    private fun spacer(name: String = "") = Result(name, ResultType.Spacer)

    override fun simulationFinished() {
        super.simulationFinished()
        simTime.mean().let { println(it) }
    }
}

fun SimComponent.isWarmedUp()    = mySim().currentTime() > const.WarmUpTime
fun SimComponent.isCoolingDown() = mySim().currentTime() > const.ClosingDown

fun <E> SimQueue<E>.clearStat() {
    clear()
    lengthStatistic()?.clear()
}

fun <E> SimQueue<E>.mean() = lengthStatistic().mean()

fun main(args: Array<String>) {
    val configs = listOf(
        Config(minibuses = 2, employees = 3, busType = BusType.A, replicationCount = 5000, slowDownAfterWarmUp = false)
    )

    configs.forEach {
        NewsstandSimulation(it).start()
    }

}