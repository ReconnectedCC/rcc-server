package ct.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;


public class Ctserver implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("ct-server");
	private int tickCounter = 0;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Starting ct-client");
		ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                onEndServerTick(server);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
	}
	private void sendPostRequest(String urlString, String plaintext) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/plain; utf-8");
		con.setRequestProperty("TPS", plaintext);
		con.setRequestProperty("Accept", "text/plain");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
			byte[] input = plaintext.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		}

		int code = con.getResponseCode();
		//LOGGER.info("POST Response Code :: " + code);
		con.disconnect();
	}
	long zeroTicks = 0;
	private void onEndServerTick(MinecraftServer minecraftServer) throws Exception {
		tickCounter++;

		if (tickCounter >= 20) {

			double afterTicks = Instant.now().toEpochMilli();
			double timeBetween = afterTicks - zeroTicks;
			double mspt = (timeBetween / 20) ;
			double tps = 1000 / mspt;
			//LOGGER.info(String.valueOf(timeBetween));
			//LOGGER.info(String.valueOf(mspt));
			//LOGGER.info(String.valueOf(tps));
            CompletableFuture.runAsync(() -> {
				try {
					sendPostRequest("http://us-ky-medium-0004.knijn.one:58926/tps", String.valueOf(tps));
				} catch (Exception e) {
					LOGGER.error("Failed to send POST request", e);
				}
			});
			//LOGGER.info("Sent HTTP request");
			tickCounter = 0;
			zeroTicks = Instant.now().toEpochMilli();
		}
	}

}