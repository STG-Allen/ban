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

import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.ConsoleIdentity;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IMojangService;
import com.proximyst.ban.service.IUserService;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class ImplUserService implements IUserService {
  private final @NonNull IMojangService mojangService;
  private final @NonNull IDataService dataService;
  private final @NonNull Executor executor;
  private final @NonNull ConsoleIdentity banConsole;

  @Inject
  ImplUserService(final @NonNull IMojangService mojangService,
      final @NonNull IDataService dataService,
      final @NonNull @BanAsyncExecutor Executor executor,
      final @NonNull ConsoleIdentity banConsole) {
    this.mojangService = mojangService;
    this.dataService = dataService;
    this.executor = executor;
    this.banConsole = banConsole;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUser(final @NonNull UUID uuid) {
    if (uuid.version() != 4) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return this.getUserInternal(() -> this.dataService.getUser(uuid).flatMap(BanIdentity::asUuidIdentity),
        () -> this.mojangService.getUser(uuid));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUser(final @NonNull String name) {
    return this.getUserInternal(() -> this.dataService.getUser(name).flatMap(BanIdentity::asUuidIdentity),
        () -> this.mojangService.getUser(name));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUserUpdated(final @NonNull UUID uuid) {
    return this.mojangService.getUser(uuid);
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Boolean> scheduleUpdateIfNecessary(final @NonNull UUID uuid) {
    return CompletableFuture
        .supplyAsync(() -> this.dataService.getUserCacheDate(uuid).orElse(null), this.executor)
        .thenCompose(lastUpdate -> {
          if (lastUpdate == null
              || lastUpdate + TimeUnit.DAYS.toMillis(1L) <= System.currentTimeMillis()) {
            return this.getUserUpdated(uuid)
                .thenApply($ -> true);
          }

          return CompletableFuture.completedFuture(false);
        });
  }

  @Override
  public @NonNull CompletableFuture<@NonNull UuidIdentity> saveUser(final @NonNull UUID uuid,
      final @NonNull String username) {
    return CompletableFuture.supplyAsync(() -> this.dataService.createIdentity(uuid, username), this.executor);
  }

  private @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUserInternal(
      final @NonNull Supplier<@NonNull Optional<@NonNull UuidIdentity>> banUserSupplier,
      final @NonNull Supplier<@NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>>> mojangUserSupplier) {
    return CompletableFuture
        .supplyAsync(banUserSupplier, this.executor)
        .thenCompose(opt -> {
          if (opt.isPresent()) {
            return CompletableFuture.completedFuture(opt);
          }

          return mojangUserSupplier.get()
              .thenApply(mojangUser -> {
                mojangUser.ifPresent(ident ->
                    this.executor.execute(() ->
                        this.dataService.createIdentity(ident.uuid(), ident.username())));

                return mojangUser;
              });
        });
  }
}
