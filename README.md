# SimpleJava LL(1) Parser

An implementation of an LL(1) parser for a simplified subset of Java, developed as part of the Theory of Computing Science course (41080).

## Overview

This project implements a syntactic analyser that can parse and verify the syntactical correctness of programs written in SimpleJava, a restricted subset of the Java programming language. The parser utilises an LL(1) parsing approach with a parsing table and pushdown automaton.

## Features

- **LL(1) Parsing**: Implements left-to-right scanning with leftmost derivation using one token lookahead
- **Top-down Parsing**: Provides straightforward visualisation and tracking of inputs
- **No Backtracking**: Uses lookahead tokens for efficient parsing without backtracking
- **Parse Tree Generation**: Creates a detailed parse tree for valid SimpleJava programs
- **Syntax Error Detection**: Identifies and reports syntax errors in the input code

## Technical Details

### Components

1. **Lexical Analyser**
   - Tokenises input into a stream of symbols
   - Uses a definite finite automaton for token recognition

2. **Parsing Table**
   - Implemented as a HashMap for efficient lookup
   - Maps non-terminals and tokens to production rules
   - Eliminates the need for backtracking

3. **Pushdown Automaton**
   - Uses a stack-based memory system
   - Implemented using Java's ArrayDeque class
   - Manages parsing state and tree construction

### SimpleJava Language Support

The parser supports a subset of Java with the following restrictions:
- Limited to three variable types: int, char, and boolean
- Basic control structures and expressions
- No support for advanced features like objects or floating-point numbers

## Implementation Details

### Time Complexity

- Linear time complexity O(n) where n is the number of input tokens
- Constant time O(1) for each parsing table lookup
- No backtracking required

### Error Handling

- Basic syntax error detection and reporting
- Throws SyntaxException with descriptive error messages

## Limitations

1. **Limited Language Support**
   - Restricted to SimpleJava grammar subset
   - No support for advanced Java features

2. **No Semantic Analysis**
   - Focuses only on syntactic correctness
   - Does not perform type checking or variable scoping

## Course Information

- Course: 41080 Theory of Computing Science
- Term: Spring 2024

## License

This project is part of academic coursework and is subject to UTS policies regarding academic work.
