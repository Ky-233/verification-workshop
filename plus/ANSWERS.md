# Homework plus: answers

Name(s): Siyuan LIAO 999019805

### 1. Invariants from an LLM

| method | LLM | rounds | mistakes | final invariants by |
| --- | --- | --- | --- | --- |
| count | GPT-6 | 2 | Round 1: proof timeout; adding `0 <= c && c <= i` resolved it. | LLM |
| reverse | GPT-6 | 1 | None | LLM |
| isqrt | GPT-6 | 1 | None | LLM |
| dutchFlag | GPT-6 | 2 | Round 1: Missing an invariant ensuring all array elements remain in {0, 1, 2}; OpenJML could not prove that the else branch handles only 2. | LLM |

Q: Why is it safe to use invariants written by an LLM here, even if the LLM is often wrong?

Because OpenJML independently verifies the proposed invariants. The LLM only suggests invariants; OpenJML checks whether they hold initially, are preserved by each loop iteration, and are strong enough to prove the specified postconditions. Therefore, an incorrect or insufficient LLM proposal does not count as a successful proof.


### 2. The Zune bug

Q: `--unwind 31 --no-unwinding-assertions` says SUCCESSFUL. Does that mean `yearOf` is correct for every day it checked?

No. Disabling unwinding assertions excludes execution paths exceeding the bound, including potentially nonterminating paths(which is exactly the bug we have here). Moreover, the harness only checks whether the returned year is within the expected range, not whether it is the correct year for the given day.

Q: What does JBMC report without `--no-unwinding-assertions`? Is K too small, or is something else wrong?

JBMC reports an unwinding assertion failure. The counterexample reaches year = 1984 and days = 366, where the loop repeats without changing either variable. This is a genuine infinite loop, but not an insufficient unwinding bound.

With a larger bound, K = 40, we still have a counterexample on the last day of 1980.

Q: What is wrong with the loop? Which date froze the Zune?

The loop condition `days > 365` allows `days == 366`, but in a leap year, the inner condition `days > 366` is false. Neither `days` nor `year` changes, causing an infinite loop. This bug froze the Zune on December 31, 2008 in real life.

Q: With your fix, what is the smallest K that gives VERIFICATION SUCCESSFUL with unwinding assertions? What exactly has been proved?

The smallest bound is K = 30. With K = 29, JBMC reports an unwinding assertion failure, while K = 30 verifies successfully. This proves that yearOf terminates within the bound and returns a year in [1980, 2010) for every input satisfying the harness precondition. However, it does not prove that the returned year is correct.

Q: A colleague changes `days > 366` to `days >= 366`. Which harness notices, and why?

The `yearOfIsRight` harness detects the bug, while `yearOfInRange` does not. For `days = 366`, the modified code incorrectly returns 1981 instead of 1980. This still satisfies the year-range check, but violates the assertion that the input day belongs to the returned year.

Q (★): Why does `./esc` say OK on the original? What changes with `decreases`?

Without a `decreases` clause, OpenJML verifies partial correctness: if the method terminates, its postcondition holds. It does not prove termination.

After adding `decreases days`, OpenJML reports a `LoopDecreases` failure. When `days == 366` in a leap year, the loop repeats without changing `days`, violating the requirement that the decreasing expression strictly decreases on each iteration.

### 3. Range

Q: Why does `size()` not verify? Give concrete values.

The method uses `int` arithmetic, which may overflow even though the class invariant `lo <= hi` holds. OpenJML reports ArithmeticOperationRange failures for both subtraction and addition, as well as a Postcondition failure.

For example, with `lo = -1` and `hi = Integer.MAX_VALUE`, the subtraction `hi - lo` overflows. With `lo = 0` and `hi = Integer.MAX_VALUE`, the final addition overflows, producing `-2147483648` instead of the correct size `2147483648`.

The postcondition uses `long` arithmetic to specify the correct interval size, so the overflowed `int` result violates it. Thus, the problem is in the implementation, not the postcondition.

Q: Compare JBMC's counterexample with your answer above.

JBMC confirms the same integer overflow problem. It finds `lo = -1985200127` and `hi = 506855424`, for which the correct interval size is 2492055552. However, `size()` returns -1802911744 due to `int` overflow, violating the postcondition.

This agrees with the earlier analysis, although JBMC finds different concrete values.

### 4. StackAr

Q: Which tool reports your bug, and how? Could a bug get past OpenJML but not JBMC, or the other way around?

Both tools detected the bug introduced by removing `top--` from `pop()`.

OpenJML reported a Postcondition failure because `top` no longer satisfies `top == \old(top) - 1`.

JBMC found a counterexample with capacity 1: after a push followed by a pop, the reference model has size 0, but the actual stack still has size 1, violating `assert s.size() == n`.

A bug may pass OpenJML if the contracts are too weak to specify the incorrect behavior, while JBMC may detect it through a stronger harness. Conversely, JBMC may miss bugs outside its bounds, even when OpenJML detects them.

For example, we temporarily changed isFull() to return top == elems.length - 1 || top == 2(which is a stupid bug). OpenJML reported a Postcondition failure indeed. However, JBMC reported VERIFICATION SUCCESSFUL because its harness only considers capacities up to 3, where the bug cannot occur. This illustrates how bounded verification can miss errors outside the tested input range.

### 5. SafeLock

Q: Smallest K that finds a bug? Which sequence of events? What is wrong in the code?

The smallest bound is K = 5. JBMC finds the sequence: wrong PIN, wrong PIN, Close, wrong PIN, correct PIN.

The bug is in `close()`, which resets `failed` to 0 even when the safe is already LOCKED. As a result, the three wrong PIN attempts do not trigger BLOCKED, and the next correct PIN opens the safe, violating the property.

Q: Formula size and time for K = 8, 10, 12.

| K | Variables | Clauses | Solver time | Result |
| --- | ---: | ---: | ---: | --- |
| 8 | 46050 | 68865 | 0.40s | SUCCESSFUL |
| 10 | 56941 | 87971 | 0.78s | SUCCESSFUL |
| 12 | 67836 | 107563 | 2.04s | SUCCESSFUL |

As K increases, the SAT formula becomes larger and the solver takes more time. All three bounds verified successfully after the fix.


Q: Does SUCCESSFUL with K = 12 mean the property holds for every sequence? Can unwinding assertions help?

No. With unwinding assertions disabled, JBMC only verifies the property for event sequences within the unwinding bound. Longer sequences are not covered.

Enabling unwinding assertions would not establish correctness for arbitrary sequences, because the nondeterministic loop can continue indefinitely. A general proof would require an invariant or another unbounded verification technique.
