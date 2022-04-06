package org.sugarcanemc.sugarcane.util.versioning;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BranchReader {
	@Nonnull
	public static final String getBranch() {
		@Nonnull String branch = "unknown";
		InputStream branchStream = BranchReader.class.getClassLoader().getResourceAsStream("branch-info.properties");
		Properties properties = new Properties();
		if (branchStream != null) {
			try {
				properties.load(branchStream);
				branch = properties.getProperty("branch");
			} catch (IOException ex) {
				Logger.getLogger(BranchReader.class.getName()).log(Level.WARNING, "Could not get branch info from branch-info.properties!", ex);
			}
		} else {
			Logger.getLogger(BranchReader.class.getName()).log(Level.WARNING, "Could not load branch-info.properties from the JAR!");
		}
		return branch;
	}
}