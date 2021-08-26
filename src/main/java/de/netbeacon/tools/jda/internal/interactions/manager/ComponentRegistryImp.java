package de.netbeacon.tools.jda.internal.interactions.manager;

import de.netbeacon.tools.jda.api.event.listener.EventListenerPriority;
import de.netbeacon.tools.jda.api.interactions.manager.ComponentRegistry;
import de.netbeacon.tools.jda.api.interactions.type.ComponentEntry;
import de.netbeacon.tools.jda.internal.interactions.records.DeactivationMode;
import de.netbeacon.tools.jda.internal.interactions.records.TimeoutPolicy;
import de.netbeacon.tools.jda.internal.interactions.type.ComponentEntryImp;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.*;

public class ComponentRegistryImp extends ListenerAdapter implements EventListenerPriority, ComponentRegistry {

    private final ConcurrentHashMap<String, ComponentEntry<?>> registry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Future<?>> registryTimeout = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutExecutorService = Executors.newScheduledThreadPool(2);
    private final int priority;

    public ComponentRegistryImp(){
        this.priority = 0;
        startTimeoutCleanup();
    }

    public ComponentRegistryImp(int priority){
        this.priority = priority;
        startTimeoutCleanup();
    }

    public void startTimeoutCleanup(){
        this.timeoutExecutorService.scheduleAtFixedRate(() -> {
            try{
                registry.forEach((k, v) -> {
                    if(!v.isValid()){
                        deactivate(v);
                        unregister(v);
                    }
                });
            }
            catch(Exception ignored){
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public <T extends ComponentInteraction> ComponentEntry<T> get(String id) {
        return null;
    }

    @Override
    public synchronized void register(ComponentEntry<?> entry) {
        registry.put(entry.getId(), entry);
        TimeoutPolicy top = entry.getTimeoutPolicy();
        if(!top.equals(TimeoutPolicy.NONE)){
            registryTimeout.put(entry.getId(),
                    timeoutExecutorService.schedule(() -> {
                        deactivate(entry);
                        unregister(entry);
                    }, top.timeoutInMS(), TimeUnit.MILLISECONDS)
            );
        }
        entry.setRegistry(this);
    }

    public synchronized void unregister(ComponentEntry<?> entry){
        String id = entry.getId();
        registry.remove(id);
        Future<?> future = registryTimeout.remove(id);
        if(future != null){
            future.cancel(false);
        }
        entry.setRegistry(null);
    }

    public synchronized void deactivate(ComponentEntry<?> entry){
        deactivate(entry, null);
    }

    public synchronized void deactivate(ComponentEntry<?> entry_, Message message){
        ComponentEntryImp<?> entry = (ComponentEntryImp<?>) entry_;
        if(!entry.markDeactivated())
            return;
        var deactivationMode = entry.getDeactivationMode();
        var deactivationModeIds = deactivationMode.getIds();
        if(message == null){
            // can't actually modify the other entries, can't actually know the other entries, this shouldnt normally get fired
            for(var other : deactivationMode.entrySupplier().get()){
                ((ComponentEntryImp<?>) other).markDeactivated();  // mark the others as deactivated and
                unregister(other);                                  // remove the registration
            }
        }else{
            var newRows = new ArrayList<ActionRow>();
            var currentRows = message.getActionRows();
            for(var row : currentRows){
                var newComponents = new ArrayList<Component>();
                for(var component : row.getComponents()){
                    if((deactivationMode.equals(DeactivationMode.SELF) && entry.getId().equals(component.getId()))
                            || deactivationMode.equals(DeactivationMode.ALL)
                            || deactivationModeIds.contains(component.getId())
                    ){
                        if(component instanceof Button){
                            newComponents.add(((Button) component).asDisabled());
                        }else if(component instanceof SelectionMenu){
                            newComponents.add(((SelectionMenu) component).asDisabled());
                        }
                        if(!entry.getId().equals(component.getId())){ // unregister
                            ComponentEntryImp<?> oEntry = (ComponentEntryImp<?>) get(component.getId());
                            if(oEntry != null){
                                oEntry.markDeactivated();
                                unregister(oEntry);
                            }
                        }
                    } else {
                        newComponents.add(component);
                    }
                }
                newRows.add(ActionRow.of(newComponents));
            }
            message.editMessageComponents(newRows).queue();
        }
    }

    // EVENT

    @Override
    public void onGenericComponentInteractionCreate(@NotNull GenericComponentInteractionCreateEvent event) {
        var componentId = event.getComponentId();
        var component = this.get(componentId);
        if(component == null) {
            return;
        }
        if(event.getMember() == null ? !component.getAccessor().isAllowedAccessor(event.getUser()) : !component.getAccessor().isAllowedAccessor(event.getMember())){
            return;
        }
        if(!component.isValid() || !((ComponentEntryImp<?>) component).performActivation()){
            return;
        }
        event.deferEdit().queue();
        if(component.successConsumer() != null){
            try {
                component.successConsumer().accept(event);
            }catch (RuntimeException e){
                if(component.exceptionConsumer() != null){
                    component.exceptionConsumer().accept(e);
                }
            }
        }
        if(!component.isValid()){
            deactivate(component);
        }
    }
}
