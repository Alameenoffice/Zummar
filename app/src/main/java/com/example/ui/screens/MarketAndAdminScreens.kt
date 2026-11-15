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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FacilityDocument
import com.example.data.MessageDocument
import com.example.data.NormalizedPoint
import com.example.data.ProductDocument
import com.example.data.StoreDocument
import com.example.data.ZoneDocument
import com.example.data.ZummarRole
import com.example.ui.components.CustomVectorSymbol
import com.example.ui.components.InteractiveOpeningStorefrontCanvas
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
fun ZummarMarketScreen(
    activeRole: ZummarRole,
    zones: List<ZoneDocument>,
    stores: List<StoreDocument>,
    products: List<ProductDocument>,
    messages: List<MessageDocument>,
    selectedStoreId: String?,
    drawingMode: ZoneDrawingMode,
    pickedPoint: NormalizedPoint?,
    onSelectStore: (StoreDocument) -> Unit,
    onSetDrawingMode: (ZoneDrawingMode) -> Unit,
    onPickLocation: (NormalizedPoint) -> Unit,
    onRegisterStore: (String, String, String, String) -> Unit,
    onAddProduct: (StoreDocument, String, String, String, String, Boolean) -> Unit,
    onSendMessage: (String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val visibleStores = if (activeRole == ZummarRole.SUPER_ADMIN || activeRole == ZummarRole.MERCHANT) {
        stores
    } else {
        stores.filter { it.approvalStatus == "APPROVED" }
    }
    val selectedStore = visibleStores.find { it.id == selectedStoreId } ?: visibleStores.firstOrNull()
    val storeProducts = products.filter { it.storeId == selectedStore?.id }

    var storefrontDoorsOpen by remember(selectedStore?.id) { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var showChatModal by remember { mutableStateOf(false) }
    var showMerchantForms by remember { mutableStateOf(false) }

    // New Store Inputs
    var storeNameInput by remember { mutableStateOf("") }
    var storeCategoryInput by remember { mutableStateOf("مواد غذائية وتموينية") }
    var storeDescInput by remember { mutableStateOf("") }
    var storeAddressInput by remember { mutableStateOf("") }

    // New Product Inputs
    var prodNameInput by remember { mutableStateOf("") }
    var prodCategoryInput by remember { mutableStateOf("") }
    var prodPriceInput by remember { mutableStateOf("") }
    var prodDescInput by remember { mutableStateOf("") }
    var prodInStock by remember { mutableStateOf(true) }

    val categories = remember(products) {
        listOf("الكل") + products.map { it.category }.distinct()
    }
    val filteredMarketProducts = products.filter { prod ->
        val matchesCategory = selectedCategory == "الكل" || prod.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
            prod.name.contains(searchQuery, ignoreCase = true) ||
            prod.storeName.contains(searchQuery, ignoreCase = true) ||
            prod.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTopHeader(
            title = "سوق زمار والمحلات التجارية",
            subtitle = "المتاجر التفاعلية على الخريطة • سوق زمار الشامل",
            symbol = VectorSymbol.STOREFRONT,
            accentColor = ZummarGold,
            onBack = onBack
        )

        // 1. Interactive Zummar Map in MARKET Mode
        ZummarInteractiveMap(
            mode = MapVisualizationMode.MARKET,
            zones = zones,
            facilities = emptyList(),
            stores = visibleStores,
            selectedZoneId = null,
            selectedFacilityId = null,
            selectedStoreId = selectedStore?.id,
            drawingMode = drawingMode,
            draftPoints = emptyList(),
            pickedPoint = pickedPoint,
            onSelectZone = {},
            onSelectFacility = {},
            onSelectStore = { store ->
                onSelectStore(store)
                storefrontDoorsOpen = true
            },
            onAddDraftPoint = {},
            onSetDraftPoints = {},
            onPickLocation = onPickLocation
        )

        // Store Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleStores.forEach { st ->
                val isSelected = st.id == selectedStore?.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) ZummarGold else RoyalNavySurface)
                        .border(1.dp, ZummarGold, RoundedCornerShape(14.dp))
                        .clickable {
                            onSelectStore(st)
                            storefrontDoorsOpen = true
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = st.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) RoyalNavyDeep else TextIvory
                    )
                }
            }
        }

        // 2. Interactive Opening Storefront Vector (`Interactive Opening Storefront Vector`)
        if (selectedStore != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RoyalNavyCard)
                    .border(1.5.dp, ZummarGold.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedStore.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = ZummarGoldSoft
                        )
                        Text(
                            text = "${selectedStore.category} • ${selectedStore.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSilver
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoyalNavyDeep)
                            .border(1.dp, ZummarGoldSoft, RoundedCornerShape(12.dp))
                            .clickable { storefrontDoorsOpen = !storefrontDoorsOpen }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("toggle_storefront_doors_button")
                    ) {
                        Text(
                            text = if (storefrontDoorsOpen) "إغلاق أبواب المحل" else "فتح أبواب المحل",
                            style = MaterialTheme.typography.labelLarge,
                            color = ZummarGoldSoft
                        )
                    }
                }

                // Animated Opening Storefront Canvas revealing interior shelves
                InteractiveOpeningStorefrontCanvas(
                    isOpen = storefrontDoorsOpen,
                    storeName = selectedStore.name,
                    productCount = storeProducts.size
                )

                Text(
                    text = selectedStore.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextIvory
                )

                // Store's Own Shelf Products
                Text(
                    text = "رفوف بضائع (${selectedStore.name}) — ${storeProducts.size} منتجات:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZummarCyanLight
                )

                storeProducts.forEach { prod ->
                    ProductShelfItemCard(
                        product = prod,
                        onOrderChat = { showChatModal = true }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(ZummarGold, ZummarCyanLight)))
                        .clickable { showChatModal = true }
                        .testTag("message_merchant_online_button"),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مراسلة التاجر أونلاين / طلب منتج",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalNavyDeep
                        )
                    }
                }
            }
        }

        // 3. Comprehensive Zummar Market (`سوق زمار الشامل`) with Search & Category Filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RoyalNavySurface)
                .border(1.dp, ZummarCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.SEARCH_LENS,
                    modifier = Modifier.size(26.dp),
                    primaryColor = ZummarGold,
                    secondaryColor = ZummarCyanLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سوق زمار الشامل (${filteredMarketProducts.size} بضائع معروضة)",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextIvory
                )
            }

            ZummarTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "ابحث عن منتج أو بضاعة أو اسم محل في زمار..."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val active = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) ZummarCyan else RoyalNavyCard)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) RoyalNavyDeep else TextIvory
                        )
                    }
                }
            }

            filteredMarketProducts.forEach { prod ->
                ProductShelfItemCard(
                    product = prod,
                    onOrderChat = {
                        stores.find { it.id == prod.storeId }?.let { onSelectStore(it) }
                        showChatModal = true
                    }
                )
            }
        }

        // 4. Merchant Store & Product Publisher Panel (`حساب التاجر وصاحب المحل`)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RoyalNavyCard)
                .border(1.dp, ZummarGold.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMerchantForms = !showMerchantForms },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomVectorSymbol(
                        symbol = VectorSymbol.PLUS_ADD,
                        modifier = Modifier.size(24.dp),
                        primaryColor = ZummarGold,
                        secondaryColor = ZummarEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "لوحة التاجر: تسجيل محل جديد أو نشر بضاعة بالسعر العراقي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZummarGoldSoft
                    )
                }
            }

            if (showMerchantForms || activeRole == ZummarRole.MERCHANT) {
                if (selectedStore != null) {
                    Text(
                        text = "أولاً: نشر بضاعة جديدة في (${selectedStore.name})",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZummarCyanLight
                    )
                    ZummarTextField(
                        value = prodNameInput,
                        onValueChange = { prodNameInput = it },
                        label = "اسم المنتج / البضاعة"
                    )
                    ZummarTextField(
                        value = prodPriceInput,
                        onValueChange = { prodPriceInput = it },
                        label = "السعر بالدينار العراقي (مثال: 15000)"
                    )
                    ZummarTextField(
                        value = prodCategoryInput,
                        onValueChange = { prodCategoryInput = it },
                        label = "صنف البضاعة (مثال: مواد غذائية / كهربائيات)"
                    )
                    ZummarTextField(
                        value = prodDescInput,
                        onValueChange = { prodDescInput = it },
                        label = "وصف المنتج والعرض"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(ZummarGold, ZummarGoldWarm)))
                            .clickable {
                                onAddProduct(
                                    selectedStore,
                                    prodNameInput,
                                    prodCategoryInput,
                                    prodPriceInput,
                                    prodDescInput,
                                    prodInStock
                                )
                                prodNameInput = ""
                                prodPriceInput = ""
                                prodDescInput = ""
                            }
                            .testTag("publish_product_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "نشر البضاعة في المحل وسوق زمار الشامل",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalNavyDeep
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ثانياً: تسجيل محل تجاري جديد على خريطة زمار",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZummarEmeraldGlow
                )
                ZummarTextField(
                    value = storeNameInput,
                    onValueChange = { storeNameInput = it },
                    label = "اسم المحل التجاري"
                )
                ZummarTextField(
                    value = storeCategoryInput,
                    onValueChange = { storeCategoryInput = it },
                    label = "تصنيف المحل"
                )
                ZummarTextField(
                    value = storeAddressInput,
                    onValueChange = { storeAddressInput = it },
                    label = "عنوان المحل في زمار"
                )
                ZummarTextField(
                    value = storeDescInput,
                    onValueChange = { storeDescInput = it },
                    label = "نبذة عن البضائع والعروض"
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
                            .background(if (drawingMode == ZoneDrawingMode.PICK_LOCATION) ZummarCyan else RoyalNavySurface)
                            .border(1.dp, ZummarCyan, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetDrawingMode(
                                    if (drawingMode == ZoneDrawingMode.PICK_LOCATION) ZoneDrawingMode.NONE
                                    else ZoneDrawingMode.PICK_LOCATION
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (drawingMode == ZoneDrawingMode.PICK_LOCATION) "انقر على الخريطة..." else "تحديد موقع المحل بالخريطة",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (drawingMode == ZoneDrawingMode.PICK_LOCATION) RoyalNavyDeep else ZummarCyanLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZummarEmerald)
                            .clickable {
                                onRegisterStore(storeNameInput, storeCategoryInput, storeDescInput, storeAddressInput)
                                storeNameInput = ""
                                storeDescInput = ""
                                storeAddressInput = ""
                            }
                            .testTag("submit_store_registration_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "حفظ وتسجيل المحل",
                            style = MaterialTheme.typography.labelLarge,
                            color = RoyalNavyDeep
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showChatModal && selectedStore != null) {
        OnlineDirectChatDialog(
            targetId = selectedStore.id,
            targetName = selectedStore.name,
            targetType = "STORE",
            activeRole = activeRole,
            messages = messages.filter { it.targetId == selectedStore.id },
            onSend = { senderName, content ->
                onSendMessage(selectedStore.id, selectedStore.name, "STORE", senderName, content)
            },
            onDismiss = { showChatModal = false }
        )
    }
}

@Composable
private fun ProductShelfItemCard(
    product: ProductDocument,
    onOrderChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RoyalNavyDeep)
            .border(1.dp, ZummarGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RoyalNavyCard)
                    .border(1.dp, ZummarGoldSoft.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CustomVectorSymbol(
                    symbol = VectorSymbol.STOREFRONT,
                    modifier = Modifier.size(26.dp),
                    primaryColor = ZummarGold,
                    secondaryColor = ZummarCyanLight,
                    animated = false
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextIvory
                )
                Text(
                    text = "${product.storeName} • ${product.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZummarCyanLight
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSilver
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ZummarGold)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${product.priceIqd} د.ع",
                    style = MaterialTheme.typography.labelLarge,
                    color = RoyalNavyDeep
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ZummarEmerald.copy(alpha = 0.22f))
                    .border(1.dp, ZummarEmerald, RoundedCornerShape(8.dp))
                    .clickable { onOrderChat() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (product.inStock) "متوفر • اطلب الآن" else "اسأل التاجر",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZummarEmeraldGlow
                )
            }
        }
    }
}

