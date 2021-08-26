package de.netbeacon.tools.jda.internal.interactions.records;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Accessor(Long... accessors){

    public static final Accessor ANY = new Accessor((Long) null);

    public static Accessor ANY_OF(Long... accessors){
        return new Accessor(accessors);
    }

    public Set<Long> asSet(){
        return new HashSet<>(List.of(accessors));
    }

    public boolean isAllowedAccessor(Long accessor){
        if(this.equals(ANY)){
            return true;
        }
        return asSet().contains(accessor);
    }

    public boolean isAllowedAccessor(Long... accessors){
        return Arrays.stream(accessors).anyMatch(this::isAllowedAccessor);
    }

    public boolean isAllowedAccessor(User user){
        return isAllowedAccessor(user.getIdLong());
    }

    public boolean isAllowedAccessor(Member member){
        return isAllowedAccessor(member.getIdLong()) || member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(this::isAllowedAccessor);
    }

}
