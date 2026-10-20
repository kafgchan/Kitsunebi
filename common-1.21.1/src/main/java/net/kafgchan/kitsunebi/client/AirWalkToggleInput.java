package net.kafgchan.kitsunebi.client;

/** One complete chord per press, regardless of key order or keyboard repeat. */
public final class AirWalkToggleInput {
    private boolean armed=true;
    public boolean update(boolean jump,boolean sneak,boolean enabled) {
        if(!jump&&!sneak){armed=true;return false;}
        if(!enabled){armed=false;return false;}
        if(armed&&jump&&sneak){armed=false;return true;}
        return false;
    }
    public void suspend(boolean jump,boolean sneak){armed=!jump&&!sneak;}
}
