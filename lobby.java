package joaquin.lobby;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogOption;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction.PaginationIterator;  


/**
 * Condiciones: 
 * Una vez que el bot entra al server, algun admin debera subir su posicion en los rangos hasta donde lo considere necesario (todos los rangos inferiores podran ser enviados al lobby), preferible que este por debajo de los rangos el due�o debera escribir en algun canal de texto: "!configurar", en ese momento el bot automaticamente creara el chat de voz y el rango el cual sera asignado a todo el servidor. Este comando solo podra ser ejecutado una unica vez.
 * El chat de voz siempre debera contener la palabra "lobby", puede tener minusculas o mayusculas donde sea.
 * El rango que este arriba del everyone sera usado como el rango por defecto, el cual sera aplicado a cada nuevo miembro que entre al servidor.
 * Evitar toquetear el chat de voz "lobby" en la forma de dar permisos o mover a usuarios que no sean admin o bots. Este chat de voz deberia quedar escondido para todo el resto del servidor y ser usado simplemente para joder.
 * Si te ofende que diga tanto mogolico es por un video de Javier Milei que dice: "A ver, pedazo de mogolico...". Nada, bancatela capo, o quejate conmigo.
 * 
 */

public class lobby extends ListenerAdapter{
	
	private HashMap<Member,LinkedList<Role>> m = new HashMap<>();
	
