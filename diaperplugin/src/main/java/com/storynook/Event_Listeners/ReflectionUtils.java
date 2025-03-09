package com.storynook.Event_Listeners;

import org.bukkit.entity.ItemFrame;
import java.lang.reflect.Field;

public class ReflectionUtils {
    public static void setInvisible(ItemFrame frame) {
        try {
            Field invariantField = ItemFrame.class.getDeclaredField("invariant");
            invariantField.setAccessible(true);
            invariantField.set(frame, true);
            
            // Also prevent glowing
            frame.setGlowing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
