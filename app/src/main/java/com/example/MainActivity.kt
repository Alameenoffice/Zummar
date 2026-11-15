package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.PortalDestination
import com.example.data.ZummarRepository
import com.example.ui.components.CustomVectorSymbol
import com.example.ui.components.VectorSymbol
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.ElectricityNetworkScreen
import com.example.ui.screens.MedicalSectorScreen
import com.example.ui.screens.MunicipalityUpcomingScreen
import com.example.ui.screens.SuperAdminApprovalScreen
import com.example.ui.screens.WaterNetworkScreen
import com.example.ui.screens.WelcomeGatewayScreen
import com.example.ui.screens.ZummarMarketScreen
import com.example.ui.screens.signOutUser
import com.example.ui.theme.MyApplicationTheme
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
import com.example.viewmodel.ZummarViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZummarAppAuthRoot()
            }
        }
    }
}

@Composable
fun ZummarAppAuthRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthGateScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        val uid = user.uid
        key(uid) {
            ZummarAuthenticatedPortal(currentUserId = uid)
        }
    }
}

@Composable
fun ZummarAuthenticatedPortal(
    currentUserId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: ZummarViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val prefs = app.getSharedPreferences(
                    "smart_zummar_gate_permanent_prefs",
                    Context.MODE_PRIVATE
                )
                ZummarViewModel(ZummarRepository(db, prefs), currentUserId)
            }
        }
    )

    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val zones by viewModel.zones.collectAsStateWithLifecycle()
    val facilities by viewModel.facilities.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val stores by viewModel.stores.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val selectedZoneId by viewModel.selectedZoneId.collectAsStateWithLifecycle()
    val selectedFacilityId by viewModel.selectedFacilityId.collectAsStateWithLifecycle()
    val selectedStoreId by viewModel.selectedStoreId.collectAsStateWithLifecycle()
    val drawingMode by viewModel.drawingMode.collectAsStateWithLifecycle()
    val draftPoints by viewModel.draftPoints.collectAsStateWithLifecycle()
    val pickedPoint by viewModel.pickedPoint.collectAsStateWithLifecycle()
    val bannerNotification by viewModel.bannerNotification.collectAsStateWithLifecycle()

    LaunchedEffect(bannerNotification) {
        if (bannerNotification != null) {
            delay(3800L)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        containerColor = RoyalNavyDeep,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            ZummarCustomBottomBar(
                currentDestination = currentDestination,
                onSelectDestination = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                PortalDestination.WELCOME -> WelcomeGatewayScreen(
                    activeRole = activeRole,
                    zones = zones,
                    facilities = facilities,
                    stores = stores,
                    onSelectRole = { viewModel.selectRole(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onSignOut = {
                        signOutUser(credentialManager, scope)
                    }
                )

                PortalDestination.ELECTRICITY -> ElectricityNetworkScreen(
                    activeRole = activeRole,
                    zones = zones,
                    selectedZoneId = selectedZoneId,
                    drawingMode = drawingMode,
                    draftPoints = draftPoints,
                    onSelectZone = { viewModel.selectZone(it) },
                    onSetDrawingMode = { viewModel.setDrawingMode(it) },
                    onAddDraftPoint = { viewModel.addDraftPoint(it) },
                    onSetDraftPoints = { viewModel.setDraftPoints(it) },
                    onClearDraftPoints = { viewModel.clearDraftPoints() },
                    onSaveDrawnZone = { viewModel.saveNewDrawnZone(it) },
                    onUpdateElectricity = { id, status, start, dur, reason, fix ->
                        viewModel.updateZoneElectricity(id, status, start, dur, reason, fix)
                    },
                    onSelectRole = { viewModel.selectRole(it) },
                    onBack = { viewModel.navigateTo(PortalDestination.WELCOME) }
                )

                PortalDestination.WATER -> WaterNetworkScreen(
                    activeRole = activeRole,
                    zones = zones,
                    selectedZoneId = selectedZoneId,
                    drawingMode = drawingMode,
                    draftPoints = draftPoints,
                    onSelectZone = { viewModel.selectZone(it) },
                    onSetDrawingMode = { viewModel.setDrawingMode(it) },
                    onAddDraftPoint = { viewModel.addDraftPoint(it) },
                    onSetDraftPoints = { viewModel.setDraftPoints(it) },
                    onClearDraftPoints = { viewModel.clearDraftPoints() },
                    onSaveDrawnZone = { viewModel.saveNewDrawnZone(it) },
                    onUpdateWater = { id, status, openTime, dur, note, ret ->
                        viewModel.updateZoneWater(id, status, openTime, dur, note, ret)
                    },
                    onSelectRole = { viewModel.selectRole(it) },
                    onBack = { viewModel.navigateTo(PortalDestination.WELCOME) }
                )

                PortalDestination.MEDICAL -> MedicalSectorScreen(
                    activeRole = activeRole,
                    zones = zones,
                    facilities = facilities,
                    appointments = appointments,
                    messages = messages,
                    selectedFacilityId = selectedFacilityId,
                    drawingMode = drawingMode,
                    pickedPoint = pickedPoint,
                    onSelectFacility = { viewModel.selectFacility(it) },
                    onSetDrawingMode = { viewModel.setDrawingMode(it) },
                    onPickLocation = { viewModel.setPickedLocation(it) },
                    onRegisterFacility = { name, type, spec, addr, hours, onDuty ->
                        viewModel.registerMedicalFacility(name, type, spec, addr, hours, onDuty)
                    },
                    onToggleOnDuty = { viewModel.togglePharmacyOnDuty(it) },
                    onBookAppointment = { fac, patName, patPhone, date ->
                        viewModel.bookDoctorAppointment(fac, patName, patPhone, date)
                    },
                    onSendMessage = { targetId, targetName, targetType, senderName, content ->
                        viewModel.sendOnlineMessage(targetId, targetName, targetType, senderName, content)
                    },
                    onSelectRole = { viewModel.selectRole(it) },
                    onBack = { viewModel.navigateTo(PortalDestination.WELCOME) }
                )

                PortalDestination.MARKET -> ZummarMarketScreen(
                    activeRole = activeRole,
                    zones = zones,
                    stores = stores,
                    products = products,
                    messages = messages,
                    selectedStoreId = selectedStoreId,
                    drawingMode = drawingMode,
                    pickedPoint = pickedPoint,
                    onSelectStore = { viewModel.selectStore(it) },
                    onSetDrawingMode = { viewModel.setDrawingMode(it) },
                    onPickLocation = { viewModel.setPickedLocation(it) },
                    onRegisterStore = { name, cat, desc, addr ->
                        viewModel.registerStore(name, cat, desc, addr)
                    },
                    onAddProduct = { store, name, cat, price, desc, inStock ->
                        viewModel.addProductToStore(store, name, cat, price, desc, inStock)
                    },
                    onSendMessage = { targetId, targetName, targetType, senderName, content ->
                        viewModel.sendOnlineMessage(targetId, targetName, targetType, senderName, content)
                    },
                    onBack = { viewModel.navigateTo(PortalDestination.WELCOME) }
                )

                PortalDestination.ADMIN_PANEL -> SuperAdminApprovalScreen(
                    facilities = facilities,
                    stores = stores,
                    onApproveFacility = { viewModel.approveFacility(it) },
                    onApproveStore = { viewModel.approveStore(it) },
                    onBack = { viewModel.navigateTo(PortalDestination.WELCOME) }
                )

                PortalDestination.MUNICIPALITY -> MunicipalityUpcomingScreen(
                    onBack = { viewModel.navigateTo(PortalDestination.WELCOME) }
                )
            }

            // Interactive Toast / Action Banner Notification (`إشعارات تفاعلية عند إتمام أي إجراء`)
            AnimatedVisibility(
                visible = bannerNotification != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(RoyalNavyCard, RoyalNavySurface)
                            )
                        )
                        .border(1.5.dp, ZummarEmeraldGlow, RoundedCornerShape(16.dp))
                        .clickable { viewModel.dismissNotification() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomVectorSymbol(
                        symbol = VectorSymbol.CHECK_MARK,
                        modifier = Modifier.size(24.dp),
                        primaryColor = ZummarEmeraldGlow,
                        secondaryColor = ZummarGold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = bannerNotification ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextIvory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZummarCustomBottomBar(
    currentDestination: PortalDestination,
    onSelectDestination: (PortalDestination) -> Unit
) {
    val navItems = listOf(
        Triple(PortalDestination.WELCOME, "الرئيسية", VectorSymbol.MUNICIPAL_ARCH to ZummarGoldSoft),
        Triple(PortalDestination.ELECTRICITY, "الكهرباء", VectorSymbol.ELECTRIC_GRID to ZummarGold),
        Triple(PortalDestination.WATER, "الماء", VectorSymbol.WATER_WAVE to ZummarCyanLight),
        Triple(PortalDestination.MEDICAL, "الطبي", VectorSymbol.PHARMACY_CHALICE to ZummarEmeraldGlow),
        Triple(PortalDestination.MARKET, "السوق", VectorSymbol.STOREFRONT to ZummarGold)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RoyalNavySurface)
            .border(width = 1.dp, color = RoyalNavyCard)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { (dest, label, symPair) ->
            val isSelected = currentDestination == dest
            val (symbol, color) = symPair
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) color.copy(alpha = 0.16f) else Color.Transparent
                    )
                    .clickable { onSelectDestination(dest) }
                    .padding(vertical = 6.dp)
                    .testTag("bottom_nav_${dest.name}"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomVectorSymbol(
                    symbol = symbol,
                    modifier = Modifier.size(24.dp),
                    primaryColor = if (isSelected) color else TextSilver,
                    secondaryColor = TextIvory,
                    animated = isSelected
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) color else TextSilver
                )
            }
        }
    }
}
