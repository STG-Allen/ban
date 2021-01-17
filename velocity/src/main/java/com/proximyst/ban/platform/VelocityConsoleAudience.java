package com.proximyst.ban.platform;

import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class VelocityConsoleAudience implements IBanConsole, ForwardingAudience.Single {
  private final @NonNull Audience console;

  @Inject
  VelocityConsoleAudience(final @NonNull ProxyServer proxyServer) {
    this.console = proxyServer.getConsoleCommandSource();
  }

  @Override
  public @NonNull UUID uuid() {
    return UUID;
  }

  @Override
  public @NonNull String username() {
    return USERNAME;
  }

  @Override
  public boolean hasPermission(final @NonNull String permission) {
    return true;
  }

  @Override
  public void disconnect(final @NonNull Component reason) {
  }

  @Override
  public @NonNull Audience audience() {
    return this.console;
  }
}
