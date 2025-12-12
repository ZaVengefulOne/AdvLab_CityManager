package org.vengeful.citymanager.medicService


import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.medicService.db.MedicineDao
import org.vengeful.citymanager.medicService.db.MedicineOrderDao
import org.vengeful.citymanager.medicService.db.Medicines
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.models.medicine.MedicineOrder

class MedicineRepository {

    fun getAllMedicines(): List<Medicine> = transaction {
        MedicineDao.all().map { it.toMedicine() }
    }

    fun getMedicineById(id: Int): Medicine? = transaction {
        MedicineDao.findById(id)?.toMedicine()
    }

    fun createMedicine(medicine: Medicine): Medicine = transaction {
        val medicineDao = MedicineDao.new {
            name = medicine.name
            price = medicine.price
        }
        medicineDao.toMedicine()
    }

    fun updateMedicine(medicine: Medicine): Medicine = transaction {
        val medicineDao = MedicineDao.findById(medicine.id)
            ?: throw IllegalStateException("Medicine with id ${medicine.id} not found")

        medicineDao.name = medicine.name
        medicineDao.price = medicine.price

        medicineDao.toMedicine()
    }

    fun deleteMedicine(id: Int): Boolean = transaction {
        MedicineDao.findById(id)?.delete() != null
    }

    fun createMedicineOrder(
        medicineId: Int,
        medicineName: String,
        quantity: Int,
        totalPrice: Double,
        accountId: Int,
        orderedByPersonId: Int?
    ): MedicineOrder = transaction {
        val orderDao = MedicineOrderDao.new {
            this.medicineId = medicineId
            this.medicineName = medicineName
            this.quantity = quantity
            this.totalPrice = totalPrice
            this.accountId = accountId
            this.orderedByPersonId = orderedByPersonId
            this.createdAt = System.currentTimeMillis()
        }
        orderDao.toMedicineOrder()
    }
}
