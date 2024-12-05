package com.storynook;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;

public class ScoreBoard {
    
    private static ScoreboardManager manager = Bukkit.getScoreboardManager();

    private static String getBladderBarString(int bladderLevel) {
        int barLength = 10;
        int filledBars = bladderLevel/10;

        StringBuilder bladderBar = new StringBuilder();
        // bladderBar.append(color);  // Use the passed color for the filled part
        for (int i = 0; i < filledBars; i++) {
            bladderBar.append("\uE050");
        }

        // bladderBar.append(ChatColor.RESET);  // Reset color for the empty part
        for (int i = filledBars; i < barLength; i++) {
            bladderBar.append("\uE051");
        }

        return bladderBar.toString();
    }
    private static String getBowelBarString(int bowelLevel) {
        int barLength = 10;
        int filledBars = bowelLevel/10; 

        StringBuilder bowelBar = new StringBuilder();
        for (int i = 0; i < filledBars; i++) {
            bowelBar.append("\uE052");
        }

        for (int i = filledBars; i < barLength; i++) {
            bowelBar.append("\uE053");
        }

        return bowelBar.toString();
    }

    static String getUnderwearStatus(int wetness, int fullness, int type, boolean small) {
        // Define character mappings for each underwear type
        char[][] smallStages = {  
            {'\uE134', '\uE136', '\uE135', '\uE137'}, // Underwear
            {'\uE11c', '\uE124', '\uE12c', '\uE11f', '\uE127', '\uE12f'}, // Pull-up
            {'\uE11c', '\uE120', '\uE124', '\uE128', '\uE11d', '\uE11f', '\uE125', '\uE129', '\uE123', '\uE127', '\uE12b'}, // Diaper
            //I need to finish updating the thick diaper section
            {'\uE11c', '\uE120', '\uE124', '\uE128', '\uE12c', '\uE130', '\uE11d', '\uE11e', '\uE11f', '\uE113', '\uE114', '\uE115', '\uE116', '\uE117'}  // Thick diaper
        };
        
        char[][] normalStages = {  //I need to update this entire section
            {'\uE018', '\uE01a', '\uE019', '\uE01b'}, // Underwear
            {'\uE000', '\uE004', '\uE008', '\uE001', '\uE005', '\uE009'}, // Pull-up
            {'\uE000', '\uE004', '\uE008', '\uE00c', '\uE001', '\uE002', '\uE005', '\uE009', '\uE00d', '\uE006', '\uE00a', '\uE00e', '\uE00f'}, // Diaper
            {'\uE000', '\uE004', '\uE008', '\uE00c', '\uE010', '\uE014', '\uE001', '\uE002', '\uE003', '\uE005', '\uE009', '\uE00d', '\uE011', '\uE015', '\uE006', '\uE00a', '\uE00e', '\uE012', '\uE016', '\uE007', '\uE00b', '\uE00f', '\uE013', '\uE017'} // Thick diaper
        };
        
        // Select the stages based on the size of underwear
        char[] stages = small ? smallStages[type] : normalStages[type];
    
        // Determine the indices for wet and mess stages according to percentage filled
        int wetStage = (int) Math.floor((wetness / 100.0) * (stages.length - 1));
        int messStage = (int) Math.floor((fullness / 100.0) * (stages.length - 1));
    
        // Calculate the final index based on the wet and mess stages
        // The index pattern is based on how the combinations were designated in stages array
        int stageIndex = messStage + wetStage * (type == 0 ? 1 : (type + 1));
        
        // Ensure we don't go out of bounds
        stageIndex = Math.min(stageIndex, stages.length - 1);
    
        return String.valueOf(stages[stageIndex]);
    }

    // private static String getUnderwearGroupImage(int wetness, int fullness) {
    //     if (wetness == 0 && fullness == 0) return "\uE018"; // Clean
    //     if (wetness > 0 && fullness == 0) return "\uE01a"; // Wet
    //     if (wetness == 0 && fullness > 0) return "\uE019"; // Messy
    //     return "\uE01b"; // Wet & Messy
    // }

    // static String getUnderwearStatus(int wetness, int fullness, int type, boolean small) {
    //     String image = new String();

    //     char Clean = '\uE000';
    //     char Wet1 = '\uE004';
    //     char Messy1 = '\uE001';
    //     char Wet1AndMessy1 = '\uE005';

