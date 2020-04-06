package de.tum.in.test.api.internal.sanitization;

import static de.tum.in.test.api.util.BlacklistedInvoker.invoke;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.in.test.api.security.ArtemisSecurityManager;
import de.tum.in.test.api.util.IgnorantUnmodifiableList;
import de.tum.in.test.api.util.UnexpectedExceptionError;

@API(status = Status.INTERNAL)
public final class ThrowableSanitizer {

	private ThrowableSanitizer() {
		// static methods only
	}

	private static final List<SpecificThrowableSanitizer> SANITIZERS = List.of(SimpleThrowableSanitizer.INSTANCE,
			AssertionFailedErrorSanitizer.INSTANCE, MultipleFailuresErrorSanitizer.INSTANCE,
			MultipleAssertionsErrorSanitizer.INSTANCE, ExceptionInInitializerErrorSanitizer.INSTANCE,
			SoftAssertionErrorSanitizer.INSTANCE);

	private static final Field STACKTRACE;
	private static final Field CAUSE;
	private static final Field SUPPRESSED;
	static {
		try {
			STACKTRACE = Throwable.class.getDeclaredField("stackTrace");
			STACKTRACE.setAccessible(true);
			CAUSE = Throwable.class.getDeclaredField("cause");
			CAUSE.setAccessible(true);
			SUPPRESSED = Throwable.class.getDeclaredField("suppressedExceptions");
			SUPPRESSED.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static Throwable sanitize(final Throwable t) throws SanitizationError {
		if (t == null)
			return null;
		// use synchronized to prevent modification of t during sanitization
		synchronized (t) {
			if (UnexpectedExceptionError.class.equals(t.getClass()))
				return t;
			var firstPossibleSan = SANITIZERS.stream().filter(s -> s.canSanitize(t)).findFirst();
			if (firstPossibleSan.isPresent())
				return firstPossibleSan.get().sanitize(t);
			return UnexpectedExceptionError.wrap(t);
		}
	}

	static void copyThrowableInfo(Throwable from, Throwable to) throws SanitizationError {
		to.setStackTrace(from.getStackTrace());
		try {
			Throwable cause = (Throwable) CAUSE.get(from);
			List<Throwable> suppr = IgnorantUnmodifiableList.wrapWith(Arrays.stream(invoke(from::getSuppressed))
					.map(ThrowableSanitizer::sanitize).collect(Collectors.toList()),
					ArtemisSecurityManager.getOnSuppressedModification());
			if (cause == from)
				CAUSE.set(to, to);
			else
				CAUSE.set(to, sanitize(cause));
			SUPPRESSED.set(to, suppr);
		} catch (IllegalArgumentException | ReflectiveOperationException e) {
			throw new SanitizationError(e);
		}
	}
}