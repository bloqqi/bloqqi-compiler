# The Bloqqi compiler

To test and run the Bloqqi compiler:

    $ git clone git@bitbucket.org:bloqqi/bloqqi-compiler.git
    $ cd bloqqi-compiler/
    $ ant test
    <All tests should pass>
    $ ant jar
    $ java -jar bloqqi-compiler.jar examples/TwoCounters.dia
    Program OK

Generate C code and execute the program TwoCounters:

    $ java -jar bloqqi-compiler.jar examples/TwoCounters.dia --c --o=TwoCounters.c
    $ gcc -std=c99 TwoCounters.c examples/PrintFunction.c -o TwoCounters
    $ ./TwoCounters
    1
    2
    2
    4
    ...

The file `example/PrintFunction.c` contains C functions for printing different kinds of values.