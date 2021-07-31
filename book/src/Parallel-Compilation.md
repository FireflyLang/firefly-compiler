# Parallel Compilation

Firefly first accounts all files it needs to compile and index them in a `HashMap`, then Firefly split those files into different tasks, using **Kotlin Coroutines**.

## Type Resolution

In order to resolve which functions to invoke (as firefly is a strongly-typed language), Firefly uses a **Function Name Cache**, which stores the name of the function and a list of all functions with the same name.

The first time a component tries to resolve a function, firefly first looks into the cache, if the function is not present in the cache and the unit was not fully processed yet, firefly subscribes the component to a `SharedFlow`, which broadcasts processed functions.

Firefly always looks for a function with the exact signature as the provided argument types, so in the best case, it is able to resolve the function by only looking at the cache.

If at the end of the process, Firefly does not find a function with a signature exactly matching the argument types, it will try to find a function which the types match or are convertible to the parameter types counterpart. And only if all resolution strategies fails, Firefly sends an error to the **Error Reporting Channel** about resolution failure.

The same concept applies to other kinds of resolution¹.

## Compile with errors

Firefly is able to fully compile code with errors, because **KoresFramework** allows structures with errors to be compiled as well. However, it does not mean that produced bytecode is runnable. It could be at a certain point.