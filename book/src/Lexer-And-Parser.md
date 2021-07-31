# Antlr4 Lexer and Parser

**firefly-compiler** uses the power of [Antlr4](https://www.antlr.org/) to generate lexer and parser from grammar files. Antlr generated parser produces a Tree from the input source file, then an intermediary translator (the [**FireflyLangAstTranslatorListener**](https://github.com/FireflyLang/firefly-compiler/blob/main/src/main/kotlin/io/github/fireflylang/compiler/parser/FireflyLangAstTranslatorListener.kt)) translates Antlr4 AST into [Kores AST](https://github.com/koresframework/Kores), which is further compiled by the [Kores BytecodeWriter](https://github.com/koresframework/Kores-BytecodeWriter).

## Grammar

The grammar used to generate the parser can be found in `src/main/antlr` path.