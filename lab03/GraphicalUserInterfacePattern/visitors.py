import os
from openpyxl import Workbook
from models import Visitor, Student, Lecturer

class ExcelExportVisitor(Visitor):
    def __init__(self):
        self.wb = Workbook()
        self.ws = self.wb.active
        self.ws.title = "Exported Visitors"
        
        headers = [
            "Тип", "ФИО", "Возраст", "Факультет", "Email", 
            "ID Студента", "Курс", "GPA", 
            "Должность", "Стаж"
        ]

        self.ws.append(headers)
        self.logs = []

    def visitStudent(self, s: Student):
        row = ["Студент", s.fullName, s.age, s.faculty, s.email, s.studentID, s.course, s.GPA, "", ""]
        self.ws.append(row)
        self.logs.append(f"[Visitor Excel] Экспорт студента: {s.fullName}")

    def visitLecturer(self, l: Lecturer):
        row = ["Преподаватель", l.fullName, l.age, l.faculty, l.email, "", "", "", l.post, l.experience]
        self.ws.append(row)
        self.logs.append(f"[Visitor Excel] Экспорт преподавателя: {l.fullName}")

    def save_file(self):
        os.makedirs("exports", exist_ok=True)
        path = "exports/Visitor_Export.xlsx"
        self.wb.save(path)
        return path, len(self.logs)

class TxtExportVisitor(Visitor):
    def __init__(self):
        self.lines = []
        self.logs = []

    def visitStudent(self, s: Student):
        self.lines.append(f"[СТУДЕНТ] {s.fullName} | Возраст: {s.age} | Фак: {s.faculty} | ID: {s.studentID} | Курс: {s.course} | GPA: {s.GPA}")
        self.logs.append(f"[Visitor TXT] Обработан студент: {s.fullName}")

    def visitLecturer(self, l: Lecturer):
        self.lines.append(f"[ПРЕПОД] {l.fullName} | Возраст: {l.age} | Фак: {l.faculty} | Должность: {l.post} | Стаж: {l.experience} лет")
        self.logs.append(f"[Visitor TXT] Обработан преподаватель: {l.fullName}")

    def save_file(self):
        os.makedirs("exports", exist_ok=True)
        path = "exports/Visitor_Export.txt"
        with open(path, "w", encoding="utf-8") as f:
            f.write("\n".join(self.lines))
        return path, len(self.logs)