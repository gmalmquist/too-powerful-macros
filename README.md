# too-powerful-macros
C-like pre-processor for macros, constant symbols, includes, and calls to external compilers.

## Why
I built TPM with web coding in mind, because too often I find myself wishing I could define constants when writing CSS of HTML (eg, defining BACKGROUND_COLOR a symbol). Another big use case was processing includes in arbitrary locations in arbitrary files -- because HTML files are a pain to split up.

Now, this could all obviously be smoothed over with server-side scripting, but (at the strong risk of being completely hypocritical) server-side scripting seems like overkill for a static webpage with no dynamic elements that actually need to be changed by the server, outside of programmatic convenience. I also frequently find that most consumer webhosting is fairly limited in what server-side languages are supported, and I like to avoid PHP whenever possible.

This macro processor is "too powerful" because it gives you plenty of rope to hang yourself with if mis-used.

## Building
TPM uses pants (https://github.com/pantsbuild/pants) for building. Pants is hermetic, so if you clone TPM you shouldn't have to install anything to get things up and running. 

To build the runnable tpm bundled jar from the root directory, use the command:
```bash
./pants bundle src/java
```

This assumes your are building TPM on a unix-based machine; it may or may not actually build on windows. But once TPM is built, it should be cross-platform (though that hasn't yet been tested).


## Syntax and Usage Examples
TPM syntax is a lot like the C-preprocessor. For example:
```
#define MY_AWESOME_CONSTANT 27
#define my_lowercase_constant 42.8
#define RECURSION (MY_AWESOME_CONSTANT*3)
#define ADD(a,b)  ((a)+(b))
#define multiline multi \
                  line \
                  symbol
#define MUL(a,b)  ((a) * \
                   (b))
...
void someFunction() {
  int x = MY_AWESOME_CONSTANT;
  int y = MUL(x, ADD(x, 10.0)) / RECURSION;
}
```

Include syntax is:
```
#include "filepath/relative/to/working/directory/foo.css"
```

If blocks:
```
#ifdef SYMBOL
... 
#endif
```
Supported IF statements include:
- *ifdef* SYMBOL: include block if SYMBOL is defined.
- *ifndef* SYMBOL 
- *ifdir* DIRECTORY: include block only if DIRECTORY is found along the path to the current source file. (eg, '#ifdir styles')
- *ifndir* DIRECTORY
- *ifext* EXTENSION: include block only if EXTENSION is the file extension of the current source file. (eg, '#ifext css')
- *ifnext*


In addition to the standard symbols, macros, and includes, TPM also supports external compilers. Consider the code:
```
#external cool-program with some commandline arguments
... input code to cool-program 
...
#end
```
When TPM sees this block, it make a system call to "cool-program" with the given arguments, and will write anything inside the block (the input) to cool-program's standard input. It then replaces the entire block in the original source file with whatever the standard output of cool-program was. Obviously, this is extremely powerful, and has incredible potentional for both utility and abuse.

## Processing order
For efficiency and robustness, TPM processes input code in the following phases:
1. includes - all *#include* statements are replaced by the source files they reference. Circular includes are ignored.
2. constant definitions, macro definitions, and if-statements
3. macro calls
4. constant references
5. external compilers

Steps 3 and 4 are run repeatedly until no changes are made, thus supporting recursion. Currently the recursion depth is hard-coded to a maximum limit of 10.

## TODO
- Allow commandline specified recursion limit.
- Suppress all the debug messages polluting STDOUT unless a -verbose option is specified.
- Allow users to skip certain steps (eg, external calls) if they don't need them, to save time.
- Handle includes more efficiently.
- Clean up and document all the code.

## FAQ
- *Why didn't you just use X templating library?* I'd been reading up on grammars for fun, and wanted to try my hand at creating something resembling a programming language.
