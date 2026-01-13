import java.util.*;

public class SyntaxAnalyzerSunda {

    // ===== KAMUS KATA =====
    static Set<String> pronomina = Set.of("abdi", "maneh", "anjeunna", "arurang");
    static Set<String> nomina = Set.of("sagara", "lauk", "sangu", "cai", "acuk", "tuyul");
    static Set<String> verba = Set.of("ngaput", "indit", "netepan", "nguseup", "madog");
    static Set<String> adjektiva = Set.of("gede", "leutik", "alus", "panas", "cingceng");
    static Set<String> preposisi = Set.of("di", "ka", "ti");
    static Set<String> adverbia = Set.of("ayeuna", "enjing", "wengi");

    static List<String> tokens;
    static int index;

    // ===== NODE PARSE TREE =====
    static class Node {
        String label;
        List<Node> children = new ArrayList<>();

        Node(String label) {
            this.label = label;
        }

        void add(Node child) {
            children.add(child);
        }
    }

    static Node root;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("================================");
        System.out.println(" SYNTAX ANALYZER BAHASA SUNDA ");
        System.out.println("================================");

        System.out.print("Masukkan kalimat: ");
        tokens = Arrays.asList(sc.nextLine().toLowerCase().trim().split("\\s+"));
        index = 0;

        if (!cekLexikal()) {
            System.out.println("❌ Kesalahan leksikal.");
            return;
        }

        root = parseS();

        if (root != null && index == tokens.size()) {
            System.out.println("\nSTATUS: VALID ✅");
            System.out.println("\n[ VISUALISASI PARSE TREE ]");
            printTree(root, "", true);
        } else {
            System.out.println("\nSTATUS: TIDAK VALID ❌");
        }
    }

    // ===== CEK LEKSIKAL =====
    static boolean cekLexikal() {
        for (String w : tokens) {
            if (!(pronomina.contains(w) || nomina.contains(w) || verba.contains(w)
                    || adjektiva.contains(w) || preposisi.contains(w) || adverbia.contains(w))) {
                System.out.println("Kata tidak dikenal: " + w);
                return false;
            }
        }
        return true;
    }

    // ===== S → NP VP [NP] [PP] [AdvP] =====
    static Node parseS() {
        Node S = new Node("S");

        Node NP = parseNP();
        if (NP == null) return null;
        S.add(NP);

        Node VP = parseVP();
        if (VP == null) return null;
        S.add(VP);

        Node obj = parseNP();
        if (obj != null) S.add(obj);

        Node pp = parsePP();
        if (pp != null) S.add(pp);

        Node adv = parseAdvP();
        if (adv != null) S.add(adv);

        return S;
    }

    // ===== NP → Pronomina | Nomina | Nomina Adjektiva =====
    static Node parseNP() {
        if (index >= tokens.size()) return null;
        String w = tokens.get(index);

        Node NP = new Node("NP");

        if (pronomina.contains(w)) {
            Node pro = new Node("Pronomina");
            pro.add(new Node(w));
            NP.add(pro);
            index++;
            return NP;
        }

        if (nomina.contains(w)) {
            Node nom = new Node("Nomina");
            nom.add(new Node(w));
            NP.add(nom);
            index++;

            if (index < tokens.size() && adjektiva.contains(tokens.get(index))) {
                Node adj = new Node("Adjektiva");
                adj.add(new Node(tokens.get(index)));
                NP.add(adj);
                index++;
            }
            return NP;
        }
        return null;
    }

    // ===== VP → Verba | Verba Adverbia =====
    static Node parseVP() {
        if (index >= tokens.size()) return null;
        String w = tokens.get(index);

        if (!verba.contains(w)) return null;

        Node VP = new Node("VP");

        Node v = new Node("Verba");
        v.add(new Node(w));
        VP.add(v);
        index++;

        if (index < tokens.size() && adverbia.contains(tokens.get(index))) {
            Node adv = new Node("Adverbia");
            adv.add(new Node(tokens.get(index)));
            VP.add(adv);
            index++;
        }
        return VP;
    }

    // ===== PP → Preposisi Nomina =====
    static Node parsePP() {
        if (index + 1 >= tokens.size()) return null;

        String p = tokens.get(index);
        String n = tokens.get(index + 1);

        if (preposisi.contains(p) && nomina.contains(n)) {
            Node PP = new Node("PP");

            Node prep = new Node("Preposisi");
            prep.add(new Node(p));

            Node nom = new Node("Nomina");
            nom.add(new Node(n));

            PP.add(prep);
            PP.add(nom);

            index += 2;
            return PP;
        }
        return null;
    }

    // ===== AdvP → Adverbia =====
    static Node parseAdvP() {
        if (index >= tokens.size()) return null;

        String w = tokens.get(index);
        if (adverbia.contains(w)) {
            Node AdvP = new Node("AdvP");
            Node adv = new Node("Adverbia");
            adv.add(new Node(w));
            AdvP.add(adv);
            index++;
            return AdvP;
        }
        return null;
    }

    // ===== CETAK PARSE TREE =====
    static void printTree(Node node, String indent, boolean last) {
        System.out.println(indent + (last ? "└── " : "├── ") + node.label);
        indent += last ? "    " : "│   ";

        for (int i = 0; i < node.children.size(); i++) {
            printTree(node.children.get(i), indent, i == node.children.size() - 1);
        }
    }
}
