package scraper.nodes.core.flow;

import java.util.Objects;

public class JoinKey {
    public final int size;
    public final int num;
    public final int uid;

    public JoinKey(int size, int uid, int num) {
        this.size = size;
        this.uid = uid;
        this.num = num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinKey joinKey = (JoinKey) o;
        return uid == joinKey.uid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
