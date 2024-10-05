import java.util.*;

public class SyntacticAnalyser {

    private static Map<Pair<Symbol, Token.TokenType>, List<Symbol>> parsingTable = new HashMap<>();

    static {
        initialiseParsingTable();
    }

    public static ParseTree parse(List<Token> tokens) throws SyntaxException {
        // Prepare the tokens list, adding an EOF token at the end
        List<Token> inputTokens = new ArrayList<>(tokens);
        inputTokens.add(new Token(Token.TokenType.EOF));  // Assuming EOF is defined in TokenType

        // Initialise stack with start symbol and root TreeNode
        Deque<Pair<Symbol, TreeNode>> stack = new ArrayDeque<>();

        // Create the root TreeNode
        TreeNode root = new TreeNode(TreeNode.Label.prog, null);

        // Push the starting symbol onto the stack
        stack.push(new Pair<>(TreeNode.Label.prog, root));

        int index = 0; // Index into input tokens

        while (!stack.isEmpty()) {
            Pair<Symbol, TreeNode> top = stack.pop();
            Symbol symbol = top.fst();
            TreeNode node = top.snd();

            Token currentToken = inputTokens.get(index);

            if (!symbol.isVariable()) { // Terminal
				if (symbol.equals(currentToken.getType())) {
					// Assign the token directly to the node
					node.setToken(currentToken);
					index++;
				} else {
					// Error: terminal symbol mismatch
					throw new SyntaxException("Expected " + symbol + " but found " + currentToken.getType());
				}
            } else { // Non-terminal
                Pair<Symbol, Token.TokenType> key = new Pair<>(symbol, currentToken.getType());
                List<Symbol> production = parsingTable.get(key);

                if (production != null) {
                    // For epsilon production, add epsilon node
                    if (production.size() == 1 && production.get(0) == TreeNode.Label.epsilon) {
                        TreeNode epsilonNode = new TreeNode(TreeNode.Label.epsilon, node);
                        node.addChild(epsilonNode);
                    } else {
                        // For each symbol in RHS, push onto stack in reverse order
                        ListIterator<Symbol> iterator = production.listIterator(production.size());
                        List<TreeNode> children = new ArrayList<>();

                        while (iterator.hasPrevious()) {
                            Symbol prodSymbol = iterator.previous();
                            TreeNode childNode;

                            if (!prodSymbol.isVariable()) {
                                childNode = new TreeNode(TreeNode.Label.terminal, node);
                            } else {
                                childNode = new TreeNode((TreeNode.Label) prodSymbol, node);
                            }

                            stack.push(new Pair<>(prodSymbol, childNode));
                            children.add(0, childNode); // Add to the beginning of the list
                        }

                        // Add the children to the node
                        for (TreeNode child : children) {
                            node.addChild(child);
                        }
                    }
                } else {
                    // Error: no production found in parsing table
                    throw new SyntaxException("No production found for " + symbol + " with input " + currentToken.getType());
                }
            }
        }

        // If all tokens have been consumed and stack is empty, parsing is successful
        // Optionally check if all tokens are consumed
        if (index != inputTokens.size() - 1) { // except EOF
            throw new SyntaxException("Extra tokens at the end");
        }

        // Return ParseTree with root node
        return new ParseTree(root);
    }

    private static void initialiseParsingTable() {
        // Initialise the parsing table entries based on the provided parsing table

        // Production 1: PROG -> public class ID { public static void main ( String[] args ) { LOS } }
        parsingTable.put(new Pair<>(TreeNode.Label.prog, Token.TokenType.PUBLIC), Arrays.asList(
                Token.TokenType.PUBLIC,
                Token.TokenType.CLASS,
                Token.TokenType.ID,
                Token.TokenType.LBRACE,
                Token.TokenType.PUBLIC,
                Token.TokenType.STATIC,
                Token.TokenType.VOID,
                Token.TokenType.MAIN,
                Token.TokenType.LPAREN,
                Token.TokenType.STRINGARR,
                Token.TokenType.ARGS,
                Token.TokenType.RPAREN,
                Token.TokenType.LBRACE,
                TreeNode.Label.los,
                Token.TokenType.RBRACE,
                Token.TokenType.RBRACE
        ));

        // Productions for LOS
        // First set for LOS: { WHILE, FOR, IF, ID, TYPE, PRINT, SEMICOLON }
        List<Token.TokenType> losFirstSet = Arrays.asList(
                Token.TokenType.WHILE,
                Token.TokenType.FOR,
                Token.TokenType.IF,
                Token.TokenType.ID,
                Token.TokenType.TYPE,
                Token.TokenType.PRINT,
                Token.TokenType.SEMICOLON
        );

        for (Token.TokenType tt : losFirstSet) {
            parsingTable.put(new Pair<>(TreeNode.Label.los, tt), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        }

        // Follow set for LOS: { RBRACE }
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.RBRACE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.EOF), Arrays.asList(TreeNode.Label.epsilon));