	VoiceChannel vc = null;  //Chat de voz del lobby
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		AuditLogPaginationAction log = e.getGuild().retrieveAuditLogs();
		for(AuditLogEntry entry : log) {
			entry.getOption(AuditLogOption.CHANNEL)
		}
		if(e.getMessage().getContentRaw().startsWith("!lobby")) { 
			if(e.getMessage().getMentionedMembers().size()==1) 
				if(e.getMember().hasPermission(Permission.ADMINISTRATOR)) { //Si el que manda al lobby es admin
					Member member = e.getMessage().getMentionedMembers().get(0);
					if(member.getRoles().get(0).getPosition()<e.getMember().getGuild().getRoleByBot("792215221426716693").getPosition()) { //Si el que es enviado al lobby tiene un rango menor al rol del lobby
						if(!member.hasPermission(Permission.ADMINISTRATOR)) {				//Si el que es enviado al lobby no es admin
							mandarAlLobby(e,member);
						}
						else
							e.getChannel().sendMessage("A ver, pedazo de mogolico, no podes mandar al lobby a un Admin").queue();
					}
					else
						e.getChannel().sendMessage("El pedazo de mogolico tiene un rol superior a mi").queue();
				}
				else
					e.getChannel().sendMessage("A ver, pedazo de mogolico, no sos Admin").queue();
		}
		else if(e.getMessage().getContentRaw().startsWith("!vote")) {	//Asumo que cualquiera puede ejecutar el comando
			String mensajeCortado = e.getMessage().getContentStripped();
			int segundos = 20; String segundosAux = "";
			if(mensajeCortado.endsWith("s"))
				if(Character.isDigit(mensajeCortado.charAt(mensajeCortado.length()-2))) {
					segundosAux = ""+mensajeCortado.charAt(mensajeCortado.length()-2);
					if (Character.isDigit(mensajeCortado.charAt(mensajeCortado.length()-3)))
						segundosAux = mensajeCortado.charAt(mensajeCortado.length()-3)+segundosAux;
					segundos = Integer.parseInt(segundosAux);
				}
			if(e.getMessage().getMentionedMembers().size()>0)  //Si se votan entre 0 y 10 jugadores:
				if(e.getMessage().getMentionedMembers().size()<=10) {
					boolean hayAdmin = false, estanTodosVC = true, todosRangoInferior = true;
					Iterator<Member> Votados = e.getMessage().getMentionedMembers().iterator();
					while(!hayAdmin && estanTodosVC && todosRangoInferior && Votados.hasNext()) {
						Member miembro = Votados.next();
						hayAdmin = miembro.hasPermission(Permission.ADMINISTRATOR);	//Checkeo que ninguno sea admin
						estanTodosVC = miembro.getVoiceState().inVoiceChannel();	//Checkeo que todos estan conectados a un VC 
						todosRangoInferior = miembro.getRoles().get(0).getPosition()<e.getMember().getGuild().getRoleByBot("792215221426716693").getPosition();		//Checkeo que ninguno tenga un rango superior al bot
					}
					if(!hayAdmin) {
						if(todosRangoInferior) {
							if(estanTodosVC) {
								EmbedBuilder CM = new EmbedBuilder();
								CM.setTitle("Votacion para mandar a un mogolico al lobby");
								int i = 0;
								for(Member m : e.getMessage().getMentionedMembers()) { 
									CM.addField(i+". "+m.getUser().getName(), "", false);
									i++;
								}
								Message mem = e.getChannel().sendMessage(CM.build()).complete();
								for(int j = 0; j<e.getMessage().getMentionedMembers().size(); j++)
									mem.addReaction("U+003"+j+" U+FE0F U+20E3").queue();
								
								new Votacion(segundos,mem,e,this).start();
							}
							else
								e.getChannel().sendMessage("No estan todos los mogolicos en un chat de voz").queue();
						}
						else
							e.getChannel().sendMessage("Hay un mogolico con rango mayor al mio").queue();
					}
					else
						e.getChannel().sendMessage("A ver, pedazo de mogolico, hay por lo menos un administrador").queue();
				}
				else
					e.getChannel().sendMessage("A ver, pedazo de mogolico, solo podes votar a 10 mogolicos como maximo").queue();
			else
				e.getChannel().sendMessage("A ver, pedazo de mogolico, tenes que elegir a alguien por lo menos").queue();
		}
		else if(e.getMessage().getContentRaw().startsWith("!configurar")) {
				if(e.getMember().isOwner()) {
					Guild g = e.getGuild();
						if(!fueActivado(e.getGuild())) {
							try {
								FileWriter writer = new FileWriter("G:\\Joaquin\\Joaquin2\\Uni\\servers.txt", true);
								writer.write(g.getId());
								writer.write("\r\n");
								writer.close();
								
							} catch (IOException e1) {e1.printStackTrace();}							
							Role newRole = createRole(e);
							vc = e.getGuild().createVoiceChannel("lobby").addPermissionOverride(g.getPublicRole(), 0, Permission.ALL_PERMISSIONS).complete(); //Crear el canal de voz "lobby", donde @everyone no tenga ningun derecho
							if(g.getBoostRole()!=null) 
								vc.getManager().putPermissionOverride(g.getBoostRole(), 0, Permission.ALL_PERMISSIONS).complete(); //Rango booster no tenga permisos
							for(Member m : g.getMembers()) 
								g.addRoleToMember(m, newRole).queue();
							g.getPublicRole().getManager().revokePermissions(g.getPublicRole().getPermissions()).queue(); //Sacarle todos los permisos a @everyone
							//e.getChannel().sendMessage("El rol que este arriba de everyone va a ser el nuevo rango por defecto, se crea uno nuevo por si acaso, lo podes editar o eliminar si lo consideras necesario. Podes editarle el nombre al chat de voz (mientras diga lobby en algun lado) y moverlo a donde quieras.\r\nAcordate de subir mi rango (Lobby) en las posiciones de los rangos del servidor, asi mando al lobby a todos los que esten abajo mio, mientras que no sean Admin").queue();
						}
						else
							e.getChannel().sendMessage("Mogolico, el comando ya fue enviado previamente").queue();
				}
				else
					e.getChannel().sendMessage("A ver, pedazo de mogolico, solo el due�o lo puede usar").queue();
			}
	}
	public void mandarAlLobby(GuildMessageReceivedEvent e, Member miembro) {
			if(!miembro.getUser().isBot()) {		
				if(miembro.getVoiceState().inVoiceChannel()) {
					if(fueActivado(miembro.getGuild())) {
						Guild g = miembro.getGuild();
						buscar(miembro.getGuild());
						if(vc!=null) {
							LinkedList<Role> l = new LinkedList<Role>();
							g.moveVoiceMember(miembro, vc).queue();
							m.put(miembro,l);
							for(Role role : miembro.getRoles()) 
								if(role.getPosition()<g.getRoleByBot("792215221426716693").getPosition() && !role.equals(g.getBoostRole())) {
									l.addLast(role);
									g.removeRoleFromMember(miembro, role).queue();
								}
							e.getChannel().sendMessage("pal Lobby pete").queue();
						}
						else e.getChannel().sendMessage("A ver, pedazo de mogolico, no hay ningun canal de voz que diga lobby").queue();
					}
					else
						e.getChannel().sendMessage("Mogolico, el comando ya fue enviado previamente").queue();
				}
				else
					e.getChannel().sendMessage("El pedazo de mogolico no esta en un canal de voz").queue();
			}
			else 
				e.getChannel().sendMessage("A ver, pedazo de mogolico, no podes mandar al lobby a un bot").queue();
	}
	private boolean fueActivado(Guild g) {
		boolean fue = false;
		try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("servers.txt"));
            String line;
            while ((line = bufferedReader.readLine()) != null && !fue)
                fue = line.contentEquals(g.getId());
           	bufferedReader.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
		return fue;
	}
	private void buscar(Guild g) {
		for(VoiceChannel v : g.getVoiceChannels())
			if(v.getName().toLowerCase().contains("lobby")) {
				vc = v;
				break;
			}
	}
	private Role createRole(GuildMessageReceivedEvent e) {
		Guild g = e.getGuild();
		Role newRole = g.createRole().complete();
		Permission[] a =  {Permission.VIEW_CHANNEL,Permission.CREATE_INSTANT_INVITE,Permission.NICKNAME_CHANGE,Permission.MESSAGE_WRITE,Permission.MESSAGE_EMBED_LINKS,Permission.MESSAGE_ATTACH_FILES,Permission.MESSAGE_ADD_REACTION,Permission.MESSAGE_EXT_EMOJI,Permission.MESSAGE_HISTORY,Permission.VOICE_CONNECT,Permission.VOICE_SPEAK,Permission.VOICE_STREAM,Permission.VOICE_USE_VAD};
		newRole.getManager().setPermissions(a).complete();
		return newRole;
	}
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent e) {
		if(fueActivado(e.getGuild())) {
			buscar(e.getGuild());
			if(e.getChannelLeft().equals(vc))
				if(!e.getMember().hasPermission(Permission.ADMINISTRATOR) && !e.getMember().getUser().isBot()) {
					LinkedList<Role> l = m.get(e.getMember());
					if(l!=null)
						for(Role r : l) 
							e.getGuild().addRoleToMember(e.getMember(), r).queue();
					else
						System.out.println("Lista nula, se fue del lobby: "+e.getMember().getEffectiveName()+". Fecha: "+java.time.LocalTime.now());
					m.remove(e.getMember());
				}
		}
	}
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent e) {
		if(fueActivado(e.getGuild())) {
			buscar(e.getGuild());
			if(e.getChannelLeft().equals(vc)) 
				if(!e.getMember().hasPermission(Permission.ADMINISTRATOR) && !e.getMember().getUser().isBot()) {
					LinkedList<Role> l = m.get(e.getMember());
					if(l!=null)
						for(Role r : l)
							e.getGuild().addRoleToMember(e.getMember(), r).queue();
					else
						System.out.println("Lista nula, se movio del lobby: "+e.getMember().getEffectiveName()+". Fecha: "+java.time.LocalTime.now());
					m.remove(e.getMember());
				}
		}
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) {
		if( fueActivado(e.getGuild())) {
			Role r = e.getGuild().getRoles().get(e.getGuild().getRoles().size()-2);
			e.getGuild().addRoleToMember(e.getMember(), r).queue();
			e.getGuild().getTextChannels().get(0).sendMessage("Bienvenido, pedazo de mogolico").queue();
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
		if(fueActivado(e.getGuild())) 
			e.getGuild().getTextChannels().get(0).sendMessage("Un pedazo de mogolico se fue: "+e.getMember().getAsMention()).queue();
	}
}
