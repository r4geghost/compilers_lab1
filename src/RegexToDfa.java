import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegexToDfa {

    private static Set<Integer>[] followPos;
    private static Node root;
    private static Set<State> DStates;

    private static Set<String> alphabet;

    // Группа неконечных состояний
    private static Set<State> nonAcceptableStates  = new HashSet<>();
    // Группа конечных состояний
    private static Set<State> acceptableStates  = new HashSet<>();
    private static HashMap<Integer, String> symbNum;

    public static void main(String[] args) {
        initialize();
    }

    public static void initialize() {
        Scanner in = new Scanner(System.in);

        DStates = new HashSet<>();
        alphabet = new HashSet<>();

        String regex = getRegex(in);
        getSymbols(regex);

        SyntaxTree st = new SyntaxTree(regex);
        root = st.getRoot(); // корень синтаксического дерева
        followPos = st.getFollowPos(); // значение followpos синтаксического дерева

        // создание ДКА по регулярному выражению (q0 - начальное состояние)
        State q0 = createDFA();
        DfaTraversal dfaTraversal = new DfaTraversal(q0, alphabet);

        DStates.forEach(state -> {
            if (!state.getName().isEmpty()) {
                if (state.getIsAcceptable()) {
                    acceptableStates.add(state);
                } else {
                    nonAcceptableStates.add(state);
                }
            }
        });

        System.out.println("Исходный алфавит: "+ alphabet);

        System.out.println("Группа конечных состояний:");
        acceptableStates.forEach(state -> {
            System.out.println(state.getName());
        });
        System.out.println("Группа неконечных состояний:");
        nonAcceptableStates.forEach(state -> {
            System.out.println(state.getName());
        });

//        minimizeDfaHopcroftAlgorithm();

        // Проверка строки на соответствие регулярному выражению
        String str = getStr(in);
        boolean acc = false;
        for (char c : str.toCharArray()) {
            if (dfaTraversal.setCharacter(c)) {
                acc = dfaTraversal.traverse();
            } else {
                System.out.println("WRONG CHARACTER!");
                System.exit(0);
            }
        }
        if (acc) {
            System.out.println("this string is acceptable by the regex!");
        } else {
            System.out.println("this string is NOT acceptable by the regex!");
        }
        in.close();
    }

    // АЛГОРИТМ ХОПКРОФТА
    private static void minimizeDfaHopcroftAlgorithm() {
        Set<State> startStates = nonAcceptableStates;
        Set<State> finishStates = acceptableStates;
        // добавляем их в сет сетов (current) и проходимся по нему в цикле
        Set<Set<State>> current = Stream.of(startStates, finishStates)
                .collect(Collectors.toCollection(HashSet::new));
        // создаем новый сет сетов
        Set<Set<State>> P = new HashSet<>();

        System.out.println("Исходные состояния:");
        current.forEach(set -> {
            System.out.println("Initial set:" + set + ", items: ");
            set.forEach(state -> System.out.println(state.getName()));
        });

        while (!P.containsAll(current)) {
            P = new HashSet<>(current);
            current.clear(); // очищаем текущий набор
            for (Set<State> states : P) {
                current.addAll(splitStates(states)); // добавляем новые состояния
                current.forEach(set -> {
                    if (!set.isEmpty()) {
                        System.out.println("set:" + set + ", items: ");
                        set.forEach(state -> System.out.println(state.getName()));
                    }
                });
                System.out.println("---");
            }
        }

        System.out.println("Итоговые состояния:");
        current.forEach(set -> set.forEach(state -> System.out.println(state.getName())));
    }

    private static Set<Set<State>> splitStates(Set<State> states) {
        Set<State> newStates = new HashSet<>();
        for (String c : alphabet) {
            for (State s : states) {
                // найти состояние, в которое мы перейдем из s по символу c
                // если оно находится в данной группе, то добавляем его в новый набор
                if (states.contains(s.getNextStateBySymbol(c))) {
                    newStates.add(s);
                }
            }
        }
        // удаляем все элементы из набора states, которые есть в newStates
        states.removeAll(newStates);
        return Stream.of(states, newStates).collect(Collectors.toCollection(HashSet::new));
    }

    private static String getRegex(Scanner in) {
        System.out.print("Enter a regex: ");
        String regex = in.nextLine();
        return regex+"#";
    }

    private static void getSymbols(String regex) {
        Set<Character> op = new HashSet<>();
        Character[] ch = {'(', ')', '*', '|', '&', '.', '\\', '[', ']', '+'};
        op.addAll(Arrays.asList(ch));

        alphabet = new HashSet<>();
        symbNum = new HashMap<>();
        int num = 1;
        for (int i = 0; i < regex.length(); i++) {
            char charAt = regex.charAt(i);
            if (op.contains(charAt)) {
                if (i - 1 >= 0 && regex.charAt(i - 1) == '\\') {
                    alphabet.add("\\" + charAt);
                    symbNum.put(num++, "\\" + charAt);
                }
            } else {
                alphabet.add("" + charAt);
                symbNum.put(num++, "" + charAt);
            }
        }
    }

    private static State createDFA() {
        int id = 0;
        Set<Integer> firstpos_n0 = root.getFirstPos();

        State q0 = new State(id++);
        q0.addAllToName(firstpos_n0);
        if (q0.getName().contains(followPos.length)) {
            q0.setAccept();
        }
        DStates.clear();
        DStates.add(q0);

        while (true) {
            boolean exit = true;
            State s = null;
            for (State state : DStates) {
                if (!state.getIsMarked()) {
                    exit = false;
                    s = state;
                }
            }
            if (exit) {
                break;
            }

            if (s.getIsMarked()) {
                continue;
            }
            s.setIsMarked(true);
            Set<Integer> name = s.getName();
            for (String a : alphabet) {
                Set<Integer> U = new HashSet<>();
                for (int p : name) {
                    if (symbNum.get(p).equals(a)) {
                        U.addAll(followPos[p - 1]);
                    }
                }
                boolean flag = false;
                State tmp = null;
                for (State state : DStates) {
                    if (state.getName().equals(U)) {
                        tmp = state;
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    State q = new State(id++);
                    q.addAllToName(U);
                    if (U.contains(followPos.length)) {
                        q.setAccept();
                    }
                    DStates.add(q);
                    tmp = q;
                }
                // добавляем переход
                s.addMove(a, tmp);
            }
        }

        return q0;
    }

    private static String getStr(Scanner in) {
        System.out.print("\nEnter a string: ");
        String str;
        str = in.nextLine();
        return str;
    }

}
