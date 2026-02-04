package engine.animation;

import java.lang.reflect.Method;

public record AnimationMapping(
        boolean relative,
        Method method
) {}
