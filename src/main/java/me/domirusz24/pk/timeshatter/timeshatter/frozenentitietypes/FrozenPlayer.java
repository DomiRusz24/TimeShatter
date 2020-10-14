package me.domirusz24.pk.timeshatter.timeshatter.frozenentitietypes;

import com.projectkorra.projectkorra.BendingPlayer;
import me.domirusz24.pk.timeshatter.timeshatter.TimeShatter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FrozenPlayer extends FrozenLivingEntity {

    private Player player;

    public FrozenPlayer(Player player, TimeShatter ability) {
        super(player, ability);
        this.player = player;
        if (BendingPlayer.getBendingPlayer(player) != null) BendingPlayer.getBendingPlayer(player).blockChi();
    }

    @Override
    public void unFreeze() {
        super.unFreeze();
        if (BendingPlayer.getBendingPlayer(player) != null) BendingPlayer.getBendingPlayer(player).unblockChi();
    }
}
