public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    SwingUtilities.invokeLater(() -> new TodoApp().setVisible(true));
                }
            }).setVisible(true);
        });
    }
}