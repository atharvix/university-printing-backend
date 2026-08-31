package com.universityprinting.printing_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrintingBackendApplication {

	public static void main(String[] args) {
		printMongoDiagnostic();
		SpringApplication.run(PrintingBackendApplication.class, args);
	}

	private static void printMongoDiagnostic() {
		String mongoUri = System.getenv("MONGODB_URI");
		boolean isPresent = mongoUri != null && !mongoUri.trim().isEmpty();

		System.out.println("==================================================");
		System.out.println("[DIAGNOSTIC] MONGODB_URI env var present: " + isPresent);
		if (isPresent) {
			System.out.println("[DIAGNOSTIC] Connection target / host: " + extractHostOnly(mongoUri));
		}
		System.out.println("==================================================");
	}

	private static String extractHostOnly(String uri) {
		try {
			// Strip scheme (e.g. mongodb:// or mongodb+srv://)
			String withoutScheme = uri.contains("://") ? uri.substring(uri.indexOf("://") + 3) : uri;
			// Strip userinfo/credentials (everything before '@')
			String hostAndRest = withoutScheme.contains("@") ? withoutScheme.substring(withoutScheme.indexOf("@") + 1) : withoutScheme;
			// Strip database path and query parameters (everything after '/' or '?')
			int endIdx = hostAndRest.length();
			int slashIdx = hostAndRest.indexOf('/');
			int questionIdx = hostAndRest.indexOf('?');
			if (slashIdx != -1) {
				endIdx = Math.min(endIdx, slashIdx);
			}
			if (questionIdx != -1) {
				endIdx = Math.min(endIdx, questionIdx);
			}
			return hostAndRest.substring(0, endIdx);
		} catch (Exception e) {
			return "<unable to parse host>";
		}
	}

}

