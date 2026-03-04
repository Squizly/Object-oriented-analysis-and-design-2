using System;
using System.Collections.Generic;

namespace WithoutPrototype
{
    class TextNote
    {
        public string Title { get; set; }
        public string Content { get; set; }

        public TextNote(string title, string content)
        {
            Title = title;
            Content = content;
        }

        public void Edit(string newContent)
        {
            Content = newContent;
        }

        public void Delete()
        {
            Title = null;
            Content = null;
        }

        public TextNote Clone()
        {
            return new TextNote(this.Title, this.Content);
        }
    }

    class TasksNote
    {
        public string Title { get; set; }
        public List<string> TaskNames { get; set; } = new List<string>();
        public List<bool> TaskDone { get; set; } = new List<bool>();

        public TasksNote(string title)
        {
            Title = title;
        }

        public void NewTask(string name)
        {
            TaskNames.Add(name);
            TaskDone.Add(false);
        }

        public void Delete()
        {
            Title = null;
            TaskNames.Clear();
            TaskDone.Clear();
        }

        public TasksNote Clone()
        {
            var clone = new TasksNote(this.Title);
            clone.TaskNames.AddRange(this.TaskNames);
            clone.TaskDone.AddRange(this.TaskDone);
            return clone;
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("=== Без паттерна Prototype ===");

            var textNote = new TextNote("Заметка 1", "Контент заметки");
            var textClone = textNote.Clone();
            textClone.Edit("Измененный контент");

            Console.WriteLine($"Оригинал: {textNote.Content}");
            Console.WriteLine($"Клон: {textClone.Content}");

            Console.WriteLine("=== === === === === === ===");

            var taskNote = new TasksNote("Список задач");
            taskNote.NewTask("Сделать лабораторку");

            var taskClone = taskNote.Clone();
            taskClone.NewTask("Сходить в магазин");

            Console.WriteLine($"Оригинал задач: {string.Join(", ", taskNote.TaskNames)}");
            Console.WriteLine($"Клон задач: {string.Join(", ", taskClone.TaskNames)}");
        }
    }
}