import model.*;
import persistence.*;

public class FeatureRestartProbe {
    public static void main(String[] args) throws Exception {
        System.setProperty("laundry.dataDir", args[0]);
        if (Repositories.settings().get().receiptWidth() != 58 || Repositories.operations().branches().size() != 2
                || Repositories.operations().machines().size() != 2 || Repositories.operations().transfers().isEmpty()
                || Repositories.orders().read().sales().size() != 1)
            throw new AssertionError("Restored state did not survive a fresh process");
        System.out.println("RESTORED_READY");
    }
}
