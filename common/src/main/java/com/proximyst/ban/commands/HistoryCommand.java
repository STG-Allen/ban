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

package com.proximyst.ban.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.MessageService;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class HistoryCommand extends BaseCommand {
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull MessageService messageService;

  @Inject
  HistoryCommand(final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull MessageService messageService) {
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.punishmentService = punishmentService;
    this.messageService = messageService;
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull IBanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("history")
        .permission(BanPermissions.COMMAND_HISTORY)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<IBanAudience> ctx) {
    final BanUser target = ctx.get("target");

    this.messageService.commandsFeedbackHistory(target)
        .send(ctx.getSender());

    this.punishmentService.getPunishments(target.getUuid())
        .thenCompose(this::composePunishmentEntryComponents)
        .thenCompose(components -> this.sendHeaderReturnEntries(components, target, ctx.getSender()))
        .thenAccept(components -> this.sendMessages(components, ctx.getSender()))
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }

  private @NonNull CompletableFuture<@NonNull Component @NonNull []> composePunishmentEntryComponents(
      final @NonNull Collection<@NonNull Punishment> punishments) {
    final CompletableFuture<?>[] futures = new CompletableFuture<?>[punishments.size()];
    int index = 0;
    for (final Punishment punishment : punishments) {
      futures[index++] = this.messageService.commandsHistoryEntry(punishment)
          .component();
    }

    return CompletableFuture.allOf(futures)
        .thenApply($ -> Arrays.stream(futures)
            .map(f -> (Component) f.join())
            .toArray(Component[]::new));
  }

  private @NonNull CompletableFuture<@NonNull Component @NonNull []> sendHeaderReturnEntries(
      final @NonNull Component @NonNull [] components,
      final @NonNull BanUser target,
      final @NonNull Audience receiver) {
    return this.messageService.commandsHistoryHeader(target, components.length)
        .send(receiver)
        .thenApply($ -> components);
  }

  private void sendMessages(final @NonNull Component @NonNull [] components,
      final @NonNull Audience receiver) {
    for (final Component message : components) {
      receiver.sendMessage(Identity.nil(), message);
    }
  }
}
