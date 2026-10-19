import java.lang.reflect.*;

/** Runs against compiled production spring math; no game world or renderer required. */
class TailMotionCheck {
    static Constructor<?> constructor;
    static Method update;
    static Field lift,yaw;
    static Object fresh() throws Exception {return constructor.newInstance();}
    static void step(Object state,float age,float l,float y) throws Exception {update.invoke(state,age,l,y);}
    public static void main(String[] args) throws Exception {
        Class<?> type=Class.forName("net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals$TailMotion");
        constructor=type.getDeclaredConstructor();constructor.setAccessible(true);
        update=type.getDeclaredMethod("update",float.class,float.class,float.class);update.setAccessible(true);
        lift=type.getDeclaredField("lift");lift.setAccessible(true);yaw=type.getDeclaredField("yaw");yaw.setAccessible(true);
        Object a=fresh();step(a,0,0,0);step(a,1,.3F,.4F);
        float initial=lift.getFloat(a);
        if(!(initial>0&&initial<.3F))throw new AssertionError("Must lag movement");
        step(a,1,.3F,.4F);
        if(lift.getFloat(a)!=initial)throw new AssertionError("Duplicate render advanced spring");
        for(int i=2;i<=60;i++)step(a,i,.3F,.4F);
        if(Math.abs(lift.getFloat(a)-.3F)>.001||Math.abs(yaw.getFloat(a)-.4F)>.001)throw new AssertionError("Motion did not settle");
        step(a,61,0,0);
        if(lift.getFloat(a)<=0)throw new AssertionError("Stopping must retain inertia");
        for(int i=62;i<=120;i++)step(a,i,0,0);
        if(Math.abs(lift.getFloat(a))>.001||Math.abs(yaw.getFloat(a))>.001)throw new AssertionError("Stopped tail did not settle downward");
        Object slow=fresh(),fast=fresh();step(slow,0,0,0);step(fast,0,0,0);
        for(int i=1;i<=30;i++)step(slow,i*20F/30,.3F,.4F);
        for(int i=1;i<=120;i++)step(fast,i*20F/120,.3F,.4F);
        if(Math.abs(lift.getFloat(slow)-lift.getFloat(fast))>.002)throw new AssertionError("Frame rate changes spring response");
        if(lift.getFloat(fresh())!=0)throw new AssertionError("New entity inherited another tail");
        System.out.println("PASS: movement lag, duplicate render, turn response, stopping inertia, rest, frame-rate consistency and entity isolation.");
    }
}
