package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// Network API Models (Django REST Framework)
// ==========================================

@JsonClass(generateAdapter = true)
data class NetworkClerigo(
    val id: Int,
    val nome: String,
    val slug: String,
    @Json(name = "foto") val fotoUrl: String?,
    @Json(name = "link_foto") val linkFoto: String?,
    val biografia: String?,
    @Json(name = "grau_ordem") val grauOrdem: String, // DIACONO, PADRE, BISPO, ARCEBISPO
    val paroco: Boolean = false,
    val monsenhor: Boolean = false,
    @Json(name = "vigario_arq") val vigarioArq: Boolean = false,
    val oficio: String?,
    val situacao: String = "ATIVO",
    val religioso: Boolean = false,
    val ordem: String?,
    @Json(name = "sigla_ordem") val siglaOrdem: String?,
    @Json(name = "data_nasc") val dataNasc: String?,
    @Json(name = "data_ordenacao_diaconal") val dataOrdenacaoDiaconal: String?,
    @Json(name = "data_ordenacao_presbiteral") val dataOrdenacaoPresbiteral: String?,
    @Json(name = "data_ordenacao_episcopal") val dataOrdenacaoEpiscopal: String?,
    val email: String?,
    val telefone: String?,
    val facebook: String?,
    val instagram: String?,
    val youtube: String?
) {
    fun getFormattedTitle(): String {
        val prefix = when {
            grauOrdem == "BISPO" || grauOrdem == "ARCEBISPO" -> "Dom"
            monsenhor -> "Monsenhor"
            grauOrdem == "DIACONO" -> "Diácono"
            else -> "Padre"
        }
        var finalName = "$prefix $nome"
        if (religioso && !siglaOrdem.isNullOrBlank()) {
            finalName = "$finalName, $siglaOrdem"
        }
        return finalName
    }

    fun getInstagramUrl(): String? {
        if (instagram.isNullOrBlank()) return null
        val clean = instagram.trim().replace("@", "")
        return "https://instagram.com/$clean"
    }
}

@JsonClass(generateAdapter = true)
data class NetworkIgreja(
    val id: Int,
    val nome: String,
    val endereco: String,
    val bairro: String?,
    val cidade: String,
    val paroquia: Boolean = false,
    val capela: Boolean = false,
    @Json(name = "aberta_ao_publico") val abertaAoPublico: Boolean = true,
    val latitude: Double?,
    val longitude: Double?,
    val sacerdotes: String?,
    val telefone: String?,
    val imagem: String?,
    val email: String?,
    val site: String?,
    val facebook: String?,
    val instagram: String?,
    val youtube: String?,
    val maps: String?,
    val slug: String,
    @Json(name = "contato_whatsapp") val contatoWhatsapp: String?
) {
    fun getDisplayType(): String {
        return when {
            paroquia -> "Paróquia"
            capela -> "Capela"
            else -> "Igreja"
        }
    }

    fun getInstagramUrl(): String? {
        if (instagram.isNullOrBlank()) return null
        val clean = instagram.trim().replace("@", "")
        return "https://instagram.com/$clean"
    }

    fun getWhatsappUrl(): String? {
        if (contatoWhatsapp.isNullOrBlank()) return null
        val clean = contatoWhatsapp.replace("\\s+".toRegex(), "").replace("-", "")
        return "https://wa.me/$clean"
    }
}

@JsonClass(generateAdapter = true)
data class NetworkTipoCelebracao(
    val id: Int,
    val igreja: Int, // Igreja ID
    @Json(name = "igreja_details") val igrejaDetails: NetworkIgreja?, // Optional nested or side-loaded details
    val nome: String,
    val categoria: String, // missa, missa_votiva, novena, adoracao, confissao, etc.
    val recorrencia: String, // semanal, mensal_dia_fixo, mensal_dia_semana, data_especifica
    @Json(name = "horario_inicio") val horarioInicio: String?,
    @Json(name = "horario_fim") val horarioFim: String?,
    val descricao: String?,
    val dia: String?, // segunda, terca, quarta, quinta, sexta, sabado, domingo
    @Json(name = "dia_mes") val diaMes: Int?,
    @Json(name = "data_especifica") val dataEspecifica: String?,
    @Json(name = "semana_do_mes") val semanaDoMes: Int?,
    @Json(name = "exige_agendamento") val exigeAgendamento: Boolean = false,
    @Json(name = "exibir_no_site") val exibirNoSite: Boolean = true,
    val ativo: Boolean = true
) {
    fun getFormattedRecorrencia(): String {
        return when (recorrencia) {
            "semanal" -> {
                val diaPt = when (dia) {
                    "segunda" -> "Segunda-feira"
                    "terca" -> "Terça-feira"
                    "quarta" -> "Quarta-feira"
                    "quinta" -> "Quinta-feira"
                    "sexta" -> "Sexta-feira"
                    "sabado" -> "Sábado"
                    "domingo" -> "Domingo"
                    else -> "Semanal"
                }
                diaPt
            }
            "mensal_dia_fixo" -> "Todo dia $diaMes do mês"
            "mensal_dia_semana" -> {
                val ord = when (semanaDoMes) {
                    1 -> "1ª"
                    2 -> "2ª"
                    3 -> "3ª"
                    4 -> "4ª"
                    5 -> "5ª"
                    -1 -> "Última"
                    else -> ""
                }
                val diaPt = when (dia) {
                    "segunda" -> "Segunda-feira"
                    "terca" -> "Terça-feira"
                    "quarta" -> "Quarta-feira"
                    "quinta" -> "Quinta-feira"
                    "sexta" -> "Sexta-feira"
                    "sabado" -> "Sábado"
                    "domingo" -> "Domingo"
                    else -> ""
                }
                "$ord $diaPt do mês"
            }
            "data_especifica" -> "Dia $dataEspecifica"
            else -> "Consultar"
        }
    }

    fun getCategoryLabel(): String {
        return when (categoria) {
            "missa" -> "Santa Missa"
            "missa_votiva" -> "Missa Votiva"
            "novena" -> "Novena"
            "missa_corpo_presente" -> "Missa de Corpo Presente"
            "celebracao" -> "Celebração da Palavra"
            "adoracao" -> "Adoração ao Santíssimo"
            "batismo" -> "Batismo"
            "casamento" -> "Casamento"
            "confissao" -> "Confissão"
            else -> "Outros"
        }
    }
}

// ==========================================
// Local Database Entity (Room)
// ==========================================

@Entity(tableName = "exame_consciencia")
data class ExameItem(
    @PrimaryKey val id: String,
    val categoria: String, // ex: "Pecados contra Deus", "Pecados contra o Próximo"
    val pergunta: String,
    val vergonhaOuGravidade: String = "Normal", // "Grave", "Leve"
    val isChecked: Boolean = false
)

// Liturgia Diária Local/Model
data class LiturgiaDiaria(
    val data: String,
    val corLiturgica: String, // "Verde", "Branco", "Vermelho", "Roxo", "Rosa", "Preto"
    val corHex: String,       // #Hex corresponding color for background
    val titulo: String,       // ex: "Tempo Comum - Quinta-feira da XII Semana"
    val antifonaEntrada: String,
    val primeiraLeitura: String,
    val primeiraLeituraReferencia: String,
    val salmoResponsorial: String,
    val salmoRefrao: String,
    val salmoReferencia: String,
    val evangelho: String,
    val evangelhoReferencia: String,
    val evangelhoDestaque: String
)
