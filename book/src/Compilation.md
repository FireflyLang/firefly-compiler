# Compilation

The compilation is done by the **FireflyCompiler**, which takes **parsed units** from a **Channel** and send their **TypeDeclaration** to [**Kores-BytecodeWriter Processor**](https://github.com/koresframework/Kores-BytecodeWriter/blob/master/src/main/kotlin/com/github/jonathanxd/kores/bytecode/processor/BytecodeGenerator.kt#L198), then send the result (i.e. the compiled code) to a **Channel**.