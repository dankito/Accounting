package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Document
import java.io.File


// TODO: replace by a database based implementation
class JsonDocumentDao(dataFolder: File) : IDocumentDao, JsonBasedDao<Document>(Document::class.java, dataFolder, "documents.json")