using Avalonia.Controls;
using WithoutPrototypeApp.ViewModels;

namespace WithoutPrototypeApp;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
        DataContext = new MainWindowViewModel();
    }
}