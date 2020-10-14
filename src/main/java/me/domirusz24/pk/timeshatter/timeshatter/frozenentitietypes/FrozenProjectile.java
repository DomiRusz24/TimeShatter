package me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes;

import com.projectkorra.projectkorra.util.TempBlock;
import me.domirusz24.pk.timeshatter.timeshatter.TimeShatter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.graalvm.compiler.lir.LIRInstruction;

public class FrozenProjectile extends FrozenEntity {

    private Projectile projectile;
    private Location location;
    private ProjectileSource source;
    private Vector vector;

    public FrozenProjectile(Projectile projectile, TimeShatter ability) {
        super(projectile, ability);
        this.projectile = projectile;
        this.source = this.projectile.getShooter();
        vector = projectile.getVelocity();
        projectile.setBounce(false);
        location = projectile.getLocation();
        TempBlock tb = new TempBlock(projectile.getLocation().add(projectile.getLocation().getDirection().multiply(1)).getBlock(), Material.BARRIER, (byte) 0);
        tb.setRevertTime(50);
    }

    @Override
    public void unFreeze() {
        getAbility().unFreeze(this);
        FrozenEntities.remove(projectile.getUniqueId().toString());
        if (projectile.isDead()) return;
        projectile.remove();
        Projectile p = location.getWorld().spawn(location, projectile.getClass());
        p.setShooter(source);
        p.setBounce(false);
        p.setVelocity(vector);
    }
}
