package net.dankito.accounting.data.dao


interface IBaseDao<T> {

    fun getAll(): List<T>

    fun saveOrUpdate(T: T)

    fun delete(entity: T)

}