package me.domirusz24.pk.timeshatter.timeshatter;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.TempBlock;
import me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes.FrozenEntity;
import me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes.FrozenLivingEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.sql.Time;
import java.util.HashSet;

public class TimeShatterListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }

        if (BendingPlayer.getBendingPlayer(event.getPlayer()).getBoundAbilityName().equalsIgnoreCase("TimeShatter") && !event.getPlayer().isSneaking()) {
            new TimeShatter(event.getPlayer());
        }
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
        } else if (event.isSneaking()) {
            if (BendingPlayer.getBendingPlayer(event.getPlayer()).getBoundAbilityName().equalsIgnoreCase("TimeShatter")) {
                new TimeShatter(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDestroy(BlockBreakEvent event) {
        for (TimeShatter t : CoreAbility.getAbilities(TimeShatter.class)) {
            for (TempBlock b : t.getStage()) {
                if (b.getBlock().equals(event.getBlock())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onAni(PlayerAnimationEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getEntity().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onGrow(VehicleEnterEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getEntered().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getEntity().getUniqueId().toString())) {
            FrozenEntity.FrozenEntities.get(event.getEntity().getUniqueId().toString()).unFreeze();
            return;
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            FrozenEntity.FrozenEntities.get(event.getPlayer().getUniqueId().toString()).unFreeze();
            return;
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getPlayer().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        FrozenEntity fe = FrozenEntity.FrozenEntities.get(event.getEntity().getUniqueId().toString());
        if (fe != null) {
            if (fe instanceof FrozenLivingEntity) {
                LivingEntity livingEntity = (LivingEntity) event.getEntity();
                livingEntity.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, livingEntity.getEyeLocation().add(0, 0.5, 0), 5);
                ((FrozenLivingEntity) fe).damage(event.getDamage());
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        if (FrozenEntity.FrozenEntities.containsKey(event.getDamager().getUniqueId().toString())) {
            event.setCancelled(true);
            return;
        }
    }
}
