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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.NormalizedPoint
import com.example.data.ZoneDocument
import com.example.data.ZummarRole
import com.example.ui.components.CustomVectorSymbol
import com.example.ui.components.VectorSymbol
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
import com.example.ui.theme.ZummarGoldWarm

@Composable
fun ElectricityNetworkScreen(
    activeRole: ZummarRole,
    zones: List<ZoneDocument>,
    selectedZoneId: String?,
    drawingMode: ZoneDrawingMode,
    draftPoints: List<NormalizedPoint>,
    onSelectZone: (ZoneDocument) -> Unit,
    onSetDrawingMode: (ZoneDrawingMode) -> Unit,
    onAddDraftPoint: (NormalizedPoint) -> Unit,
    onSetDraftPoints: (List<NormalizedPoint>) -> Unit,
    onClearDraftPoints: () -> Unit,
    onSaveDrawnZone: (String) -> Unit,
    onUpdateElectricity: (String, String, String, String, String, String) -> Unit,
    onSelectRole: (ZummarRole) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val selectedZone = zones.find { it.id == selectedZoneId } ?: zones.firstOrNull()
    val canManageElectricity = activeRole == ZummarRole.ELECTRICITY_DEPT || activeRole == ZummarRole.SUPER_ADMIN

    var newZoneName by remember { mutableStateOf("") }
    var startTimeInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.powerStartTime ?: "08:00 صباحاً") }
    var offDurationInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.powerOffDuration ?: "3 ساعات") }
    var faultReasonInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.powerFaultReason ?: "لا يوجد خلل") }
    var expectedFixInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.powerExpectedFix ?: "مستقر") }

    LaunchedEffect(selectedZone) {
        if (selectedZone != null) {
            startTimeInput = selectedZone.powerStartTime
            offDurationInput = selectedZone.powerOffDuration
            faultReasonInput = selectedZone.powerFaultReason
            expectedFixInput = selectedZone.powerExpectedFix
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTopHeader(
            title = "شبكة الكهرباء الوطنية والمولدات",
            subtitle = "خريطة زمار التفاعلية • التيار الحي والأعطال",
            symbol = VectorSymbol.ELECTRIC_GRID,
            accentColor = ZummarGold,
            onBack = onBack
        )

        // Quick Role Helper Banner if Citizen wants to test Electricity Dept powers
        if (!canManageElectricity) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoyalNavyCard)
                    .border(1.dp, ZummarGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أنت تتصفح كـ (${activeRole.titleAr}). لتجربة رسم الأحياء والتحكم بالكهرباء:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSilver,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ZummarGold)
                        .clickable { onSelectRole(ZummarRole.ELECTRICITY_DEPT) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("switch_to_elec_role_button")
                ) {
                    Text("تفعيل حساب الكهرباء", style = MaterialTheme.typography.labelLarge, color = RoyalNavyDeep)
                }
            }
        }

        // Interactive Zummar Map in ELECTRICITY Mode
        ZummarInteractiveMap(
            mode = MapVisualizationMode.ELECTRICITY,
            zones = zones,
            facilities = emptyList(),
            stores = emptyList(),
            selectedZoneId = selectedZone?.id,
            selectedFacilityId = null,
            selectedStoreId = null,
            drawingMode = drawingMode,
            draftPoints = draftPoints,
            pickedPoint = null,
            onSelectZone = onSelectZone,
            onSelectFacility = {},
            onSelectStore = {},
            onAddDraftPoint = onAddDraftPoint,
            onSetDraftPoints = onSetDraftPoints,
            onPickLocation = {}
        )

        // Horizontal Neighborhood Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            zones.forEach { zone ->
                val isSelected = zone.id == selectedZone?.id
                val statusColor = when (zone.powerStatus) {
                    "NATIONAL" -> ZummarGold
                    "GENERATOR" -> ZummarGoldWarm
                    "FAULT" -> ZummarAlertRed
                    else -> TextSilver
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) RoyalNavyCard else RoyalNavySurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) statusColor else statusColor.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectZone(zone) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.ELECTRIC_GRID,
                            modifier = Modifier.size(18.dp),
                            primaryColor = statusColor,
                            secondaryColor = ZummarGoldSoft,
                            animated = zone.powerStatus == "NATIONAL" || zone.powerStatus == "GENERATOR"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextIvory
                        )
                    }
                }
            }
        }

        // Luxury Glassmorphic Citizen Card for Selected Neighborhood
        if (selectedZone != null) {
            val statusTitle = when (selectedZone.powerStatus) {
                "NATIONAL" -> "تعمل الآن (كهرباء وطنية)"
                "GENERATOR" -> "تعمل الآن (مولدة أهلية)"
                "FAULT" -> "خلل فني / صيانة طارئة"
                else -> "منطفئة حالياً"
            }
            val badgeColor = when (selectedZone.powerStatus) {
                "NATIONAL" -> ZummarGold
                "GENERATOR" -> ZummarGoldWarm
                "FAULT" -> ZummarAlertRed
                else -> TextSilver
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                RoyalNavyCard.copy(alpha = 0.95f),
                                RoyalNavySurface.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .border(1.5.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.ELECTRIC_GRID,
                            modifier = Modifier.size(30.dp),
                            primaryColor = badgeColor,
                            secondaryColor = ZummarGoldSoft
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedZone.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextIvory
                            )
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = badgeColor
                            )
                        }
                    }
                }

                InfoRowGlass("وقت بدء التشغيل:", selectedZone.powerStartTime, ZummarGoldSoft)
                InfoRowGlass("مدة الإطفاء المتوقعة:", selectedZone.powerOffDuration, TextSilver)
                InfoRowGlass("حالة المغذي / سبب الخلل:", selectedZone.powerFaultReason, if (selectedZone.powerStatus == "FAULT") ZummarAlertRed else ZummarEmeraldGlow)
                InfoRowGlass("وقت الإصلاح / العودة المتوقع:", selectedZone.powerExpectedFix, ZummarCyanLight)
            }
        }

        // Electricity Department & Admin Controls: Instant Toggle + Polygon/Freehand Zone Drawer
        if (canManageElectricity && selectedZone != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RoyalNavySurface)
                    .border(1.dp, ZummarGold.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "لوحة تحكم مسؤول الكهرباء لـ (${selectedZone.name})",
                    style = MaterialTheme.typography.titleLarge,
                    color = ZummarGoldSoft
                )

                // Instant Status Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerControlButton(
                        label = "تشغيل وطنية",
                        active = selectedZone.powerStatus == "NATIONAL",
                        color = ZummarGold,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateElectricity(
                                selectedZone.id,
                                "NATIONAL",
                                startTimeInput,
                                offDurationInput,
                                faultReasonInput,
                                expectedFixInput
                            )
                        }
                    )
                    PowerControlButton(
                        label = "تشغيل مولدة",
                        active = selectedZone.powerStatus == "GENERATOR",
                        color = ZummarGoldWarm,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateElectricity(
                                selectedZone.id,
                                "GENERATOR",
                                startTimeInput,
                                offDurationInput,
                                faultReasonInput,
                                expectedFixInput
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerControlButton(
                        label = "إطفاء الحي",
                        active = selectedZone.powerStatus == "OFF",
                        color = Color(0xFF64748B),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateElectricity(
                                selectedZone.id,
                                "OFF",
                                startTimeInput,
                                offDurationInput,
                                faultReasonInput,
                                expectedFixInput
                            )
                        }
                    )
                    PowerControlButton(
                        label = "خلل فني طارئ",
                        active = selectedZone.powerStatus == "FAULT",
                        color = ZummarAlertRed,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateElectricity(
                                selectedZone.id,
                                "FAULT",
                                startTimeInput,
                                offDurationInput,
                                faultReasonInput,
                                expectedFixInput
                            )
                        }
                    )
                }

                // Editable Schedule & Fault Details
                ZummarTextField(
                    value = startTimeInput,
                    onValueChange = { startTimeInput = it },
                    label = "وقت بدء التشغيل (مثال: 08:00 صباحاً)"
                )
                ZummarTextField(
                    value = offDurationInput,
                    onValueChange = { offDurationInput = it },
                    label = "مدة الإطفاء (مثال: 3 ساعات)"
                )
                ZummarTextField(
                    value = faultReasonInput,
                    onValueChange = { faultReasonInput = it },
                    label = "سبب الخلل أو ملاحظات الصيانة"
                )
                ZummarTextField(
                    value = expectedFixInput,
                    onValueChange = { expectedFixInput = it },
                    label = "وقت الإصلاح المتوقع"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(ZummarGold, ZummarGoldWarm)))
                        .clickable {
                            onUpdateElectricity(
                                selectedZone.id,
                                selectedZone.powerStatus,
                                startTimeInput,
                                offDurationInput,
                                faultReasonInput,
                                expectedFixInput
                            )
                        }
                        .testTag("save_electricity_details_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "حفظ وتعميم بيانات الكهرباء للحي",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalNavyDeep
                    )
                }
            }

            // Polygon & Freehand Zone Drawer Tool (`Polygon & Freehand Zone Drawer`)
            ZoneDrawerControlCard(
                drawingMode = drawingMode,
                draftPointsCount = draftPoints.size,
                newZoneName = newZoneName,
                onNewZoneNameChange = { newZoneName = it },
                onSetDrawingMode = onSetDrawingMode,
                onClearDraftPoints = onClearDraftPoints,
                onSaveZone = {
                    onSaveDrawnZone(newZoneName)
                    newZoneName = ""
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WaterNetworkScreen(
    activeRole: ZummarRole,
    zones: List<ZoneDocument>,
    selectedZoneId: String?,
    drawingMode: ZoneDrawingMode,
    draftPoints: List<NormalizedPoint>,
    onSelectZone: (ZoneDocument) -> Unit,
    onSetDrawingMode: (ZoneDrawingMode) -> Unit,
    onAddDraftPoint: (NormalizedPoint) -> Unit,
    onSetDraftPoints: (List<NormalizedPoint>) -> Unit,
    onClearDraftPoints: () -> Unit,
    onSaveDrawnZone: (String) -> Unit,
    onUpdateWater: (String, String, String, String, String, String) -> Unit,
    onSelectRole: (ZummarRole) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val selectedZone = zones.find { it.id == selectedZoneId } ?: zones.firstOrNull()
    val canManageWater = activeRole == ZummarRole.WATER_DEPT || activeRole == ZummarRole.SUPER_ADMIN

    var newZoneName by remember { mutableStateOf("") }
    var openTimeInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.waterOpenTime ?: "06:00 صباحاً") }
    var durationInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.waterDuration ?: "6 ساعات") }
    var faultNoteInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.waterFaultNote ?: "الضخ منتظم") }
    var expectedReturnInput by remember(selectedZone?.id) { mutableStateOf(selectedZone?.waterExpectedReturn ?: "مستمر") }

    LaunchedEffect(selectedZone) {
        if (selectedZone != null) {
            openTimeInput = selectedZone.waterOpenTime
            durationInput = selectedZone.waterDuration
            faultNoteInput = selectedZone.waterFaultNote
            expectedReturnInput = selectedZone.waterExpectedReturn
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTopHeader(
            title = "شبكة إسالة وضخ المياه",
            subtitle = "خريطة قواطع زمار الحية • تدفق المياه والصيانة",
            symbol = VectorSymbol.WATER_WAVE,
            accentColor = ZummarCyan,
            onBack = onBack
        )

        if (!canManageWater) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoyalNavyCard)
                    .border(1.dp, ZummarCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أنت تتصفح كـ (${activeRole.titleAr}). لتجربة رسم القواطع والتحكم بضخ الماء:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSilver,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ZummarCyan)
                        .clickable { onSelectRole(ZummarRole.WATER_DEPT) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("switch_to_water_role_button")
                ) {
                    Text("تفعيل حساب الماء", style = MaterialTheme.typography.labelLarge, color = RoyalNavyDeep)
                }
            }
        }

        // Interactive Zummar Map in WATER Mode (`Flowing Water Waves Canvas`)
        ZummarInteractiveMap(
            mode = MapVisualizationMode.WATER,
            zones = zones,
            facilities = emptyList(),
            stores = emptyList(),
            selectedZoneId = selectedZone?.id,
            selectedFacilityId = null,
            selectedStoreId = null,
            drawingMode = drawingMode,
            draftPoints = draftPoints,
            pickedPoint = null,
            onSelectZone = onSelectZone,
            onSelectFacility = {},
            onSelectStore = {},
            onAddDraftPoint = onAddDraftPoint,
            onSetDraftPoints = onSetDraftPoints,
            onPickLocation = {}
        )

        // Sector Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            zones.forEach { zone ->
                val isSelected = zone.id == selectedZone?.id
                val statusColor = when (zone.waterStatus) {
                    "PUMPING" -> ZummarCyanLight
                    "MAINTENANCE" -> ZummarGoldWarm
                    else -> TextSilver
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) RoyalNavyCard else RoyalNavySurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) statusColor else statusColor.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectZone(zone) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomVectorSymbol(
                            symbol = VectorSymbol.WATER_WAVE,
                            modifier = Modifier.size(18.dp),
                            primaryColor = statusColor,
                            secondaryColor = ZummarCyan,
                            animated = zone.waterStatus == "PUMPING"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextIvory
                        )
                    }
                }
            }
        }

        // Citizen Details Card for Selected Water Sector
        if (selectedZone != null) {
            val statusAr = when (selectedZone.waterStatus) {
                "PUMPING" -> "الضخ مفتوح الآن — المياه تتدفق في القاطع"
                "MAINTENANCE" -> "تنبيه كسر / صيانة في الأنبوب الناقل"
                else -> "الضخ متوقف حالياً (حسب جدول التناوب)"
            }
            val badgeColor = when (selectedZone.waterStatus) {
                "PUMPING" -> ZummarCyanLight
                "MAINTENANCE" -> ZummarGoldWarm
                else -> TextSilver
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RoyalNavyCard)
                    .border(1.5.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomVectorSymbol(
                        symbol = VectorSymbol.WATER_WAVE,
                        modifier = Modifier.size(30.dp),
                        primaryColor = badgeColor,
                        secondaryColor = ZummarCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "قاطع إسالة: ${selectedZone.name}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextIvory
                        )
                        Text(
                            text = statusAr,
                            style = MaterialTheme.typography.labelLarge,
                            color = badgeColor
                        )
                    }
                }

                InfoRowGlass("وقت فتح الماء:", selectedZone.waterOpenTime, ZummarCyanLight)
                InfoRowGlass("مدة الضخ المقررة:", selectedZone.waterDuration, TextIvory)
                InfoRowGlass("حالة الأنابيب والصيانة:", selectedZone.waterFaultNote, if (selectedZone.waterStatus == "MAINTENANCE") ZummarGoldWarm else ZummarEmeraldGlow)
                InfoRowGlass("موعد عودة الضخ:", selectedZone.waterExpectedReturn, ZummarGoldSoft)
            }
        }

        // Water Department & Admin Controls
        if (canManageWater && selectedZone != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RoyalNavySurface)
                    .border(1.dp, ZummarCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "لوحة تحكم دائرة ماء زمار لـ (${selectedZone.name})",
                    style = MaterialTheme.typography.titleLarge,
                    color = ZummarCyanLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerControlButton(
                        label = "فتح الضخ الآن",
                        active = selectedZone.waterStatus == "PUMPING",
                        color = ZummarCyan,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateWater(
                                selectedZone.id,
                                "PUMPING",
                                openTimeInput,
                                durationInput,
                                faultNoteInput,
                                expectedReturnInput
                            )
                        }
                    )
                    PowerControlButton(
                        label = "إيقاف الضخ",
                        active = selectedZone.waterStatus == "STOPPED",
                        color = Color(0xFF64748B),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateWater(
                                selectedZone.id,
                                "STOPPED",
                                openTimeInput,
                                durationInput,
                                faultNoteInput,
                                expectedReturnInput
                            )
                        }
                    )
                    PowerControlButton(
                        label = "صيانة أنبوب",
                        active = selectedZone.waterStatus == "MAINTENANCE",
                        color = ZummarGoldWarm,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onUpdateWater(
                                selectedZone.id,
                                "MAINTENANCE",
                                openTimeInput,
                                durationInput,
                                faultNoteInput,
                                expectedReturnInput
                            )
                        }
                    )
                }

                ZummarTextField(
                    value = openTimeInput,
                    onValueChange = { openTimeInput = it },
                    label = "وقت فتح الماء (مثال: 06:00 صباحاً)"
                )
                ZummarTextField(
                    value = durationInput,
                    onValueChange = { durationInput = it },
                    label = "مدة الضخ (مثال: 6 ساعات)"
                )
                ZummarTextField(
                    value = faultNoteInput,
                    onValueChange = { faultNoteInput = it },
                    label = "تنبيه وجود كسر أو صيانة في الأنبوب"
                )
                ZummarTextField(
                    value = expectedReturnInput,
                    onValueChange = { expectedReturnInput = it },
                    label = "موعد عودة الضخ"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(ZummarCyan, ZummarCyanLight)))
                        .clickable {
                            onUpdateWater(
                                selectedZone.id,
                                selectedZone.waterStatus,
                                openTimeInput,
                                durationInput,
                                faultNoteInput,
                                expectedReturnInput
                            )
                        }
                        .testTag("save_water_details_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "حفظ وتعميم جدول ضخ المياه للقاطع",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalNavyDeep
                    )
                }
            }

            ZoneDrawerControlCard(
                drawingMode = drawingMode,
                draftPointsCount = draftPoints.size,
                newZoneName = newZoneName,
                onNewZoneNameChange = { newZoneName = it },
                onSetDrawingMode = onSetDrawingMode,
                onClearDraftPoints = onClearDraftPoints,
                onSaveZone = {
                    onSaveDrawnZone(newZoneName)
                    newZoneName = ""
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ZoneDrawerControlCard(
    drawingMode: ZoneDrawingMode,
    draftPointsCount: Int,
    newZoneName: String,
    onNewZoneNameChange: (String) -> Unit,
    onSetDrawingMode: (ZoneDrawingMode) -> Unit,
    onClearDraftPoints: () -> Unit,
    onSaveZone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RoyalNavyCard)
            .border(1.dp, ZummarCyanLight.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CustomVectorSymbol(
                symbol = VectorSymbol.POLYGON_DRAW,
                modifier = Modifier.size(26.dp),
                primaryColor = ZummarGold,
                secondaryColor = ZummarCyanLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "أداة رسم وتحديد البقع الجغرافية والأحياء على الخريطة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ZummarGoldSoft
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (drawingMode == ZoneDrawingMode.POLYGON_POINTS) ZummarGold else RoyalNavySurface
                    )
                    .border(1.dp, ZummarGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable {
                        onSetDrawingMode(
                            if (drawingMode == ZoneDrawingMode.POLYGON_POINTS) ZoneDrawingMode.NONE
                            else ZoneDrawingMode.POLYGON_POINTS
                        )
                    }
                    .padding(10.dp)
                    .testTag("draw_polygon_points_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomVectorSymbol(
                        symbol = VectorSymbol.POLYGON_DRAW,
                        modifier = Modifier.size(20.dp),
                        primaryColor = if (drawingMode == ZoneDrawingMode.POLYGON_POINTS) RoyalNavyDeep else ZummarGold,
                        secondaryColor = ZummarCyanLight,
                        animated = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تحديد النقاط المتصلة",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (drawingMode == ZoneDrawingMode.POLYGON_POINTS) RoyalNavyDeep else TextIvory
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (drawingMode == ZoneDrawingMode.FREEHAND_LASSO) ZummarCyan else RoyalNavySurface
                    )
                    .border(1.dp, ZummarCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable {
                        onSetDrawingMode(
                            if (drawingMode == ZoneDrawingMode.FREEHAND_LASSO) ZoneDrawingMode.NONE
                            else ZoneDrawingMode.FREEHAND_LASSO
                        )
                    }
                    .padding(10.dp)
                    .testTag("draw_freehand_lasso_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomVectorSymbol(
                        symbol = VectorSymbol.FREEHAND_LASSO,
                        modifier = Modifier.size(20.dp),
                        primaryColor = if (drawingMode == ZoneDrawingMode.FREEHAND_LASSO) RoyalNavyDeep else ZummarCyanLight,
                        secondaryColor = ZummarGold,
                        animated = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الرسم الحر بالإصبع",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (drawingMode == ZoneDrawingMode.FREEHAND_LASSO) RoyalNavyDeep else TextIvory
                    )
                }
            }
        }

        if (drawingMode != ZoneDrawingMode.NONE || draftPointsCount > 0) {
            Text(
                text = "عدد النقاط الجغرافية المحددة حالياً فوق الخريطة: $draftPointsCount نقاط",
                style = MaterialTheme.typography.bodyMedium,
                color = ZummarCyanLight
            )

            ZummarTextField(
                value = newZoneName,
                onValueChange = onNewZoneNameChange,
                label = "اسم الحي أو القرية الجديدة (مثال: تل موزان / حي التآخي)"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ZummarEmerald)
                        .clickable { onSaveZone() }
                        .testTag("confirm_save_zone_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "حفظ البقعة الجغرافية للحي",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalNavyDeep
                    )
                }

                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalNavySurface)
                        .border(1.dp, ZummarAlertRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable {
                            onClearDraftPoints()
                            onSetDrawingMode(ZoneDrawingMode.NONE)
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("مسح النقاط", style = MaterialTheme.typography.labelLarge, color = ZummarAlertRed)
                }
            }
        }
    }
}

