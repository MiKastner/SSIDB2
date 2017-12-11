import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllPairs {

    public static void main(String[] args) throws Exception {

        final double NANOSECONDS_PER_SECOND = 1000000000;
        final ThreadMXBean threadTimer= ManagementFactory.getThreadMXBean();
        final long start;

        // exit if no command line argument present or invalid filename
        if (args.length == 0 || !new File(args[0]).exists()) {
            System.out.println("Please enter a valid filename as argument!");
            return;
        }

        double threshold = Double.parseDouble(args[2]);
        ArrayList<ArrayList<Integer>> set = new ArrayList<>();

        // open file for reading
        // if the file is not found the method will throw an exception and exit
        BufferedReader b = new BufferedReader(new FileReader(args[0]));

        // read first line from file
        String line = b.readLine();

        // as long as there are lines in the file
        while (line != null) {

            //parse line -> make stream -> map integers -> make ArrayList
            ArrayList<Integer> item = Stream.of(line.split(" ")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toCollection(ArrayList::new));

            set.add(item);

            // read next line from file
            line = b.readLine();
        }

        // read weights
        HashMap<Integer, Double> weights = new HashMap<>();
        b = new BufferedReader(new FileReader(args[1]));

        // read first line from file
        line = b.readLine();

        // as long as there are lines in the file
        while (line != null) {
            String[] s = line.split(":");
            if (s.length != 2)
                line = b.readLine();
            else {
                weights.put(Integer.parseInt(s[0]), Double.parseDouble(s[1]));
                line = b.readLine();
            }
        }
        System.out.println(weights);


        // sort set
//        set.sort(Comparator.comparingInt(ArrayList::size));
//        for (ArrayList<Integer> i: set)
//            Collections.sort(i);

        start = threadTimer.getCurrentThreadCpuTime();

        int allpairs = allPairs(set, threshold);

        long now = threadTimer.getCurrentThreadCpuTime();

        double time= (now - start)/ NANOSECONDS_PER_SECOND;

        System.out.println(allpairs);
        System.out.println(time);
    }

    // indexing prefix length, pseudo code (pi_r)^I
    private static int eqo(ArrayList<Integer> r, ArrayList<Integer> s, double t) {
        return Math.min(s.size(), (int)Math.ceil(t/(t + 1) * (r.size() + s.size())));
    }

    // size lower bound on join partners for r
    private static double lb(ArrayList<Integer> r, double t){
        return r.size()*t;
    }

    // size upper bound on join partners for r
    private static int ub(ArrayList<Integer> r, double t){
        return (int)Math.floor(r.size()/t);
    }

    // probing prefix length, pseudo code pi_r
    private static int ppl(ArrayList<Integer> r, double t){
        return (r.size() - (int)Math.ceil(lb(r, t)) + 1);
    }

    // indexing prefix length, pseudo code (pi_r)^I
    private static int ipl(ArrayList<Integer> r, double t){
        return (r.size() - eqo(r, r, t) + 1);
    }

    //verify
    private static int verify(ArrayList<ArrayList<Integer>> R, ArrayList<Integer> r, HashMap<Integer, Integer> M, double t){
        int res = 0;
        for (Integer idx: M.keySet()) {
            ArrayList<Integer> s = R.get(idx);

            int eqoverlap = eqo(r, s, t);
            int pr = ppl(r, t);
            int ps = ipl(s, t);
            int overlap = M.get(idx);
			
            if (r.get(pr-1)<s.get(ps-1))
                ps = overlap;
            else
                pr = overlap;
			
            int maxr = r.size()-pr+overlap;
            int maxs = s.size()-ps+overlap;
			
            while (maxr >= eqoverlap && maxs >= eqoverlap && eqoverlap > overlap) {
                if (Objects.equals(r.get(pr), s.get(ps))) {
                    pr++;
                    ps++;
                    overlap++;
                } else {
                    if (r.get(pr).compareTo(s.get(ps))<0) {
                        pr++;
                        maxr--;
                    } else {
                        ps++;
                        maxs--;
                    }
                }
            }
            if(eqoverlap <= overlap)
                res++;
        }
        return res;
    }

    // allPairs
    private static int allPairs(ArrayList<ArrayList<Integer>> R, double t){
        int res = 0;
        HashMap<Integer, ArrayList<Integer>> I = new HashMap<>();   // inverted list, key tokens, value index of sets containing key
        HashMap<Integer, Integer> start = new HashMap<>();  // start Index for Inverted list instead removing
        for (int k = 0; k < R.size(); k++) {
            ArrayList<Integer> r = R.get(k);
            HashMap<Integer, Integer> M = new HashMap<>();    // dictionary for index of candidate sets, key index, value #intersections
            for (int p = 0; p < ppl(r, t); p++) {
                Integer key = r.get(p);
                if (I.get(key)!=null) {
                    for (int i = start.getOrDefault(key, 0); i < I.get(key).size(); i++) {
                        int idx = I.get(key).get(i);
                        ArrayList<Integer> s = R.get(idx);
                        if (R.get(idx).size() < lb(r, t)) {
                            if (start.containsKey(key))
                                start.put(key, start.get(key) + 1);
                            else
                                start.put(key, 1);
                        } else {
                            if (!M.containsKey(idx))
                                M.put(idx, 0);
                            M.put(idx, M.get(idx)+1);
                        }
                    }
                }
            }
            for (int p = 0; p < ipl(r, t); p++) {
                ArrayList<Integer> x = I.get(r.get(p));
                if (x == null)
                    x = new ArrayList<>();
                x.add(k);
                I.put(r.get(p), x);
            }
            if(M.size()>0)
                res += verify(R, r, M, t);
        }
        return res;
    }
}