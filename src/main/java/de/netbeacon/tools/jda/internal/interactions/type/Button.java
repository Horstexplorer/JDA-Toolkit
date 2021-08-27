package de.netbeacon.tools.jda.internal.interactions.type;

import de.netbeacon.tools.jda.internal.interactions.records.Accessors;
import de.netbeacon.tools.jda.internal.interactions.records.Activations;
import de.netbeacon.tools.jda.internal.interactions.records.DeactivationMode;
import de.netbeacon.tools.jda.internal.interactions.records.TimeoutPolicy;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.function.Consumer;

public class Button extends ComponentEntryImp<ButtonClickEvent> {
    public Button(Accessors accessors, Activations activations, DeactivationMode deactivationMode, TimeoutPolicy timeoutPolicy, Consumer<ButtonClickEvent> successConsumer, Consumer<Exception> exceptionConsumer) {
        super(accessors, activations, deactivationMode, timeoutPolicy, successConsumer, exceptionConsumer);
    }
}
