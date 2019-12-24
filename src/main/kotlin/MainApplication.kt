package com.jamespatrickkeegan


interface Token {
    fun getValue(): Any?
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

    fun getNextToken(): Token {
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

class Interpreter(expression: String) {
    private var lexer: Lexer = Lexer(expression)
    private var currentToken: Token = lexer.getNextToken()

    private inline fun <reified T: Token> eatToken(token: Token): T {
         if (token !is T) {
             throw Exception("Syntax error, expected %s but got %s".format(
                 T::class, token::class
             ))
         }

        currentToken = lexer.getNextToken()
        return token
    }

    private fun expression(): Double {
        var result = term()

        while (true) {
            if (!(currentToken.getValue() == "+" || currentToken.getValue() == "-")) {
                return result
            }

            val operatorToken = eatToken<OperatorToken>(currentToken)
            val rightHandSide = term()

            result += when (operatorToken.getValue()) {
                "+" -> rightHandSide
                "-" -> -rightHandSide
                else -> throw Exception("Error: Wrong operator.")
            }
        }
    }

    private fun factor(): Double {
        return when(currentToken) {
            is IntegerToken -> eatToken<IntegerToken>(currentToken).getValue().toDouble()
            is LeftParenToken -> {
                eatToken<LeftParenToken>(currentToken)
                val result: Double = this.expression()
                eatToken<RightParenToken>(currentToken)
                return result
            }
            else -> throw Exception("SYNTAX ERROR")
        }
    }

    private fun term(): Double {
        var result = factor()

        while (true) {
            if (!(currentToken.getValue() == "/" || currentToken.getValue() == "*")) {
                return result
            }

            val operatorToken = eatToken<OperatorToken>(currentToken)

            val rightHandSide = factor()

            result *= when (operatorToken.getValue()) {
                "*" -> rightHandSide
                "/" -> 1 / rightHandSide
                else -> throw Exception("Error: Wrong operator.")
            }
        }
    }

    fun evaluate(): Double {
        return expression()
    }
}

fun main(args: Array<String>) {
    println("Please type your mathematical expression down here:")

    val inputText = readLine()
    val interpreter = Interpreter(
        inputText ?: ""
    )

    val result = interpreter.evaluate()
    println(result)
}


