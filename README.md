# The Bloqqi compiler

To test and run the Bloqqi compiler:

    $ git clone git@bitbucket.org:bloqqi/bloqqi-compiler.git
    $ cd bloqqi-compiler/
    $ ant test
    <All tests should pass>
    $ ant jar
    $ java -jar bloqqi-compiler.jar examples/NestedState.dia
    Program OK