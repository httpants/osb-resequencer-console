package au.com.cyberavenue.osb.resequencer.web;

import static java.util.stream.Collectors.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import au.com.cyberavenue.osb.resequencer.entity.soainfra.ComponentGroupStatusProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupReport;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupReportProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupStatus;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageStats;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbGroupStatusRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbResequencerMessageRepository;

@Controller
public class DashboardController {

    @Autowired
    private OsbGroupStatusRepository messageGroupRepository;

    @Autowired
    private OsbResequencerMessageRepository messageRepository;

    @Autowired
    private OsbGroupStatusRepository osbGroupStatusRepository;

    @Autowired
    private PagedResourcesAssembler<MessageGroupReportProjection> assembler;

    @GetMapping("")
    public String getHomePage() {
        return "redirect:/groups";
    }

    @GetMapping("/dashboard")
    public String getDashboard() {
        return "dashboard";
    }

    @GetMapping("/dashboard/message-groups-pie-chart.png")
    public void getDashboardMessageGroupsPieChart(HttpServletResponse response) throws IOException {

        List<MessageGroupReport> messageGroupReports = messageGroupRepository
                .getMessageGroupReport();

        Map<MessageGroupStatus, Long> messageGroupsPerGroupStatus = messageGroupReports.stream()
                .collect(groupingBy(mgr -> MessageGroupStatus.of(mgr.getGroupStatus()), counting()));

        DefaultPieDataset<MessageGroupStatus> dataset = new DefaultPieDataset<>();
        Stream.of(MessageGroupStatus.values()).forEach(
                mgs -> dataset.setValue(mgs, messageGroupsPerGroupStatus.get(mgs)));

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        PiePlot<MessageGroupStatus> plot = (PiePlot) chart.getPlot();
//        plot.setBackgroundPaint(new Color(255, 255, 255, 0));
        plot.setBackgroundImageAlpha(0f);
        plot.setSectionPaint(MessageGroupStatus.SUSPENDED, Color.RED);
        plot.setSectionPaint(MessageGroupStatus.READY, Color.GREEN);
        plot.setSectionPaint(MessageGroupStatus.LOCKED, Color.YELLOW);
        plot.setSectionPaint(MessageGroupStatus.ERROR, Color.BLACK);
        plot.setSectionPaint(MessageGroupStatus.TIMEOUT, Color.MAGENTA);
        ChartUtils.writeChartAsPNG(response.getOutputStream(), chart, 640, 480, true, 0);
    }

