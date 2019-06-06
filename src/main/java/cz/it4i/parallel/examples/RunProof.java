package cz.it4i.parallel.examples;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;

import cz.it4i.parallel.learnathon.Constants;
import cz.it4i.parallel.learnathon.SimpleParallelService;

public class RunProof {

	public static void main(String... args) throws InterruptedException {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		final Context context = ij.context();
		SimpleParallelService service = (SimpleParallelService) context.getService(
			ParallelService.class);
		service.deleteProfiles();
		service.selectProfile(Constants.LEARNATHON_DEMO_PROFILE_NAME);
		try (ParallelizationParadigm paradigm = service.getParadigm()) {
			paradigm.init();
			Thread.sleep(5000);
		}
	}

	
}
