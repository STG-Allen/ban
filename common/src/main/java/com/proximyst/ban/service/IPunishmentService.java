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

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentType;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IPunishmentService {
  /**
   * Get the punishments of a user.
   *
   * @param target The target whose punishments are requested.
   * @return An immutable copy of the punishments of the player where order is not guaranteed.
   */
  @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(final @NonNull UUID target);

  /**
   * Get the punishments of a user.
   *
   * @param target The target whose punishments are requested.
   * @return An immutable copy of the punishments of the player where order is not guaranteed.
   */
  default @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(
      final @NonNull BanUser target) {
    return this.getPunishments(target.getUuid());
  }

  /**
   * Save the given punishment.
   *
   * @param punishment The punishment to save.
   * @see IDataService#savePunishment(Punishment)
   */
  @NonNull CompletableFuture<@Nullable Void> savePunishment(final @NonNull Punishment punishment);

  /**
   * Apply the punishment to the target if they are online.
   * <p>
   * This is only useful for {@link PunishmentType#BAN bans} and {@link PunishmentType#KICK kicks}.
   *
   * @param punishment The punishment to apply.
   */
  @NonNull CompletableFuture<@Nullable Void> applyPunishment(final @NonNull Punishment punishment);

  /**
   * Get the current active ban on a target, if any.
   *
   * @param target The target of the ban.
   * @return An optional of the punishment record of this ban.
   */
  default @NonNull CompletableFuture<@NonNull Optional<@NonNull Punishment>> getActiveBan(final @NonNull UUID target) {
    return this.getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.BAN
                && punishment.currentlyApplies())
            .max(Comparator.comparingLong(Punishment::getTime))
        );
  }

  /**
   * Get the current active mute on a target, if any.
   *
   * @param target The target of the mute.
   * @return An optional of the punishment record of this mute.
   */
  default @NonNull CompletableFuture<@NonNull Optional<@NonNull Punishment>> getActiveMute(final @NonNull UUID target) {
    return this.getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.MUTE
                && punishment.currentlyApplies())
            .max(Comparator.comparingLong(Punishment::getTime))
        );
  }

  /**
   * Get all notes applied to a target.
   *
   * @param target The target to get the notes of.
   * @return The notes of the target. The list will never be {@code null}, but may be {@link ImmutableList#isEmpty()
   * empty}.
   */
  default @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getNotes(final @NonNull UUID target) {
    return this.getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.NOTE)
            .sorted(Comparator.comparingLong(Punishment::getTime))
            .collect(ImmutableList.toImmutableList())
        );
  }
}
