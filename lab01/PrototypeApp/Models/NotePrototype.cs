using System;

namespace PrototypeApp.Models
{
    public abstract class NotePrototype
    {
        public string Title { get; set; } = string.Empty;
        public DateTime CreatedAt { get; protected set; } = DateTime.Now;
        public DateTime UpdatedAt { get; set; } = DateTime.Now;

        public abstract NotePrototype Clone();
        
        public abstract string PreviewText { get; }
    }
}