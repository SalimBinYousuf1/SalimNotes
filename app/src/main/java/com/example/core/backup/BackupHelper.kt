package com.example.core.backup

import android.content.Context
import com.example.data.local.SalimDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupHelper {
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 600000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12

    suspend fun exportBackup(
        context: Context,
        targetFile: File,
        password: String? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("salim_notes.db")
                val attachmentsDir = File(context.filesDir, "attachments")

                // First package into a ZIP in memory
                val zipByteStream = ByteArrayOutputStream()
                ZipOutputStream(zipByteStream).use { zipOut ->
                    // 1. Add database file
                    if (dbFile.exists()) {
                        zipOut.putNextEntry(ZipEntry("salim_notes.db"))
                        FileInputStream(dbFile).use { it.copyTo(zipOut) }
                        zipOut.closeEntry()
                    }

                    // 2. Add attachments
                    if (attachmentsDir.exists() && attachmentsDir.isDirectory) {
                        attachmentsDir.walkTopDown().filter { it.isFile }.forEach { file ->
                            val relativePath = "attachments/" + file.relativeTo(attachmentsDir).path
                            zipOut.putNextEntry(ZipEntry(relativePath))
                            FileInputStream(file).use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                }

                val zipBytes = zipByteStream.toByteArray()

                if (password.isNullOrBlank()) {
                    // Plain backup
                    FileOutputStream(targetFile).use { it.write(zipBytes) }
                } else {
                    // Encrypted with PBKDF2 + AES-GCM
                    val salt = ByteArray(SALT_LENGTH)
                    SecureRandom().nextBytes(salt)

                    val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                    val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                    val keyBytes = factory.generateSecret(spec).encoded
                    val secretKey = SecretKeySpec(keyBytes, "AES")

                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    val iv = cipher.iv
                    val encrypted = cipher.doFinal(zipBytes)

                    FileOutputStream(targetFile).use { out ->
                        out.write(salt)
                        out.write(iv)
                        out.write(encrypted)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun restoreBackup(
        context: Context,
        sourceFile: File,
        password: String? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val rawBytes = FileInputStream(sourceFile).use { it.readBytes() }
                val zipBytes: ByteArray = if (password.isNullOrBlank()) {
                    rawBytes
                } else {
                    val salt = rawBytes.copyOfRange(0, SALT_LENGTH)
                    val iv = rawBytes.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
                    val cipherBytes = rawBytes.copyOfRange(SALT_LENGTH + IV_LENGTH, rawBytes.size)

                    val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                    val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                    val keyBytes = factory.generateSecret(spec).encoded
                    val secretKey = SecretKeySpec(keyBytes, "AES")

                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    val gcmSpec = GCMParameterSpec(128, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                    cipher.doFinal(cipherBytes)
                }

                // Extract Zip contents
                ZipInputStream(zipBytes.inputStream()).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        if (name == "salim_notes.db") {
                            val dbFile = context.getDatabasePath("salim_notes.db")
                            FileOutputStream(dbFile).use { zipIn.copyTo(it) }
                        } else if (name.startsWith("attachments/")) {
                            val subPath = name.removePrefix("attachments/")
                            val target = File(File(context.filesDir, "attachments"), subPath)
                            target.parentFile?.mkdirs()
                            FileOutputStream(target).use { zipIn.copyTo(it) }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun exportMarkdownZip(
        context: Context,
        targetFile: File
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = SalimDatabase.getDatabase(context)
                val notes = db.noteDao().getAllActiveNotesSync()

                ZipOutputStream(FileOutputStream(targetFile)).use { zipOut ->
                    for (note in notes) {
                        val safeTitle = note.title.ifBlank { "Untitled" }
                            .replace(Regex("[^a-zA-Z0-9_ -]"), "_")
                            .take(50)
                        val fileName = "${safeTitle}_${note.id.take(8)}.md"
                        zipOut.putNextEntry(ZipEntry(fileName))

                        val checklists = db.checklistDao().getItemsForNoteSync(note.id)
                        val markdownContent = buildString {
                            appendLine("# ${note.title.ifBlank { "Untitled" }}")
                            appendLine()
                            if (note.plainText.isNotBlank()) {
                                appendLine(note.plainText)
                                appendLine()
                            }
                            if (checklists.isNotEmpty()) {
                                appendLine("## Checklist")
                                for (item in checklists) {
                                    val mark = if (item.isChecked) "[x]" else "[ ]"
                                    appendLine("- $mark ${item.text}")
                                }
                            }
                        }
                        zipOut.write(markdownContent.toByteArray(Charsets.UTF_8))
                        zipOut.closeEntry()
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
