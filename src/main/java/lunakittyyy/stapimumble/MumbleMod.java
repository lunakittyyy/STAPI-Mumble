package lunakittyyy.stapimumble;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.entity.player.PlayerHandler;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Null;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class MumbleMod implements PlayerHandler {

    @Entrypoint.Logger
    private static final Logger LOGGER = Null.get();

    private boolean mumbleInited = false;
    private static boolean libLoaded = false;
    private static ArrayList<UnsatisfiedLinkError> errors = new ArrayList<UnsatisfiedLinkError>();
    private static final String libName = "mumble";
    private static final String modName = "STAPI-Mumble";

    @EventListener
    private void tickEvent(GameTickEvent.End event) {
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
        LOGGER.warn(dllFolder + "lib" + libName + "_x64.dylib");

        tryInitMumble();
    }

    private native int initMumble();

    static {
        String s = File.separator;
        String dllFolder = FabricLoader.getInstance().getGameDir().toString() + s + "mods" + s + "STAPI-Mumble" + s + "natives" + s;

        attemptLoadLibrary(dllFolder + libName + "_x64.dll", true);

        attemptLoadLibrary(dllFolder + "lib" + libName + "_x64.so", true);

        attemptLoadLibrary(dllFolder + "lib" + libName + "_x64.dylib", true);

        if (!libLoaded) {
            UnsatisfiedLinkError err;
            // if no errors were registered

            if (errors.isEmpty()) {
                // throw missing libraries error
                err = new UnsatisfiedLinkError("Library files not found!");

            } else {
                // throw incompatibility error
                err = new UnsatisfiedLinkError("Required library could not be loaded, available libraries are incompatible!");

            }
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

            Vec3d camera = game.player.method_1320();

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

    /**
     * load the specified library from path
     *
     * @param lib library name
     * @throws UnsatisfiedLinkError loading of a found library failed
     */
    private static void attemptLoadLibrary(String lib) {
        attemptLoadLibrary(lib, false);


    }

    /**
     * load library from either path or a file
     *
     * @param lib name of the library or path of the file
     * @param file if true lib is expected to specify a file
     * @throws UnsatisfiedLinkError loading of a found library failed
     */
    private static void attemptLoadLibrary(String lib, boolean file) {
        // if the library was already loaded skip
        if (!libLoaded) {

            // try loading lib
            try {
                // if supplied lib is a file path
                if (file) {
                    // attempt to load library file
                    System.load(lib);

                } else {
                    // attemt to load the library from jpath
                    System.loadLibrary(lib);

                }

            } catch (UnsatisfiedLinkError err) {
                //ModLoader.getLogger().fine("[DEBUG] " + err);

                // check if the library was not found
                if (err.getMessage().startsWith("no ")
                        || err.getMessage().startsWith("Can't load library")) {

                    // library was not loaded because it was not found
                    return;

                } else {
                    // loading failed, throw error
                    errors.add(err);

                    return;
                }
            }

            // mark success
            libLoaded = true;

            //ModLoader.getLogger().fine("[DEBUG] loaded: " + lib);
        }
    }
}