@Composable
fun SuperAdminApprovalScreen(
    facilities: List<FacilityDocument>,
    stores: List<StoreDocument>,
    onApproveFacility: (String) -> Unit,
    onApproveStore: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTopHeader(
            title = "لوحة الإدارة العليا لبوابة زمار",
            subtitle = "اعتماد الصيدليات والعيادات والمحلات والرقابة الشاملة",
            symbol = VectorSymbol.ADMIN_SHIELD,
            accentColor = ZummarGold,
            onBack = onBack
        )

        Text(
            text = "طلبات تسجيل القطاع الطبي (${facilities.size} مؤسسات)",
            style = MaterialTheme.typography.titleLarge,
            color = ZummarEmeraldGlow
        )

        facilities.forEach { fac ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RoyalNavyCard)
                    .border(
                        width = 1.dp,
                        color = if (fac.approvalStatus == "APPROVED") ZummarEmerald else ZummarGold,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fac.name, style = MaterialTheme.typography.titleMedium, color = TextIvory)
                    Text("${fac.specialty} • ${fac.address}", style = MaterialTheme.typography.bodyMedium, color = TextSilver)
                }
                if (fac.approvalStatus == "PENDING") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ZummarEmerald)
                            .clickable { onApproveFacility(fac.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("موافقة واعتماد", style = MaterialTheme.typography.labelLarge, color = RoyalNavyDeep)
                    }
                } else {
                    Text("معتمد للعامة", style = MaterialTheme.typography.labelLarge, color = ZummarEmeraldGlow)
                }
            }
        }

        Text(
            text = "طلبات تسجيل المحلات والمتاجر (${stores.size} متاجر)",
            style = MaterialTheme.typography.titleLarge,
            color = ZummarGoldSoft
        )

        stores.forEach { st ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RoyalNavyCard)
                    .border(
                        width = 1.dp,
                        color = if (st.approvalStatus == "APPROVED") ZummarCyan else ZummarGold,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(st.name, style = MaterialTheme.typography.titleMedium, color = TextIvory)
                    Text("${st.category} • ${st.address}", style = MaterialTheme.typography.bodyMedium, color = TextSilver)
                }
                if (st.approvalStatus == "PENDING") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ZummarGold)
                            .clickable { onApproveStore(st.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("موافقة واعتماد", style = MaterialTheme.typography.labelLarge, color = RoyalNavyDeep)
                    }
                } else {
                    Text("معتمد للعامة", style = MaterialTheme.typography.labelLarge, color = ZummarCyanLight)
                }
            }
        }
    }
}

