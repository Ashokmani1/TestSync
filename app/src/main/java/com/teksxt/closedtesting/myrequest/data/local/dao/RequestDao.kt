package com.teksxt.closedtesting.myrequest.data.local.dao

import androidx.room.*
import com.teksxt.closedtesting.myrequest.data.local.entity.RequestEntity
import com.teksxt.closedtesting.myrequest.domain.model.Request
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests ORDER BY createdAt DESC")
    fun getAllRequestsEntities(): Flow<List<RequestEntity>>

    fun getAllRequests(): Flow<List<Request>> =
        getAllRequestsEntities().map { entities ->
            entities.map { it.toDomainModel() }
        }

    @Query("SELECT * FROM requests WHERE id = :requestId")
    fun getRequestEntityById(requestId: String): Flow<RequestEntity?>

    fun getRequestById(requestId: String): Flow<Request?> =
        getRequestEntityById(requestId).map { entity ->
            entity?.toDomainModel()
        }

    @Query("SELECT * FROM requests WHERE id = :requestId")
    suspend fun getRequestEntityByIdSync(requestId: String): RequestEntity?

    suspend fun getRequestByIdSync(requestId: String): Request? =
        getRequestEntityByIdSync(requestId)?.toDomainModel()

    @Query("SELECT * FROM requests WHERE createdBy = :userId ORDER BY createdAt DESC")
    fun getRequestEntitiesByUser(userId: String): Flow<List<RequestEntity>>

    fun getRequestsByUser(userId: String): Flow<List<Request>> =
        getRequestEntitiesByUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestEntity(requestEntity: RequestEntity)

    suspend fun insertRequest(request: Request) {
        insertRequestEntity(RequestEntity.fromDomainModel(request))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestEntities(requestEntities: List<RequestEntity>)

    suspend fun insertRequests(requests: List<Request>) {
        insertRequestEntities(requests.map { RequestEntity.fromDomainModel(it) })
    }

    @Update
    suspend fun updateRequestEntity(requestEntity: RequestEntity)

    suspend fun updateRequest(request: Request) {
        updateRequestEntity(RequestEntity.fromDomainModel(request))
    }

    @Delete
    suspend fun deleteRequestEntity(requestEntity: RequestEntity)

    suspend fun deleteRequest(request: Request) {
        deleteRequestEntity(RequestEntity.fromDomainModel(request))
    }

    @Query("DELETE FROM requests")
    suspend fun clearAllRequests()
}