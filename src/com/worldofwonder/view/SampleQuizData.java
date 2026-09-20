package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

import com.worldofwonder.model.Level;
import com.worldofwonder.model.Question;
import com.worldofwonder.model.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleQuizData {

    private final List<World> worlds;
    private final Map<Integer, List<Level>> levelsByWorld;
    private final Map<Integer, List<Question>> questionsByLevel;

    public SampleQuizData() {
        this.worlds = new ArrayList<>();
        this.levelsByWorld = new HashMap<>();
        this.questionsByLevel = new HashMap<>();
        build();
    }

    public List<World> getWorlds() {
        return Collections.unmodifiableList(worlds);
    }

    public List<Level> getLevels(int worldId) {
        List<Level> levels = levelsByWorld.get(worldId);
        return levels == null ? Collections.emptyList() : Collections.unmodifiableList(levels);
    }

    public List<Question> getQuestions(int levelId) {
        List<Question> questions = questionsByLevel.get(levelId);
        return questions == null ? Collections.emptyList() : Collections.unmodifiableList(questions);
    }

    private void addWorld(int id, String name, String description) {
        worlds.add(new World(id, name, description));
        levelsByWorld.put(id, new ArrayList<>());
    }

    private void addLevel(int id, int worldId, String name, String difficulty, int reward) {
        levelsByWorld.get(worldId).add(new Level(id, worldId, name, difficulty, reward));
        questionsByLevel.put(id, new ArrayList<>());
    }

    private void addQuestion(int levelId, String text, String a, String b, String c, String d,
                             String correct, String hint) {
        questionsByLevel.get(levelId)
                .add(new Question(0, levelId, text, a, b, c, d, correct, hint));
    }

    private void addQuestion(int levelId, String text, String a, String b, String c, String d,
                             String correct, String hint,
                             String textKm, String aKm, String bKm, String cKm, String dKm, String hintKm) {
        Question q = new Question(0, levelId, text, a, b, c, d, correct, hint);
        q.setQuestionTextKm(textKm);
        q.setOptionAKm(aKm);
        q.setOptionBKm(bKm);
        q.setOptionCKm(cKm);
        q.setOptionDKm(dKm);
        q.setHintKm(hintKm);
        questionsByLevel.get(levelId).add(q);
    }

    private void build() {
        addWorld(1, "Ancient Egypt", "Pyramids, pharaohs and the mighty Nile.");
        addWorld(2, "Outer Space", "Explore planets, stars and the galaxy.");
        addWorld(3, "The Deep Ocean", "Dive into reefs and the mysterious deep sea.");
        addWorld(4, "Dinosaur World", "Travel back in time to the prehistoric giants.");
        addWorld(5, "Medieval Kingdoms", "Enter the age of castles, knights, and quests.");
        addWorld(6, "Rainforest Adventure", "Venture into the lush jungle and meet creatures.");

        // World 1: Ancient Egypt
        addLevel(1, 1, "The Nile", "easy", 100);
        addQuestion(1, "Which river flows through Egypt?",
                "Amazon", "Nile", "Ganges", "Thames", "B", "It is the longest river in the world.",
                "តើទន្លេណាដែលហូរកាត់ប្រទេសអេហ្ស៊ីប?",
                "អាម៉ាហ្សូន", "នីល", "គង្គា", "ថេមស៍", "វាជាទន្លេដែលវែងជាងគេនៅលើពិភពលោក។");
        addQuestion(1, "What is the Egyptian sun god called?",
                "Ra", "Zeus", "Odin", "Thor", "A", "Its name starts with the letter R.",
                "តើព្រះអាទិត្យរបស់អេហ្ស៊ីបបុរាណមានឈ្មោះអ្វី?",
                "រ៉ា (Ra)", "ហ្ស៊ូស", "អូឌីន", "ថ័រ", "ឈ្មោះនេះចាប់ផ្តើមដោយព្យញ្ជនៈ រ។");
        addQuestion(1, "The ancient Egyptians wrote using...",
                "Runes", "Cuneiform", "Hieroglyphs", "Latin", "C", "They are picture symbols carved in stone.",
                "ជនជាតិអេហ្ស៊ីបបុរាណបានសរសេរអក្សរដោយប្រើ...",
                "រូន", "គុយនីហ្វម", "ហ៊ីរ៉ូគ្លីហ្វ", "ឡាតាំង", "ជាអក្សររូបភាពឆ្លាក់លើផ្ទាំងថ្ម។");

        addLevel(2, 1, "Pyramids", "medium", 150);
        addQuestion(2, "The Great Pyramid of Giza was a tomb for which pharaoh?",
                "Tutankhamun", "Ramses II", "Khufu", "Cleopatra", "C", "He built the Great Pyramid of Giza.",
                "តើមហាពីរ៉ាមីតហ្គីហ្សាត្រូវបានសាងសង់ឡើងជាផ្នូរសម្រាប់ស្តេចផារ៉ាអុងណា?",
                "ទូតង់ខាមុន", "រ៉ាមសេស ទី២", "ឃូហ្វូ (Khufu)", "ក្លេអូប៉ាត្រា", "ទ្រង់ជាអ្នកសាងសង់មហាពីរ៉ាមីតហ្គីហ្សា។");
        addQuestion(2, "The Great Sphinx has the body of a lion and the head of a...",
                "Cat", "Human", "Crocodile", "Falcon", "B", "It guards the pyramids of Giza.",
                "រូបចម្លាក់ស្ពីងដ៏ធំ (Sphinx) មានដងខ្លួនជាសត្វតោ និងក្បាលជា...",
                "ឆ្មា", "មនុស្ស", "ក្រពើ", "សត្វឥន្ទ្រី", "វាជាអ្នកយាមកាមពីរ៉ាមីតហ្គីហ្សា។");
        addQuestion(2, "Cleopatra was the last ruler of which dynasty?",
                "Ptolemaic", "Roman", "Macedonian", "Ottoman", "A", "Founded by a general of Alexander the Great.",
                "តើព្រះនាងក្លេអូប៉ាត្រាជាអ្នកគ្រប់គ្រងចុងក្រោយនៃរាជវង្សណា?",
                "តូលេម៉ាអ៊ីក", "រ៉ូម៉ាំង", "ម៉ាសេដូនី", "អូតូម៉ង់", "បង្កើតឡើងដោយមេទ័ពរបស់អាឡិចសាន់ឌឺដ៏អស្ចារ្យ។");

        // World 2: Outer Space
        addLevel(3, 2, "The Solar System", "easy", 100);
        addQuestion(3, "Which planet is closest to the Sun?",
                "Venus", "Mercury", "Earth", "Mars", "B", "It is the smallest and fastest planet.",
                "តើភពណាដែលនៅជិតព្រះអាទិត្យជាងគេ?",
                "ភពសុក្រ", "ភពពុធ", "ភពផែនដី", "ភពអង្គារ", "វាជាភពតូចជាងគេ និងវិលលឿនជាងគេ។");
        addQuestion(3, "Which planet is known as the Red Planet?",
                "Jupiter", "Saturn", "Mars", "Neptune", "C", "Named after the Roman god of war.",
                "តើភពណាដែលគេស្គាល់ថាជាភពក្រហម?",
                "ភពព្រហស្បតិ៍", "ភពសៅរ៍", "ភពអង្គារ", "ភពណិបទូន", "មានផ្ទៃដីក្រហមសម្បូរទៅដោយជាតិដែក។");
        addQuestion(3, "The Sun is made mostly of...",
                "Iron", "Hydrogen", "Oxygen", "Gold", "B", "It fuses this gas to create energy.",
                "តើព្រះអាទិត្យផ្សំឡើងភាគច្រើនពីអ្វី?",
                "ដែក", "ឧស្ម័នអ៊ីដ្រូសែន", "អុកស៊ីសែន", "មាស", "វាបង្កើតថាមពលតាមរយៈឧស្ម័ននេះ។");

        addLevel(4, 2, "Galaxies", "hard", 200);
        addQuestion(4, "The closest star to Earth is...",
                "Sirius", "Betelgeuse", "The Sun", "Proxima Centauri", "C", "It rises every morning.",
                "តើផ្កាយណាដែលនៅជិតផែនដីបំផុត?",
                "ស៊ីរីយូស", "បេតែលហ្គីស", "ព្រះអាទិត្យ", "ប្រូកស៊ីម៉ា", "វាផ្តល់ពន្លឺដល់យើងរៀងរាល់ព្រឹក។");
        addQuestion(4, "How many planets are there in our solar system?",
                "Seven", "Eight", "Nine", "Ten", "B", "Pluto used to be one of them.",
                "តើប្រព័ន្ធព្រះអាទិត្យរបស់យើងមានភពចំនួនប៉ុន្មាន?",
                "៧", "៨", "៩", "១០", "ភពភ្លុយតូត្រូវបានដកចេញកាលពីមុន។");
        addQuestion(4, "Saturn is famous for its...",
                "Moons", "Rings", "Storms", "Volcanoes", "B", "They are made of ice and rock.",
                "តើភពសៅរ៍ល្បីល្បាញដោយសារអ្វី?",
                "ព្រះច័ន្ទ", "កងវង់ជុំវិញ", "ព្យុះ", "ភ្នំភ្លើង", "កងវង់ទាំងនោះផ្សំឡើងពីដុំទឹកកក និងថ្ម។");

        // World 3: The Deep Ocean
        addLevel(5, 3, "Coral Reefs", "easy", 100);
        addQuestion(5, "Which of these is a living creature?",
                "Coral", "Rock", "Shell", "Sand", "A", "It looks like a plant but is an animal.",
                "តើមួយណាជាភាវៈមានជីវិត?",
                "ផ្កាថ្ម", "ដុំថ្ម", "សំបកខ្យង", "ខ្សាច់", "មើលទៅដូចជារុក្ខជាតិ ប៉ុន្តែវាជាសត្វសមុទ្រ។");
        addQuestion(5, "Clownfish live safely inside which creature?",
                "Sea urchin", "Sea anemone", "Starfish", "Jellyfish", "B", "Its tentacles protect the fish.",
                "តើត្រីត្លុក (Clownfish) រស់នៅដោយសុវត្ថិភាពក្នុងសត្វណា?",
                "បន្លាខ្យងសមុទ្រ", "ផ្កាអានីម៉ូនសមុទ្រ", "ផ្កាយសមុទ្រ", "ចាហួយសមុទ្រ", "ពុកមាត់របស់វាជួយការពារត្រីពីសត្រូវ។");
        addQuestion(5, "What colour is most coral reef water?",
                "Red", "Green", "Blue", "Purple", "C", "The sky reflects into it.",
                "តើទឹកជុំវិញផ្កាថ្មភាគច្រើនមានពណ៌អ្វី?",
                "ក្រហម", "បៃតង", "ខៀវថ្លា", "ស្វាយ", "ទឹកសមុទ្រថ្លាឆ្លុះបញ្ចាំងផ្ទៃមេឃ។");

        addLevel(6, 3, "Deep Sea", "medium", 150);
        addQuestion(6, "Which animal is the largest to ever live on Earth?",
                "Blue whale", "Elephant", "Megalodon", "Giant squid", "A", "It can weigh over 150 tonnes.",
                "តើសត្វណាដែលធំជាងគេបំផុតនៅលើផែនដី?",
                "ត្រីបាឡែនខៀវ", "ដំរី", "ត្រីឆ្លាមមេហ្គាឡូដុន", "មឹកយក្ស", "វាអាចមានទម្ងន់លើសពី ១៥០ តោន។");
        addQuestion(6, "A jellyfish is made mostly of...",
                "Water", "Bone", "Muscle", "Sand", "A", "It is about 95% of this.",
                "តើសត្វចាហួយសមុទ្រផ្សំឡើងភាគច្រើនពីអ្វី?",
                "ទឹក", "ឆ្អឹង", "សាច់ដុំ", "ខ្សាច់", "ប្រមាណ ៩៥% នៃរាងកាយរបស់វា។");
        addQuestion(6, "Which creature can change colour to hide from predators?",
                "Octopus", "Dolphin", "Whale", "Sea turtle", "A", "It has eight arms and three hearts.",
                "តើសត្វសមុទ្រណាដែលអាចប្តូរពណ៌ដើម្បីបន្លំភ្នែកសត្រូវ?",
                "មឹកបារាំង (Octopus)", "ផ្សោត", "បាឡែន", "អណ្តើកសមុទ្រ", "វាមានដៃ ៨ និងបេះដូង ៣។");

        // World 4: Dinosaur World
        addLevel(7, 4, "Prehistoric Giants", "medium", 150);
        addQuestion(7, "Which dinosaur was known as the King of Dinosaurs?",
                "T-Rex", "Stegosaurus", "Triceratops", "Brachiosaurus", "A", "Tyrannosaurus Rex had powerful jaws.",
                "តើសត្វដាយណូស័រណាដែលគេស្គាល់ថាជាស្តេចដាយណូស័រ?",
                "ធីរ៉ិច (T-Rex)", "ស្តេហ្គោស័រ", "ទ្រីសេរ៉ាតប", "ប្រាគីយ៉ូស័រ", "ធីរ៉ិចមានធ្មេញមុតស្រួច និងកម្លាំងខាំខ្លាំង។");
        addQuestion(7, "What caused the extinction of the dinosaurs?",
                "Asteroid Impact", "Volcanoes", "Ice Age", "Flood", "A", "It hit near Mexico 66 million years ago.",
                "តើអ្វីដែលបណ្តាលឱ្យដាយណូស័រផុតពូជ?",
                "អាចម៍ផ្កាយធ្លាក់", "ភ្នំភ្លើងផ្ទុះ", "យុគសម័យទឹកកក", "ទឹកជំនន់", "អាចម៍ផ្កាយបានធ្លាក់មកលើផែនដីកាលពី ៦៦ លានឆ្នាំមុន។");
        addQuestion(7, "The Triceratops is famous for having how many horns?",
                "One", "Two", "Three", "Four", "C", "Tri means three.",
                "តើសត្វទ្រីសេរ៉ាតប (Triceratops) មានស្នែងចំនួនប៉ុន្មាន?",
                "១", "២", "៣", "៤", "ពាក្យ ទ្រី (Tri) មានន័យថា បី។");

        // World 5: Medieval Kingdoms
        addLevel(8, 5, "Knights & Castles", "medium", 150);
        addQuestion(8, "What armor did medieval knights wear for protection?",
                "Plate Armor", "Leather Coat", "Silk Robe", "Wooden Vest", "A", "Made of forged steel plates.",
                "តើអ្នកចម្បាំងមជ្ឈិមសម័យពាក់អ្វីដើម្បីការពារខ្លួន?",
                "អាវក្រោះដែក", "អាវស្បែក", "អាវសូត្រ", "អាវកាក់ឈើ", "ធ្វើឡើងពីបន្ទះដែកថែបដើម្បីទប់ទល់នឹងដាវ។");
        addQuestion(8, "What was the water-filled ditch around a castle called?",
                "Moat", "River", "Canal", "Pond", "A", "It prevented enemies from climbing walls.",
                "តើរណ្តៅទឹកព័ទ្ធជុំវិញប្រាសាទបុរាណត្រូវបានហៅថាអ្វី?",
                "គូទឹកព័ទ្ធជុំវិញ (Moat)", "ទន្លេ", "ព្រែកជីក", "ស្រះទឹក", "វាការពារកុំឱ្យសត្រូវឡើងជញ្ជាំងបន្ទាយបានងាយ។");
        addQuestion(8, "Which legendary weapon is tied to King Arthur?",
                "Excalibur", "Mjolnir", "Trident", "Gungnir", "A", "The sword pulled from the stone.",
                "តើអាវុធរឿងព្រេងនិទានណាដែលជាប់ទាក់ទងនឹងស្តេច Arthur?",
                "ដាវអិចស្កាលីបឺ (Excalibur)", "ញញួរម្ញ៉ូលនៀ", "ត្រីសូល៍", "លំពែងហ្គូងនៀ", "ដាវវេទមន្តដែលដកចេញពីផ្ទាំងថ្ម។");

        // World 6: Rainforest Adventure
        addLevel(9, 6, "The Amazon Jungle", "hard", 200);
        addQuestion(9, "Which is the largest tropical rainforest in the world?",
                "Amazon", "Congo", "Daintree", "Borneo", "A", "It spans nine South American countries.",
                "តើព្រៃទឹកភ្លៀងត្រូពិចណាដែលធំជាងគេបំផុតនៅលើពិភពលោក?",
                "ព្រៃអាម៉ាហ្សូន", "ព្រៃកុងហ្គោ", "ព្រៃដេនទ្រី", "ព្រៃប័រណេអូ", "លាតសន្ធឹងកាត់ប្រទេសចំនួន ៩ នៅអាមេរិកខាងត្បូង។");
        addQuestion(9, "Which bird is famous for its massive colourful bill in the rainforest?",
                "Toucan", "Eagle", "Penguin", "Owl", "A", "It feeds on fruit and nests in hollow trees.",
                "តើសត្វស្លាបណាដែលមានចំពុះធំ និងពណ៌ស្រស់ឆើតឆាយក្នុងព្រៃទឹកភ្លៀង?",
                "សត្វទូកង់ (Toucan)", "ឥន្ទ្រី", "ភេនឃ្វីន", "មៀម", "វាចូលចិត្តស៊ីផ្លែឈើ និងរស់នៅក្នុងរន្ធឈើ។");
        addQuestion(9, "Why are rainforests often called the 'Lungs of the Earth'?",
                "They produce oxygen", "They make rain", "They clean rivers", "They block wind", "A", "Trees absorb carbon dioxide and release oxygen.",
                "ហេតុអ្វីបានជាព្រៃទឹកភ្លៀងត្រូវបានគេហៅថា 'សួតនៃផែនដី'?",
                "វាផលិតឧស្ម័នអុកស៊ីសែន", "វាបង្កើតភ្លៀង", "វាសម្អាតទន្លេ", "វាបាំងខ្យល់", "ដើមឈើស្រូបយកកាបូន និងបញ្ចេញអុកស៊ីសែនយ៉ាងច្រើន។");
    }
}
