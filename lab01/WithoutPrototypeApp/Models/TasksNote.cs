using System;
using System.Collections.Generic;

namespace WithoutPrototypeApp.Models
{
    public class TasksNote
    {
        public string Title { get; set; } = string.Empty;
        public List<string> TaskNames { get; set; } = new List<string>();
        public List<bool> TaskDone { get; set; } = new List<bool>();
        public DateTime CreatedAt { get; private set; } = DateTime.Now;
        public DateTime UpdatedAt { get; set; } = DateTime.Now;

        public void NewTask(string name)
        {
            TaskNames.Add(name);
            TaskDone.Add(false);
            UpdatedAt = DateTime.Now;
        }

        public TasksNote Clone()
        {
            return new TasksNote
            {
                Title = this.Title,
                CreatedAt = this.CreatedAt,
                UpdatedAt = DateTime.Now,
                TaskNames = new List<string>(this.TaskNames),
                TaskDone = new List<bool>(this.TaskDone)
            };
        }

        // ✅ Свойство вместо метода
        public string PreviewText => $"{TaskNames.Count} задач";
    }
}