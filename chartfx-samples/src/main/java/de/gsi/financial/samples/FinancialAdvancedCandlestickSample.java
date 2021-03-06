package de.gsi.financial.samples;

import java.util.Calendar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.renderer.spi.financial.AbstractFinancialRenderer;
import de.gsi.chart.renderer.spi.financial.CandleStickRenderer;
import de.gsi.chart.renderer.spi.financial.css.FinancialColorSchemeConstants;
import de.gsi.dataset.spi.DefaultDataSet;
import de.gsi.dataset.spi.financial.OhlcvDataSet;
import de.gsi.dataset.spi.financial.api.attrs.AttributeKey;
import de.gsi.dataset.spi.financial.api.ohlcv.IOhlcvItem;

/**
 * Advanced configuration of Candlestick renderer. Support for PaintBars and extension points.
 *
 * @author afischer
 */
public class FinancialAdvancedCandlestickSample extends AbstractBasicFinancialApplication {
    public static final AttributeKey<Boolean> MARK_BAR = AttributeKey.create(Boolean.class, "MARK_BAR");

    /**
     * Prepare charts to the root.
     */
    protected Scene prepareScene() {
        timeRange = "2020/06/24 0:00-2020/11/12 0:00";

        final Chart chart = getDefaultFinancialTestChart(FinancialColorSchemeConstants.SAND);
        final AbstractFinancialRenderer<?> renderer = (AbstractFinancialRenderer<?>) chart.getRenderers().get(0);

        // prepare top financial toolbar
        ToolBar testVariableToolBar = getTestToolBar(chart, renderer, false);

        VBox root = new VBox();
        VBox.setVgrow(chart, Priority.SOMETIMES);
        root.getChildren().addAll(testVariableToolBar, chart);

        return new Scene(root, prefSceneWidth, prefSceneHeight);
    }

    protected void prepareRenderers(XYChart chart, OhlcvDataSet ohlcvDataSet, DefaultDataSet indiSet) {
        // create and apply renderers
        CandleStickRenderer candleStickRenderer = new CandleStickRenderer(true);
        candleStickRenderer.getDatasets().addAll(ohlcvDataSet);

        ErrorDataSetRenderer avgRenderer = new ErrorDataSetRenderer();
        avgRenderer.setDrawMarker(false);
        avgRenderer.setErrorType(ErrorStyle.NONE);
        avgRenderer.getDatasets().addAll(indiSet);

        chart.getRenderers().clear();
        chart.getRenderers().add(candleStickRenderer);
        chart.getRenderers().add(avgRenderer);

        //------------------------------------------
        // Example of extension possibilities

        // PaintBar Service Usage
        candleStickRenderer.setPaintBarMarker(d -> d.ohlcvItem.getOpen() - d.ohlcvItem.getClose() > 100.0 ? Color.MAGENTA : null);

        // PaintAfter Extension Point Usage
        // select every friday with yellow square point in the middle of candle
        Calendar cal = Calendar.getInstance(); // set this up however you need it.
        for (IOhlcvItem ohlcvItem : ohlcvDataSet) {
            cal.setTime(ohlcvItem.getTimeStamp());
            int day = cal.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.FRIDAY) {
                ohlcvItem.getAddonOrCreate().setAttribute(MARK_BAR, true);
            }
        }

        // example of extension point PaintAfter - Paint yellow square if the bar is selected by addon model attribute
        candleStickRenderer.addPaintAfterEp(d -> {
            // addon extension with MARK BAR settings
            if (d.ohlcvItem.getAddon() != null) {
                if (d.ohlcvItem.getAddon().getAttribute(MARK_BAR, false)) {
                    double yy;
                    if (d.ohlcvItem.getClose() > d.ohlcvItem.getOpen()) {
                        yy = d.yClose - (d.yClose - d.yOpen) / 2;
                        d.gc.setFill(Color.CRIMSON);
                    } else {
                        yy = d.yOpen - (d.yOpen - d.yClose) / 2;
                        d.gc.setFill(Color.YELLOW);
                    }
                    double rectCorr = d.barWidthHalf / 2.0;
                    d.gc.fillRect(d.xCenter - rectCorr, yy - rectCorr, rectCorr * 2.0, rectCorr * 2.0);
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}
