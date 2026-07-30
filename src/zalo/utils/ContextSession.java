
package zalo.utils;

import zalo.utils.Context;

public class ContextSession {
    

    public static boolean isContextSession(Context ctx) {
        return ctx != null && ctx.getSecretKey() != null && !ctx.getSecretKey().isEmpty();
    }
}

