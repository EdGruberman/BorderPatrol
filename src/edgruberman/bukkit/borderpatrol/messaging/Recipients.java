package edgruberman.bukkit.borderpatrol.messaging;

import edgruberman.bukkit.borderpatrol.messaging.messages.Confirmation;

public interface Recipients {

    public abstract Confirmation send(Message message);

}
