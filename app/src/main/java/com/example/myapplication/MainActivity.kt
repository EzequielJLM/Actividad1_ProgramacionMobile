package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// 1. MODELO DE DATOS (CONSIGNA 2)
// ==========================================

enum class Prioridad {
    ALTA, MEDIA, BAJA
}

sealed class EstadoRecordatorio {
    object Pendiente : EstadoRecordatorio()
    object EnProceso : EstadoRecordatorio()
    data class Completado(val fechaFinalizacion: String) : EstadoRecordatorio()
}

data class Categoria(
    val id: Int,
    val nombre: String
)

data class Recordatorio(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val fecha: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio
)

// ==========================================
// 2. FUENTE DE DATOS Y COLECCIONES (CONSIGNA 3)
// ==========================================

object RepositorioRecordatorios {

    val categoriasSet: Set<Categoria> = setOf(
        Categoria(1, "Trabajo"),
        Categoria(2, "Personal"),
        Categoria(3, "Estudio"),
        Categoria(1, "Trabajo") // Duplicado evitado por el Set
    )

    val coloresCategoriaMap: Map<Categoria, Color> = mapOf(
        Categoria(1, "Trabajo") to Color(0xFFE53935),
        Categoria(2, "Personal") to Color(0xFF1E88E5),
        Categoria(3, "Estudio") to Color(0xFF43A047)
    )

    val listaRecordatorios: List<Recordatorio> = listOf(
        Recordatorio(
            id = 1,
            titulo = "Entregar Actividad 1 Mobile",
            descripcion = "Subir informe PDF y repositorio a la plataforma.",
            fecha = "04/11/2026",
            categoria = Categoria(3, "Estudio"),
            prioridad = Prioridad.ALTA,
            estado = EstadoRecordatorio.EnProceso
        ),
        Recordatorio(
            id = 2,
            titulo = "Reunión de Sincronización",
            descripcion = "Discutir arquitectura del nuevo módulo con el equipo.",
            fecha = "05/11/2026",
            categoria = Categoria(1, "Trabajo"),
            prioridad = Prioridad.MEDIA,
            estado = EstadoRecordatorio.Pendiente
        ),
        Recordatorio(
            id = 3,
            titulo = "Comprar insumos de oficina",
            descripcion = null,
            fecha = "02/11/2026",
            categoria = Categoria(2, "Personal"),
            prioridad = Prioridad.BAJA,
            estado = EstadoRecordatorio.Completado("02/11/2026")
        )
    )
}

// ==========================================
// 3. ACTIVIDAD PRINCIPAL
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaRecordatorios()
                }
            }
        }
    }
}

// ==========================================
// 4. INTERFAZ GRÁFICA CON COMPOSE (CONSIGNA 4)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecordatorios() {
    var estadoFiltroSeleccionado by remember { mutableStateOf("Todos") }

    val recordatoriosFiltrados = remember(estadoFiltroSeleccionado) {
        when (estadoFiltroSeleccionado) {
            "Pendientes" -> RepositorioRecordatorios.listaRecordatorios.filter { it.estado is EstadoRecordatorio.Pendiente }
            "En Proceso" -> RepositorioRecordatorios.listaRecordatorios.filter { it.estado is EstadoRecordatorio.EnProceso }
            "Completados" -> RepositorioRecordatorios.listaRecordatorios.filter { it.estado is EstadoRecordatorio.Completado }
            else -> RepositorioRecordatorios.listaRecordatorios
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Recordatorios", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Filtrar por estado:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            FiltroChips(
                filtroSeleccionado = estadoFiltroSeleccionado,
                onFiltroCambiado = { estadoFiltroSeleccionado = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recordatoriosFiltrados) { recordatorio ->
                    ItemRecordatorio(recordatorio = recordatorio)
                }
            }
        }
    }
}

@Composable
fun FiltroChips(filtroSeleccionado: String, onFiltroCambiado: (String) -> Unit) {
    val opciones = listOf("Todos", "Pendientes", "En Proceso", "Completados")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opciones) { opcion ->
            FilterChip(
                selected = filtroSeleccionado == opcion,
                onClick = { onFiltroCambiado(opcion) },
                label = { Text(opcion) }
            )
        }
    }
}

@Composable
fun ItemRecordatorio(recordatorio: Recordatorio) {
    val colorCategoria = RepositorioRecordatorios.coloresCategoriaMap[recordatorio.categoria] ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recordatorio.categoria.nombre,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(colorCategoria, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                val colorPrioridad = when (recordatorio.prioridad) {
                    Prioridad.ALTA -> Color(0xFFD32F2F)
                    Prioridad.MEDIA -> Color(0xFFF57C00)
                    Prioridad.BAJA -> Color(0xFF388E3C)
                }
                Text(
                    text = "Prioridad ${recordatorio.prioridad.name}",
                    color = colorPrioridad,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = recordatorio.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            recordatorio.descripcion?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Fecha: ${recordatorio.fecha}", fontSize = 12.sp, color = Color.Gray)

                val estadoTexto = when (val est = recordatorio.estado) {
                    is EstadoRecordatorio.Completado -> "Finalizado el ${est.fechaFinalizacion}"
                    is EstadoRecordatorio.EnProceso -> "En Proceso"
                    is EstadoRecordatorio.Pendiente -> "Pendiente"
                }
                Text(text = "Estado: $estadoTexto", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
