package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class ZummarRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveZone_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ZummarRepository(firestore)
        val uniqueId = "zone_${UUID.randomUUID().toString().replace("-", "")}"
        val zone = ZoneDocument(
            id = uniqueId,
            name = "حي الشهداء",
            powerStatus = "NATIONAL",
            waterStatus = "PUMPING",
            coordsText = "0.2,0.2;0.4,0.2;0.4,0.4;0.2,0.4"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createZone(zone) }
        assertTrue("Expected zone creation to succeed", result.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeZones().first { list -> list.any { it.id == uniqueId } }
        }
        assertTrue(emitted.any { it.id == uniqueId && it.name == "حي الشهداء" })
    }

    @Test
    fun getZoneById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = ZummarRepository(firestore)
        val uniqueId = "zone_${UUID.randomUUID().toString().replace("-", "")}"
        val zoneId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createZone(ZoneDocument(id = uniqueId, name = "حي المعلمين")).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = ZummarRepository(firestore)
        val bobResult = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getZoneById(zoneId) }
        assertTrue("Expected cross-user read to fail", bobResult.isFailure)
        val ex = bobResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeZones_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = ZummarRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeZones().first()
            }
            fail("Expected unauthenticated observation to fail")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@zummar.com"
        const val BOB_EMAIL = "bob@zummar.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
