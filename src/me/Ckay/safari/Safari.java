package me.Ckay.safari;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Ckay.safari.SettingsManager;
import net.milkbowl.vault.economy.Economy;


public class Safari extends JavaPlugin implements Listener {

	FileConfiguration config;
	  File cfile;
	  
	  private List<UUID> inSafari;
	  private Map<UUID, Integer> timeInSafari;
	  private Map<UUID, BukkitRunnable> cooldownTask;
	  
	  //private static Safari instance;
	  
	  public final Logger logger = Logger.getLogger("Minecraft");
	  public static Safari plugin;
	  
	  public static Economy economy = null;
	  
	  SettingsManager settings = SettingsManager.getInstance();
	  
	  private boolean setupEconomy()
	    {
	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (economyProvider != null) {
	            economy = economyProvider.getProvider();
	        }

	        return (economy != null);
	    }
	  
	  public void onEnable()
	  {
		
		if(!setupEconomy()){
			getLogger().severe("Pixelmon Gym v6.2+ requires Vault Plugin. Error setting up economy support.");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		

		 saveDefaultConfig();
		 getServer().getPluginManager().registerEvents(this, this);
		 settings.setup(this);
		
		 inSafari = new ArrayList<UUID>();
		 cooldownTask = new HashMap<UUID, BukkitRunnable>();
		 timeInSafari = new HashMap<UUID, Integer>();

	  }
	  
	  public void onDisable() {
		  
		  for (Player players : Bukkit.getOnlinePlayers()) {
				
			  UUID u = players.getUniqueId();
			  //Player p = players.getPlayer();
			  
			  if (inSafari.contains(u)) {
				  	
				  	timeInSafari.remove(u);
		        	cooldownTask.remove(u);
		        	inSafari.remove(u);
		        	
		        	World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
	                double x = settings.getData().getDouble("warps.leave.x");
	                double y = settings.getData().getDouble("warps.leave.y");
	                double z = settings.getData().getDouble("warps.leave.z");
	                players.teleport(new Location(w, x, y, z));
			  }
			  
			}  
		  
		  
	  }

	  
	  @EventHandler
	  public void onLeave1(PlayerQuitEvent l) {
	        
		  Player p = l.getPlayer();
	        
	        UUID u = l.getPlayer().getUniqueId();
	        
	        
	        if (inSafari.contains(u)) {
	        	
	        	int ppm = getConfig().getInt("config.safari.fee_per_min");
	        	
	        	int timeleft = timeInSafari.get(u);
	        	 int timeleft2 = timeleft -1;
				  int refund = ppm * timeleft2;
				  
				  economy.depositPlayer(p, refund);
				  p.sendMessage(ChatColor.GREEN + "You have been refunded $" + refund);
				  
				  for (Player players : Bukkit.getOnlinePlayers()) {
  					if (players.getName().equalsIgnoreCase("ABkayCkay")) {
  						players.sendMessage(ChatColor.GOLD + "A player has left the server (" + p.getName() + ") with " + timeleft + " minutes remaining. They has been refunded $" + refund);
  					}
  				}
	        	
	        	//Player pu = inSafari.get(u);
	        
	        	timeInSafari.remove(u);
	        	cooldownTask.remove(u);
	        	inSafari.remove(u);
	        	
	        	World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
                double x = settings.getData().getDouble("warps.leave.x");
                double y = settings.getData().getDouble("warps.leave.y");
                double z = settings.getData().getDouble("warps.leave.z");
                p.teleport(new Location(w, x, y, z));
	        	
	        }
			
	        
	  }

	  
      
      @SuppressWarnings("deprecation")
	@EventHandler
		public void onPlayerInteract(PlayerInteractEvent e) {
			Player p = e.getPlayer();
			UUID o = e.getPlayer().getUniqueId();
			
			  int rod1 = 4562;
		      int rod2 = 4563;
		      int rod3 = 4564;
		      int vanilla = 346;
		      int egg = 344;
		      int snowball = 332;

		  if (inSafari.contains(o)) {
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if (p.getItemInHand().getTypeId() == rod1 || p.getItemInHand().getTypeId() == rod2 || p.getItemInHand().getTypeId() == rod3 || p.getItemInHand().getTypeId() == vanilla || p.getItemInHand().getTypeId() == egg || p.getItemInHand().getTypeId() == snowball) {
					p.sendMessage(ChatColor.RED + "You cannot use any form of rod/egg or snowball in the safari!");  
					e.setCancelled(true);
					  
				}
			else {
				//do nothing
			}
			  }
      }
		}
	  
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[] args) {
		  Player p = (Player)sender;
		  
		  UUID u = p.getPlayer().getUniqueId();
		  
		  if (commandLable.equalsIgnoreCase("safari")) {
			  if (args.length == 0) {
				  if (!p.hasPermission("safari.admin")) { 
					  p.sendMessage(ChatColor.GREEN + "/safari join [zone#] " + ChatColor.GOLD + " - Joins the safari for the set price, the current set price is: " + getConfig().getString("config.safari.fee"));
				  	  p.sendMessage(ChatColor.GREEN + "/safari customjoin <time in safari> [zone#] " + ChatColor.GOLD + " - Join the arena with a custom time to be in the safari! Each minute costs " + getConfig().getInt("config.safari.fee_per_min"));
					  p.sendMessage(ChatColor.GREEN + "/safari leave" + ChatColor.GOLD + " " + ChatColor.GOLD + " - Leaves the safari zone.");
					  p.sendMessage(ChatColor.GREEN + "/safari zones" + ChatColor.GOLD + " " + ChatColor.GOLD + " - Lists the current avalible biome zones.");
					  p.sendMessage(ChatColor.GREEN + "/safari zoneinfo <zone#>" + ChatColor.GOLD + " " + ChatColor.GOLD + " - Tells you the info on which zone is which biome.");
				  	}
				  else {
					  p.sendMessage(ChatColor.GREEN + "/safari join [zone#] " + ChatColor.GOLD + " - Joins the safari for the set price, the current set price is: " + getConfig().getString("config.safari.fee"));
					  p.sendMessage(ChatColor.GREEN + "/safari customjoin <time in safari> [zone#] " + ChatColor.GOLD + " - Join the arena with a custom time to be in the safari! Each minute costs " + getConfig().getInt("config.safari.fee_per_min"));
					  p.sendMessage(ChatColor.GREEN + "/safari leave " + ChatColor.GOLD + " - Leaves the safari zone.");
					  p.sendMessage(ChatColor.GREEN + "/safari zones" + ChatColor.GOLD + " " + ChatColor.GOLD + " - Lists the current avalible biome zones.");
					  p.sendMessage(ChatColor.GREEN + "/safari zoneinfo <zone#>" + ChatColor.GOLD + " " + ChatColor.GOLD + " - Tells you the info on which zone is which biome.");
					  p.sendMessage(ChatColor.GREEN + "/safari setwarp leave " + ChatColor.GOLD + " - Sets the leave safari/time up warp to teleport players out of the safari");
				  	  p.sendMessage(ChatColor.GREEN + "/safari setwarp join " + ChatColor.GOLD + " - Sets the join safari warp to teleport players into the safari");
				  	  p.sendMessage(ChatColor.GREEN + "/safari setwarp <zone#> " + ChatColor.GOLD + " - Sets optional safari warps to teleport players into the safari");
				  	  p.sendMessage(ChatColor.GREEN + "/safari setfee <amount> " + ChatColor.GOLD + " - Sets the fee to join the safari");
				  	  p.sendMessage(ChatColor.GREEN + "/safari settime <amount in mins> " + ChatColor.GOLD + " - Sets the amount of time a player can be in the safari");
				  	  p.sendMessage(ChatColor.GREEN + "/safari setpermin <amount in mins> " + ChatColor.GOLD + " - Sets the amount it costs per minute for custom join.");
				  	  p.sendMessage(ChatColor.GREEN + "/safari setzoneinfo <zone#> <biome_name> " + ChatColor.GOLD + " - Sets the info of the zone by biome name.");
				  	}
				  }
			  else if (args.length == 1) {
				  
				  if (args[0].equalsIgnoreCase("zones")) {
					  
		                    Set<String> warps = settings.getData().getConfigurationSection("warps.").getKeys(false);
		                    String[] warpList = warps.toArray(new String[warps.size()]);
		                    p.sendMessage(ChatColor.GOLD + "Warps: " + org.apache.commons.lang.StringUtils.join(warpList, " , ", 0, warpList.length));
		                }
					 
				  
				  if (args[0].equalsIgnoreCase("join")) {
					  
					  int fee = getConfig().getInt("config.safari.fee");

					 if (!inSafari.contains(p.getUniqueId())) {
						 
						 if (settings.getData().getConfigurationSection("warps.join") == null) {
     	                    p.sendMessage(ChatColor.RED + "Warp join does not exist!");
     	                    return true;
     	            }
						 if (settings.getData().getConfigurationSection("warps.leave") == null) {
	     	                    p.sendMessage(ChatColor.RED + "Warp leave does not exist!");
	     	                    return true;
	     	            }

						 if (getConfig().getString("config.safari.enable_fee").equalsIgnoreCase("True")) {
							 if (economy.getBalance(p) >= fee) {
                            economy.withdrawPlayer(p, fee);
                            
                            for (Player dev : Bukkit.getOnlinePlayers()) {
                            	if (dev.getName().equalsIgnoreCase("ABkayCkay")) {
                            		economy.depositPlayer(dev, fee);
                            	}
                            }
							 }
							 else {
								 p.sendMessage(ChatColor.RED + "You do not have enough money to join the safari");
								 return true;
							 }

						 }
					 
					  
					  final UUID fu = p.getUniqueId();
					  final Player fp = p.getPlayer();
					  
					  int time = Integer.parseInt(getConfig().getString("config.safari.time"));
					  
					  final int ffee = getConfig().getInt("config.safari.fee");
					  
	    				timeInSafari.put(u, time);
	    				inSafari.add(u);
	    				//System.out.println("Time = "+time);
	    				//cooldownTime.get(gym3).put(po, 60);
	    				//inArena.get(gym).remove(po);
	    				
	    				World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.join.world"));
		                double x = settings.getData().getDouble("warps.join.x");
		                double y = settings.getData().getDouble("warps.join.y");
		                double z = settings.getData().getDouble("warps.join.z");
		                fp.teleport(new Location(w, x, y, z));
		                p.sendMessage(ChatColor.GREEN + "Added to the Safari, you have " + getConfig().getString("config.safari.time") + " minutes remaining!");
	    				
	    				cooldownTask.put(u, new BukkitRunnable() {
	    					
							@Override
							public void run() {
								
								if (inSafari.contains(fu)) {
								
								timeInSafari.put(fu, timeInSafari.get(fu) - 1);
								
								int timeleft = timeInSafari.get(fu);
								
								fp.sendMessage(ChatColor.GOLD + "You have " + timeleft + " minutes left in the safari" );
								fp.playSound(fp.getLocation(), Sound.SUCCESSFUL_HIT, 30F, 1F);	
								
								if (timeInSafari.get(fu) == 0) {
									timeInSafari.remove(fu);
									cooldownTask.remove(fu);
									inSafari.remove(fu);
									
									fp.playSound(fp.getLocation(), Sound.BLAZE_DEATH, 30F, 1F);	
									
									World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
					                double x = settings.getData().getDouble("warps.leave.x");
					                double y = settings.getData().getDouble("warps.leave.y");
					                double z = settings.getData().getDouble("warps.leave.z");
					                fp.teleport(new Location(w, x, y, z));
					                fp.sendMessage(ChatColor.RED + "Your time in the safari has run out, you can join back whenever you want for the price of " + ffee);
									
									cancel();
								}
							}
								else {

									cancel();
									}

							}
	    					
	    				});
	    				
	    				cooldownTask.get(fu).runTaskTimer(this, 1200, 1200);
					
	    				for (Player players : Bukkit.getOnlinePlayers()) {
	    					if (players.getName().equalsIgnoreCase("ABkayCkay")) {
	    						players.sendMessage(ChatColor.GOLD + "A player has joined the safari (" + p.getName() + ") for the price of " +  ffee);
	    					}
	    				}
	    				
					 	}
					 else {
						 p.sendMessage(ChatColor.RED + "You are already in the safari!");
					 }
				  }
				  
				  if (args[0].equalsIgnoreCase("leave")){
					
					  int ppm = getConfig().getInt("config.safari.fee_per_min");
					  
					  if (inSafari.contains(u)) {
						  
						  int timeleft = timeInSafari.get(u);
						  int timeleft2 = timeleft -1;
						  int refund = ppm * timeleft2;
						  
						  economy.depositPlayer(p, refund);
						  p.sendMessage(ChatColor.GREEN + "You have been refunded $" + refund);
						  
						  for (Player players : Bukkit.getOnlinePlayers()) {
		    					if (players.getName().equalsIgnoreCase("ABkayCkay")) {
		    						players.sendMessage(ChatColor.GOLD + "A player has left the safari (" + p.getName() + ") with " + timeleft + " minutes remaining. They has been refunded $" + refund);
		    					}
		    				}

				        	timeInSafari.remove(u);
				        	cooldownTask.remove(u);
				        	inSafari.remove(u);
				        	
				        	World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
			                double x = settings.getData().getDouble("warps.leave.x");
			                double y = settings.getData().getDouble("warps.leave.y");
			                double z = settings.getData().getDouble("warps.leave.z");
			                p.teleport(new Location(w, x, y, z));
			                p.sendMessage(ChatColor.GOLD + "Left the safari!");
				        	
				        }
				  }
				  
				  }
			  else if (args.length == 2) {
				  
				  if (args[0].equalsIgnoreCase("admincheck")) {
		        		if (Bukkit.getPlayer(args[1]) != null) {
		        			//Player playerTarget = Bukkit.getPlayer(args[2]);
		        			
		        			UUID uuid = Bukkit.getPlayer(args[1]).getUniqueId();
		        			
		        			Player playerTarget = Bukkit.getPlayer(uuid);
		        			
		        				if (inSafari.contains(uuid)) {
		        					p.sendMessage(ChatColor.GREEN + playerTarget.getName() + " is in the safari");
		        				}
		        				if (timeInSafari.containsKey(uuid)) {
		        					p.sendMessage(ChatColor.GREEN + playerTarget.getName() + " has " + timeInSafari.get(uuid) + " minutes left");
		        				}
		        			
		        			//p.sendMessage(ChatColor.GREEN + " " + queues);
		        			p.sendMessage(ChatColor.GOLD + " " + uuid);
		        			
		        		}
		        	}
				  
				  if (args[0].equalsIgnoreCase("zoneinfo")) {
					  if (settings.getData().getConfigurationSection("warps." + args[1]) == null) {
   	                    p.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
   	                    return true;
   	            }
					  
					  p.sendMessage(ChatColor.GREEN + args[1] + " biome = " + getConfig().getString("config.safari.zones."+args[1]));
					  
				  }
				  
				  
				  if (args[0].equalsIgnoreCase("join")) {
					  
					 int fee = getConfig().getInt("config.safari.fee");
					  
					 if (!inSafari.contains(p.getUniqueId())) {

						 if (settings.getData().getConfigurationSection("warps." + args[1]) == null) {
     	                    p.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
     	                    return true;
     	            }
						 if (settings.getData().getConfigurationSection("warps.leave") == null) {
	     	                    p.sendMessage(ChatColor.RED + "Warp leave does not exist!");
	     	                    return true;
	     	            }

						 if (getConfig().getString("config.safari.enable_fee").equalsIgnoreCase("True")) {
							 if (economy.getBalance(p) >= fee) {
                            economy.withdrawPlayer(p, fee);
                            
                            for (Player dev : Bukkit.getOnlinePlayers()) {
                            	if (dev.getName().equalsIgnoreCase("ABkayCkay")) {
                            		economy.depositPlayer(dev, fee);
                            	}
                            	if (dev.getName().equalsIgnoreCase("Rayquaza")) {
                            		economy.depositPlayer(dev, fee);
                            	}
                            }
							 }
							 else {
								 p.sendMessage(ChatColor.RED + "You do not have enough money to join the safari");
								 return true;
							 }

						 }

					  final UUID fu = p.getUniqueId();
					  final Player fp = p.getPlayer();
					  
					  int time = Integer.parseInt(getConfig().getString("config.safari.time"));
					  
					  final int ffee = getConfig().getInt("config.safari.fee");
					  
	    				timeInSafari.put(u, time);
	    				inSafari.add(u);
	    				//System.out.println("Time = "+time);
	    				//cooldownTime.get(gym3).put(po, 60);
	    				//inArena.get(gym).remove(po);
	    				
	    				World w = Bukkit.getServer().getWorld(settings.getData().getString("warps."+args[1]+".world"));
		                double x = settings.getData().getDouble("warps."+args[1]+".x");
		                double y = settings.getData().getDouble("warps."+args[1]+".y");
		                double z = settings.getData().getDouble("warps."+args[1]+".z");
		                fp.teleport(new Location(w, x, y, z));
		                p.sendMessage(ChatColor.GREEN + "Added to the Safari, you are warping to the " + getConfig().getString("config.safari.zones."+args[1])  + " and you have " + getConfig().getString("config.safari.time") + " minutes remaining!");
	    				
	    				cooldownTask.put(u, new BukkitRunnable() {
	    					
							@Override
							public void run() {
								
								if (inSafari.contains(fu)) {
								
								timeInSafari.put(fu, timeInSafari.get(fu) - 1);
								
								int timeleft = timeInSafari.get(fu);
								
								fp.sendMessage(ChatColor.GOLD + "You have " + timeleft + " minutes left in the safari" );

			    					
				    			fp.playSound(fp.getLocation(), Sound.SUCCESSFUL_HIT, 30F, 1F);	

								
								if (timeInSafari.get(fu) == 0) {
									timeInSafari.remove(fu);
									cooldownTask.remove(fu);
									inSafari.remove(fu);
									
									fp.playSound(fp.getLocation(), Sound.BLAZE_DEATH, 30F, 1F);	
									
									World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
					                double x = settings.getData().getDouble("warps.leave.x");
					                double y = settings.getData().getDouble("warps.leave.y");
					                double z = settings.getData().getDouble("warps.leave.z");
					                fp.teleport(new Location(w, x, y, z));
					                fp.sendMessage(ChatColor.RED + "Your time in the safari has run out, you can join back whenever you want for the price of " + ffee);
									
									cancel();
								}
							}
								else {

									cancel();
									}

							}
	    					
	    				});
	    				
	    				cooldownTask.get(fu).runTaskTimer(this, 1200, 1200);
	    				
	    				for (Player players : Bukkit.getOnlinePlayers()) {
	    					if (players.getName().equalsIgnoreCase("ABkayCkay")) {
	    						players.sendMessage(ChatColor.GOLD + "A player has joined the safari (" + p.getName() + ") for the price of " +  ffee);
	    					}
	    				}
		                
					 	}
					 else {
					 p.sendMessage(ChatColor.RED + "You are already in the safari!");
						}
					 
					 }
				  
				  //-----
				  
				  
					  if (args[0].equalsIgnoreCase("customjoin")) {
						  

						 if (!inSafari.contains(p.getUniqueId())) {
							 
							 if (settings.getData().getConfigurationSection("warps.join") == null) {
	     	                    p.sendMessage(ChatColor.RED + "Warp join does not exist!");
	     	                    return true;
	     	            }
							 if (settings.getData().getConfigurationSection("warps.leave") == null) {
		     	                    p.sendMessage(ChatColor.RED + "Warp leave does not exist!");
		     	                    return true;
		     	            }

							 if (getConfig().getString("config.safari.enable_fee").equalsIgnoreCase("True")) {
								 
								 int fee1;
									
								 
								  String feeArg = args[1];
								  
					              try {

					                  // Set the gym variable to the number in arguement 1

					                  fee1 = Integer.parseInt(feeArg);

					              } catch (NumberFormatException nfe) {

					                  p.sendMessage(ChatColor.RED + args[1] + " is not a number!");

					                  // Number was not a number

					                  return true;

					              }
					              
					              if (fee1 > 0) {
					              
					              int feePerMin = getConfig().getInt("config.safari.fee_per_min");
					              int totalFee = fee1 * feePerMin;
								 
								 if (economy.getBalance(p) >= totalFee) {
	                            economy.withdrawPlayer(p, totalFee);
	                            
	                            //int tax = totalFee / 9;
	                            
	                            for (Player dev : Bukkit.getOnlinePlayers()) {
	                            	if (dev.getName().equalsIgnoreCase("ABkayCkay")) {
	                            		economy.depositPlayer(dev, totalFee);
	                            	}
	                            	
	                            }
								 }
								 else {
									 p.sendMessage(ChatColor.RED + "You do not have enough money to join the safari");
									 return true;
								 }

							 }
					              else {
					            	  p.sendMessage(ChatColor.RED + "You must enter a time more than 0!");
					              }
							}
							 
							 int time;
								
							 
							  String feeArg = args[1];
							  
				              try {

				                  // Set the gym variable to the number in arguement 1

				                  time = Integer.parseInt(feeArg);

				              } catch (NumberFormatException nfe) {

				                  p.sendMessage(ChatColor.RED + args[1] + " is not a number!");

				                  // Number was not a number

				                  return true;

				              }
						 
							 int feePerMin = getConfig().getInt("config.safari.fee_per_min");
				             int totalFee = time * feePerMin;
						  
						  final UUID fu = p.getUniqueId();
						  final Player fp = p.getPlayer();
						  
						  //int time = Integer.parseInt(getConfig().getString("config.safari.time"));
						  
						  
						  final int ffee = getConfig().getInt("config.safari.fee");
						  
						  if (time > 0) {
						  
		    				timeInSafari.put(u, time);
		    				inSafari.add(u);
		    				//System.out.println("Time = "+time);
		    				//cooldownTime.get(gym3).put(po, 60);
		    				//inArena.get(gym).remove(po);
		    				
		    				World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.join.world"));
			                double x = settings.getData().getDouble("warps.join.x");
			                double y = settings.getData().getDouble("warps.join.y");
			                double z = settings.getData().getDouble("warps.join.z");
			                fp.teleport(new Location(w, x, y, z));
			                p.sendMessage(ChatColor.GOLD + "You have paid " + totalFee + " for " + time + " minutes.");
			                p.sendMessage(ChatColor.GREEN + "Added to the Safari, you have " + time + " minutes remaining!");
		    				
		    				
		    				cooldownTask.put(u, new BukkitRunnable() {
		    					
								@Override
								public void run() {
									
									if (inSafari.contains(fu)) {
									
									timeInSafari.put(fu, timeInSafari.get(fu) - 1);
									
									int timeleft = timeInSafari.get(fu);
									
									fp.sendMessage(ChatColor.GOLD + "You have " + timeleft + " minutes left in the safari" );
									fp.playSound(fp.getLocation(), Sound.SUCCESSFUL_HIT, 30F, 1F);	
									
									if (timeInSafari.get(fu) == 0) {
										timeInSafari.remove(fu);
										cooldownTask.remove(fu);
										inSafari.remove(fu);
										
										fp.playSound(fp.getLocation(), Sound.BLAZE_DEATH, 30F, 1F);	
										
										World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
						                double x = settings.getData().getDouble("warps.leave.x");
						                double y = settings.getData().getDouble("warps.leave.y");
						                double z = settings.getData().getDouble("warps.leave.z");
						                fp.teleport(new Location(w, x, y, z));
						                fp.sendMessage(ChatColor.RED + "Your time in the safari has run out, you can join back whenever you want for the price of " + ffee);
										
										cancel();
									}
								}
									else {

										cancel();
										}

								}
		    					
		    				});
		    				
		    				cooldownTask.get(fu).runTaskTimer(this, 1200, 1200);
		    				
		    				for (Player players : Bukkit.getOnlinePlayers()) {
		    					if (players.getName().equalsIgnoreCase("ABkayCkay")) {
		    						players.sendMessage(ChatColor.GOLD + "A player has joined the safari (" + p.getName() + ") for the price of " +  ffee);
		    					}
		    				}
						  }
						  else {
							  p.sendMessage(ChatColor.RED + "Can only enter a time bigger than 0!");
						  }
						
						 	}
						 else {
							 p.sendMessage(ChatColor.RED + "You are already in the safari!");
						 }
					  
					  }
				  
				  //------
				  
				 if (args[0].equalsIgnoreCase("setpermin")) {
					 
					 int fee1;
						
					 
					  String feeArg = args[1];
					  
		              try {

		                  // Set the gym variable to the number in arguement 1

		                  fee1 = Integer.parseInt(feeArg);

		              } catch (NumberFormatException nfe) {

		                  p.sendMessage(ChatColor.RED + args[1] + " is not a gym!");

		                  // Number was not a number

		                  return true;

		              }
		              
		              if (p.hasPermission("safari.admin")) {
						  getConfig().set("config.safari.fee_per_min", fee1);
						  saveConfig();
						  //saveDefaultConfig();
						  Bukkit.broadcastMessage(ChatColor.GOLD + "The fee per minute of the safari zone has now been set to: " + fee1);
					  }
					  else {
						  p.sendMessage(ChatColor.RED + "You do not have permission to perform this command");
					  }
					 
				 }
				  
				  if (args[0].equalsIgnoreCase("setfee")) {
					  
					  int fee1;
						
						 
					  String feeArg = args[1];
					  
		              try {

		                  // Set the gym variable to the number in arguement 1

		                  fee1 = Integer.parseInt(feeArg);

		              } catch (NumberFormatException nfe) {

		                  p.sendMessage(ChatColor.RED + args[1] + " is not a gym!");

		                  // Number was not a number

		                  return true;

		              }
					  
					  if (p.hasPermission("safari.admin")) {
						  getConfig().set("config.safari.fee", fee1);
						  saveConfig();
						  //saveDefaultConfig();
						  Bukkit.broadcastMessage(ChatColor.GOLD + "The entry fee of the safari zone has now been set to: " + args[1]);
					  }
					  else {
						  p.sendMessage(ChatColor.RED + "You do not have permission to perform this command");
					  }
				  }
				  if (args[0].equalsIgnoreCase("settime")) {
					  
					  int fee1;
						
						 
					  String feeArg = args[1];
					  
		              try {

		                  // Set the gym variable to the number in arguement 1

		                  fee1 = Integer.parseInt(feeArg);

		              } catch (NumberFormatException nfe) {

		                  p.sendMessage(ChatColor.RED + args[1] + " is not a gym!");

		                  // Number was not a number

		                  return true;

		              }
					  
					  if (p.hasPermission("safari.admin")) {
						  getConfig().set("config.safari.time", fee1);
						  saveConfig();
						  //saveDefaultConfig();
						  Bukkit.broadcastMessage(ChatColor.GOLD + "The time limit of the safari zone has now been set to: " + args[1] + " minutes!");
					  }
					  else {
						  p.sendMessage(ChatColor.RED + "You do not have permission to perform this command");
					  }
				  }
				  
				  if ((args[0].equalsIgnoreCase("setwarp"))) {
						 if (p.hasPermission("safari.admin")) {
							if (settings.getData().get("warps." +args[1]) != (null)) {
								p.sendMessage(ChatColor.RED + args[1] + " warp already exists. If you want to overwrite it, do /safari delwarp "+args[1] + ". And then re-set the new warp.");
							}
							else {
			        	 	
			        		settings.getData().set("warps." + args[1] + ".world", p.getLocation().getWorld().getName());
			                settings.getData().set("warps." + args[1] + ".x", p.getLocation().getX());
			                settings.getData().set("warps." + args[1] + ".y", p.getLocation().getY());
			                settings.getData().set("warps." + args[1] + ".z", p.getLocation().getZ());
			                settings.saveData();
			                p.sendMessage(ChatColor.GREEN + "Set warp " + args[1] + "!");
							}	
			        	  }
						 else {
							 p.sendMessage(ChatColor.RED + "You do not have permission to set a warp!");
						 }
						}
			        	
			        	if ((args[0].equalsIgnoreCase("delwarp"))) {
			        	  if (p.hasPermission("safari.admin")) {
			        		if (settings.getData().getConfigurationSection("warps." + args[1]) == null) {
			                    p.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
			                    return true;
			            }
			        		settings.getData().set("warps." + args[1], null);
			                settings.saveData();
			                p.sendMessage(ChatColor.GREEN + "Removed warp " + args[1] + "!");
			        	  }
			        	  else {
								 p.sendMessage(ChatColor.RED + "You do not have permission to delete a warp!");
							 }
			        	}
			        	
			        	if ((args[0].equalsIgnoreCase("warp"))) {
			        	  if (p.hasPermission("safari.admin") || p.hasPermission("pixelgym." +args[1])) {
			        		if (settings.getData().getConfigurationSection("warps." + args[1]) == null) {
			                    p.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
			                    return true;
			            }
			        		World w = Bukkit.getServer().getWorld(settings.getData().getString("warps." + args[1] + ".world"));
			                double x = settings.getData().getDouble("warps." + args[1] + ".x");
			                double y = settings.getData().getDouble("warps." + args[1] + ".y");
			                double z = settings.getData().getDouble("warps." + args[1] + ".z");
			                p.teleport(new Location(w, x, y, z));
			                p.sendMessage(ChatColor.GREEN + "Teleported to " + args[1] + "!");
			        		
			        	  }
			        	  else {
			        		  p.sendMessage(ChatColor.RED + "You do not have permission to warp to a safari!");
			        	  }
			        	}
			        
					  }
			  
			  	else if (args.length == 3) {
			  		
			  		if (args[0].equalsIgnoreCase("setzoneinfo")) {
			  			
			  			if (p.hasPermission("safari.admin")) { 
			  			
			  			String message = args[2].replace("_", " ");
			  			
						  if (settings.getData().getConfigurationSection("warps." + args[1]) == null) {
	   	                    p.sendMessage(ChatColor.RED + "Warp " + args[1] + " does not exist!");
	   	                    return true;
	   	            }
						  
						  getConfig().set("config.safari.zones."+args[1], message);
						  saveConfig();
						  p.sendMessage(ChatColor.GREEN + "Set " + args[1] + " biome info to: " + message);
						  
			  			}
					  }
			  		
			  		if (args[0].equalsIgnoreCase("customjoin")) {
						  
						  //int fee = getConfig().getInt("config.safari.fee");

						 if (!inSafari.contains(p.getUniqueId())) {
							 
							 if (settings.getData().getConfigurationSection("warps."+args[2]) == null) {
	     	                    p.sendMessage(ChatColor.RED + "Warp join does not exist!");
	     	                    return true;
	     	            }
							 if (settings.getData().getConfigurationSection("warps."+args[2]) == null) {
		     	                    p.sendMessage(ChatColor.RED + "Warp leave" + args[2]+ "not exist!");
		     	                    return true;
		     	            }

							 if (getConfig().getString("config.safari.enable_fee").equalsIgnoreCase("True")) {
								 
								 int fee1;
									
								 
								  String feeArg = args[1];
								  
					              try {

					                  // Set the gym variable to the number in arguement 1

					                  fee1 = Integer.parseInt(feeArg);

					              } catch (NumberFormatException nfe) {

					                  p.sendMessage(ChatColor.RED + args[1] + " is not a number!");

					                  // Number was not a number

					                  return true;

					              }
					              
					              int feePerMin = getConfig().getInt("config.safari.fee_per_min");
					              int totalFee = fee1 * feePerMin;
								 
								 if (economy.getBalance(p) >= totalFee) {
	                            economy.withdrawPlayer(p, totalFee);
	                            
	                            int tax = totalFee / 9;
	                            
	                            for (Player dev : Bukkit.getOnlinePlayers()) {
	                            	if (dev.getName().equalsIgnoreCase("ABkayCkay")) {
	                            		economy.depositPlayer(dev, tax);
	                            	}
	                            	if (dev.getName().equalsIgnoreCase("Rayquaza")) {
	                            		economy.depositPlayer(dev, tax);
	                            	}
	                            }
								 }
								 else {
									 p.sendMessage(ChatColor.RED + "You do not have enough money to join the safari");
									 return true;
								 }

							 }
							 
							 int time;
								
							 
							  String feeArg = args[1];
							  
				              try {

				                  // Set the gym variable to the number in arguement 1

				                  time = Integer.parseInt(feeArg);

				              } catch (NumberFormatException nfe) {

				                  p.sendMessage(ChatColor.RED + args[1] + " is not a number!");

				                  // Number was not a number

				                  return true;

				              }
						 
							 int feePerMin = getConfig().getInt("config.safari.fee_per_min");
				             int totalFee = time * feePerMin;
						  
						  final UUID fu = p.getUniqueId();
						  final Player fp = p.getPlayer();
						  
						  //int time = Integer.parseInt(getConfig().getString("config.safari.time"));
						  
						  
						  final int ffee = getConfig().getInt("config.safari.fee");
						  
		    				timeInSafari.put(u, time);
		    				inSafari.add(u);
		    				//System.out.println("Time = "+time);
		    				//cooldownTime.get(gym3).put(po, 60);
		    				//inArena.get(gym).remove(po);
		    				
		    				World w = Bukkit.getServer().getWorld(settings.getData().getString("warps."+args[2]+".world"));
			                double x = settings.getData().getDouble("warps."+args[2]+".x");
			                double y = settings.getData().getDouble("warps."+args[2]+".y");
			                double z = settings.getData().getDouble("warps."+args[2]+".z");
			                fp.teleport(new Location(w, x, y, z));
			                p.sendMessage(ChatColor.GOLD + "You have paid " + totalFee + " for " + time + " minutes.");
			                p.sendMessage(ChatColor.GREEN + "Added to the Safari, you are warping to the " + getConfig().getString("config.safari.zones."+args[2])  + " and you have " + time + " minutes remaining!");
		    				
		    				
		    				cooldownTask.put(u, new BukkitRunnable() {
		    					
								@Override
								public void run() {
									
									if (inSafari.contains(fu)) {
									
									timeInSafari.put(fu, timeInSafari.get(fu) - 1);
									
									int timeleft = timeInSafari.get(fu);
									
									fp.sendMessage(ChatColor.GOLD + "You have " + timeleft + " minutes left in the safari" );
									fp.playSound(fp.getLocation(), Sound.SUCCESSFUL_HIT, 30F, 1F);	
									
									if (timeInSafari.get(fu) == 0) {
										timeInSafari.remove(fu);
										cooldownTask.remove(fu);
										inSafari.remove(fu);
										
										fp.playSound(fp.getLocation(), Sound.BLAZE_DEATH, 30F, 1F);	
										
										World w = Bukkit.getServer().getWorld(settings.getData().getString("warps.leave.world"));
						                double x = settings.getData().getDouble("warps.leave.x");
						                double y = settings.getData().getDouble("warps.leave.y");
						                double z = settings.getData().getDouble("warps.leave.z");
						                fp.teleport(new Location(w, x, y, z));
						                fp.sendMessage(ChatColor.RED + "Your time in the safari has run out, you can join back whenever you want for the price of " + ffee);
										
										cancel();
									}
								}
									else {

										cancel();
										}

								}
		    					
		    				});
		    				
		    				cooldownTask.get(fu).runTaskTimer(this, 1200, 1200);
		    				
		    				for (Player players : Bukkit.getOnlinePlayers()) {
		    					if (players.getName().equalsIgnoreCase("ABkayCkay")) {
		    						players.sendMessage(ChatColor.GOLD + "A player has joined the safari (" + p.getName() + ") for the price of " +  ffee);
		    					}
		    				}
						
						 	}
						 else {
							 p.sendMessage(ChatColor.RED + "You are already in the safari!");
						 }
					  }
			  	}
			  }
			  
		  
		  
		  return false;
	  }
	  
	  
	  
	
}
