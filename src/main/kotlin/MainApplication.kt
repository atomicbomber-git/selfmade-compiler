package com.jamespatrickkeegan


interface Token {

    abstract fun getValue(): Any?
}

class EndOfFileToken: Token {
    override fun getValue() = "EOF"
}

class OperatorToken(private val operatorSymbol: String): Token {
    override fun getValue() = operatorSymbol
}

class LeftParenToken: Token {
    override fun getValue() = "("
}

class RightParenToken: Token {
    override fun getValue() = ")"
}

class IntegerToken(private val value: Int): Token {
    override fun getValue() = value
}

class Lexer(private val text: String) {
    private var index = 0

    public fun getNextToken(): Token {
        skipWhitespace()

        return when {
            index > text.length - 1 -> EndOfFileToken()
            text[index].isDigit() -> getNextIntegerToken()
            text[index] == '+' -> getNextOperatorToken("+")
            text[index] == '-' -> getNextOperatorToken("-")
            text[index] == '*' -> getNextOperatorToken("*")
            text[index] == '/' -> getNextOperatorToken("/")
            text[index] == '(' -> getNextLeftParenToken()
            text[index] == ')' -> getNextRightParenToken()

            else -> throw Exception("Failed to parse '%s'".format(text[index]))
        }
    }

    private fun getNextLeftParenToken(): LeftParenToken {
        index++
        return LeftParenToken()
    }

    private fun getNextRightParenToken(): RightParenToken {
        index++
        return RightParenToken()
    }

    private fun getNextOperatorToken(operatorSymbol: String): OperatorToken {
        index++
        return OperatorToken(operatorSymbol)
    }

    private fun getNextIntegerToken(): Token {
        var digits = ""

        digits += text[index]
        ++index

        while (index < text.length && text[index].isDigit()) {
            digits += text[index]
            ++index
        }

        return IntegerToken(digits.toInt())
    }

    private fun skipWhitespace() {
        while (index < text.length && text[index].isWhitespace()) {
            index++
        }
    }
}

class Interpreter(private val expression: String) {
    private var lexer: Lexer = Lexer(expression)
    private var result: Double = 0.0

    private inline fun <reified T: Token> eatToken(token: Token): T {
         if (token !is T) {
             throw Exception("Syntax error, expected %s but got %s".format(
                 T::class, token::class
             ))
         }
        return token as T
    }

    private fun expression() {

    }

    private fun factor(): Double {
        return eatToken<IntegerToken>(lexer.getNextToken())
            .getValue()
            .toDouble()
    }

    private fun term(): Double {
        var result = factor()
        val operatorToken = eatToken<OperatorToken>(lexer.getNextToken())

        while (true) {
            val rightHandSide = factor()

            result *= when (operatorToken.getValue()) {
                "*" -> rightHandSide
                "/" -> 1 / rightHandSide
                else -> throw Exception("Error: Wrong operator.")
            }

            break
        }

        return result
    }

    public fun evaluate(): Double {
        return term()
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


