package org.vengeful.citymanager.medicService.db


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.models.medicine.MedicineOrder

object Medicines : IntIdTable("medicines") {
    val name = varchar("name", 255)
    val price = double("price")
}

class MedicineDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MedicineDao>(Medicines)

    var name by Medicines.name
    var price by Medicines.price

    fun toMedicine() = Medicine(
        id = id.value,
        name = name,
        price = price
    )
}

object MedicineOrders : IntIdTable("medicine_orders") {
    val medicineId = integer("medicine_id")
    val medicineName = varchar("medicine_name", 255)
    val quantity = integer("quantity")
    val totalPrice = double("total_price")
    val accountId = integer("account_id")
    val orderedByPersonId = integer("ordered_by_person_id").nullable()
    val createdAt = long("created_at")
}

class MedicineOrderDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MedicineOrderDao>(MedicineOrders)

    var medicineId by MedicineOrders.medicineId
    var medicineName by MedicineOrders.medicineName
    var quantity by MedicineOrders.quantity
    var totalPrice by MedicineOrders.totalPrice
    var accountId by MedicineOrders.accountId
    var orderedByPersonId by MedicineOrders.orderedByPersonId
    var createdAt by MedicineOrders.createdAt

    fun toMedicineOrder() = MedicineOrder(
        id = id.value,
        medicineId = medicineId,
        medicineName = medicineName,
        quantity = quantity,
        totalPrice = totalPrice,
        accountId = accountId,
        orderedByPersonId = orderedByPersonId,
        createdAt = createdAt
    )
}
