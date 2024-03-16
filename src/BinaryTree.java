import java.util.*;

class BinaryTree {

    private int leafNodeID = 0;

    private Stack<Node> stackNode = new Stack<>();
    private Stack<Character> operator = new Stack<Character>();

    private Set<Character> input = new HashSet<Character>();
    private ArrayList<Character> op = new ArrayList<>();

    public Node generateTree(String regular) {

        Character[] ops = {'*', '|', '&'};
        op.addAll(Arrays.asList(ops));

        // Only inputs available
        Character ch[] = new Character[26 + 26];
        for (int i = 65; i <= 90; i++) {
            ch[i - 65] = (char) i;
            ch[i - 65 + 26] = (char) (i + 32);
        }
        Character integer[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        Character others[] = {'#', '\\', '=', '_', '.', '*', '/', '+', '-', ' ', '(', ')'};
        input.addAll(Arrays.asList(ch));
        input.addAll(Arrays.asList(integer));
        input.addAll(Arrays.asList(others));

        regular = AddConcat(regular);

        stackNode.clear();
        operator.clear();

        boolean isSymbol = false;

        for (int i = 0; i < regular.length(); i++) {

            if (regular.charAt(i) == '\\') {
                isSymbol = true;
                continue;
            }
            if (isSymbol || isInputCharacter(regular.charAt(i))) {
                if (isSymbol) {
                    pushStack("\\" + Character.toString(regular.charAt(i)));
                } else {
                    pushStack(Character.toString(regular.charAt(i)));
                }
                isSymbol = false;
            } else if (operator.isEmpty()) {
                operator.push(regular.charAt(i));

            } else if (regular.charAt(i) == '(') {
                operator.push(regular.charAt(i));

            } else if (regular.charAt(i) == ')') {
                while (operator.get(operator.size() - 1) != '(') {
                    doOperation();
                }

                operator.pop();

            } else {
                while (!operator.isEmpty()
                        && Priority(regular.charAt(i), operator.get(operator.size() - 1))) {
                    doOperation();
                }
                operator.push(regular.charAt(i));
            }
        }

        while (!operator.isEmpty()) {
            doOperation();
        }

        Node completeTree = stackNode.pop();
        return completeTree;
    }

    private boolean Priority(char first, Character second) {
        if (first == second) {
            return true;
        }
        if (first == '*') {
            return false;
        }
        if (second == '*') {
            return true;
        }
        if (first == '&') {
            return false;
        }
        if (second == '&') {
            return true;
        }
        if (first == '|') {
            return false;
        }
        return true;
    }

    private void doOperation() {
        if (this.operator.size() > 0) {
            char charAt = operator.pop();

            switch (charAt) {
                case ('|'):
                    union();
                    break;

                case ('&'):
                    concatenation();
                    break;

                case ('*'):
                    star();
                    break;

                default:
                    System.out.println(">>" + charAt);
                    System.out.println("Unkown Symbol !");
                    System.exit(1);
                    break;
            }
        }
    }

    private void star() {
        Node node = stackNode.pop();

        Node root = new Node("*");
        root.setLeft(node);
        root.setRight(null);
        node.setParent(root);

        stackNode.push(root);
    }

    private void concatenation() {
        Node node2 = stackNode.pop();
        Node node1 = stackNode.pop();

        Node root = new Node("&");
        root.setLeft(node1);
        root.setRight(node2);
        node1.setParent(root);
        node2.setParent(root);

        stackNode.push(root);
    }

    private void union() {
        Node node2 = stackNode.pop();
        Node node1 = stackNode.pop();

        Node root = new Node("|");
        root.setLeft(node1);
        root.setRight(node2);
        node1.setParent(root);
        node2.setParent(root);

        stackNode.push(root);
    }

    private void pushStack(String symbol) {
        Node node = new LeafNode(symbol, ++leafNodeID);
        node.setLeft(null);
        node.setRight(null);

        stackNode.push(node);
    }

    private String AddConcat(String regular) {
        String newRegular = new String("");

        for (int i = 0; i < regular.length() - 1; i++) {
            if (regular.charAt(i) == '\\' && isInputCharacter(regular.charAt(i + 1))) {
                newRegular += regular.charAt(i);
            } else if (regular.charAt(i) == '\\' && regular.charAt(i + 1) == '(') {
                newRegular += regular.charAt(i);
            } else if ((isInputCharacter(regular.charAt(i)) || (regular.charAt(i) == '(' && i > 0 && regular.charAt(i - 1) == '\\')) && isInputCharacter(regular.charAt(i + 1))) {
                newRegular += regular.charAt(i) + "&";

            } else if ((isInputCharacter(regular.charAt(i)) || (regular.charAt(i) == '(' && i > 0 && regular.charAt(i - 1) == '\\')) && regular.charAt(i + 1) == '(') {
                newRegular += regular.charAt(i) + "&";

            } else if (regular.charAt(i) == ')' && isInputCharacter(regular.charAt(i + 1))) {
                newRegular += regular.charAt(i) + "&";

            } else if (regular.charAt(i) == '*' && isInputCharacter(regular.charAt(i + 1))) {
                newRegular += regular.charAt(i) + "&";

            } else if (regular.charAt(i) == '*' && regular.charAt(i + 1) == '(') {
                newRegular += regular.charAt(i) + "&";

            } else if (regular.charAt(i) == ')' && regular.charAt(i + 1) == '(') {
                newRegular += regular.charAt(i) + "&";

            } else {
                newRegular += regular.charAt(i);
            }

        }
        newRegular += regular.charAt(regular.length() - 1);
        return newRegular;
    }

    private boolean isInputCharacter(char charAt) {

        if (op.contains(charAt)) {
            return false;
        }
        for (Character c : input) {
            if ((char) c == charAt && charAt != '(' && charAt != ')') {
                return true;
            }
        }
        return false;
    }

    public void printInorder(Node node) {
        if (node == null) {
            return;
        }
        printInorder(node.getLeft());
        System.out.print(node.getSymbol() + " ");
        printInorder(node.getRight());
    }

    public int getNumberOfLeafs() {
        return leafNodeID;
    }

}
