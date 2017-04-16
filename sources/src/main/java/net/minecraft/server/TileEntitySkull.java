package net.minecraft.server;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import javax.annotation.Nullable;

// Spigot start
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.ProfileLookupCallback;
// Spigot end

public class TileEntitySkull extends TileEntity /*implements ITickable*/ { // Paper - remove tickable

    private int a;
    public int rotation;
    private GameProfile g;
    private int h;
    private boolean i;
    private static UserCache j;
    private static MinecraftSessionService k;
    // Spigot start
    public static final Executor executor = Executors.newFixedThreadPool(3,
            new ThreadFactoryBuilder()
                    .setNameFormat("Head Conversion Thread - %1$d")
                    .build()
    );
    public static final LoadingCache<String, GameProfile> skinCache = CacheBuilder.newBuilder()
            .maximumSize( 5000 )
            .expireAfterAccess( 60, TimeUnit.MINUTES )
            .build( new CacheLoader<String, GameProfile>()
            {
                @Override
                public GameProfile load(String key) throws Exception
                {
                    final GameProfile[] profiles = new GameProfile[1];
                    ProfileLookupCallback gameProfileLookup = new ProfileLookupCallback() {

                        @Override
                        public void onProfileLookupSucceeded(GameProfile gp) {
                            profiles[0] = gp;
                        }

                        @Override
                        public void onProfileLookupFailed(GameProfile gp, Exception excptn) {
                            profiles[0] = gp;
                        }
                    };

                    MinecraftServer.getServer().getGameProfileRepository().findProfilesByNames(new String[] { key }, Agent.MINECRAFT, gameProfileLookup);

                    GameProfile profile = profiles[ 0 ];
                    if (profile == null) {
                        UUID uuid = EntityHuman.a(new GameProfile(null, key));
                        profile = new GameProfile(uuid, key);

                        gameProfileLookup.onProfileLookupSucceeded(profile);
                    } else
                    {

                        Property property = Iterables.getFirst( profile.getProperties().get( "textures" ), null );

                        if ( property == null )
                        {
                            profile = MinecraftServer.getServer().az().fillProfileProperties( profile, true );
                        }
                    }


                    return profile;
                }
            } );
    // Spigot end

    public TileEntitySkull() {}

    public static void a(UserCache usercache) {
        TileEntitySkull.j = usercache;
    }

    public static void a(MinecraftSessionService minecraftsessionservice) {
        TileEntitySkull.k = minecraftsessionservice;
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        nbttagcompound.setByte("SkullType", (byte) (this.a & 255));
        nbttagcompound.setByte("Rot", (byte) (this.rotation & 255));
        if (this.g != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            GameProfileSerializer.serialize(nbttagcompound1, this.g);
            nbttagcompound.set("Owner", nbttagcompound1);
        }

        return nbttagcompound;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a = nbttagcompound.getByte("SkullType");
        this.rotation = nbttagcompound.getByte("Rot");
        if (this.a == 3) {
            if (nbttagcompound.hasKeyOfType("Owner", 10)) {
                this.g = GameProfileSerializer.deserialize(nbttagcompound.getCompound("Owner"));
            } else if (nbttagcompound.hasKeyOfType("ExtraType", 8)) {
                String s = nbttagcompound.getString("ExtraType");

                if (!UtilColor.b(s)) {
                    this.g = new GameProfile((UUID) null, s);
                    this.h();
                }
            }
        }

    }

    public void F_() {
        if (this.a == 5) {
            if (this.world.isBlockIndirectlyPowered(this.position)) {
                this.i = true;
                ++this.h;
            } else {
                this.i = false;
            }
        }

    }

    @Nullable
    public GameProfile getGameProfile() {
        return this.g;
    }

    @Nullable
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return new PacketPlayOutTileEntityData(this.position, 4, this.d());
    }

    public NBTTagCompound d() {
        return this.save(new NBTTagCompound());
    }

    public void setSkullType(int i) {
        this.a = i;
        this.g = null;
    }

    public void setGameProfile(@Nullable GameProfile gameprofile) {
        this.a = 3;
        this.g = gameprofile;
        this.h();
    }

    private void h() {
        // Spigot start
        GameProfile profile = this.g;
        setSkullType( 0 ); // Work around client bug
        b(profile, new Predicate<GameProfile>() {

            @Override
            public boolean apply(GameProfile input) {
                setSkullType(3); // Work around client bug
                g = input;
                update();
                if (world != null) {
                    world.m(position); // PAIL: notify
                }
                return false;
            }
        }); 
        // Spigot end
    }

    // Spigot start - Support async lookups
    public static void b(final GameProfile gameprofile, final Predicate<GameProfile> callback) {
        if (gameprofile != null && !UtilColor.b(gameprofile.getName())) {
            if (gameprofile.isComplete() && gameprofile.getProperties().containsKey("textures")) {
                callback.apply(gameprofile);
            } else if (MinecraftServer.getServer() == null) {
                callback.apply(gameprofile);
            } else {
                GameProfile profile = skinCache.getIfPresent(gameprofile.getName().toLowerCase()); // Paper
                if (profile != null && Iterables.getFirst(profile.getProperties().get("textures"), (Object) null) != null) {
                    callback.apply(profile);
                } else {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            final GameProfile profile = skinCache.getUnchecked(gameprofile.getName().toLowerCase());                            
                            MinecraftServer.getServer().processQueue.add(new Runnable() {
                                @Override
                                public void run() {
                                    if (profile == null) {
                                        callback.apply(gameprofile);
                                    } else {
                                        callback.apply(profile);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        } else {
            callback.apply(gameprofile);
        }
    }
    // Spigot end

    public int getSkullType() {
        return this.a;
    }

    public void setRotation(int i) {
        this.rotation = i;
    }

    public void a(EnumBlockMirror enumblockmirror) {
        if (this.world != null && this.world.getType(this.getPosition()).get(BlockSkull.FACING) == EnumDirection.UP) {
            this.rotation = enumblockmirror.a(this.rotation, 16);
        }

    }

    public void a(EnumBlockRotation enumblockrotation) {
        if (this.world != null && this.world.getType(this.getPosition()).get(BlockSkull.FACING) == EnumDirection.UP) {
            this.rotation = enumblockrotation.a(this.rotation, 16);
        }

    }
}
