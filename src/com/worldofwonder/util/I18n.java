package com.worldofwonder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internationalization (I18n) manager supporting English and Khmer (ភាសាខ្មែរ).
 * Provides dynamic language switching with event notification.
 */
public class I18n {

    public enum Language {
        EN("English", "English"),
        KM("ភាសាខ្មែរ", "Khmer");

        private final String nativeName;
        private final String englishName;

        Language(String nativeName, String englishName) {
            this.nativeName = nativeName;
            this.englishName = englishName;
        }

        public String getNativeName() {
            return nativeName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    private static Language currentLanguage = Language.EN;
    private static final List<Runnable> listeners = new ArrayList<>();
    private static final Map<String, Map<Language, String>> translations = new HashMap<>();

    static {
        // --- Header & Badges ---
        put("badge_adventure", "GLOBAL ADVENTURE", "ដំណើរផ្សងព្រេងពិភពលោក");
        put("tagline", "Travel the world. Answer the questions. Earn the stars.", "ធ្វើដំណើរជុំវិញពិភពលោក ឆ្លើយសំណួរ និងទទួលបានផ្កាយ។");

        // --- Tabs ---
        put("tab_login", "Login", "ចូលប្រើ");
        put("tab_register", "Register", "ចុះឈ្មោះ");

        // --- Fields & Placeholders ---
        put("placeholder_user", "Username", "ឈ្មោះអ្នកប្រើ");
        put("placeholder_pass", "Password", "ពាក្យសម្ងាត់");
        put("placeholder_email", "Email", "អ៊ីមែល");

        // --- Buttons ---
        put("btn_login", "Start Exploring", "ចាប់ផ្តើមរុករក");
        put("btn_register", "Create Account", "បង្កើតគណនី");
        put("btn_guest", "Play as Guest", "លេងជាភ្ញៀវ");
        put("btn_forgot", "Forgot password?", "ភ្លេចពាក្យសម្ងាត់?");
        put("btn_got_it", "Got it", "យល់ព្រម");
        put("btn_save_close", "Save & Close", "រក្សាទុក & បិទ");
        put("btn_close", "Close", "បិទ");
        put("btn_later", "Later", "ពេលក្រោយ");
        put("btn_cancel", "Cancel", "បោះបង់");
        put("pts", "pts", "ពិន្ទុ");

        // --- Hints & Subtitles ---
        put("guest_note", "No account needed. Progress and points won't be saved.", "មិនចាំបាច់មានគណនីទេ។ ពិន្ទុនឹងមិនត្រូវបានរក្សាទុកឡើយ។");
        put("reg_hint", "Create a profile to save your progress.", "បង្កើតប្រវត្តិរូបដើម្បីរក្សាទុកការរីកចម្រើនរបស់អ្នក។");
        put("divider_or", "or", "ឬ");

        // --- Password Reset Dialog ---
        put("recovery_badge", "ACCOUNT RECOVERY", "ការសង្គ្រោះគណនី");
        put("recovery_title", "Forgot your password?", "តើអ្នកភ្លេចពាក្យសម្ងាត់មែនទេ?");
        put("recovery_body", "<html><div style='text-align:center'>Password resets are handled by your teacher or "
                + "administrator.<br>Ask them to reset your account and you can sign in "
                + "with a fresh password right away.</div></html>",
                "<html><div style='text-align:center'>ការកំណត់ពាក្យសម្ងាត់ឡើងវិញត្រូវបានគ្រប់គ្រងដោយគ្រូ ឬអ្នកគ្រប់គ្រង (Admin)។<br>"
                + "សូមស្នើសុំឱ្យពួកគាត់កំណត់គណនីឡើងវិញ ហើយអ្នកអាចចូលប្រើ<br>ជាមួយពាក្យសម្ងាត់ថ្មីបានភ្លាមៗ។</div></html>");

        // --- Validation & Error Messages ---
        put("err_empty_login", "Please enter both username and password.", "សូមបញ្ចូលទាំងឈ្មោះអ្នកប្រើ និងពាក្យសម្ងាត់។");
        put("err_empty_register", "All fields (username, email, and password) are required.", "សូមបំពេញគ្រប់ប្រអប់ (ឈ្មោះអ្នកប្រើ អ៊ីមែល និងពាក្យសម្ងាត់)។");
        put("err_user_short", "Username must be at least 3 characters", "ឈ្មោះអ្នកប្រើត្រូវមានយ៉ាងតិច ៣ តួអក្សរ");
        put("err_user_chars", "Username can only contain letters, numbers, and underscores", "ឈ្មោះអ្នកប្រើអាចមានតែអក្សរ លេខ និងសញ្ញា _ ប៉ុណ្ណោះ");
        put("err_email_invalid", "Please enter a valid email address (e.g. user@example.com)", "សូមបញ្ចូលអាសយដ្ឋានអ៊ីមែលដែលត្រឹមត្រូវ (ឧទាហរណ៍ user@example.com)");
        put("err_pass_short", "Password must be at least 4 characters", "ពាក្យសម្ងាត់ត្រូវមានយ៉ាងតិច ៤ តួអក្សរ");
        put("err_user_not_found", "User not found. Please check spelling or register.", "រកមិនឃើញអ្នកប្រើប្រាស់ទេ។ សូមពិនិត្យ ឬចុះឈ្មោះ។");
        put("err_wrong_pass", "Incorrect password. Please try again.", "ពាក្យសម្ងាត់មិនត្រឹមត្រូវទេ។ សូមព្យាយាមម្តងទៀត។");
        put("err_user_exists", "Username already exists", "ឈ្មោះអ្នកប្រើប្រាស់នេះមានរួចហើយ");
        put("err_email_exists", "Email is already registered", "អ៊ីមែលនេះត្រូវបានចុះឈ្មោះរួចហើយ");

        // --- Success Messages ---
        put("msg_login_success", "Login successful! Welcome back, {0}!", "ការចូលប្រើបានជោគជ័យ! សូមស្វាគមន៍, {0}!");
        put("msg_register_success", "Account created successfully! Welcome, {0}!", "បង្កើតគណនីបានជោគជ័យ! សូមស្វាគមន៍, {0}!");

        // --- Dialog Titles ---
        put("title_login", "Login", "ចូលប្រើ");
        put("title_login_success", "Login Successful", "ចូលប្រើបានជោគជ័យ");
        put("title_login_failed", "Login Failed", "ការចូលប្រើបានបរាជ័យ");
        put("title_register", "Registration", "ការចុះឈ្មោះ");
        put("title_register_success", "Registration Successful", "ចុះឈ្មោះបានជោគជ័យ");
        put("title_register_failed", "Registration Failed", "ការចុះឈ្មោះបានបរាជ័យ");

        // --- Navigation & Common Actions ---
        put("back_to_games", "Back to Games", "ត្រឡប់ទៅហ្គេម");
        put("exit_to_games", "Exit to Games", "ចាកចេញទៅហ្គេម");
        put("back_to_dashboard", "Back to Dashboard", "ត្រឡប់ទៅផ្ទាំងដើម");
        put("settings", "Settings", "ការកំណត់");
        put("daily_gift", "Daily Gift", "កាដូប្រចាំថ្ងៃ");
        put("hall_of_fame", "Hall of Fame", "តារាងកិត្តិយស");
        put("admin_panel", "Admin Panel", "ផ្ទាំងគ្រប់គ្រង");
        put("logout", "Logout", "ចាកចេញ");
        put("total_points", "Total Points", "ពិន្ទុសរុប");
        put("welcome_user", "Welcome, {0}", "សូមស្វាគមន៍, {0}");
        put("rank_prefix", "Rank: {0}", "ចំណាត់ថ្នាក់: {0}");
        put("rank_novice", "Novice Explorer", "អ្នករុករកដំបូង");
        put("rank_bronze", "Bronze Adventurer", "អ្នកផ្សងព្រេងសំរិទ្ធ");
        put("rank_silver", "Silver Scholar", "អ្នកប្រាជ្ញប្រាក់");
        put("rank_gold", "Gold Master", "កំពូលអ្នកលេងមាស");
        put("rank_legendary", "Legendary Wonderer", "អ្នកអច្ឆរិយៈរឿងព្រេងនិទាន");
        put("choose_game_title", "Choose your game", "ជ្រើសរើសហ្គេមរបស់អ្នក");
        put("choose_game_sub", "Pick a destination and start earning points", "ជ្រើសរើសទិសដៅ និងចាប់ផ្តើមរកពិន្ទុ");
        put("play_now", "Play Now", "លេងឥឡូវនេះ");

        // --- Game Titles & Descriptions ---
        put("game_quiz_title", "World of Wonder Quiz", "សំណួរពិភពអច្ឆរិយៈ");
        put("game_quiz_sub", "Travel the world, answer questions, and earn points on every stop.", "ធ្វើដំណើរជុំវិញពិភពលោក ឆ្លើយសំណួរ និងរកពិន្ទុនៅគ្រប់កន្លែង។");
        put("game_wordsearch_title", "Word Search Puzzles", "ស្វែងរកពាក្យសម្ងាត់");
        put("game_wordsearch_sub", "Hunt for hidden words in a letter grid and earn points on every find.", "ស្វែងរកពាក្យលាក់ក្នុងតារាងអក្សរ និងទទួលបានពិន្ទុរាល់ពេលរកឃើញ។");
        put("game_cups_title", "Cups - Water Sort", "តម្រៀបពណ៌ទឹកក្នុងកែវ");
        put("game_cups_sub", "Pour the colored water until every cup holds a single color.", "ចាក់ទឹកពណ៌រហូតដល់កែវនីមួយៗមានពណ៌តែមួយសុទ្ធ។");
        put("game_words_title", "Words of Wonders", "ពាក្យនៃភាពអស្ចារ្យ");
        put("game_words_sub", "Connect letters, find hidden words, and complete crossword puzzles.", "ភ្ជាប់តួអក្សរ ស្វែងរកពាក្យលាក់ និងបំពេញល្បែងផ្គុំពាក្យឆ្លាស់។");

        // --- Settings Modal ---
        put("settings_title", "Game Settings & Appearance", "ការកំណត់ហ្គេម & រូបរាង");
        put("settings_sub", "Customize visual themes, switch dark/light mode, and toggle audio.", "កំណត់រូបរាង ផ្លាស់ប្តូរពន្លឺ និងបើក/បិទសម្លេង។");
        put("settings_theme_title", "Visual Theme & Color Palette", "រចនាប័ទ្មពណ៌ និងរូបរាង");
        put("settings_lang_title", "Language / ភាសា", "ភាសា / Language");
        put("settings_audio_title", "Audio & Sound Effects", "សម្លេង និងបែបផែនសម្លេង");
        put("sfx_muted", "SFX: Muted (Off)", "សម្លេង: បិទ");
        put("sfx_enabled", "SFX: Enabled (On)", "សម្លេង: បើក");
        put("test_sound", "Play Test Chime", "សាកល្បងសម្លេង");
        put("sound_muted_title", "Sound Muted", "សម្លេងត្រូវបានបិទ");
        put("lang_active", "{0} (Active)", "{0} (កំពុងប្រើ)");
        put("theme_midnight", "Midnight Nebula (Dark)", "មីដណាយ ណេប៊ុយឡា (ងងឹត)");
        put("theme_ocean", "Deep Ocean (Dark)", "មហាសមុទ្រជ្រៅ (ងងឹត)");
        put("theme_amethyst", "Royal Amethyst (Dark)", "រ៉ូយ៉ាល់ អាមេធីស (ងងឹត)");
        put("theme_daylight", "Daylight Crystal (Light)", "គ្រីស្តាល់ពន្លឺថ្ងៃ (ភ្លឺ)");
        put("theme_active", "[Active Theme]", "[រូបរាងកំពុងប្រើ]");
        put("theme_light_mode", "Light Mode", "ម៉ូដភ្លឺ");
        put("theme_dark_mode", "Dark Mode", "ម៉ូដងងឹត");

        // --- Cups Water Sort Game Screen ---
        put("choose_difficulty", "Choose Difficulty", "ជ្រើសរើសកម្រិតលំបាក");
        put("choose_diff_sub", "Select how many cups and color layers to play", "ជ្រើសរើសចំនួនកែវ និងស្រទាប់ពណ៌ដែលត្រូវលេង");
        put("diff_easy", "Easy", "ងាយស្រួល");
        put("diff_easy_desc", "4 colors • 2 empty cups", "៤ ពណ៌ • ២ កែវទទេ");
        put("diff_medium", "Medium", "មធ្យម");
        put("diff_medium_desc", "6 colors • 2 empty cups", "៦ ពណ៌ • ២ កែវទទេ");
        put("diff_hard", "Hard", "ពិបាក");
        put("diff_hard_desc", "8 colors • 2 empty cups", "៨ ពណ៌ • ២ កែវទទេ");
        put("cups_moves", "Moves: {0}", "ចំនួនលើក: {0}");
        put("cups_extra_tube", "+1 Extra Tube", "+១ កែវបន្ថែម");
        put("cups_restart", "Restart", "លេងឡើងវិញ");
        put("cups_change_diff", "Change Difficulty", "ប្តូរកម្រិតលំបាក");
        put("cups_solved_title", "PUZZLE SOLVED!", "ដោះស្រាយជោគជ័យ!");
        put("cups_solved_sub", "All colors sorted in {0} moves! +{1} Points Earned!", "តម្រៀបពណ៌បានជោគជ័យក្នុង {0} លើក! +{1} ពិន្ទុទទួលបាន!");
        put("cups_new_game", "New Game", "ហ្គេមថ្មី");
        put("tube_empty", "Empty", "ទទេ");
        put("tube_ruby", "Ruby", "ទទឹម");
        put("tube_emerald", "Emerald", "មរកត");
        put("tube_ocean", "Ocean", "សមុទ្រ");
        put("tube_gold", "Gold", "មាស");
        put("tube_amethyst", "Amethyst", "ស្វាយ");
        put("tube_tangerine", "Tangerine", "ទឹកក្រូច");
        put("tube_teal", "Teal", "ខៀវស្រងាត់");
        put("tube_orchid", "Orchid", "អ័រគីដេ");

        // --- Quiz Game Screen ---
        put("quiz_select_world", "SELECT A WONDER WORLD", "ជ្រើសរើសពិភពអច្ឆរិយៈ");
        put("quiz_select_world_sub", "Choose a continent or era to start your quiz journey", "ជ្រើសរើសទ្វីប ឬសម័យកាលដើម្បីចាប់ផ្តើមដំណើរឆ្លើយសំណួរ");
        put("quiz_select_level", "SELECT LEVEL", "ជ្រើសរើសកម្រិត");
        put("quiz_level", "Level {0}", "កម្រិត {0}");
        put("quiz_question_counter", "Question {0} of {1}", "សំណួរទី {0} នៃ {1}");
        put("quiz_score", "Score: {0}", "ពិន្ទុ: {0}");
        put("quiz_streak", "Streak: {0}", "ជាប់គ្នា: {0}");
        put("quiz_hint", "Hint (-5 pts)", "ជំនួយ (-៥ ពិន្ទុ)");
        put("quiz_lifeline", "50:50 Lifeline", "ជំនួយ ៥០:៥០");
        put("quiz_submit", "Submit Answer", "បញ្ជូនចម្លើយ");
        put("quiz_next_question", "Next Question", "សំណួរបន្ទាប់");
        put("quiz_finish_level", "Finish Level", "បញ្ចប់កម្រិត");
        put("quiz_view_results", "View Results", "មើលលទ្ធផល");
        put("quiz_victory_title", "LEVEL COMPLETE!", "បានបញ្ចប់កម្រិត!");
        put("quiz_victory_sub", "You earned {0} points! Great job!", "អ្នកទទួលបាន {0} ពិន្ទុ! ពិតជាអស្ចារ្យ!");
        put("quiz_play_again", "Play Again", "លេងម្តងទៀត");
        put("quiz_choose_world", "Choose Your World", "ជ្រើសរើសពិភពលោករបស់អ្នក");
        put("quiz_choose_world_sub", "Pick a world to begin your quiz adventure", "ជ្រើសរើសពិភពលោកដើម្បីចាប់ផ្តើមដំណើរផ្សងព្រេង");
        put("quiz_choose_level_sub", "Pick a level to start the quiz", "ជ្រើសរើសកម្រិតដើម្បីចាប់ផ្តើមសំណួរ");
        put("quiz_levels_title", "{0} Levels", "កម្រិត {0}");
        put("quiz_back_levels", "Back to Levels", "ត្រឡប់ទៅកម្រិត");
        put("quiz_another_world", "Choose Another World", "ជ្រើសរើសពិភពផ្សេងទៀត");
        put("quiz_pts_reward", "{0} points", "{0} ពិន្ទុ");
        put("quiz_correct_feedback", "Correct!{0} +{1} points", "ត្រឹមត្រូវ!{0} +{1} ពិន្ទុ");
        put("quiz_wrong_feedback", "Not quite. The answer is {0}.", "មិនត្រឹមត្រូវទេ។ ចម្លើយគឺ {0}។");
        put("quiz_pick_answer", "Pick an answer first!", "សូមជ្រើសរើសចម្លើយជាមុនសិន!");
        put("quiz_time_up", "Time's up! The correct answer was {0}.", "អស់ពេលហើយ! ចម្លើយត្រឹមត្រូវគឺ {0}។");
        put("quiz_lifeline_used", "50:50 Lifeline used! Two incorrect choices removed.", "បានប្រើជំនួយ ៥០:៥០! ជម្រើសខុសពីរត្រូវបានដកចេញ។");
        put("quiz_total_earned", "Total Earned: +{0} points!", "ពិន្ទុសរុបដែលទទួលបាន: +{0} ពិន្ទុ!");
        put("world_1_name", "Ancient Egypt", "អេហ្ស៊ីបបុរាណ");
        put("world_1_desc", "Explore the mysteries of the pharaohs and pyramids.", "ស្វែងយល់ពីអាថ៌កំបាំងនៃស្តេចផារ៉ាអុង និងពីរ៉ាមីត។");
        put("world_2_name", "Outer Space", "លំហអាកាស");
        put("world_2_desc", "Journey through the cosmos and discover stars.", "ធ្វើដំណើរឆ្លងកាត់ចក្រវាល និងស្វែងយល់ពីតារា។");
        put("world_3_name", "The Deep Ocean", "មហាសមុទ្រជ្រៅ");
        put("world_3_desc", "Dive into the abyss and discover marine wonders.", "មុជទឹកជ្រៅដើម្បីស្វែងយល់ពីពិភពក្រោមបាតសមុទ្រ។");
        put("world_4_name", "Dinosaur World", "ពិភពដាយណូស័រ");
        put("world_4_desc", "Travel back in time to the prehistoric giants.", "ធ្វើដំណើរត្រឡប់ទៅយុគសម័យសត្វយក្សបុរេប្រវត្តិ។");
        put("world_5_name", "Medieval Kingdoms", "រាជាណាចក្រមជ្ឈិមសម័យ");
        put("world_5_desc", "Enter the age of castles, knights, and quests.", "ចូលទៅកាន់យុគសម័យប្រាសាទ អ្នកក្លាហាន និងបេសកកម្ម។");
        put("world_6_name", "Rainforest Adventure", "ដំណើរផ្សងព្រេងព្រៃទឹកភ្លៀង");
        put("world_6_desc", "Venture into the lush jungle and meet creatures.", "ធ្វើដំណើរចូលទៅក្នុងព្រៃក្រាស់ និងសត្វព្រៃអស្ចារ្យ។");

        put("level_1_name", "The Nile", "ទន្លេនីល");
        put("level_2_name", "Pyramids", "ពីរ៉ាមីត");
        put("level_3_name", "The Solar System", "ប្រព័ន្ធព្រះអាទិត្យ");
        put("level_4_name", "Deep Space", "លំហជ្រៅ");
        put("level_5_name", "Coral Reefs", "ថ្មប៉ប្រះទឹកផ្កាថ្ម");
        put("level_6_name", "The Twilight Zone", "តំបន់បាតសមុទ្រងងឹត");
        put("level_7_name", "Herbivores", "សត្វស៊ីរុក្ខជាតិ");
        put("level_8_name", "Predators", "សត្វស៊ីសាច់ជាអាហារ");
        put("level_9_name", "Castles & Siege", "ប្រាសាទ និងការឡោមព័ទ្ធ");
        put("level_10_name", "Knights & Lore", "អ្នកក្លាហាន និងរឿងព្រេង");
        put("level_11_name", "Canopy Creatures", "សត្វព្រៃលើចុងឈើ");
        put("level_12_name", "Jungle Secrets", "អាថ៌កំបាំងព្រៃជ្រៅ");

        // --- Word Search Game Screen ---
        put("ws_diff_sub", "Select grid size and search directions", "ជ្រើសរើសទំហំតារាង និងទិសដៅស្វែងរក");
        put("ws_easy_desc", "Small grid, horizontal and vertical only", "តារាងតូច ផ្តេក និងបញ្ឈរប៉ុណ្ណោះ");
        put("ws_medium_desc", "Medium grid, includes diagonal words", "តារាងមធ្យម រួមទាំងពាក្យទ្រេត");
        put("ws_hard_desc", "Large grid, all directions including backwards", "តារាងធំ គ្រប់ទិសដៅរួមទាំងបញ្ច្រាស");
        put("ws_remaining", "Remaining: {0} words", "នៅសល់: {0} ពាក្យ");
        put("ws_radar_hint", "Radar Hint (-10 pts)", "រ៉ាដាជំនួយ (-១០ ពិន្ទុ)");
        put("ws_find_words", "Find the words hidden in the grid!", "ស្វែងរកពាក្យដែលលាក់ក្នុងតារាង!");
        put("ws_solved_title", "PUZZLE COMPLETED!", "បានបញ្ចប់ការផ្គុំពាក្យ!");
        put("ws_solved_sub", "All hidden words found! +{0} Points!", "រកឃើញពាក្យលាក់ទាំងអស់ហើយ! +{0} ពិន្ទុ!");
        put("ws_words_to_find", "Words to find", "ពាក្យត្រូវស្វែងរក");
        put("ws_words_found", "{0} / {1} found", "រកឃើញ {0} / {1}");
        put("ws_pts_earned", "+{0} pts", "+{0} ពិន្ទុ");
        put("ws_found_word", "Found {0}! +{1} points", "រកឃើញ {0}! +{1} ពិន្ទុ");
        put("ws_all_found", "You found all {0} words!", "អ្នកបានរកឃើញពាក្យទាំង {0} ហើយ!");

        // --- Words of Wonders Game Screen ---
        put("choose_puzzle", "Choose your puzzle", "ជ្រើសរើសល្បែងផ្គុំរបស់អ្នក");
        put("wow_diff_sub", "Select word difficulty and letter length", "ជ្រើសរើសកម្រិតពាក្យ និងប្រវែងតួអក្សរ");
        put("wow_bonus_jar", "Bonus Words: {0}", "ពាក្យបន្ថែម: {0}");
        put("wow_shuffle", "Shuffle", "ច្របល់");
        put("wow_hint", "Hint (-10 pts)", "ជំនួយ (-១០ ពិន្ទុ)");
        put("wow_reveal", "Reveal Letter (-15 pts)", "បើកអក្សរ (-១៥ ពិន្ទុ)");
        put("wow_solved_title", "WONDER CONQUERED!", "ដោះស្រាយភាពអស្ចារ្យបានសម្រេច!");
        put("wow_solved_sub", "Crossword completed! +{0} Points!", "បំពេញពាក្យឆ្លាស់ជោគជ័យ! +{0} ពិន្ទុ!");
        put("wow_words_to_find", "{0} letters • {1} words to find", "{0} តួអក្សរ • {1} ពាក្យត្រូវស្វែងរក");
        put("wow_clear", "Clear", "សម្អាត");
        put("wow_solved_sub_desc", "Amazing word-finding skills!", "ជំនាញស្វែងរកពាក្យដ៏អស្ចារ្យ!");
        put("wow_found_word", "Found: {0}! +{1} pts", "រកឃើញ: {0}! +{1} ពិន្ទុ");
        put("wow_letter_revealed", "Letter revealed in #{0}! -{1} pts", "បានបើកអក្សរក្នុងពាក្យទី {0}! -{1} ពិន្ទុ");
        put("wow_word_revealed", "Revealed: {0}! net {1} pts", "បានបើកពាក្យ: {0}! ទទួលបាន {1} ពិន្ទុ");
        put("wow_progress", "{0} / {1} words", "{0} / {1} ពាក្យ");

        // --- Leaderboard & Daily Reward Modals ---
        put("leaderboard_title", "Hall of Fame - Top Adventurers", "តារាងកិត្តិយស - កំពូលអ្នកផ្សងព្រេង");
        put("leaderboard_sub", "Live global rankings across all games & challenges", "ចំណាត់ថ្នាក់សកលផ្ទាល់នៅគ្រប់ហ្គេម & ការប្រកួតប្រជែង");
        put("daily_modal_title", "Daily Treasure Reward", "រង្វាន់កំណប់ប្រចាំថ្ងៃ");
        put("daily_claimed_today", "CLAIMED TODAY", "បានបើករួចរាល់ថ្ងៃនេះ");
        put("daily_claim_button", "Claim +{0} Coins", "បើកយក +{0} កាក់");
        put("daily_come_back", "Come back tomorrow for your next reward!", "សូមត្រឡប់មកវិញនៅថ្ងៃស្អែកដើម្បីទទួលរង្វាន់បន្ទាប់!");
        put("daily_ready", "Your daily gift is ready to open!", "កាដូប្រចាំថ្ងៃរបស់អ្នករួចរាល់សម្រាប់បើកហើយ!");
        put("daily_desc", "Log in every day to collect free coins!", "ចូលលេងរាល់ថ្ងៃដើម្បីប្រមូលកាក់ឥតគិតថ្លៃ!");
        put("daily_coins_amount", "+{0} Coins", "+{0} កាក់");

        // --- Admin Control Modal ---
        put("admin_modal_title", "Admin Control Center", "មជ្ឈមណ្ឌលគ្រប់គ្រង Admin");
        put("admin_modal_sub", "User accounts, points adjustments, and roles", "គណនីអ្នកប្រើប្រាស់ ការកែប្រែពិន្ទុ និងតួនាទី");
        put("admin_search_user", "Search by username or email...", "ស្វែងរកតាមឈ្មោះ ឬអ៊ីមែល...");
        put("admin_create_user", "Create User", "បង្កើតអ្នកប្រើ");
        put("admin_edit_user", "Edit", "កែប្រែ");
        put("admin_delete_user", "Delete", "លុប");
        put("admin_reset_pts", "Reset Pts", "កំណត់ពិន្ទុឡើងវិញ");
        put("admin_refresh", "Refresh", "ផ្ទុកឡើងវិញ");
        put("admin_col_id", "ID", "ល.រ");
        put("admin_col_user", "Username", "ឈ្មោះអ្នកប្រើ");
        put("admin_col_email", "Email", "អ៊ីមែល");
        put("admin_col_points", "Points", "ពិន្ទុ");
        put("admin_col_rank", "Rank", "ចំណាត់ថ្នាក់");
        put("admin_col_role", "Role", "តួនាទី");
        put("admin_col_claim", "Last Claim", "បើកចុងក្រោយ");
        put("admin_role_admin", "ADMIN", "អ្នកគ្រប់គ្រង");
        put("admin_role_player", "Player", "អ្នកលេង");
        put("admin_never", "Never", "មិនធ្លាប់");

        // --- Tooltips ---
        put("tip_show_password", "Show secret password", "បង្ហាញពាក្យសម្ងាត់");
        put("tip_hide_password", "Hide secret password", "លាក់ពាក្យសម្ងាត់");
        put("tip_language", "Switch language / ប្តូរភាសា", "Switch language / ប្តូរភាសា");

        // --- API & Online Features ---
        put("api_loading", "Loading...", "កំពុងផ្ទុក...");
        put("api_offline", "Offline Mode", "មុខងារក្រៅបណ្តាញ");
        put("api_error", "Connection error. Using offline data.", "មានបញ្ហាក្នុងការតភ្ជាប់។ កំពុងប្រើទិន្នន័យក្រៅបណ្តាញ។");
        put("api_fetching", "Fetching new questions...", "កំពុងទាញយកសំណួរថ្មី...");

        // --- Speed Scoring & Streak ---
        put("speed_bonus", "Speed Bonus: +{0}", "ពិន្ទុល្បឿន: +{0}");
        put("streak_multiplier", "{0}x Streak!", "{0}x ជាប់គ្នា!");
        put("streak_broken", "Streak broken!", "ជាប់គ្នាត្រូវបានបាត់!");
        put("time_remaining", "Time: {0}s", "ពេល: {0}វិ");
        put("time_up", "Time's up!", "អស់ពេលហើយ!");

        // --- Power-Ups ---
        put("powerup_5050", "50:50", "៥០:៥០");
        put("powerup_freeze", "Freeze Time", "បង្កកពេល");
        put("powerup_second_chance", "2nd Chance", "ឱកាសម្តងទៀត");
        put("powerup_skip", "Skip", "រំលង");
        put("powerup_used", "Power-up used!", "បានប្រើថាមពលពិសេស!");
        put("powerup_no_coins", "Not enough coins!", "មិនមានកាក់គ្រប់គ្រាន់!");

        // --- Hearts System ---
        put("hearts_remaining", "Hearts: {0}/5", "បេះដូង: {0}/៥");
        put("hearts_lost", "Lost a heart!", "បាត់បេះដូង!");
        put("hearts_empty", "No hearts left! Wait or practice to restore.", "អស់បេះដូងហើយ! សូមរង់ចាំ ឬហាត់ដើម្បីស្ដារឡើងវិញ។");
        put("hearts_restored", "Hearts restored!", "បេះដូងត្រូវបានស្ដារឡើងវិញ!");

        // --- Daily Streak ---
        put("streak_title", "Daily Streak", "ការចូលប្រចាំថ្ងៃ");
        put("streak_days", "{0} day streak!", "ជាប់គ្នា {0} ថ្ងៃ!");
        put("streak_reward", "+{0} hint coins earned!", "+{0} កាក់ជំនួយទទួលបាន!");
        put("streak_frozen", "Streak preserved with freeze!", "បានរក្សាការចូលប្រចាំថ្ងៃ!");
        put("hint_coins", "Hint Coins: {0}", "កាក់ជំនួយ: {0}");

        // --- Avatar ---
        put("avatar_loading", "Loading avatar...", "កំពុងផ្ទុករូបតំណាង...");
        put("avatar_default", "Default Avatar", "រូបតំណាងដើម");

        // --- Encyclopedia / World Codex ---
        put("encyclopedia_title", "World Encyclopedia", "សព្វវចនាធិប្បាយពិភពលោក");
        put("encyclopedia_sub", "Discover fascinating facts about each world", "ស្វែងយល់ពីការពិតគួរឱ្យចាប់អារម្មណ៍អំពីពិភពនីមួយៗ");
        put("encyclopedia_unlock", "Beat levels to unlock more!", "ឈ្នះកម្រិតដើម្បីដោះសោបន្ថែម!");
        put("encyclopedia_locked", "Locked", "ចាក់សោ");
        put("encyclopedia_read_more", "Read More", "អានបន្ថែម");

        // --- Leaderboard Online ---
        put("leaderboard_local", "Local", "មូលដ្ឋាន");
        put("leaderboard_global", "Global", "ពិភពលោក");
        put("leaderboard_loading", "Loading global scores...", "កំពុងផ្ទុកពិន្ទុពិភពលោក...");
        put("leaderboard_offline", "Global leaderboard unavailable offline", "តារាងពិន្ទុពិភពលោកមិនអាចប្រើក្រៅបណ្តាញ");

        // --- Match Game ---
        put("game_match_title", "Match Master", "ផ្គូផ្គងម៉ាស្ទ័រ");
        put("game_match_sub", "Match words to definitions before time runs out!", "ផ្គូផ្គងពាក្យទៅនិយមន័យមុនពេលអស់ពេល!");
        put("match_time", "Time: {0}s", "ពេល: {0}វិ");
        put("match_pairs", "Pairs: {0}/{1}", "គូ: {0}/{1}");
        put("match_complete", "All pairs matched!", "បានផ្គូផ្គងគូទាំងអស់!");
        put("match_timeout", "Time's up! {0}/{1} pairs matched.", "អស់ពេលហើយ! {0}/{1} គូត្រូវបានផ្គូផ្គង។");

        // --- World Wheel ---
        put("wheel_title", "Spin the Wonder Wheel!", "វិលកង់អច្ឆរិយៈ!");
        put("wheel_sub", "Spin to pick a random world for bonus XP!", "វិលដើម្បីជ្រើសរើសពិភពចៃដន្យសម្រាប់ XP បន្ថែម!");
        put("wheel_spin", "SPIN!", "វិល!");
        put("wheel_result", "You got: {0}! Double XP activated!", "អ្នកទទួលបាន: {0}! ពិន្ទុទ្វេដងត្រូវបានដំណើរការ!");

        // --- Fun Facts ---
        put("did_you_know", "Did you know?", "តើអ្នកដឹងទេ?");
        put("fun_fact_loading", "Loading fun fact...", "កំពុងផ្ទុកការពិតគួរឱ្យចាប់អារម្មណ៍...");
        put("score_fact", "You scored {0}! {1}", "អ្នកទទួលបាន {0} ពិន្ទុ! {1}");

        // --- Bonus Words ---
        put("bonus_words_title", "Bonus Words Jar", "ពាក្យបន្ថែម");
        put("bonus_word_found", "Bonus word found: {0}! +{1} coins", "ពាក្យបន្ថែមត្រូវបានរកឃើញ: {0}! +{1} កាក់");
        put("bonus_words_count", "Bonus: {0}", "បន្ថែម: {0}");

        // --- Word Definition ---
        put("definition_title", "Word Definition", "និយមន័យពាក្យ");
        put("definition_not_found", "Definition not available", "មិនមាននិយមន័យទេ");
        put("definition_phonetic", "Pronunciation: {0}", "ការបញ្ចេញសម្លេង: {0}");
    }

    private static void put(String key, String en, String km) {
        Map<Language, String> map = new HashMap<>();
        map.put(Language.EN, en);
        map.put(Language.KM, km);
        translations.put(key, map);
    }

    public static synchronized Language getLanguage() {
        return currentLanguage;
    }

    public static synchronized void setLanguage(Language lang) {
        if (lang != null && lang != currentLanguage) {
            currentLanguage = lang;
            for (Runnable listener : new ArrayList<>(listeners)) {
                try {
                    listener.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized void toggleLanguage() {
        setLanguage(currentLanguage == Language.EN ? Language.KM : Language.EN);
    }

    public static synchronized void addLanguageListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static synchronized void removeLanguageListener(Runnable listener) {
        listeners.remove(listener);
    }

    public static String get(String key) {
        Map<Language, String> map = translations.get(key);
        if (map != null) {
            String val = map.get(currentLanguage);
            if (val != null) return val;
            String enVal = map.get(Language.EN);
            if (enVal != null) return enVal;
        }
        return key;
    }

    public static String get(String key, Object... args) {
        String template = get(key);
        if (args == null || args.length == 0) return template;
        for (int i = 0; i < args.length; i++) {
            template = template.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return template;
    }

    public static boolean isKhmer() {
        return currentLanguage == Language.KM;
    }
}

