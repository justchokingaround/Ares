package de.tum.in.test.api;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.ExtendWith;

import de.tum.in.test.api.locked.ArtemisTestGuard;

/**
 * Extends a {@link Deadline} for a {@link HiddenTest}, is optional. This can be
 * applied both to the test class and the test method. If the method does not
 * specify a {@link Deadline}, both two {@link ExtendedDeadline}s may be
 * combined. A test annotated with {@link ExtendedDeadline} must be hidden.
 * <p>
 * The format has to be like (spaces in between required)
 * <ul>
 * <li><code>2d</code></li>
 * <li><code>12h 30m</code></li>
 * <li><code>1d 5h</code></li>
 * <li><code>1d 5h 8m</code></li>
 * </ul>
 * 
 * @see Deadline
 * @author Christian Femers
 * @since 0.1.0
 * @version 1.0.0
 */
@API(status = Status.MAINTAINED)
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@ExtendWith(ArtemisTestGuard.class)
public @interface ExtendedDeadline {
	String value();
}
