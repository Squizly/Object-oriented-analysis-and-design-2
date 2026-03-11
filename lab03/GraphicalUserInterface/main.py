import customtkinter as ctk
from tkinter import messagebox
from models import University, Student, Lecturer
from data_manager import DataManager

ctk.set_appearance_mode("Dark")
ctk.set_default_color_theme("blue")

BG_COLOR = "#18181B"
PANEL_BG = "#27272A"
SIDEBAR_BG = "#09090B"
TEXT_MAIN = "#F4F4F5"
TEXT_MUTED = "#A1A1AA"
ACCENT_STUDENT = "#3B82F6"
ACCENT_TEACHER = "#8B5CF6"

class App(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Университет PRO | БЕЗ ПАТТЕРНА") # Изменено название
        self.geometry("1280x800")
        self.configure(fg_color=BG_COLOR)
        
        self.univer = University()
        self.selected_person = None
        self.edit_entries = {}

        self.setup_ui()
        # Автозагрузка убрана. На старте пустой дашборд.

    def setup_ui(self):
        self.grid_columnconfigure(1, weight=1)
        self.grid_rowconfigure(0, weight=1)

        # === БОКОВОЕ МЕНЮ ===
        self.sidebar = ctk.CTkFrame(self, width=240, corner_radius=0, fg_color=SIDEBAR_BG)
        self.sidebar.grid(row=0, column=0, sticky="nsew")
        self.sidebar.grid_rowconfigure(5, weight=1)

        ctk.CTkLabel(self.sidebar, text="🏛️ UniverPRO", font=("Segoe UI", 24, "bold"), text_color=TEXT_MAIN).pack(pady=(30, 40))

        ctk.CTkLabel(self.sidebar, text="БАЗА ДАННЫХ", font=("Segoe UI", 12, "bold"), text_color=TEXT_MUTED, anchor="w").pack(fill="x", padx=20, pady=(0, 10))
        self.create_menu_btn(self.sidebar, "📥 Загрузить (Excel)", self.load_db, "#2563EB")
        self.create_menu_btn(self.sidebar, "💾 Сохранить (Excel)", self.save_db, "#D97706")

        ctk.CTkLabel(self.sidebar, text="ЭКСПОРТ (БЕЗ ПАТТЕРНА)", font=("Segoe UI", 12, "bold"), text_color=TEXT_MUTED, anchor="w").pack(fill="x", padx=20, pady=(30, 10))
        self.create_menu_btn(self.sidebar, "📊 Прямой экспорт в Excel", self.direct_excel, "#059669")
        self.create_menu_btn(self.sidebar, "📝 Прямой экспорт в TXT", self.direct_txt, "#0891B2")

        self.log_box = ctk.CTkTextbox(self.sidebar, height=150, font=("Consolas", 11), fg_color="#1F1F22", text_color="#EAB308", corner_radius=10)
        self.log_box.pack(side="bottom", fill="x", padx=15, pady=20)
        ctk.CTkLabel(self.sidebar, text="Системный лог:", font=("Segoe UI", 11), text_color=TEXT_MUTED, anchor="w").pack(side="bottom", fill="x", padx=20)

        # === ОСНОВНОЙ КОНТЕНТ ===
        self.main_view = ctk.CTkFrame(self, fg_color="transparent")
        self.main_view.grid(row=0, column=1, sticky="nsew", padx=20, pady=20)
        self.main_view.grid_rowconfigure(1, weight=1)
        self.main_view.grid_columnconfigure(0, weight=4)
        self.main_view.grid_columnconfigure(1, weight=3)

        # --- ДАШБОРД ---
        self.dashboard_frame = ctk.CTkFrame(self.main_view, fg_color="transparent", height=100)
        self.dashboard_frame.grid(row=0, column=0, columnspan=2, sticky="ew", pady=(0, 20))
        self.dashboard_frame.grid_columnconfigure((0, 1, 2, 3), weight=1)
        
        self.stat_total = self.create_stat_card(self.dashboard_frame, 0, "👥 Всего людей", "0")
        self.stat_students = self.create_stat_card(self.dashboard_frame, 1, "👨‍🎓 Студентов", "0", ACCENT_STUDENT)
        self.stat_teachers = self.create_stat_card(self.dashboard_frame, 2, "👨‍🏫 Преподавателей", "0", ACCENT_TEACHER)
        self.stat_gpa = self.create_stat_card(self.dashboard_frame, 3, "⭐ Средний GPA", "0.0")

        # --- СПИСОК ---
        self.list_frame = ctk.CTkScrollableFrame(self.main_view, fg_color=PANEL_BG, corner_radius=15)
        self.list_frame.grid(row=1, column=0, sticky="nsew", padx=(0, 10))

        # --- РЕДАКТОР ---
        self.edit_container = ctk.CTkFrame(self.main_view, fg_color=PANEL_BG, corner_radius=15)
        self.edit_container.grid(row=1, column=1, sticky="nsew")
        
        self.placeholder_lbl = ctk.CTkLabel(self.edit_container, text="👈 Выберите человека из списка\nдля просмотра и редактирования", font=("Segoe UI", 16), text_color=TEXT_MUTED)
        self.placeholder_lbl.place(relx=0.5, rely=0.5, anchor="center")

        self.edit_form = ctk.CTkFrame(self.edit_container, fg_color="transparent")

    def create_menu_btn(self, parent, text, command, color):
        btn = ctk.CTkButton(parent, text=text, command=command, fg_color=color, hover_color="#3F3F46", 
                            font=("Segoe UI", 13, "bold"), anchor="w", height=40, corner_radius=8)
        btn.pack(fill="x", padx=15, pady=5)
        return btn

    def create_stat_card(self, parent, col, title, value, color=TEXT_MAIN):
        card = ctk.CTkFrame(parent, fg_color=PANEL_BG, corner_radius=15, height=90)
        card.grid(row=0, column=col, sticky="ew", padx=5)
        card.grid_propagate(False)
        ctk.CTkLabel(card, text=title, font=("Segoe UI", 12, "bold"), text_color=TEXT_MUTED).place(relx=0.1, rely=0.2)
        lbl_val = ctk.CTkLabel(card, text=value, font=("Segoe UI", 28, "bold"), text_color=color)
        lbl_val.place(relx=0.1, rely=0.5)
        return lbl_val

    def log(self, text):
        self.log_box.insert("end", text + "\n")
        self.log_box.see("end")

    def update_dashboard(self):
        total = len(self.univer.persons)
        students = [p for p in self.univer.persons if isinstance(p, Student)]
        teachers = [p for p in self.univer.persons if isinstance(p, Lecturer)]
        avg_gpa = sum([s.GPA for s in students]) / len(students) if students else 0.0

        self.stat_total.configure(text=str(total))
        self.stat_students.configure(text=str(len(students)))
        self.stat_teachers.configure(text=str(len(teachers)))
        self.stat_gpa.configure(text=f"{avg_gpa:.2f}")

    def load_db(self):
        try:
            DataManager.load_data(self.univer)
            self.update_list()
            self.update_dashboard()
            self.clear_edit_panel()
            self.log("[DB] Данные загружены.")
        except Exception as e:
            messagebox.showerror("Ошибка", f"Файл не найден:\n{e}")

    def save_db(self):
        try:
            DataManager.save_data(self.univer)
            self.log("[DB] Сохранено в Excel.")
            messagebox.showinfo("Сохранено", "Изменения записаны!")
        except Exception as e:
            messagebox.showerror("Ошибка", f"Ошибка сохранения:\n{e}")

    def update_list(self):
        for widget in self.list_frame.winfo_children():
            widget.destroy()

        for person in self.univer.persons:
            is_student = isinstance(person, Student)
            icon = "👨‍🎓" if is_student else "👨‍🏫"
            role = "Студент" if is_student else "Преподаватель"
            color = ACCENT_STUDENT if is_student else ACCENT_TEACHER
            bg_hover = "#1E3A8A" if is_student else "#4C1D95"

            card = ctk.CTkButton(
                self.list_frame, text="", fg_color="#3F3F46", hover_color=bg_hover, corner_radius=10, height=60,
                command=lambda p=person: self.open_editor(p)
            )
            card.pack(fill="x", pady=5, padx=5)

            # Передача клика с текста на карточку
            def on_label_click(event, p=person): self.open_editor(p)

            lbl_icon = ctk.CTkLabel(card, text=icon, font=("Segoe UI", 24))
            lbl_icon.place(relx=0.03, rely=0.2)
            lbl_icon.bind("<Button-1>", on_label_click)

            lbl_name = ctk.CTkLabel(card, text=person.fullName, font=("Segoe UI", 15, "bold"), text_color=TEXT_MAIN)
            lbl_name.place(relx=0.15, rely=0.15)
            lbl_name.bind("<Button-1>", on_label_click)

            lbl_role = ctk.CTkLabel(card, text=f"{role} • {person.faculty}", font=("Segoe UI", 11), text_color=TEXT_MUTED)
            lbl_role.place(relx=0.15, rely=0.55)
            lbl_role.bind("<Button-1>", on_label_click)
            
            info_text = f"Курс {person.course}" if is_student else f"{person.experience} лет"
            lbl_info = ctk.CTkLabel(card, text=info_text, font=("Segoe UI", 12, "bold"), text_color=color)
            lbl_info.place(relx=0.95, rely=0.3, anchor="e")
            lbl_info.bind("<Button-1>", on_label_click)

    def clear_edit_panel(self):
        self.selected_person = None
        self.edit_form.place_forget()
        self.placeholder_lbl.place(relx=0.5, rely=0.5, anchor="center")

    def open_editor(self, person):
        self.selected_person = person
        self.edit_entries.clear()
        
        self.placeholder_lbl.place_forget()
        self.edit_form.place(relx=0, rely=0, relwidth=1, relheight=1)

        for widget in self.edit_form.winfo_children(): widget.destroy()

        is_student = isinstance(person, Student)
        icon = "👨‍🎓" if is_student else "👨‍🏫"
        color = ACCENT_STUDENT if is_student else ACCENT_TEACHER

        header_frame = ctk.CTkFrame(self.edit_form, fg_color="transparent")
        header_frame.pack(fill="x", pady=(30, 20), padx=30)
        
        ctk.CTkLabel(header_frame, text=icon, font=("Arial", 60)).pack(side="left")
        info_frame = ctk.CTkFrame(header_frame, fg_color="transparent")
        info_frame.pack(side="left", padx=15, fill="y")
        
        ctk.CTkLabel(info_frame, text="ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ", font=("Segoe UI", 10, "bold"), text_color=TEXT_MUTED, anchor="w").pack(fill="x")
        ctk.CTkLabel(info_frame, text=person.fullName, font=("Segoe UI", 20, "bold"), text_color=color, anchor="w").pack(fill="x")

        fields = {"fullName": "ФИО", "age": "Возраст", "faculty": "Факультет", "email": "Email"}
        if is_student:
            fields.update({"studentID": "ID Студента", "course": "Курс", "GPA": "Средний балл"})
        else:
            fields.update({"post": "Должность", "experience": "Стаж (лет)"})

        grid_frame = ctk.CTkFrame(self.edit_form, fg_color="transparent")
        grid_frame.pack(fill="both", expand=True, padx=30)
        grid_frame.grid_columnconfigure((0, 1), weight=1)

        row, col = 0, 0
        for attr, label_text in fields.items():
            cell = ctk.CTkFrame(grid_frame, fg_color="transparent")
            cell.grid(row=row, column=col, padx=10, pady=10, sticky="ew")
            
            ctk.CTkLabel(cell, text=label_text, font=("Segoe UI", 11, "bold"), text_color=TEXT_MUTED, anchor="w").pack(fill="x")
            entry = ctk.CTkEntry(cell, height=35, corner_radius=8, font=("Segoe UI", 13), border_width=1)
            entry.pack(fill="x", pady=(2, 0))
            entry.insert(0, str(getattr(person, attr)))
            self.edit_entries[attr] = entry
            
            col += 1
            if col > 1: col, row = 0, row + 1

        btn_save = ctk.CTkButton(self.edit_form, text="✓ Применить изменения", font=("Segoe UI", 14, "bold"), 
                                 height=45, corner_radius=8, fg_color=color, hover_color="#27272A",
                                 border_width=2, border_color=color, command=self.apply_edits)
        btn_save.pack(fill="x", padx=40, pady=30, side="bottom")

    def apply_edits(self):
        if not self.selected_person: return
        for attr, entry in self.edit_entries.items():
            val = entry.get()
            if attr in ["age", "course", "experience"]: val = int(val) if val.isdigit() else 0
            elif attr == "GPA": val = float(val) if val.replace('.','',1).isdigit() else 0.0
            setattr(self.selected_person, attr, val)

        self.update_list()
        self.update_dashboard()
        self.log(f"[EDIT] Изменен: {self.selected_person.fullName}")

    # === ПРЯМОЙ ЭКСПОРТ (БЕЗ ПАТТЕРНА) ===

    def direct_excel(self):
        if not self.univer.persons: return messagebox.showwarning("Пусто", "Нет данных.")
        path, count, logs = self.univer.exportAllToExcel()
        for l in logs: self.log(l)
        self.log(f"✅ Excel сгенерирован: {path}")
        messagebox.showinfo("Успех", "Excel сгенерирован прямым вызовом методов!")

    def direct_txt(self):
        if not self.univer.persons: return messagebox.showwarning("Пусто", "Нет данных.")
        path, count, logs = self.univer.exportAllToTxt()
        for l in logs: self.log(l)
        self.log(f"✅ TXT сгенерирован: {path}")
        messagebox.showinfo("Успех", "TXT сгенерирован прямым вызовом методов!")

if __name__ == "__main__":
    app = App()
    app.mainloop()