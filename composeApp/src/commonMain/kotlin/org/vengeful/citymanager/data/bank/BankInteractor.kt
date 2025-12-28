package org.vengeful.citymanager.data.bank

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.data.USER_AGENT
import org.vengeful.citymanager.data.USER_AGENT_TAG
import org.vengeful.citymanager.data.client
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.users.CreateBankAccountRequest
import org.vengeful.citymanager.models.users.UpdateBankAccountRequest

class BankInteractor(
    private val authManager: AuthManager
): IBankInteractor {

    override suspend fun getAllBankAccounts(): List<BankAccount> {
        return try {
            val response = client.get("$SERVER_BASE_URL/bank/accounts") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<List<BankAccount>>()
            } else {
                throw Exception("HTTP error ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch bank accounts: ${e.message}")
        }
    }

    override suspend fun getBankAccountByPersonId(personId: Int): BankAccount? {
        return try {
            val response = client.get("$SERVER_BASE_URL/bank/accounts/person/$personId") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<BankAccount>()
            } else if (response.status.value == 404) {
                null
            } else {
                throw Exception("HTTP error ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch bank account: ${e.message}")
        }
    }

    override suspend fun getBankAccountById(id: Int): BankAccount? {
        return try {
            val response = client.get("$SERVER_BASE_URL/bank/accounts/$id") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<BankAccount>()
            } else if (response.status.value == 404) {
                null
            } else {
                throw Exception("HTTP error ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch bank account: ${e.message}")
        }
    }

    override suspend fun createBankAccount(
        personId: Int?,
        enterpriseName: String?,
        creditAmount: Double,
        personBalance: Double?
    ): BankAccount {
        return try {
            val request = CreateBankAccountRequest(
                personId = personId,
                enterpriseName = enterpriseName,
                creditAmount = creditAmount,
                personBalance = personBalance
            )
            val response: HttpResponse = client.post("$SERVER_BASE_URL/bank/accounts") {
                setHttpBuilder()
                setBody(request)
            }
            if (response.status.isSuccess()) {
                response.body<BankAccount>()
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    response.status.description
                }
                throw Exception("HTTP error ${response.status.value}: $errorBody")
            }
        } catch (e: Exception) {
            throw Exception("Failed to create bank account: ${e.message}")
        }
    }

    override suspend fun closeCredit(accountId: Int): BankAccount {
        return try {
            val response: HttpResponse = client.post("$SERVER_BASE_URL/bank/accounts/$accountId/close-credit") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<BankAccount>()
            } else {
                val errorBody = try {
                    response.body<Map<String, String>>()["error"] ?: response.status.description
                } catch (e: Exception) {
                    response.status.description
                }
                throw Exception("HTTP error ${response.status.value}: $errorBody")
            }
        } catch (e: Exception) {
            throw Exception("Failed to close credit: ${e.message}")
        }
    }

    override suspend fun getBankAccountByEnterpriseName(enterpriseName: String): BankAccount? {
        return try {
            val response = client.get("$SERVER_BASE_URL/bank/accounts/enterprise/$enterpriseName") {
                setHttpBuilder()
            }
            if (response.status.isSuccess()) {
                response.body<BankAccount>()
            } else if (response.status.value == 404) {
                null
            } else {
                throw Exception("HTTP error ${response.status} : ${response.status.description}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch bank account: ${e.message}")
        }
    }

    override suspend fun updateBankAccount(bankAccount: BankAccount, personBalance: Double?): Boolean {
        return try {
            val request = UpdateBankAccountRequest(
                id = bankAccount.id,
                personId = bankAccount.personId,
                enterpriseName = bankAccount.enterpriseName,
                creditAmount = bankAccount.creditAmount,
                personBalance = personBalance
            )
            val response: HttpResponse = client.put("$SERVER_BASE_URL/bank/accounts/${bankAccount.id}") {
                setHttpBuilder()
                setBody(request)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            throw Exception("Failed to update bank account: ${e.message}")
        }
    }

    override suspend fun deleteBankAccount(id: Int): Boolean {
        return try {
            val response: HttpResponse = client.delete("$SERVER_BASE_URL/bank/accounts/$id") {
                setHttpBuilder()
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            throw Exception("Failed to delete bank account: ${e.message}")
        }
    }

    private fun HttpRequestBuilder.setHttpBuilder(withAuth: Boolean = true) {
        contentType(ContentType.Application.Json)
        header(USER_AGENT_TAG, USER_AGENT)
        if (withAuth) {
            val token = authManager.getToken()
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            } else {
                println("WARNING: No token found in AuthManager for authenticated request")
            }
        }
    }

}
