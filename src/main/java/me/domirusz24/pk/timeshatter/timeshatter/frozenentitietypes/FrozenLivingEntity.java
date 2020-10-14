package me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.domirusz24.pk.timeshatter.timeshatter.TimeShatter;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FrozenLivingEntity extends FrozenEntity {

    private LivingEntity livingEntity;
    private double dmg = 0;

    public FrozenLivingEntity(LivingEntity livingEntity, TimeShatter ability) {
        super(livingEntity, ability);
        this.livingEntity = livingEntity;
        livingEntity.setCanPickupItems(false);
        livingEntity.setAI(false);
    }

    public void damage(double dmg) {
        this.dmg+= dmg;
    }

    @Override
    public void unFreeze() {
        super.unFreeze();
        livingEntity.setCanPickupItems(true);
        livingEntity.setAI(true);
        if (dmg != 0) {
            DamageHandler.damageEntity(livingEntity, dmg, getAbility());
            livingEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK, livingEntity.getLocation().add(0, 0.5, 0), 10);
        }
    }
}
