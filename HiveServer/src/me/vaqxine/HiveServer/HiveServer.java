package me.vaqxine.HiveServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.vaqxine.CommunityMechanics.CommunityMechanics;
import me.vaqxine.Hive.Hive;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.fusesource.jansi.Ansi;

public class HiveServer extends JavaPlugin implements Listener {

	public static String rootDir = "";
	
	Logger log = Logger.getLogger("Minecraft");
	public void onEnable() {
		setSystemPath();

		log.info(Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString() + "**************************");
		log.info(Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString() + "[HIVE (Server Edition)] has been enabled.");
		log.info(Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString() + "**************************" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
	}

	public void onDisable() {
		log.info(Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "**************************");
		log.info(Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "[HIVE (Server Edition)] has been disabled.");
		log.info(Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "**************************" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
	}

	public void setSystemPath(){
		CodeSource codeSource = HiveServer.class.getProtectionDomain().getCodeSource();
		File jarFile = null;
		try {jarFile = new File(codeSource.getLocation().toURI().getPath());} catch (URISyntaxException e1) {}
		rootDir = jarFile.getParentFile().getPath();
		rootDir = rootDir.substring(0, rootDir.indexOf("/plugins"));
	}

	public boolean isThisRootMachine(){
		File f = new File("key");
		if(f.exists()){return true;}
		else{return false;}
	}	


	public void send8008Packet(String input, String server_ip, boolean all){

		Socket kkSocket = null;
		PrintWriter out = null;

		List<String> servers = new ArrayList<String>();
		if(server_ip != null){
			servers.add(server_ip);
		}
		if(all){
			for(String s : CommunityMechanics.server_list.values()){
				servers.add(s);
			}
		}
		
		for(String s : servers){
			// s == IP of server.a
			try {
				try {
					kkSocket = new Socket();
					kkSocket.connect(new InetSocketAddress(s, 8008), 2500);
					out = new PrintWriter(kkSocket.getOutputStream(), true);
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					log.info(Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "[HIVE (Server Edition)] Failed to send payload to server @ " + s + " ; this server may be offline.");
					continue;
				}

				out.println(input);
				out.close();
				kkSocket.close();
				log.info(Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString() + "[HIVE (SERVER Edition)] Sent payload to " + s + "..." + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
			} catch (IOException e) {
				e.printStackTrace();
				log.info(Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "[HIVE (Server Edition)] Failed to send payload to server @ " + s + "");
				continue;
			}

		}
	}

	public void sendProxyShutdown(){
		Socket kkSocket = null;
		PrintWriter out = null;
		try {

			kkSocket = new Socket();
			//kkSocket.bind(new InetSocketAddress(Hive.local_IP, Hive.transfer_port+1));
			kkSocket.connect(new InetSocketAddress(Hive.Proxy_IP, Hive.transfer_port), 2000);
			out = new PrintWriter(kkSocket.getOutputStream(), true);

			out.println("[cycle]");

		} catch (IOException e) {
			e.printStackTrace();
		}

		if(out != null){
			out.close();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

		if(cmd.getName().equalsIgnoreCase("classicrollout")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				p.sendMessage(ChatColor.RED + "You cannot issue this command from anywhere but the console window.");
				return true;
			}

			if(args.length != 1){
				log.info("Invalid Syntax. /rollout <IP/*>");
				return true;
			}
			
			String ip = args[0];

			if(isThisRootMachine()){
				if(ip.equalsIgnoreCase("*")){
					send8008Packet("@rollout@", null, true);
					//CommunityMechanics.sendPacketCrossServer("@rollout@", -1, true);
					sendProxyShutdown();
				}
				else{
					//CommunityMechanics.sendPacketCrossServer("@rollout@", args[0]);
					send8008Packet("@rollout@", args[0], false);
				}
			}
			
			if(isThisRootMachine()){
				for(Player p : getServer().getOnlinePlayers()){
					p.saveData();
					p.kickPlayer("Launching a Content Patch to ALL #DungeonRealms Servers...");
				}				
				
				World w = Bukkit.getWorlds().get(0);
				Bukkit.unloadWorld(w, true);
				
				Bukkit.shutdown();
				return true;
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("rollout")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				p.sendMessage(ChatColor.RED + "You cannot issue this command from anywhere but the console window.");
				return true;
			}

			if(args.length != 1){
				log.info("Invalid Syntax. /rollout <IP/*>");
				return true;
			}
			
			String ip = args[0];

			if(isThisRootMachine()){
				if(ip.equalsIgnoreCase("*")){
					//send8008Packet("@rollout@", null, true);
					CommunityMechanics.sendPacketCrossServer("@rollout@", -1, true);
					sendProxyShutdown();
				}
				else{
					CommunityMechanics.sendPacketCrossServer("@rollout@", args[0]);
					//send8008Packet("@rollout@", args[0], false);
				}
			}
			
			if(isThisRootMachine()){
				for(Player p : getServer().getOnlinePlayers()){
					p.saveData();
					p.kickPlayer("Launching a Content Patch to ALL #DungeonRealms Servers...");
				}				
				
				World w = Bukkit.getWorlds().get(0);
				Bukkit.unloadWorld(w, true);
				
				Bukkit.shutdown();
				return true;
			}
		}

		if(cmd.getName().equalsIgnoreCase("cycle")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				p.sendMessage(ChatColor.RED + "You cannot issue this command from anywhere but the console window.");
				return true;
			}

			if(args.length != 1){
				log.info("Invalid Syntax. /cycle <IP/*>");
				return true;
			}
			
			String ip = args[0];
			
			if(isThisRootMachine()){
				if(ip.equalsIgnoreCase("*")){
					//send8008Packet("@restart@", null, true);
					CommunityMechanics.sendPacketCrossServer("@restart@", -1, true);
					sendProxyShutdown();
					return true;
				}
				else if(ip.equalsIgnoreCase("proxy")){
					sendProxyShutdown();
					return true;
				}
				else{
					CommunityMechanics.sendPacketCrossServer("@rollout@", args[0]);
					//send8008Packet("@restart@", args[0], false);
					return true;
				}
			}
		}

		return true;
	}

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	public void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
	
	public static final void zipDirectory( File directory, File zip ) throws IOException {
		ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zip ) );
		zip( directory, directory, zos );
		zos.close();
	}

	private static final void zip(File directory, File base,
			ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isDirectory()) {
				zip(files[i], base, zos);
			} else {
				FileInputStream in = new FileInputStream(files[i]);
				ZipEntry entry = new ZipEntry(files[i].getPath().substring(
						base.getPath().length() + 1));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

}

