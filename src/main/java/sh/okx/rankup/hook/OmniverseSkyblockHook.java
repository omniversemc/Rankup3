package sh.okx.rankup.hook;

import java.util.UUID;

public class OmniverseSkyblockHook {

    public static String getProfile(UUID uuid) {
        try {
            Class<?> skyPlayerClass = Class.forName("us.omniverse.sbi.skyblock.managers.generic.SkyPlayer");
            Object skyPlayer = skyPlayerClass.getMethod("getPlayer", UUID.class).invoke(null, uuid);
            Object skyProfile = skyPlayerClass.getMethod("getProfile").invoke(skyPlayer);
            return (String) skyProfile.getClass().getDeclaredField("id").get(skyProfile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
