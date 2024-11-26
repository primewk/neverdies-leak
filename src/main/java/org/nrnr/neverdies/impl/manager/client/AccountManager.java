package org.nrnr.neverdies.impl.manager.client;

import net.minecraft.client.session.Session;
import org.nrnr.neverdies.Neverdies;
import org.nrnr.neverdies.api.account.config.AccountFile;
import org.nrnr.neverdies.api.account.config.EncryptedAccountFile;
import org.nrnr.neverdies.api.account.msa.MSAAuthenticator;
import org.nrnr.neverdies.api.account.type.MinecraftAccount;
import org.nrnr.neverdies.mixin.accessor.AccessorMinecraftClient;
import org.nrnr.neverdies.util.Globals;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xgraza
 * @see MinecraftAccount
 * @since 03/31/24
 */
public final class AccountManager implements Globals {
    // The Microsoft authenticator
    public static final MSAAuthenticator MSA_AUTHENTICATOR = new MSAAuthenticator();
    private final List<MinecraftAccount> accounts = new LinkedList<>();

    private AccountFile configFile;

    public void postInit() {
        // Handle account file encryption
        final Path runDir = Neverdies.CONFIG.getClientDirectory();
        if (runDir.resolve("accounts_enc.json").toFile().exists()) {
            System.out.println("Encrypted account file exists");
            configFile = new EncryptedAccountFile(runDir);
        } else {
            System.out.println("Normal account file");
            configFile = new AccountFile(runDir);
        }

        Neverdies.CONFIG.addFile(configFile);
    }

    /**
     * @param account
     */
    public void register(MinecraftAccount account) {
        accounts.add(account);
    }

    /**
     * @param account
     */
    public void unregister(final MinecraftAccount account) {
        accounts.remove(account);
    }

    public void setSession(final Session session) {
        ((AccessorMinecraftClient) mc).setSession(session);
        Neverdies.info("Set session to {} ({})", session.getUsername(), session.getUuidOrNull());
    }

    /**
     * @return
     */
    public List<MinecraftAccount> getAccounts() {
        return accounts;
    }

    public boolean isEncrypted() {
        return configFile instanceof EncryptedAccountFile;
    }
}
