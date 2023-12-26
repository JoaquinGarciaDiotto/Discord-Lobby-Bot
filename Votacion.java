package joaquin.lobby;

import java.util.Iterator;
import java.util.Random;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;


public class Votacion {
	
	private int segundos;
	private Message mensaje;
	private GuildMessageReceivedEvent evento;
	private lobby lobby;
	
	public Votacion(int segundos, Message mensaje, GuildMessageReceivedEvent e, lobby l) {
		this.segundos = segundos;
		this.mensaje = mensaje;
		evento = e;
		lobby = l;
	}
	
	public void start() {
		new Thread(){
            @Override
            public void run() {
                while(segundos>0){
                    segundos--;
                    try{Thread.sleep(1000);}catch(Exception e){}
                }
                int numReac = 0, actual, max = 1;
                for(MessageReaction Reacciones :  mensaje.getChannel().retrieveMessageById(mensaje.getId()).complete().getReactions())
                	if(Reacciones.isSelf()) {
	                	actual = Reacciones.getCount();
	                	if(actual>max) {
	                		max = actual;
	                		numReac = Integer.parseInt(Reacciones.getReactionEmote().getName().substring(0, 1));
	                	}
                	}
                if(max>1) {
                	Member miembro = evento.getMessage().getMentionedMembers().get(numReac);
                	if(miembro.getVoiceState().inVoiceChannel()) {
	                	Random r = new Random();
	                	int num = r.nextInt(10); //0-9
	                	if(num<2)
	                		evento.getChannel().sendMessage(". 。　　　　•　 　ﾟ　　。 　　. \n  　　.　　　 　　.　　　　　。　　 。　. 　\n  .　　 。　　　　　 ඞ   。 . 　　 •   • \n 　　ﾟ　　"+miembro.getUser().getName()+" era el Impostor.　 。　. \n  	　　'　　　 　　.　•　　　　　。　　 。　\n　ﾟ　　　.　　　. 　　,　　.　 .").queue();
	                	else
	                		evento.getChannel().sendMessage(". 。　　　　•　 　ﾟ　　。 　　. \n  　　.　　　 　　.　　　　　。　　 。　. 　\n  .　　 。　　　　　 ඞ   。 . 　　 •   • \n 　　ﾟ　　"+miembro.getUser().getName()+" no era el Impostor.　 。　. \n  	　　'　　　 　　.　•　　　　　。　　 。　 \n　ﾟ　　　.　　　. 　　,　　.　 .").queue();
	                	lobby.mandarAlLobby(evento, miembro);
                	}
                	else
                		evento.getChannel().sendMessage("El pedazo de mogolico cagon se salio del chat de voz").queue();
                }
                else 
                	evento.getChannel().sendMessage("Nadie voto a ningun mogolico").queue();
            }
        }.start();
    }
}
