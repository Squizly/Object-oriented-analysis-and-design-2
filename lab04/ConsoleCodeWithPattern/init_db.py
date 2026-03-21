import sqlite3
import random
from datetime import datetime, timedelta

def init_database():
    conn = sqlite3.connect('finance.db')
    cursor = conn.cursor()

    # 1. Таблица курсов
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS exchange_rates (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            from_cur TEXT NOT NULL,
            to_cur TEXT NOT NULL,
            rate REAL NOT NULL
        )
    ''')

    # 2. Таблица трат
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS expenses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            category TEXT NOT NULL,
            amount REAL NOT NULL,
            currency TEXT NOT NULL,
            date TEXT NOT NULL
        )
    ''')

    # --- ПОЛНЫЙ СПИСОК КУРСОВ ВАЛЮТ ---
    cursor.execute('DELETE FROM exchange_rates')
    
    rates_data = [
        # --- К РУБЛЮ (Прямые) ---
        ('USD', 'RUB', 92.45), ('EUR', 'RUB', 100.15), ('CNY', 'RUB', 12.75),
        ('AED', 'RUB', 25.17), ('KZT', 'RUB', 0.20), ('BYN', 'RUB', 28.35),
        ('TRY', 'RUB', 2.87), ('AMD', 'RUB', 0.23), ('GEL', 'RUB', 34.50),
        ('KGS', 'RUB', 1.03), ('UZS', 'RUB', 0.0074), ('AZN', 'RUB', 54.38),
        ('INR', 'RUB', 1.11), ('THB', 'RUB', 2.58), ('VND', 'RUB', 0.0037),
        ('GBP', 'RUB', 117.40), ('CHF', 'RUB', 104.80), ('JPY', 'RUB', 0.61),
        ('RSD', 'RUB', 0.85), ('EGP', 'RUB', 1.95),

        # --- ОБРАТНЫЕ КУРСЫ ---
        ('RUB', 'USD', 0.0108), ('RUB', 'EUR', 0.0099), ('RUB', 'CNY', 0.0784),
        ('RUB', 'AED', 0.0397), ('RUB', 'KZT', 4.88), ('RUB', 'BYN', 0.0352),
        ('RUB', 'TRY', 0.348), ('RUB', 'AMD', 4.31), ('RUB', 'GEL', 0.0289),
        ('RUB', 'GBP', 0.0085), ('RUB', 'CHF', 0.0095), ('RUB', 'JPY', 1.63),

        # --- КРОСС-КУРСЫ ---
        ('USD', 'EUR', 0.92), ('EUR', 'USD', 1.08),
        ('USD', 'CNY', 7.24), ('CNY', 'USD', 0.138),
        ('EUR', 'CNY', 7.85), ('CNY', 'EUR', 0.127),
        ('USD', 'AED', 3.67), ('AED', 'USD', 0.272),
        ('USD', 'TRY', 32.20), ('TRY', 'USD', 0.031),
        ('EUR', 'GBP', 0.85), ('GBP', 'EUR', 1.17),
        ('USD', 'JPY', 151.50), ('JPY', 'USD', 0.0066),
        ('KZT', 'USD', 0.0022), ('USD', 'KZT', 448.50),
        ('EUR', 'CHF', 0.96), ('CHF', 'EUR', 1.04)
    ]
    
    cursor.executemany('INSERT INTO exchange_rates (from_cur, to_cur, rate) VALUES (?, ?, ?)', rates_data)

    # --- ГЕНЕРАЦИЯ ТРАТ (70 строк) ---
    cursor.execute('DELETE FROM expenses')

    categories = ['Продукты', 'Транспорт', 'Кофе', 'Рестораны', 'Жилье', 'Развлечения', 'Здоровье', 'Подписки', 'Одежда', 'Подарки', 'Техника', 'Учеба']
    # Берем валюты, которые реально есть в нашем списке курсов
    available_currencies = [r[0] for r in rates_data if r[0] != 'RUB'] + ['RUB']
    
    expenses_to_insert = []
    end_date = datetime.now()
    start_date = end_date - timedelta(days=90)

    for _ in range(70):
        cat = random.choice(categories)
        curr = random.choice(available_currencies)
        
        # Сумма: для RUB побольше, для USD поменьше
        if curr == 'RUB':
            amt = round(random.uniform(100.0, 10000.0), 2)
        elif curr in ['VND', 'UZS', 'KZT']: # Валюты с мелким номиналом
            amt = round(random.uniform(1000.0, 50000.0), 2)
        else:
            amt = round(random.uniform(5.0, 200.0), 2)
            
        random_days = random.randint(0, 90)
        ex_date = (start_date + timedelta(days=random_days)).strftime('%Y-%m-%d')
        
        expenses_to_insert.append((cat, amt, curr, ex_date))

    cursor.executemany('''
        INSERT INTO expenses (category, amount, currency, date) 
        VALUES (?, ?, ?, ?)
    ''', expenses_to_insert)

    conn.commit()
    
    print("------------------------------------------")
    print(f"База 'finance.db' готова!")
    print(f"Загружено курсов: {len(rates_data)}")
    print(f"Сгенерировано трат: 70")
    print("------------------------------------------")
    conn.close()

if __name__ == "__main__":
    init_database()