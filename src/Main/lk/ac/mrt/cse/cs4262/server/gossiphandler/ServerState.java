package lk.ac.mrt.cse.cs4262.server.gossiphandler;

import lk.ac.mrt.cse.cs4262.server.Connectable;
import lk.ac.mrt.cse.cs4262.server.Constant;

import java.util.HashMap;

public class ServerState implements Connectable {
    HashMap<String, VersionedValue> values = new HashMap<>();
    public ServerState(){
    }
    public void putValue(String key, VersionedValue versionedValue){
        values.put(key,versionedValue);
    }

    public HashMap<String, VersionedValue> getValues() {
        return values;
    }

    public void setValues(HashMap<String, VersionedValue> values) {
        this.values = values;
    }

    public ServerState diff(ServerState toStates) {
        ServerState s = new ServerState();
        values.forEach((s1, versionedValue) -> {
            if(toStates.getValues().containsKey(s1)){
                if(toStates.getValues().get(s1).getVersion()<versionedValue.getVersion()){
                    s.putValue(s1,versionedValue);
                }else{
                    s.putValue(s1,toStates.getValues().get(s1));
                }
            }else{
                s.putValue(s1,versionedValue);
            }
        });
        return s;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void putAll(ServerState serverState) {
        serverState.getValues().forEach(this::putValue);
    }

    @Override
    public int getCoordinationPort() {
        return (int) values.get(Constant.GOSSIPDATA_COORDINATIONPORT).getValue();
    }

    @Override
    public String getAddress() {
        return (String) values.get(Constant.GOSSIPDATA_ADDRESS).getValue();
    }
}
