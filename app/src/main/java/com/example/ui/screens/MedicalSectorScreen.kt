package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.AppointmentDocument
import com.example.data.FacilityDocument
import com.example.data.MessageDocument
import com.example.data.NormalizedPoint
import com.example.data.ZoneDocument
import com.example.data.ZummarRole
import com.example.ui.components.CustomVectorSymbol
import com.example.ui.components.VectorSymbol
import com.example.ui.components.ZummarCalendarPickerDialog
import com.example.ui.map.MapVisualizationMode
import com.example.ui.map.ZoneDrawingMode
import com.example.ui.map.ZummarInteractiveMap
import com.example.ui.theme.RoyalNavyCard
import com.example.ui.theme.RoyalNavyDeep
import com.example.ui.theme.RoyalNavySurface
import com.example.ui.theme.TextIvory
import com.example.ui.theme.TextSilver
import com.example.ui.theme.ZummarAlertRed
import com.example.ui.theme.ZummarCyan
import com.example.ui.theme.ZummarCyanLight
import com.example.ui.theme.ZummarEmerald
import com.example.ui.theme.ZummarEmeraldGlow
import com.example.ui.theme.ZummarGold
import com.example.ui.theme.ZummarGoldSoft

@Composable
fun MedicalSectorScreen(
    activeRole: ZummarRole,
    zones: List<ZoneDocument>,
    facilities: List<FacilityDocument>,
    appointments: List<AppointmentDocument>,
    messages: List<MessageDocument>,
    selectedFacilityId: String?,
    drawingMode: ZoneDrawingMode,
    pickedPoint: NormalizedPoint?,
    onSelectFacility: (FacilityDocument) -> Unit,
    onSetDrawingMode: (ZoneDrawingMode) -> Unit,
    onPickLocation: (NormalizedPoint) -> Unit,
    onRegisterFacility: (String, String, String, String, String, Boolean) -> Unit,
    onToggleOnDuty: (String) -> Unit,
    onBookAppointment: (FacilityDocument, String, String, String) -> Unit,
    onSendMessage: (String, String, String, String, String) -> Unit,
    onSelectRole: (ZummarRole) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val visibleFacilities = if (activeRole == ZummarRole.SUPER_ADMIN || activeRole == ZummarRole.MEDICAL_SECTOR) {
        facilities
    } else {
        facilities.filter { it.approvalStatus == "APPROVED" }
    }
    val onDutyPharmacies = visibleFacilities.filter { it.facilityType == "PHARMACY" && it.isOnDuty }
    val selectedFacility = visibleFacilities.find { it.id == selectedFacilityId } ?: visibleFacilities.firstOrNull()

    var showBookingModal by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showChatModal by remember { mutableStateOf(false) }
    var showRegistrationForm by remember { mutableStateOf(false) }

    var patientName by remember { mutableStateOf("") }
    var patientPhone by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("2026-11-16 (16 تشرين الثاني)") }

    // Facility Registration Inputs
    var regName by remember { mutableStateOf("") }
    var regType by remember { mutableStateOf("PHARMACY") }
    var regSpecialty by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regHours by remember { mutableStateOf("24 ساعة - مناوبة") }
    var regOnDuty by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTopHeader(
            title = "الصيدليات الخافرة والعيادات والمختبرات",
            subtitle = "رادار الصيدليات الخافرة • الحجز الإلكتروني والمراسلة",
            symbol = VectorSymbol.PHARMACY_CHALICE,
            accentColor = ZummarEmerald,
            onBack = onBack
        )

        // 1. Live On-Duty Pharmacies Top Bar (`الصيدليات الخافرة في زمار الآن`)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(ZummarEmerald.copy(alpha = 0.22f), RoyalNavyCard)
                    )
                )
                .border(1.5.dp, ZummarEmerald, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.RADAR_BEACON,
                    modifier = Modifier.size(28.dp),
                    primaryColor = ZummarEmeraldGlow,
                    secondaryColor = ZummarEmerald
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الصيدليات الخافرة في زمار الآن (${onDutyPharmacies.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZummarEmeraldGlow
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onDutyPharmacies.forEach { pharm ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoyalNavyDeep)
                            .border(1.dp, ZummarEmeraldGlow, RoundedCornerShape(12.dp))
                            .clickable { onSelectFacility(pharm) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(pharm.name, style = MaterialTheme.typography.labelLarge, color = TextIvory)
                            Text(pharm.address, style = MaterialTheme.typography.bodyMedium, color = ZummarEmeraldGlow)
                        }
                    }
                }
            }
        }

        // 2. Interactive Zummar Map in MEDICAL Mode
        ZummarInteractiveMap(
            mode = MapVisualizationMode.MEDICAL,
            zones = zones,
            facilities = visibleFacilities,
            stores = emptyList(),
            selectedZoneId = null,
            selectedFacilityId = selectedFacility?.id,
            selectedStoreId = null,
            drawingMode = drawingMode,
            draftPoints = emptyList(),
            pickedPoint = pickedPoint,
            onSelectZone = {},
            onSelectFacility = onSelectFacility,
            onSelectStore = {},
            onAddDraftPoint = {},
            onSetDraftPoints = {},
            onPickLocation = onPickLocation
        )

        // Filter / Selector Pills for All Medical Facilities
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleFacilities.forEach { fac ->
                val isSelected = fac.id == selectedFacility?.id
                val symbol = when (fac.facilityType) {
                    "PHARMACY" -> VectorSymbol.PHARMACY_CHALICE
                    "CLINIC" -> VectorSymbol.CLINIC_PULSE
                    else -> VectorSymbol.LAB_FLASK
                }
                val color = when (fac.facilityType) {
                    "PHARMACY" -> ZummarEmerald
                    "CLINIC" -> ZummarCyanLight
                    else -> ZummarGold
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) RoyalNavyCard else RoyalNavySurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) color else color.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectFacility(fac) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomVectorSymbol(
                            symbol = symbol,
                            modifier = Modifier.size(20.dp),
                            primaryColor = color,
                            secondaryColor = TextIvory
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(fac.name, style = MaterialTheme.typography.labelLarge, color = TextIvory)
                    }
                }
            }
        }

        // 3. Detailed Card for Selected Pharmacy / Clinic / Lab
        if (selectedFacility != null) {
            val typeLabel = when (selectedFacility.facilityType) {
                "PHARMACY" -> if (selectedFacility.isOnDuty) "صيدلية خافرة الآن (مناوبة)" else "صيدلية مجازة"
                "CLINIC" -> "عيادة طبية تخصصية"
                else -> "مختبر تحليلات مرضية"
            }
            val cardAccent = when (selectedFacility.facilityType) {
                "PHARMACY" -> ZummarEmerald
                "CLINIC" -> ZummarCyanLight
                else -> ZummarGold
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RoyalNavyCard)
                    .border(1.5.dp, cardAccent.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedFacility.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextIvory
                        )
                        Text(
                            text = "$typeLabel • ${if (selectedFacility.approvalStatus == "APPROVED") "معتمد من الإدارة" else "بانتظار موافقة الإدارة"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = cardAccent
                        )
                    }
                }

                InfoRowGlass("الاختصاص / الخدمات:", selectedFacility.specialty, TextIvory)
                InfoRowGlass("العنوان على الخريطة:", selectedFacility.address, ZummarCyanLight)
                InfoRowGlass("أوقات الدوام والتواجد:", selectedFacility.workHours, ZummarGoldSoft)

                // Action Buttons: Online Chat & Electronic Appointment Booking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(ZummarEmerald, ZummarCyan))
                            )
                            .clickable { showChatModal = true }
                            .testTag("chat_with_facility_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CustomVectorSymbol(
                                symbol = VectorSymbol.CHAT_BUBBLE,
                                modifier = Modifier.size(20.dp),
                                primaryColor = RoyalNavyDeep,
                                secondaryColor = RoyalNavySurface,
                                animated = false
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedFacility.facilityType == "PHARMACY") "مراسلة الصيدلية أونلاين فوراً" else "مراسلة العيادة / المختبر",
                                style = MaterialTheme.typography.labelLarge,
                                color = RoyalNavyDeep
                            )
                        }
                    }

                    if (selectedFacility.facilityType == "CLINIC" || selectedFacility.facilityType == "LAB") {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(ZummarGold, ZummarGoldSoft))
                                )
                                .clickable { showBookingModal = true }
                                .testTag("book_doctor_appointment_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CustomVectorSymbol(
                                    symbol = VectorSymbol.CALENDAR_GRID,
                                    modifier = Modifier.size(20.dp),
                                    primaryColor = RoyalNavyDeep,
                                    secondaryColor = RoyalNavySurface,
                                    animated = false
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حجز موعد إلكتروني",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RoyalNavyDeep
                                )
                            }
                        }
                    }
                }

                // Medical Sector / Admin Quick Toggle for On-Duty Pharmacy
                if ((activeRole == ZummarRole.MEDICAL_SECTOR || activeRole == ZummarRole.SUPER_ADMIN) &&
                    selectedFacility.facilityType == "PHARMACY"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedFacility.isOnDuty) ZummarAlertRed.copy(alpha = 0.25f) else ZummarEmerald.copy(alpha = 0.25f))
                            .border(1.dp, if (selectedFacility.isOnDuty) ZummarAlertRed else ZummarEmerald, RoundedCornerShape(12.dp))
                            .clickable { onToggleOnDuty(selectedFacility.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedFacility.isOnDuty) "إيقاف شارة الصيدلية الخافرة" else "تفعيل شارة (صيدلية خافرة) على الخريطة",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextIvory
                        )
                    }
                }
            }
        }

        // 4. Serialized Electronic Booking Cards (`بطاقة حجز برقم تسلسلي`)
        if (appointments.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RoyalNavySurface)
                    .border(1.dp, ZummarCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "بطاقات الحجز الإلكتروني الصادرة (${appointments.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = ZummarGoldSoft
                )
                appointments.forEach { apt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RoyalNavyCard)
                            .border(1.dp, ZummarEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${apt.facilityName} • المريض: ${apt.patientName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextIvory
                            )
                            Text(
                                text = "التاريخ: ${apt.appointmentDate} | هاتف: ${apt.patientPhone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSilver
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZummarGold)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "تسلسل #${apt.serialNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                color = RoyalNavyDeep
                            )
                        }
                    }
                }
            }
        }

        // 5. Medical Account Registration Form (`تسجيل الحسابات الطبية واعتماد الإدارة`)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(RoyalNavySurface)
                .border(1.dp, ZummarEmerald.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRegistrationForm = !showRegistrationForm },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomVectorSymbol(
                        symbol = VectorSymbol.PLUS_ADD,
                        modifier = Modifier.size(24.dp),
                        primaryColor = ZummarEmeraldGlow,
                        secondaryColor = ZummarGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل صيدلية / عيادة طبيب / مختبر جديد على الخريطة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZummarEmeraldGlow
                    )
                }
            }

            if (showRegistrationForm || activeRole == ZummarRole.MEDICAL_SECTOR) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "PHARMACY" to "صيدلية",
                        "CLINIC" to "عيادة طبيب",
                        "LAB" to "مختبر تحليلات"
                    ).forEach { (typeKey, labelAr) ->
                        val selected = regType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) ZummarEmerald else RoyalNavyCard)
                                .clickable { regType = typeKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelAr,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) RoyalNavyDeep else TextIvory
                            )
                        }
                    }
                }

                ZummarTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = "اسم الصيدلية أو العيادة أو المختبر"
                )
                ZummarTextField(
                    value = regSpecialty,
                    onValueChange = { regSpecialty = it },
                    label = "الاختصاص الطبي أو الخدمات المتوفرة"
                )
                ZummarTextField(
                    value = regAddress,
                    onValueChange = { regAddress = it },
                    label = "العنوان في ناحية زمار"
                )
                ZummarTextField(
                    value = regHours,
                    onValueChange = { regHours = it },
                    label = "أوقات الدوام وأيام التواجد"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (drawingMode == ZoneDrawingMode.PICK_LOCATION) ZummarGold else RoyalNavyCard)
                            .border(1.dp, ZummarGold, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetDrawingMode(
                                    if (drawingMode == ZoneDrawingMode.PICK_LOCATION) ZoneDrawingMode.NONE
                                    else ZoneDrawingMode.PICK_LOCATION
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (drawingMode == ZoneDrawingMode.PICK_LOCATION) "انقر فوق الخريطة الآن..." else "تحديد الموقع على الخريطة",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (drawingMode == ZoneDrawingMode.PICK_LOCATION) RoyalNavyDeep else ZummarGoldSoft
                        )
                    }

                    if (regType == "PHARMACY") {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (regOnDuty) ZummarEmerald else RoyalNavyCard)
                                .border(1.dp, ZummarEmerald, RoundedCornerShape(12.dp))
                                .clickable { regOnDuty = !regOnDuty },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (regOnDuty) "شارة خافرة: مفعلة" else "شارة خافرة: غير مفعلة",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (regOnDuty) RoyalNavyDeep else TextIvory
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(ZummarEmerald, ZummarCyan)))
                        .clickable {
                            onRegisterFacility(regName, regType, regSpecialty, regAddress, regHours, regOnDuty)
                            regName = ""
                            regSpecialty = ""
                            regAddress = ""
                        }
                        .testTag("submit_medical_registration_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إرسال طلب التسجيل وتثبيت الموقع",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalNavyDeep
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Electronic Appointment Booking Dialog
    if (showBookingModal && selectedFacility != null) {
        Dialog(onDismissRequest = { showBookingModal = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = RoyalNavySurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ZummarGold, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "حجز موعد إلكتروني — ${selectedFacility.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = ZummarGoldSoft
                    )
                    Text(
                        text = "الاختصاص: ${selectedFacility.specialty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSilver
                    )

                    ZummarTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = "الاسم الكامل للمريض"
                    )
                    ZummarTextField(
                        value = patientPhone,
                        onValueChange = { patientPhone = it },
                        label = "رقم الهاتف للتواصل"
                    )

                    // Interactive Calendar Date Trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RoyalNavyCard)
                            .border(1.dp, ZummarEmerald, RoundedCornerShape(14.dp))
                            .clickable { showCalendarDialog = true }
                            .padding(14.dp)
                            .testTag("open_calendar_picker_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تاريخ الحجز: $selectedDate",
                                style = MaterialTheme.typography.labelLarge,
                                color = ZummarEmeraldGlow
                            )
                            CustomVectorSymbol(
                                symbol = VectorSymbol.CALENDAR_GRID,
                                modifier = Modifier.size(22.dp),
                                primaryColor = ZummarEmerald,
                                secondaryColor = ZummarGold,
                                animated = false
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.horizontalGradient(listOf(ZummarGold, ZummarEmerald)))
                                .clickable {
                                    onBookAppointment(selectedFacility, patientName, patientPhone, selectedDate)
                                    patientName = ""
                                    patientPhone = ""
                                    showBookingModal = false
                                }
                                .testTag("confirm_booking_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "إصدار بطاقة الحجز",
                                style = MaterialTheme.typography.labelLarge,
                                color = RoyalNavyDeep
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RoyalNavyCard)
                                .clickable { showBookingModal = false }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("إلغاء", style = MaterialTheme.typography.labelLarge, color = TextSilver)
                        }
                    }
                }
            }
        }
    }

    if (showCalendarDialog) {
        ZummarCalendarPickerDialog(
            initialDate = selectedDate,
            onDateSelected = { picked ->
                selectedDate = picked
                showCalendarDialog = false
            },
            onDismiss = { showCalendarDialog = false }
        )
    }

    if (showChatModal && selectedFacility != null) {
        OnlineDirectChatDialog(
            targetId = selectedFacility.id,
            targetName = selectedFacility.name,
            targetType = selectedFacility.facilityType,
            activeRole = activeRole,
            messages = messages.filter { it.targetId == selectedFacility.id },
            onSend = { senderName, text ->
                onSendMessage(
                    selectedFacility.id,
                    selectedFacility.name,
                    selectedFacility.facilityType,
                    senderName,
                    text
                )
            },
            onDismiss = { showChatModal = false }
        )
    }
}

