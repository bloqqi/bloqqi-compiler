#!/bin/sh
cloc --by-file --force-lang=java,parser --force-lang=java,jadd --force-lang=java,jrag --force-lang=java,ast spec/grammar/StateMachine.ast spec/frontend/StateMachine.jrag spec/backend/PrettyPrintStateMachine.jadd spec/backend/GenerateCStateMachine.jadd spec/parser/StateMachine.parser 
