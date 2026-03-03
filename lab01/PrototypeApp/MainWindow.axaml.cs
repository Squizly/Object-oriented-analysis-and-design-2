using Avalonia.Controls;
using PrototypeApp.ViewModels;

namespace PrototypeApp;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
        DataContext = new MainWindowViewModel();
    }
}