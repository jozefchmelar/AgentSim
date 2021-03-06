package newsstand

import OSPDataStruct.SimQueue
import OSPStat.Stat
import newsstand.components.entity.*

data class SimState(
    val queueT1: SimQueue<Customer>,
    val queueT2: SimQueue<Customer>,
    val queueAcr: SimQueue<Group>,
    val queueAcrToT3: SimQueue<Customer>,
    val acrEmployees: List<Employee>,
    val minibuses: List<Minibus>,
    val timeInSystemIncoming: Stat = Stat(),
    val timeInSystemLeaving: Stat = Stat(),
    val timeInSystemTotal: Stat = Stat(),
    val timeStatQueueT1: Stat = Stat(),
    val timeStatQueueT2: Stat = Stat(),
    val timeStatQueueAcr: Stat = Stat(),
    val timeStatQueueAcrToT3: Stat = Stat(),

    val employeeOccupancy : Double = 0.0,
    val busOccupancy : Double = 0.0
)



