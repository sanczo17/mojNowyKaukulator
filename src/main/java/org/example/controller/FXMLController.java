package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.example.model.CryptoCalculator;
import org.example.service.CalculationService;
import org.example.util.LanguageManager;
import org.example.util.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Locale;
import java.io.File;

@Component
public class FXMLController {

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private LanguageManager languageManager;

    @Autowired
    private FileManager fileManager;

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem menuItemLoad, menuItemSave, menuItemSwitchLanguage;
    @FXML
    private TextField hashRateField, powerConsumptionField, initialCostField, electricityCostField, cryptoPriceField, dailyEarningField;
    @FXML
    private Label hashRateLabel, powerConsumptionLabel, initialCostLabel, electricityCostLabel, cryptoPriceLabel, dailyMiningLabel, dailyProfitLabel,  energyCostLabel, breakEvenDaysLabel;
    @FXML
    private Button calculateButton;

    @FXML
    public void initialize() {
        switchLanguage(Locale.getDefault());
        setupMenu();
        calculateButton.setOnAction(e -> calculateAction());
    }

    private void setupMenu() {
        menuBar.getMenus().clear();
        Menu menu = new Menu(languageManager.getString("menu"));

        MenuItem menuItemLoad = new MenuItem(languageManager.getString("load"));
        menuItemLoad.setOnAction(e -> loadAction());

        MenuItem menuItemSave = new MenuItem(languageManager.getString("save"));
        menuItemSave.setOnAction(e -> saveAction());

        MenuItem menuItemSwitchLanguage = new MenuItem(languageManager.getString("switch_language"));
        menuItemSwitchLanguage.setOnAction(e -> switchLanguageAction());

        menu.getItems().addAll(menuItemLoad, menuItemSave, menuItemSwitchLanguage);
        menuBar.getMenus().add(menu);
    }

    private void calculateAction() {
        try {
            double hashRate = Double.parseDouble(hashRateField.getText());
            double powerConsumption = Double.parseDouble(powerConsumptionField.getText());
            double electricityCost = Double.parseDouble(electricityCostField.getText());
            double cryptoPrice = Double.parseDouble(cryptoPriceField.getText());
            double initialCost = Double.parseDouble(initialCostField.getText());

            CryptoCalculator calculator = new CryptoCalculator(hashRate, powerConsumption, electricityCost, cryptoPrice, initialCost);
            double dailyMiningValue = calculationService.calculateDailyMiningValue(calculator);
            double dailyProfit = calculationService.calculateDailyProfit(calculator);;
            double dailyEnergyCost = calculationService.calculateDailyEnergyCost(calculator);
            double breakEvenDays = calculationService.calculateDaysToBreakEven(calculator);

            updateLabels(dailyMiningValue, dailyEnergyCost,dailyProfit, breakEvenDays);
        } catch (NumberFormatException e) {
            showAlert("Error", languageManager.getString("invalidInput"));
        }
    }


    private void switchLanguageAction() {
        Locale currentLocale = languageManager.getLocale();
        Locale newLocale = "pl".equals(currentLocale.getLanguage()) ? new Locale("en") : new Locale("pl");
        switchLanguage(newLocale);
        updateTexts();
    }
    public void switchLanguage(Locale locale) {
        languageManager.setLocale(locale);
        updateTexts();
    }


    private void updateTexts() {
        menuItemLoad.setText(languageManager.getString("load"));
        menuItemSave.setText(languageManager.getString("save"));
        menuItemSwitchLanguage.setText(languageManager.getString("switch_language"));

        calculateButton.setText(languageManager.getString("calculate"));
        hashRateLabel.setText(languageManager.getString("hashrate"));
        powerConsumptionLabel.setText(languageManager.getString("power_consumption"));
        initialCostLabel.setText(languageManager.getString("initial_cost"));
        electricityCostLabel.setText(languageManager.getString("energy_cost"));
        cryptoPriceLabel.setText(languageManager.getString("crypto_price"));
        energyCostLabel.setText(languageManager.getString("energy_cost"));
        breakEvenDaysLabel.setText(languageManager.getString("break_even_days"));
        menuBar.getMenus().clear();
        setupMenu();
    }
    private void updateLabels(double dailyMining, double dailyEnergyCost, double dailyProfit, double breakEvenDays) {
        energyCostLabel.setText(String.format(languageManager.getString("energy_cost") + ": %.2f PLN", dailyEnergyCost));
        dailyMiningLabel.setText(String.format(languageManager.getString("daily_mining") + ": %.2f PLN", dailyMining));
        dailyProfitLabel.setText(String.format(languageManager.getString("daily_profit") + ": %.2f PLN", dailyProfit));
        breakEvenDaysLabel.setText(String.format("%s: %.0f %s", languageManager.getString("break_even_days"), breakEvenDays, languageManager.getString("days")));

    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void loadAction() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            CryptoCalculator calculator = fileManager.loadData(file.getPath());
            if (calculator != null) {
                hashRateField.setText(String.valueOf(calculator.getHashRate()));
                powerConsumptionField.setText(String.valueOf(calculator.getPowerConsumption()));
                electricityCostField.setText(String.valueOf(calculator.getElectricityCost()));
                cryptoPriceField.setText(String.valueOf(calculator.getCryptoPrice()));
                initialCostField.setText(String.valueOf(calculator.getInitialCost()));
            } else {
                showAlert("Error", "Failed to load data.");
            }
        }
    }
    private void saveAction() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                CryptoCalculator calculator = new CryptoCalculator(
                        Double.parseDouble(hashRateField.getText()),
                        Double.parseDouble(powerConsumptionField.getText()),
                        Double.parseDouble(electricityCostField.getText()),
                        Double.parseDouble(cryptoPriceField.getText()),
                        Double.parseDouble(initialCostField.getText()));
                fileManager.saveData(calculator, file.getPath(), calculationService);
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid numbers.");
            }
        }
    }
}