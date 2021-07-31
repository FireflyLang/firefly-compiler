# AST Translation

**firefly-compiler** uses the [Kores Framework](https://github.com/koresframework) to compile Firefly code into JVM Bytecode. In order to be able to compile Firefly code, we need first to translate the AST produced by **Antlr Generated Parser** into **Kores AST**.

## Translation process

The translation process uses a context represented by **TypedData** which is stored in a **Stack** structure. Once a step of the conversion is done and the visitor **exits** the **node**, it pops the stack, until it get to the root context and exits the **root** **node**. 

When the visitor exits the **root node**, it sends the **Parsed Structure** to the **FireflyCompiler** through a **Channel**.