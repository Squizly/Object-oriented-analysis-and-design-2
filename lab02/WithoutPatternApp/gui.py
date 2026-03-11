import sys
import subprocess
import re
from PyQt6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                             QHBoxLayout, QLineEdit, QPushButton, QTextEdit, 
                             QLabel, QGraphicsView, QGraphicsScene, QFrame, 
                             QGraphicsDropShadowEffect, QComboBox, QSpinBox, QCheckBox)
from PyQt6.QtGui import QFont, QColor, QFontMetrics
from PyQt6.QtCore import Qt, QTimer

STYLESHEET = """
QMainWindow { background-color: #1a1b26; }
QLabel { color: #a9b1d6; font-family: '-apple-system', 'Segoe UI', sans-serif; }
QLabel#Header { font-size: 16px; font-weight: bold; color: #7aa2f7; margin-bottom: 5px; margin-top: 10px; }
QLabel#StatText { font-size: 15px; font-weight: bold; color: #c0caf5; }

QFrame#SidePanel { background-color: #24283b; border-radius: 12px; border: 1px solid #414868; }
QFrame#StatsBox { background-color: #1a1b26; border-radius: 8px; border: 1px solid #414868; }

QLineEdit, QComboBox, QSpinBox {
    background-color: #16161e; color: #c0caf5;
    border: 1px solid #414868; border-radius: 6px;
    padding: 10px; font-size: 14px;
}
QLineEdit:focus, QComboBox:focus, QSpinBox:focus { border: 1px solid #7aa2f7; }

QPushButton {
    background-color: #7aa2f7; color: #16161e;
    border-radius: 8px; font-weight: bold; font-size: 15px; padding: 14px;
}
QPushButton:hover { background-color: #8ab0f8; }
QPushButton:pressed { background-color: #3d59a1; }
QPushButton:disabled { background-color: #414868; color: #565f89; }

QCheckBox { font-size: 14px; color: #a9b1d6; spacing: 8px; }
QCheckBox::indicator { width: 18px; height: 18px; border-radius: 4px; border: 1px solid #414868; background-color: #16161e; }
QCheckBox::indicator:checked { background-color: #7aa2f7; border: 1px solid #7aa2f7; }

QGraphicsView { background-color: #16161e; border: 1px solid #414868; border-radius: 12px; }
QTextEdit {
    background-color: #16161e; color: #a9b1d6; font-family: 'Menlo', monospace;
    border: 1px solid #414868; border-radius: 12px; padding: 12px; font-size: 13px;
}
"""