    @GetMapping("/dashboard/message-groups-bar-chart.png")
    public void getDashboardMessageGroupsBarChart(HttpServletResponse response) throws IOException {

        List<MessageGroupReport> messageGroupReports = messageGroupRepository
                .getMessageGroupReport();

        Map<MessageGroupStatus, Long> messageGroupsPerGroupStatus = messageGroupReports.stream()
                .collect(groupingBy(mgr -> MessageGroupStatus.of(mgr.getGroupStatus()), counting()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Stream.of(MessageGroupStatus.values()).forEach(
                mgs -> dataset.setValue(messageGroupsPerGroupStatus.get(mgs), mgs, "Status"));

        JFreeChart chart = ChartFactory.createBarChart(null, "Group Status", "Groups", dataset);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundImageAlpha(0f);
        ChartUtils.writeChartAsPNG(response.getOutputStream(), chart, 640, 480, true, 0);
    }

    private String getCategoryLabel(String componentName) {
        Pattern p = Pattern.compile("(\\w{3}\\.\\w{3}\\.\\w+\\.)(.*)");
        Matcher m = p.matcher(componentName);
        if (m.find()) {
//            return m.group(1) + "\n" + m.group(2);
            return m.group(2);
        }
        return componentName;
    }

    @GetMapping("/dashboard/component-messages-bar-chart.png")
    public void getMessagesBarChart(HttpServletResponse response) throws IOException {

        List<MessageStats> messageStats = messageRepository.getMessageReport();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(null, "", "Messages", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        chart.setBorderPaint(new Color(0x77, 0x77, 0x77));
        chart.setBorderStroke(new BasicStroke(0.2f));
        chart.setPadding(new RectangleInsets(0, 100, 0, 100));

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.TOP);

        CategoryPlot cp = (CategoryPlot) chart.getPlot();
        messageStats
                .stream()
                .forEach(ms -> {
                    String category = getCategoryLabel(ms.getComponent());
                    dataset.setValue(ms.getMessageAbortedCount(), "Aborted", category);
                    dataset.setValue(ms.getMessageFaultedCount(), "Faulted", category);
//                    dataset.setValue(ms.getMessageTimeoutCount(), "Timeout", category);
//                    dataset.setValue(ms.getMessageCompleteCount(), "Complete", category);
                    dataset.setValue(ms.getMessageReadyCount(), "Ready", category);
                    cp.addDomainMarker(getCategoryMarker(category), Layer.BACKGROUND);
                });

        int upperLimit = messageStats.stream()
                .map(MessageStats::getMessageTotalCount)
                .max(Integer::compare)
                .map(ul -> ul * 2)
                .orElse(100);

        cp.setBackgroundImageAlpha(0f);
        cp.setAxisOffset(RectangleInsets.ZERO_INSETS);
        cp.setRangeGridlinesVisible(true);
        cp.setBackgroundPaint(new Color(0xdadde0));
        cp.setOutlineVisible(true);

        Map<String, Paint> columnColors = new HashMap<>();
        columnColors.put("Faulted", Color.RED);
        columnColors.put("Timeout", Color.ORANGE);
        columnColors.put("Ready", Color.GREEN);
        columnColors.put("Aborted", Color.GRAY);
        columnColors.put("Complete", Color.BLUE);
        BarRenderer renderer = new CustomRenderer(dataset, columnColors);
        cp.setRenderer(renderer);
        renderer.setDefaultLegendShape(new Ellipse2D.Double(0, 0, 10, 10));
        renderer.setDefaultOutlineStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setDrawBarOutline(true);
        renderer.setDefaultOutlinePaint(Color.BLACK);
        renderer.setShadowVisible(true);
        renderer.setItemMargin(0.01);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new CategoryItemLabelGenerator() {

            @Override
            public String generateRowLabel(CategoryDataset dataset, int row) {
                return null;
            }

            @Override
            public String generateColumnLabel(CategoryDataset dataset, int column) {
                return null;
            }

            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {
                return String.valueOf(dataset.getValue(row, column).intValue());
            }

        });

        CategoryAxis domainAxis = cp.getDomainAxis();
        domainAxis.setCategoryMargin(0.2);
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.10);
        domainAxis.setMaximumCategoryLabelLines(50);
        domainAxis.setTickLabelFont(new Font("Helvetica", Font.BOLD, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        domainAxis.setMaximumCategoryLabelWidthRatio(1);

        LogAxis logAxis = new MyLogAxis("Messages");
        logAxis.setBase(2);
        logAxis.setRange(new Range(0.5, upperLimit));
        logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        logAxis.setNumberFormatOverride(new DecimalFormat());
        logAxis.setMinorTickMarksVisible(true);
        cp.setRangeAxis(logAxis);
        cp.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        ChartUtils.writeChartAsPNG(
                response.getOutputStream(),
                chart,
                350 + dataset.getColumnCount() * 60,
                650,
                true,
                0);
    }

    @GetMapping("/dashboard/component-groups-bar-chart.png")
    public void getComponentGroupsBarChart(HttpServletResponse response) throws IOException {

        List<ComponentGroupStatusProjection> componentGroups = osbGroupStatusRepository.getComponentGroupStatusReport();
        int maxRange = componentGroups.stream()
                .map(c -> Arrays.stream(new Integer[] {
                        c.getReady(),
                        c.getLocked(),
                        c.getProcessing(),
                        c.getError(),
                        c.getSuspended(),
                        c.getTimeout() })
                        .max(Integer::compare)
                        .orElse(0))
                .max(Integer::compare)
                .map(ul -> ul * 2)
                .orElse(100);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(null, "", "Groups", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        chart.setBorderPaint(new Color(0x77, 0x77, 0x77));
        chart.setBorderStroke(new BasicStroke(0.2f));
        chart.setPadding(new RectangleInsets(0, 100, 0, 100));
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.TOP);
        CategoryPlot cp = (CategoryPlot) chart.getPlot();

        componentGroups.forEach(c -> {
            String category = getCategoryLabel(c.getComponentDn());
            dataset.setValue(c.getError() > 0 ? c.getError() : null, "Error", category);
            dataset.setValue(c.getSuspended() > 0 ? c.getSuspended() : null, "Suspended", category);
            dataset.setValue(c.getReady() > 0 ? c.getReady() : null, "Ready", category);
//            dataset.setValue(c.getLocked() > 0 ? c.getLocked() : null, "Locked", category);
//            dataset.setValue(c.getTimeout() > 0 ? c.getTimeout() : null, "Timeout", category);
//            dataset.setValue(c.getProcessing() > 0 ? c.getProcessing() : null, "Processing", category);
            cp.addDomainMarker(getCategoryMarker(category), Layer.BACKGROUND);
        });

        cp.setBackgroundImageAlpha(0f);
        cp.setAxisOffset(RectangleInsets.ZERO_INSETS);
        cp.setRangeGridlinesVisible(true);
        cp.setBackgroundPaint(new Color(0xdadde0));
        cp.setOutlineVisible(true);
        cp.setDomainGridlinesVisible(true);
        cp.setRangeGridlinesVisible(true);

        Map<String, Paint> columnColors = new HashMap<>();
        columnColors.put("Suspended", Color.RED);
        columnColors.put("Error", Color.YELLOW);
        columnColors.put("Timeout", Color.ORANGE);
        columnColors.put("Ready", Color.GREEN);
        columnColors.put("Locked", Color.BLUE);
        columnColors.put("Processing", Color.CYAN);
        BarRenderer renderer = new CustomRenderer(dataset, columnColors);
        cp.setRenderer(renderer);
        renderer.setDefaultLegendShape(new Ellipse2D.Double(0, 0, 10, 10));
        renderer.setDefaultOutlineStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);
        renderer.setItemMargin(0.0001);
        renderer.setDataBoundsIncludesVisibleSeriesOnly(true);
        renderer.setDefaultOutlinePaint(Color.BLACK);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new CategoryItemLabelGenerator() {
            @Override
            public String generateRowLabel(CategoryDataset dataset, int row) {
                return null;
            }

            @Override
            public String generateColumnLabel(CategoryDataset dataset, int column) {
                return null;
            }

            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {
                return String.valueOf(dataset.getValue(row, column).intValue());
            }
        });

        CategoryAxis domainAxis = cp.getDomainAxis();
        domainAxis.setCategoryMargin(0.2);
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setMaximumCategoryLabelLines(50);
        domainAxis.setTickLabelFont(new Font("Helvetica", Font.BOLD, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        domainAxis.setMaximumCategoryLabelWidthRatio(1);

        LogAxis logAxis = new MyLogAxis("Groups");
        logAxis.setBase(2);
        logAxis.setRange(new Range(0.5, maxRange));
        logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        logAxis.setNumberFormatOverride(new DecimalFormat());
        logAxis.setMinorTickMarksVisible(true);
        cp.setRangeAxis(logAxis);
        cp.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        ChartUtils.writeChartAsPNG(
                response.getOutputStream(),
                chart,
                350 + dataset.getColumnCount() * 60,
                650,
                true,
                0);
    }

    private CategoryMarker getCategoryMarker(String category) {
        CategoryMarker cm = new CategoryMarker(category);
        cm.setPaint(new Color(0xf0f0f0));
        cm.setDrawAsLine(false);
        cm.setAlpha(0.4F);

//        cm.setLabel(category);
        return cm;
    }

    public static class MyLogAxis extends LogAxis {

        public MyLogAxis() {}

        public MyLogAxis(String label) {
            super(label);
        }

        @Override
        public double calculateLog(double value) {
            return value > 0 ? super.calculateLog(value) : 0;
        }

        @Override
        public double calculateValue(double log) {
            return log > 0.0 ? super.calculateValue(log) : 0;
        }

    }

    class CustomRenderer extends BarRenderer {

        Map<String, Paint> columnPaint;
        DefaultCategoryDataset dataset;

        public CustomRenderer(DefaultCategoryDataset dataset, Map<String, Paint> rowPaint) {
            this.columnPaint = rowPaint;
            this.dataset = dataset;
        }

        @Override
        public Paint getSeriesPaint(int series) {
            String rowKey = (String) dataset.getRowKey(series);
            Paint paint = columnPaint.get(rowKey);
            return paint != null ? paint : super.getSeriesPaint(series);
        }
    }

}
