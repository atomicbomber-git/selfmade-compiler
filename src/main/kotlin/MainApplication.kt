package com.jamespatrickkeegan

import sun.misc.OSEnvironment

interface Token {
    abstract fun getValue(): Any
}

class EndOfFileToken: Token {
    override fun getValue() = "EOF"
}

class OperatorToken(private val value: String): Token {
    override fun getValue() = value
}

class IntegerToken(private val value: Int): Token {
    override fun getValue() = value
}

class Interpreter(private val expression: String) {
    private var index = 0
    private var result = 0

    private fun getNextToken(): Token {
        skipWhitespace()

        when {
            index > expression.length - 1 -> {
                return EndOfFileToken()
            }
            expression[index].isDigit() -> {
                var digits: String = ""

                digits += expression[index]
                ++index

                while (index < expression.length && expression[index].isDigit()) {
                    digits += expression[index]
                    ++index
                }

                return IntegerToken(digits.toInt())
            }
            expression[index] == '+' -> {
                ++index
                return OperatorToken("+")
            }
            expression[index] == '-' -> {
                ++index
                return OperatorToken("-")
            }
            else -> throw Exception("Failed to parse '%s'".format(expression[index]))
        }
    }

    private inline fun <reified T: Token> eatToken(token: Token): T {
         if (token !is T) {
             throw Exception("Syntax error, expected %s but got %s".format(
                 T::class, token::class
             ))
         }

        return token as T
    }

    private fun skipWhitespace() {
        while (index < expression.length && expression[index].isWhitespace()) {
            index++
        }
    }

    public fun evaluate(): Int {

        var nextToken = getNextToken()

        val left: Int = eatToken<IntegerToken>(nextToken).getValue()

        result += left

        while (true) {
            nextToken = getNextToken()

            if (nextToken is EndOfFileToken) {
                break
            }

            val operator: String = eatToken<OperatorToken>(nextToken).getValue()
            val right: Int = eatToken<IntegerToken>(getNextToken()).getValue()

            if (operator == "+") {
                result += right
            }
            else if (operator == "-") {
                result -= right
            }
            else {
                throw Exception("OPERATOR ERROR")
            }
        }

        return result
    }
}

fun main(args: Array<String>) {
    val inputText = readLine()

    val interpreter: Interpreter = Interpreter(
        inputText ?: ""
    )

    val result = interpreter.evaluate()
    println(result)
}


