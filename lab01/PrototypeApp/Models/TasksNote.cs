using System;
using System.Collections.Generic;

namespace PrototypeApp.Models
{
    public class TasksNote : NotePrototype
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
            return new TasksNote
            {
                Title = this.Title,
                CreatedAt = this.CreatedAt,
                UpdatedAt = DateTime.Now,
                TaskNames = new List<string>(this.TaskNames),
                TaskDone = new List<bool>(this.TaskDone)
            };
        }

        public override string PreviewText => $"{TaskNames.Count} задач";
    }
}