#include <iostream>
#include <string>
#include <vector>

class Character {
private:
    std::string symbol;
    std::string fontFamily;
    float size;
    bool isBold;
    int x;
    int y;

public:
    Character(std::string s, int posX, int posY, std::string font = "Arial", float sz = 12.0f, bool bold = false) {
        this->symbol = s;
        this->x = posX;
        this->y = posY;
        this->fontFamily = font;
        this->size = sz;
        this->isBold = bold;

        std::cout << "[Memory] Created a new Object for '" << symbol << "' [" << fontFamily << ", " << size << "pt]" << std::endl;
    }

    void draw() const {
        std::cout << "Drawing : '" << symbol << "' at [" << x << "," << y << "] style: " << fontFamily;
        if (isBold) std::cout << " (Bold)";
        std::cout << std::endl;
    }
};

class TextRender {
private:
    std::vector<Character> characters;

public:
    void addChar(const Character& c) {
        characters.push_back(c);
    }

    void renderAll() const {
        for (const auto& c : this->characters) {
            c.draw();
        }
    }
};

int main() {
    std::cout << "=== === === Implementation without Flyweight === === === \n" << std::endl;
    
    TextRender render;

    std::string text = "Hello";
    int currentX = 10;

    std::cout << "Source text = '" << text << "'" << std::endl;

    for (char c : text) {
        std::string s(1, c);
        std::cout << "\nUse method TextRender::addChar('" << c << "');" << std::endl;
    
        render.addChar(Character(s, currentX, 10));
        
        currentX += 20;
    }

    std::cout << "\nUse method TextRender::renderAll();\n" << std::endl; 
    render.renderAll();

    std::cout << "\nTotal objects in memory: " << text.length() << std::endl;
    std::cout << "Each object stores its own copy of 'Arial' string." << std::endl;

    std::cout << "\n === === === === === === === === === === === \n" << std::endl;
    return 0;
}