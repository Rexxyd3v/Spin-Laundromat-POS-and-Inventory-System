package src.ui.panels.userPanel;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import src.model.User;

/**
 * Table model for user accounts
 */
public class UserTableModel extends AbstractTableModel {
    private final String[] columnNames = { "User", "Role", "Status", "Created", "Last Login", "Actions" };
    private List<User> users;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public UserTableModel(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return users.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return user;
            case 1:
                return user.getRole();
            case 2:
                return user.isActive() ? "Active" : "Inactive";
            case 3:
                return user.getCreatedDate().format(dateFormatter);
            case 4:
                return user.getLastLoginDate().format(dateFormatter);
            case 5:
                return "actions";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return User.class;
        if (columnIndex == 1)
            return User.UserRole.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5;
    }

    public void addUser(User user) {
        users.add(user);
        fireTableRowsInserted(users.size() - 1, users.size() - 1);
    }

    public void removeUser(int rowIndex) {
        users.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public User getUserAt(int rowIndex) {
        return users.get(rowIndex);
    }

    public int getAdminCount() {
        return (int) users.stream().filter(u -> u.getRole() == User.UserRole.ADMIN).count();
    }

    public int getStaffCount() {
        return (int) users.stream().filter(u -> u.getRole() == User.UserRole.STAFF).count();
    }

    public int getActiveCount() {
        return (int) users.stream().filter(User::isActive).count();
    }
}
