package soft.divan.financemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.CurrencyEntity

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currency WHERE id = :id")
    suspend fun getCurrencyById(id: String): CurrencyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)

    @Query("DELETE FROM currency")
    suspend fun clearCurrencies()
}
