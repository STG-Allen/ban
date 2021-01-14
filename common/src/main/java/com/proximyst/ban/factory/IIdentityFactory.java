package com.proximyst.ban.factory;

import com.proximyst.ban.model.BanIdentity.IpIdentity;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import java.net.InetAddress;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IIdentityFactory {
  @NonNull UuidIdentity uuid(final long id, final @NonNull UUID uuid, final @NonNull String username);

  @NonNull IpIdentity ip(final long id, final @NonNull InetAddress address);
}
