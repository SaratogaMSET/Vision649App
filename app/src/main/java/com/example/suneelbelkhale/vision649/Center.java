package com.example.suneelbelkhale.vision649;

/**
 * Created by suneelbelkhale1 on 3/15/16.
 *
 */
public class Center {
    public double x, y;

    public Center(double _x, double _y){
        this.x = _x;
        this.y = _y;
    }

    @Override
    public boolean equals(Object c2){
        if (c2 instanceof Center) {
            return ((Center) c2).x == this.x && ((Center) c2).y == this.y;
        }
        else{
            return false;
        }
    }
}