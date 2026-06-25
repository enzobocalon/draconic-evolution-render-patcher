package com.derenderpatcher.compat;

import java.util.List;

public record CompatStatus(String name, State state, List<Setting> settings, String detail) {
    public CompatStatus {
        settings = List.copyOf(settings);
    }

    public static CompatStatus disabled(String name) {
        return new CompatStatus(name, State.DISABLED, List.of(), "disabled in Draconic Render Patcher");
    }

    public static CompatStatus notInstalled(String name) {
        return new CompatStatus(name, State.NOT_INSTALLED, List.of(), "mod not found");
    }

    public static CompatStatus unavailable(String name, String detail) {
        return new CompatStatus(name, State.UNAVAILABLE, List.of(), detail);
    }

    public static CompatStatus verified(String name, boolean healthy, List<Setting> settings, String detail) {
        return new CompatStatus(name, healthy ? State.ACTIVE : State.NEEDS_ATTENTION, settings, detail);
    }

    public enum State {
        DISABLED,
        NOT_INSTALLED,
        ACTIVE,
        NEEDS_ATTENTION,
        UNAVAILABLE
    }

    public record Setting(String name, Value configured, Value effective) {
        public static Setting effectiveOnly(String name, boolean effective) {
            return new Setting(name, Value.UNAVAILABLE, Value.of(effective));
        }

        public static Setting transition(String name, boolean configured, boolean effective) {
            return new Setting(name, Value.of(configured), Value.of(effective));
        }
    }

    public enum Value {
        TRUE,
        FALSE,
        UNAVAILABLE;

        public static Value of(boolean value) {
            return value ? TRUE : FALSE;
        }
    }
}
