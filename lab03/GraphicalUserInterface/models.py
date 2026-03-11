import os
from abc import ABC, abstractmethod
from openpyxl import Workbook

class Persons(ABC):
    def __init__(self, fullName: str, age: int, faculty: str, email: str):
        self.fullName = fullName
        self.age = age
        self.faculty = faculty
        self.email = email

    @abstractmethod
    def exportToExcel(self, ws, logs: list): pass

    @abstractmethod
    def exportToTxt(self, lines: list, logs: list): pass

class Student(Persons):
    def __init__(self, fullName, age, faculty, email, studentID: str, course: int, gpa: float):
        super().__init__(fullName, age, faculty, email)
        self.studentID = studentID
        self.course = course
        self.GPA = gpa

    def exportToExcel(self, ws):
        row = ["Студент", self.fullName, self.age, self.faculty, self.email, self.studentID, self.course, self.GPA, "", ""]
        ws.append(row)

    def exportToTxt(self, lines: list):
        lines.append(f"[СТУДЕНТ] {self.fullName} | Возраст: {self.age} | Фак: {self.faculty} | ID: {self.studentID} | Курс: {self.course} | GPA: {self.GPA}")

class Lecturer(Persons):
    def __init__(self, fullName, age, faculty, email, post: str, experience: int):
        super().__init__(fullName, age, faculty, email)
        self.post = post
        self.experience = experience

    def exportToExcel(self, ws):
        row = ["Преподаватель", self.fullName, self.age, self.faculty, self.email, "", "", "", self.post, self.experience]
        ws.append(row)

    def exportToTxt(self, lines: list):
        lines.append(f"[ПРЕПОД] {self.fullName} | Возраст: {self.age} | Фак: {self.faculty} | Должность: {self.post} | Стаж: {self.experience} лет")

class University:
    def __init__(self):
        self.persons = []

    def addPers(self, p: Persons):
        self.persons.append(p)

    def clear(self):
        self.persons = []

    def exportAllToExcel(self):
        logs = []
        logs.append(f"[University] вызван метод exportAllToExcel()")
        wb = Workbook()
        ws = wb.active
        ws.title = "Direct Export"
        headers = ["Тип", "ФИО", "Возраст", "Факультет", "Email", "ID Студента", "Курс", "GPA", "Должность", "Стаж"]
        ws.append(headers)
        
        for p in self.persons:
            p.exportToExcel(ws)

        os.makedirs("exports", exist_ok=True)
        path = "exports/Direct_Export.xlsx"
        wb.save(path)
        return path, len(self.persons), logs

    def exportAllToTxt(self):
        lines = []
        logs = []
        logs.append(f"[University] вызван метод exportAllToTxt()")
        for p in self.persons:
            p.exportToTxt(lines)

        os.makedirs("exports", exist_ok=True)
        path = "exports/Direct_Export.txt"
        with open(path, "w", encoding="utf-8") as f:
            f.write("\n".join(lines))
        return path, len(self.persons), logs