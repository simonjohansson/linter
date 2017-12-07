import build.ConcoursePipelineBuilder
import lint.linters.*
import model.Result
import model.hasErrors
import parser.Parser
import reader.Reader
import secrets.Secrets
import us.jimschubert.kopper.typed.StringArgument
import us.jimschubert.kopper.typed.TypedArgumentParser
import kotlin.system.exitProcess

fun printResult(result: Result) {
    println("${result.linter}")
    if (result.hasErrors()) {
        for (error in result.errors) {
            println("\t${error.type}\t\t${error.message}")
            println("\tHow to fix:\t\t${error.documentation}")
            println()
        }
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

fun lint(arguments: Args): List<Result> {
    val reader = Reader(arguments.path)
    val secrets = Secrets(vaultToken = arguments.vaultToken, sslVerify = false)
    return Linter(
            requiredFilesLinter = RequiredFilesLinter(reader),
            requiredFieldsLinter = RequiredFieldsLinter(),
            runLinter = RunLinter(reader, secrets),
            deployLinter = DeployLinter(reader, secrets),
            repoLinter = RepoLinter(secrets),
            dockerLinter = DockerLinter(reader, secrets),
            parser = Parser(reader)).lint()
}

fun runLint(arguments: Args) = printResults(lint(arguments))

fun runBuild(arguments: Args) {
    val lint = lint(arguments)
    if (lint.hasErrors()) {
        println("Cannot build, as there are lint errors...")
        printResults(lint)
    }

    Parser(Reader(arguments.path)).parseManifest().map { manifest ->
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
        "lint" -> runLint(arguments)
        "build" -> runBuild(arguments)
        else -> {
            println("I have no idea what ${arguments.type} is")
            println("Please run with -t lint|build")
        }
    }
}

class Args(args: Array<String>) : TypedArgumentParser(args) {
    val path by StringArgument(self, "p", default = "", longOption = listOf("path"))
    val type by StringArgument(self, shortOption = "t", description = "'lint' or 'build'")
    val vaultToken by StringArgument(self, shortOption = "v", description = "Vault token")
}