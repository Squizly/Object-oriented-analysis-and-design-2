using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;
using WithoutPrototypeApp.Models;

namespace WithoutPrototypeApp.ViewModels
{
    public class MainWindowViewModel : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler? PropertyChanged;
        
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

        public ObservableCollection<TextNote> TextNotes { get; } = new();
        public ObservableCollection<TasksNote> TaskNotes { get; } = new();

        // ✅ Отдельные выбранные для каждой коллекции
        private TextNote? _selectedTextNote;
        public TextNote? SelectedTextNote
        {
            get => _selectedTextNote;
            set
            {
                if (_selectedTextNote != value)
                {
                    _selectedTextNote = value;
                    if (value != null)
                    {
                        SelectedNote = value;
                        SelectedNoteType = "text";
                    }
                    OnPropertyChanged();
                }
            }
        }

        private TasksNote? _selectedTaskNote;
        public TasksNote? SelectedTaskNote
        {
            get => _selectedTaskNote;
            set
            {
                if (_selectedTaskNote != value)
                {
                    _selectedTaskNote = value;
                    if (value != null)
                    {
                        SelectedNote = value;
                        SelectedNoteType = "tasks";
                    }
                    OnPropertyChanged();
                }
            }
        }

        // ✅ Общее выбранное
        private object? _selectedNote;
        public object? SelectedNote
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
                    OnPropertyChanged(nameof(SelectedNoteTitle));
                    OnPropertyChanged(nameof(CreatedAt));
                    OnPropertyChanged(nameof(UpdatedAt));
                    UpdateCurrentTaskItems();
                }
            }
        }

        // ✅ Тип выбранной заметки
        private string _selectedNoteType = string.Empty;
        public string SelectedNoteType
        {
            get => _selectedNoteType;
            set
            {
                _selectedNoteType = value;
                OnPropertyChanged();
                OnPropertyChanged(nameof(IsTextNoteSelected));
                OnPropertyChanged(nameof(IsTasksNoteSelected));
            }
        }

        public bool IsTextNoteSelected => SelectedNoteType == "text";
        public bool IsTasksNoteSelected => SelectedNoteType == "tasks";

        public string SelectedNoteTitle => SelectedNote switch
        {
            TextNote tn => tn.Title,
            TasksNote tasks => tasks.Title,
            _ => string.Empty
        };

        public DateTime CreatedAt => SelectedNote switch
        {
            TextNote tn => tn.CreatedAt,
            TasksNote tasks => tasks.CreatedAt,
            _ => DateTime.Now
        };

        public DateTime UpdatedAt => SelectedNote switch
        {
            TextNote tn => tn.UpdatedAt,
            TasksNote tasks => tasks.UpdatedAt,
            _ => DateTime.Now
        };

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

            TextNotes.Add(new TextNote { Title = "Идея проекта", Content = "Сделать приложение БЕЗ Prototype Pattern" });
            
            var tasks = new TasksNote { Title = "План на неделю" };
            tasks.TaskNames.Add("Разработать GUI");
            tasks.TaskNames.Add("Протестировать клонирование");
            tasks.TaskDone.Add(true);
            tasks.TaskDone.Add(false);
            TaskNotes.Add(tasks);
        }

        private void CreateTextNote(string? title)
        {
            if (string.IsNullOrWhiteSpace(title)) return;
            TextNotes.Add(new TextNote { Title = title!, Content = "Новая заметка" });
        }

        private void CreateTasksNote(string? title)
        {
            if (string.IsNullOrWhiteSpace(title)) return;
            TaskNotes.Add(new TasksNote { Title = title! });
        }

        private void CloneSelected()
        {
            if (SelectedNote is TextNote tn)
            {
                var clone = tn.Clone();
                clone.Title += " (копия)";
                TextNotes.Add(clone);
            }
            else if (SelectedNote is TasksNote tasks)
            {
                var clone = tasks.Clone();
                clone.Title += " (копия)";
                TaskNotes.Add(clone);
            }
        }

        private void DeleteNote()
        {
            if (SelectedNote is TextNote tn)
            {
                TextNotes.Remove(tn);
                SelectedTextNote = null;
            }
            else if (SelectedNote is TasksNote tasks)
            {
                TaskNotes.Remove(tasks);
                SelectedTaskNote = null;
            }
            SelectedNote = null;
            SelectedNoteType = string.Empty;
        }

        private void SaveEdit()
        {
            if (SelectedNote is TextNote tn)
            {
                tn.Edit(SelectedTextContent);
                OnPropertyChanged(nameof(SelectedNote));
                OnPropertyChanged(nameof(UpdatedAt));
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
                OnPropertyChanged(nameof(UpdatedAt));
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
                    OnPropertyChanged(nameof(UpdatedAt));
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