package nobility.handler.modules.hits;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HitsExporter {

    private static final String lowQualityFileName = "lowquality.txt";
    private static final String highQualityFileName = "highquality.txt";
    private static final String hitSeperator = "------------------------------------------------------------------------";

    public static void export(List<Hit> hitList, File saveFolder) throws IOException {
        File lowQualityText = new File(saveFolder.getAbsolutePath() + File.separator
                + lowQualityFileName);
        File highQualityText = new File(saveFolder.getAbsolutePath() + File.separator
                + highQualityFileName);

        List<Hit> lqHits = hitList.stream().filter(hit -> !hit.isHighQuality())
                .collect(Collectors.toList());

        if (!lqHits.isEmpty()) {
            BufferedWriter lqWriter = new BufferedWriter(new FileWriter(lowQualityText));
            for (Hit hit : lqHits) {
                lqWriter.write(hitSeperator);
                lqWriter.newLine();
                lqWriter.newLine();
                lqWriter.write("Combo: " + hit.getCombo());
                lqWriter.newLine();
                lqWriter.write("Proxy Used: " + hit.getProxy());
                lqWriter.write(hit.getResults().toString());
                lqWriter.newLine();
                lqWriter.write(hitSeperator);
                lqWriter.newLine();
            }
            lqWriter.flush();
            lqWriter.close();
        }

        List<Hit> hqHits = hitList.stream().filter(Hit::isHighQuality)
                .collect(Collectors.toList());

        if (!hqHits.isEmpty()) {
            BufferedWriter hqWriter = new BufferedWriter(new FileWriter(highQualityText));
            for (Hit hit : hqHits) {
                hqWriter.newLine();
                hqWriter.write(hitSeperator);
                hqWriter.newLine();
                hqWriter.newLine();
                hqWriter.write("Combo: " + hit.getCombo());
                hqWriter.newLine();
                hqWriter.write("Proxy Used: " + hit.getProxy());
                hqWriter.write(hit.getResults().toString());
                hqWriter.newLine();
                hqWriter.write(hitSeperator);
                hqWriter.newLine();
            }
            hqWriter.flush();
            hqWriter.close();
        }
    }

}
