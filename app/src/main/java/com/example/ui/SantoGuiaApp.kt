package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ExameItem
import com.example.data.LiturgiaDiaria
import com.example.data.NetworkClerigo
import com.example.data.NetworkIgreja
import com.example.data.NetworkTipoCelebracao
import com.example.ui.theme.DeepBlueDark
import com.example.ui.theme.DeepBlueMedium
import com.example.ui.theme.PremiumGold
import java.text.SimpleDateFormat
import java.util.*

// Tabs definition matching our cohesive organization of the 7 features
enum class SantoGuiaTab(
    val title: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    CELEBRACOES("Celebrações", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    IGREJAS("Igrejas", Icons.Filled.Place, Icons.Outlined.Place),
    LITURGIA("Liturgia", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    CONFISSAO("Confissão", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    CLERO("Sacerdotes", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SantoGuiaApp(viewModel: SantoGuiaViewModel) {
    var currentTab by remember { mutableStateOf(SantoGuiaTab.CELEBRACOES) }
    
    // Detailed item states for popups/detail dialogs
    var selectedClerigoDetail by remember { mutableStateOf<NetworkClerigo?>(null) }
    var selectedIgrejaDetail by remember { mutableStateOf<NetworkIgreja?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Church,
                            contentDescription = "Santo Guia Icon",
                            tint = PremiumGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Santo Guia",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlueDark,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        // General Info Toast or Action
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Sobre",
                            tint = PremiumGold
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepBlueDark,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                SantoGuiaTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.iconSelected else tab.iconUnselected,
                                contentDescription = tab.title,
                                tint = if (isSelected) DeepBlueDark else Color.White.copy(alpha = 0.7f)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) PremiumGold else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = PremiumGold
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                SantoGuiaTab.CELEBRACOES -> CelebracoesScreen(viewModel, onIgrejaClick = { selectedIgrejaDetail = it })
                SantoGuiaTab.IGREJAS -> IgrejasScreen(
                    viewModel = viewModel,
                    onIgrejaClick = { selectedIgrejaDetail = it }
                )
                SantoGuiaTab.LITURGIA -> LiturgiaScreen(viewModel)
                SantoGuiaTab.CONFISSAO -> ConfissaoScreen(
                    viewModel = viewModel,
                    onIgrejaClick = { selectedIgrejaDetail = it }
                )
                SantoGuiaTab.CLERO -> SacerdotesScreen(
                    viewModel = viewModel,
                    onClerigoClick = { selectedClerigoDetail = it }
                )
            }

            // Clergy Detail Modal Dialog
            selectedClerigoDetail?.let { clerigo ->
                ClerigoDetailDialog(
                    clerigo = clerigo,
                    onDismiss = { selectedClerigoDetail = null },
                    onIgrejaClick = { igrejaSlug ->
                        viewModel.selectIgrejaBySlug(igrejaSlug) { church ->
                            selectedIgrejaDetail = church
                        }
                    }
                )
            }

            // Church Detail Modal Dialog
            selectedIgrejaDetail?.let { igreja ->
                IgrejaDetailDialog(
                    igreja = igreja,
                    viewModel = viewModel,
                    onDismiss = { selectedIgrejaDetail = null }
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: SACERDOTES DE TERESINA
// ==========================================
@Composable
fun SacerdotesScreen(
    viewModel: SantoGuiaViewModel,
    onClerigoClick: (NetworkClerigo) -> Unit
) {
    val state by viewModel.clerigosState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchClerigosQuery.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner/Hero Section
        HeaderHeroSection(
            title = "Clero de Teresina",
            subtitle = "Sacerdotes, Bispos e Diáconos ativos em nossa Arquidiocese"
        )

        // Elegant Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchClerigos(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("sacerdotes_search_input"),
            placeholder = { Text("Pesquisar clérigo...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepBlueDark,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )

        when (val uiState = state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            }
            is UiState.Error -> {
                EmptyStateView(
                    message = "Erro ao carregar clérigos: ${uiState.message}",
                    actionLabel = "Tentar Novamente",
                    onAction = { viewModel.loadClerigos() }
                )
            }
            is UiState.Success -> {
                val list = uiState.data
                if (list.isEmpty()) {
                    EmptyStateView(message = "Nenhum clérigo encontrado.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { clerigo ->
                            ClerigoCard(clerigo = clerigo, onClick = { onClerigoClick(clerigo) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClerigoCard(clerigo: NetworkClerigo, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("clerigo_card_${clerigo.slug}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)))
    ) {
        Column {
            // Priest Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFF2F4F7))
            ) {
                if (!clerigo.linkFoto.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(clerigo.linkFoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de ${clerigo.nome}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = DeepBlueDark.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                // Order badge (e.g., Bispo, Padre)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(PremiumGold, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when(clerigo.grauOrdem) {
                            "ARCEBISPO" -> "Arcebispo"
                            "BISPO" -> "Bispo"
                            "DIACONO" -> "Diácono"
                            else -> "Padre"
                        },
                        fontSize = 9.sp,
                        color = DeepBlueDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = clerigo.getFormattedTitle(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DeepBlueDark,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = clerigo.oficio ?: "Vigário Paroquial",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = if (clerigo.situacao == "ATIVO") Color(0xFF2E7D32) else Color.Red,
                        modifier = Modifier.size(8.dp)
                    )
                    Text(
                        text = clerigo.situacao,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (clerigo.situacao == "ATIVO") Color(0xFF2E7D32) else Color.Red
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2 & 4: IGREJAS & MAPA DE TERESINA
// ==========================================
@Composable
fun IgrejasScreen(
    viewModel: SantoGuiaViewModel,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    var subTabSelected by remember { mutableStateOf(0) } // 0: Lista, 1: Mapa

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTabSelected,
            containerColor = DeepBlueDark,
            contentColor = Color.White
        ) {
            Tab(
                selected = subTabSelected == 0,
                onClick = { subTabSelected = 0 },
                text = { Text("Lista de Igrejas") },
                icon = { Icon(Icons.Default.List, contentDescription = "Lista") }
            )
            Tab(
                selected = subTabSelected == 1,
                onClick = { subTabSelected = 1 },
                text = { Text("Mapa Interativo") },
                icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") }
            )
        }

        if (subTabSelected == 0) {
            IgrejasListTab(viewModel, onIgrejaClick)
        } else {
            IgrejasMapTab(viewModel, onIgrejaClick)
        }
    }
}

@Composable
fun IgrejasListTab(
    viewModel: SantoGuiaViewModel,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    val state by viewModel.igrejasState.collectAsStateWithLifecycle()
    val bairroFilter by viewModel.bairroFilter.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter input
        OutlinedTextField(
            value = bairroFilter,
            onValueChange = { viewModel.setBairroFilter(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("igrejas_bairro_input"),
            placeholder = { Text("Filtrar por bairro (ex: Centro, Fátima)...") },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filtro") },
            shape = RoundedCornerShape(12.dp)
        )

        when (val uiState = state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            }
            is UiState.Error -> {
                EmptyStateView(
                    message = "Erro ao carregar igrejas: ${uiState.message}",
                    actionLabel = "Tentar novamente",
                    onAction = { viewModel.loadIgrejas() }
                )
            }
            is UiState.Success -> {
                val list = uiState.data
                if (list.isEmpty()) {
                    EmptyStateView(message = "Nenhuma igreja encontrada neste bairro.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { igreja ->
                            IgrejaListItem(igreja = igreja, onClick = { onIgrejaClick(igreja) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IgrejaListItem(igreja: NetworkIgreja, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("igreja_item_${igreja.slug}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Hero image with Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (!igreja.imagem.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(igreja.imagem)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto da ${igreja.nome}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepBlueDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Church,
                            contentDescription = null,
                            tint = PremiumGold.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                // Header status badge (Open/Closed)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(
                            if (igreja.abertaAoPublico) Color(0xFF2E7D32) else Color.Red,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (igreja.abertaAoPublico) "Aberta ao Público" else "Fechada ao Público",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = igreja.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepBlueDark,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Local",
                        tint = PremiumGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${igreja.endereco}${if (!igreja.bairro.isNullOrBlank()) " - ${igreja.bairro}" else ""}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WhatsApp Quick Integration Button
                    TextButton(
                        onClick = {
                            val url = igreja.getWhatsappUrl()
                            if (url != null) {
                                openWebUrl(context, url)
                            } else {
                                Toast.makeText(context, "WhatsApp indisponível para esta igreja", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = "Falar com Secretaria")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Secretaria", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlueMedium)
                    ) {
                        Text("Ver Detalhes", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// Map Component plotted with dynamic Pins using local Leaflet JS in WebView
@Composable
fun IgrejasMapTab(
    viewModel: SantoGuiaViewModel,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    val churchesState by viewModel.igrejasState.collectAsStateWithLifecycle()
    val selectedIgrejaForMap by viewModel.selectedIgrejaForMap.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when (val uiState = churchesState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            }
            is UiState.Error -> {
                EmptyStateView(message = "Erro ao carregar pontos do mapa.")
            }
            is UiState.Success -> {
                val churches = uiState.data

                // Build a JSON of churches for the JS Leaflet plot
                val churchesJson = buildString {
                    append("[")
                    churches.forEachIndexed { index, church ->
                        if (church.latitude != null && church.longitude != null) {
                            append("{")
                            append("\"id\": ${church.id},")
                            append("\"nome\": \"${church.nome.replace("\"", "\\\"")}\",")
                            append("\"slug\": \"${church.slug}\",")
                            append("\"lat\": ${church.latitude},")
                            append("\"lng\": ${church.longitude}")
                            append("}")
                            if (index < churches.size - 1) append(",")
                        }
                    }
                    append("]")
                }

                // Full hybrid Leaflet setup with gold marker colors
                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Santo Guia Map</title>
                        <meta charset="utf-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
                        <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                        <style>
                            html, body, #map {
                                width: 100%;
                                height: 100%;
                                margin: 0;
                                padding: 0;
                                background-color: #FAFAFC;
                            }
                            .leaflet-bar a {
                                background-color: #FFFFFF !important;
                                color: #0C2036 !important;
                            }
                        </style>
                    </head>
                    <body>
                        <div id="map"></div>
                        <script>
                            var map = L.map('map', {
                                zoomControl: true
                            }).setView([-5.0910, -42.8020], 12);

                            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                                attribution: '&copy; OpenStreetMap'
                            }).addTo(map);

                            var goldIcon = L.icon({
                                iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-gold.png',
                                shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                                iconSize: [25, 41],
                                iconAnchor: [12, 41],
                                popupAnchor: [1, -34],
                                shadowSize: [41, 41]
                            });

                            var churches = $churchesJson;

                            churches.forEach(function(church) {
                                if (church.lat && church.lng) {
                                    var marker = L.marker([church.lat, church.lng], {icon: goldIcon}).addTo(map);
                                    marker.on('click', function() {
                                        Android.onChurchClick(church.slug);
                                    });
                                }
                            });
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onChurchClick(slug: String) {
                                    // Switch thread to trigger UI changes safely in Compose
                                    post {
                                        val match = churches.firstOrNull { it.slug == slug }
                                        viewModel.selectIgrejaForMap(match)
                                    }
                                }
                            }, "Android")
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Beautiful interactive slide bottom sheet when Pin is selected
                AnimatedVisibility(
                    visible = selectedIgrejaForMap != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    selectedIgrejaForMap?.let { igreja ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onIgrejaClick(igreja) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = igreja.nome,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DeepBlueDark,
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.selectIgrejaForMap(null) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Fechar",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = igreja.endereco,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { onIgrejaClick(igreja) }) {
                                        Text("Mais Info", color = DeepBlueMedium)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            // Traçar Rota via Google Maps Intent
                                            val geoUri = Uri.parse("google.navigation:q=${igreja.latitude},${igreja.longitude}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                // Fallback browser maps Link
                                                val webMapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${igreja.latitude},${igreja.longitude}")
                                                openWebUrl(context, webMapUri.toString())
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)
                                    ) {
                                        Icon(imageVector = Icons.Default.Directions, contentDescription = "Rota", tint = DeepBlueDark)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rotas", color = DeepBlueDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: CELEBRAÇÕES DE TERESINA
// ==========================================
@Composable
fun CelebracoesScreen(
    viewModel: SantoGuiaViewModel,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    val state by viewModel.celebracoesState.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategoriaCelebracao.collectAsStateWithLifecycle()
    val selectedDia by viewModel.selectedDiaCelebracao.collectAsStateWithLifecycle()

    val categorias = listOf("Todas", "missa", "adoracao", "novena", "outros")
    val dias = listOf("Todos", "segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo")

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderHeroSection(
            title = "Celebrações da Fé",
            subtitle = "Horários de Santas Missas, Adorações e Novenas em Teresina"
        )

        // Categorias Horizontal Scroll
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Filtrar por Categoria",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBlueDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { cat ->
                    val isSelected = selectedCat == cat
                    val label = when(cat) {
                        "Todas" -> "Todas"
                        "missa" -> "Santa Missa"
                        "adoracao" -> "Adoração"
                        "novena" -> "Novenas"
                        else -> "Outras"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCelebracoesFilters(cat, selectedDia) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PremiumGold,
                            selectedLabelColor = DeepBlueDark
                        )
                    )
                }
            }
        }

        // Dias Horizontal Scroll
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Filtrar por Dia da Semana",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBlueDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dias) { dia ->
                    val isSelected = selectedDia == dia
                    val label = when(dia) {
                        "Todos" -> "Todos"
                        "segunda" -> "Seg"
                        "terca" -> "Ter"
                        "quarta" -> "Qua"
                        "quinta" -> "Qui"
                        "sexta" -> "Sex"
                        "sabado" -> "Sáb"
                        "domingo" -> "Dom"
                        else -> ""
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCelebracoesFilters(selectedCat, dia) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepBlueDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Divider(color = Color.LightGray.copy(alpha = 0.5f))

        when (val uiState = state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            }
            is UiState.Error -> {
                EmptyStateView(
                    message = "Erro ao carregar celebrações: ${uiState.message}",
                    actionLabel = "Tentar novamente",
                    onAction = { viewModel.loadCelebracoes() }
                )
            }
            is UiState.Success -> {
                val list = uiState.data
                if (list.isEmpty()) {
                    EmptyStateView(message = "Nenhuma celebração encontrada com os filtros selecionados.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { cel ->
                            CelebracaoItemCard(celebracao = cel, onIgrejaClick = onIgrejaClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CelebracaoItemCard(
    celebracao: NetworkTipoCelebracao,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clock Icon and Time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(DeepBlueDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .width(55.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Horário",
                    tint = PremiumGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = celebracao.horarioInicio ?: "--:--",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = celebracao.nome,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DeepBlueDark,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Recurrence text
                Text(
                    text = celebracao.getFormattedRecorrencia(),
                    fontSize = 11.sp,
                    color = PremiumGold,
                    fontWeight = FontWeight.SemiBold
                )

                celebracao.igrejaDetails?.let { details ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Igreja: ${details.nome}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable { onIgrejaClick(details) }
                    )
                }
            }

            // Quick Arrow to Church
            celebracao.igrejaDetails?.let { details ->
                IconButton(onClick = { onIgrejaClick(details) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Ver Igreja",
                        tint = DeepBlueDark
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 5: LITURGIA DIÁRIA
// ==========================================
@Composable
fun LiturgiaScreen(viewModel: SantoGuiaViewModel) {
    val liturgia by viewModel.liturgiaState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedLiturgiaDate.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner reflecting liturgical color
        val bgColor = Color(android.graphics.Color.parseColor(liturgia.corHex))
        val isLightText = liturgia.corLiturgica != "Branco" && liturgia.corLiturgica != "Rosa"
        val bannerTextColor = if (isLightText) Color.White else DeepBlueDark

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = liturgia.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = bannerTextColor,
                        fontFamily = FontFamily.Serif
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (isLightText) Color.White.copy(alpha = 0.2f) else DeepBlueDark.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = liturgia.corLiturgica,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = bannerTextColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = liturgia.data,
                    fontSize = 13.sp,
                    color = bannerTextColor.copy(alpha = 0.9f)
                )
            }
        }

        // Horizontal week date selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.selectLiturgiaOffset(-1) }) {
                Icon(Icons.Default.ArrowBackIos, contentDescription = "Anterior", tint = DeepBlueDark, modifier = Modifier.size(16.dp))
            }
            Text(
                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DeepBlueDark
            )
            IconButton(onClick = { viewModel.selectLiturgiaOffset(1) }) {
                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Seguinte", tint = DeepBlueDark, modifier = Modifier.size(16.dp))
            }
        }

        Divider(color = Color.LightGray.copy(alpha = 0.4f))

        // Readings List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                LiturgiaReadingBlock(
                    title = "Antífona de Entrada",
                    content = liturgia.antifonaEntrada
                )
            }
            item {
                LiturgiaReadingBlock(
                    title = "1ª Leitura (${liturgia.primeiraLeituraReferencia})",
                    content = liturgia.primeiraLeitura
                )
            }
            item {
                // Highlighted Refrain in Gold
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF7E7)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PremiumGold, PremiumGold)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Salmo Responsorial (${liturgia.salmoReferencia})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DeepBlueDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = liturgia.salmoRefrao,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PremiumGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = liturgia.salmoResponsorial,
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
            item {
                LiturgiaReadingBlock(
                    title = "Evangelho (${liturgia.evangelhoReferencia})",
                    content = liturgia.evangelho,
                    highlight = liturgia.evangelhoDestaque
                )
            }
        }
    }
}

@Composable
fun LiturgiaReadingBlock(
    title: String,
    content: String,
    highlight: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PremiumGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = DeepBlueDark
            )
            if (highlight != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(DeepBlueDark.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "“ $highlight ”",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = DeepBlueDark,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 6 & 7: CONFISSÕES E EXAME DE CONSCIÊNCIA
// ==========================================
@Composable
fun ConfissaoScreen(
    viewModel: SantoGuiaViewModel,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    var confTabSelected by remember { mutableStateOf(0) } // 0: Exame, 1: Horários

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = confTabSelected,
            containerColor = DeepBlueDark,
            contentColor = Color.White
        ) {
            Tab(
                selected = confTabSelected == 0,
                onClick = { confTabSelected = 0 },
                text = { Text("Exame de Consciência") },
                icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = "Preparação") }
            )
            Tab(
                selected = confTabSelected == 1,
                onClick = { confTabSelected = 1 },
                text = { Text("Locais de Confissão") },
                icon = { Icon(Icons.Default.Spa, contentDescription = "Confissões") }
            )
        }

        if (confTabSelected == 0) {
            ExameConscienciaTab(viewModel)
        } else {
            LocaisConfissaoTab(viewModel, onIgrejaClick)
        }
    }
}

@Composable
fun ExameConscienciaTab(viewModel: SantoGuiaViewModel) {
    val items by viewModel.exameItensState.collectAsStateWithLifecycle()
    val expandedCategories by viewModel.expandedExameCategories.collectAsStateWithLifecycle()

    // Grouping by category
    val grouped = items.groupBy { it.categoria }

    // Progress calculations
    val totalItems = items.size
    val checkedItems = items.count { it.isChecked }
    val progress = if (totalItems > 0) checkedItems.toFloat() / totalItems.toFloat() else 0f
    val progressPercent = (progress * 100).toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Elegant Progress Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepBlueDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Minha Preparação",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Serif
                        )
                        Box(
                            modifier = Modifier
                                .background(PremiumGold, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$checkedItems/$totalItems itens",
                                color = DeepBlueDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PremiumGold,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Concluído $progressPercent% - Responda com sinceridade em preparação para receber o Sacramento.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (categoria, list) ->
                    item {
                        val isExpanded = expandedCategories.contains(categoria)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column {
                                // Category Header (Expandable)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleExameCategoryExpanded(categoria) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (categoria.contains("Deus")) Icons.Filled.Star else Icons.Filled.Favorite,
                                            contentDescription = null,
                                            tint = PremiumGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = categoria,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = DeepBlueDark,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expandir",
                                        tint = DeepBlueDark
                                    )
                                }

                                if (isExpanded) {
                                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                    list.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.toggleExameItem(item) }
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = item.isChecked,
                                                onCheckedChange = { viewModel.toggleExameItem(item) },
                                                colors = CheckboxDefaults.colors(checkedColor = PremiumGold)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.pergunta,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp,
                                                    color = DeepBlueDark
                                                )
                                                if (item.vergonhaOuGravidade == "Grave") {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFFDE8E8), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Matéria Grave",
                                                            color = Color.Red,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Divider(color = Color.LightGray.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reset FAB
        FloatingActionButton(
            onClick = { viewModel.resetExameProgress() },
            containerColor = PremiumGold,
            contentColor = DeepBlueDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("reset_exame_progress_fab")
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reiniciar Progresso")
        }
    }
}

// Special filtration for only "confissao" categories across parishes in Teresina
@Composable
fun LocaisConfissaoTab(
    viewModel: SantoGuiaViewModel,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    val state by viewModel.celebracoesState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderHeroSection(
            title = "Sacramento da Reconciliação",
            subtitle = "Horários, dias e locais onde encontrar confissões sacramentais hoje em Teresina"
        )

        // Force reload/load only confissões (we can filter locally as requested!)
        LaunchedEffect(Unit) {
            viewModel.selectCelebracoesFilters("confissao", "Todos")
        }

        when (val uiState = state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            }
            is UiState.Error -> {
                EmptyStateView(message = "Erro ao carregar locais de confissão.")
            }
            is UiState.Success -> {
                val list = uiState.data.filter { it.categoria == "confissao" }
                if (list.isEmpty()) {
                    EmptyStateView(message = "Nenhum horário de confissão cadastrado no momento.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { cel ->
                            ConfissaoItemCard(celebracao = cel, onIgrejaClick = onIgrejaClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfissaoItemCard(
    celebracao: NetworkTipoCelebracao,
    onIgrejaClick: (NetworkIgreja) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confissões",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DeepBlueDark,
                        fontFamily = FontFamily.Serif
                    )
                }
                Box(
                    modifier = Modifier
                        .background(DeepBlueLightColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${celebracao.horarioInicio} - ${celebracao.horarioFim ?: ""}",
                        color = DeepBlueDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = celebracao.descricao ?: "Sacramento da Penitência para preparação da alma.",
                fontSize = 13.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = celebracao.getFormattedRecorrencia(),
                    fontSize = 12.sp,
                    color = DeepBlueMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            celebracao.igrejaDetails?.let { details ->
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = details.nome,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { onIgrejaClick(details) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlueDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ver Local", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT DIALOGS & HELPER LAYOUTS
// ==========================================

@Composable
fun HeaderHeroSection(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(DeepBlueDark, DeepBlueMedium)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Church,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlueDark)
            ) {
                Text(actionLabel, color = Color.White)
            }
        }
    }
}

// 16:9 Profile layout detailed dialog for Sacerdotes
@Composable
fun ClerigoDetailDialog(
    clerigo: NetworkClerigo,
    onDismiss: () -> Unit,
    onIgrejaClick: (String) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            LazyColumn {
                item {
                    // Profile Image 16:9 proportion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.777f) // Exact 16:9 ratio
                            .background(Color(0xFFF2F4F7))
                    ) {
                        if (!clerigo.linkFoto.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(clerigo.linkFoto)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de ${clerigo.nome}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = DeepBlueDark.copy(alpha = 0.2f),
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = clerigo.getFormattedTitle(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DeepBlueDark,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = clerigo.oficio ?: "Auxiliar Arquidiocesano",
                            fontSize = 13.sp,
                            color = PremiumGold,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Biografia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBlueDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = clerigo.biografia ?: "Nascido e consagrado no amor do Senhor, serve no pastoreio com o rebanho da cidade de Teresina-PI.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Contatos e Redes Sociais",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBlueDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Social Links
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!clerigo.instagram.isNullOrBlank()) {
                                SocialButton(
                                    label = "Instagram",
                                    icon = Icons.Default.CameraAlt,
                                    onClick = {
                                        clerigo.getInstagramUrl()?.let { openWebUrl(context, it) }
                                    }
                                )
                            }
                            if (!clerigo.facebook.isNullOrBlank()) {
                                SocialButton(
                                    label = "Facebook",
                                    icon = Icons.Default.Facebook,
                                    onClick = { openWebUrl(context, clerigo.facebook) }
                                )
                            }
                            if (!clerigo.telefone.isNullOrBlank()) {
                                SocialButton(
                                    label = "Ligar",
                                    icon = Icons.Default.Phone,
                                    onClick = {
                                        val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${clerigo.telefone}"))
                                        context.startActivity(phoneIntent)
                                    }
                                )
                            }
                        }

                        // Serves at Link
                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Onde Atua",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBlueDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Trigger quick church lookup
                                    val churchSlug = if (clerigo.slug.contains("fatima")) {
                                        "paroquia-nossa-senhora-de-fatima"
                                    } else {
                                        "catedral-nossa-senhora-das-dores"
                                    }
                                    onDismiss()
                                    onIgrejaClick(churchSlug)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Church, contentDescription = null, tint = PremiumGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (clerigo.slug.contains("fatima")) "Paróquia de Fátima" else "Catedral de Teresina",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = DeepBlueDark
                                    )
                                }
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DeepBlueDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IgrejaDetailDialog(
    igreja: NetworkIgreja,
    viewModel: SantoGuiaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allCelebracoesState by viewModel.celebracoesState.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            LazyColumn {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        if (!igreja.imagem.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(igreja.imagem)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Imagem da ${igreja.nome}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(DeepBlueDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Church,
                                    contentDescription = null,
                                    tint = PremiumGold.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = igreja.nome,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DeepBlueDark,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = igreja.getDisplayType(),
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = igreja.endereco, fontSize = 12.sp, color = Color.DarkGray)
                        }

                        if (!igreja.telefone.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = igreja.telefone, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // WhatsApp Secretária integration
                        Text(
                            text = "Secretaria Paroquial",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBlueDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val url = igreja.getWhatsappUrl()
                                if (url != null) {
                                    openWebUrl(context, url)
                                } else {
                                    Toast.makeText(context, "WhatsApp indisponível", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar conversa no WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Social Media Integration
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!igreja.instagram.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        igreja.getInstagramUrl()?.let { openWebUrl(context, it) }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Instagram", fontSize = 12.sp, color = DeepBlueDark)
                                }
                            }
                            if (!igreja.facebook.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { openWebUrl(context, igreja.facebook) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Facebook", fontSize = 12.sp, color = DeepBlueDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic celebrations inside this church
                        Text(
                            text = "Próximas Celebrações",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBlueDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        when (val uiState = allCelebracoesState) {
                            is UiState.Success -> {
                                val relevant = uiState.data.filter { it.igreja == igreja.id }
                                if (relevant.isEmpty()) {
                                    Text("Nenhum horário cadastrado no momento.", fontSize = 12.sp, color = Color.Gray)
                                } else {
                                    relevant.forEach { cel ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${cel.nome} (${cel.getFormattedRecorrencia()})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.DarkGray,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = cel.horarioInicio ?: "",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = PremiumGold
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                Text("Carregando horários...", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = DeepBlueMedium),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color.White)
    }
}

// Global Intent caller
fun openWebUrl(context: Context, url: String) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Impossível abrir o link.", Toast.LENGTH_SHORT).show()
    }
}

// Supporting light colors
val DeepBlueLightColor = Color(0xFFEDF3F9)
