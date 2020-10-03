package calculator

import java.math.BigInteger
import java.util.*

val scanner = Scanner(System.`in`)
val variables = mutableMapOf<String, String?>()

fun main() {
    var isRun = true
    var n1 = 0
    var n2 = 0
    mainLoop@ while (isRun) {
        var sum = 0
        val input = scanner.nextLine().replace(" ", "")
        when  {
            input == "/exit" -> isRun = false
            input == "/help" -> println("The program calculates the sum of numbers")
            input == "" -> continue
            input.matches("\\s*[a-zA-Z0-9]+".toRegex()) && input.contains("[0-9]+".toRegex()) -> println("Invalid identifier")
            input.matches("\\s*[a-zA-Z]+\\s*=.*".toRegex()) -> assignVars(input)
            input.matches("\\s*[a-zA-Z0-9]+\\s*=.*".toRegex()) -> assignVars(input)
            input.matches("\\s*[a-zA-Z]+".toRegex()) -> printVars(input)
            input.matches("/.*".toRegex()) -> println("Unknown command")
            else -> {
                checkFormula(rebuildInput(input))
            }
        }
    }
    println("Bye!")
}

fun isVars(input: String):Boolean = variables.containsKey(input)
fun isDigit(input: String): Boolean = input.matches("\\d+".toRegex())
fun getVars(input: String): String = variables[input]!!
fun printVars(input: String) {
    if (variables.containsKey(input)) println (variables[input])
    else println("Unknown variables")
}
fun varErrors(input: String) {
    when {
        input.contains("[0-9]+".toRegex()) -> println("Invalid identifier")
        input.matches("[a-zA-Z]+".toRegex()) -> println("Unknown variables")
        else -> println("Invalid assignment")
    }
}
fun assignVars(input: String) {
    val name = input.replace(" ", "").split("=")[0]
    val value = input.replace(" ", "").split("=")[1]
    when {
        name.contains("[0-9]+".toRegex()) -> varErrors(name)
        value.matches("[+-]*[0-9]+".toRegex()) -> variables[name] = value
        variables.containsKey(value) -> variables[name] = variables[value]!!
        //value.matches("[a-zA-Z]+".toRegex()) -> variables[name] = variables[value]!!
        else -> varErrors(value)
    }
}
fun parse(input: String): String {
    when {
        isDigit(input) -> return input
        isVars(input) -> return variables[input]!!
        else -> return "0"
    }
}

fun rebuildInput(input: String): String {
    var output = input.replace("\\s+".toRegex(), "")
    var formula = ""
    output = output.replace("\\++".toRegex(), "\\+")
    var prev = ' '
    var isDigit = false
    var startPos = 0
    if (output[0] == '-') {
        startPos = 1
        isDigit = true
        formula += "-"
    }
    for (i in startPos until output.length) {
        if (output[i].isDigit() || output[i].isLetter()) {
            if (!isDigit) {
                formula += " "
            }
            isDigit = true
            formula += output[i]
        } else {
            if (isDigit) {
                formula += " "
            }
            if (output[i] == '(') {
                formula += " "
            }
            if (prev == ')') {
                formula += " "
            }
            formula += output[i]
            isDigit = false
        }
        prev = output[i]
    }
    formula = formula.trim { it <= ' ' }

    var outputFormula = ""
    for (members in formula.split(" ")) {
        when {
            members.matches("[\\+\\-]*[a-zA-Z0-9]+".toRegex()) -> outputFormula += "$members "
            members.matches("[\\-]+".toRegex()) -> outputFormula += "${convertMinus(members)} "
            else -> outputFormula += "$members "
        }
    }
    outputFormula = outputFormula.trim { it <= ' ' }
    return outputFormula
}

