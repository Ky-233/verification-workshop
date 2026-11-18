# Homework: answers

Name(s): Siyuan LIAO 999019805

## OpenJML

### 1. Absolute

Q: What does OpenJML complain about before you write a contract, and for which input?

OpenJML reports an ArithmeticOperationRange verification failure at the negation operation -n. The problematic input is Integer.MIN_VALUE, whose negation exceeds the maximum representable Java int value.

Q: Which `requires` did you need, and why? Could you avoid it by changing the `ensures`?

The required precondition is n != Integer.MIN_VALUE, because negating this value causes integer overflow.
NO. Because the verification failure originates from an input value that causes integer overflow when negated, the correction must restrict the input domain. Weakening the postcondition does not make the operation on that input safe.

### 2. SumX

Q: Which proof obligations fail? Is the program wrong?

OpenJML reports a Postcondition failure at return s and an ArithmeticOperationRange failure at s = s + i. The existing invariant only constrains i and does not provide information about the accumulated sum s. Therefore, the verifier cannot establish either the functional correctness of the result or the safety of the addition.
The program is not wrong. Based on the precondition, the actual maximum sum is 500500, which is well within the Java int range. Also, the arithmetic is correct, and the failure comes from weak loop invariant, but is not mathematically incorrect.

Q: Largest N for which `x <= N` verifies? What happens with N + 1, and why?

The largest bound is N = 65535. For N + 1 = 65536, OpenJML reports an ArithmeticOperationRange failure because the accumulated sum exceeds the maximum Java int value.

### 3. Max

Q: Is the remaining failure caused by your invariants or by the code? (Your hypothesis.)

The remaining failure is caused by the code, not the invariants. The loop condition `i < a.length - 1` skips the last array element. Consequently, the invariant only confirms that `m` is the maximum of all elements except the last element, hence it cannot guarantee that it is the maximum of the entire array. For a simple counterexample, with `a = {1, 2, 3}`, the method returns 2 instead of 3.

### 4. BinarySearch

Q: What does OpenJML report, in which method, and for which concrete call? How large must the array be?

OpenJML reports an ArithmeticOperationRange failure and a Postcondition failure both in method `midpoint`. ArithmeticOperationRange failure is reported w.r.t. the call `return (lo + hi) / 2;` regarding the potential overflow in the integer sum `lo + hi`. Also, consequently, Postcondition failure is reported w.r.t. the same call because the resulting midpoint cannot be guaranteed to lie between lo and hi due to overflow.
For example, midpoint(1073741824, 1073741824) causes overflow because the sum exceeds Integer.MAX_VALUE. To trigger this through search, the array must have at least 1073741825 elements, allowing the search to reach index 1073741824. For example, searching for 1 in an array of this length containing only zeros triggers the problematic call.

Q: What happens without `/*@ pure @*/` on `midpoint`, and why?

Without pure, OpenJML reports additional failures in search. It reports an Assignable failure at the call to midpoint, because the method is no longer guaranteed to have no side effects and may modify anything (\everything). Consequently, OpenJML cannot preserve the loop invariants that depend on the contents of array a, resulting in two additional LoopInvariant failures.
The reason is that pure tells OpenJML that the method does not modify the program state, allowing the verifier to rely on the existing array-related invariants after the method call.

## JBMC

### 5. Max, again

Q: What input did JBMC find, and what goes wrong? Was your hypothesis of exercise 3 right?

JBMC finds the counterexample `a = {3, 32770}`. The loop condition `i < a.length - 1` is false when `i = 1`, so the loop skips the last element and the method returns `3` instead of `32770`. This violates the assertion `r >= a[j]` when `j = 1`.

Yes, this confirms the hypothesis from Exercise 3: the verification failure is caused by the incorrect loop condition, not by insufficient loop invariants.

Q: Smallest `--unwind` and `--max-nondet-array-length` that find it? Relation to the small scope hypothesis?

The smallest `--unwind` is 2, and the smallest `--max-nondet-array-length` is 2.

An array of length 2 is sufficient to expose the bug because `Max.max` skips the last element. The harness needs two loop iterations to check both array elements and detect the incorrect result. With `--unwind 1`, the second iteration is ignored.

This supports the small scope hypothesis: the bug can be detected using a very small input and a small unwinding bound.

Q: Why does `--unwind 5` with unwinding assertions fail on the fixed version? Smallest K with `--max-nondet-array-length 4`, and why not one less? What has been proved?

With `--unwind 5`, JBMC reports an unwinding assertion failure in `MaxCheck.check`, rather than a functional correctness failure. The input array may have length 5, so the harness loop needs five iterations followed by a final condition check to establish termination. The unwinding bound is insufficient for this check.

