using System;

namespace WithoutPrototypeApp.Models
{
    public class TextNote
    {
        public string Title { get; set; } = string.Empty;
        public string Content { get; set; } = string.Empty;
        public DateTime CreatedAt { get; private set; } = DateTime.Now;
        public DateTime UpdatedAt { get; set; } = DateTime.Now;

        public void Edit(string newContent)
        {
            Content = newContent;
            UpdatedAt = DateTime.Now;
        }

        public TextNote Clone()
        {
            return new TextNote
            {
                Title = this.Title,
                Content = this.Content,
                CreatedAt = this.CreatedAt,
                UpdatedAt = DateTime.Now
            };
        }

        public string PreviewText => 
            string.IsNullOrEmpty(Content) ? "Нет содержимого" : Content;
    }
}