@Composable
fun OnlineDirectChatDialog(
    targetId: String,
    targetName: String,
    targetType: String,
    activeRole: ZummarRole,
    messages: List<MessageDocument>,
    onSend: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var senderName by remember { mutableStateOf(activeRole.titleAr) }
    var messageInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RoyalNavySurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, ZummarCyanLight, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.CHAT_BUBBLE,
                            modifier = Modifier.size(26.dp),
                            primaryColor = ZummarCyanLight,
                            secondaryColor = ZummarEmeraldGlow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "محادثة فورية: $targetName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextIvory
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(RoyalNavyCard)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.CLOSE_CROSS,
                            modifier = Modifier.size(16.dp),
                            primaryColor = TextSilver,
                            secondaryColor = ZummarAlertRed,
                            animated = false
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RoyalNavyDeep)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        Text(
                            text = "ابدأ المحادثة الآن للاستفسار عن توفر دواء أو طلب بضاعة أو حجز خاص...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSilver
                        )
                    } else {
                        messages.forEach { msg ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoyalNavyCard)
                                    .border(0.8.dp, ZummarCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "${msg.senderName} (${msg.senderRole})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ZummarGoldSoft
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextIvory
                                )
                            }
                        }
                    }
                }

                ZummarTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = "اسم المرسل"
                )
                ZummarTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = "اكتب رسالتك أو طلبك هنا..."
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(ZummarCyan, ZummarEmerald)))
                        .clickable {
                            if (messageInput.isNotBlank()) {
                                onSend(senderName, messageInput)
                                messageInput = ""
                            }
                        }
                        .testTag("send_online_message_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إرسال الرسالة فوراً",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalNavyDeep
                    )
                }
            }
        }
    }
}
