package Ghreborn.model.players.skills.woodcutting;

import Ghreborn.Server;
import Ghreborn.event.CycleEvent;
import Ghreborn.model.players.Client;
import Ghreborn.model.players.skills.Skill;

public class Woodcutting {
	
	private static final Woodcutting INSTANCE = new Woodcutting();
	
	public void chop(Client player, int objectId, int x, int y) {
		Tree tree = Tree.forObject(objectId);
		player.face(x, y);
		if (player.playerLevel[player.playerWoodcutting] < tree.getLevelRequired()) {
			player.sendMessage("You do not have the woodcutting level required to cut this tree down.");
			return;
		}
		Hatchet hatchet = Hatchet.getBest(player);
		if (hatchet == null) {
			player.sendMessage("You must have an axe and the level required to cut this tree down.");
			return;
		}
		if (player.getItems().freeSlots() == 0) {
			player.sendMessage("You must have at least one free inventory space to do this.");
			return;
		}
		if (Server.getGlobalObjects().exists(tree.getStumpId(), x, y)) {
			player.sendMessage("This tree has been cut down to a stump, you must wait for it to grow.");
			return;
		}
		player.getSkilling().stop();
		player.sendMessage("You swing your axe at the tree.");
		player.animation(hatchet.getAnimation());
		player.getSkilling().setSkill(Skill.WOODCUTTING);
		CycleEvent woodcuttingEvent = new WoodcuttingEvent(player, tree, hatchet, objectId, x , y);
		player.getSkilling().add(woodcuttingEvent, 1);
	}
	
	public static Woodcutting getInstance() {
		return INSTANCE;
	}

}
