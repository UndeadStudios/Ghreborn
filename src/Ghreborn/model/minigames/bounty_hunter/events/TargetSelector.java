package Ghreborn.model.minigames.bounty_hunter.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import Ghreborn.core.PlayerHandler;
import Ghreborn.event.CycleEventContainer;
import Ghreborn.model.minigames.bounty_hunter.BountyHunter;
import Ghreborn.model.minigames.bounty_hunter.Target;
import Ghreborn.model.minigames.bounty_hunter.TargetEvent;
import Ghreborn.model.minigames.bounty_hunter.TargetState;
import Ghreborn.model.players.Client;
import Ghreborn.model.players.Player;
import Ghreborn.util.Misc;

/**
 * 
 * @author Jason MacKeigan
 * @date Nov 13, 2014, 2:27:05 PM
 */
public class TargetSelector extends TargetEvent {

	public TargetSelector(BountyHunter bountyHunter) {
		super(bountyHunter);
	}

	@Override
	public void execute(CycleEventContainer container) {
		BountyHunter bh = super.bountyHunter;
		Client player = bh.getPlayer();
		if (!isExecutable()) {
			container.stop();
			return;
		}
		Predicate<Client> viableTarget = t -> t != null && t != player && t.inWild() && !t.getBH().hasTarget()
				&& t.wildLevel > 0 && !t.getBH().getTargetState().isPenalized() && !t.getBH().getTargetState()
					.hasKilledRecently() && !player.getPlayerKills().killedRecently(t.connectedFrom) &&
					!t.getPlayerKills().killedRecently(player.connectedFrom)
					&& !t.connectedFrom.equalsIgnoreCase(player.connectedFrom);
		List<Client> possibleTargets = new ArrayList<>(1);
		for (int levelOffset = 0; levelOffset < 10; levelOffset++) {
			final int level = levelOffset;
			//possibleTargets = PlayerHandler.getPlayers().stream().filter(viableTarget.and(
				//	t -> Misc.combatDifference(player, t) <= level)).collect(Collectors.toList());
			if (possibleTargets.size() > 0) {
				break;
			}
		}
		if (possibleTargets.size() <= 0) {
			return;
		}
		Optional<Client> randomTarget = Optional.of(possibleTargets.get(Misc.random(possibleTargets.size() - 1)));
		if (randomTarget.isPresent()) {
			Client target = randomTarget.get();
			assignTarget(player, target);
			assignTarget(target, player);
			container.stop();
			return;
		}
	}
	
	@Override
	public void stop() {
		if (Objects.nonNull(bountyHunter.getPlayer())) {
			bountyHunter.setTargetState(TargetState.NONE);
		}
	}
	
	/**
	 * Determines if the selection event should be executed based on some
	 * conditions. 
	 * @return	if true, the event will start. Otherwise, the event should come to a halt.
	 */
	public boolean isExecutable() {
		BountyHunter bh = super.bountyHunter;
		Client player = bh.getPlayer();
		if (Objects.isNull(bh) || Objects.isNull(player) || player.disconnected) {
			return false;
		}
		if (bh.hasTarget()) {
			return false;
		}
		if (!player.inWild()) {
			return false;
		}
		if (bh.getTargetState().hasKilledRecently() || bh.getTargetState().isPenalized()) {
			return false;
		}
		//if (player.isInvisible()) {
			//return false;
	//	}
		return true;
	}
	
	private void assignTarget(Client player, Client target) {
		player.getBH().setTargetState(TargetState.SELECTED);
		player.getBH().setTarget(new Target(target.playerName));
		player.getBH().updateTargetUI();
		player.sendMessage("<col=FF0000>You've been assigned a target: "+Misc.capitalize(target.playerName)+"</col>");
		player.getPA().createPlayerHints(10, target.index);
	}
}
