package newsstand.components.entity

data class Customer(
    val arrivedToSystem: Double,
    val building: Building
) {
    override fun toString() = "${arrivedToSystem.toString()} $building"
}

data class Group(
    val leader : Customer,
    val family : MutableList<Customer> = mutableListOf()
){
    fun add(customer: Customer) = family.add(customer)
    fun everyone() = listOf(listOf(leader),family).flatten().toMutableList()
    fun size() = everyone().size
    fun arrivedToSystem() = leader.arrivedToSystem
    fun building() = leader.building
}

