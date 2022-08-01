package com.thclouds.agent.context;

public final class EntryContext {

    private static final ThreadLocal<EntryHolder> holderThreadLocal = new ThreadLocal<EntryHolder>();

    public static EntryHolder getEntryHolder(){
        EntryHolder entryHolder = holderThreadLocal.get();
        holderThreadLocal.remove();
        return entryHolder;
    }

    public static void putEntryHolder(EntryHolder entryHolder){
         holderThreadLocal.set(entryHolder);
    }

}
