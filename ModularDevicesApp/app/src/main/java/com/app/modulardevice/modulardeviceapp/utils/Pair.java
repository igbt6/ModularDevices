package com.app.modulardevice.modulardeviceapp.utils;

/**
 * Created by igbt6 on 01.11.2015.
 */
public class Pair<F, S> {
    private F mF;
    private S mS;

    public Pair(F f, S s){

        mF = f;
        mS = s;
    }

    public Pair(){

    }



    public F getFirst() {
        return mF;
    }

    public void setFirst(F f) {
        this.mF = f;
    }

    public S getSecond() {
        return mS;
    }

    public void setSecond(S s) {
        this.mS = s;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Pair){
            Pair pair = (Pair)o;
            return ((mF==pair.mF&&mF!=null&&pair.mF!=null&&mF.equals(pair.mF))&&
                    (mS==pair.mS&&mS!=null&&pair.mS!=null&&mS.equals(pair.mS)));



        }
        return false;
    }

    @Override
    public String toString()
    {
        return "(" + mF + ", " + mS + ")";
    }

    @Override
    public int hashCode(){

        int hashFirst =  mF != null ?  mF.hashCode() : 0;
        int hashSecond =  mS != null ?  mS.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }
}
