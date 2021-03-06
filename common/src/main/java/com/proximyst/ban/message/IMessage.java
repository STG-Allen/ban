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

package com.proximyst.ban.message;

import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A message to be sent to an {@link Audience}.
 * <p>
 * This is intentionally not a {@link FunctionalInterface}, because a message <i>should</i> always contain some kind of
 * data about its displayed message(s), as to be as generic as possible.
 */
public interface IMessage {
  /**
   * Eventually send this message to the given {@link Audience}.
   *
   * @param audience The audience to receive the message. This may be a {@link ForwardingAudience} to target more than
   *                 {@code 1} audience at a time.
   * @param source   The source of this message.
   * @return A {@link CompletableFuture} that is completed once the message has been sent, or {@link
   * CompletableFuture#isCompletedExceptionally() completes exceptionally} if something went wrong.
   */
  @NonNull CompletableFuture<@Nullable Void> send(final @NonNull Audience audience, final @NonNull Identity source);

  /**
   * Eventually send this message to the given {@link Audience}.
   *
   * @param audience The audience to receive the message. This may be a {@link ForwardingAudience} to target more than
   *                 {@code 1} audience at a time.
   * @param source   The source of this message.
   * @return A {@link CompletableFuture} that is completed once the message has been sent, or {@link
   * CompletableFuture#isCompletedExceptionally() completes exceptionally} if something went wrong.
   */
  default @NonNull CompletableFuture<@Nullable Void> send(final @NonNull Audience audience,
      final @NonNull Identified source) {
    return this.send(audience, source.identity());
  }

  /**
   * Eventually send this message to the given {@link Audience}.
   *
   * @param audience The audience to receive the message. This may be a {@link ForwardingAudience} to target more than
   *                 {@code 1} audience at a time.
   * @return A {@link CompletableFuture} that is completed once the message has been sent, or {@link
   * CompletableFuture#isCompletedExceptionally() completes exceptionally} if something went wrong.
   */
  default @NonNull CompletableFuture<@Nullable Void> send(final @NonNull Audience audience) {
    return this.send(audience, Identity.nil());
  }

  /**
   * Create the {@link Component} that is (meant) to be sent to an {@link Audience}.
   *
   * @return The component created by this message.
   */
  @NonNull CompletableFuture<@NonNull Component> component();
}
