import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class StudentForm extends JFrame {
    private JTextField rollNoField, fullNameField, classField, birthDateField, addressField, enrollmentDateField;
    private JButton saveButton, updateButton, resetButton;

    public StudentForm() {
        setTitle("Student Enrollment Form");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Labels and TextFields for Student Info
        JLabel rollNoLabel = new JLabel("Roll No:");
        rollNoLabel.setBounds(10, 10, 120, 25);
        panel.add(rollNoLabel);

        rollNoField = new JTextField();
        rollNoField.setBounds(150, 10, 200, 25);
        panel.add(rollNoField);

        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setBounds(10, 40, 120, 25);
        panel.add(fullNameLabel);

        fullNameField = new JTextField();
        fullNameField.setBounds(150, 40, 200, 25);
        fullNameField.setEnabled(false);
        panel.add(fullNameField);

        JLabel classLabel = new JLabel("Class:");
        classLabel.setBounds(10, 70, 120, 25);
        panel.add(classLabel);

        classField = new JTextField();
        classField.setBounds(150, 70, 200, 25);
        classField.setEnabled(false);
        panel.add(classField);

        JLabel birthDateLabel = new JLabel("Birth Date:");
        birthDateLabel.setBounds(10, 100, 120, 25);
        panel.add(birthDateLabel);

        birthDateField = new JTextField();
        birthDateField.setBounds(150, 100, 200, 25);
        birthDateField.setEnabled(false);
        panel.add(birthDateField);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(10, 130, 120, 25);
        panel.add(addressLabel);

        addressField = new JTextField();
        addressField.setBounds(150, 130, 200, 25);
        addressField.setEnabled(false);
        panel.add(addressField);

        JLabel enrollmentDateLabel = new JLabel("Enrollment Date:");
        enrollmentDateLabel.setBounds(10, 160, 120, 25);
        panel.add(enrollmentDateLabel);

        enrollmentDateField = new JTextField();
        enrollmentDateField.setBounds(150, 160, 200, 25);
        enrollmentDateField.setEnabled(false);
        panel.add(enrollmentDateField);

        // Buttons
        saveButton = new JButton("Save");
        saveButton.setBounds(50, 200, 80, 25);
        saveButton.setEnabled(false);
        panel.add(saveButton);

        updateButton = new JButton("Update");
        updateButton.setBounds(150, 200, 80, 25);
        updateButton.setEnabled(false);
        panel.add(updateButton);

        resetButton = new JButton("Reset");
        resetButton.setBounds(250, 200, 80, 25);
        panel.add(resetButton);

        add(panel);

        // Add action listeners
        addListeners();
    }

    private void addListeners() {
        // Roll No field listener
        rollNoField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRollNoExists();
            }
        });

        // Save Button Action
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });

        // Update Button Action
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateData();
            }
        });

        // Reset Button Action
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });
    }

    // Reset form fields
    private void resetForm() {
        rollNoField.setText("");
        fullNameField.setText("");
        classField.setText("");
        birthDateField.setText("");
        addressField.setText("");
        enrollmentDateField.setText("");
        disableFields();
    }

    // Disable fields except Roll No
    private void disableFields() {
        fullNameField.setEnabled(false);
        classField.setEnabled(false);
        birthDateField.setEnabled(false);
        addressField.setEnabled(false);
        enrollmentDateField.setEnabled(false);
        saveButton.setEnabled(false);
        updateButton.setEnabled(false);
    }

    // Enable fields for new entry
    private void enableFieldsForNewEntry() {
        fullNameField.setEnabled(true);
        classField.setEnabled(true);
        birthDateField.setEnabled(true);
        addressField.setEnabled(true);
        enrollmentDateField.setEnabled(true);
        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
    }

    // Enable fields for updating an entry
    private void enableFieldsForUpdate() {
        fullNameField.setEnabled(true);
        classField.setEnabled(true);
        birthDateField.setEnabled(true);
        addressField.setEnabled(true);
        enrollmentDateField.setEnabled(true);
        saveButton.setEnabled(false);
        updateButton.setEnabled(true);
    }

    // Check if Roll-No exists in the database
    private void checkRollNoExists() {
        String rollNo = rollNoField.getText();
        String jsonStr = "{\"Roll-No\":\"" + rollNo + "\"}";

        try {
            URL url = new URL("http://api.login2explore.com:5577/api/irl");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            
            // Set the authorization header if required by the API
            conn.setRequestProperty("Authorization", "90932022|-31949219240279807|90962684"); // Replace with your actual API key
            
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonStr.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.out.println("Unauthorized: Invalid API Key or Token");
                return;
            }

            BufferedReader br;
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();

            if (response.toString().contains("data")) {
                populateForm(response.toString()); // Extract data and populate form
                enableFieldsForUpdate();
            } else {
                enableFieldsForNewEntry();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while making the HTTP request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Populate form if Roll-No exists
    private void populateForm(String jsonResponse) {
        fullNameField.setText(extractFromJSON(jsonResponse, "Full-Name"));
        classField.setText(extractFromJSON(jsonResponse, "Class"));
        birthDateField.setText(extractFromJSON(jsonResponse, "Birth-Date"));
        addressField.setText(extractFromJSON(jsonResponse, "Address"));
        enrollmentDateField.setText(extractFromJSON(jsonResponse, "Enrollment-Date"));
    }

    private String extractFromJSON(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    // Save data to database
    private void saveData() {
        String jsonString = "{\"Roll-No\":\"" + rollNoField.getText() + "\",\"Full-Name\":\"" + fullNameField.getText() + "\",\"Class\":\"" + classField.getText() + "\",\"Birth-Date\":\"" + birthDateField.getText() + "\",\"Address\":\"" + addressField.getText() + "\",\"Enrollment-Date\":\"" + enrollmentDateField.getText() + "\"}";

        try {
            URL url = new URL("http://api.login2explore.com:5577/api/iml");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // Set the authorization header if required by the API
            conn.setRequestProperty("Authorization", "90932022|-31949219240279807|90962684"); // Replace with your actual API key

            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();

            System.out.println("Save Response: " + response.toString());
        } catch (IOException e) {
            System.err.println("An error occurred while making the HTTP request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update data in the database
    private void updateData() {
        String jsonString = "{\"Roll-No\":\"" + rollNoField.getText() + "\",\"Full-Name\":\"" + fullNameField.getText() + "\",\"Class\":\"" + classField.getText() + "\",\"Birth-Date\":\"" + birthDateField.getText() + "\",\"Address\":\"" + addressField.getText() + "\",\"Enrollment-Date\":\"" + enrollmentDateField.getText() + "\"}";

        try {
            URL url = new URL("http://api.login2explore.com:5577/api/uml");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");

            // Set the authorization header if required by the API
            conn.setRequestProperty("Authorization", "90932022|-31949219240279807|90962684"); // Replace with your actual API key

            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();

            System.out.println("Update Response: " + response.toString());
        } catch (IOException e) {
            System.err.println("An error occurred while making the HTTP request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentForm form = new StudentForm();
            form.setVisible(true);
        });
    }
}