@Composable
fun MunicipalityUpcomingScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTopHeader(
            title = "خدمات البلدية والمجتمع (قريباً)",
            subtitle = "المشاريع الخدمية والمبادرات الأهلية في ناحية زمار",
            symbol = VectorSymbol.MUNICIPAL_ARCH,
            accentColor = ZummarCyanLight,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RoyalNavyCard)
                .border(1.5.dp, ZummarCyanLight.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "الحزمة الخدمية البلدية القادمة لناحية زمار",
                style = MaterialTheme.typography.headlineMedium,
                color = ZummarGoldSoft
            )
            Text(
                text = "يجري العمل على ربط قسم بلدية زمار لإتاحة التبليغ المباشر عن أعمال التبليط، الإنارة الشمسية للشوارع، وحملات النظافة والتشجير في جميع أحياء وقرى ناحية زمار.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextIvory
            )
            InfoRowGlass("مشروع تأهيل الشارع العام:", "قيد الإنجاز - المرحلة الثانية", ZummarEmeraldGlow)
            InfoRowGlass("منظومة الإنارة الحديثة:", "تشمل مركز زمار وحي الشهداء والمعلمين", ZummarGoldSoft)
            InfoRowGlass("خطط التشجير والحدائق:", "تعاون مشترك بين بلدية زمار والفرق التطوعية", ZummarCyanLight)
        }
    }
}
