using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;
using PrototypeApp.Models;

namespace PrototypeApp.ViewModels
{
    public class MainWindowViewModel : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler? PropertyChanged;
        
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

        public ObservableCollection<NotePrototype> AllNotes { get; } = new();
        public ObservableCollection<NotePrototype> TextNotes { get; } = new();
        public ObservableCollection<NotePrototype> TaskNotes { get; } = new();

        private NotePrototype? _selectedNote;
        public NotePrototype? SelectedNote
        {
            get => _selectedNote;
            set
            {
                if (_selectedNote != value)
                {
                    _selectedNote = value;
                    OnPropertyChanged();
                    OnPropertyChanged(nameof(IsTextNoteSelected));
                    OnPropertyChanged(nameof(IsTasksNoteSelected));
                    OnPropertyChanged(nameof(SelectedTextContent));
                    OnPropertyChanged(nameof(CurrentTaskItems));
                    OnPropertyChanged(nameof(DisplayPreview));
                    UpdateCurrentTaskItems();
                }
            }
        }

        // ✅ Превью для списка (работает для всех типов)
        public string DisplayPreview
        {
            get
            {
                if (SelectedNote is TextNote tn)
                    return string.IsNullOrEmpty(tn.Content) ? "Нет содержимого" : tn.Content;
                if (SelectedNote is TasksNote tasks)
                    return $"{tasks.TaskNames.Count} задач";
                return string.Empty;
            }
        }

        public bool IsTextNoteSelected => SelectedNote is TextNote;
        public bool IsTasksNoteSelected => SelectedNote is TasksNote;

        private string _selectedTextContent = string.Empty;
        public string SelectedTextContent
        {
            get => SelectedNote is TextNote tn ? tn.Content : string.Empty;
            set
            {
                if (SelectedNote is TextNote tn)
                {
                    _selectedTextContent = value;
                    tn.Content = value;
                    OnPropertyChanged();
                    OnPropertyChanged(nameof(DisplayPreview));
                }
            }
        }

        public ObservableCollection<TaskItem> CurrentTaskItems { get; } = new();

        public ICommand CreateTextNoteCommand { get; }
        public ICommand CreateTasksNoteCommand { get; }
        public ICommand CloneSelectedCommand { get; }
        public ICommand SaveEditCommand { get; }
        public ICommand AddTaskCommand { get; }
        public ICommand DeleteNoteCommand { get; }

        public MainWindowViewModel()
        {
            CreateTextNoteCommand = new RelayCommand<string>(CreateTextNote);
            CreateTasksNoteCommand = new RelayCommand<string>(CreateTasksNote);
            CloneSelectedCommand = new RelayCommand(CloneSelected);
            SaveEditCommand = new RelayCommand(SaveEdit);
            AddTaskCommand = new RelayCommand<string>(AddTask);
            DeleteNoteCommand = new RelayCommand(DeleteNote);

            AddNote(new TextNote { Title = "Идея проекта", Content = "Сделать приложение с Prototype Pattern" });
            
            var tasks = new TasksNote { Title = "План на неделю" };
            tasks.TaskNames.Add("Разработать GUI");
            tasks.TaskNames.Add("Протестировать клонирование");
            tasks.TaskDone.Add(true);
            tasks.TaskDone.Add(false);
            AddNote(tasks);
        }

        private void AddNote(NotePrototype note)
        {
            AllNotes.Add(note);
            RefreshFolders();
        }

        private void RefreshFolders()
        {
            TextNotes.Clear();
            TaskNotes.Clear();
            
            foreach (var note in AllNotes)
            {
                if (note is TextNote)
                    TextNotes.Add(note);
                else if (note is TasksNote)
                    TaskNotes.Add(note);
            }
        }

        private void CreateTextNote(string? title)
        {
            if (string.IsNullOrWhiteSpace(title)) return;
            AddNote(new TextNote { Title = title!, Content = "Новая заметка" });
        }

        private void CreateTasksNote(string? title)
        {
            if (string.IsNullOrWhiteSpace(title)) return;
            AddNote(new TasksNote { Title = title! });
        }

        private void CloneSelected()
        {
            if (SelectedNote == null) return;
            var clone = SelectedNote.Clone();
            clone.Title += " (копия)";
            AddNote(clone);
        }

        private void DeleteNote()
        {
            if (SelectedNote == null) return;
            AllNotes.Remove(SelectedNote);
            SelectedNote = null;
            RefreshFolders();
        }

        private void SaveEdit()
        {
            if (SelectedNote is TextNote tn)
            {
                tn.Edit(SelectedTextContent);
                OnPropertyChanged(nameof(SelectedNote));
                OnPropertyChanged(nameof(DisplayPreview));
            }
        }

        private void AddTask(string? taskName)
        {
            if (SelectedNote is TasksNote tasks && !string.IsNullOrWhiteSpace(taskName))
            {
                tasks.NewTask(taskName);
                UpdateCurrentTaskItems();
                OnPropertyChanged(nameof(CurrentTaskItems));
                OnPropertyChanged(nameof(SelectedNote));
                OnPropertyChanged(nameof(DisplayPreview));
            }
        }

        private void ToggleTask(TaskItem? task)
        {
            if (task != null && SelectedNote is TasksNote tasks)
            {
                var index = tasks.TaskNames.IndexOf(task.Name);
                if (index >= 0)
                {
                    tasks.TaskDone[index] = task.Done;
                    tasks.UpdatedAt = DateTime.Now;
                    OnPropertyChanged(nameof(SelectedNote));
                }
            }
        }

        private void UpdateCurrentTaskItems()
        {
            CurrentTaskItems.Clear();
            if (SelectedNote is TasksNote tasks)
            {
                for (int i = 0; i < tasks.TaskNames.Count; i++)
                {
                    CurrentTaskItems.Add(new TaskItem
                    {
                        Name = tasks.TaskNames[i],
                        Done = tasks.TaskDone[i]
                    });
                }
            }
        }
    }

    public class TaskItem : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler? PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

        private string _name = string.Empty;
        public string Name
        {
            get => _name;
            set { _name = value; OnPropertyChanged(); }
        }

        private bool _done;
        public bool Done
        {
            get => _done;
            set { _done = value; OnPropertyChanged(); }
        }
    }

    public class RelayCommand<T> : ICommand
    {
        private readonly Action<T?> _execute;
        private readonly Func<T?, bool>? _canExecute;

        public RelayCommand(Action<T?> execute, Func<T?, bool>? canExecute = null)
        {
            _execute = execute ?? throw new ArgumentNullException(nameof(execute));
            _canExecute = canExecute;
        }

        public bool CanExecute(object? parameter) => _canExecute == null || _canExecute((T?)parameter);
        public void Execute(object? parameter) => _execute((T?)parameter);
        public event EventHandler? CanExecuteChanged;
        public void RaiseCanExecuteChanged() => CanExecuteChanged?.Invoke(this, EventArgs.Empty);
    }

    public class RelayCommand : ICommand
    {
        private readonly Action _execute;
        private readonly Func<bool>? _canExecute;

        public RelayCommand(Action execute, Func<bool>? canExecute = null)
        {
            _execute = execute ?? throw new ArgumentNullException(nameof(execute));
            _canExecute = canExecute;
        }

        public bool CanExecute(object? parameter) => _canExecute == null || _canExecute();
        public void Execute(object? parameter) => _execute();
        public event EventHandler? CanExecuteChanged;
        public void RaiseCanExecuteChanged() => CanExecuteChanged?.Invoke(this, EventArgs.Empty);
    }
}