With `--max-nondet-array-length 4`, the smallest successful unwinding bound is `K = 5`. Using `K = 4` still fails because the harness loop may execute four times and requires an additional condition check to establish that it has terminated.

Therefore, JBMC has verified that the fixed `Max.max` satisfies both postconditions for all nonempty Java `int` arrays of length at most 4, not for arrays of arbitrary length.

Note: JML proposed no issue as well.

### 6. SumX

Q: Which values of `x` were checked with `--unwind 10 --no-unwinding-assertions`? Why does it fail with unwinding assertions? Which K is a proof? Sizes and times for K = 10, 100 and that K.

With `--unwind 10 --no-unwinding-assertions`, JBMC checks `x` from 0 to 9. Larger values are excluded because their executions exceed the unwinding bound.

With unwinding assertions enabled, `K = 10` fails because the precondition allows `x` up to 1000, requiring more iterations than the current bound. For example, JBMC finds `x = 161`, which triggers an unwinding assertion failure.

The smallest bound that provides a complete proof is `K = 1001`, since the loop may execute 1000 times and requires an additional condition check to establish termination.

| K | Variables | Clauses | Solver time | Result |
|---|---:|---:|---:|---|
| 10 | 4,638 | 13,644 | 0.01s | FAILED |
| 100 | 9,587 | 30,260 | 0.03s | FAILED |
| 1001 | 66,870 | 666,136 | 5.93s | SUCCESSFUL |

The results show that increasing the unwinding bound increases the SAT formula size and verification cost.

Q: What did OpenJML need from you, and what did JBMC need?

OpenJML required a sufficiently strong loop invariant, particularly `s == i * (i + 1) / 2`, to prove the postcondition for all permitted inputs.

JBMC did not require loop invariants. Instead, it required a harness containing the precondition and postcondition as assumptions and assertions, together with a sufficiently large unwinding bound (in this particular case `K = 1001`) to cover all executions.

### 7. BinarySearch

Q: Why does JBMC miss the bug with the `search` harness? Could a larger bound help?

JBMC misses the bug because the `search` harness restricts the input arrays to a maximum length of 5. Consequently, the indices passed to `midpoint` are too small for `lo + hi` to overflow.

To trigger the overflow through `search`, an array of at least 1073741825 elements is required. Increasing the unwinding bound alone cannot expose the bug. Increasing the array-length bound sufficiently could help theoretically, but such a large array would make bounded verification impractical.

Q: What did the `midpoint` harness find?

The `midpoint` harness found an integer overflow counterexample with `lo = 272646145` and `hi = 2143272958`. Their sum exceeds `Integer.MAX_VALUE`, causing the computed midpoint to become `-939524096`, which is smaller than `lo` and violates the postcondition.

Unlike the `search` harness, this harness directly checks `midpoint` without restricting its arguments through array indices, allowing JBMC to detect the bug.

Both verifiers successfully proved the program after the fix.

## Comparison

### 8. Comparison

| program | OpenJML: result, what you wrote | JBMC: result, bound used |
| --- | --- | --- |
| SumX | Verified successfully after adding the loop invariant `s == i * (i + 1) / 2`, with `x <= 1000`. | Verified successfully with `--unwind 1001`. No loop invariant was required, but the harness needed the precondition and postcondition. |
| Max | The original code failed its postcondition even after adding loop invariants and a decreases clause. After fixing the loop condition to `i < a.length`, verification succeeded. | Found a counterexample with `--unwind 2 --max-nondet-array-length 2 --no-unwinding-assertions`. After the fix, verification succeeded with `--unwind 5 --max-nondet-array-length 4`. |
| BinarySearch | Detected integer overflow and a postcondition failure in `midpoint`. After replacing `(lo + hi) / 2` with `lo + (hi - lo) / 2`, verification succeeded. | The `search` harness missed the bug with `--unwind 6 --max-nondet-array-length 5`. The `midpoint` harness found an overflow counterexample with `--unwind 1`, and verification succeeded after the fix. |

Q: When would you use each tool?

I would use OpenJML when I need to prove program correctness for all inputs satisfying the preconditions, especially when loop invariants can be established. However, it requires suitable contracts and invariants, and a verification failure does not necessarily imply an actual bug.

I would use JBMC when I want to automatically find concrete counterexamples without writing loop invariants. It is particularly useful for detecting bugs with small inputs or short execution paths. However, verification success only guarantees correctness within the checked bounds unless the bounds are sufficient to cover all possible executions-and it would not be practical to blindly increase the bounds.
