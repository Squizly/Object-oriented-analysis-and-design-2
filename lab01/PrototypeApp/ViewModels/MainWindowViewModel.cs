using System;
using System.Collections.Generic;
using System.Collections.ObjectModel; // ObservableCollection для автообновления UI
using System.ComponentModel; // INotifyPropertyChanged
using System.Runtime.CompilerServices; // CallerMemberName
using System.Windows.Input; // ICommand
using PrototypeApp.Models;

namespace PrototypeApp.ViewModels
{
    // Главная ViewModel окна (слой MVVM)
    public class MainWindowViewModel : INotifyPropertyChanged
    {
        // Событие для уведомления UI об изменении свойств
        public event PropertyChangedEventHandler? PropertyChanged;
        
        // Универсальный метод вызова уведомления об изменении свойства
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

        // Общий список всех заметок
        public ObservableCollection<NotePrototype> AllNotes { get; } = new();

        // Отдельная коллекция текстовых заметок
        public ObservableCollection<NotePrototype> TextNotes { get; } = new();

        // Отдельная коллекция задачных заметок
        public ObservableCollection<NotePrototype> TaskNotes { get; } = new();

        // Выбранная в UI заметка
        private NotePrototype? _selectedNote;
        public NotePrototype? SelectedNote
        {
            get => _selectedNote;
            set
            {
                if (_selectedNote != value)
                {
                    _selectedNote = value;

                    // Уведомляем UI о смене выбранной заметки
                    OnPropertyChanged();

                    // Обновляем вычисляемые свойства
                    OnPropertyChanged(nameof(IsTextNoteSelected));
                    OnPropertyChanged(nameof(IsTasksNoteSelected));
                    OnPropertyChanged(nameof(SelectedTextContent));
                    OnPropertyChanged(nameof(CurrentTaskItems));
                    OnPropertyChanged(nameof(DisplayPreview));

                    // Обновляем список задач для текущей заметки
                    UpdateCurrentTaskItems();
                }
            }
        }

        // Превью содержимого заметки (универсально для всех типов)
        public string DisplayPreview
        {
            get
            {
                // Если это текстовая заметка — показываем текст
                if (SelectedNote is TextNote tn)
                    return string.IsNullOrEmpty(tn.Content) ? "Нет содержимого" : tn.Content;

                // Если это список задач — показываем количество задач
                if (SelectedNote is TasksNote tasks)
                    return $"{tasks.TaskNames.Count} задач";

                return string.Empty;
            }
        }

        // Флаги для UI (видимость разных редакторов)
        public bool IsTextNoteSelected => SelectedNote is TextNote;
        public bool IsTasksNoteSelected => SelectedNote is TasksNote;

        // Текст текущей выбранной текстовой заметки
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

