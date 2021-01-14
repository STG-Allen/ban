//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package com.proximyst.ban.service.impl;

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.MessageService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplPunishmentService implements IPunishmentService {
  private static final int MAXIMUM_PUNISHMENT_CACHE_CAPACITY =
      Integer.getInteger("ban.maximumPunishmentCacheCapacity", 512);

  private final @NonNull IDataService dataService;
  private final @NonNull MessageService messageService;
  private final @NonNull Executor executor;

  @Inject
  ImplPunishmentService(final @NonNull IDataService dataService,
      final @NonNull MessageService messageService,
      final @NonNull @BanAsyncExecutor Executor executor) {
    this.dataService = dataService;
    this.messageService = messageService;
    this.executor = executor;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(
      final @NonNull BanIdentity identity) {
    return CompletableFuture.supplyAsync(() -> ImmutableList.copyOf(
        this.dataService.getPunishmentsForTarget(identity)), this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Punishment> savePunishment(
      final @NonNull PunishmentBuilder punishmentBuilder) {
    return CompletableFuture.supplyAsync(() -> this.dataService.savePunishment(punishmentBuilder), this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> applyPunishment(final @NonNull Punishment punishment) {
    if (!punishment.getPunishmentType().isApplicable()) {
      return CompletableFuture.completedFuture(null);
    }

    return punishment.getTarget().audiences()
        .thenAccept(audiences -> CompletableFuture.allOf(
            StreamSupport.stream(audiences.spliterator(), false)
                .map(audience -> this.applyPunishmentTo(punishment, audience))
                .toArray(CompletableFuture<?>[]::new)));
  }

  private @NonNull CompletableFuture<@Nullable Void> applyPunishmentTo(final @NonNull Punishment punishment,
      final @NonNull IBanAudience audience) {
    final String bypassPermission = punishment.getPunishmentType().getBypassPermission().orElse(null);
    if (bypassPermission != null && audience.hasPermission(bypassPermission)) {
      // Don't apply the punishment; they can bypass it.
      return CompletableFuture.completedFuture(null);
    }

    return this.messageService.punishmentMessage(
        punishment
            .getPunishmentType()
            .getApplicationMessage(punishment.getReason().isPresent())
            .orElseThrow(() -> new IllegalStateException("Applicable punishment without punishment type")),
        punishment)
        .component()
        .thenAccept(component -> {
          switch (punishment.getPunishmentType()) {
            case BAN:
              // Fall through.
            case KICK:
              audience.disconnect(component);
              break;

            case MUTE:
              // Fall through.
            default:
              audience.sendMessage(Identity.nil(), component);
              break;
          }
        });
  }
}
