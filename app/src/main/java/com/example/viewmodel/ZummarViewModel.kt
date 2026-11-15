package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppointmentDocument
import com.example.data.FacilityDocument
import com.example.data.MessageDocument
import com.example.data.NormalizedPoint
import com.example.data.PortalDestination
import com.example.data.ProductDocument
import com.example.data.StoreDocument
import com.example.data.ZoneDocument
import com.example.data.ZummarRepository
import com.example.data.ZummarRole
import com.example.ui.map.ZoneDrawingMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class ZummarViewModel(
    private val repository: ZummarRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _activeRole = MutableStateFlow(repository.loadRolePreference())
    val activeRole: StateFlow<ZummarRole> = _activeRole.asStateFlow()

    private val _currentDestination = MutableStateFlow(PortalDestination.WELCOME)
    val currentDestination: StateFlow<PortalDestination> = _currentDestination.asStateFlow()

    private val _zones = MutableStateFlow<List<ZoneDocument>>(repository.loadZonesFromLocalJson())
    val zones: StateFlow<List<ZoneDocument>> = _zones.asStateFlow()

    private val _facilities = MutableStateFlow<List<FacilityDocument>>(repository.loadFacilitiesFromLocalJson())
    val facilities: StateFlow<List<FacilityDocument>> = _facilities.asStateFlow()

    private val _appointments = MutableStateFlow<List<AppointmentDocument>>(repository.loadAppointmentsFromLocalJson())
    val appointments: StateFlow<List<AppointmentDocument>> = _appointments.asStateFlow()

    private val _stores = MutableStateFlow<List<StoreDocument>>(repository.loadStoresFromLocalJson())
    val stores: StateFlow<List<StoreDocument>> = _stores.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDocument>>(repository.loadProductsFromLocalJson())
    val products: StateFlow<List<ProductDocument>> = _products.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageDocument>>(repository.loadMessagesFromLocalJson())
    val messages: StateFlow<List<MessageDocument>> = _messages.asStateFlow()

    private val _selectedZoneId = MutableStateFlow<String?>(null)
    val selectedZoneId: StateFlow<String?> = _selectedZoneId.asStateFlow()

    private val _selectedFacilityId = MutableStateFlow<String?>(null)
    val selectedFacilityId: StateFlow<String?> = _selectedFacilityId.asStateFlow()

    private val _selectedStoreId = MutableStateFlow<String?>(null)
    val selectedStoreId: StateFlow<String?> = _selectedStoreId.asStateFlow()

    private val _drawingMode = MutableStateFlow(ZoneDrawingMode.NONE)
    val drawingMode: StateFlow<ZoneDrawingMode> = _drawingMode.asStateFlow()

    private val _draftPoints = MutableStateFlow<List<NormalizedPoint>>(emptyList())
    val draftPoints: StateFlow<List<NormalizedPoint>> = _draftPoints.asStateFlow()

    private val _pickedPoint = MutableStateFlow<NormalizedPoint?>(NormalizedPoint(0.52f, 0.48f))
    val pickedPoint: StateFlow<NormalizedPoint?> = _pickedPoint.asStateFlow()

    private val _bannerNotification = MutableStateFlow<String?>(null)
    val bannerNotification: StateFlow<String?> = _bannerNotification.asStateFlow()

    private var hasSeededInitialData = false

    init {
        observeAllStreams()
    }

    private fun observeAllStreams() {
        viewModelScope.launch {
            repository.observeZones()
                .catch { e -> unwrapAndLog("observeZones", e) }
                .collect { cloudZones ->
                    if (cloudZones.isEmpty() && !hasSeededInitialData) {
                        val local = repository.loadZonesFromLocalJson()
                        if (local.isEmpty()) {
                            seedInitialZummarPortalData()
                        } else {
                            _zones.value = local
                            local.forEach { repository.createZone(it) }
                        }
                    } else if (cloudZones.isNotEmpty()) {
                        hasSeededInitialData = true
                        _zones.value = cloudZones
                        repository.saveZonesToLocalJson(cloudZones)
                        if (_selectedZoneId.value == null) {
                            _selectedZoneId.value = cloudZones.firstOrNull()?.id
                        }
                    }
                }
        }

        viewModelScope.launch {
            repository.observeFacilities()
                .catch { e -> unwrapAndLog("observeFacilities", e) }
                .collect { cloudFacilities ->
                    if (cloudFacilities.isNotEmpty()) {
                        _facilities.value = cloudFacilities
                        repository.saveFacilitiesToLocalJson(cloudFacilities)
                        if (_selectedFacilityId.value == null) {
                            _selectedFacilityId.value = cloudFacilities.firstOrNull()?.id
                        }
                    }
                }
        }

        viewModelScope.launch {
            repository.observeAppointments()
                .catch { e -> unwrapAndLog("observeAppointments", e) }
                .collect { cloudAppointments ->
                    if (cloudAppointments.isNotEmpty()) {
                        _appointments.value = cloudAppointments
                        repository.saveAppointmentsToLocalJson(cloudAppointments)
                    }
                }
        }

        viewModelScope.launch {
            repository.observeStores()
                .catch { e -> unwrapAndLog("observeStores", e) }
                .collect { cloudStores ->
                    if (cloudStores.isNotEmpty()) {
                        _stores.value = cloudStores
                        repository.saveStoresToLocalJson(cloudStores)
                        if (_selectedStoreId.value == null) {
                            _selectedStoreId.value = cloudStores.firstOrNull()?.id
                        }
                    }
                }
        }

        viewModelScope.launch {
            repository.observeProducts()
                .catch { e -> unwrapAndLog("observeProducts", e) }
                .collect { cloudProducts ->
                    if (cloudProducts.isNotEmpty()) {
                        _products.value = cloudProducts
                        repository.saveProductsToLocalJson(cloudProducts)
                    }
                }
        }

        viewModelScope.launch {
            repository.observeMessages()
                .catch { e -> unwrapAndLog("observeMessages", e) }
                .collect { cloudMessages ->
                    if (cloudMessages.isNotEmpty()) {
                        _messages.value = cloudMessages
                        repository.saveMessagesToLocalJson(cloudMessages)
                    }
                }
        }
    }

    private fun unwrapAndLog(tag: String, error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w("ZummarVM", "Stream error in $tag: ${(firestoreError ?: error).message}")
    }

    private fun seedInitialZummarPortalData() {
        if (hasSeededInitialData) return
        hasSeededInitialData = true

        val initialZones = listOf(
            ZoneDocument(
                id = "zone_shuhada",
                userId = currentUserId,
                name = "حي الشهداء",
                powerStatus = "NATIONAL",
                powerStartTime = "08:00 صباحاً",
                powerOffDuration = "ساعتان فقط",
                powerFaultReason = "لا يوجد خلل - المغذي الرئيسي مستقر",
                powerExpectedFix = "يعمل بكفاءة عالية",
                waterStatus = "PUMPING",
                waterOpenTime = "06:30 صباحاً",
                waterDuration = "6 ساعات متواصلة",
                waterFaultNote = "ضخ إسالة زمار يعمل بضغط ممتاز",
                waterExpectedReturn = "مستمر",
                coordsText = "0.10,0.22;0.34,0.20;0.36,0.42;0.12,0.44"
            ),
            ZoneDocument(
                id = "zone_muallimeen",
                userId = currentUserId,
                name = "حي المعلمين",
                powerStatus = "GENERATOR",
                powerStartTime = "01:00 ظهراً",
                powerOffDuration = "3 ساعات",
                powerFaultReason = "التشغيل حالياً على المولدة الأهلية (مغذيات ذهبية)",
                powerExpectedFix = "عودة الوطنية 05:00 مساءً",
                waterStatus = "PUMPING",
                waterOpenTime = "07:00 صباحاً",
                waterDuration = "5 ساعات",
                waterFaultNote = "توزيع منتظم لقاطع المعلمين",
                waterExpectedReturn = "مستمر",
                coordsText = "0.40,0.20;0.64,0.20;0.64,0.42;0.40,0.42"
            ),
            ZoneDocument(
                id = "zone_markaz_zummar",
                userId = currentUserId,
                name = "مركز زمار",
                powerStatus = "NATIONAL",
                powerStartTime = "07:00 صباحاً",
                powerOffDuration = "ساعة واحدة",
                powerFaultReason = "خط الطوارئ والمركز التجاري يعمل باستمرار",
                powerExpectedFix = "مستقر",
                waterStatus = "PUMPING",
                waterOpenTime = "06:00 صباحاً",
                waterDuration = "8 ساعات",
                waterFaultNote = "الخط الناقل الرئيسي لمركز الناحية",
                waterExpectedReturn = "مستمر",
                coordsText = "0.36,0.46;0.64,0.46;0.66,0.68;0.34,0.68"
            ),
            ZoneDocument(
                id = "zone_ziraee",
                userId = currentUserId,
                name = "الحي الزراعي",
                powerStatus = "FAULT",
                powerStartTime = "متوقف مؤقتاً",
                powerOffDuration = "ساعتان للصيانة",
                powerFaultReason = "صيانة طارئة على محولة الحي الزراعي رقم 4",
                powerExpectedFix = "الإصلاح المتوقع الساعة 04:30 عصراً",
                waterStatus = "STOPPED",
                waterOpenTime = "05:00 مساءً",
                waterDuration = "4 ساعات مسائية",
                waterFaultNote = "بانتظار نوبة الضخ المسائية",
                waterExpectedReturn = "05:00 مساءً",
                coordsText = "0.08,0.52;0.30,0.52;0.30,0.78;0.08,0.78"
            ),
            ZoneDocument(
                id = "zone_ain_hilwa",
                userId = currentUserId,
                name = "عين حلوة",
                powerStatus = "OFF",
                powerStartTime = "06:00 مساءً",
                powerOffDuration = "4 ساعات",
                powerFaultReason = "ضمن جدول التناوب الاعتيادي",
                powerExpectedFix = "التشغيل القادم 06:00 مساءً",
                waterStatus = "MAINTENANCE",
                waterOpenTime = "متوقف للصيانة",
                waterDuration = "4 ساعات بعد الإصلاح",
                waterFaultNote = "معالجة نضح في الأنبوب الفرعي لقاطع عين حلوة",
                waterExpectedReturn = "عودة الضخ الساعة 07:00 مساءً",
                coordsText = "0.70,0.44;0.92,0.42;0.92,0.72;0.70,0.72"
            )
        )

        val initialFacilities = listOf(
            FacilityDocument(
                id = "fac_noor_pharmacy",
                userId = currentUserId,
                name = "صيدلية النور الخافرة - زمار",
                facilityType = "PHARMACY",
                specialty = "أدوية عامة ومستلزمات طبية وطوارئ",
                address = "مركز زمار - الشارع العام قرب تقاطع السوق",
                workHours = "مناوبة خافرة 24 ساعة",
                isOnDuty = true,
                approvalStatus = "APPROVED",
                mapX = 0.48,
                mapY = 0.54
            ),
            FacilityDocument(
                id = "fac_shifaa_pharmacy",
                userId = currentUserId,
                name = "صيدلية الشفاء الحديثة",
                facilityType = "PHARMACY",
                specialty = "أدوية تخصصية وأمراض مزمنة",
                address = "حي المعلمين - مقابل المدرسة المركزية",
                workHours = "08:00 صباحاً - 11:00 ليلاً",
                isOnDuty = true,
                approvalStatus = "APPROVED",
                mapX = 0.52,
                mapY = 0.32
            ),
            FacilityDocument(
                id = "fac_dr_ahmed_clinic",
                userId = currentUserId,
                name = "عيادة د. أحمد الموصلي الاستشارية",
                facilityType = "CLINIC",
                specialty = "اختصاص باطنية وقلبية وصدرية",
                address = "مركز زمار - مجمع العيادات الطبية",
                workHours = "السبت - الخميس (04:00 عصراً - 09:00 ليلاً)",
                isOnDuty = false,
                approvalStatus = "APPROVED",
                mapX = 0.42,
                mapY = 0.58
            ),
            FacilityDocument(
                id = "fac_dajla_lab",
                userId = currentUserId,
                name = "مختبر دجلة للتحليلات المرضية",
                facilityType = "LAB",
                specialty = "تحوصات شاملة، هرمونات، وأمراض الدم",
                address = "حي الشهداء - الشارع الخدمي",
                workHours = "07:30 صباحاً - 08:30 مساءً",
                isOnDuty = false,
                approvalStatus = "APPROVED",
                mapX = 0.24,
                mapY = 0.34
            )
        )

        val initialStores = listOf(
            StoreDocument(
                id = "store_baraka_market",
                userId = currentUserId,
                name = "أسواق البركة المركزية",
                category = "مواد غذائية وتموينية",
                description = "كافة المواد الغذائية الطازجة، البقوليات، الألبان، والمشروبات بأسعار الجملة والمفرد.",
                address = "سوق زمار الكبير - المدخل الأول",
                approvalStatus = "APPROVED",
                mapX = 0.54,
                mapY = 0.56
            ),
            StoreDocument(
                id = "store_rafidain_elec",
                userId = currentUserId,
                name = "معرض الرافدين للأجهزة والإنارة",
                category = "كهربائيات ومنزلية",
                description = "منظومات طاقة، بطاريات، شاشات، ومستلزمات كهربائية منزلية.",
                address = "مركز زمار - شارع المصارف",
                approvalStatus = "APPROVED",
                mapX = 0.46,
                mapY = 0.62
            ),
            StoreDocument(
                id = "store_zummar_bakery",
                userId = currentUserId,
                name = "مخابز ومعجنات سنابل زمار",
                category = "مخابز وحلويات",
                description = "صمون حار، خبز عراقي، كليجة موصلية ومعجنات طازجة يومياً.",
                address = "حي الشهداء - قرب الفلكة",
                approvalStatus = "APPROVED",
                mapX = 0.26,
                mapY = 0.36
            )
        )

        val initialProducts = listOf(
            ProductDocument(
                id = "prod_rice_amber",
                userId = currentUserId,
                storeId = "store_baraka_market",
                storeName = "أسواق البركة المركزية",
                name = "كيس رز عنبر درجه أولى (10 كغم)",
                category = "مواد غذائية وتموينية",
                priceIqd = 24000L,
                description = "رز عنبر عراقي فاخر مفحوص ونظيف إنتاج الموسم الجديد.",
                inStock = true,
                visualStyle = "GOLD_BOX"
            ),
            ProductDocument(
                id = "prod_olive_oil",
                userId = currentUserId,
                storeId = "store_baraka_market",
                storeName = "أسواق البركة المركزية",
                name = "زيت زيتون بكر ممتاز (1 لتر)",
                category = "مواد غذائية وتموينية",
                priceIqd = 11500L,
                description = "عصرة أولى على البارد طبيعي 100%.",
                inStock = true,
                visualStyle = "EMERALD_BOTTLE"
            ),
            ProductDocument(
                id = "prod_led_inverter",
                userId = currentUserId,
                storeId = "store_rafidain_elec",
                storeName = "معرض الرافدين للأجهزة والإنارة",
                name = "مصباح شحن ذكي طوارئ (40 واط)",
                category = "كهربائيات ومنزلية",
                priceIqd = 15000L,
                description = "يعمل تلقائياً عند انقطاع التيار الكهربائي لمدة 8 ساعات متواصلة.",
                inStock = true,
                visualStyle = "CYAN_TECH"
            ),
            ProductDocument(
                id = "prod_mosul_kleija",
                userId = currentUserId,
                storeId = "store_zummar_bakery",
                storeName = "مخابز ومعجنات سنابل زمار",
                name = "علبة كليجة زمار بالتمر والجوز (1 كغم)",
                category = "مخابز وحلويات",
                priceIqd = 8000L,
                description = "مخبوزة بالدهن الحر والهيل الطازج يومياً.",
                inStock = true,
                visualStyle = "GOLD_BOX"
            )
        )

        _zones.value = initialZones
        _facilities.value = initialFacilities
        _stores.value = initialStores
        _products.value = initialProducts
        _selectedZoneId.value = initialZones.first().id
        _selectedFacilityId.value = initialFacilities.first().id
        _selectedStoreId.value = initialStores.first().id

        repository.saveZonesToLocalJson(initialZones)
        repository.saveFacilitiesToLocalJson(initialFacilities)
        repository.saveStoresToLocalJson(initialStores)
        repository.saveProductsToLocalJson(initialProducts)

        viewModelScope.launch {
            initialZones.forEach { repository.createZone(it) }
            initialFacilities.forEach { repository.createFacility(it) }
            initialStores.forEach { repository.createStore(it) }
            initialProducts.forEach { repository.createProduct(it) }
        }
    }

    fun showNotification(msg: String) {
        _bannerNotification.value = msg
    }

    fun dismissNotification() {
        _bannerNotification.value = null
    }

    fun selectRole(role: ZummarRole) {
        _activeRole.value = role
        repository.saveRolePreference(role)
        showNotification("تم التبديل إلى صلاحية: ${role.titleAr}")
    }

    fun navigateTo(destination: PortalDestination) {
        _currentDestination.value = destination
        _drawingMode.value = ZoneDrawingMode.NONE
        _draftPoints.value = emptyList()
    }

    fun selectZone(zone: ZoneDocument) {
        _selectedZoneId.value = zone.id
    }

    fun selectFacility(facility: FacilityDocument) {
        _selectedFacilityId.value = facility.id
    }

    fun selectStore(store: StoreDocument) {
        _selectedStoreId.value = store.id
    }

    fun setDrawingMode(mode: ZoneDrawingMode) {
        _drawingMode.value = mode
        if (mode != ZoneDrawingMode.PICK_LOCATION) {
            _draftPoints.value = emptyList()
        }
    }

    fun addDraftPoint(pt: NormalizedPoint) {
        _draftPoints.value = _draftPoints.value + pt
    }

    fun setDraftPoints(pts: List<NormalizedPoint>) {
        _draftPoints.value = pts
    }

    fun clearDraftPoints() {
        _draftPoints.value = emptyList()
    }

    fun setPickedLocation(pt: NormalizedPoint) {
        _pickedPoint.value = pt
    }

    // Save a newly drawn Polygon or Freehand Lasso neighborhood zone
    fun saveNewDrawnZone(name: String) {
        val pts = _draftPoints.value
        if (name.isBlank() || pts.size < 3) {
            showNotification("يرجى كتابة اسم الحي وتحديد 3 نقاط على الأقل فوق الخريطة")
            return
        }
        val coordsText = pts.joinToString(";") {
            "${"%.3f".format(java.util.Locale.US, it.x)},${"%.3f".format(java.util.Locale.US, it.y)}"
        }
        val newZone = ZoneDocument(
            id = "zone_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = currentUserId,
            name = name.trim(),
            powerStatus = "NATIONAL",
            powerStartTime = "08:00 صباحاً",
            powerOffDuration = "ساعتان",
            powerFaultReason = "لا يوجد خلل",
            powerExpectedFix = "مستقر",
            waterStatus = "PUMPING",
            waterOpenTime = "06:00 صباحاً",
            waterDuration = "6 ساعات",
            waterFaultNote = "الضخ منتظم",
            waterExpectedReturn = "مستمر",
            coordsText = coordsText
        )

        val updatedList = _zones.value + newZone
        _zones.value = updatedList
        _selectedZoneId.value = newZone.id
        _drawingMode.value = ZoneDrawingMode.NONE
        _draftPoints.value = emptyList()
        repository.saveZonesToLocalJson(updatedList)

        viewModelScope.launch {
            repository.createZone(newZone)
            showNotification("تم حفظ الحي الجديد (${newZone.name}) على خريطة زمار بنجاح")
        }
    }

    fun updateZoneElectricity(
        zoneId: String,
        powerStatus: String,
        startTime: String,
        offDuration: String,
        faultReason: String,
        expectedFix: String
    ) {
        val current = _zones.value.find { it.id == zoneId } ?: return
        val updated = current.copy(
            powerStatus = powerStatus,
            powerStartTime = startTime.ifBlank { "غير محدد" },
            powerOffDuration = offDuration.ifBlank { "غير محدد" },
            powerFaultReason = faultReason.ifBlank { "لا يوجد خلل" },
            powerExpectedFix = expectedFix.ifBlank { "مستقر" }
        )
        val newList = _zones.value.map { if (it.id == zoneId) updated else it }
        _zones.value = newList
        repository.saveZonesToLocalJson(newList)

        viewModelScope.launch {
            repository.updateZone(updated)
            val statusAr = when (powerStatus) {
                "NATIONAL" -> "تشغيل الكهرباء الوطنية"
                "GENERATOR" -> "تشغيل المولدة الأهلية"
                "FAULT" -> "إعلان خلل فني / صيانة"
                else -> "إطفاء التيار"
            }
            showNotification("تم تحديث ${updated.name}: $statusAr")
        }
    }

    fun updateZoneWater(
        zoneId: String,
        waterStatus: String,
        openTime: String,
        duration: String,
        faultNote: String,
        expectedReturn: String
    ) {
        val current = _zones.value.find { it.id == zoneId } ?: return
        val updated = current.copy(
            waterStatus = waterStatus,
            waterOpenTime = openTime.ifBlank { "غير محدد" },
            waterDuration = duration.ifBlank { "غير محدد" },
            waterFaultNote = faultNote.ifBlank { "لا يوجد" },
            waterExpectedReturn = expectedReturn.ifBlank { "مستمر" }
        )
        val newList = _zones.value.map { if (it.id == zoneId) updated else it }
        _zones.value = newList
        repository.saveZonesToLocalJson(newList)

        viewModelScope.launch {
            repository.updateZone(updated)
            val statusAr = when (waterStatus) {
                "PUMPING" -> "فتح ضخ المياه"
                "MAINTENANCE" -> "تنبيه صيانة أنبوب"
                else -> "إيقاف الضخ"
            }
            showNotification("تم تحديث قاطع ماء ${updated.name}: $statusAr")
        }
    }

    fun registerMedicalFacility(
        name: String,
        facilityType: String,
        specialty: String,
        address: String,
        workHours: String,
        isOnDuty: Boolean
    ) {
        if (name.isBlank() || address.isBlank()) {
            showNotification("يرجى إدخال اسم المؤسسة الطبية والعنوان")
            return
        }
        val pt = _pickedPoint.value ?: NormalizedPoint(0.50f, 0.50f)
        val approval = if (_activeRole.value == ZummarRole.SUPER_ADMIN) "APPROVED" else "PENDING"
        val newFacility = FacilityDocument(
            id = "fac_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = currentUserId,
            name = name.trim(),
            facilityType = facilityType,
            specialty = specialty.ifBlank { "خدمات طبية عامة" },
            address = address.trim(),
            workHours = workHours.ifBlank { "08:00 صباحاً - 10:00 ليلاً" },
            isOnDuty = isOnDuty,
            approvalStatus = approval,
            mapX = pt.x.toDouble(),
            mapY = pt.y.toDouble()
        )
        val newList = _facilities.value + newFacility
        _facilities.value = newList
        _selectedFacilityId.value = newFacility.id
        _drawingMode.value = ZoneDrawingMode.NONE
        repository.saveFacilitiesToLocalJson(newList)

        viewModelScope.launch {
            repository.createFacility(newFacility)
            val msg = if (approval == "APPROVED") {
                "تم تسجيل واعتماد (${newFacility.name}) على خريطة زمار فوراً"
            } else {
                "تم إرسال طلب تسجيل (${newFacility.name}) إلى الإدارة العليا للمصادقة"
            }
            showNotification(msg)
        }
    }

    fun togglePharmacyOnDuty(facilityId: String) {
        val current = _facilities.value.find { it.id == facilityId } ?: return
        val updated = current.copy(isOnDuty = !current.isOnDuty)
        val newList = _facilities.value.map { if (it.id == facilityId) updated else it }
        _facilities.value = newList
        repository.saveFacilitiesToLocalJson(newList)

        viewModelScope.launch {
            repository.updateFacility(updated)
            val stateText = if (updated.isOnDuty) "تفعيل شارة صيدلية خافرة" else "إلغاء المناوبة الخافرة"
            showNotification("${updated.name}: $stateText")
        }
    }

    fun approveFacility(facilityId: String) {
        val current = _facilities.value.find { it.id == facilityId } ?: return
        val updated = current.copy(approvalStatus = "APPROVED")
        val newList = _facilities.value.map { if (it.id == facilityId) updated else it }
        _facilities.value = newList
        repository.saveFacilitiesToLocalJson(newList)

        viewModelScope.launch {
            repository.updateFacility(updated)
            showNotification("تمت المصادقة على (${updated.name}) وظهورها للعامة على الخريطة")
        }
    }

    fun bookDoctorAppointment(
        facility: FacilityDocument,
        patientName: String,
        patientPhone: String,
        appointmentDate: String
    ) {
        if (patientName.isBlank() || patientPhone.isBlank() || appointmentDate.isBlank()) {
            showNotification("يرجى إكمال اسم المريض ورقم الهاتف وتاريخ الحجز")
            return
        }
        val nextSerial = (_appointments.value.maxOfOrNull { it.serialNumber } ?: 100L) + 1L
        val booking = AppointmentDocument(
            id = "apt_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = currentUserId,
            facilityId = facility.id,
            facilityName = facility.name,
            doctorSpecialty = facility.specialty,
            patientName = patientName.trim(),
            patientPhone = patientPhone.trim(),
            appointmentDate = appointmentDate,
            serialNumber = nextSerial,
            status = "CONFIRMED"
        )
        val newList = listOf(booking) + _appointments.value
        _appointments.value = newList
        repository.saveAppointmentsToLocalJson(newList)

        viewModelScope.launch {
            repository.createAppointment(booking)
            showNotification("تم تأكيد حجز الموعد بنجاح — رقم البطاقة التسلسلي: #$nextSerial")
        }
    }

    fun registerStore(
        name: String,
        category: String,
        description: String,
        address: String
    ) {
        if (name.isBlank() || address.isBlank()) {
            showNotification("يرجى إدخال اسم المحل التجاري وعنوانه")
            return
        }
        val pt = _pickedPoint.value ?: NormalizedPoint(0.52f, 0.52f)
        val approval = if (_activeRole.value == ZummarRole.SUPER_ADMIN) "APPROVED" else "PENDING"
        val newStore = StoreDocument(
            id = "store_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = currentUserId,
            name = name.trim(),
            category = category.ifBlank { "تجارة عامة" },
            description = description.ifBlank { "متجر في سوق زمار" },
            address = address.trim(),
            approvalStatus = approval,
            mapX = pt.x.toDouble(),
            mapY = pt.y.toDouble()
        )
        val newList = _stores.value + newStore
        _stores.value = newList
        _selectedStoreId.value = newStore.id
        _drawingMode.value = ZoneDrawingMode.NONE
        repository.saveStoresToLocalJson(newList)

        viewModelScope.launch {
            repository.createStore(newStore)
            val msg = if (approval == "APPROVED") {
                "تم تسجيل واعتماد متجر (${newStore.name}) على الخريطة"
            } else {
                "تم إرسال طلب تسجيل متجر (${newStore.name}) لاعتماد الإدارة العليا"
            }
            showNotification(msg)
        }
    }

    fun approveStore(storeId: String) {
        val current = _stores.value.find { it.id == storeId } ?: return
        val updated = current.copy(approvalStatus = "APPROVED")
        val newList = _stores.value.map { if (it.id == storeId) updated else it }
        _stores.value = newList
        repository.saveStoresToLocalJson(newList)

        viewModelScope.launch {
            repository.updateStore(updated)
            showNotification("تم اعتماد محل (${updated.name}) في سوق زمار")
        }
    }

    fun addProductToStore(
        store: StoreDocument,
        name: String,
        category: String,
        priceIqdText: String,
        description: String,
        inStock: Boolean
    ) {
        val price = priceIqdText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (name.isBlank() || price <= 0L) {
            showNotification("يرجى إدخال اسم البضاعة والسعر بالدينار العراقي")
            return
        }
        val newProduct = ProductDocument(
            id = "prod_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = currentUserId,
            storeId = store.id,
            storeName = store.name,
            name = name.trim(),
            category = category.ifBlank { store.category },
            priceIqd = price,
            description = description.ifBlank { "متوفر في ${store.name}" },
            inStock = inStock,
            visualStyle = "GOLD_BOX"
        )
        val newList = listOf(newProduct) + _products.value
        _products.value = newList
        repository.saveProductsToLocalJson(newList)

        viewModelScope.launch {
            repository.createProduct(newProduct)
            showNotification("تم نشر المنتج (${newProduct.name}) في ${store.name} وسوق زمار الشامل")
        }
    }

    fun sendOnlineMessage(
        targetId: String,
        targetName: String,
        targetType: String,
        senderName: String,
        content: String
    ) {
        if (content.isBlank()) return
        val msg = MessageDocument(
            id = "msg_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            userId = currentUserId,
            targetId = targetId,
            targetName = targetName,
            targetType = targetType,
            senderRole = _activeRole.value.titleAr,
            senderName = senderName.ifBlank { _activeRole.value.titleAr },
            content = content.trim()
        )
        val newList = _messages.value + msg
        _messages.value = newList
        repository.saveMessagesToLocalJson(newList)

        viewModelScope.launch {
            repository.createMessage(msg)
            showNotification("تم إرسال رسالتك إلى $targetName")
        }
    }
}
