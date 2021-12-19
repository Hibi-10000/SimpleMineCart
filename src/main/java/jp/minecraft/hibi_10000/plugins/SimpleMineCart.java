package jp.minecraft.hibi_10000.plugins;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SimpleMineCart extends JavaPlugin implements Listener {
	String ver = getDescription().getVersion();
	FileConfiguration config = getConfig();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();
		getServer().getPluginManager().registerEvents(this,this);
	}

	@EventHandler
	public  void onVehicleMove(VehicleMoveEvent e) {
		if (e.getVehicle() instanceof Minecart) {
			if ((!e.getVehicle().getPassengers().isEmpty())) {
				if (e.getVehicle().getPassengers().get(0) instanceof Player) {
					cartMove(e);
				}
			} else if (e.getVehicle().hasMetadata("smc-control")) {
				cartMove(e);
			}
		}
	}

	public void cartMove(VehicleMoveEvent e) {
		Vector old = e.getVehicle().getVelocity();
		int x = 0; if (old.getX()< 0) x = -2; if (old.getX()> 0) x = 2;
		int y= 0; if (old.getY()< 0) y = -2; if (old.getY()> 0) y = 2;
		int z = 0; if (old.getZ()< 0) z = -2; if (old.getZ()> 0) z = 2;

		Location downlocf = e.getFrom().clone();
		downlocf.setY(downlocf.getY() - 1);
		Location downloct = e.getTo().clone();
		downloct.setY(downloct.getY() - 1);
		if (downlocf.getBlock().getType().name().equalsIgnoreCase((String) config.get("Block.SpeedUp")) && !downlocf.getBlock().isBlockPowered()) {
			e.getVehicle().setVelocity(new Vector(x*2, y*2, z*2));
			e.getVehicle().removeMetadata("stopped",this);
			e.getVehicle().removeMetadata("inverted",this);
		} else if (downloct.getBlock().getType().name().equalsIgnoreCase((String) config.get("Block.Stop")) && !downloct.getBlock().isBlockPowered()) {
			if (!e.getVehicle().hasMetadata("stopped")) {
				e.getVehicle().setVelocity(new Vector(0, 0, 0));
				e.getVehicle().setMetadata("stopped", new FixedMetadataValue(this, ""));
			} else {
				e.getVehicle().setVelocity(new Vector(x, y, z));
			}
			e.getVehicle().removeMetadata("inverted",this);
		} else if (downlocf.getBlock().getType().name().equalsIgnoreCase((String) config.get("Block.Inversion")) && !downlocf.getBlock().isBlockPowered()) {
			if (!e.getVehicle().hasMetadata("inverted")) {
				e.getVehicle().setVelocity(new Vector(old.getX() * -1.0, old.getY() * -1.0, old.getZ() * -1.0));
				e.getVehicle().setMetadata("inverted", new FixedMetadataValue(this, ""));
			} else {
				x = 0; if (old.getX()< 0) x = -2; if (old.getX()> 0) x = 2;
				y= 0; if (old.getY()< 0) y = -2; if (old.getY()> 0) y = 2;
				z = 0; if (old.getZ()< 0) z = -2; if (old.getZ()> 0) z = 2;
				e.getVehicle().setVelocity(new Vector(x, y, z));
			}
			e.getVehicle().removeMetadata("stopped",this);
		} else {
			e.getVehicle().removeMetadata("stopped",this);
			e.getVehicle().removeMetadata("inverted",this);
		}
	}

	/*@EventHandler
	public  void  onVehicleEntityCollision(VehicleEntityCollisionEvent e) {
		if (e.getEntity().getType() == EntityType.MINECART && e.getVehicle().getType() == EntityType.MINECART) {
			if (e.getEntity().getVehicle().getPassengers().isEmpty() && !e.getVehicle().getPassengers().isEmpty()) {
				e.getEntity().getVehicle().
			}
		}
	}*/

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("simpleminecart.command.use")) {
			sender.sendMessage("§a[SimpleMineCart] §c権限が不足しています。");
			return false;
		}
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("smc")) {
			if (!(args.length == 1)) {
				sender.sendMessage("§a[SimpleMineCart] §cコマンドが間違っています。\"/smc help\"で使用法を表示します。 ");
				return false;
			}

			if (args[0].equalsIgnoreCase("help")) {
				if (!sender.hasPermission("simpleminecart.command.help")) {
					sender.sendMessage("§a[SimpleMineCart] §c権限が不足しています。");
					return false;
				}
				sender.sendMessage("§a[SimpleMineCart] §bVersion" + ver + " §6help");
				sender.sendMessage(" §b[Command]  (権限は §6simpleminecart.§6command.§b +括弧内)");
				sender.sendMessage(" §b- /smc help      ヘルプを表示します。                   (§6help§b)");
				sender.sendMessage(" §b- /smc reload    ConfigをReloadします。                   (§6reload§b)");
				sender.sendMessage(" §b- /smc on       トロッコ単体ののSMC機能をONにします。 (§6smctoggle§b)");
				sender.sendMessage(" §b- /smc off       トロッコ単体ののSMC機能をOFFにします。(§6smctoggle§b)");
				return true;
			}

			if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("simpleminecart.command.reload")) {
					sender.sendMessage("§a[SimpleMineCart] §c権限が不足しています。");
					return false;
				}
				reloadConfig();
				config = getConfig();
				sender.sendMessage("§a[SimpleMineCart] §bConfigをリロードしました。");
				return true;
			} else if (args[0].equalsIgnoreCase("on")) {
				if (!sender.hasPermission("simpleminecart.command.smctoggle")) {
					sender.sendMessage("§a[SimpleMineCart] §c権限が不足しています。");
					return false;
				}
				int i = 0;
				for (Entity e : p.getWorld().getNearbyEntities(p.getTargetBlock(null, 10).getLocation(), 1, 1, 1, Minecart.class::isInstance)) {
					e.setMetadata("smc-control", new FixedMetadataValue(this, ""));
					i++;
				}
				if (i > 0) {
					sender.sendMessage("§a[SimpleMineCart] §b" + i + "個のトロッコのSMC機能をONにしました。");
				} else {
					sender.sendMessage("§a[SimpleMineCart] §c設定するトロッコを見た状態で実行してください。");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("off")) {
				if (!sender.hasPermission("simpleminecart.command.smctoggle")) {
					sender.sendMessage("§a[SimpleMineCart] §c権限が不足しています。");
					return false;
				}
				int i = 0;
				for (Entity e : p.getWorld().getNearbyEntities(p.getTargetBlock(null, 10).getLocation(), 1, 1, 1, Minecart.class::isInstance)) {
					e.removeMetadata("smc-control", this);
					i++;
				}
				if (i > 0) {
					sender.sendMessage("§a[SimpleMineCart] §b" + i + "個のトロッコのSMC機能をOFFにしました。");
				} else {
					sender.sendMessage("§a[SimpleMineCart] §c設定するトロッコを見た状態で実行してください。");
				}
				return true;
			}
		}
		sender.sendMessage("§a[SimpleMineCart] §cコマンドが間違っています。\"/smc help\"で使用法を表示します。 ");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if (cmd.getName().equalsIgnoreCase("smc")) {
			list.add("help");
			list.add("reload");
			list.add("on");
			list.add("off");
			return list;
		}
		return null;
	}
}
