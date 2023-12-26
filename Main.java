package joaquin.lobby;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {
	public static JDA jda;
	
	public static void main(String [] args) {
		JDABuilder jdaBuilder = JDABuilder.createDefault("");
		jdaBuilder.setActivity(Activity.playing("mandar gente al Lobby"));
		jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
		jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
		jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
		jdaBuilder.enableCache(CacheFlag.ROLE_TAGS);
		lobby lobby = new lobby();
		jdaBuilder.addEventListeners(lobby);
		try {
			jda = jdaBuilder.build();
		} catch (LoginException e) {e.printStackTrace();}
	}
}
