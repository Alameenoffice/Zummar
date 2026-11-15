package com.example.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FacilityDocument
import com.example.data.PortalDestination
import com.example.data.StoreDocument
import com.example.data.ZoneDocument
import com.example.data.ZummarRole
import com.example.ui.components.CustomVectorSymbol
import com.example.ui.components.DestinationCardCanvasIllustration
import com.example.ui.components.VectorSymbol
import com.example.ui.components.ZummarWelcomeHeroCanvas
import com.example.ui.theme.RoyalNavyCard
import com.example.ui.theme.RoyalNavyDeep
import com.example.ui.theme.RoyalNavySurface
import com.example.ui.theme.TextIvory
import com.example.ui.theme.TextSilver
import com.example.ui.theme.ZummarCyan
import com.example.ui.theme.ZummarCyanLight
import com.example.ui.theme.ZummarEmerald
import com.example.ui.theme.ZummarEmeraldGlow
import com.example.ui.theme.ZummarGold
import com.example.ui.theme.ZummarGoldSoft

@Composable
fun WelcomeGatewayScreen(
    activeRole: ZummarRole,
    zones: List<ZoneDocument>,
    facilities: List<FacilityDocument>,
    stores: List<StoreDocument>,
    onSelectRole: (ZummarRole) -> Unit,
    onNavigate: (PortalDestination) -> Unit,
    onSignOut: () -> Unit
) {
    val onDutyPharmacies = facilities.filter {
        it.facilityType == "PHARMACY" && it.isOnDuty && it.approvalStatus == "APPROVED"
    }
    val pendingApprovals = facilities.count { it.approvalStatus == "PENDING" } +
        stores.count { it.approvalStatus == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Bar with Active Role & Sign Out
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "بوابة زمار الذكية",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZummarGoldSoft
                )
                Text(
                    text = "الصفة الحالية: ${activeRole.titleAr}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZummarCyanLight
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RoyalNavyCard)
                    .border(1.dp, ZummarCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onSignOut() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("sign_out_button")
            ) {
                Text(
                    text = "تبديل الحساب",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSilver
                )
            }
        }

        // 2. Artistic Welcome Canvas Panorama + Warm Welcome Message
        ZummarWelcomeHeroCanvas()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(RoyalNavySurface, RoyalNavyCard, RoyalNavySurface)
                    )
                )
                .border(1.dp, ZummarGold.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "أهلاً بك في بوابة زمار الذكية — نحن هنا لخدمتك وتسهيل حياتك اليومية في ناحية زمار، اختر وجهتك الخدمية الآن",
                style = MaterialTheme.typography.titleMedium,
                color = TextIvory
            )
        }

        // 3. Live On-Duty Pharmacies Radar Ticker
        if (onDutyPharmacies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ZummarEmerald.copy(alpha = 0.14f))
                    .border(1.dp, ZummarEmerald, RoundedCornerShape(16.dp))
                    .clickable { onNavigate(PortalDestination.MEDICAL) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.RADAR_BEACON,
                    modifier = Modifier.size(30.dp),
                    primaryColor = ZummarEmeraldGlow,
                    secondaryColor = ZummarEmerald
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الصيدليات الخافرة في زمار الآن (${onDutyPharmacies.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZummarEmeraldGlow
                    )
                    Text(
                        text = onDutyPharmacies.joinToString(" • ") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextIvory
                    )
                }
            }
        }

        // 4. Role-Based System Selector (`نظام الحسابات والأدوار`)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(RoyalNavySurface)
                .border(1.dp, ZummarCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.ROLE_BADGE,
                    modifier = Modifier.size(24.dp),
                    primaryColor = ZummarGold,
                    secondaryColor = ZummarCyanLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "نظام الحسابات والأدوار المتعددة في زمار",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZummarGoldSoft
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZummarRole.entries.forEach { role ->
                    val isSelected = role == activeRole
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(ZummarGold, ZummarGoldSoft))
                                } else {
                                    Brush.horizontalGradient(listOf(RoyalNavyCard, RoyalNavyCard))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ZummarGoldSoft else ZummarCyan.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectRole(role) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("role_chip_${role.name}")
                    ) {
                        Column {
                            Text(
                                text = role.titleAr,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) RoyalNavyDeep else TextIvory
                            )
                            Text(
                                text = role.badgeAr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) RoyalNavySurface else TextSilver
                            )
                        }
                    }
                }
            }

            Text(
                text = activeRole.descriptionAr,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSilver
            )

            if (activeRole == ZummarRole.SUPER_ADMIN || pendingApprovals > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ZummarGold.copy(alpha = 0.22f), ZummarEmerald.copy(alpha = 0.22f))
                            )
                        )
                        .border(1.dp, ZummarGold, RoundedCornerShape(14.dp))
                        .clickable { onNavigate(PortalDestination.ADMIN_PANEL) }
                        .padding(12.dp)
                        .testTag("open_admin_panel_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CustomVectorSymbol(
                                symbol = VectorSymbol.ADMIN_SHIELD,
                                modifier = Modifier.size(26.dp),
                                primaryColor = ZummarGold,
                                secondaryColor = ZummarEmerald
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "لوحة الإدارة العليا والمصادقات ($pendingApprovals طلبات بانتظار الموافقة)",
                                style = MaterialTheme.typography.labelLarge,
                                color = ZummarGoldSoft
                            )
                        }
                    }
                }
            }
        }

        // 5. The 5 Main Service Destination Cards (`بطاقات اختيار سريعة مرسومة بالكامل بـ Canvas`)
        Text(
            text = "القطاعات الخدمية والتجارية والطبية في ناحية زمار",
            style = MaterialTheme.typography.titleLarge,
            color = TextIvory
        )

        DestinationGatewayCard(
            destination = PortalDestination.ELECTRICITY,
            title = "شبكة الكهرباء الوطنية والمولدات",
            subtitle = "خريطة الأحياء الحية (${zones.count { it.powerStatus == "NATIONAL" || it.powerStatus == "GENERATOR" }} أحياء تعمل الآن) • متابعة المغذيات والأعطال",
            accentColor = ZummarGold,
            secondaryColor = ZummarGoldSoft,
            testTag = "dest_card_electricity",
            onClick = { onNavigate(PortalDestination.ELECTRICITY) }
        )

        DestinationGatewayCard(
            destination = PortalDestination.WATER,
            title = "شبكة إسالة وضخ المياه",
            subtitle = "خريطة قواطع الضخ الحية (${zones.count { it.waterStatus == "PUMPING" }} قواطع يضخ فيها الماء الآن) • جداول الفتح والصيانة",
            accentColor = ZummarCyan,
            secondaryColor = ZummarCyanLight,
            testTag = "dest_card_water",
            onClick = { onNavigate(PortalDestination.WATER) }
        )

        DestinationGatewayCard(
            destination = PortalDestination.MEDICAL,
            title = "الصيدليات الخافرة والعيادات والمختبرات",
            subtitle = "رادار الصيدليات الخافرة (${onDutyPharmacies.size} خافرة الآن) • حجز الأطباء بالتقويم والمراسلة الفورية",
            accentColor = ZummarEmerald,
            secondaryColor = ZummarEmeraldGlow,
            testTag = "dest_card_medical",
            onClick = { onNavigate(PortalDestination.MEDICAL) }
        )

        DestinationGatewayCard(
            destination = PortalDestination.MARKET,
            title = "سوق زمار والمحلات التجارية",
            subtitle = "تصفح المتاجر التفاعلية على الخريطة (${stores.count { it.approvalStatus == "APPROVED" }} متاجر معتمدة) • الأسعار والمراسلة أونلاين",
            accentColor = ZummarGoldSoft,
            secondaryColor = ZummarCyanLight,
            testTag = "dest_card_market",
            onClick = { onNavigate(PortalDestination.MARKET) }
        )

        DestinationGatewayCard(
            destination = PortalDestination.MUNICIPALITY,
            title = "خدمات البلدية والمجتمع (قريباً)",
            subtitle = "مشاريع التبليط، الإنارة العامة، حملات النظافة ومبادرات شباب زمار",
            accentColor = ZummarCyanLight,
            secondaryColor = ZummarEmerald,
            testTag = "dest_card_municipality",
            onClick = { onNavigate(PortalDestination.MUNICIPALITY) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DestinationGatewayCard(
    destination: PortalDestination,
    title: String,
    subtitle: String,
    accentColor: Color,
    secondaryColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        RoyalNavyCard,
                        RoyalNavySurface,
                        accentColor.copy(alpha = 0.14f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.7f), secondaryColor.copy(alpha = 0.35f))),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(RoyalNavyDeep)
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            DestinationCardCanvasIllustration(
                destination = destination,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextIvory
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSilver
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            CustomVectorSymbol(
                symbol = VectorSymbol.ARROW_BACK,
                modifier = Modifier.size(18.dp),
                primaryColor = accentColor,
                secondaryColor = secondaryColor,
                animated = false
            )
        }
    }
}
