package pebbleantivpn.pebbleantivpn;

import com.google.inject.Inject;

import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "pebbleantivpn",
        name = "PebbleAntiVPN",
        version = "1.5",
        description = "Block any type of <Tor/Proxy/VPNs>",
        authors = {"Binkie"}
)
public class PebbleAntiVPNVelocity {

    @Inject
    public PebbleAntiVPNVelocity(Logger logger) {
        logger.info("§bGood day there, §6PebbleAntiVPN §bdoes not yet support §6Velocity; §bhowever, the owner will ensure that it does in the upcoming release.");
    }
}
