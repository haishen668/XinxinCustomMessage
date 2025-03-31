package ltd.dreamcraft.xinxincustommessage.objects;

import java.util.List;
import java.util.Objects;

public class CustomMessage {
    public final String trigger;

    public final List<String> responses;

    public final List<String> unbind_messages;

    public final List<Long> groups;

    public final String id;
    public final List<Long> admins;
    public final List<String> scripts;

    public CustomMessage(String trigger, List<String> responses, List<String> unbind_messages, List<Long> groups, String id, List<Long> admins, List<String> scripts) {
        this.trigger = trigger;
        this.responses = responses;
        this.unbind_messages = unbind_messages;
        this.groups = groups;
        this.id = id;
        this.admins = admins;
        this.scripts = scripts;
    }

    public List<Long> getAdmins() {
        return admins;
    }

    public String getId() {
        return this.id;
    }

    public List<Long> getGroups() {
        return this.groups;
    }

    public List<String> getResponses() {
        return this.responses;
    }

    public List<String> getUnbind_messages() {
        return this.unbind_messages;
    }

    public String getTrigger() {
        return this.trigger;
    }

    public List<String> getScripts() {
        return scripts;
    }

    /**
     * 两个对象相等的条件：
     * 1. 引用相同（地址相同）。
     * 2. 类型相同且所有关键字段相等。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomMessage that = (CustomMessage) o;
        return Objects.equals(trigger, that.trigger) &&
                Objects.equals(responses, that.responses) &&
                Objects.equals(unbind_messages, that.unbind_messages) &&
                Objects.equals(groups, that.groups) &&
                Objects.equals(admins, that.admins) &&
                Objects.equals(scripts, that.scripts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger);
    }
}


