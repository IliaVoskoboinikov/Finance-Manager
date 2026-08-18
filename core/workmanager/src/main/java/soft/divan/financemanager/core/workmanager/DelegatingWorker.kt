package soft.divan.financemanager.core.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * Точка входа для получения [HiltWorkerFactory] в рантайме.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun hiltWorkerFactory(): HiltWorkerFactory
}

/** Ключ в [Data], под которым лежит имя класса делегата. */
const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Кладёт в WorkRequest метаданные о том, какому [CoroutineWorker] должен делегировать
 * выполнение [DelegatingWorker].
 */
fun KClass<out CoroutineWorker>.delegatedData(): Data =
    Data.Builder()
        .putString(WORKER_CLASS_NAME, java.name)
        .build()

/**
 * Воркер, который делегирует работу другому [CoroutineWorker], созданному через
 * [HiltWorkerFactory].
 *
 * Нужен потому, что `:app` не настраивает WorkManager вручную (нет
 * `Configuration.Provider`), а значит WorkManager инициализируется по умолчанию и умеет
 * создавать только воркеры с конструктором `(Context, WorkerParameters)`. `@HiltWorker`
 * с внедрёнными зависимостями такой фабрике недоступен.
 *
 * Поэтому в очередь всегда ставится [DelegatingWorker], а имя реального воркера
 * передаётся в inputData через [delegatedData]. Это позволяет держать `@HiltWorker`
 * в библиотечных модулях (`:sync`, `:core:notifications`), не отдавая им владение
 * конфигурацией WorkManager.
 */
class DelegatingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val workerClassName = workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

    private val delegateWorker =
        EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
            .hiltWorkerFactory()
            .createWorker(appContext, workerClassName, workerParams) as? CoroutineWorker
            ?: throw IllegalArgumentException("Unable to find appropriate worker: $workerClassName")

    override suspend fun getForegroundInfo(): ForegroundInfo = delegateWorker.getForegroundInfo()

    override suspend fun doWork(): Result = delegateWorker.doWork()
}
