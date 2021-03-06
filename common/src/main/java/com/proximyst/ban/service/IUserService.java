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

package com.proximyst.ban.service;

import com.proximyst.ban.model.BanUser;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IUserService {
  /**
   * Save the given user.
   *
   * @param user The user to update the database with.
   * @see IDataService#saveUser(BanUser)
   */
  @NonNull CompletableFuture<@Nullable Void> saveUser(final @NonNull BanUser user);

  /**
   * Get the user data of a user with the given name, if they exist.
   *
   * @param name The name of the user to get the data of.
   * @return The user data of the user.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull String name);

  /**
   * Get the user data of a user with the given UUID, if they exist.
   *
   * @param uuid The UUID of the user to get the data of.
   * @return The user data of the user.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull UUID uuid);

  /**
   * Schedule an update on the data of a user, only if necessary.
   *
   * @param uuid The UUID to schedule an update for.
   * @return Whether an update was scheduled.
   */
  @NonNull CompletableFuture<@NonNull Boolean> scheduleUpdateIfNecessary(final @NonNull UUID uuid);

  /**
   * Get the user data of a user with the given UUID, if they exist.
   * <p>
   * This will always fetch the latest data about the user.
   *
   * @param uuid The UUID of the user to get the data of.
   * @return The user data of the user.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUserUpdated(final @NonNull UUID uuid);
}
