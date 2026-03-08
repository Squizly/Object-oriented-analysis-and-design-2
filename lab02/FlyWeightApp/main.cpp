#include <iostream>
#include <string>
#include <vector>

// Внутреннее состояние (Flyweight)
struct TextStyle {
    std::string fontName;
    float fontSize;
    bool isBold;

    TextStyle(std::string name, float size, bool bold) {
        this->fontName = name;
        this->fontSize = size;
        this->isBold = bold;
    }
};

class Character {
private:
    static int objectCount;

public:
    std::string symbol;
    TextStyle* style;

    Character(std::string s, TextStyle* st) {
        this->symbol = s;
        this->style = st;
        objectCount++;
        std::cout << "[Memory] Created NEW Character for: '" << symbol << "'" << std::endl;
    }

    static int getObjectCount() { return objectCount; }

    void draw(float x, float y) const {
        // Печатаем координаты с субпиксельной точностью
        std::cout << "  -> Drawing: [" << symbol << "] at " << x << " " << y << std::endl;
    }
};

int Character::objectCount = 0;

class CharacterFactory {
private:
    std::vector<Character*> characters;

public:
    Character* getCharacter(std::string key, TextStyle* style) {
        for (auto c : characters) {
            if (c->symbol == key && c->style->fontName == style->fontName && 
                c->style->fontSize == style->fontSize && c->style->isBold == style->isBold) {
                std::cout << "[Factory] Reusing existing object for: '" << key << "'" << std::endl;
                return c;
            }
        }
        Character* newCharacter = new Character(key, style);
        characters.push_back(newCharacter);
        return newCharacter;
    }

    ~CharacterFactory() {
        for (auto c : characters) delete c;
    }
};

class TextPoint {
public:
    float x, y;
    Character* character;

    TextPoint(float posX, float posY, Character* c) {
        this->x = posX; this->y = posY; this->character = c;
    }
    void render() { character->draw(x, y); }
};

class TextRender {
private:
    std::vector<TextPoint*> points;
public:
    void addTextPoint(TextPoint* tp) { points.push_back(tp); }
    void renderAll() { for (auto p : points) p->render(); }
    ~TextRender() { for (auto p : points) delete p; }
};

int main(int argc, char* argv[]) {
    std::string text = "Flyweight";
    std::string fontName = "Arial";
    float fontSize = 24.0f;
    bool isBold = false;
    float stepX = 20.0f; // Шаг по X (передается из Python)
    float stepY = 30.0f; // Шаг по Y (передается из Python)

    // Читаем все параметры интерфейса
    if (argc > 1) text = argv[1];
    if (argc > 2) fontName = argv[2];
    if (argc > 3) fontSize = std::stof(argv[3]);
    if (argc > 4) isBold = (std::string(argv[4]) == "1");
    if (argc > 5) stepX = std::stof(argv[5]);
    if (argc > 6) stepY = std::stof(argv[6]);

    CharacterFactory factory;
    TextRender render;
    TextStyle* commonStyle = new TextStyle(fontName, fontSize, isBold); 

    float currentX = 20.0f;
    float currentY = 20.0f;

    for (char c : text) {
        std::string s(1, c);
        
        Character* sharedChar = factory.getCharacter(s, commonStyle);
        render.addTextPoint(new TextPoint(currentX, currentY, sharedChar));
        
        // Сдвигаемся на идеально рассчитанный шаг
        currentX += stepX;
        
        // Перенос строки (холст примерно 800px)
        if (currentX > 800.0f) {
            currentX = 20.0f;
            currentY += stepY;
        }
    }

    render.renderAll();

    std::cout << "===STATISTICS===" << std::endl;
    std::cout << "Total characters: " << text.length() << std::endl;
    std::cout << "Objects in memory: " << Character::getObjectCount() << std::endl;
    
    if (Character::getObjectCount() < text.length()) {
        std::cout << "Optimization: Saved " << (text.length() - Character::getObjectCount()) << " duplicates!" << std::endl;
    }

    delete commonStyle;
    return 0;
}