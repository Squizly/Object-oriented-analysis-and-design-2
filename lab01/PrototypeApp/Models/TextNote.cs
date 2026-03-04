using System;

namespace PrototypeApp.Models
{
    public class TextNote : NotePrototype
    {
        public string Content { get; set; } = string.Empty;

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
                CreatedAt = this.CreatedAt,
                UpdatedAt = DateTime.Now
            };
        }

        public override string PreviewText => 
            string.IsNullOrEmpty(Content) ? "Нет содержимого" : Content;
    }
}