package de.netbeacon.tools.jda.internal.interactions.type;

import de.netbeacon.tools.jda.internal.interactions.records.Accessors;
import de.netbeacon.tools.jda.internal.interactions.records.Activations;
import de.netbeacon.tools.jda.internal.interactions.records.DeactivationMode;
import de.netbeacon.tools.jda.internal.interactions.records.TimeoutPolicy;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;

import java.util.function.Consumer;

public class Selection extends ComponentEntryImp<SelectionMenuEvent> {
    public Selection(Accessors accessors, Activations activations, DeactivationMode deactivationMode, TimeoutPolicy timeoutPolicy, Consumer<SelectionMenuEvent> successConsumer, Consumer<Exception> exceptionConsumer) {
        super(accessors, activations, deactivationMode, timeoutPolicy, successConsumer, exceptionConsumer);
    }
}
