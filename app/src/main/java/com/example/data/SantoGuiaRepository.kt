package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Locale

class SantoGuiaRepository(
    private val exameDao: ExameDao,
    private val api: SantoGuiaApi? = null
) {
    // ----------------------------------------------------------------
    // 1. Examination of Conscience Local Operations
    // ----------------------------------------------------------------
    val allExameItens: Flow<List<ExameItem>> = exameDao.getAllExameItens()

    suspend fun updateExameItem(item: ExameItem) {
        exameDao.updateItem(item)
    }

    suspend fun resetExameProgress() {
        exameDao.resetAllCheckmarks()
    }

    // ----------------------------------------------------------------
    // 2. Sacerdotes / Clerigos
    // ----------------------------------------------------------------
    suspend fun getClerigos(search: String? = null): List<NetworkClerigo> {
        return try {
            if (api != null) {
                api.getClerigos(search)
            } else {
                getMockClerigos(search)
            }
        } catch (e: Exception) {
            getMockClerigos(search)
        }
    }

    suspend fun getClerigoBySlug(slug: String): NetworkClerigo {
        return try {
            api?.getClerigoDetail(slug) ?: getMockClerigos().first { it.slug == slug }
        } catch (e: Exception) {
            getMockClerigos().firstOrNull { it.slug == slug }
                ?: throw IllegalArgumentException("Clérigo não encontrado")
        }
    }

    // ----------------------------------------------------------------
    // 3. Igrejas
    // ----------------------------------------------------------------
    suspend fun getIgrejas(bairro: String? = null): List<NetworkIgreja> {
        return try {
            if (api != null) {
                api.getIgrejas(bairro)
            } else {
                getMockIgrejas(bairro)
            }
        } catch (e: Exception) {
            getMockIgrejas(bairro)
        }
    }

    suspend fun getIgrejaBySlug(slug: String): NetworkIgreja {
        return try {
            api?.getIgrejaDetail(slug) ?: getMockIgrejas().first { it.slug == slug }
        } catch (e: Exception) {
            getMockIgrejas().firstOrNull { it.slug == slug }
                ?: throw IllegalArgumentException("Igreja não encontrada")
        }
    }

    // ----------------------------------------------------------------
    // 4. Celebracoes
    // ----------------------------------------------------------------
    suspend fun getCelebracoes(categoria: String? = null, dia: String? = null): List<NetworkTipoCelebracao> {
        return try {
            if (api != null) {
                api.getCelebracoes(categoria, dia)
            } else {
                getMockCelebracoes(categoria, dia)
            }
        } catch (e: Exception) {
            getMockCelebracoes(categoria, dia)
        }
    }

    // ----------------------------------------------------------------
    // 5. Liturgia Diária
    // ----------------------------------------------------------------
    fun getLiturgiaDiaria(calendar: Calendar): LiturgiaDiaria {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("pt", "BR"))
        val year = calendar.get(Calendar.YEAR)
        
        val dateString = "$dayOfMonth de $monthName de $year"

        return when (dayOfWeek) {
            Calendar.SUNDAY -> LiturgiaDiaria(
                data = dateString,
                corLiturgica = "Verde",
                corHex = "#2E7D32", // Rich Liturgical Green
                titulo = "Domingo do Tempo Comum",
                antifonaEntrada = "Cantai ao Senhor um cântico novo, cantai ao Senhor, terra inteira. Esplendor e majestade diante dele, beleza e poder no seu santuário.",
                primeiraLeitura = "Leitura do Livro do Profeta Isaías:\n\n'Buscai o Senhor enquanto se pode achar, invocai-o enquanto está perto. Abandone o ímpio o seu caminho, e o homem injusto os seus pensamentos; converta-se ao Senhor, que se compadecerá dele, e para o nosso Deus, porque é rico em perdoar.'",
                primeiraLeituraReferencia = "Is 55,6-9",
                salmoResponsorial = "O Senhor está perto de quem o invoca, de quem o invoca de coração sincero.",
                salmoRefrao = "O Senhor está perto de todos os que o invocam.",
                salmoReferencia = "Sl 144",
                evangelho = "Proclamação do Evangelho de Jesus Cristo segundo Mateus:\n\n'Naquele tempo, disse Jesus aos seus discípulos esta parábola: O Reino dos Céus é semelhante a um pai de família que saiu muito cedo a contratar trabalhadores para a sua vinha. Ajustou com eles um denário por dia e mandou-os para a vinha...'",
                evangelhoReferencia = "Mt 20,1-16",
                evangelhoDestaque = "Assim, os últimos serão os primeiros, e os primeiros serão os últimos. Pois muitos são chamados, mas poucos escolhidos."
            )
            Calendar.FRIDAY -> LiturgiaDiaria(
                data = dateString,
                corLiturgica = "Roxo",
                corHex = "#6A1B9A", // Liturgical Violet / Purple
                titulo = "Sexta-feira da Quaresma / Penitência",
                antifonaEntrada = "Tende piedade de mim, Senhor, pois clamo a vós o dia inteiro; alegrai o coração do vosso servo, pois a vós elevo a minha alma.",
                primeiraLeitura = "Leitura do Livro do Profeta Joel:\n\n'Rasgai o vosso coração e não as vossas vestes, e convertei-vos ao Senhor, vosso Deus; porque ele é benigno e misericordioso, paciente e rico em bondade, e se compadece da desgraça.'",
                primeiraLeituraReferencia = "Jl 2,12-18",
                salmoResponsorial = "Piedade, ó Senhor, tende piedade, pois pecamos contra vós.",
                salmoRefrao = "Misericórdia, Senhor, pois pecamos contra vós.",
                salmoReferencia = "Sl 50",
                evangelho = "Proclamação do Evangelho de Jesus Cristo segundo Lucas:\n\n'Se alguém quer vir após mim, renuncie a si mesmo, tome sua cruz cada dia e siga-me. Porque quem quiser salvar a sua vida, irá perdê-la; mas quem perder a sua vida por minha causa, esse a salvará.'",
                evangelhoReferencia = "Lc 9,22-25",
                evangelhoDestaque = "O que aproveita ao homem ganhar o mundo inteiro, se perder ou arruinar a si mesmo?"
            )
            Calendar.SATURDAY -> LiturgiaDiaria(
                data = dateString,
                corLiturgica = "Branco",
                corHex = "#F5F5F0", // Off-white/Gold border representing Marian or Feast
                titulo = "Memória de Nossa Senhora / Sábado",
                antifonaEntrada = "Salve, Santa Mãe, que destes à luz o Rei que governa o céu e a terra para sempre.",
                primeiraLeitura = "Leitura do Livro do Eclesiástico:\n\n'Como o cinamomo e o bálsamo aromático, espalhei perfume; como mirra escolhida, derramei o aroma de suavidade. Como o terebinto, estendi os meus ramos, ramos de honra e de graça.'",
                primeiraLeituraReferencia = "Eclo 24,15-20",
                salmoResponsorial = "O Senhor fez em mim maravilhas, santo é o seu nome.",
                salmoRefrao = "A minha alma engrandece o Senhor.",
                salmoReferencia = "Lc 1",
                evangelho = "Proclamação do Evangelho de Jesus Cristo segundo Lucas:\n\n'Enquanto Jesus falava, uma mulher levantou a voz no meio da multidão e disse: \"Feliz o ventre que te trouxe e os seios que te amamentaram!\" Mas ele respondeu: \"Felizes, antes, os que ouvem a Palavra de Deus e a guardam!\"'",
                evangelhoReferencia = "Lc 11,27-28",
                evangelhoDestaque = "Felizes, antes, os que ouvem a Palavra de Deus e a guardam!"
            )
            else -> LiturgiaDiaria(
                data = dateString,
                corLiturgica = "Verde",
                corHex = "#2E7D32",
                titulo = "Dia de Semana - Tempo Comum",
                antifonaEntrada = "Vinde, adoremos a Deus e prostremo-nos diante dele, ajoelhemos diante do Senhor que nos criou. Pois ele é o nosso Deus.",
                primeiraLeitura = "Leitura da Carta de São Paulo aos Romanos:\n\n'Justificados, pois, pela fé, temos paz com Deus por meio de nosso Senhor Jesus Cristo, por quem obtivemos também acesso, pela fé, a esta graça na qual estamos firmes; e gloriamo-nos na esperança da glória de Deus.'",
                primeiraLeituraReferencia = "Rm 5,1-5",
                salmoResponsorial = "Cantai ao Senhor um cântico novo, cantai ao Senhor, terra inteira.",
                salmoRefrao = "Anunciai entre as nações as maravilhas do Senhor.",
                salmoReferencia = "Sl 95",
                evangelho = "Proclamação do Evangelho de Jesus Cristo segundo João:\n\n'Jesus disse: Eu sou o pão da vida. Quem vem a mim não terá fome, e quem crê em mim nunca terá sede. Mas eu vos disse que vós me vistes e não credes. Tudo o que o Pai me dá virá a mim; e o que vem a mim de maneira nenhuma o lançarei fora.'",
                evangelhoReferencia = "Jo 6,35-37",
                evangelhoDestaque = "Eu sou o pão vivo que desceu do céu; se alguém comer deste pão, viverá para sempre."
            )
        }
    }

    // ----------------------------------------------------------------
    // MOCK DATA GENERATION FOR OFFLINE / FALLBACK USE
    // ----------------------------------------------------------------

    private fun getMockClerigos(search: String? = null): List<NetworkClerigo> {
        val list = listOf(
            NetworkClerigo(
                id = 1,
                nome = "Juarez Sousa da Silva",
                slug = "dom-juarez-sousa",
                fotoUrl = null,
                linkFoto = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=300",
                biografia = "Dom Juarez Sousa da Silva é o oitavo Arcebispo Metropolitano de Teresina. Nascido em Cabeceiras do Piauí, formou-se em Filosofia e Teologia e foi ordenado presbítero em 1994. Possui mestrado em Teologia Dogmática pela Pontifícia Universidade Gregoriana, em Roma. Foi nomeado bispo de Parnaíba em 2008 pelo Papa Bento XVI e tomou posse como Arcebispo de Teresina em 2023.",
                grauOrdem = "ARCEBISPO",
                paroco = false,
                monsenhor = false,
                oficio = "Arcebispo Metropolitano",
                situacao = "ATIVO",
                religioso = false,
                ordem = null,
                siglaOrdem = null,
                dataNasc = "1961-06-30",
                dataOrdenacaoDiaconal = "1993-11-19",
                dataOrdenacaoPresbiteral = "1994-03-19",
                dataOrdenacaoEpiscopal = "2008-05-17",
                email = "chancelaria@arquidiocesedeteresina.org",
                telefone = "(86) 3221-1843",
                facebook = "https://facebook.com/arquidiocesetere",
                instagram = "domjuarezmarques",
                youtube = "https://youtube.com/@ArquidiocesedeTeresina"
            ),
            NetworkClerigo(
                id = 2,
                nome = "Tony Batista",
                slug = "monsenhor-tony-batista",
                fotoUrl = null,
                linkFoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=300",
                biografia = "Monsenhor Tony Batista é uma das figuras clericais mais emblemáticas e queridas da Arquidiocese de Teresina. Dedicou décadas de sua vida pastoral aos necessitados, liderando projetos sociais memoráveis como a ASA (Ação Social Arquidiocesana). Atua como Vigário Geral da Arquidiocese de Teresina e cura com carinho e zelo do povo de Deus na Paróquia de Fátima.",
                grauOrdem = "PADRE",
                paroco = true,
                monsenhor = true,
                vigarioArq = true,
                oficio = "Vigário Geral e Pároco de Fátima",
                situacao = "ATIVO",
                religioso = false,
                ordem = null,
                siglaOrdem = null,
                dataNasc = "1946-05-12",
                dataOrdenacaoDiaconal = null,
                dataOrdenacaoPresbiteral = "1972-12-08",
                dataOrdenacaoEpiscopal = null,
                email = "tonybatista@hotmail.com",
                telefone = "(86) 3232-1526",
                facebook = "https://facebook.com/paroquiafatima",
                instagram = "padretonybatista",
                youtube = null
            ),
            NetworkClerigo(
                id = 3,
                nome = "Leonildo Campelo",
                slug = "padre-leonildo-campelo",
                fotoUrl = null,
                linkFoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=300",
                biografia = "Padre Leonildo Campelo atua no zêlo das vocações sacerdotais e na liturgia paroquial. Conhecido pela profundidade doutrinária de suas homilias e dedicação pastoral diária no confessionário, orientando centenas de fiéis na cidade de Teresina.",
                grauOrdem = "PADRE",
                paroco = true,
                monsenhor = false,
                oficio = "Pároco",
                situacao = "ATIVO",
                religioso = false,
                ordem = null,
                siglaOrdem = null,
                dataNasc = "1980-09-15",
                dataOrdenacaoDiaconal = "2006-12-15",
                dataOrdenacaoPresbiteral = "2007-06-29",
                dataOrdenacaoEpiscopal = null,
                email = "leonildo.campelo@gmail.com",
                telefone = "(86) 3211-5460",
                facebook = null,
                instagram = "padreleonildo",
                youtube = null
            ),
            NetworkClerigo(
                id = 4,
                nome = "Francisco Sales, SJ",
                slug = "padre-francisco-sales",
                fotoUrl = null,
                linkFoto = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=300",
                biografia = "Padre Francisco Sales pertence à Companhia de Jesus (Jesuítas). Doutor em Sagrada Escritura, atua em Teresina ministrando cursos bíblicos, retiros espirituais inacianos e auxiliando capelas e paróquias locais com sua profunda sabedoria espiritual.",
                grauOrdem = "PADRE",
                paroco = false,
                monsenhor = false,
                oficio = "Superior da Residência Jesuíta",
                situacao = "ATIVO",
                religioso = true,
                ordem = "Companhia de Jesus",
                siglaOrdem = "SJ",
                dataNasc = "1975-02-10",
                dataOrdenacaoDiaconal = "2001-08-11",
                dataOrdenacaoPresbiteral = "2002-07-20",
                dataOrdenacaoEpiscopal = null,
                email = "sales.sj@jesuitas.org.br",
                telefone = "(86) 3222-4512",
                facebook = null,
                instagram = "salesjesuita",
                youtube = null
            ),
            NetworkClerigo(
                id = 5,
                nome = "Paulo de Tarso",
                slug = "diacono-paulo-de-tarso",
                fotoUrl = null,
                linkFoto = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=300",
                biografia = "Diácono Paulo de Tarso é diácono permanente ordenado para a Arquidiocese de Teresina. Casado, pai de família, exerce seu ministério na caridade e na liturgia paroquial, auxiliando na celebração de batizados, matrimônios e celebrações da Palavra.",
                grauOrdem = "DIACONO",
                paroco = false,
                monsenhor = false,
                oficio = "Diácono Assistente",
                situacao = "ATIVO",
                religioso = false,
                ordem = null,
                siglaOrdem = null,
                dataNasc = "1968-11-20",
                dataOrdenacaoDiaconal = "2015-10-12",
                dataOrdenacaoPresbiteral = null,
                dataOrdenacaoEpiscopal = null,
                email = "paulotarso.diacono@gmail.com",
                telefone = "(86) 99982-1243",
                facebook = null,
                instagram = "diaconopauloteresina",
                youtube = null
            )
        )

        return if (search.isNullOrBlank()) {
            list
        } else {
            list.filter { it.nome.contains(search, ignoreCase = true) || it.biografia?.contains(search, ignoreCase = true) == true }
        }
    }

    private fun getMockIgrejas(bairro: String? = null): List<NetworkIgreja> {
        val list = listOf(
            NetworkIgreja(
                id = 1,
                nome = "Catedral Metropolitana de Nossa Senhora das Dores",
                endereco = "Praça Saraiva, s/n - Centro",
                bairro = "Centro",
                cidade = "Teresina",
                paroquia = true,
                capela = false,
                abertaAoPublico = true,
                latitude = -5.093409,
                longitude = -42.812239,
                sacerdotes = "Dom Juarez Sousa da Silva, Padre Leonildo Campelo",
                telefone = "(86) 3222-2634",
                imagem = "https://images.unsplash.com/photo-1548625361-155deee21623?auto=format&fit=crop&q=80&w=800",
                email = "catedralteresina@hotmail.com",
                site = "https://arquidiocesedeteresina.org/catedral",
                facebook = "https://facebook.com/catedralteresina",
                instagram = "catedralteresina",
                youtube = "https://youtube.com/catedralteresina",
                maps = "https://www.google.com/maps/place/Catedral+Metropolitana+Nossa+Senhora+das+Dores/@-5.093409,-42.812239,17z",
                slug = "catedral-nossa-senhora-das-dores",
                contatoWhatsapp = "5586981242345"
            ),
            NetworkIgreja(
                id = 2,
                nome = "Paróquia Nossa Senhora de Fátima",
                endereco = "Avenida Nossa Senhora de Fátima, 1420 - Bairro Fátima",
                bairro = "Fátima",
                cidade = "Teresina",
                paroquia = true,
                capela = false,
                abertaAoPublico = true,
                latitude = -5.074092,
                longitude = -42.791559,
                sacerdotes = "Monsenhor Tony Batista",
                telefone = "(86) 3232-1526",
                imagem = "https://images.unsplash.com/photo-1545987796-200677ee1011?auto=format&fit=crop&q=80&w=800",
                email = "secretaria@paroquiafatima.org",
                site = "https://paroquiafatimatere.com.br",
                facebook = "https://facebook.com/paroquiafatimatere",
                instagram = "paroquiadefatima_teresina",
                youtube = null,
                maps = "https://www.google.com/maps/place/Par%C3%B3quia+Nossa+Senhora+de+F%C3%A1tima/@-5.074092,-42.791559,17z",
                slug = "paroquia-nossa-senhora-de-fatima",
                contatoWhatsapp = "5586994112233"
            ),
            NetworkIgreja(
                id = 3,
                nome = "Igreja Matriz de Nossa Senhora do Amparo (Padroeira)",
                endereco = "Praça da Bandeira, s/n - Centro",
                bairro = "Centro",
                cidade = "Teresina",
                paroquia = true,
                capela = false,
                abertaAoPublico = true,
                latitude = -5.088656,
                longitude = -42.812604,
                sacerdotes = "Padre José de Anchieta",
                telefone = "(86) 3221-1234",
                imagem = "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&q=80&w=800",
                email = "amparoteresina@gmail.com",
                site = null,
                facebook = null,
                instagram = "amparoteresina",
                youtube = null,
                maps = "https://www.google.com/maps/place/Igreja+Nossa+Senhora+do+Amparo/@-5.088656,-42.812604,17z",
                slug = "igreja-nossa-senhora-do-amparo",
                contatoWhatsapp = "5586999812345"
            ),
            NetworkIgreja(
                id = 4,
                nome = "Capela São Bento",
                endereco = "Quadra 45, Casa 12 - Dirceu Arcoverde II",
                bairro = "Dirceu Arcoverde",
                cidade = "Teresina",
                paroquia = false,
                capela = true,
                abertaAoPublico = true,
                latitude = -5.118749,
                longitude = -42.756209,
                sacerdotes = "Padre Geraldo, Diácono Paulo",
                telefone = "(86) 3218-4560",
                imagem = "https://images.unsplash.com/photo-1601342614660-f6551b9d4ba4?auto=format&fit=crop&q=80&w=800",
                email = "saobentodirceu@gmail.com",
                site = null,
                facebook = null,
                instagram = "capelasaobento_dirceu",
                youtube = null,
                maps = "https://www.google.com/maps/place/Par%C3%B3quia+S%C3%A3o+Bento/@-5.118749,-42.756209,17z",
                slug = "capela-sao-bento-dirceu",
                contatoWhatsapp = "5586991029384"
            )
        )

        return if (bairro.isNullOrBlank()) {
            list
        } else {
            list.filter { it.bairro?.contains(bairro, ignoreCase = true) == true }
        }
    }

    private fun getMockCelebracoes(categoria: String? = null, dia: String? = null): List<NetworkTipoCelebracao> {
        val igrejas = getMockIgrejas()
        val list = listOf(
            NetworkTipoCelebracao(
                id = 1,
                igreja = 1,
                igrejaDetails = igrejas[0], // Catedral
                nome = "Santa Missa Dominical",
                categoria = "missa",
                recorrencia = "semanal",
                horarioInicio = "08:00",
                horarioFim = "09:15",
                descricao = "Missa solene do Domingo, presidida pelo Arcebispo.",
                dia = "domingo",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 2,
                igreja = 1,
                igrejaDetails = igrejas[0], // Catedral
                nome = "Santa Missa Diária",
                categoria = "missa",
                recorrencia = "semanal",
                horarioInicio = "12:00",
                horarioFim = "12:45",
                descricao = "Missa do meio-dia para trabalhadores e fiéis em geral.",
                dia = "quarta",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 3,
                igreja = 1,
                igrejaDetails = igrejas[0], // Catedral
                nome = "Confissões Individuais",
                categoria = "confissao",
                recorrencia = "semanal",
                horarioInicio = "10:00",
                horarioFim = "11:30",
                descricao = "Sacramento da Reconciliação com agendamento prévio ou ordem de chegada.",
                dia = "quinta",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 4,
                igreja = 2,
                igrejaDetails = igrejas[1], // Fátima
                nome = "Santa Missa de Fátima",
                categoria = "missa",
                recorrencia = "semanal",
                horarioInicio = "19:00",
                horarioFim = "20:00",
                descricao = "Missa comunitária de cura e libertação comandada pelo Monsenhor Tony Batista.",
                dia = "quinta",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 5,
                igreja = 2,
                igrejaDetails = igrejas[1], // Fátima
                nome = "Adoração ao Santíssimo",
                categoria = "adoracao",
                recorrencia = "semanal",
                horarioInicio = "17:30",
                horarioFim = "18:45",
                descricao = "Momento de oração pessoal silenciosa diante do Santíssimo Sacramento exposto.",
                dia = "quinta",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 6,
                igreja = 2,
                igrejaDetails = igrejas[1], // Fátima
                nome = "Confissão Sacramental",
                categoria = "confissao",
                recorrencia = "semanal",
                horarioInicio = "16:00",
                horarioFim = "18:00",
                descricao = "Atendimento pastoral de confissões na Paróquia de Fátima.",
                dia = "sexta",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 7,
                igreja = 3,
                igrejaDetails = igrejas[2], // Amparo
                nome = "Missa Devocional da Padroeira",
                categoria = "missa_votiva",
                recorrencia = "mensal_dia_fixo",
                horarioInicio = "10:00",
                horarioFim = "11:15",
                descricao = "Novena perpétua e missa devocional em honra a Nossa Senhora do Amparo, padroeira de Teresina.",
                dia = null,
                diaMes = 16,
                dataEspecifica = null,
                semanaDoMes = null
            ),
            NetworkTipoCelebracao(
                id = 8,
                igreja = 4,
                igrejaDetails = igrejas[3], // Capela Dirceu
                nome = "Confissões Comunitárias",
                categoria = "confissao",
                recorrencia = "semanal",
                horarioInicio = "15:00",
                horarioFim = "17:00",
                descricao = "Sacramento da Penitência na Capela São Bento no Bairro Dirceu.",
                dia = "sabado",
                diaMes = null,
                dataEspecifica = null,
                semanaDoMes = null
            )
        )

        var filtered = list
        if (!categoria.isNullOrBlank()) {
            filtered = filtered.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }
        if (!dia.isNullOrBlank()) {
            filtered = filtered.filter { it.dia.equals(dia, ignoreCase = true) }
        }
        return filtered
    }
}
