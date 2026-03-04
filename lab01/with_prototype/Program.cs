using System;
using System.Collections.Generic;

namespace WithPrototype
{
    abstract class NotePrototype
    {
        public string Title { get; set; }
        public DateTime CreatedAt { get; private set; } = DateTime.Now;
        public DateTime UpdatedAt { get; set; } = DateTime.Now;

        public void Delete()
        {
            Title = null;
        }

        public abstract NotePrototype Clone();
    }

    class TextNote : NotePrototype
    {
        public string Content { get; set; }

        public void Edit(string newContent)
        {
            Content = newContent;
            UpdatedAt = DateTime.Now;
        }

        public override NotePrototype Clone()
        {
            return new TextNote
            {
                Title = this.Title,
                Content = this.Content,
                UpdatedAt = this.UpdatedAt
            };
        }
    }

    class TasksNote : NotePrototype
    {
        public List<string> TaskNames { get; set; } = new List<string>();
        public List<bool> TaskDone { get; set; } = new List<bool>();

        public void NewTask(string name)
        {
            TaskNames.Add(name);
            TaskDone.Add(false);
            UpdatedAt = DateTime.Now;
        }

        public override NotePrototype Clone()
        {
            var clone = new TasksNote
            {
                Title = this.Title,
                TaskNames = new List<string>(this.TaskNames),
                TaskDone = new List<bool>(this.TaskDone),
                UpdatedAt = this.UpdatedAt
            };
            return clone;
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("=== С паттерном Prototype ===");

            var textNote = new TextNote { Title = "Заметка 1", Content = "Контент заметки" };
            var textClone = (TextNote)textNote.Clone();
            textClone.Edit("Измененный контент");

            Console.WriteLine($"Оригинал: {textNote.Content}");
            Console.WriteLine($"Клон: {textClone.Content}");

            var taskNote = new TasksNote { Title = "Список задач" };
            taskNote.NewTask("Сделать лабораторку");

            var taskClone = (TasksNote)taskNote.Clone();
            taskClone.NewTask("Сходить в магазин");

            Console.WriteLine($"Оригинал задач: {string.Join(", ", taskNote.TaskNames)}");
            Console.WriteLine($"Клон задач: {string.Join(", ", taskClone.TaskNames)}");
        }
    }
}