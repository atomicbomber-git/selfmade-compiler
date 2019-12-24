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

interface Tree {
    fun getToken(): Token
    fun print()
}

class BinaryOperatorTree(
    val leftTree: Tree,
    val rightTree: Tree,
    private val token: Token
): Tree
{
    override fun getToken() = token
    override fun print() {
        println("Encountered BinaryOperatorTree with value " + token.getValue())
        leftTree.print()
        rightTree.print()
    }
}

class NumberTree(private val token: IntegerToken): Tree {
    override fun getToken(): IntegerToken = token
    override fun print() = println("Encountered NumberTree with value " + token.getValue())
}

class NormalTreeVisitor(private val tree: Tree) {
    fun visit(): Double {
        when (tree) {
            is NumberTree -> return tree.getToken().getValue().toDouble()
            is BinaryOperatorTree -> {
                val leftHandSide = NormalTreeVisitor(tree.leftTree).visit()
                val rightHandSide = NormalTreeVisitor(tree.rightTree).visit()

                return when(tree.getToken().getValue()) {
                    "*" -> leftHandSide * rightHandSide
                    "/" -> leftHandSide / rightHandSide
                    "-" -> leftHandSide - rightHandSide
                    "+" -> leftHandSide + rightHandSide
                    else -> throw Exception("Unknown operator exception.")
                }
            }
            else -> throw Exception("Unknown tree type.")
        }
    }
}



class Parser(expression: String) {
    private var lexer: Lexer = Lexer(expression)
    private var currentToken: Token = lexer.getNextToken()

    private inline fun <reified T: Token> eatCurrentToken(): T {
        if (currentToken !is T) {
            throw Exception("Syntax error, expected %s but got %s".format(
                T::class, currentToken::class
            ))
        }

        val oldToken = currentToken
        currentToken = lexer.getNextToken()

        return oldToken as T
    }

    private fun expression(): Tree {
        var leftHandSide = term()

        while (true) {
            if (!(currentToken.getValue() == "+" || currentToken.getValue() == "-")) {
                return leftHandSide
            }

            val operatorToken = eatCurrentToken<OperatorToken>()
            val rightHandSide = term()

            leftHandSide = when (operatorToken.getValue()) {
                "+" -> BinaryOperatorTree(leftHandSide, rightHandSide, operatorToken)
                "-" -> BinaryOperatorTree(leftHandSide, rightHandSide, operatorToken)
                else -> throw Exception("Error: Wrong operator.")
            }
        }
    }

    private fun factor(): Tree {
        return when(currentToken) {
            is IntegerToken -> NumberTree(eatCurrentToken<IntegerToken>())
            is LeftParenToken -> {
                eatCurrentToken<LeftParenToken>()
                val result = expression()
                eatCurrentToken<RightParenToken>()
                return result
            }
            else -> throw Exception("SYNTAX ERROR")
        }
    }

    private fun term(): Tree {
        var leftHandSide = factor()

        while (true) {
            if (!(currentToken.getValue() == "/" || currentToken.getValue() == "*")) {
                return leftHandSide
            }

            val operatorToken = eatCurrentToken<OperatorToken>()
            val rightHandSide = factor()

            leftHandSide = when (operatorToken.getValue()) {
                "*" -> BinaryOperatorTree(leftHandSide, rightHandSide, operatorToken)
                "/" -> BinaryOperatorTree(leftHandSide, rightHandSide, operatorToken)
                else -> throw Exception("Error: Unknown or wrong operator")
            }
        }
    }

    fun parse(): Tree {
        return expression()
    }
}

fun main(args: Array<String>) {

    while (true) {
        println("Please type your mathematical expression down here (q for quit):")

        val inputText = readLine()

        if (inputText.equals("q")) {
            break
        }

        val parser = Parser(
            inputText ?: ""
        )

        val tree: Tree = parser.parse()
        tree.print()

        println(NormalTreeVisitor(tree).visit())
    }
}


