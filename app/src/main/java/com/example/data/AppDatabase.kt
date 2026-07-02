package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExameItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exameDao(): ExameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "santo_guia_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.exameDao())
                }
            }
        }

        suspend fun populateDatabase(exameDao: ExameDao) {
            val items = listOf(
                // Categoria 1: Relação com Deus (1º ao 3º Mandamento)
                ExameItem(
                    id = "d1",
                    categoria = "Deveres para com Deus",
                    pergunta = "Duvidei de alguma verdade de Fé ensinada pela Igreja ou negligenciei minha formação espiritual?",
                    vergonhaOuGravidade = "Grave"
                ),
                ExameItem(
                    id = "d2",
                    categoria = "Deveres para com Deus",
                    pergunta = "Fiz minhas orações diárias com desatenção, descaso ou passei dias inteiros sem falar com Deus?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "d3",
                    categoria = "Deveres para com Deus",
                    pergunta = "Deixei de ir à Santa Missa aos Domingos ou Dias de Guarda por pura preguiça ou desleixo?",
                    vergonhaOuGravidade = "Grave"
                ),
                ExameItem(
                    id = "d4",
                    categoria = "Deveres para com Deus",
                    pergunta = "Comunguei em estado de pecado grave sem antes ter feito uma boa Confissão?",
                    vergonhaOuGravidade = "Grave"
                ),
                ExameItem(
                    id = "d5",
                    categoria = "Deveres para com Deus",
                    pergunta = "Falei o Nome de Deus em vão, fiz juramentos falsos ou usei expressões desrespeitosas?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "d6",
                    categoria = "Deveres para com Deus",
                    pergunta = "Fui supersticioso, participei de práticas de adivinhação, consultei horóscopos ou rituais contrários à fé católica?",
                    vergonhaOuGravidade = "Grave"
                ),

                // Categoria 2: Relação com o Próximo (4º ao 10º Mandamento)
                ExameItem(
                    id = "p1",
                    categoria = "Deveres para com o Próximo",
                    pergunta = "Tratei meus pais, cônjuge, superiores ou familiares com desrespeito, rispidez, grosseria ou desobediência?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "p2",
                    categoria = "Deveres para com o Próximo",
                    pergunta = "Alimentei ódio, rancor ou desejei mal a alguém? Recusei perdoar ou busquei vingança?",
                    vergonhaOuGravidade = "Grave"
                ),
                ExameItem(
                    id = "p3",
                    categoria = "Deveres para com o Próximo",
                    pergunta = "Espalhei fofocas, mentiras, julguei as pessoas precipitadamente ou prejudiquei a reputação de alguém?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "p4",
                    categoria = "Deveres para com o Próximo",
                    pergunta = "Roubei, fraudou ou causei dano material de propósito ao patrimônio de outra pessoa? Devolvi ou reparei o dano?",
                    vergonhaOuGravidade = "Grave"
                ),
                ExameItem(
                    id = "p5",
                    categoria = "Deveres para com o Próximo",
                    pergunta = "Fui egoísta, recusei-me a estender a mão aos necessitados ou ignorei as obras de misericórdia?",
                    vergonhaOuGravidade = "Normal"
                ),

                // Categoria 3: Vida Pessoal e Virtudes (Modéstia, Pureza e Autocontrole)
                ExameItem(
                    id = "v1",
                    categoria = "Deveres para consigo mesmo",
                    pergunta = "Fui vaidoso, orgulhoso, arrogante ou menosprezei os outros para sobressair?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "v2",
                    categoria = "Deveres para consigo mesmo",
                    pergunta = "Consumi pornografia ou consenti em pensamentos, desejos ou fantasias impuras de forma deliberada?",
                    vergonhaOuGravidade = "Grave"
                ),
                ExameItem(
                    id = "v3",
                    categoria = "Deveres para consigo mesmo",
                    pergunta = "Fui preguiçoso nas minhas responsabilidades do trabalho, de estudo ou no cuidado com a saúde?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "v4",
                    categoria = "Deveres para consigo mesmo",
                    pergunta = "Excedi-me no consumo de bebida alcoólica, comida ou abusei de remédios/substâncias nocivas?",
                    vergonhaOuGravidade = "Normal"
                ),
                ExameItem(
                    id = "v5",
                    categoria = "Deveres para consigo mesmo",
                    pergunta = "Senti ciúme ou inveja excessiva do sucesso, beleza, bens ou dons dos outros?",
                    vergonhaOuGravidade = "Normal"
                )
            )
            exameDao.insertAll(items)
        }
    }
}
