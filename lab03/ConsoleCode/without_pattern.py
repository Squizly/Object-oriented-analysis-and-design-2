from abc import ABC, abstractmethod

class Persons(ABC):
    def __init__(self, fullName: str, age: int, faculty: str, email: str):
        self.fullName = fullName
        self.age = age
        self.faculty = faculty
        self.email = email

    @abstractmethod
    def exportToExcel(self): pass

    @abstractmethod
    def exportToTxt(self): pass

class Student(Persons):
    def __init__(self, fullName, age, faculty, email, studentID, course, gpa):
        super().__init__(fullName, age, faculty, email)
        self.studentID = studentID
        self.course = course
        self.GPA = gpa

    def exportToExcel(self):
        print(f"[Без паттерна] Excel: Студент {self.fullName} (GPA: {self.GPA})")

    def exportToTxt(self):
        print(f"[Без паттерна] TXT: Студент {self.fullName} (ID: {self.studentID})")

class Lecturer(Persons):
    def __init__(self, fullName, age, faculty, email, post, experience):
        super().__init__(fullName, age, faculty, email)
        self.post = post
        self.experience = experience

    def exportToExcel(self):
        print(f"[Без паттерна] Excel: Преподаватель {self.fullName} ({self.post})")

    def exportToTxt(self):
        print(f"[Без паттерна] TXT: Преподаватель {self.fullName} (Стаж: {self.experience})")

class University:
    def __init__(self):
        self.persons = []

    def addPers(self, p):
        self.persons.append(p)

    # Приходится плодить методы на каждый формат
    def exportAllToExcel(self):
        for p in self.persons:
            p.exportToExcel()

    def exportAllToTxt(self):
        for p in self.persons:
            p.exportToTxt()

if __name__ == "__main__":
    uni = University()
    uni.addPers(Student("Роман Кузнецов", 20, "ИПМКН", "roma@mail.ru", "S092-24", 3, 4.6))
    uni.addPers(Lecturer("Анна Смирнова", 38, "ИПМКН", "anna@univer.ru", "Профессор", 10))

    print("--- Работа БЕЗ паттерна ---")
    uni.exportAllToExcel()
    uni.exportAllToTxt()