package com.hubbox.seismicmonitor.view;


import static com.hubbox.seismicmonitor.constants.MonitorViewText.Button.CLEAR_CHARTS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Button.RESET_ZOOM;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Button.SETTINGS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Button.START_MONITORING;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Button.STOP_MONITORING;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.AMPLITUDE_AXIS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.SEISMIC_CHART_TITLE;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.SEISMIC_SERIES_NAME;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.STA_LTA_CHART_TITLE;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.STA_LTA_RATIO_AXIS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.STA_LTA_SERIES_NAME;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Chart.TIME_AXIS;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Error.CONFIG_ERROR_TITLE;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Status.CONNECTION_ACTIVE;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Status.CONNECTION_WAITING;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Status.MONITORING;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Status.READY;
import static com.hubbox.seismicmonitor.constants.MonitorViewText.Status.SEISMIC_EVENT_DETECTED;

import com.hubbox.seismicmonitor.config.AppConfig;
import com.hubbox.seismicmonitor.model.SeismicData;
import com.hubbox.seismicmonitor.viewmodel.MonitorViewModel;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorView extends BorderPane {
    private final MonitorViewModel viewModel;
    private LineChart<Number, Number> seismicChart;
    private LineChart<Number, Number> staLtaChart;
    private XYChart.Series<Number, Number> seismicSeries;
    private XYChart.Series<Number, Number> staLtaSeries;
    private Label statusLabel;
    private Label connectionStatusLabel;
    private ToggleButton monitoringButton;
    private Button configButton;
    private VBox chartsBox;
    private HBox controlPanel;

    public MonitorView(MonitorViewModel viewModel) {
        this.viewModel = viewModel;
        initializeView();
        setupBindings();
    }

    private void initializeView() {
        createCharts();
        createControlPanel();
        createStatusBar();
        layoutComponents();
        setupStyles();
    }

    private void createCharts() {
        NumberAxis xAxis1 = new NumberAxis(TIME_AXIS, 0, 1000, 100);
        NumberAxis yAxis1 = new NumberAxis(AMPLITUDE_AXIS, -1, 1, 0.2);
        seismicChart = new LineChart<>(xAxis1, yAxis1);
        seismicChart.setTitle(SEISMIC_CHART_TITLE);
        seismicChart.setAnimated(false);
        seismicChart.setCreateSymbols(false);

        setupZoomHandler(seismicChart, xAxis1, yAxis1);

        seismicSeries = new XYChart.Series<>();
        seismicSeries.setName(SEISMIC_SERIES_NAME);
        seismicChart.getData().add(seismicSeries);

        NumberAxis xAxis2 = new NumberAxis(TIME_AXIS, 0, 1000, 100);
        NumberAxis yAxis2 = new NumberAxis(STA_LTA_RATIO_AXIS, 0, 5, 0.5);
        staLtaChart = new LineChart<>(xAxis2, yAxis2);
        staLtaChart.setTitle(STA_LTA_CHART_TITLE);
        staLtaChart.setAnimated(false);
        staLtaChart.setCreateSymbols(false);

        setupZoomHandler(staLtaChart, xAxis2, yAxis2);

        staLtaSeries = new XYChart.Series<>();
        staLtaSeries.setName(STA_LTA_SERIES_NAME);
        staLtaChart.getData().add(staLtaSeries);

        Button resetZoomButton = new Button(RESET_ZOOM);
        resetZoomButton.setOnAction(e -> {
            resetZoom(xAxis1, yAxis1);
            resetZoom(xAxis2, yAxis2);
        });

        chartsBox = new VBox(10);
        chartsBox.getChildren().addAll(resetZoomButton, seismicChart, staLtaChart);
    }

    private void setupZoomHandler(LineChart<Number, Number> chart, NumberAxis xAxis, NumberAxis yAxis) {
        Rectangle selectRect = new Rectangle();
        selectRect.setFill(Color.TRANSPARENT);
        selectRect.setStroke(Color.BLUE);
        selectRect.setStrokeWidth(1);
        selectRect.getStrokeDashArray().addAll(5d, 5d);
        selectRect.setVisible(false);

        final ObjectProperty<Point2D> selectionStart = new SimpleObjectProperty<>();
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();

        final double[] xAxisInitialLowerBound = {xAxis.getLowerBound()};
        final double[] xAxisInitialUpperBound = {xAxis.getUpperBound()};
        final double[] yAxisInitialLowerBound = {yAxis.getLowerBound()};
        final double[] yAxisInitialUpperBound = {yAxis.getUpperBound()};

        chart.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.isShiftDown()) {
                selectionStart.set(new Point2D(event.getX(), event.getY()));
                selectRect.setX(event.getX());
                selectRect.setY(event.getY());
                selectRect.setWidth(0);
                selectRect.setHeight(0);
                selectRect.setVisible(true);
                event.consume();
            } else if (event.isMiddleButtonDown()) {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                event.consume();
            }
        });

        chart.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown() && event.isShiftDown() && selectionStart.get() != null) {
                double x = selectionStart.get().getX();
                double y = selectionStart.get().getY();
                double width = event.getX() - x;
                double height = event.getY() - y;

                selectRect.setX(width > 0 ? x : event.getX());
                selectRect.setY(height > 0 ? y : event.getY());
                selectRect.setWidth(Math.abs(width));
                selectRect.setHeight(Math.abs(height));
                event.consume();
            } else if (event.isMiddleButtonDown() && mouseAnchor.get() != null) {  // Orta tuş ile pan
                double deltaX = event.getX() - mouseAnchor.get().getX();
                double deltaY = event.getY() - mouseAnchor.get().getY();

                double xAxisScale = xAxis.getScale();
                double yAxisScale = yAxis.getScale();

                xAxis.setLowerBound(xAxis.getLowerBound() - deltaX / xAxisScale);
                xAxis.setUpperBound(xAxis.getUpperBound() - deltaX / xAxisScale);

                yAxis.setLowerBound(yAxis.getLowerBound() + deltaY / yAxisScale);
                yAxis.setUpperBound(yAxis.getUpperBound() + deltaY / yAxisScale);

                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                event.consume();
            }
        });

        chart.setOnMouseReleased(event -> {
            if (event.isShiftDown() && selectionStart.get() != null) {
                double x1 = selectRect.getX();
                double y1 = selectRect.getY();
                double x2 = x1 + selectRect.getWidth();
                double y2 = y1 + selectRect.getHeight();

                double xStart = xAxis.getValueForDisplay(x1).doubleValue();
                double xEnd = xAxis.getValueForDisplay(x2).doubleValue();
                double yStart = yAxis.getValueForDisplay(y2).doubleValue();
                double yEnd = yAxis.getValueForDisplay(y1).doubleValue();

                if (Math.abs(xEnd - xStart) > 1 && Math.abs(yEnd - yStart) > 0.1) {
                    Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                            new KeyValue(xAxis.lowerBoundProperty(), Math.min(xStart, xEnd)),
                            new KeyValue(xAxis.upperBoundProperty(), Math.max(xStart, xEnd)),
                            new KeyValue(yAxis.lowerBoundProperty(), Math.min(yStart, yEnd)),
                            new KeyValue(yAxis.upperBoundProperty(), Math.max(yStart, yEnd))
                        )
                    );
                    timeline.play();
                }

                selectRect.setVisible(false);
                selectionStart.set(null);
                event.consume();
            }
        });

        chart.setOnScroll(event -> {
            event.consume();
            if (event.isControlDown()) {  // CTRL tuşu ile scroll
                double zoomFactor = 1.1;
                double deltaY = event.getDeltaY();

                if (deltaY < 0) {
                    zoomFactor = 1 / zoomFactor;
                }

                double xAxisRange = xAxis.getUpperBound() - xAxis.getLowerBound();
                double yAxisRange = yAxis.getUpperBound() - yAxis.getLowerBound();
                double mouseXPosition = xAxis.getValueForDisplay(event.getX()).doubleValue();
                double mouseYPosition = yAxis.getValueForDisplay(event.getY()).doubleValue();

                double xNewRange = xAxisRange * zoomFactor;
                double yNewRange = yAxisRange * zoomFactor;

                xAxis.setLowerBound(mouseXPosition - (mouseXPosition - xAxis.getLowerBound()) * zoomFactor);
                xAxis.setUpperBound(xAxis.getLowerBound() + xNewRange);

                yAxis.setLowerBound(mouseYPosition - (mouseYPosition - yAxis.getLowerBound()) * zoomFactor);
                yAxis.setUpperBound(yAxis.getLowerBound() + yNewRange);
            }
        });

        chart.setOnMouseEntered(e -> {
            xAxisInitialLowerBound[0] = xAxis.getLowerBound();
            xAxisInitialUpperBound[0] = xAxis.getUpperBound();
            yAxisInitialLowerBound[0] = yAxis.getLowerBound();
            yAxisInitialUpperBound[0] = yAxis.getUpperBound();
        });

        Region chartPlotBackground = (Region) chart.lookup(".chart-plot-background");
        if (chartPlotBackground != null && chartPlotBackground.getParent() instanceof Pane chartPane) {
            chartPane.getChildren().add(selectRect);
        }
    }

    private void createControlPanel() {
        controlPanel = new HBox(10);
        controlPanel.setPadding(new Insets(10));

        monitoringButton = new ToggleButton(START_MONITORING);
        monitoringButton.setOnAction(e -> handleMonitoringToggle());

        configButton = new Button(SETTINGS);
        configButton.setOnAction(e -> handleConfigButton());

        Button clearButton = new Button(CLEAR_CHARTS);
        clearButton.setOnAction(e -> handleClearButton());

        controlPanel.getChildren().addAll(
            monitoringButton,
            configButton,
            clearButton
        );
    }

    private void resetZoom(NumberAxis xAxis, NumberAxis yAxis) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(xAxis.lowerBoundProperty(), 0),
                new KeyValue(xAxis.upperBoundProperty(), 1000),
                new KeyValue(yAxis.lowerBoundProperty(), yAxis.getTickUnit() * -5),
                new KeyValue(yAxis.upperBoundProperty(), yAxis.getTickUnit() * 5)
            )
        );
        timeline.play();
    }

    private void createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");

        statusLabel = new Label(READY);
        connectionStatusLabel = new Label(CONNECTION_WAITING);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBar.getChildren().addAll(
            statusLabel,
            spacer,
            connectionStatusLabel
        );

        setBottom(statusBar);
    }

    private void layoutComponents() {
        setCenter(chartsBox);
        setTop(controlPanel);
        setPadding(new Insets(10));

        seismicChart.setPrefHeight(300);
        staLtaChart.setPrefHeight(200);
    }

    private void setupStyles() {
        controlPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; " +
            "-fx-border-width: 0 0 1 0; -fx-padding: 10;");

        String buttonStyle = "-fx-min-width: 120px; -fx-min-height: 30px;";
        monitoringButton.setStyle(buttonStyle);
        configButton.setStyle(buttonStyle);

        seismicChart.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        staLtaChart.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
    }

    private void setupBindings() {
        viewModel.getSeismicDataList().addListener((ListChangeListener.Change<? extends SeismicData> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (SeismicData data : change.getAddedSubList()) {
                        updateChart(seismicSeries, data.getValue());
                        updateChart(staLtaSeries, data.getStaLtaRatio());
                        Platform.runLater(() -> updateStatus(data.isEventDetected()));
                    }
                }
            }
        });

        viewModel.getMonitoring().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> {
            monitoringButton.setSelected(newVal);
            monitoringButton.setText(Boolean.TRUE.equals(newVal) ? STOP_MONITORING : START_MONITORING);
            connectionStatusLabel.setText((Boolean.TRUE.equals(newVal) ? CONNECTION_ACTIVE : CONNECTION_WAITING));
            connectionStatusLabel.setTextFill(Boolean.TRUE.equals(newVal) ? Color.GREEN : Color.BLACK);
        }));

        viewModel.getStatusMessage().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> statusLabel.setText(newVal)));
    }

    private void handleMonitoringToggle() {
        if (monitoringButton.isSelected()) {
            viewModel.startMonitoring();
        } else {
            viewModel.stopMonitoring();
        }
    }

    private void handleConfigButton() {
        try {
            boolean wasMonitoring = viewModel.getMonitoring().get();
            if (wasMonitoring) {
                viewModel.stopMonitoring();
            }

            ConfigurationDialog dialog = new ConfigurationDialog(viewModel.getConfig());
            Optional<AppConfig> result = dialog.showAndWait();

            if (result.isPresent()) {
                viewModel.updateConfig(result.get());
                if (wasMonitoring) {
                    viewModel.startMonitoring();
                }
            } else if (wasMonitoring) {
                viewModel.startMonitoring();
            }
        } catch (Exception e) {
            log.error("Configuration dialog error", e);
            showError(CONFIG_ERROR_TITLE, e.getMessage());
        }
    }

    private void handleClearButton() {
        seismicSeries.getData().clear();
        staLtaSeries.getData().clear();
        viewModel.clearData();
    }

    private void updateChart(XYChart.Series<Number, Number> series, double value) {
        try {
            if (series.getData().size() > viewModel.getConfig().maxDataPoints()) {
                Platform.runLater(() -> {
                    if (!series.getData().isEmpty()) {
                        series.getData().remove(0);
                    }
                });
            }

            Platform.runLater(() -> series.getData().add(
                new XYChart.Data<>(series.getData().size(), value)
            ));
        } catch (Exception e) {
            log.error("Error updating seismic chart", e);
        }
    }

    private void updateStatus(boolean eventDetected) {
        if (eventDetected) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(SEISMIC_EVENT_DETECTED);
        } else {
            statusLabel.setTextFill(Color.BLACK);
            statusLabel.setText(MONITORING);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
