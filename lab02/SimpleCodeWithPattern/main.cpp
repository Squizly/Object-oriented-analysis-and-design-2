#include <iostream>
#include <string>
#include <vector>

struct TextStyle {
    std::string fontName;
    float fontSize;
    bool isBold;

    TextStyle(std::string name = "Arial", float size = 12.0f, bool bold = false) {
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
        std::cout << "[Memory] Created NEW Character object for: '" << symbol << "'" << std::endl;
    }

    static int getObjectCount() {
        return objectCount;
    }

    void draw(int x, int y) const {
        std::cout << "  -> Drawing: '" << symbol << "' at [" << x << "," << y << "] "
                  << "Style: " << style->fontName << " " << style->fontSize << "pt" << std::endl;
    }
};

int Character::objectCount = 0;

class CharacterFactory {
private:
    std::vector<Character*> characters;

public:
    Character* getCharacter(std::string key, TextStyle* style) {
        for (auto c : characters) {
            if (c->symbol == key && c->style == style) {
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
    int x, y;
    Character* character;

    TextPoint(int posX, int posY, Character* c) {
        this->x = posX;
        this->y = posY;
        this->character = c;
    }

    void render() {
        character->draw(x, y);
    }
};

class TextRender {
private:
    std::vector<TextPoint*> points;

public:
    void addTextPoint(TextPoint* tp) {
        points.push_back(tp);
    }

    void renderAll() {
        for (auto p : points) {
            p->render();
        }
    }

    ~TextRender() {
        for (auto p : points) delete p;
    }
};

int main() {
    std::cout << "=== === === Flyweight Pattern Analysis === === ===\n" << std::endl;

    CharacterFactory factory;
    TextRender render;

    TextStyle* commonStyle = new TextStyle(); 

    std::string text = "Hello World";
    int currentX = 10;

    std::cout << "Text to process: '" << text << "'\n" << std::endl;

    for (char c : text) {
        std::string s(1, c);
        
        Character* sharedChar = factory.getCharacter(s, commonStyle);
        
        render.addTextPoint(new TextPoint(currentX, 10, sharedChar));
        
        currentX += 20;
    }

    std::cout << "\n--- Final Rendering ---" << std::endl; 
    render.renderAll();

    std::cout << "\n--- Memory Statistics ---" << std::endl;
    std::cout << "Total characters in text: " << text.length() << std::endl;
    std::cout << "Total Character objects in memory: " << Character::getObjectCount() << std::endl;
    
    if (Character::getObjectCount() < text.length()) {
        std::cout << "Optimization: Saved memory for " << (text.length() - Character::getObjectCount()) << " duplicate(s)." << std::endl;
    }

    std::cout << "\n=== === === === === === === === === === === ===" << std::endl;

    delete commonStyle;
    return 0;
}