                    // Обновляем UI
                    OnPropertyChanged();
                    OnPropertyChanged(nameof(DisplayPreview));
                }
            }
        }

        // Коллекция задач для отображения в UI
        public ObservableCollection<TaskItem> CurrentTaskItems { get; } = new();

        // Команды UI
        public ICommand CreateTextNoteCommand { get; }
        public ICommand CreateTasksNoteCommand { get; }
        public ICommand CloneSelectedCommand { get; }
        public ICommand SaveEditCommand { get; }
        public ICommand AddTaskCommand { get; }
        public ICommand DeleteNoteCommand { get; }

        // Конструктор ViewModel
        public MainWindowViewModel()
        {
            // Инициализация команд
            CreateTextNoteCommand = new RelayCommand<string>(CreateTextNote);
            CreateTasksNoteCommand = new RelayCommand<string>(CreateTasksNote);
            CloneSelectedCommand = new RelayCommand(CloneSelected);
            SaveEditCommand = new RelayCommand(SaveEdit);
            AddTaskCommand = new RelayCommand<string>(AddTask);
            DeleteNoteCommand = new RelayCommand(DeleteNote);

            // Тестовые данные
            AddNote(new TextNote { Title = "Сильная заметка", Content = "средний контент" });
            
            var tasks = new TasksNote { Title = "Годовой план" };
            tasks.TaskNames.Add("НАВАЙБКОДИТЬ GUI");
            tasks.TaskNames.Add("ПРОТЕСТИРОВАТЬ GUI");
            tasks.TaskDone.Add(true);
            tasks.TaskDone.Add(false);
            AddNote(tasks);
        }

        // Добавление заметки в общий список
        private void AddNote(NotePrototype note)
        {
            AllNotes.Add(note);
            RefreshFolders(); // Перераспределяем по типам
        }

        // Обновление папок (разделение по типу)
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

        // Создание текстовой заметки
        private void CreateTextNote(string? title)
        {
            if (string.IsNullOrWhiteSpace(title)) return;
            AddNote(new TextNote { Title = title!, Content = "Новая заметка" });
        }

        // Создание заметки со списком задач
        private void CreateTasksNote(string? title)
        {
            if (string.IsNullOrWhiteSpace(title)) return;
            AddNote(new TasksNote { Title = title! });
        }

        // Клонирование выбранной заметки (Prototype-паттерн)
        private void CloneSelected()
        {
            if (SelectedNote == null) return;

            var clone = SelectedNote.Clone();
            clone.Title += " (копия)";
            AddNote(clone);
        }

        // Удаление выбранной заметки
        private void DeleteNote()
        {
            if (SelectedNote == null) return;

            AllNotes.Remove(SelectedNote);
            SelectedNote = null;
            RefreshFolders();
        }

        // Сохранение изменений текстовой заметки
        private void SaveEdit()
        {
            if (SelectedNote is TextNote tn)
            {
                tn.Edit(SelectedTextContent);
                OnPropertyChanged(nameof(SelectedNote));
                OnPropertyChanged(nameof(DisplayPreview));
            }
        }

        // Добавление новой задачи в задачную заметку
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

        // Переключение состояния задачи (выполнено/не выполнено)
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

        // Обновление отображаемого списка задач
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

    // Модель одной задачи для отображения в UI
    public class TaskItem : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler? PropertyChanged;

        // Метод уведомления UI
        private void OnPropertyChanged([CallerMemberName] string name = "") =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

        private string _name = string.Empty;

        // Название задачи
        public string Name
        {
            get => _name;
            set { _name = value; OnPropertyChanged(); }
        }

        private bool _done;

        // Статус выполнения задачи
        public bool Done
        {
            get => _done;
            set { _done = value; OnPropertyChanged(); }
        }
    }

    // Универсальная команда с параметром (Generic ICommand)
    public class RelayCommand<T> : ICommand
    {
        private readonly Action<T?> _execute;
        private readonly Func<T?, bool>? _canExecute;

        public RelayCommand(Action<T?> execute, Func<T?, bool>? canExecute = null)
        {
            _execute = execute ?? throw new ArgumentNullException(nameof(execute));
            _canExecute = canExecute;
        }

        // Проверка возможности выполнения
        public bool CanExecute(object? parameter) => 
            _canExecute == null || _canExecute((T?)parameter);

        // Выполнение команды
        public void Execute(object? parameter) => 
            _execute((T?)parameter);

        public event EventHandler? CanExecuteChanged;

        // Принудительное обновление состояния кнопки
        public void RaiseCanExecuteChanged() => 
            CanExecuteChanged?.Invoke(this, EventArgs.Empty);
    }

    // Команда без параметров
    public class RelayCommand : ICommand
    {
        private readonly Action _execute;
        private readonly Func<bool>? _canExecute;

        public RelayCommand(Action execute, Func<bool>? canExecute = null)
        {
            _execute = execute ?? throw new ArgumentNullException(nameof(execute));
            _canExecute = canExecute;
        }

        public bool CanExecute(object? parameter) => 
            _canExecute == null || _canExecute();

        public void Execute(object? parameter) => 
            _execute();

        public event EventHandler? CanExecuteChanged;

        public void RaiseCanExecuteChanged() => 
            CanExecuteChanged?.Invoke(this, EventArgs.Empty);
    }
}