        // Productions for STAT
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.WHILE), Arrays.asList(TreeNode.Label.whilestat));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.FOR), Arrays.asList(TreeNode.Label.forstat));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.IF), Arrays.asList(TreeNode.Label.ifstat));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.ID), Arrays.asList(TreeNode.Label.assign, Token.TokenType.SEMICOLON));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.decl, Token.TokenType.SEMICOLON));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.PRINT), Arrays.asList(TreeNode.Label.print, Token.TokenType.SEMICOLON));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.SEMICOLON), Arrays.asList(Token.TokenType.SEMICOLON));

        // WHILE -> while ( REL_EXPR BOOL_EXPR ) { LOS }
        parsingTable.put(new Pair<>(TreeNode.Label.whilestat, Token.TokenType.WHILE), Arrays.asList(
                Token.TokenType.WHILE,
                Token.TokenType.LPAREN,
                TreeNode.Label.relexpr,
                TreeNode.Label.boolexpr,
                Token.TokenType.RPAREN,
                Token.TokenType.LBRACE,
                TreeNode.Label.los,
                Token.TokenType.RBRACE
        ));

        // FOR -> for ( FOR_START ; REL_EXPR BOOL_EXPR ; FOR_ARITH ) { LOS }
        parsingTable.put(new Pair<>(TreeNode.Label.forstat, Token.TokenType.FOR), Arrays.asList(
                Token.TokenType.FOR,
                Token.TokenType.LPAREN,
                TreeNode.Label.forstart,
                Token.TokenType.SEMICOLON,
                TreeNode.Label.relexpr,
                TreeNode.Label.boolexpr,
                Token.TokenType.SEMICOLON,
                TreeNode.Label.forarith,
                Token.TokenType.RPAREN,
                Token.TokenType.LBRACE,
                TreeNode.Label.los,
                Token.TokenType.RBRACE
        ));

        // FOR_START -> DECL | ASSIGN | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.decl));
        parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.ID), Arrays.asList(TreeNode.Label.assign));
        parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));

        // FOR_ARITH -> ARITH_EXPR | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.ID), Arrays.asList(TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));


        // IF -> if ( REL_EXPR BOOL_EXPR ) { LOS } ELSE_IF
        parsingTable.put(new Pair<>(TreeNode.Label.ifstat, Token.TokenType.IF), Arrays.asList(
                Token.TokenType.IF,
                Token.TokenType.LPAREN,
                TreeNode.Label.relexpr,
                TreeNode.Label.boolexpr,
                Token.TokenType.RPAREN,
                Token.TokenType.LBRACE,
                TreeNode.Label.los,
                Token.TokenType.RBRACE,
                TreeNode.Label.elseifstat
        ));

        // ELSE_IF -> ELSE?IF { LOS } ELSE_IF | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.ELSE), Arrays.asList(
                TreeNode.Label.elseorelseif,
                Token.TokenType.LBRACE,
                TreeNode.Label.los,
                Token.TokenType.RBRACE,
                TreeNode.Label.elseifstat
        ));

        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.WHILE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.FOR), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.IF), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.ID), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.PRINT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.RBRACE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.EOF), Arrays.asList(TreeNode.Label.epsilon));

        // ELSE?IF -> else POSS_IF
        parsingTable.put(new Pair<>(TreeNode.Label.elseorelseif, Token.TokenType.ELSE), Arrays.asList(
                Token.TokenType.ELSE,
                TreeNode.Label.possif
        ));

        // POSS_IF -> if ( REL_EXPR BOOL_EXPR )  | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.possif, Token.TokenType.IF), Arrays.asList(
                Token.TokenType.IF,
                Token.TokenType.LPAREN,
                TreeNode.Label.relexpr,
                TreeNode.Label.boolexpr,
                Token.TokenType.RPAREN
        ));

        parsingTable.put(new Pair<>(TreeNode.Label.possif, Token.TokenType.LBRACE), Arrays.asList(TreeNode.Label.epsilon));

        // ASSIGN -> ID = EXPR
        parsingTable.put(new Pair<>(TreeNode.Label.assign, Token.TokenType.ID), Arrays.asList(
                Token.TokenType.ID,
                Token.TokenType.ASSIGN,
                TreeNode.Label.expr
        ));

        // DECL -> TYPE ID POSS_ASSIGN
        parsingTable.put(new Pair<>(TreeNode.Label.decl, Token.TokenType.TYPE), Arrays.asList(
                TreeNode.Label.type,
                Token.TokenType.ID,
                TreeNode.Label.possassign
        ));

        // POSS_ASSIGN -> = EXPR | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.possassign, Token.TokenType.ASSIGN), Arrays.asList(
                Token.TokenType.ASSIGN,
                TreeNode.Label.expr
        ));

        parsingTable.put(new Pair<>(TreeNode.Label.possassign, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));

        // PRINT -> System.out.println ( PRINT_EXPR )
        parsingTable.put(new Pair<>(TreeNode.Label.print, Token.TokenType.PRINT), Arrays.asList(
                Token.TokenType.PRINT,
                Token.TokenType.LPAREN,
                TreeNode.Label.printexpr,
                Token.TokenType.RPAREN
        ));

        // TYPE -> int | boolean | char
        parsingTable.put(new Pair<>(TreeNode.Label.type, Token.TokenType.TYPE), Arrays.asList(Token.TokenType.TYPE));


        // EXPR -> REL_EXPR BOOL_EXPR | CHAR_EXPR
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.TRUE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.FALSE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.SQUOTE), Arrays.asList(TreeNode.Label.charexpr));


        // CHAR_EXPR -> ' CHAR '
        parsingTable.put(new Pair<>(TreeNode.Label.charexpr, Token.TokenType.SQUOTE), Arrays.asList(
                Token.TokenType.SQUOTE,
                Token.TokenType.CHARLIT,
                Token.TokenType.SQUOTE
        ));


        // BOOL_EXPR -> BOOL_OP REL_EXPR BOOL_EXPR | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.AND), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.OR), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));


        // BOOL_OP -> BOOL_EQ | BOOL_LOG
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.booleq));
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.booleq));
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.AND), Arrays.asList(TreeNode.Label.boollog));
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.OR), Arrays.asList(TreeNode.Label.boollog));

        // BOOL_EQ -> == | !=
        parsingTable.put(new Pair<>(TreeNode.Label.booleq, Token.TokenType.EQUAL), Arrays.asList(Token.TokenType.EQUAL));
        parsingTable.put(new Pair<>(TreeNode.Label.booleq, Token.TokenType.NEQUAL), Arrays.asList(Token.TokenType.NEQUAL));


        // BOOL_LOG -> && | ||
        parsingTable.put(new Pair<>(TreeNode.Label.boollog, Token.TokenType.AND), Arrays.asList(Token.TokenType.AND));
        parsingTable.put(new Pair<>(TreeNode.Label.boollog, Token.TokenType.OR), Arrays.asList(Token.TokenType.OR));



        // REL_EXPR -> ARITH_EXPR REL_EXPR' | TRUE | FALSE
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.arithexpr, TreeNode.Label.relexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.arithexpr, TreeNode.Label.relexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.arithexpr, TreeNode.Label.relexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.TRUE), Arrays.asList(Token.TokenType.TRUE));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.FALSE), Arrays.asList(Token.TokenType.FALSE));



        // REL_EXPR' -> REL_OP ARITH_EXPR | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.LT), Arrays.asList(TreeNode.Label.relop, TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.LE), Arrays.asList(TreeNode.Label.relop, TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.GT), Arrays.asList(TreeNode.Label.relop, TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.GE), Arrays.asList(TreeNode.Label.relop, TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.AND), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.OR), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));

        // REL_OP -> < | <= | > | >=
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.LT), Arrays.asList(Token.TokenType.LT));
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.LE), Arrays.asList(Token.TokenType.LE));
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.GT), Arrays.asList(Token.TokenType.GT));
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.GE), Arrays.asList(Token.TokenType.GE));


        // ARITH_EXPR -> TERM ARITH_EXPR'
        parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.term, TreeNode.Label.arithexprprime));


        // ARITH_EXPR' -> + TERM ARITH_EXPR' | - TERM ARITH_EXPR' | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.PLUS), Arrays.asList(Token.TokenType.PLUS, TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.MINUS), Arrays.asList(Token.TokenType.MINUS, TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.LT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.LE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.GT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.GE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.AND), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.OR), Arrays.asList(TreeNode.Label.epsilon));

        // TERM -> FACTOR TERM'
        parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.ID), Arrays.asList(TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.factor, TreeNode.Label.termprime));


        // TERM' -> * FACTOR TERM' | / FACTOR TERM' | % FACTOR TERM' | epsilon
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.TIMES), Arrays.asList(Token.TokenType.TIMES, TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.DIVIDE), Arrays.asList(Token.TokenType.DIVIDE, TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.MOD), Arrays.asList(Token.TokenType.MOD, TreeNode.Label.factor, TreeNode.Label.termprime));

        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.PLUS), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.MINUS), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.LT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.LE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.GT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.GE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.AND), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.OR), Arrays.asList(TreeNode.Label.epsilon));



        // FACTOR -> ( ARITH_EXPR ) | ID | NUM
        parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.LPAREN), Arrays.asList(
                Token.TokenType.LPAREN,
                TreeNode.Label.arithexpr,
                Token.TokenType.RPAREN
        ));

        parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.ID), Arrays.asList(Token.TokenType.ID));
        parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.NUM), Arrays.asList(Token.TokenType.NUM));



        // PRINT_EXPR -> REL_EXPR BOOL_EXPR | " STRINGLIT "
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.TRUE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.FALSE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.DQUOTE), Arrays.asList(
                Token.TokenType.DQUOTE,
                Token.TokenType.STRINGLIT,
                Token.TokenType.DQUOTE
        ));
    }
}

// The following class may be helpful.

class Pair<A, B> {
    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A fst() {
        return a;
    }

    public B snd() {
        return b;
    }

    @Override
    public int hashCode() {
        return 3 * a.hashCode() + 7 * b.hashCode();
    }

    @Override
    public String toString() {
        return "{" + a + ", " + b + "}";
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof Pair<?, ?>)) {
            Pair<?, ?> other = (Pair<?, ?>) o;
            return other.fst().equals(a) && other.snd().equals(b);
        }

        return false;
    }

}
