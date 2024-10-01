import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntacticAnalyser {

    private static final Map<Pair<Symbol, Token.TokenType>, List<Symbol>> parsingTable = new HashMap<>();

    static {
        parsingTable.put(new Pair<>(TreeNode.Label.prog, Token.TokenType.PUBLIC),
                Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal,
                        TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal,
                        TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal,
                        TreeNode.Label.terminal, TreeNode.Label.los, TreeNode.Label.terminal, TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.LBRACE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.WHILE), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.FOR), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.IF), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.ID), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.PRINT), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));
        parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.stat, TreeNode.Label.los));

        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.WHILE), Arrays.asList(TreeNode.Label.whilestat));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.FOR), Arrays.asList(TreeNode.Label.forstat));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.IF), Arrays.asList(TreeNode.Label.ifstat));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.ID), Arrays.asList(TreeNode.Label.assign, TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.decl, TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.PRINT), Arrays.asList(TreeNode.Label.print, TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.whilestat, Token.TokenType.WHILE), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.relexpr, TreeNode.Label.boolexpr, TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.los, TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.forstat, Token.TokenType.FOR), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.forstart, TreeNode.Label.terminal, TreeNode.Label.relexpr, TreeNode.Label.boolexpr, TreeNode.Label.terminal, TreeNode.Label.forarith, TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.los, TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.decl));
        parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.ID), Arrays.asList(TreeNode.Label.assign));
        parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));

        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.ID), Arrays.asList(TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.arithexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));

        parsingTable.put(new Pair<>(TreeNode.Label.ifstat, Token.TokenType.IF), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.relexpr, TreeNode.Label.boolexpr, TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.los, TreeNode.Label.terminal, TreeNode.Label.elseifstat));

        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.RBRACE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.ELSE), Arrays.asList(TreeNode.Label.elseorelseif, TreeNode.Label.terminal, TreeNode.Label.los, TreeNode.Label.terminal, TreeNode.Label.elseifstat));

        parsingTable.put(new Pair<>(TreeNode.Label.elseorelseif, Token.TokenType.ELSE), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.possif));

        parsingTable.put(new Pair<>(TreeNode.Label.possif, Token.TokenType.IF), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.possif, Token.TokenType.LBRACE), Arrays.asList(TreeNode.Label.epsilon));


        parsingTable.put(new Pair<>(TreeNode.Label.assign, Token.TokenType.ID), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.expr));

        parsingTable.put(new Pair<>(TreeNode.Label.decl, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.type, TreeNode.Label.terminal, TreeNode.Label.possassign));

        parsingTable.put(new Pair<>(TreeNode.Label.possassign, Token.TokenType.ASSIGN), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.expr));
        parsingTable.put(new Pair<>(TreeNode.Label.possassign, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));

        parsingTable.put(new Pair<>(TreeNode.Label.print, Token.TokenType.PRINT), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.printexpr, TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.type, Token.TokenType.TYPE), Arrays.asList(TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.TRUE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.FALSE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.SQUOTE), Arrays.asList(TreeNode.Label.charexpr));

        parsingTable.put(new Pair<>(TreeNode.Label.charexpr, Token.TokenType.SQUOTE), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.AND), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.OR), Arrays.asList(TreeNode.Label.boolop, TreeNode.Label.relexpr, TreeNode.Label.boolexpr));


        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.booleq));
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.booleq));
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.AND), Arrays.asList(TreeNode.Label.boollog));
        parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.OR), Arrays.asList(TreeNode.Label.boollog));

        parsingTable.put(new Pair<>(TreeNode.Label.booleq, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.booleq, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.boollog, Token.TokenType.AND), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.boollog, Token.TokenType.OR), Arrays.asList(TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.arithexpr, TreeNode.Label.relexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.arithexpr, TreeNode.Label.relexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.arithexpr, TreeNode.Label.relexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.TRUE), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.FALSE), Arrays.asList(TreeNode.Label.terminal));

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


        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.LT), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.LE), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.GT), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.GE), Arrays.asList(TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.term, TreeNode.Label.arithexprprime));

        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.PLUS), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.MINUS), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.term, TreeNode.Label.arithexprprime));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.LT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.LE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.GT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.GE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.AND), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.OR), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));

        parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.ID), Arrays.asList(TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.factor, TreeNode.Label.termprime));

        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.TIMES), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.DIVIDE), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.MOD), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.factor, TreeNode.Label.termprime));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.PLUS), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.MINUS), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.LT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.LE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.GT), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.GE), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.EQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.NEQUAL), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.AND), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.OR), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.SEMICOLON), Arrays.asList(TreeNode.Label.epsilon));
        parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.RPAREN), Arrays.asList(TreeNode.Label.epsilon));


        parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.arithexpr, TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.ID), Arrays.asList(TreeNode.Label.terminal));
        parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.terminal));

        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.LPAREN), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.ID), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.NUM), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.TRUE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.FALSE), Arrays.asList(TreeNode.Label.relexpr, TreeNode.Label.boolexpr));
        parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.DQUOTE), Arrays.asList(TreeNode.Label.terminal, TreeNode.Label.terminal, TreeNode.Label.terminal));

    }

    public static ParseTree parse(List<Token> tokens) throws SyntaxException {
        ParseTree parseTree = new ParseTree();
        Deque<Pair<Symbol, TreeNode>> stack = new ArrayDeque<>();

        stack.push(new Pair<>(TreeNode.Label.terminal, null)); // $
        stack.push(new Pair<>(TreeNode.Label.prog, new TreeNode(TreeNode.Label.prog, null))); // Start symbol

        int tokenIndex = 0;
        while (!stack.isEmpty()) {
            Pair<Symbol, TreeNode> top = stack.peek();
            Token currentToken = (tokenIndex < tokens.size()) ? tokens.get(tokenIndex) : null;

            if (top.fst() instanceof Token.TokenType) {
                // Top of stack is a terminal
                if (currentToken != null && top.fst() == currentToken.getType()) {
                    // Match!
                    stack.pop();
                    if (top.snd() != null)
                    {
                        top.snd().addChild(new TreeNode(TreeNode.Label.terminal, currentToken, top.snd()));
                    }
                    tokenIndex++;
                } else {
                    // Error: terminal mismatch
                    throw new SyntaxException("Syntax error: Expected " + top.fst() + ", found " + currentToken);
                }
            } else if (top.fst() instanceof TreeNode.Label) {
                // Top of stack is a non-terminal
                if (currentToken != null) {
                    List<Symbol> rule = parsingTable.get(new Pair<>(top.fst(), currentToken.getType()));
                    if (rule != null) {
                        // Found a rule to apply
                        stack.pop();
                        for (int i = rule.size() - 1; i >= 0; i--) {
                            Symbol symbol = rule.get(i);
                            TreeNode node = new TreeNode((TreeNode.Label)symbol, top.snd());
                            if (symbol.isVariable()) // Only add variable nodes to tree
                            {
                                top.snd().addChild(node);
                            }
                            stack.push(new Pair<>(symbol, node));
                        }
                    } else {
                        // Error: no rule found
                        throw new SyntaxException(
                                "Syntax error: Unexpected token " + currentToken + " for non-terminal " + top.fst());
                    }
                } else
                {
                    // Error: Non-terminal on top of the stack, no more token
                    throw new SyntaxException("Syntax error: Non-Terminal " + top.fst() + " read but no more tokens...");
                }
            }
        }

        if (tokenIndex != tokens.size())
        {
            throw new SyntaxException("Syntax error: Unused tokens at the end of the program...");
        }

        parseTree.setRoot(stack.pop().snd());
        return parseTree;
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
