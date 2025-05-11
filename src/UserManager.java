import java.io.*;
import java.util.*;

public class UserManager {
    private static final String USER_FILE = "users.txt";
    private static UserManager instance;
    private List<User> users;

    private UserManager() {
        users = new ArrayList<>();
        loadUsers();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public boolean registerUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        // Check if username already exists
        if (users.stream().anyMatch(u -> u.getUsername().equals(username.trim()))) {
            return false;
        }

        User newUser = new User(username.trim(), password.trim());
        users.add(newUser);
        saveUsers();
        return true;
    }

    public boolean validateUser(String username, String password) {
        return users.stream()
                .anyMatch(u -> u.getUsername().equals(username.trim()) && 
                             u.getPassword().equals(password.trim()));
    }

    private void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }
        } catch (IOException ignored) {}
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : users) {
                writer.write(user.getUsername() + "|" + user.getPassword() + "\n");
            }
        } catch (IOException ignored) {}
    }
}