package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Document
import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File


// TODO: replace by a database based implementation
class JsonDocumentDao(dataFolder: File) : IDocumentDao {

    private val documentsJsonFile = File(dataFolder, "documents.json")

    private val serializer = JacksonJsonSerializer()

    private val documents: MutableSet<Document> = retrieveDocuments()


    init {
        documentsJsonFile.parentFile.mkdirs()
    }


    private fun retrieveDocuments(): MutableSet<Document> {
        return serializer.deserializeSet(documentsJsonFile, Document::class.java).toMutableSet()
    }


    override fun getAll(): List<Document> {
        return documents.toList()
    }

    override fun saveOrUpdate(entity: Document) {
        documents.add(entity)

        saveDocuments()
    }

    override fun delete(entity: Document) {
        documents.remove(entity)

        saveDocuments()
    }


    private fun saveDocuments() {
        serializer.serializeObject(documents, documentsJsonFile)
    }

}