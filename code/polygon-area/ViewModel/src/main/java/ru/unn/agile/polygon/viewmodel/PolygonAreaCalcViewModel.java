package ru.unn.agile.polygon.viewmodel;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.unn.agile.polygon.model.Polygon;
import ru.unn.agile.polygon.model.Point;

import java.util.List;
import java.util.regex.Pattern;

import static ru.unn.agile.polygon.viewmodel.LogMessages.CALCULATE_BUTTON_PRESSED;

public class PolygonAreaCalcViewModel {
    private Polygon polygon;

    private static final Pattern COORDINATE_INPUT_ALLOWED_SYMBOLS =
            Pattern.compile("^[-+]?[0-9]+\\.?[0-9]*$");
    private final SimpleBooleanProperty addingNewPointDisabled = new SimpleBooleanProperty();

    private final StringProperty logsArea = new SimpleStringProperty();
    private final StringProperty xCoordinate = new SimpleStringProperty();
    private final StringProperty yCoordinate = new SimpleStringProperty();
    private final ObservableList<Point> pointList = FXCollections.observableArrayList();

    private final StringProperty result = new SimpleStringProperty();
    private ILogger logger;

    public PolygonAreaCalcViewModel(final ILogger logger) {
        setLogger(logger);
        init();
    }

    private void init() {
        clearCoordinatesFormInput();

        BooleanBinding canCalculateBoolBinding = new BooleanBinding() {
            {
                super.bind(xCoordinate, yCoordinate);
            }
            @Override
            protected boolean computeValue() {
                return (isCoordinatesInputCorrect());
            }
        };
        addingNewPointDisabled.bind(canCalculateBoolBinding.not());
    }

    public boolean isCoordinatesInputCorrect() {
        String exprText1 = xCoordinate.get();
        String exprText2 = yCoordinate.get();
        return (COORDINATE_INPUT_ALLOWED_SYMBOLS.matcher(exprText1).matches()
                && COORDINATE_INPUT_ALLOWED_SYMBOLS.matcher(exprText2).matches());
    }

    public void addPoint() {
        double x = parseCoordinate(xCoordinate);
        double y = parseCoordinate(yCoordinate);
        Point newPoint = new Point(x, y);
        pointList.add(newPoint);

        clearCoordinatesFormInput();
    }

    public void calcArea() {
        if (pointList.isEmpty()) {
            return;
        }

        Point[] pointArray = pointList.toArray(Point[]::new);

        try {
            log(CALCULATE_BUTTON_PRESSED);
            polygon = new Polygon(pointArray);
            result.setValue(Double.toString(polygon.getArea()));
        } catch (IllegalArgumentException e) {
            result.setValue(e.getMessage());
        }
    }

    private void log(String message) {
        logger.log(message);
        updateUiLogs();
    }

    private void clearCoordinatesFormInput() {
        xCoordinate.set("");
        yCoordinate.set("");
    }

    private double parseCoordinate(final StringProperty coordinate) {
        if (!isCoordinatesInputCorrect()) {
            throw new IllegalArgumentException("Can't parse invalid input");
        }
        return Double.parseDouble(coordinate.get());
    }

    public StringProperty xProperty() {
        return xCoordinate;
    }
    public StringProperty yProperty() {
        return yCoordinate;
    }
    public ObservableList<Point> getPointList() {
        return pointList;
    }

    public StringProperty resultProperty() {
        return result;
    }
    public final String getResult() {
        return result.get();
    }

    public BooleanProperty addingNewPointDisabledProperty() {
        return addingNewPointDisabled;
    }
    public final boolean isAddingNewPointDisabled() {
        return addingNewPointDisabled.get();
    }

    public final void setLogger(final ILogger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger can not be null");
        }
        this.logger = logger;
    }

    private void updateUiLogs() {
        List<String> fullLog = logger.getLog();
        String uiLogRecord = String.join("\n", fullLog);
        logsArea.set(uiLogRecord);
    }

    public final List<String> getLog() {
        return logger.getLog();
    }

}
