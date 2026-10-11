package com.example.tibiachat.hud;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.Conversation;
import java.util.ArrayList;
import java.util.List;

public final class UnreadNotifier {
    private static boolean expanded;
    private static final List<Entry> entries = new ArrayList<>();

    private UnreadNotifier() {}

    public static boolean isExpanded() {
        return expanded;
    }

    public static void collapse() {
        expanded = false;
    }

    public static List<Conversation> unreadConversations() {
        List<Conversation> list = new ArrayList<>();
        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            if (c.unread() > 0) {
                list.add(c);
            }
        }
        return list;
    }

    public static void clearEntries() {
        entries.clear();
    }

    public static void addEntry(String key, int x0, int y0, int x1, int y1) {
        entries.add(new Entry(key, x0, y0, x1, y1));
    }

    public static String handleClick(double mx, double my) {
        if (TibiaChatTabsClient.CHAT.totalUnread() <= 0) {
            expanded = false;
            return null;
        }
        for (Entry e : entries) {
            if (mx >= e.x0 && mx < e.x1 && my >= e.y0 && my < e.y1) {
                if ("__badge__".equals(e.key)) {
                    expanded = !expanded;
                    return "";
                }
                expanded = false;
                return e.key;
            }
        }
        if (expanded) {
            expanded = false;
        }
        return null;
    }

    private record Entry(String key, int x0, int y0, int x1, int y1) {}
}
