package com.storynook;

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

static String getUnderwearStatus(int wetness, int fullness, int type, int size) {
    // Define character mappings for each underwear type
    char[][] smallStages = { //DONE
        {'\uE134', '\uE136', '\uE135', '\uE137'}, // Underwear DONE
        {'\uE11c', '\uE124', '\uE12c', '\uE130', '\uE11f', '\uE127', '\uE12f', '\uE133'}, // Pull-up DONE
        {'\uE11c', '\uE120', '\uE124', '\uE12c', '\uE130', '\uE11d', '\uE11f', '\uE121', '\uE125', '\uE12d', '\uE131', '\uE123', '\uE127', '\uE12f', '\uE133'}, // Diaper DONE
        {'\uE11c', '\uE120', '\uE124', '\uE128', '\uE12c', '\uE130', '\uE11d', '\uE11e', '\uE11f', '\uE121', '\uE125', '\uE129', '\uE12d', '\uE131', '\uE122', '\uE126', '\uE12a', '\uE12e', '\uE132', '\uE123', '\uE127', '\uE12b', '\uE12f', '\uE133'} //Thick Diaper DONE
    };//DONE
    
    char[][] normalStages = {//DONE
        {'\uE018', '\uE01a', '\uE019', '\uE01b'}, //Underwear DONE
        {'\uE000', '\uE008', '\uE010', '\uE014', '\uE003', '\uE00b', '\uE013', '\uE017'}, // Pull-up DONE
        {'\uE000', '\uE004', '\uE008', '\uE010', '\uE014', '\uE001', '\uE003', '\uE005', '\uE009', '\uE011', '\uE015', '\uE007', '\uE00b', '\uE013', '\uE017'}, // Diaper DONE
        {'\uE000', '\uE004', '\uE008', '\uE00c', '\uE010', '\uE014', '\uE001', '\uE002', '\uE003', '\uE005', '\uE009', '\uE00d', '\uE011', '\uE015', '\uE006', '\uE00a', '\uE00e', '\uE012', '\uE016', '\uE007', '\uE00b', '\uE00f', '\uE013', '\uE017'} //Thick Diaper DONE
    };//DONE
    char[][] bigStages = {//DONE
        {'\uE034', '\uE036', '\uE035', '\uE037'}, //Underwear DONE
        {'\uE01c', '\uE024', '\uE02c', '\uE030', '\uE01f', '\uE027', '\uE02f', '\uE033'}, // Pull-up DONE
        {'\uE01c', '\uE020', '\uE024', '\uE02c', '\uE030', '\uE01d', '\uE01f', '\uE021', '\uE025', '\uE02d', '\uE031', '\uE023', '\uE027', '\uE02f', '\uE033'}, // Diaper DONE
        {'\uE01c', '\uE020', '\uE024', '\uE028', '\uE02c', '\uE030', '\uE01d', '\uE01e', '\uE01f', '\uE021', '\uE025', '\uE029', '\uE02d', '\uE031', '\uE022', '\uE026', '\uE02a', '\uE02e', '\uE032', '\uE023', '\uE027', '\uE02b', '\uE02f', '\uE033'} //Thick Diaper DONE
    };//DONE
    char[] stages;
    // Select the appropriate stage array
    switch (size) {
        case 0:
            stages = smallStages[type];
            break;
        case 1:
            stages = normalStages[type];
            break;
        case 2:
            stages = bigStages[type];
            break;
        default:
            stages = null;
            break;
    }
    int[][] stageMapping = {
        {1, 1}, // Type 0: Underwear
        {3, 1}, // Type 1: Pull-up
        {4, 2}, // Type 2: Diaper
        {5, 3}  // Type 3: Thick Diaper
    };
    // Define maximum stages for wetness and fullness
    int maxWetnessLevels = stageMapping[type][0];
    int maxFullnessLevels = stageMapping[type][1];

    // Normalize wetness and fullness values to stages
    int wetStage = Math.min(wetness / (100 / maxWetnessLevels), maxWetnessLevels-1);
    int messStage = Math.min(fullness / (100 / maxFullnessLevels), maxFullnessLevels-1);

    int stageIndex;

    // Check the specific scenarios of wetting and messing
    if(wetness > 0 && fullness > 0) {
        // When both wetness and fullness are present
        int startingIndex = 0;
        messStage++;
        if(type == 3){
            if (messStage == 1){startingIndex = 9;}
            if (messStage == 2) {startingIndex = 14;}
            if(messStage == 3){startingIndex = 19;}
        }
        if(type == 2){
            if (messStage == 1){startingIndex = 7;}
            if (messStage == 2) {startingIndex = 11;}
        }
        if(type == 1){
            startingIndex = 5;
        }
        stageIndex = startingIndex + wetStage;
    } else if(wetness > 0) {
        // Wetness only
        stageIndex = wetStage;
    } else if (fullness > 0) {
        // Fullness only
        stageIndex = maxWetnessLevels + 1 + messStage;
        // stageIndex = messStage;
    } else {
        // Handle the case where there is neither wetness nor fullness (default state)
        stageIndex = 0;
    }

    // Ensure the index does not exceed the bounds of the stages array
    stageIndex = Math.min(stageIndex, stages.length - 1);
    
    // Return the corresponding character
    return String.valueOf(stages[stageIndex]);
}
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

            String bladderBar = getBladderBarString((int)stats.getBladder());
            String underwearImgage = getUnderwearStatus((int)stats.getDiaperWetness(), (int)stats.getDiaperFullness(), (int)stats.getUnderwearType(), 1);

            Score UnderwearStatus = objective.getScore(underwearImgage);
            UnderwearStatus.setScore(2);

            Score bladderScore = objective.getScore(bladderBar);
            bladderScore.setScore(0); 
            
            if(stats.getMessing()){
                String bowelsBar = getBowelBarString((int)stats.getBowels());
                Score bowelScore = objective.getScore(bowelsBar);
                bowelScore.setScore(1); 
            }
            
            // Score Overlay = objective.getScore("\uF001");
            // Overlay.setScore(8);

            // Score Logo = objective.getScore("\uF002");
            // Logo.setScore(9);

        }
    }
}
