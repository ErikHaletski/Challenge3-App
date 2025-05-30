//Daily Quests

package de.challenge3.questapp.ui.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.challenge3.questapp.ui.quest.Quest.QuestType;

public class DailyQuestPool {

    // Liste aller Daily
    private static final List<Quest> allDailyTemplates = new ArrayList<>();

    static {
        // Dailies
        allDailyTemplates.add(new Quest("d1", "10 Pushups", "Mach 10 Liegestütze", 50, "Strength", 2, QuestType.DAILY, 0));
        allDailyTemplates.add(new Quest("d2", "Komm zur Ruhe", "5 Min meditieren", 40, "Willpower", 1, QuestType.DAILY, 0));
        allDailyTemplates.add(new Quest("d3", "Bleib hydriert", "2L Wasser trinken", 30, "Health", 1, QuestType.DAILY, 0));
        allDailyTemplates.add(new Quest("d4", "Frische Luft tut gut", "15 Minuten spazieren", 25, "Stamina", 1, QuestType.DAILY, 0));
    }

    // Gibt zufällig count Quests zurück (mit Ablaufzeit von 24h)
    public static List<Quest> getRandomDailyQuests(int count) {
        List<Quest> selected = new ArrayList<>();
        Collections.shuffle(allDailyTemplates);

        for (int i = 0; i < Math.min(count, allDailyTemplates.size()); i++) {
            Quest q = allDailyTemplates.get(i);

            // Ablaufzeit: jetzt + 24h
            long expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
            q.setExpiresAt(expiresAt);

            selected.add(q);
        }
        return selected;
    }
}

