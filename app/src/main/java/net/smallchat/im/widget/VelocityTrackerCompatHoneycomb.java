 package net.smallchat.im.widget;
 
 import android.view.VelocityTracker;
 
 class VelocityTrackerCompatHoneycomb
 {
   public static float getXVelocity(VelocityTracker tracker, int pointerId)
   {
     return tracker.getXVelocity(pointerId);
   }
   public static float getYVelocity(VelocityTracker tracker, int pointerId) {
     return tracker.getYVelocity(pointerId);
   }
 }