    //     char Wet2 = '\uE008';
    //     char Messy2 = '\uE002';
    //     char Wet1AndMessy2 = '\uE006';
    //     char Wet2AndMessy1 = '\uE009';
    //     char Wet2AndMessy2 = '\uE00a';

    //     char Wet3 = '\uE00c';
    //     char Messy3 = '\uE003';
    //     char Wet1AndMessy3 = '\uE007';
    //     char Wet2AndMessy3 = '\uE00b';
    //     char Wet3AndMessy1 = '\uE00d';
    //     char Wet3AndMessy2 = '\uE00e';
    //     char Wet3AndMessy3 = '\uE00f';

    //     char Wet4 = '\uE010';
    //     char Wet4AndMessy1 = '\uE011';
    //     char Wet4AndMessy2 = '\uE012';
    //     char Wet4AndMessy3 = '\uE013';

    //     char Wet5 = '\uE014';
    //     char Wet5AndMessy1 = '\uE015';
    //     char Wet5AndMessy2 = '\uE016';
    //     char Wet5AndMessy3 = '\uE017';
    //     if (small) {
    //         Clean = '\uE100';
    //         Wet1 = '\uE104';
    //         Messy1 = '\uE101';
    //         Wet1AndMessy1 = '\uE105';

    //         Wet2 = '\uE108';
    //         Messy2 = '\uE102';
    //         Wet1AndMessy2 = '\uE106';
    //         Wet2AndMessy1 = '\uE109';
    //         Wet2AndMessy2 = '\uE10a';

    //         Wet3 = '\uE10c';
    //         Messy3 = '\uE103';
    //         Wet1AndMessy3 = '\uE107';
    //         Wet2AndMessy3 = '\uE10b';
    //         Wet3AndMessy1 = '\uE10d';
    //         Wet3AndMessy2 = '\uE10e';
    //         Wet3AndMessy3 = '\uE10f';

    //         Wet4 = '\uE110';
    //         Wet4AndMessy1 = '\uE111';
    //         Wet4AndMessy2 = '\uE112';
    //         Wet4AndMessy3 = '\uE113';

    //         Wet5 = '\uE114';
    //         Wet5AndMessy1 = '\uE115';
    //         Wet5AndMessy2 = '\uE116';
    //         Wet5AndMessy3 = '\uE117';
    //     }
    //     //Underwear Grouping
    //     if (type == 0){image = getUnderwearGroupImage(wetness, fullness);}
    //     //Pull-up Grouping
    //     if (wetness == 0 && fullness == 0 && type > 0){image = "\uE000";} // Clean
    //     if (wetness > 0 && fullness == 0 && type > 0){image = "\uE01a";} //Wet
    //     if (wetness == 100 && fullness > 0 && type > 0){image = "\uE013";} //Soaked & Full
    //     if (wetness == 0 && fullness > 0 && type > 0){image = "\uE001";} //Messy
    //     return image;
    // }
    public static void createSidebar(Player player) {
            Scoreboard board = manager.getNewScoreboard();
            Objective objective = board.registerNewObjective("stats", "dummy", " ");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);  // Show on the right side of the screen

            player.setScoreboard(board);
        }
    public static void updateSidebar(Player player, PlayerStats stats) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective("stats");

        if (objective != null) {
            board.getEntries().forEach(board::resetScores);

            String bladderBar = getBladderBarString((int)stats.getBladder()); // Yellow for bladder
            String bowelsBar = getBowelBarString((int)stats.getBowels()); // Green for bowels
            String underwearImgage = getUnderwearStatus((int)stats.getDiaperWetness(), (int)stats.getDiaperFullness(), (int)stats.getUnderwearType(), false);
            // String fullnessBar = getUnderwearFullnessString((int)stats.getDiaperFullness(), ChatColor.AQUA);

            Score bladderScore = objective.getScore(bladderBar);
            bladderScore.setScore(2); 

            Score bowelScore = objective.getScore(bowelsBar);
            bowelScore.setScore(3); 

            Score UnderwearStatus = objective.getScore(underwearImgage);
            UnderwearStatus.setScore(9);
            
            // Score Overlay = objective.getScore("\uF001");
            // Overlay.setScore(8);

            // Score Logo = objective.getScore("\uF002");
            // Logo.setScore(9);

        }
    }
}
