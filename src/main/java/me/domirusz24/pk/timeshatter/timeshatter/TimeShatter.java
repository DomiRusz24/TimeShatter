package me.domirusz24.pk.timeshatter.timeshatter;


import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes.FrozenEntity;
import me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes.FrozenLivingEntity;
import me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes.FrozenPlayer;
import me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes.FrozenProjectile;
import me.xnuminousx.spirits.ability.api.SpiritAbility;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class TimeShatter extends SpiritAbility implements AddonAbility {


    private static long maxDuration = 100;

    private static long minDuration = 60;

    private static long chargeTime = 80;

    private static int maxRange = 12;

    private static int minRange = 8;

    private static long cooldown = 8000;

    private static final Material[] blocks = { Material.AIR, Material.BEDROCK, Material.CHEST, Material.TRAPPED_CHEST, Material.OBSIDIAN, Material.PORTAL, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME, Material.FIRE, Material.WALL_SIGN, Material.SIGN_POST, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.BANNER, Material.WALL_BANNER, Material.DROPPER, Material.FURNACE, Material.DISPENSER, Material.HOPPER, Material.BEACON, Material.BARRIER, Material.MOB_SPAWNER };

    public static HashSet<TempBlock> createZone(Location loc, int radius) {
        HashSet<TempBlock> tempBlocks = new HashSet<>();
        for (Location l : GeneralMethods.getCircle(loc, radius, 1, true, true, 0)) {
            if (!GeneralMethods.isSolid(l.getBlock())) {
                TempBlock tb;
                Block b = l.getBlock();
                int ch = (int) (Math.random() * 100);
                if (ch <= 50) {
                    tb = new TempBlock(b, Material.STAINED_GLASS, (byte) 2);
                } else {
                    tb = new TempBlock(b, Material.STAINED_GLASS, (byte) 3);
                }
                tempBlocks.add(tb);
            }
        }
        return tempBlocks;
    }

    private static Random random = new Random();

    private static void displaySphere(Player player, double rad, boolean crit) {
        Location loc = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(rad / 2).add(player.getEyeLocation().getDirection().multiply(1.5))).add(0, 0.2, 0);
        if (crit) loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 4);
        for (double[] r : maxSphere) {
            loc.add(r[0] * rad, r[1] * rad, r[2] * rad);
            int rand = random.nextInt(3);
            switch (rand) {
                case 0:
                    GeneralMethods.displayColoredParticle(loc, "9400ff");
                    break;
                case 1:
                    GeneralMethods.displayColoredParticle(loc, "ff00eb");
                    break;
                case 2:
                    GeneralMethods.displayColoredParticle(loc, "1400ff");
                    break;
            }
            loc.subtract(r[0] * rad, r[1] * rad, r[2] * rad);
        }
    }

    private static double getMinMax(double min, double max, double ratio) {
        return Math.round(min + ((max - min) * ratio));
    }

    private int tick = 0;

    private Location location;

    private int duration;

    private int range;

    private int chargedTime = 0;

    private int currentStage = 1;

    private int freezeRange = 1;

    private boolean charging = true;

    private boolean contractZone = false;

    private boolean expandZone = true;

    private boolean finished = false;

    private Random rand = new Random();

    private HashSet<FrozenEntity> entities = new HashSet<>();

    private HashSet<TempBlock> stage = new HashSet<>();

    public HashSet<TempBlock> getStage() {
        return stage;
    }

    public void unFreeze(FrozenEntity entity) {
        entities.remove(entity);
    }

    public void freeze(Entity e) {
        boolean bool = true;
        for (FrozenEntity entity : entities) {
            if (entity.getEntity().equals(e)) {
                bool = false;
            }
        }
        if (bool) {
            if (e.getType().equals(EntityType.PLAYER)) {
                entities.add(new FrozenPlayer((Player) e, this));
            } else if (e instanceof LivingEntity) {
                entities.add(new FrozenLivingEntity((LivingEntity) e, this));
            } else if (e instanceof ProjectileSource) {
                entities.add(new FrozenProjectile((Projectile) e, this));
            } else {
                entities.add(new FrozenEntity(e, this));
            }
        }
    }

    public void removeAbility() {
        stage.forEach(TempBlock::revertBlock);
        stage.clear();
        new HashSet<>(entities).forEach(FrozenEntity::unFreeze);
        this.remove();
        finished = true;
    }

    public TimeShatter(Player player) {
        super(player);
        if (bPlayer.isOnCooldown(this)) return;
        TimeShatter oldShatter = getAbility(player, TimeShatter.class);
        if (oldShatter != null) {
            return;
        }
        start();
    }

    private boolean checkForCollision() {
        for (TimeShatter ts : CoreAbility.getAbilities(TimeShatter.class)) {
            if (ts.equals(this)) continue;
            if (ts.getLocation() == null) continue;
            if (Math.pow(ts.getCurrentStage() + this.getCurrentStage(), 2) >= ts.getLocation().distanceSquared(this.getLocation())) {
                ts.removeAbility();
                this.removeAbility();
                Vector vec = this.getLocation().clone().subtract(ts.getLocation()).toVector().normalize();
                createExplosion(ts.getLocation().add(vec.multiply(ts.getCurrentStage())), 10, 4);
                return true;
            }
        }
        return false;
    }

    @Override
    public void progress() {
        if (finished) return;
        if (!player.isOnline() || player.isDead() || bPlayer.isChiBlocked()) {
            removeAbility();
            return;
        }
        tick++;
        if (charging) {
            if (!bPlayer.getBoundAbilityName().equals("TimeShatter")) {
                removeAbility();
                return;
            }
            if (chargedTime == chargeTime && player.isSneaking()) {
                if (tick % 3 == 0) displaySphere(player, 2, true);
            } else if (player.isSneaking()) {
                if (tick % 3 == 0) {
                    float effec = (float) chargedTime / chargeTime;
                    effec = effec * 2;
                    displaySphere(player, effec, false);
                }
                chargedTime++;
            } else {
                this.location = player.getLocation().clone();
                bPlayer.addCooldown(this);
                charging = false;
                float effec = (float) chargedTime / chargeTime;
                duration = (int) getMinMax(minDuration, maxDuration, effec);
                range = (int) getMinMax(minRange, maxRange, effec);
                stage = createZone(location, currentStage);
                location.getWorld().playSound(location, Sound.BLOCK_ENDERCHEST_OPEN, 1F, 1F);
                player.chat(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ZA WARUDO!");
                tick = 0;
            }
        } else {
            float effec = (float) tick / duration;
            if (tick % getMinMax(1, 20, 1 - (float) tick / duration) == 0) {
                location.getWorld().playSound(location, Sound.BLOCK_ANVIL_PLACE, 1F, (float) Math.round(5 + (15 * (effec))) / 10);
            }
            if (expandZone) {
                if (tick > range) {
                    expandZone = false;
                } else {
                    expandZone();
                }
            } else if (contractZone) {
                if (currentStage == 1) {
                    removeAbility();
                    return;
                }
                contractZone();
            } else if (duration - range <= tick) {
                startContract();
            }
            for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, freezeRange - 1)) {
                if (e.equals(player) || e.getType().equals(EntityType.FALLING_BLOCK)) continue;
                freeze(e);
            }
        }
    }

    private void expandZone() {
        stage.forEach(TempBlock::revertBlock);
        stage.clear();
        currentStage++;
        freezeRange = currentStage;
        location.getWorld().playSound(location, Sound.ENTITY_ENDERDRAGON_FLAP, 1F, (float) Math.round(5 + (15 * (1 - (float) currentStage / range))) / 10);
        stage = createZone(location, currentStage);
    }

    private void contractZone() {
        stage.forEach(TempBlock::revertBlock);
        stage.clear();
        currentStage--;
        location.getWorld().playSound(location, Sound.ENTITY_ENDERDRAGON_FLAP, 1F, (float) Math.round(5 + (15 * (1 - (float) currentStage / range))) / 10);
        freezeRange = currentStage;
        stage = createZone(location, currentStage);
    }

    public void startContract() {
        location.getWorld().playSound(location, Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
        contractZone = true;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void explosion(Location loc) {
        ParticleEffect.FLAME.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
        ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
        ParticleEffect.FIREWORKS_SPARK.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
        ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, loc, 257D);
        ParticleEffect.EXPLOSION_HUGE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 5, loc, 257D);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
    }

    private void createExplosion(Location loc, int size, double damage) {
        explosion(loc);
        loc.getWorld().createExplosion(loc, 0.0F);
        for (Location l : GeneralMethods.getCircle(loc, size, size, false, true, 0)) {
            if (TempBlock.isTempBlock(l.getBlock())) {
                TempBlock.revertBlock(l.getBlock(), Material.AIR);
                TempBlock.removeBlock(l.getBlock());
            }
            if (!isTransparent(l.getBlock()) && (!(Arrays.asList(blocks).contains(l.getBlock().getType()))) && (!(GeneralMethods.isRegionProtectedFromBuild(player, "Combustion", l)))) {
                new RegenTempBlock(l.getBlock(), Material.AIR, (byte) 0, 10000, false);
                placeRandomBlock(l);
            }
        }
        for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, size)) {
            if (e instanceof LivingEntity) {
                DamageHandler.damageEntity(e, damage, this);
            }
        }
    }

    private void placeRandomBlock(Location l) {
        int chance = rand.nextInt(3);
        if (!(l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ()).getType().isSolid()))
            return;
        Material block = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ()).getType();
        if (chance == 0)
            l.getBlock().setType(block);
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "TimeShatter";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    private static double[][] maxSphere = new double[134][3];

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new TimeShatterListener(), ProjectKorra.plugin);
        int arrayCounter = 0;
        for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
            double radius = Math.sin(i);
            double y = Math.cos(i);
            double amount = Math.PI / (radius * 10);
            for (double a = 0; a < Math.PI * 2; a+= amount) {
                double x = Math.cos(a) * radius;
                double z = Math.sin(a) * radius;
                maxSphere[arrayCounter] = new double[] {x, y, z};
                arrayCounter++;
            }
        }
    }

    @Override
    public void stop() {
        for (TimeShatter t : CoreAbility.getAbilities(TimeShatter.class)) {
            t.removeAbility();
        }
        super.remove();
    }

    @Override
    public String getAuthor() {
        return "DomiRusz24";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