@Composable
fun SectionTopHeader(
    title: String,
    subtitle: String,
    symbol: VectorSymbol,
    accentColor: Color,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RoyalNavySurface)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            CustomVectorSymbol(
                symbol = symbol,
                modifier = Modifier.size(34.dp),
                primaryColor = accentColor,
                secondaryColor = TextIvory
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextIvory
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSilver
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(RoyalNavyCard)
                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { onBack() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("back_to_welcome_button")
        ) {
            Text(
                text = "الرئيسية",
                style = MaterialTheme.typography.labelLarge,
                color = accentColor
            )
        }
    }
}

@Composable
fun InfoRowGlass(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RoyalNavyDeep.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSilver
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = valueColor
        )
    }
}

@Composable
private fun PowerControlButton(
    label: String,
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) color else RoyalNavyCard)
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (active) RoyalNavyDeep else TextIvory
        )
    }
}

@Composable
fun ZummarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZummarCyanLight,
            unfocusedBorderColor = RoyalNavyCard,
            focusedTextColor = TextIvory,
            unfocusedTextColor = TextIvory,
            focusedLabelColor = ZummarCyanLight,
            unfocusedLabelColor = TextSilver,
            focusedContainerColor = RoyalNavyDeep.copy(alpha = 0.6f),
            unfocusedContainerColor = RoyalNavyDeep.copy(alpha = 0.4f)
        ),
        singleLine = true
    )
}
