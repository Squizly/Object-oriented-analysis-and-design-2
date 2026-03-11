from abc import ABC, abstractmethod

# --- ИЕРАРХИЯ ПОСЕТИТЕЛЕЙ (Visitor) ---
class Visitor(ABC):
    @abstractmethod
    def visitStudent(self, s): pass
    
    @abstractmethod
    def visitLecturer(self, l): pass

class ExcelExportVisitor(Visitor):
    def visitStudent(self, s):
        print(f"[Excel] Экспорт студента: {s.fullName} (ID: {s.studentID}, GPA: {s.GPA}) -> Students1.xlsx")

    def visitLecturer(self, l):
        print(f"[Excel] Экспорт преподавателя: {l.fullName} ({l.post}) -> Teachers1.xlsx")

class TxtExportVisitor(Visitor):
    def visitStudent(self, s):
        print(f"[TXT] Запись: {s.fullName}, Курс: {s.course} -> Students1.txt")

    def visitLecturer(self, l):
        print(f"[TXT] Запись: {l.fullName}, Стаж: {l.experience} лет -> Teachers1.txt")


# --- ИЕРАРХИЯ ЭЛЕМЕНТОВ (Persons) ---
class Persons(ABC):
    def __init__(self, fullName: str, age: int, faculty: str, email: str):
        self.fullName = fullName
        self.age = age
        self.faculty = faculty
        self.email = email

    @abstractmethod
    def accept(self, v: Visitor): pass

class Student(Persons):
    def __init__(self, fullName, age, faculty, email, studentID: str, course: int, gpa: float):
        super().__init__(fullName, age, faculty, email)
        self.studentID = studentID
        self.course = course
        self.GPA = gpa

    def accept(self, v: Visitor):
        v.visitStudent(self)

class Lecturer(Persons):
    def __init__(self, fullName, age, faculty, email, post: str, experience: int):
        super().__init__(fullName, age, faculty, email)
        self.post = post
        self.experience = experience

    def accept(self, v: Visitor):
        v.visitLecturer(self)


# --- СТРУКТУРА ДАННЫХ (University) ---
class University:
    def __init__(self):
        self.persons = []

    def addPers(self, p: Persons):
        self.persons.append(p)

    def accept(self, v: Visitor):
        # Университет просто "проводит" гостя по всем комнатам (объектам)
        for p in self.persons:
            p.accept(v)


# --- ТОЧКА ВХОДА (Эмуляция MainWindow) ---
if __name__ == "__main__":
    uni = University()
    
    # Представим, что мы загрузили 500 строк. Для примера создадим двоих:
    uni.addPers(Student("Максим Старпештес", 20, "ИПМКН", "max@mail.ru", "S092-22", 3, 4.7))
    uni.addPers(Lecturer("Иван Иванов", 45, "ИПМКН", "ivanov@univer.ru", "Доцент", 15))

    print("--- Работа с паттерном Visitor ---")
    uni.accept(ExcelExportVisitor())
    print("-" * 20)
    uni.accept(TxtExportVisitor())