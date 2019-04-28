package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.BaseEntity


interface IBaseDao<T : BaseEntity> {

    fun getAll(): List<T>

    fun saveOrUpdate(entity: T)

    fun saveOrUpdate(entities: List<T>)

    fun delete(entity: T)

}