# The Bloqqi compiler

Clone the repository and test the Bloqqi compiler:

    $ git clone git@github.com:bloqqi/bloqqi-compiler.git
    $ cd bloqqi-compiler/
    $ ant test
    <All tests should pass (code generation tests excluded)>

Run all tests, including code generation tests (requires the command ``gcc``):

    $ ant test-all
    <All tests should pass>

Run the Bloqqi compiler on the example program TwoCounters:

    $ ant jar
    $ java -jar bloqqi-compiler.jar examples/TwoCounters.dia
    Program OK

Execute the program TwoCounters by generating C code:

    $ java -jar bloqqi-compiler.jar examples/TwoCounters.dia --c --o=TwoCounters.c
    $ gcc -std=c99 TwoCounters.c examples/PrintFunction.c -o TwoCounters
    $ ./TwoCounters
    1
    2
    2
    4
    ...

The file `example/PrintFunction.c` contains C functions for printing different kinds of values.

## License

The Bloqqi compiler is covered by the modified BSD License. For the full license text see the LICENSE file.

## Website

The website for Bloqqi is [bloqqi.org](http://bloqqi.org).