class NoPatternApp(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Without Flyweight - Inefficient Renderer")
        self.resize(1280, 800) 
        self.setMinimumSize(1100, 700)
        self.setStyleSheet(STYLESHEET)

        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        main_layout = QHBoxLayout(central_widget)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(20)

        # ==================== ЛЕВАЯ КОЛОНКА ====================
        side_panel = QFrame()
        side_panel.setObjectName("SidePanel")
        side_panel.setFixedWidth(400) 
        side_layout = QVBoxLayout(side_panel)
        side_layout.setContentsMargins(25, 25, 25, 25)
        side_layout.setSpacing(15)

        lbl_input = QLabel("Текст для рендера:")
        lbl_input.setObjectName("Header")
        self.input_field = QLineEdit()
        # Большой текст для проверки
        self.input_field.setText("Flyweight Pattern: Maximum Efficiency! Minimum Memory! Repeating characters are shared effortlessly. Flyweight Pattern: Maximum Efficiency! Minimum Memory! Repeating characters are shared effortlessly. Flyweight Pattern: Maximum Efficiency! Minimum Memory! Repeating characters are shared effortlessly. This design pattern is perfect for optimizing memory usage when dealing with large numbers of similar objects. Each character object shares its formatting style with other identical characters, dramatically reducing memory consumption. The factory ensures that we never create duplicate objects for the same character with the same style. Instead, we reuse existing objects, saving precious memory resources. This is particularly useful in text editors, graphical applications, and any system that needs to handle large amounts of repetitive data efficiently.")
        
        lbl_font = QLabel("Настройки шрифта:")
        lbl_font.setObjectName("Header")
        
        self.font_combo = QComboBox()
        self.font_combo.addItems(["Menlo", "Courier New", "Helvetica", "Arial", "Impact", "Georgia"])
        
        size_layout = QHBoxLayout()
        lbl_size = QLabel("Размер (px):")
        self.size_spin = QSpinBox()
        self.size_spin.setRange(10, 80)
        self.size_spin.setValue(32)
        
        self.bold_check = QCheckBox("Жирное начертание (Bold)")
        self.bold_check.setChecked(True)
        
        size_layout.addWidget(lbl_size)
        size_layout.addWidget(self.size_spin)
        
        self.btn_run = QPushButton("⚡ Сгенерировать БЕЗ паттерна")
        self.btn_run.setCursor(Qt.CursorShape.PointingHandCursor)
        self.btn_run.clicked.connect(self.run_cpp)

        lbl_stats_title = QLabel("Статистика использования памяти:")
        lbl_stats_title.setObjectName("Header")
        lbl_stats_title.setWordWrap(True)
        
        stats_box = QFrame()
        stats_box.setObjectName("StatsBox")
        stats_layout = QVBoxLayout(stats_box)
        stats_layout.setContentsMargins(15, 15, 15, 15)
        stats_layout.setSpacing(12)
        
        self.lbl_total = QLabel("Всего символов: 0")
        self.lbl_memory = QLabel("Создано объектов: 0")
        self.lbl_total_memory = QLabel("Всего памяти: 0 байт")
        self.lbl_avg_memory = QLabel("В среднем на объект: 0 байт")
        self.lbl_optimization = QLabel("Сэкономлено памяти: 0 байт")
        
        for lbl in [self.lbl_total, self.lbl_memory, self.lbl_total_memory, 
                    self.lbl_avg_memory, self.lbl_optimization]:
            lbl.setObjectName("StatText")
            stats_layout.addWidget(lbl)

        side_layout.addWidget(lbl_input)
        side_layout.addWidget(self.input_field)
        side_layout.addWidget(lbl_font)
        side_layout.addWidget(self.font_combo)
        side_layout.addLayout(size_layout)
        side_layout.addWidget(self.bold_check)
        side_layout.addSpacing(15)
        side_layout.addWidget(self.btn_run)
        side_layout.addSpacing(25)
        side_layout.addWidget(lbl_stats_title)
        side_layout.addWidget(stats_box)
        side_layout.addStretch() 

        # ==================== ПРАВАЯ КОЛОНКА ====================
        right_panel = QWidget()
        right_layout = QVBoxLayout(right_panel)
        right_layout.setContentsMargins(0, 0, 0, 0)
        right_layout.setSpacing(15)

        lbl_canvas = QLabel("Окно Рендера")
        lbl_canvas.setObjectName("Header")
        
        self.scene = QGraphicsScene()
        self.view = QGraphicsView(self.scene)
        self.view.setRenderHint(self.view.renderHints().Antialiasing)
        
        lbl_logs = QLabel("📜 Логи C++ (каждый символ - новый объект!):")
        lbl_logs.setObjectName("Header")
        
        self.log_console = QTextEdit()
        self.log_console.setReadOnly(True)
        self.log_console.setFixedHeight(250)

        right_layout.addWidget(lbl_canvas)
        right_layout.addWidget(self.view, stretch=1)
        right_layout.addWidget(lbl_logs)
        right_layout.addWidget(self.log_console)

        main_layout.addWidget(side_panel)
        main_layout.addWidget(right_panel, stretch=1)

        self.timer = QTimer()
        self.timer.timeout.connect(self.process_next_line)
        self.cpp_lines =[]
        self.current_line_idx = 0

    def run_cpp(self):
        text = self.input_field.text()
        font_name = self.font_combo.currentText()
        font_size = self.size_spin.value()
        is_bold = self.bold_check.isChecked()

        if not text: return

        self.btn_run.setEnabled(False)
        self.log_console.clear()
        self.scene.clear()
        self.lbl_total.setText("Всего символов: ...")
        self.lbl_memory.setText("Создано объектов: ...")
        self.lbl_total_memory.setText("Всего памяти: ...")
        self.lbl_avg_memory.setText("В среднем на объект: ...")
        self.lbl_optimization.setText("Сэкономлено памяти: ...")
        self.lbl_optimization.setStyleSheet("") 

        weight = QFont.Weight.Bold if is_bold else QFont.Weight.Normal
        qfont = QFont(font_name, font_size, weight)
        metrics = QFontMetrics(qfont)
        
        step_x = str(metrics.horizontalAdvance("W") * 1.1) 
        step_y = str(metrics.height() * 1.2)
        
        is_bold_str = "1" if is_bold else "0"

        # 1. Компилируем C++
        subprocess.run(["g++", "-std=c++17", "main.cpp", "-o", "backend_app"])
        
        # 2. Запускаем
        run_args = ["./backend_app", text, font_name, str(font_size), is_bold_str, step_x, step_y]
        result = subprocess.run(run_args, capture_output=True, text=True)
        
        self.cpp_lines = result.stdout.split('\n')
        self.current_line_idx = 0
        self.timer.start(20) 

    def process_next_line(self):
        if self.current_line_idx >= len(self.cpp_lines):
            self.timer.stop()
            self.btn_run.setEnabled(True)
            return

        line = self.cpp_lines[self.current_line_idx].strip()
        self.current_line_idx += 1

        if not line: return

        # В логах будет только красный цвет для новых объектов
        if "[Memory] Created NEW" in line:
            # Извлекаем информацию о памяти
            match = re.search(r"Memory used: (\d+) bytes", line)
            if match:
                memory = match.group(1)
                self.log_console.append(f'<span style="color:#f7768e;">{line}</span>')
            else:
                self.log_console.append(f'<span style="color:#f7768e;">{line}</span>')
        
        elif "-> Drawing:" in line:
            match = re.search(r"Drawing:\s+\[(.*?)\]\s+at\s+([\d\.]+)\s+([\d\.]+)", line)
            if match:
                char = match.group(1)
                x = float(match.group(2))
                y = float(match.group(3))
                self.draw_character(char, x, y)
                
        elif "Total characters:" in line:
            val = line.split(":")[-1].strip()
            self.lbl_total.setText(f"Всего символов: {val}")
            
        elif "Objects in memory:" in line:
            val = line.split(":")[-1].strip()
            self.lbl_memory.setText(f"Создано тяжелых объектов: {val}")
            self.lbl_memory.setStyleSheet("color: #f7768e;")
            
        elif "Total memory used:" in line:
            match = re.search(r"(\d+) bytes", line)
            if match:
                val = match.group(1)
                self.lbl_total_memory.setText(f"Всего памяти: {val} байт")
                self.lbl_total_memory.setStyleSheet("color: #f7768e;")
                
        elif "Average per object:" in line:
            match = re.search(r"(\d+) bytes", line)
            if match:
                val = match.group(1)
                self.lbl_avg_memory.setText(f"В среднем на объект: {val} байт")
                
        elif "Optimization: Saved" in line:
            match = re.search(r"Saved\s+(\d+)\s+duplicates", line)
            if match:
                val = match.group(1)
                self.lbl_optimization.setText(f"Сэкономлено: {val} дубликатов! ❌")
                self.lbl_optimization.setStyleSheet("color: #f7768e; font-size: 14px;")
                
        elif "Memory wasted:" in line:
            match = re.search(r"(\d+) bytes", line)
            if match:
                val = match.group(1)
                # Обновляем информацию о wasted памяти (дублируем для наглядности)
                self.lbl_optimization.setText(f"Потрачено впустую: {val} байт (нет шаринга!)")
                self.lbl_optimization.setStyleSheet("color: #f7768e; font-size: 14px; font-weight: 900;")

        scrollbar = self.log_console.verticalScrollBar()
        scrollbar.setValue(scrollbar.maximum())

    def draw_character(self, char, x, y):
        font_name = self.font_combo.currentText()
        font_size = self.size_spin.value()
        is_bold = self.bold_check.isChecked()

        weight = QFont.Weight.Bold if is_bold else QFont.Weight.Normal
        qfont = QFont(font_name, font_size, weight)

        text_item = self.scene.addText(char, qfont)
        text_item.setDefaultTextColor(QColor("#f7768e"))  # Красный цвет для неоптимизированной версии
        text_item.setPos(x, y)

        shadow = QGraphicsDropShadowEffect()
        shadow.setBlurRadius(12)
        shadow.setColor(QColor("#f7768e"))
        shadow.setOffset(0, 0)
        text_item.setGraphicsEffect(shadow)

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = NoPatternApp()
    window.show()
    sys.exit(app.exec())