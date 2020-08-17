package de.tum.in.testuser.subject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.xyz.Circumvention;
import org.apache.xyz.MaliciousExceptionB;

import de.tum.in.test.api.TestUtils;
import de.tum.in.test.api.security.ArtemisSecurityManager;

public class Penguin extends MiniJava {

	public static void main(String[] args) {
		write("Pinguine sind die Besten!");
		write("Nieder mit den Eisbären!");
	}

	@SuppressWarnings("unused")
	public static void mäin(String[] args) {
		int a = readInt("Zahl eingeben:");
		int b = a * a;
		write("Ausgabe:");
		write(b);
	}

	@SuppressWarnings("unused")
	public static void readTwoTimes() {
		int a = readInt("Zahl eingeben:");
		int b = readInt("Nächste");
		write("Ausgabe:");
	}

	public static void useReflection() {
		try {
			Class.forName("de.tum.in.test.api.io.IOTester").getDeclaredFields()[0].setAccessible(true);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void useReflection2() {
		Thread d = new Circumvention();
		d.start();
		try {
			d.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void accessPath(Path p) throws IOException {
		Files.readString(p);
	}

	public static void maliciousExceptionA() {
		throw new MaliciousExceptionA();
	}

	public static boolean maliciousExceptionB() {
		AtomicBoolean ab = new AtomicBoolean();
		try {
			throw new MaliciousExceptionB(ab);
		} catch (@SuppressWarnings("unused") SecurityException e) {
			// nothing
		}
		return ab.get();
	}

	@SuppressWarnings("resource")
	public static String tryExecuteGit() {
		try {
			return new String(Runtime.getRuntime().exec("git --help").getInputStream().readAllBytes())
					.replaceAll("\\W| ", "_");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void tryBreakThreadGroup() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		for (;;) {
			ThreadGroup parent = threadGroup.getParent();
			if (parent == null)
				break;
			threadGroup = parent;
		}
		new Thread(threadGroup, () -> {
			// nothing
		}).start();
	}

	public static boolean tryEvilPermission() {
		AtomicBoolean ab = new AtomicBoolean();
		try {
			System.getSecurityManager().checkPermission(new Permission("setIO") {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean implies(Permission permission) {
					return false;
				}

				@Override
				public int hashCode() {
					return 0;
				}

				@Override
				public String getActions() {
					return null;
				}

				@Override
				public boolean equals(Object obj) {
					return false;
				}

				@Override
				public String toString() {
					try {
						accessPath(Path.of("pom.xml"));
						ab.set(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return super.toString();
				}
			});
		} catch (@SuppressWarnings("unused") SecurityException e) {
			// do nothing
		}
		return ab.get();
	}

	public static void spawnEndlessThreads() {
		try {
			Thread.sleep(2);
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			// nothing
		}
		for (int i = 0; i < 2000; i++) {
			Thread t = new Thread(Penguin::spawnEndlessThreads);
			t.start();
		}
	}

	public static void tryThreadWhitelisting() throws Throwable {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread t = new Thread(() -> Path.of("pom.xml").toFile().canWrite());
		ArtemisSecurityManager.requestThreadWhitelisting(t);
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				failure.set(e);
			}
		});
		t.start();
		t.join();
		if (failure.get() != null)
			throw failure.get();
	}

	public static void throwNullPointerException() {
		throw new NullPointerException("xyz");
	}

	public static void throwPrivilegedNullPointerException() {
		TestUtils.privilegedThrow((Runnable) () -> {
			throw new NullPointerException("xyz");
		});
	}
}