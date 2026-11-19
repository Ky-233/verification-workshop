import org.cprover.CProver;

// 3. A harness for the record Range.
//   ./bmc RangeCheck.size --unwind 1
public class RangeCheck {

    public static void size(int lo, int hi) {
        // TODO (3): build a valid Range and check the contract of size()
        CProver.assume(lo <= hi);

        Range r = new Range(lo, hi);
        long result = r.size();

        assert (long) result == (long) hi - lo + 1;
    }
}
