package de.netbeacon.tools.jda.internal.exception;

import net.dv8tion.jda.api.Permission;

import java.util.Map;

public class PermissionException extends RuntimeException{

    public enum Type {
        BOT,
        USER;
    }

    private final Type type;
    private final Map<Permission, Boolean> permissionCheck;

    public PermissionException(Type type, String message, Map<Permission, Boolean> permissionCheck){
        super(message);
        this.type = type;
        this.permissionCheck = permissionCheck;
    }

    public Type getType() {
        return type;
    }

    public Map<Permission, Boolean> getPermissionCheck() {
        return permissionCheck;
    }
}
