import openpyxl
import random

def create_initial_excel():
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.title = "University DB"

    headers = ["Type", "FullName", "Age", "Faculty", "Email", "StudentID", "Course", "GPA", "Post", "Experience"]
    ws.append(headers)

    # --- МУЖСКИЕ КОМПОНЕНТЫ (Смешные и странные) ---
    m_first = ["Дмитрий", "Максим", "Иван", "Сергей", "Андрей", "Роман", "Артем", "Никита", "Михаил", "Олег", 
               "Акакий", "Пантелеймон", "Евлампий", "Сидор", "Фрол", "Добрыня", "Бронислав", "Аполлон"]
    
    m_last = ["Мышкин", "Клавиатуров", "Процессоров", "Котлетов", "Перебейнос", "Нетудыхата", "Забейворота", 
              "Криворучко", "Чипсетов", "Мониторов", "Вайфаев", "Пирожков", "Самокатов", "Огурцов", 
              "Сковородкин", "Тапочкин", "Подлизевич", "Шнурков", "Джаваскриптов", "Питонко", "Пингвинов",
              "Кирпич", "Борщ", "Шпатель", "Барабан", "Кактус", "Сыр", "Чебурек", "Пельмень", "Виндовс"]
    
    m_mid = ["Иванович", "Сергеевич", "Андреевич", "Романович", "Павлович", "Николаевич", "Протоколович", 
             "Алгоритмович", "Байтович", "Серверович", "Степанович", "Петрович"]

    # --- ЖЕНСКИЕ КОМПОНЕНТЫ (Смешные и странные) ---
    w_first = ["Анна", "Мария", "Елена", "Ольга", "Наталья", "Екатерина", "Дарья", "Ирина", "Татьяна", "Светлана", 
               "Агриппина", "Пульхерия", "Олимпиада", "Глафира", "Фекла", "Василиса", "Матильда"]
    
    w_last = ["Клавиатура", "Мышкина", "Материнка", "Флешка", "Сметана", "Ватрушка", "Колбаса", "Гречка", 
              "Кофеварка", "Микроволновка", "Розетка", "Швабра", "Морковка", "Тапочка", "Кривошея", 
              "Бесподобная", "Веселая", "Грозная", "Крутая", "Симпатичная", "Чебуречная", "Пиксель",
              "Малинка", "Булочка", "Зубочистка", "Заплатка", "Кнопка", "Скрепочка", "Лямбда"]
    
    w_mid = ["Александровна", "Дмитриевна", "Максимовна", "Ивановна", "Сергеевна", "Андреевна", "Романовна", 
             "Матрицовна", "Флоппиевна", "Шифровна", "Степановна", "Петровна"]

    faculties = ["ИПМКН", "ФИТ", "ФФ", "РФФ", "ХФ", "БИ", "ИФ", "ФилФ", "ФЖ", "ФП", "ФФУ", "ИЭМ", "ФИЯ", "ЮИ", "ГГФ"]
    posts = ["Доцент", "Старший преподаватель", "Ассистент", "Профессор", "Главный по мемам", "Магистр черной магии"]
    domains = ["gmail.com", "yandex.ru", "mail.ru", "tsu.ru", "omsk.ru", "zoo.com"]

    def translit(text):
        d = {'а':'a','б':'b','в':'v','г':'g','д':'d','е':'e','ё':'yo','ж':'zh','з':'z','и':'i','й':'y','к':'k','л':'l','м':'m','н':'n','о':'o','п':'p','р':'r','с':'s','т':'t','у':'u','ф':'f','х':'kh','ц':'ts','ч':'ch','ш':'sh','щ':'shch','ы':'y','э':'e','ю':'yu','я':'ya'}
        res = "".join([d.get(c.lower(), c) for c in text if c.lower() in d or c.isalpha()])
        return res if res else "user"

    used_ids = set()

    for i in range(500):
        is_student = i < 420
        is_male = random.choice([True, False])
        
        if is_male:
            f, l, m = random.choice(m_first), random.choice(m_last), random.choice(m_mid)
        else:
            f, l, m = random.choice(w_first), random.choice(w_last), random.choice(w_mid)
        
        full_name = f"{l} {f} {m}"
        
        # Генерация Email (берём фамилию в транслите)
        email_login = translit(l)
        email = f"{email_login.lower()}_{random.randint(100, 999)}@{random.choice(domains)}"
        
        faculty = random.choice(faculties)
        
        if is_student:
            age = random.randint(17, 25)
            while True:
                sid = f"ID-{random.randint(10, 99)}-{random.randint(1000, 9999)}"
                if sid not in used_ids:
                    used_ids.add(sid)
                    break
            ws.append(["Student", full_name, age, faculty, email, sid, random.randint(1, 5), round(random.uniform(2.0, 5.0), 2), None, None])
        else:
            age = random.randint(28, 85)
            ws.append(["Lecturer", full_name, age, faculty, email, None, None, None, random.choice(posts), random.randint(0, 60)])

    wb.save("funny_university_500.xlsx")
    print("Файл на 500 строк со смешными именами готов! Проверяй файл funny_university_500.xlsx")

if __name__ == "__main__":
    create_initial_excel()