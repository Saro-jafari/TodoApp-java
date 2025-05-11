import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    String currentUser = UserManager.getInstance().getCurrentUser();
                    SwingUtilities.invokeLater(() -> new TodoApp(currentUser).setVisible(true));
                }
            }).setVisible(true);
        });
    }
}
