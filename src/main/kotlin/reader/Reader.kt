package reader

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

interface IReader {
    fun fileExists(path: String): Boolean
    fun readFile(path: String): String
    fun fileExecutable(path: String): Boolean
}

class Reader(val repoRootPath: String) : IReader {

    private fun fullPath(path: String) = Paths.get(repoRootPath, path).toString()

    override fun fileExecutable(path: String) = File(fullPath(path)).canExecute()

    override fun readFile(path: String)= Files.readAllLines(Paths.get(fullPath(path))).joinToString("\n")

    override fun fileExists(path: String) = File(fullPath(path)).exists()
}