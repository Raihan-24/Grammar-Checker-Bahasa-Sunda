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
    static int step = 1;

    static List<String[]> derivasi = new ArrayList<>();

    static String subjek = "-";
    static String predikat = "-";
    static String objek = "-";
    static String prep = "-";
    static String keterangan = "-";

    static String subjekKategori = "-";
    static String objekKategori = "-";
    static String ketKategori = "-";

    static String adjSubjek = "-";
    static String adjObjek = "-";

    static String aturanCFG = "-";

    // ===== MAIN =====
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("==============================");
        System.out.println(" SYNTAX ANALYZER BAHASA SUNDA ");
        System.out.println("==============================");

        System.out.print("\nMasukkan kalimat: ");
        String input = sc.nextLine().toLowerCase().trim();

        tokens = Arrays.asList(input.split("\\s+"));
        index = 0;
        step = 1;
        derivasi.clear();

        if (!cekLexikal()) {
            System.out.println("\n - STATUS : TIDAK VALID ❌");
            System.out.println(" - ANALISIS :");
            System.out.println(" Terdapat kata yang tidak dikenali.");
            return;
        }

        boolean valid = parseS();
        tampilkanTabel();

        if (valid && index == tokens.size()) {
            tentukanAturanCFG();
            System.out.println("\n - STATUS : VALID ✅");
            System.out.println(" - ATURAN CFG : " + aturanCFG);
            tampilkanParseTree();
        } else {
            System.out.println("\n - STATUS : TIDAK VALID ❌");
            tampilkanPenjelasanTidakValid();
        }

        sc.close();
    }

    // ===== CEK LEKSIKAL =====
    static boolean cekLexikal() {
        for (String w : tokens) {
            if (!(pronomina.contains(w) || nomina.contains(w) || verba.contains(w)
                    || adjektiva.contains(w) || preposisi.contains(w) || adverbia.contains(w))) {
                return false;
            }
        }
        return true;
    }

    // ===== PARSER =====
    static boolean parseS() {
        catat("S → NP VP [NP] [PP] [AdvP]", sisa());

        if (!parseNP(true)) return false;
        if (!parseVP()) return false;

        parseNP(false);
        parsePP();
        parseAdvP();

        return true;
    }

    static boolean parseNP(boolean isSubjek) {
        if (index >= tokens.size()) return false;
        String w = tokens.get(index);

        if (pronomina.contains(w)) {
            catat("NP → Pronomina", sisa());
            if (isSubjek) {
                subjek = w;
                subjekKategori = "Pronomina";
            } else {
                objek = w;
                objekKategori = "Pronomina";
            }
            index++;
            return true;
        }

        if (nomina.contains(w)) {
            catat("NP → Nomina", sisa());
            if (isSubjek) {
                subjek = w;
                subjekKategori = "Nomina";
            } else {
                objek = w;
                objekKategori = "Nomina";
            }
            index++;

            if (index < tokens.size() && adjektiva.contains(tokens.get(index))) {
                catat("Nomina → Adjektiva", sisa());
                if (isSubjek) adjSubjek = tokens.get(index);
                else adjObjek = tokens.get(index);
                index++;
            }
            return true;
        }
        return false;
    }

    static boolean parseVP() {
        if (index >= tokens.size()) return false;
        String w = tokens.get(index);

        if (verba.contains(w)) {
            catat("VP → Verba", sisa());
            predikat = w;
            index++;
            return true;
        }
        return false;
    }

    static boolean parsePP() {
        if (index + 1 >= tokens.size()) return false;

        String w = tokens.get(index);
        String n = tokens.get(index + 1);

        if (preposisi.contains(w) && nomina.contains(n)) {
            catat("PP → Preposisi Nomina", sisa());
            prep = w;
            keterangan = n;
            ketKategori = "Nomina";
            index += 2;
            return true;
        }
        return false;
    }

    static boolean parseAdvP() {
        if (index >= tokens.size()) return false;
        String w = tokens.get(index);

        if (adverbia.contains(w)) {
            catat("AdvP → Adverbia", sisa());
            keterangan = w;
            ketKategori = "Adverbia";
            index++;
            return true;
        }
        return false;
    }

    static void tentukanAturanCFG() {
        boolean adaObjek = !objek.equals("-");
        boolean adaPP = !prep.equals("-");
        boolean adaAdv = adverbia.contains(keterangan);

        if (adaObjek && adaPP) aturanCFG = "S → NP VP NP PP";
        else if (adaObjek && adaAdv) aturanCFG = "S → NP VP NP AdvP";
        else if (adaObjek) aturanCFG = "S → NP VP NP";
        else if (adaAdv) aturanCFG = "S → NP VP AdvP";
        else aturanCFG = "S → NP VP";
    }

    static void catat(String aturan, String hasil) {
        derivasi.add(new String[]{String.valueOf(step++), aturan, hasil});
    }

    static String sisa() {
        if (index >= tokens.size()) return "-";
        return String.join(" ", tokens.subList(index, tokens.size()));
    }

    static void tampilkanTabel() {
        System.out.println("\nLangkah | Aturan Produksi | Sisa Input");
        System.out.println("---------------------------------------------------------");
        for (String[] r : derivasi) {
            System.out.printf("%-7s | %-30s | %s\n", r[0], r[1], r[2]);
        }
    }

    // ===== PARSE TREE (HANYA BAGIAN INI YANG DISESUAIKAN) =====
    static void tampilkanParseTree() {
        System.out.println("\n[ VISUALISASI PARSE TREE ]");
        System.out.println("S");

        System.out.println("├── NP");
        System.out.println("│   └── " + subjekKategori);
        System.out.println("│       └── " + subjek);
        if (!adjSubjek.equals("-")) {
            System.out.println("│           └── Adjektiva");
            System.out.println("│               └── " + adjSubjek);
        }

        System.out.println("├── VP");
        System.out.println("│   └── Verba");
        System.out.println("│       └── " + predikat);

        // 👉 PERBAIKAN KHUSUS UNTUK: arurang indit ayeuna
        if ("Adverbia".equals(ketKategori) && objek.equals("-") && prep.equals("-")) {
            System.out.println("│           └── Adverbia");
            System.out.println("│               └── " + keterangan);
        }

        if (!objek.equals("-")) {
            System.out.println("├── NP");
            System.out.println("│   └── " + objekKategori);
            System.out.println("│       └── " + objek);
            if (!adjObjek.equals("-")) {
                System.out.println("│           └── Adjektiva");
                System.out.println("│               └── " + adjObjek);
            }
        }

        if (!prep.equals("-")) {
            System.out.println("└── Keterangan");
            System.out.println("    ├── Preposisi");
            System.out.println("    │   └── " + prep);
            System.out.println("    └── Nomina");
            System.out.println("        └── " + keterangan);
        }
    }

    // ===== ANALISIS TIDAK VALID (TIDAK DIUBAH) =====
    static void tampilkanPenjelasanTidakValid() {
        System.out.println(" - ANALISIS :");

        List<String> pola = new ArrayList<>();
        for (String t : tokens) {
            pola.add(kategori(t));
        }

        System.out.println(" Pola terbaca : " + String.join(" ", pola));

        if (!tokens.isEmpty() &&
                !pronomina.contains(tokens.get(0)) &&
                !nomina.contains(tokens.get(0))) {
            System.out.println(" Masalah : Kalimat tidak diawali subjek");
            System.out.println(" Aturan : S → NP VP [NP] [PP] [AdvP]");
            return;
        }

        boolean adaVerba = false;
        for (String t : tokens) {
            if (verba.contains(t)) {
                adaVerba = true;
                break;
            }
        }

        if (!adaVerba) {
            System.out.println(" Masalah : Predikat tidak ditemukan");
            System.out.println(" Aturan : VP → Verba");
            return;
        }

        System.out.println(" Masalah : Urutan frasa tidak sesuai CFG");
        System.out.println(" Aturan : S → NP VP [NP] [PP] [AdvP]");
    }

    static String kategori(String w) {
        if (pronomina.contains(w)) return "Pronomina";
        if (nomina.contains(w)) return "Nomina";
        if (verba.contains(w)) return "Verba";
        if (adjektiva.contains(w)) return "Adjektiva";
        if (preposisi.contains(w)) return "Preposisi";
        if (adverbia.contains(w)) return "Adverbia";
        return "TidakDikenal";
    }
}
