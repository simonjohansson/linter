import build.ConcoursePipelineBuilder
import lint.linters.*
import model.Result
import model.hasErrors
import parser.Parser
import reader.Reader
import us.jimschubert.kopper.typed.StringArgument
import us.jimschubert.kopper.typed.TypedArgumentParser
import kotlin.system.exitProcess

fun printResult(result: Result) {
    println("${result.linter}")
    if (result.hasErrors()) {
        for (error in result.errors)
            println("\t${error.type}\t\t${error.message}")
    } else {
        println("\tNo errors!")
    }
    println()
}

fun printResults(results: List<Result>) {
    results.map { printResult(it) }
    if (results.hasErrors()) {
        println("Please fix the issues :_(")
        exitProcess(-1)
    } else {
        println("No errors detected, yay!")
    }
}

fun lint(path: String): List<Result> {
    val reader = Reader(path)
    return Linter(
            requiredFilesLinter = RequiredFilesLinter(reader),
            requiredFieldsLinter = RequiredFieldsLinter(),
            testLinter = TestLinter(reader),
            buildLinter = BuildLinter(reader),
            repoLinter = RepoLinter(),
            parser = Parser(reader)).lint()
}

fun runLint(path: String) = printResults(lint(path))

fun runBuild(path: String) {
    val lint = lint(path)
    if (lint.hasErrors()) {
        println("Cannot build, as there are lint errors...")
        printResults(lint)
    }

    Parser(Reader(path)).parseManifest().map { manifest ->
        println(ConcoursePipelineBuilder().build(manifest))
    }
}

fun main(args: Array<String>) {
    val arguments = Args(args)
    if (arguments.path.isEmpty()) {
        println("Please run with -p /full/path/to/root/of/repo")
        return
    }

    if (arguments.type.isEmpty()) {
        println("Please run with -t lint|build")
        return
    }

    when (arguments.type) {
        "lint" -> runLint(arguments.path)
        "build" -> runBuild(arguments.path)
        else -> {
            println("I have no idea what ${arguments.type} is")
            println("Please run with -t lint|build")
        }
    }
}

class Args(args: Array<String>) : TypedArgumentParser(args) {
    val path by StringArgument(self, "p", default = "", longOption = listOf("path"))
    val type by StringArgument(self, shortOption = "t", description = "'lint' or 'build'")
}