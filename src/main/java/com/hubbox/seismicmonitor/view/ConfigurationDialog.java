package com.hubbox.seismicmonitor.view;

import static com.hubbox.seismicmonitor.constants.MonitorViewText.Dialog.ANALYSIS_SETTINGS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Dialog.CONFIG_HEADER;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Dialog.CONFIG_TITLE;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Dialog.CONNECTION_SETTINGS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Dialog.SAVE;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.CHANNEL_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.CHANNEL_PROMPT;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.HOST_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.HOST_PROMPT;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.LTA_WINDOW_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.MAX_DATA_POINTS_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.NETWORK_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.NETWORK_PROMPT;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.NORMALIZATION_FACTOR_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.PORT_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.STATION_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.STATION_PROMPT;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.STA_WINDOW_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Form.THRESHOLD_LABEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.CHANNEL;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.HOST;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.LTA_WINDOW;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.MAX_DATA_POINTS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.NETWORK;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.NORMALIZATION_FACTOR;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.PORT;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.STATION;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.STA_WINDOW;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Tooltip.THRESHOLD;

import com.hubbox.seismicmonitor.config.AppConfig;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationDialog extends Dialog<AppConfig> {
    private static final String REGEX = "[A-Za-z0-9]*";
    private static final String BORDER_RED = "-fx-border-color: red";
    private final TextField hostField;
    private final Spinner<Integer> portSpinner;
    private final TextField networkField;
    private final TextField stationField;
    private final TextField channelField;
    private final Spinner<Integer> staWindowSpinner;
    private final Spinner<Integer> ltaWindowSpinner;
    private final Spinner<Double> thresholdSpinner;
    private final Spinner<Integer> maxDataPointsSpinner;
    private final Spinner<Double> normalizationFactorSpinner;
    private final AppConfig currentConfig;
    private final GridPane grid;

    public ConfigurationDialog(AppConfig config) {
        this.currentConfig = config;

        setTitle(CONFIG_TITLE);
        setHeaderText(CONFIG_HEADER);

        ButtonType saveButtonType = new ButtonType(SAVE, ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        hostField = createHostField();
        portSpinner = createPortSpinner();
        networkField = createTextFieldWithRegexValidation(NETWORK_PROMPT);
        stationField = createTextFieldWithRegexValidation(STATION_PROMPT);
        channelField = createTextFieldWithRegexValidation(CHANNEL_PROMPT);
        staWindowSpinner = createStaWindowSpinner();
        ltaWindowSpinner = createLtaWindowSpinner();
        thresholdSpinner = createThresholdSpinner();
        maxDataPointsSpinner = createMaxDataPointsSpinner();
        normalizationFactorSpinner = createNormalizationFactorSpinner();

        addFormElements();

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label(CONNECTION_SETTINGS),
            createSection(CONNECTION_SETTINGS),
            new Separator(),
            new Label(ANALYSIS_SETTINGS),
            createSection(ANALYSIS_SETTINGS)
        );

        getDialogPane().setContent(content);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return createConfigFromFields();
            }
            return null;
        });

        loadCurrentConfig();

        addListeners();

        validateForm();
    }

    private VBox createSection(String sectionType) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        if (sectionType.equals(CONNECTION_SETTINGS)) {
            GridPane connectionGrid = new GridPane();
            connectionGrid.setHgap(10);
            connectionGrid.setVgap(10);
            connectionGrid.setPadding(new Insets(10));

            connectionGrid.add(new Label(HOST_LABEL), 0, 0);
            connectionGrid.add(hostField, 1, 0);
            connectionGrid.add(new Label(PORT_LABEL), 0, 1);
            connectionGrid.add(portSpinner, 1, 1);
            connectionGrid.add(new Label(NETWORK_LABEL), 0, 2);
            connectionGrid.add(networkField, 1, 2);
            connectionGrid.add(new Label(STATION_LABEL), 0, 3);
            connectionGrid.add(stationField, 1, 3);
            connectionGrid.add(new Label(CHANNEL_LABEL), 0, 4);
            connectionGrid.add(channelField, 1, 4);

            section.getChildren().add(connectionGrid);
        } else {
            GridPane analysisGrid = new GridPane();
            analysisGrid.setVgap(10);
            analysisGrid.setHgap(10);
            analysisGrid.setPadding(new Insets(10));

            analysisGrid.add(new Label(STA_WINDOW_LABEL), 0, 0);
            analysisGrid.add(staWindowSpinner, 1, 0);
            analysisGrid.add(ltaWindowSpinner, 1, 1);
            analysisGrid.add(new Label(LTA_WINDOW_LABEL), 0, 1);
            analysisGrid.add(new Label(THRESHOLD_LABEL), 0, 2);
            analysisGrid.add(thresholdSpinner, 1, 2);
            analysisGrid.add(new Label(MAX_DATA_POINTS_LABEL), 0, 3);
            analysisGrid.add(maxDataPointsSpinner, 1, 3);
            analysisGrid.add(normalizationFactorSpinner, 1, 4);
            analysisGrid.add(new Label(NORMALIZATION_FACTOR_LABEL), 0, 4);

            section.getChildren().add(analysisGrid);
        }

        return section;
    }

    private TextField createHostField() {
        TextField field = new TextField();
        field.setPromptText(HOST_PROMPT);
        return field;
    }

    private Spinner<Integer> createPortSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 18000);
        Spinner<Integer> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
    }

    private TextField createTextFieldWithRegexValidation(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches(REGEX)) {
                field.setText(oldValue);
            }
        });
        return field;
    }

    private Spinner<Integer> createStaWindowSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 100);
        Spinner<Integer> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
    }

    private Spinner<Integer> createLtaWindowSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 10000, 5000);
        Spinner<Integer> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
    }

    private Spinner<Double> createThresholdSpinner() {
        SpinnerValueFactory<Double> valueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 10.0, 0.1, 0.01);
        Spinner<Double> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
    }

    private Spinner<Integer> createMaxDataPointsSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 10000, 1000);
        Spinner<Integer> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
    }

    private Spinner<Double> createNormalizationFactorSpinner() {
        SpinnerValueFactory<Double> valueFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000000.0, 100000.0, 1000.0);
        Spinner<Double> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
    }

    private void loadCurrentConfig() {
        hostField.setText(currentConfig.host());
        portSpinner.getValueFactory().setValue(currentConfig.port());
        networkField.setText(currentConfig.network());
        stationField.setText(currentConfig.station());
        channelField.setText(currentConfig.channel());
        staWindowSpinner.getValueFactory().setValue(currentConfig.staWindow());
        ltaWindowSpinner.getValueFactory().setValue(currentConfig.ltaWindow());
        thresholdSpinner.getValueFactory().setValue(currentConfig.threshold());
        maxDataPointsSpinner.getValueFactory().setValue(currentConfig.maxDataPoints());
        normalizationFactorSpinner.getValueFactory().setValue(currentConfig.normalizationFactor());
    }

    private AppConfig createConfigFromFields() {
        return new AppConfig(
            hostField.getText(),
            portSpinner.getValue(),
            networkField.getText(),
            stationField.getText(),
            channelField.getText(),
            staWindowSpinner.getValue(),
            ltaWindowSpinner.getValue(),
            thresholdSpinner.getValue(),
            maxDataPointsSpinner.getValue(),
            normalizationFactorSpinner.getValue()
        );
    }

    private void validateForm() {
        boolean isValid = true;

        try {
            String host = hostField.getText().trim();
            if (host.isEmpty()) {
                hostField.setStyle(BORDER_RED);
                isValid = false;
            } else {
                hostField.setStyle("");
            }

            int port = portSpinner.getValue();
            if (port <= 0 || port > 65535) {
                portSpinner.setStyle(BORDER_RED);
                isValid = false;
            } else {
                portSpinner.setStyle("");
            }

            String network = networkField.getText().trim();
            if (network.isEmpty()) {
                networkField.setStyle(BORDER_RED);
                isValid = false;
            } else {
                networkField.setStyle("");
            }

            String station = stationField.getText().trim();
            if (station.isEmpty()) {
                stationField.setStyle(BORDER_RED);
                isValid = false;
            } else {
                stationField.setStyle("");
            }

            String channel = channelField.getText().trim();
            if (channel.isEmpty()) {
                channelField.setStyle(BORDER_RED);
                isValid = false;
            } else {
                channelField.setStyle("");
            }

            int staWindow = staWindowSpinner.getValue();
            int ltaWindow = ltaWindowSpinner.getValue();
            if (ltaWindow <= staWindow) {
                staWindowSpinner.setStyle(BORDER_RED);
                ltaWindowSpinner.setStyle(BORDER_RED);
                isValid = false;
            } else {
                staWindowSpinner.setStyle("");
                ltaWindowSpinner.setStyle("");
            }

            Button okButton = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().stream()
                .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst().orElse(null));

            if (okButton != null) {
                okButton.setDisable(!isValid);
            }

        } catch (Exception e) {
            log.error("Form validation error", e);
        }

    }

    private void addFormElements() {
        int row = 0;

        Label connectionTitle = new Label(CONNECTION_SETTINGS);
        connectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(connectionTitle, 0, row++, 2, 1);

        grid.add(new Label(HOST_LABEL), 0, row);
        grid.add(hostField, 1, row++);

        grid.add(new Label(PORT_LABEL), 0, row);
        grid.add(portSpinner, 1, row++);

        grid.add(new Label(NETWORK_LABEL), 0, row);
        grid.add(networkField, 1, row++);

        grid.add(new Label(STATION_LABEL), 0, row);
        grid.add(stationField, 1, row++);

        grid.add(new Label(CHANNEL_LABEL), 0, row);
        grid.add(channelField, 1, row++);

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        grid.add(separator, 0, row++, 2, 1);

        Label analysisTitle = new Label(ANALYSIS_SETTINGS);
        analysisTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(analysisTitle, 0, row++, 2, 1);

        grid.add(new Label(STA_WINDOW_LABEL), 0, row);
        grid.add(staWindowSpinner, 1, row++);

        grid.add(new Label(LTA_WINDOW_LABEL), 0, row);
        grid.add(ltaWindowSpinner, 1, row++);

        grid.add(new Label(THRESHOLD_LABEL), 0, row);
        grid.add(thresholdSpinner, 1, row++);

        grid.add(new Label(MAX_DATA_POINTS_LABEL), 0, row);
        grid.add(maxDataPointsSpinner, 1, row++);

        grid.add(new Label(NORMALIZATION_FACTOR_LABEL), 0, row);
        grid.add(normalizationFactorSpinner, 1, row++);

        addTooltips();

        getDialogPane().setContent(grid);
    }

    private void addListeners() {
        hostField.textProperty().addListener((obs, old, newValue) -> validateForm());
        networkField.textProperty().addListener((obs, old, newValue) -> validateForm());
        stationField.textProperty().addListener((obs, old, newValue) -> validateForm());
        channelField.textProperty().addListener((obs, old, newValue) -> validateForm());

        portSpinner.valueProperty().addListener((obs, old, newValue) -> validateForm());
        staWindowSpinner.valueProperty().addListener((obs, old, newValue) -> validateForm());
        ltaWindowSpinner.valueProperty().addListener((obs, old, newValue) -> validateForm());
        thresholdSpinner.valueProperty().addListener((obs, old, newValue) -> validateForm());
        maxDataPointsSpinner.valueProperty().addListener((obs, old, newValue) -> validateForm());
        normalizationFactorSpinner.valueProperty().addListener((obs, old, newValue) -> validateForm());
    }

    private void addTooltips() {
        hostField.setTooltip(new Tooltip(HOST));
        portSpinner.setTooltip(new Tooltip(PORT));
        networkField.setTooltip(new Tooltip(NETWORK));
        stationField.setTooltip(new Tooltip(STATION));
        channelField.setTooltip(new Tooltip(CHANNEL));
        staWindowSpinner.setTooltip(new Tooltip(STA_WINDOW));
        ltaWindowSpinner.setTooltip(new Tooltip(LTA_WINDOW));
        thresholdSpinner.setTooltip(new Tooltip(THRESHOLD));
        maxDataPointsSpinner.setTooltip(new Tooltip(MAX_DATA_POINTS));
        normalizationFactorSpinner.setTooltip(new Tooltip(NORMALIZATION_FACTOR));
    }
}
