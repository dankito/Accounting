package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.BaseEntity
import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File
import java.util.*


abstract class JsonBasedDao<T : BaseEntity>(private val entityClass: Class<T>, dataFolder: File, jsonFileName: String) : IBaseDao<T> {

    protected val jsonFile = File(dataFolder, jsonFileName)

    protected val serializer = JacksonJsonSerializer()

    protected val entities: MutableSet<T> = retrieveAllEntities()


    init {
        jsonFile.parentFile.mkdirs()
    }


    override fun getAll(): List<T> {
        return entities.toList()
    }

    override fun saveOrUpdate(entity: T) {
        setIdIfNotPersistedYet(entity)

        entities.add(entity)

        saveAllEntities()
    }

    override fun delete(entity: T) {
        entities.remove(entity)

        saveAllEntities()
    }


    protected open fun retrieveAllEntities(): MutableSet<T> {
        return serializer.deserializeSetOr(jsonFile, entityClass).toMutableSet()
    }

    protected open fun saveAllEntities() {
        serializer.serializeObject(entities, jsonFile)
    }


    protected open fun setIdIfNotPersistedYet(entity: BaseEntity) {
        if (entity.isPersisted() == false) {
            entity.id = UUID.randomUUID().toString()
        }
    }

}