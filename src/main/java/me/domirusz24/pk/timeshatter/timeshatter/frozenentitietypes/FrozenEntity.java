package me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.domirusz24.pk.timeshatter.timeshatter.TimeShatter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class FrozenEntity {

    public static HashMap<String, FrozenEntity> FrozenEntities = new HashMap<>();

    private Entity entity;
    private TimeShatter ability;
    private Vector v;

    public FrozenEntity(Entity entity, TimeShatter ability) {
        this.entity = entity;
        this.ability = ability;
        this.v = entity.getVelocity().clone();
        entity.setGravity(false);
        entity.setVelocity(new Vector(0, 0, 0));
        FrozenEntities.put(entity.getUniqueId().toString(), this);
    }

    public void unFreeze() {
        ability.unFreeze(this);
        entity.setGravity(true);
        FrozenEntities.remove(entity.getUniqueId().toString());
        entity.setVelocity(v);
    }

    public Entity getEntity() {
        return entity;
    }

    public TimeShatter getAbility() {
        return ability;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FrozenEntity) return ((FrozenEntity) obj).getEntity().equals(this.getEntity());
        return super.equals(obj);
    }
}
