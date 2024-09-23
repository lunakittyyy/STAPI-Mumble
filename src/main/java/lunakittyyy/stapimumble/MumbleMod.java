package lunakittyyy.stapimumble;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.client.event.network.ServerLoginSuccessEvent;
import net.modificationstation.stationapi.api.entity.player.PlayerHandler;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Null;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;

public class MumbleMod implements PlayerHandler {

    @Entrypoint.Logger
    private static final Logger LOGGER = Null.get();

    private boolean mumbleInited = false;
    private static boolean libLoaded = false;
    private static ArrayList<Exception> errors = new ArrayList<Exception>();
    private static final String libName = "mumble";
    private static final String modName = "STAPI-Mumble";

    @EventListener
    private void joinEvent(ServerLoginSuccessEvent event) {
        var game = (Minecraft)FabricLoader.getInstance().getGameInstance();
        for (Exception exc : errors) {
            game.inGameHud.addChatMessage("STAPI-Mumble: " + exc.getMessage());
        }
    }

    @EventListener
    private void tickEvent(GameTickEvent.End event) {
        var game = (Minecraft)FabricLoader.getInstance().getGameInstance();
        if (game.world == null || !errors.isEmpty()) return;

        if (!mumbleInited) {
            if (!tryInitMumble()) {
                return;
            }
        }
        updateMumble((Minecraft)FabricLoader.getInstance().getGameInstance());
    }

    @EventListener
    private void serverInit(InitEvent event) {
        String s = File.separator;
        String dllFolder = FabricLoader.getInstance().getGameDir().toString() + s + "mods" + s + modName + s + "natives" + s;

        LOGGER.warn(dllFolder + libName + "_x64.dll");
        LOGGER.warn(dllFolder + "lib" + libName + "_x64.so");

        tryInitMumble();
    }

    private native int initMumble();

    static {
        String s = File.separator;
        String dllFolder = FabricLoader.getInstance().getGameDir().toString() + s + "mods" + s + "STAPI-Mumble" + s + "natives" + s;
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                System.load(dllFolder + libName + "_x64.dll");
            } else if (SystemUtils.IS_OS_LINUX) {
                System.load(dllFolder + "lib" + libName + "_x64.so");
            } else {
                errors.add(new UnsupportedOperationException("Unsupported operating system. Only Windows and Linux are supported"));
            }
        } catch (UnsatisfiedLinkError err) {
            errors.add(new Exception("Failed to load natives. Make sure you are using a 64-bit JVM"));
        }
    }

    private boolean tryInitMumble() {
        // initialize the mumble link, create linked memory

        try { initMumble(); } catch (UnsatisfiedLinkError e) {
            mumbleInited = false;
            LOGGER.error(e);
            return false;
        }
        mumbleInited = true;
        return true;
    }

    private void updateMumble(Minecraft game) {
        try {
            // 1 unit = 1 meter

            // initialize multipliers
            float fAvatarFrontX = 1;
            float fAvatarFrontY = 0;
            float fAvatarFrontZ = 1;

            float fCameraFrontX = 1;
            float fCameraFrontY = 0;
            float fCameraFrontZ = 1;

            float fAvatarTopX = 0;
            float fAvatarTopY = 1;
            float fAvatarTopZ = 0;

            float fCameraTopX = 0;
            float fCameraTopY = 1;
            float fCameraTopZ = 0;

            Vec3d camera;
            try {
                camera = game.player.method_1320();
            } catch (Exception ex) { return; }


            if (camera == null) {
                LOGGER.warn("Camera is null, not attempting to send anything to Mumble");
                return;
            }

            // Position of the avatar
            float[] fAvatarPosition = {
                    Float.parseFloat(Double.toString(game.player.x)),
                    Float.parseFloat(Double.toString(game.player.z)),
                    Float.parseFloat(Double.toString(game.player.y))};


            // Unit vector pointing out of the avatars eyes (here Front looks into scene).
            float[] fAvatarFront = {
                    Float.parseFloat(Double.toString(camera.x * fAvatarFrontX)),
                    Float.parseFloat(Double.toString(camera.z * fAvatarFrontZ)),
                    Float.parseFloat(Double.toString(camera.y * fAvatarFrontY))};

            // Unit vector pointing out of the top of the avatars head (here Top looks straight up).
            float[] fAvatarTop = {fAvatarTopX, fAvatarTopZ, fAvatarTopY};

            float[] fCameraPosition = {
                    Float.parseFloat(Double.toString(game.player.x)),
                    Float.parseFloat(Double.toString(game.player.z)),
                    Float.parseFloat(Double.toString(game.player.y))};

            float[] fCameraFront = {
                    Float.parseFloat(Double.toString(camera.x * fCameraFrontX)),
                    Float.parseFloat(Double.toString(camera.z * fCameraFrontZ)),
                    Float.parseFloat(Double.toString(camera.y * fCameraFrontY))};

            float[] fCameraTop = {fCameraTopX, fCameraTopZ, fCameraTopY};

            String identity = game.player.name;

            String context = "MinecraftAllTalk";
            context = generateContextJSON(game.world);

            String name = "Minecraft";

            String description = "Link plugin for Minecraft Beta 1.7.3 with Babric and StationAPI";

            int err = updateLinkedMumble(fAvatarPosition, fAvatarFront, fAvatarTop, name, description, fCameraPosition, fCameraFront, fCameraTop, identity, context);

        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    private native int updateLinkedMumble(
            float[] fAvatarPosition, // [3]
            float[] fAvatarFront, // [3]
            float[] fAvatarTop, // [3]
            String name, // [256]
            String description,
            float[] fCameraPosition, // [3]
            float[] fCameraFront, // [3]
            float[] fCameraTop, // [3]
            String identity, // [256]
            String context);

    private String generateContextJSON(World world) {
        int contextSize = 256; // from linkedMem.h: unsigned char context[256];

        // strings needed for context
        String startStr = "{";
        String gameStr = "\"game\":\"Minecraft\", ";
        // NOTE: worldName for multiplayer servers is by default "MPServer" seed is probably unique enough
        //String worldNameInit = "\"WorldName\":\"";
        String worldSeedInit = "\"WorldSeed\":\"";
        String concatinator = "\", ";
        String endStr = "\"}";


        // 1 for 1 dynamic context only (world seed)
        // 2 for 2 dynamic contexts (world name, world seed)
        int numContents = 1;

        String worldSeed = Long.toString(world.getSeed());

        // string if world is not set
        String context_empty = startStr
                + gameStr
                + worldSeedInit
                + endStr;

        int remainderFraction = (contextSize - context_empty.getBytes().length) / numContents;
        int newWorldSeedLen = Math.min(worldSeed.getBytes().length, remainderFraction);

        String context = startStr
                + gameStr
                + worldSeedInit + worldSeed.substring(0, newWorldSeedLen)
                + endStr;

        return context;
    }
}
