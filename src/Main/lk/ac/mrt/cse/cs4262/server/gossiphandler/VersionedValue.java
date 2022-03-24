package lk.ac.mrt.cse.cs4262.server.gossiphandler;

public class VersionedValue {
    long version;
    Object value;

    public VersionedValue(Object value, long version) {
        this.version = version;
        this.value = value;
    }

    public long getVersion() {
        return version;
    }

    public Object getValue() {
        return value;
    }
}
