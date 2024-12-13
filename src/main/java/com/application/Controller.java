package com.application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Controller {
    private final FileChooser fileChooser = new FileChooser();
    private final String XLSX = TYPE.XLSX.getTitle();
    private final String XLS = TYPE.XLS.getTitle();
    public Button openFile;
    public Button openFile_1;
    public Button openFile_2;
    public Button compareFile;
    public Button saveFile;
    public Button countWire;
    public TextArea informationArea;
    public TextField fieldFilePath;
    public TextField fieldFilePath_1;
    public TextField fieldFilePath_2;
    public TextField fieldGroupAddresses;
    public TextFlow count;
    public Label cursorPosition;
    public Label numberOfPages;
    private String filePath = "";
    private String filePath1 = "";
    private String filePath2 = "";
    private int counter = 0;

    Map<Integer, Wire> wireTable1 = new HashMap<>();
    Map<Integer, Wire> wireTable2 = new HashMap<>();
    TreeMap<String, Wire> wireDifference = new TreeMap<>();
    TreeMap<String, Wire> wireNew = new TreeMap<>();
    File file = new File(filePath);
    File file1 = new File(filePath1);
    File file2 = new File(filePath2);

    public void initialize() {
        setupCursorTracking();
    }

    private void setupCursorTracking() {
        informationArea.setOnKeyReleased(event -> updateCursorPosition());
        informationArea.setOnMouseClicked(event -> updateCursorPosition());
        informationArea.setOnMouseDragged(event -> updateCursorPosition());
    }

    private void updateCursorPosition() {
        int caretPosition = informationArea.getCaretPosition();
        String text = informationArea.getText(0, caretPosition);
        String[] lines = text.split("\n");
        int lineNumber = 0;
        int columnNumber = caretPosition - text.lastIndexOf("\n") - 1;

        for (String line : lines) {
            if (!line.isEmpty()) {
                lineNumber++;
            }
        }

        if (columnNumber < 0) {
            columnNumber = caretPosition;
        }

        cursorPosition.setText("Строка: " + (lineNumber + 1) + ", Столбец: " + (columnNumber + 1));
    }

    private File getFile() {
        fileChooser.setTitle("Select file");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Select Excel file", "*.xlsx", "*.xls"));

        return fileChooser.showOpenDialog(new Stage());
    }

    public void clickButtonFileOpen() {
        FileInfo fileInfo = handleFileOpen(fieldFilePath);
        file = fileInfo.file;
        filePath = fileInfo.filePath;
    }

    public void clickButtonFileOpen_1() {
        FileInfo fileInfo = handleFileOpen(fieldFilePath_1);
        file1 = fileInfo.file;
        filePath1 = fileInfo.filePath;
    }

    public void clickButtonFileOpen_2() {
        FileInfo fileInfo = handleFileOpen(fieldFilePath_2);
        file2 = fileInfo.file;
        filePath2 = fileInfo.filePath;
    }

    private FileInfo handleFileOpen(TextField fieldFilePath) {
        File file = null;
        String filePath;

        try {
            file = getFile();
            filePath = file.toURI().getPath();
            fieldFilePath.setText(file.getName());
            fieldFilePath.setStyle("-fx-text-fill: GREEN");
        } catch (RuntimeException ignored) {
            fieldFilePath.setText("Файл не выбран.");
            fieldFilePath.setStyle("-fx-text-fill: RED");
            filePath = "";
        }

        return new FileInfo(file, filePath);
    }

    public void clickButtonCompareFile() throws IOException {
        if (filePath1.isEmpty() || filePath2.isEmpty()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ErrorWindow.fxml"));
            ButtonLoader(fxmlLoader, compareFile);
        } else {
            wireTable1 = readFileAndAnalysis(file1);
            wireTable2 = readFileAndAnalysis(file2);

            addTextToInformationArea();
        }
    }

    public void clickButtonSaveFile() throws IOException {
        if (informationArea.getText().isEmpty()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ErrorIsEmpty.fxml"));
            ButtonLoader(fxmlLoader, saveFile);
        } else {
            fileChooser.setTitle("Save file");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save Text file", "*.txt"));
            File saveFile = fileChooser.showSaveDialog(new Stage());

            if (saveFile != null) {
                saveSystem(saveFile, informationArea.getText());
            }
        }
    }

    public void clickButtonCountWire() throws IOException {
        if (filePath.isEmpty() || fieldGroupAddresses.getText().isEmpty()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ErrorWindow.fxml"));
            ButtonLoader(fxmlLoader, countWire);
        } else {
            List<String> groupNumber = List.of(fieldGroupAddresses.getText().trim().toUpperCase().split(" "));
            var groupLetter = groupNumber.get(0).charAt(0);

            counter = 0;

            var cellFrom = 1;
            var cellTo = 2;

            if (notHaveExcelFormat(file)) {
                throw new IllegalArgumentException("Unsupported file format");
            }

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                Workbook workbook = getWorkbook(fileInputStream, file.getName());
                Sheet sheet = workbook.getSheetAt(0);

                for (Row currentRow : sheet) {
                    if (!(currentRow.getRowNum() == 0 || currentRow.getRowNum() == 1)) {
                        var from = currentRow.getCell(cellFrom).getStringCellValue().trim().toUpperCase();
                        var to = currentRow.getCell(cellTo).getStringCellValue().trim().toUpperCase();

                        if (!(from.isEmpty() || to.isEmpty())) {
                            if (groupNumber.contains(from) && (!groupNumber.contains(to) && to.charAt(0) == groupLetter)) {
                                counter++;
                            }
                            if (groupNumber.contains(to) && (!groupNumber.contains(from) && from.charAt(0) == groupLetter)) {
                                counter++;
                            }
                            if ((!groupNumber.contains(from) && from.charAt(0) == groupLetter) && to.charAt(0) != groupLetter) {
                                counter++;
                            }
                            if (from.charAt(0) != groupLetter && (!groupNumber.contains(to) && to.charAt(0) == groupLetter)) {
                                counter++;
                            }
                        }
                    }
                }

                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException("Fail to parse Excel file: " + e.getMessage(), e);
            }

            Text textForTextFlow = new Text(String.format("%s шт.", counter));
            Font fontMonospaced = Font.font("Monospaced", FontWeight.BOLD, 32);

            textForTextFlow.setFont(fontMonospaced);

            if (!count.getChildren().isEmpty()) {
                count.getChildren().clear();
                count.getChildren().add(textForTextFlow);
            } else {
                count.getChildren().add(textForTextFlow);
            }
        }
    }

    private void addTextToInformationArea() {
        String fileName1 = file1.getName();
        String fileName2 = file2.getName();

        if (!informationArea.getText().isEmpty()) {
            informationArea.clear();
        }

        String fileNameFormatLeft = String.format("%s - %d шт.", fileName1.substring(0, fileName1.lastIndexOf('.')), wireTable1.size());
        String fileNameFormatRight = String.format("%s - %d шт.", fileName2.substring(0, fileName2.lastIndexOf('.')), wireTable2.size());
        String additionalInformation = String.format("Количество проводов проходящих через уплотнитель КР: %d шт.", counter);

        informationArea.appendText(getPaddedString(fileNameFormatLeft, ' '));
        informationArea.appendText(getPaddedString(fileNameFormatRight, ' '));
        informationArea.appendText(getPaddedString(additionalInformation, ' '));

        printToInformationArea(fileName1, fileName2);

        int numbersLine = informationArea.getParagraphs().size();
        int numbersOfPages = (int) Math.ceil((double) numbersLine / 60);

        numberOfPages.setText("Листов: " + numbersOfPages);
    }

    private void printToInformationArea(String fileName1, String fileName2) {
        compareWireTablesByNumber(fileName1, wireTable2, wireTable1);
        compareWireTablesByNumber(fileName2, wireTable1, wireTable2);
        compareWireTablesByParameters(fileName1, fileName2, wireTable1, wireTable2);

        String FILE_NAME_WIRE_NEW = " Провода которые есть в одном из файлов ";
        String WIRE_NEW_IS_EMPTY = " Нет уникальных проводов, которые имеются в одном из файлов ";
        String FILE_NAME_WIRE_DIFFERENCE = " Провода которые имеют отличие в параметрах ";
        String WIRE_DIFFERENCE_IS_EMPTY = " Нет отличий у параметров провода ";

        informationArea.appendText(getPaddedString(FILE_NAME_WIRE_NEW, '-'));
        printResultComparison(WIRE_NEW_IS_EMPTY, wireNew);

        informationArea.appendText(getPaddedString(FILE_NAME_WIRE_DIFFERENCE, '-'));
        printResultComparison(WIRE_DIFFERENCE_IS_EMPTY, wireDifference);

        informationArea.setStyle("-fx-text-fill: BLACK");

        wireTable1.clear();
        wireTable2.clear();
        wireNew.clear();
        wireDifference.clear();
    }

    private void printResultComparison(String informationNotification, TreeMap<String, Wire> wireTable) {
        if (!wireTable.isEmpty()) {
            for (Map.Entry<String, Wire> wire : wireTable.entrySet()) {
                String stringOutputFormat = String.format("№ %s %s", wire.getKey(), wire.getValue());
                informationArea.appendText(getPaddedString(stringOutputFormat, ' '));
            }
        } else {
            informationArea.appendText(getPaddedString(informationNotification, '+'));
        }
    }

    public static String getPaddedString(String str, char paddingChar) {
        if (str == null) {
            throw new NullPointerException("Can not add padding in null String!");
        }

        int maxPadding = 80;
        int length = str.length();
        int padding = (maxPadding - length) / 2;

        if (padding <= 0) {
            return str;
        }

/*
        ------------------Провода которые есть в одном из файлов ------------------
        ++++++++Нет уникальных проводов, которые имеются в одном из файлов++++++++
        ---------------- Провода которые имеют отличие в параметрах ----------------
        +++++++++++++++++++++Нет отличий у параметров провода +++++++++++++++++++++

       | Провода которые есть в одном из файлов | - 40 char
       | Провода которые имеют отличие в параметрах | - 44 char
       | Нет уникальных проводов, которые имеются в одном из файлов | - 60 char
       | Нет отличий у параметров провода |- 34 char
       |  №   6 - A31A12.3724 025-30 2023.10.25 Color=Ч    WireArea=2.5   Length=1140*  | - 80 char
*/

        String empty = "", hash = "#";
        String leftPadding = "%" + padding + "s";
        String rightPadding = "%" + padding + "s";
        String strFormat = leftPadding + "%s" + rightPadding + "\n";
        String formattedString = String.format(strFormat, empty, hash, empty);

        return formattedString.replace(' ', paddingChar).replace(hash, str);
    }

    //метод для сравнения проводов по номеру провода (проверка на уникальность)
    private void compareWireTablesByNumber(String fileName, Map<Integer, Wire> wireTable1, Map<Integer, Wire> wireTable2) {
        for (Map.Entry<Integer, Wire> ignored : wireTable1.entrySet()) {
            for (Map.Entry<Integer, Wire> wire2 : wireTable2.entrySet()) {
                if (!(wireTable1.containsKey(wire2.getKey()))) {

                    wireNew.put(String.format("%3s - %-19s", wire2.getKey(), fileName.substring(0, fileName.lastIndexOf('.'))), wire2.getValue());
                    wireTable2.remove(wire2.getKey());
                    break;
                }
            }
        }
    }

    //метод для сравнения проводов по параметрам (цвет, сечение, длина)
    private void compareWireTablesByParameters(String fileName1, String fileName2, Map<Integer, Wire> wireTable1, Map<Integer, Wire> wireTable2) {
        for (Map.Entry<Integer, Wire> wire1 : wireTable1.entrySet()) {
            for (Map.Entry<Integer, Wire> wire2 : wireTable2.entrySet()) {
                if (!(wire1.equals(wire2))) {

                    if (!wire1.getValue().getColor().equals(wire2.getValue().getColor())) {
                        wire1.getValue().setColor(wire1.getValue().getColor() + '*');
                    }

                    if (!wire1.getValue().getWireArea().equals(wire2.getValue().getWireArea())) {
                        wire1.getValue().setWireArea(wire1.getValue().getWireArea() + '*');
                    }

                    if (!wire1.getValue().getLength().equals(wire2.getValue().getLength())) {
                        wire1.getValue().setLength(String.format("%5s", wire1.getValue().getLength() + '*'));
                        wire2.getValue().setLength(String.format("%5s", wire2.getValue().getLength() + ' '));
                    }

                    wireDifference.put(String.format("%3s - %-19s", wire1.getKey(), fileName1.substring(0, fileName1.lastIndexOf('.'))), wire1.getValue());
                    wireDifference.put(String.format("%3s - %-19s", wire2.getKey(), fileName2.substring(0, fileName2.lastIndexOf('.'))), wire2.getValue());
                }

                wireTable2.remove(wire2.getKey());
                break;
            }
        }
    }

    //метод для проверки формата файла по расширению
    public boolean notHaveExcelFormat(File file) {
        String name = file.getName();
        return !name.endsWith(XLSX) && !name.endsWith(XLS);
    }

    private Workbook getWorkbook(FileInputStream inputStream, String fileName) throws IOException {
        if (fileName.endsWith(XLSX)) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.endsWith(XLS)) {
            return new HSSFWorkbook(inputStream);
        }
        throw new IllegalArgumentException("Unsupported file format");
    }

    // метод для вкладки Comparison of wire table
    private Map<Integer, Wire> readFileAndAnalysis(File file) {
        Map<Integer, Wire> returnWireMap = new HashMap<>();

        if (notHaveExcelFormat(file)) {
            throw new IllegalArgumentException("Unsupported file format");
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Workbook workbook = getWorkbook(fileInputStream, file.getName());
            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter dataFormatterCell = new DataFormatter(Locale.UK);

            for (Row row : sheet) {
                if (!(row.getRowNum() == 0 || row.getRowNum() == 1)) {
                    var wireNumber = (int) row.getCell(0).getNumericCellValue();
                    var valueColor = row.getCell(3).getStringCellValue();
                    var valueWireArea = dataFormatterCell.formatCellValue(row.getCell(4));
                    var valueLength = dataFormatterCell.formatCellValue(row.getCell(5));

                    Wire wire = new Wire(wireNumber, valueColor, valueWireArea, valueLength);

                    returnWireMap.put((int) row.getCell(0).getNumericCellValue(), wire);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Fail to parse Excel file: " + e.getMessage(), e);
        }
        return returnWireMap;
    }

    private void ButtonLoader(FXMLLoader fxmlLoader, Button button) throws IOException {
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("Errors");
        stage.getIcons().add(new Image("com/images/cableIcon.png"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(button.getScene().getWindow());
        stage.setResizable(false);
        stage.show();
        stage.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.ESCAPE)) {
                stage.close();
            }
        });
    }

    public void saveSystem(File file, String content) {
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.write(content);
            printWriter.close();

            informationArea.clear();
            informationArea.setStyle("-fx-text-fill: #b2b2b2");

            fieldFilePath.clear();
            fieldFilePath_1.clear();
            fieldFilePath_2.clear();
            count.getChildren().clear();
            fieldGroupAddresses.clear();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}