package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class ZummarRepository(
    private val db: FirebaseFirestore,
    private val prefs: SharedPreferences? = null
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً للوصول إلى قاعدة البيانات السحابية")
    }

    // =========================================================================
    // 1. CLOUD FIRESTORE CRUD & REALTIME SNAPSHOT FLOWS
    // =========================================================================

    fun observeZones(): Flow<List<ZoneDocument>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "zones"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ZoneDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getZoneById(zoneId: String): Result<ZoneDocument> = runCatching {
        val docRef = db.collection("zones").document(zoneId)
        try {
            val snapshot = docRef.get().await()
            snapshot.toObject(
                ZoneDocument::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: throw NoSuchElementException("Zone not found")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createZone(zone: ZoneDocument): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = if (zone.id.isNotBlank()) {
            db.collection("zones").document(zone.id)
        } else {
            db.collection("zones").document()
        }
        val payload = mapOf(
            "userId" to uid,
            "name" to zone.name.take(100),
            "powerStatus" to zone.powerStatus,
            "powerStartTime" to zone.powerStartTime.take(60),
            "powerOffDuration" to zone.powerOffDuration.take(60),
            "powerFaultReason" to zone.powerFaultReason.take(200),
            "powerExpectedFix" to zone.powerExpectedFix.take(100),
            "waterStatus" to zone.waterStatus,
            "waterOpenTime" to zone.waterOpenTime.take(60),
            "waterDuration" to zone.waterDuration.take(60),
            "waterFaultNote" to zone.waterFaultNote.take(200),
            "waterExpectedReturn" to zone.waterExpectedReturn.take(100),
            "coordsText" to zone.coordsText.take(2000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateZone(zone: ZoneDocument): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(zone.id.isNotBlank()) { "Zone ID must not be blank" }
        val docRef = db.collection("zones").document(zone.id)
        val updates = mapOf(
            "name" to zone.name.take(100),
            "powerStatus" to zone.powerStatus,
            "powerStartTime" to zone.powerStartTime.take(60),
            "powerOffDuration" to zone.powerOffDuration.take(60),
            "powerFaultReason" to zone.powerFaultReason.take(200),
            "powerExpectedFix" to zone.powerExpectedFix.take(100),
            "waterStatus" to zone.waterStatus,
            "waterOpenTime" to zone.waterOpenTime.take(60),
            "waterDuration" to zone.waterDuration.take(60),
            "waterFaultNote" to zone.waterFaultNote.take(200),
            "waterExpectedReturn" to zone.waterExpectedReturn.take(100),
            "coordsText" to zone.coordsText.take(2000),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteZone(zoneId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("zones").document(zoneId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeFacilities(): Flow<List<FacilityDocument>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "facilities"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FacilityDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createFacility(facility: FacilityDocument): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = if (facility.id.isNotBlank()) {
            db.collection("facilities").document(facility.id)
        } else {
            db.collection("facilities").document()
        }
        val payload = mapOf(
            "userId" to uid,
            "name" to facility.name.take(100),
            "facilityType" to facility.facilityType,
            "specialty" to facility.specialty.take(120),
            "address" to facility.address.take(200),
            "workHours" to facility.workHours.take(120),
            "isOnDuty" to facility.isOnDuty,
            "approvalStatus" to facility.approvalStatus,
            "mapX" to facility.mapX.coerceIn(0.0, 1.0),
            "mapY" to facility.mapY.coerceIn(0.0, 1.0),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateFacility(facility: FacilityDocument): Result<Unit> = runCatching {
        requireUserId()
        require(facility.id.isNotBlank()) { "Facility ID must not be blank" }
        val docRef = db.collection("facilities").document(facility.id)
        val updates = mapOf(
            "name" to facility.name.take(100),
            "facilityType" to facility.facilityType,
            "specialty" to facility.specialty.take(120),
            "address" to facility.address.take(200),
            "workHours" to facility.workHours.take(120),
            "isOnDuty" to facility.isOnDuty,
            "approvalStatus" to facility.approvalStatus,
            "mapX" to facility.mapX.coerceIn(0.0, 1.0),
            "mapY" to facility.mapY.coerceIn(0.0, 1.0),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAppointments(): Flow<List<AppointmentDocument>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "appointments"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AppointmentDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createAppointment(appointment: AppointmentDocument): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = if (appointment.id.isNotBlank()) {
            db.collection("appointments").document(appointment.id)
        } else {
            db.collection("appointments").document()
        }
        val payload = mapOf(
            "userId" to uid,
            "facilityId" to appointment.facilityId.take(128),
            "facilityName" to appointment.facilityName.take(100),
            "doctorSpecialty" to appointment.doctorSpecialty.take(120),
            "patientName" to appointment.patientName.take(100),
            "patientPhone" to appointment.patientPhone.take(40),
            "appointmentDate" to appointment.appointmentDate.take(40),
            "serialNumber" to appointment.serialNumber.coerceIn(1L, 1000000L),
            "status" to appointment.status,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("appointments").document(appointmentId)
        val updates = mapOf(
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeStores(): Flow<List<StoreDocument>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "stores"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StoreDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createStore(store: StoreDocument): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = if (store.id.isNotBlank()) {
            db.collection("stores").document(store.id)
        } else {
            db.collection("stores").document()
        }
        val payload = mapOf(
            "userId" to uid,
            "name" to store.name.take(100),
            "category" to store.category.take(80),
            "description" to store.description.take(300),
            "address" to store.address.take(200),
            "approvalStatus" to store.approvalStatus,
            "mapX" to store.mapX.coerceIn(0.0, 1.0),
            "mapY" to store.mapY.coerceIn(0.0, 1.0),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStore(store: StoreDocument): Result<Unit> = runCatching {
        requireUserId()
        require(store.id.isNotBlank()) { "Store ID must not be blank" }
        val docRef = db.collection("stores").document(store.id)
        val updates = mapOf(
            "name" to store.name.take(100),
            "category" to store.category.take(80),
            "description" to store.description.take(300),
            "address" to store.address.take(200),
            "approvalStatus" to store.approvalStatus,
            "mapX" to store.mapX.coerceIn(0.0, 1.0),
            "mapY" to store.mapY.coerceIn(0.0, 1.0),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeProducts(): Flow<List<ProductDocument>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "products"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ProductDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createProduct(product: ProductDocument): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = if (product.id.isNotBlank()) {
            db.collection("products").document(product.id)
        } else {
            db.collection("products").document()
        }
        val payload = mapOf(
            "userId" to uid,
            "storeId" to product.storeId.take(128),
            "storeName" to product.storeName.take(100),
            "name" to product.name.take(100),
            "category" to product.category.take(80),
            "priceIqd" to product.priceIqd.coerceIn(0L, 1000000000L),
            "description" to product.description.take(300),
            "inStock" to product.inStock,
            "visualStyle" to product.visualStyle.take(60),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeMessages(): Flow<List<MessageDocument>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "messages"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MessageDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createMessage(message: MessageDocument): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = if (message.id.isNotBlank()) {
            db.collection("messages").document(message.id)
        } else {
            db.collection("messages").document()
        }
        val payload = mapOf(
            "userId" to uid,
            "targetId" to message.targetId.take(128),
            "targetName" to message.targetName.take(100),
            "targetType" to message.targetType,
            "senderRole" to message.senderRole.take(60),
            "senderName" to message.senderName.take(100),
            "content" to message.content.take(500),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // =========================================================================
    // 2. SHAREDPREFERENCES JSON PERSISTENCE MIRROR (100% Permanent Local Backup)
    // =========================================================================

    fun saveRolePreference(role: ZummarRole) {
        prefs?.edit()?.putString(KEY_ACTIVE_ROLE, role.name)?.apply()
    }

    fun loadRolePreference(): ZummarRole {
        val raw = prefs?.getString(KEY_ACTIVE_ROLE, ZummarRole.CITIZEN.name) ?: ZummarRole.CITIZEN.name
        return try {
            ZummarRole.valueOf(raw)
        } catch (e: Exception) {
            ZummarRole.CITIZEN
        }
    }

    fun saveZonesToLocalJson(zones: List<ZoneDocument>) {
        val arr = JSONArray()
        zones.forEach { z ->
            arr.put(
                JSONObject().apply {
                    put("id", z.id)
                    put("userId", z.userId)
                    put("name", z.name)
                    put("powerStatus", z.powerStatus)
                    put("powerStartTime", z.powerStartTime)
                    put("powerOffDuration", z.powerOffDuration)
                    put("powerFaultReason", z.powerFaultReason)
                    put("powerExpectedFix", z.powerExpectedFix)
                    put("waterStatus", z.waterStatus)
                    put("waterOpenTime", z.waterOpenTime)
                    put("waterDuration", z.waterDuration)
                    put("waterFaultNote", z.waterFaultNote)
                    put("waterExpectedReturn", z.waterExpectedReturn)
                    put("coordsText", z.coordsText)
                }
            )
        }
        prefs?.edit()?.putString(KEY_ZONES_JSON, arr.toString())?.apply()
    }

    fun loadZonesFromLocalJson(): List<ZoneDocument> {
        val raw = prefs?.getString(KEY_ZONES_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ZoneDocument(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            name = obj.optString("name"),
                            powerStatus = obj.optString("powerStatus", "NATIONAL"),
                            powerStartTime = obj.optString("powerStartTime", "08:00 صباحاً"),
                            powerOffDuration = obj.optString("powerOffDuration", "3 ساعات"),
                            powerFaultReason = obj.optString("powerFaultReason", "لا يوجد خلل"),
                            powerExpectedFix = obj.optString("powerExpectedFix", "مستقر"),
                            waterStatus = obj.optString("waterStatus", "PUMPING"),
                            waterOpenTime = obj.optString("waterOpenTime", "06:00 صباحاً"),
                            waterDuration = obj.optString("waterDuration", "6 ساعات"),
                            waterFaultNote = obj.optString("waterFaultNote", "الضخ منتظم"),
                            waterExpectedReturn = obj.optString("waterExpectedReturn", "مستمر"),
                            coordsText = obj.optString("coordsText", "0.25,0.25;0.45,0.25;0.45,0.45;0.25,0.45")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFacilitiesToLocalJson(facilities: List<FacilityDocument>) {
        val arr = JSONArray()
        facilities.forEach { f ->
            arr.put(
                JSONObject().apply {
                    put("id", f.id)
                    put("userId", f.userId)
                    put("name", f.name)
                    put("facilityType", f.facilityType)
                    put("specialty", f.specialty)
                    put("address", f.address)
                    put("workHours", f.workHours)
                    put("isOnDuty", f.isOnDuty)
                    put("approvalStatus", f.approvalStatus)
                    put("mapX", f.mapX)
                    put("mapY", f.mapY)
                }
            )
        }
        prefs?.edit()?.putString(KEY_FACILITIES_JSON, arr.toString())?.apply()
    }

    fun loadFacilitiesFromLocalJson(): List<FacilityDocument> {
        val raw = prefs?.getString(KEY_FACILITIES_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        FacilityDocument(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            name = obj.optString("name"),
                            facilityType = obj.optString("facilityType", "PHARMACY"),
                            specialty = obj.optString("specialty"),
                            address = obj.optString("address"),
                            workHours = obj.optString("workHours"),
                            isOnDuty = obj.optBoolean("isOnDuty", false),
                            approvalStatus = obj.optString("approvalStatus", "APPROVED"),
                            mapX = obj.optDouble("mapX", 0.5),
                            mapY = obj.optDouble("mapY", 0.5)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAppointmentsToLocalJson(appointments: List<AppointmentDocument>) {
        val arr = JSONArray()
        appointments.forEach { a ->
            arr.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("userId", a.userId)
                    put("facilityId", a.facilityId)
                    put("facilityName", a.facilityName)
                    put("doctorSpecialty", a.doctorSpecialty)
                    put("patientName", a.patientName)
                    put("patientPhone", a.patientPhone)
                    put("appointmentDate", a.appointmentDate)
                    put("serialNumber", a.serialNumber)
                    put("status", a.status)
                }
            )
        }
        prefs?.edit()?.putString(KEY_APPOINTMENTS_JSON, arr.toString())?.apply()
    }

    fun loadAppointmentsFromLocalJson(): List<AppointmentDocument> {
        val raw = prefs?.getString(KEY_APPOINTMENTS_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        AppointmentDocument(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            facilityId = obj.optString("facilityId"),
                            facilityName = obj.optString("facilityName"),
                            doctorSpecialty = obj.optString("doctorSpecialty"),
                            patientName = obj.optString("patientName"),
                            patientPhone = obj.optString("patientPhone"),
                            appointmentDate = obj.optString("appointmentDate"),
                            serialNumber = obj.optLong("serialNumber", 1L),
                            status = obj.optString("status", "CONFIRMED")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveStoresToLocalJson(stores: List<StoreDocument>) {
        val arr = JSONArray()
        stores.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("userId", s.userId)
                    put("name", s.name)
                    put("category", s.category)
                    put("description", s.description)
                    put("address", s.address)
                    put("approvalStatus", s.approvalStatus)
                    put("mapX", s.mapX)
                    put("mapY", s.mapY)
                }
            )
        }
        prefs?.edit()?.putString(KEY_STORES_JSON, arr.toString())?.apply()
    }

    fun loadStoresFromLocalJson(): List<StoreDocument> {
        val raw = prefs?.getString(KEY_STORES_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        StoreDocument(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            name = obj.optString("name"),
                            category = obj.optString("category"),
                            description = obj.optString("description"),
                            address = obj.optString("address"),
                            approvalStatus = obj.optString("approvalStatus", "APPROVED"),
                            mapX = obj.optDouble("mapX", 0.5),
                            mapY = obj.optDouble("mapY", 0.5)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveProductsToLocalJson(products: List<ProductDocument>) {
        val arr = JSONArray()
        products.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("userId", p.userId)
                    put("storeId", p.storeId)
                    put("storeName", p.storeName)
                    put("name", p.name)
                    put("category", p.category)
                    put("priceIqd", p.priceIqd)
                    put("description", p.description)
                    put("inStock", p.inStock)
                    put("visualStyle", p.visualStyle)
                }
            )
        }
        prefs?.edit()?.putString(KEY_PRODUCTS_JSON, arr.toString())?.apply()
    }

    fun loadProductsFromLocalJson(): List<ProductDocument> {
        val raw = prefs?.getString(KEY_PRODUCTS_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ProductDocument(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            storeId = obj.optString("storeId"),
                            storeName = obj.optString("storeName"),
                            name = obj.optString("name"),
                            category = obj.optString("category"),
                            priceIqd = obj.optLong("priceIqd", 1000L),
                            description = obj.optString("description"),
                            inStock = obj.optBoolean("inStock", true),
                            visualStyle = obj.optString("visualStyle", "GOLD_BOX")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMessagesToLocalJson(messages: List<MessageDocument>) {
        val arr = JSONArray()
        messages.forEach { m ->
            arr.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("userId", m.userId)
                    put("targetId", m.targetId)
                    put("targetName", m.targetName)
                    put("targetType", m.targetType)
                    put("senderRole", m.senderRole)
                    put("senderName", m.senderName)
                    put("content", m.content)
                }
            )
        }
        prefs?.edit()?.putString(KEY_MESSAGES_JSON, arr.toString())?.apply()
    }

    fun loadMessagesFromLocalJson(): List<MessageDocument> {
        val raw = prefs?.getString(KEY_MESSAGES_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        MessageDocument(
                            id = obj.optString("id"),
                            userId = obj.optString("userId"),
                            targetId = obj.optString("targetId"),
                            targetName = obj.optString("targetName"),
                            targetType = obj.optString("targetType", "PHARMACY"),
                            senderRole = obj.optString("senderRole"),
                            senderName = obj.optString("senderName"),
                            content = obj.optString("content")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS_NAME = "smart_zummar_gate_permanent_prefs"
        private const val KEY_ACTIVE_ROLE = "active_role"
        private const val KEY_ZONES_JSON = "zones_json"
        private const val KEY_FACILITIES_JSON = "facilities_json"
        private const val KEY_APPOINTMENTS_JSON = "appointments_json"
        private const val KEY_STORES_JSON = "stores_json"
        private const val KEY_PRODUCTS_JSON = "products_json"
        private const val KEY_MESSAGES_JSON = "messages_json"
    }
}
