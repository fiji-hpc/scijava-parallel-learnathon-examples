package cz.it4i.parallel.examples;

import io.scif.services.DatasetIOService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.plugins.commands.imglib.RotateImageXY;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.TextWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.parallel.learnathon.Constants;
@Plugin(type = Command.class,
	menuPath = "Plugins > SciJava Parallel > Demo of SciJava Parallel - Learnathon 2019 - stacked")
public class BetterDemoSciJavaParallel implements Command {

	private final static Logger log = LoggerFactory.getLogger(BetterDemoSciJavaParallel.class);

	public static void main(String... args) {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		final Context context = ij.context();

		CommandService cs = context.getService(CommandService.class);
		cs.run(BetterDemoSciJavaParallel.class, true);

	}

	private static final String LINEAR = "Linear";
	private static final String NEAREST_NEIGHBOR = "Nearest Neighbor";

	@Parameter
	private ImageJ ij;

	@Parameter
	private Context context;

	@Parameter
	private ParallelService parallelService;

	@Parameter
	private DatasetIOService datasetIOService;

	@Parameter
	private UIService uiService;

	@Parameter
	private DatasetIOService datesetIOService;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "URL of image")
	private String imageUrl;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "Step of angle")
	private int step;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "Number of iteration")
	private int count;

	@Parameter(label = "Interpolation", choices = {LINEAR, NEAREST_NEIGHBOR}, persist = true)
	private String method = LINEAR;

	@Override
	public void run() {
		try {
			Dataset ds = datasetIOService.open(imageUrl);
			ArrayList<RandomAccessibleInterval<?>> imglist = new ArrayList<>();

			parallelService.selectProfile(Constants.LEARNATHON_DEMO_PROFILE_NAME);
			try (ParallelizationParadigm paradigm = parallelService.getParadigm()) {
				long start = System.currentTimeMillis();
				if (paradigm != null) {
					paradigm.init();
				} else {
					log.warn("Parallelization paradigm not created");
					return;
				}
				long init = System.currentTimeMillis();
				List<Map<String, Object>> results = paradigm.runAll(RotateImageXY.class,
					initParameters(ds, step, count, method));
				log.info("Pipeline runtime: " + (System.currentTimeMillis()-init) +  "ms");
				log.info("Total cluster runtime: " + (System.currentTimeMillis()-start) +  "ms");

				results.forEach(result -> imglist.add((RandomAccessibleInterval<?>) result.get("dataset")));

				log.info("Processing runtime: " + (System.currentTimeMillis()-start)+  "ms");

			}
			RandomAccessibleInterval<?> imgStack = Views.stack(imglist);
			uiService.show(imgStack);
		}
		catch (IOException exc) {
			log.error(exc.getMessage(), exc);
		}
	}

	private static List<Map<String, Object>> initParameters(
		Dataset dataset, int step, int count, String method)
	{
		List<Map<String, Object>> result = new LinkedList<>();
		int angle = step;
		for (int i = 0; i < count; i++) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("dataset", dataset);
			parameters.put("angle", angle);
			parameters.put("method", method);
			angle += step;
			result.add(parameters);
		}
		return result;

	}

}
