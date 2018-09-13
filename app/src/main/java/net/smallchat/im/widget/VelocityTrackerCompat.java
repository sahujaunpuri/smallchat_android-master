 package net.smallchat.im.widget;
 
 import android.os.Build;
import android.view.VelocityTracker;
 
 public class VelocityTrackerCompat
 {
   static final VelocityTrackerVersionImpl IMPL;
 
   public static float getXVelocity(VelocityTracker tracker, int pointerId)
   {
     return IMPL.getXVelocity(tracker, pointerId);
   }
 
   public static float getYVelocity(VelocityTracker tracker, int pointerId)
   {
     return IMPL.getYVelocity(tracker, pointerId);
   }
 
   static
   {
     if (Build.VERSION.SDK_INT >= 11)
       IMPL = new HoneycombVelocityTrackerVersionImpl();
     else
       IMPL = new BaseVelocityTrackerVersionImpl();
   }
 
   static class HoneycombVelocityTrackerVersionImpl
     implements VelocityTrackerCompat.VelocityTrackerVersionImpl
   {
     public float getXVelocity(VelocityTracker tracker, int pointerId)
     {
       return VelocityTrackerCompatHoneycomb.getXVelocity(tracker, pointerId);
     }
 
     public float getYVelocity(VelocityTracker tracker, int pointerId) {
       return VelocityTrackerCompatHoneycomb.getYVelocity(tracker, pointerId);
     }
   }
 
   static class BaseVelocityTrackerVersionImpl
     implements VelocityTrackerCompat.VelocityTrackerVersionImpl
   {
     public float getXVelocity(VelocityTracker tracker, int pointerId)
     {
       return tracker.getXVelocity();
     }
 
     public float getYVelocity(VelocityTracker tracker, int pointerId) {
       return tracker.getYVelocity();
     }
   }
 
   static abstract interface VelocityTrackerVersionImpl
   {
     public abstract float getXVelocity(VelocityTracker paramVelocityTracker, int paramInt);
 
     public abstract float getYVelocity(VelocityTracker paramVelocityTracker, int paramInt);
   }
 }

