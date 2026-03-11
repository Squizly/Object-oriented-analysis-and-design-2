from abc import ABC, abstractmethod

class Visitor(ABC):
    @abstractmethod
    def visitStudent(self, s): 
        pass
    
    @abstractmethod
    def visitLecturer(self, l): 
        pass

class Persons(ABC):
    def __init__(self, fullName: str, age: int, faculty: str, email: str):
        self.fullName = fullName
        self.age = age
        self.faculty = faculty
        self.email = email

    @abstractmethod
    def accept(self, v: Visitor): 
        pass

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

class University:
    def __init__(self):
        self.persons = []

    def addPers(self, p: Persons):
        self.persons.append(p)

    def clear(self):
        self.persons = []

    def accept(self, v: Visitor):
        for p in self.persons:
            p.accept(v)