package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Document
import net.dankito.utils.io.FileUtils
import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File


// TODO: replace by a database based implementation
class JsonDocumentDao(dataFolder: File) : IDocumentDao {

    private val documentsJsonFile = File(dataFolder, "documents.json")

    private val serializer = JacksonJsonSerializer()

    private val fileUtils = FileUtils()

    private val documents: MutableSet<Document> = retrieveDocuments()


    init {
        documentsJsonFile.parentFile.mkdirs()
    }


    private fun retrieveDocuments(): MutableSet<Document> {
        // TODO: use new convenience methods as soon as JavaUtils 2.0.0 is out
        if (documentsJsonFile.exists()) {
            fileUtils.readFromTextFile(documentsJsonFile)?.let { documentsJson ->
                return serializer.deserializeObject(documentsJson, MutableSet::class.java, Document::class.java)
                        as MutableSet<Document>
            }
        }

        return mutableSetOf()
    }


    override fun saveOrUpdate(document: Document) {
        documents.add(document)

        // TODO: use new convenience methods as soon as JavaUtils 2.0.0 is out
        val documentsJson = serializer.serializeObject(documents)
        fileUtils.writeToTextFile(documentsJson, documentsJsonFile)
    }

    override fun getAll(): List<Document> {
        return documents.toList()
    }

}