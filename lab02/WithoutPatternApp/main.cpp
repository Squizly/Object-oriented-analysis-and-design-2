#include <iostream>
#include <string>
#include <vector>
#include <cstring>

// Огромный тяжеловесный объект (Без паттерна Приспособленец)
class Character {
private:
    static int objectCount; // Считаем созданные объекты
    static size_t totalMemoryAllocated; // Считаем всю выделенную память

    // Внутреннее и внешнее состояние свалено в одну кучу
    std::string symbol;
    std::string fontFamily;
    float size;
    bool isBold;
    float x;
    float y;

public:
    Character(std::string s, float posX, float posY, std::string font, float sz, bool bold) {
        this->symbol = s;
        this->x = posX;
        this->y = posY;
        this->fontFamily = font;
        this->size = sz;
        this->isBold = bold;
        
        objectCount++;
        
        // Подсчет памяти для этого объекта
        size_t memoryUsed = sizeof(Character) + 
                           symbol.capacity() * sizeof(char) + 
                           fontFamily.capacity() * sizeof(char);
        totalMemoryAllocated += memoryUsed;
        
        std::cout << "[Memory] Created NEW Character for: '" << symbol 
                  << "' | Memory used: " << memoryUsed 
                  << " bytes (object: " << sizeof(Character) 
                  << ", symbol: " << symbol.capacity() * sizeof(char) 
                  << ", font: " << fontFamily.capacity() * sizeof(char) << ")" << std::endl;
    }

    static int getObjectCount() { return objectCount; }
    static size_t getTotalMemory() { return totalMemoryAllocated; }
    static void resetMemoryStats() { 
        objectCount = 0; 
        totalMemoryAllocated = 0; 
    }

    void draw() const {
        std::cout << "  -> Drawing: [" << symbol << "] at " << x << " " << y << std::endl;
    }
};

int Character::objectCount = 0;
size_t Character::totalMemoryAllocated = 0;

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

int main(int argc, char* argv[]) {
    // Сброс статистики перед началом
    Character::resetMemoryStats();
    
    // Параметры по умолчанию
    std::string text = "No Pattern";
    std::string fontName = "Arial";
    float fontSize = 24.0f;
    bool isBold = false;
    float stepX = 20.0f; 
    float stepY = 30.0f; 

    // Читаем параметры интерфейса
    if (argc > 1) text = argv[1];
    if (argc > 2) fontName = argv[2];
    if (argc > 3) fontSize = std::stof(argv[3]);
    if (argc > 4) isBold = (std::string(argv[4]) == "1");
    if (argc > 5) stepX = std::stof(argv[5]);
    if (argc > 6) stepY = std::stof(argv[6]);

    TextRender render;
    float currentX = 20.0f;
    float currentY = 20.0f;

    for (char c : text) {
        std::string s(1, c);
        
        // ВАЖНО: Мы КАЖДЫЙ РАЗ создаем новый тяжелый объект!
        render.addChar(Character(s, currentX, currentY, fontName, fontSize, isBold));
        
        currentX += stepX;
        if (currentX > 800.0f) {
            currentX = 20.0f;
            currentY += stepY;
        }
    }

    render.renderAll();

    // Статистика (Экономии нет)
    size_t totalMemory = Character::getTotalMemory();
    size_t perObjectMemory = totalMemory / text.length();
    
    std::cout << "\n===MEMORY STATISTICS===" << std::endl;
    std::cout << "Total characters: " << text.length() << std::endl;
    std::cout << "Objects in memory: " << Character::getObjectCount() << std::endl;
    std::cout << "Total memory used: " << totalMemory << " bytes" << std::endl;
    std::cout << "Average per object: " << perObjectMemory << " bytes" << std::endl;
    std::cout << "Optimization: Saved 0 duplicates! ❌" << std::endl;
    std::cout << "Memory wasted: " << totalMemory << " bytes (no sharing!)" << std::endl;

    return 0;
}