fun checkFormula(input: String) {
    var isVarCorrect = true
    var isBalanced = true
    var formula = ""
    for (oper in input.split(" ")) {
        when {
            oper.matches("\\*\\*+".toRegex()) || oper.matches("\\/\\/+".toRegex()) -> {
                println("Invalid expression")
                return
            }
            oper.matches("[+-]*[0-9]+".toRegex()) -> formula += "$oper "
            variables.containsKey(oper) -> formula += "${variables.get(oper)} "
            oper.matches("[\\+\\-\\*\\/()]+".toRegex()) -> formula += "$oper "
            else -> {
                //println ("$input --- $formula")
                println("Unknown variable")
                return
            }
        }
    }
    var countLeft = 0
    var countRight = 0
    for (oper in input.split(" ")) {
        when (oper) {
            "(" -> countLeft++
            ")" -> countRight++
        }
    }
    if (countLeft != countRight) {
        println ("Invalid expression")
        return
    }

    formula = formula.trim { it <= ' ' }
    calculateRPN(formula)

}

fun convertMinus(members: String): String = if (members.length % 2 == 0) "+" else "-"

fun calculateRPN(formula: String) {
    /* Start conversion  to RPN */
    val operators: MutableList<String> = formula.split(" ").toMutableList()
    var stack: MutableList<String> = mutableListOf<String>()
    var out: MutableList<String> = mutableListOf<String>()

    for (operator in operators) {
        when {
            operator.matches("[\\+\\-\\*\\/]+".toRegex()) -> {
                when {
                    stack.isEmpty() || stack.last().matches("[\\(]+".toRegex()) -> stack.add(operator)
                    operator == "*" && stack.last() == "+" -> stack.add(operator)
                    operator == "*" && stack.last() == "-" -> stack.add(operator)
                    operator == "/" && stack.last() == "+" -> stack.add(operator)
                    operator == "/" && stack.last() == "-" -> stack.add(operator)
                    else -> {
                        while (stack.isNotEmpty() && stack.last().matches("[\\+\\-\\*\\/]+".toRegex())) {
                            when {
                                operator == "*" && stack.last() == "+" -> {
                                    stack.add(operator)
                                    continue
                                }
                                operator == "*" && stack.last() == "-" -> {
                                    stack.add(operator)
                                    continue
                                }
                                operator == "/" && stack.last() == "+" -> {
                                    stack.add(operator)
                                    continue
                                }
                                operator == "/" && stack.last() == "-" -> {
                                    stack.add(operator)
                                    continue
                                }
                            }
                            out.add(stack.last())
                            stack.removeLast()
                            break
                        }
                        stack.add(operator)
                    }
                }
            }
            operator == "(" -> stack.add(operator)
            operator == ")" -> {
                while (stack.isNotEmpty() && stack.last() != "(") {
                    out.add(stack.last())
                    stack.removeLast()
                }
                stack.removeLast()
            }
            operator.matches("[+-]*[0-9]+".toRegex()) -> {
                out.add(operator)
            }
        }

    }
    while (stack.isNotEmpty()) {
        out.add(stack.last())
        stack.removeLast()
    }
    /* End Conversion */

    var stackRes: MutableList<String> = mutableListOf<String>()
    var outRes: MutableList<String> = mutableListOf<String>()

    for (operator in out) {
        when {
            operator.matches(("[+-]*[0-9]+").toRegex()) -> stackRes.add(operator)
            else -> {
                //var oper1 = stackRes.last().toLong()
                var oper1 = BigInteger(stackRes.last())
                stackRes.removeLast()
                //var oper2 = stackRes.last().toLong()
                var oper2 = BigInteger(stackRes.last())
                stackRes.removeLast()
                //var result = 0L
                var result = BigInteger.ZERO
                when (operator) {
                    //"+" -> result = oper2 + oper1
                    "+" -> result = oper2.add(oper1)
                    //"*" -> result = oper2 * oper1
                    "*" -> result = oper2.multiply(oper1)
                    //"-" -> result = oper2 - oper1
                    "-" -> result = oper2.subtract(oper1)
                    //"/" -> result = oper2 / oper1
                    "/" -> result = oper2.divide(oper1)
                }
                stackRes.add(result.toString())
            }
        }
    }
    println(stackRes.last())

}
