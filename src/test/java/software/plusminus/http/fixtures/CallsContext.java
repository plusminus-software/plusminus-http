package software.plusminus.http.fixtures;

import lombok.experimental.UtilityClass;
import software.plusminus.check.Checks;

import java.util.LinkedHashSet;
import java.util.Set;

@UtilityClass
public class CallsContext {

    private static final Set<String> CALLS = new LinkedHashSet<>();

    public void check(String... calls) {
        Checks.check(CALLS).is((Object[]) calls);
    }

    public void add(String call) {
        CALLS.add(call);
    }

    public void clear() {
        CALLS.clear();
    }
}
