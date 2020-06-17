package home.tuxhome.moonlight

enum class RGBPrograms(val program_id: String, val program_info: String) {
    WARM_WHITE("sun_white", "SUN WHITE (warm white)"),
    RED("red","RED (fixed color red)"),
    GREEN("green","GREEN (fixed color green)"),
    BLUE("blue","BLUE (fixed color blue)"),
    GREEN_BLUE("green_blue","GREEN-BLUE (fixed color green-blue)"),
    RED_GREEN("red_green","RED-GREEN (fixed color red-green)"),
    BLUE_RED("blue_red","BLUE-RED (fixed color blue-red)"),
    EVENING_SEA("evening_sea","EVENING SEA (slow animation red-blue)"),
    EVENING_RIVER("evening_river","EVENING RIVER (slow animation red-green)"),
    RIVIERA("riviera","RIVIERA (slow animation green-blue)"),
    NEUTRAL_WHITE("neutral_white","NEUTRAL WHITE (cold white)"),
    RAINBOW("rainbow","RAINBOW (slow animation blue-red-green)"),
    RIVER_OF_COLORS("river_of_colors","RIVER OF COLORS (rainbow followed by four seasons)"),
    DISCO("disco","DISCO (fast animation)"),
    FOUR_SEASONS("four_seasons","FOUR SEASONS (slow animation red-blue-green-violet)"),
    PARTY("party","PARTY (fast animation)"),
}