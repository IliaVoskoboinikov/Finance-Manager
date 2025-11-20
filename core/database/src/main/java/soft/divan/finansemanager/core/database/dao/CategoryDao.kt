package soft.divan.finansemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): CategoryEntity

    @Query("SELECT * FROM categories WHERE isIncome = :isIncome ORDER BY name ASC")
    fun getByType(isIncome: Boolean): Flow<List<CategoryEntity>>
}